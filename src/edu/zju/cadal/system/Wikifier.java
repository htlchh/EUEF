package edu.zju.cadal.system;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import edu.zju.cadal.cache.SystemResult;
import edu.zju.cadal.dataset.AbstractDataset;
import edu.zju.cadal.model.Annotation;
import edu.zju.cadal.model.Candidate;
import edu.zju.cadal.model.Entity;
import edu.zju.cadal.model.Mention;
import edu.zju.cadal.model.NIL;
import edu.zju.cadal.utils.Pair;
import edu.zju.cadal.utils.Timer;

/**
 * @author:chenhui 
 * @email:chenhuicn@126.com
 * @date:2015年11月18日
 * 
 * 
 * 这个ERD系统是通过加载本地程序执行的结果来进行ERD
 * 因此需要提供Wikifier本地执行后的结果文件
 */
public class Wikifier extends AbstractERDSystem {

	private final String resultBaseDirectory = "wikifier/";
	private final String suffix = ".wikification.tagged.full.xml";
	private boolean useCache = false;
	
	public Wikifier(boolean useCache) {
		this.useCache = useCache;
	}
	
	@Override
	public String getName() {
		return "Wikifier";
	}

	@Override
	public SystemResult erd(AbstractDataset ds) {
		SystemResult result = SystemResult.getInstance(useCache);
		//有缓存，直接返回
		if (result.isCached(this.getName(), ds.getName()))
			return result;
		Timer timer = new Timer();
		Map<String, String> rawTextMap = ds.getRawText();
		Map<String, Set<Mention>> mentionMap = new HashMap<String, Set<Mention>>();
		Map<String, Set<Candidate>> candidateMap = new HashMap<String, Set<Candidate>>();
		Map<String, Set<Annotation>> annotationMap = new HashMap<String, Set<Annotation>>();
		Map<String, Set<Entity>> entityMap = new HashMap<String, Set<Entity>>();
		Map<String, Set<NIL>> NILMap = new HashMap<String, Set<NIL>>();
		Map<String, Long> costTime = new HashMap<String, Long>();
		
		
		for (String title : rawTextMap.keySet()) {
			System.out.println("Processing document " + title + " ...");
			Set<Mention> mentionSet = new HashSet<Mention>();
			Set<Candidate> candidateSet = new HashSet<Candidate>();
			Set<Entity> entitySet = new HashSet<Entity>();
			Set<Annotation> annotationSet = new HashSet<Annotation>();
			Set<NIL> NILSet = new HashSet<NIL>();
			
			executor(
					resultBaseDirectory + ds.getName() + "/" + title + suffix, 
					timer, 
					mentionSet, 
					candidateSet, 
					entitySet, 
					annotationSet, 
					NILSet);
			
			mentionMap.put(title, mentionSet);
			candidateMap.put(title, candidateSet);
			annotationMap.put(title, annotationSet);
			entityMap.put(title, entitySet);
			NILMap.put(title, NILSet);
			costTime.put(title, timer.getCostTime());
		}
		
		result.setMentionCache(this.getName(), ds.getName(), mentionMap);
		result.setCandidateCache(getName(), ds.getName(), candidateMap);
		result.setAnnotationCache(getName(), ds.getName(), annotationMap);
		result.setNILCache(getName(), ds.getName(), NILMap);
		result.setEntityCache(getName(), ds.getName(), entityMap);
		result.setCostTime(getName(), ds.getName(), costTime);
		result.flush();
		return result;		
	}
	
	private void executor(String resultPath, 
			Timer timer, 
			Set<Mention> mentionSet,
			Set<Candidate> candidateSet,
			Set<Entity> entitySet,
			Set<Annotation> annotationSet,
			Set<NIL> NILSet
			) 
	{
		String text = this.readDocument(new File(resultPath));
		String[] ss = this.split(text);
		String title = ss[0];
		String inputText = ss[1];
		String entityText = ss[2];
		try {
			parse(entityText, mentionSet, candidateSet, entitySet, annotationSet);
		} catch (ParserConfigurationException | SAXException | IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 对Wikifier不进行时间统计
	 * Wikifier不输出NIL
	 * @param entityText
	 * @param mentionSet
	 * @param candidateSet
	 * @param entitySet
	 * @param annotationSet
	 * @throws ParserConfigurationException 
	 * @throws IOException 
	 * @throws SAXException 
	 */
	private void parse(String entityText, 
			Set<Mention> mentionSet,
			Set<Candidate> candidateSet,
			Set<Entity> entitySet,
			Set<Annotation> annotationSet) throws ParserConfigurationException, SAXException, IOException {
		
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		Document doc = builder.parse(new InputSource(new StringReader(entityText)));
		Element root = doc.getDocumentElement();	
		NodeList entityNodes = root.getChildNodes();
		
		for (int i = 0; i < entityNodes.getLength(); i++) {
			Node entityNode = entityNodes.item(i);
			if (entityNode.getNodeType() == Node.ELEMENT_NODE && entityNode.getNodeName().equals("Entity")) {
				String surfaceForm = null;
				int position = -1;
				int length = -1;
				float linkerScore = -1;
				float rankerScore = -1;
				int wid = -1;
				String wikiTitle = null;
				Set<Pair<Entity, Float>> pairSet = new HashSet<Pair<Entity,Float>>();
				Mention mention = null;
				Entity entity = null;
				Annotation annotation = null;
				Candidate candidate = null;
				
				NodeList childrenOfEntity = entityNode.getChildNodes();
				for (int j = 0; j < childrenOfEntity.getLength(); j++) {
					Node child = childrenOfEntity.item(j);
					if (child.getNodeType() == Node.ELEMENT_NODE && child.getNodeName().equals("EntitySurfaceForm"))
						surfaceForm = child.getTextContent().trim();
					if (child.getNodeType() == Node.ELEMENT_NODE && child.getNodeName().equals("EntityTextStart"))
						position = Integer.parseInt(child.getTextContent().trim());
					if (child.getNodeType() == Node.ELEMENT_NODE && child.getNodeName().equals("EntityTextEnd"))
						length = Integer.parseInt(child.getTextContent().trim()) - position;
					if (child.getNodeType() == Node.ELEMENT_NODE && child.getNodeName().equals("LinkerScore"))
						linkerScore = Float.parseFloat(child.getTextContent().trim());
					//填充mention
					if (surfaceForm != null && position != -1 && length != -1 && linkerScore != -1) {
						if (linkerScore < 0) {
							System.out.println(linkerScore);
							linkerScore = 0;
						}
						mention = new Mention(surfaceForm, position, length, linkerScore);
					}	
						
					
					if (child.getNodeType() == Node.ELEMENT_NODE && child.getNodeName().equals("TopDisambiguation")) {
						NodeList childrenOfDis = child.getChildNodes();
						for (int k = 0; k < childrenOfDis.getLength(); k++) {
							Node disChild = childrenOfDis.item(k);
							if (disChild.getNodeType() == Node.ELEMENT_NODE && disChild.getNodeName().equals("WikiTitle"))
								wikiTitle = disChild.getTextContent().trim();
							if (disChild.getNodeType() == Node.ELEMENT_NODE && disChild.getNodeName().equals("WikiTitleID"))
								wid = Integer.parseInt(disChild.getTextContent().trim());
							if (disChild.getNodeType() == Node.ELEMENT_NODE && disChild.getNodeName().equals("RankerScore"))
								rankerScore = Float.parseFloat(disChild.getTextContent().trim());
						}
					}
					//填充entity
					if (wid != -1 && wikiTitle != null)
						entity = new Entity(wid, wikiTitle);
					//填充annotation
					if (mention != null && entity != null && rankerScore != -1) {
						annotation = new Annotation(mention, entity, rankerScore);
						pairSet.add(new Pair<Entity, Float>(entity, rankerScore));
					}
					
					/** candidates **/
					if (child.getNodeType() == Node.ELEMENT_NODE && child.getNodeName().equals("DisambiguationCandidates")) {
						NodeList childrenOfCandidates = child.getChildNodes();
						for (int k = 0; k < childrenOfCandidates.getLength(); k++) {
							Node cchild = childrenOfCandidates.item(k);
							if (cchild.getNodeType() == Node.ELEMENT_NODE && cchild.getNodeName().equals("Candidate")) {
								NodeList childrenOfCChild = cchild.getChildNodes();
								float s = -1;
								int id = -1;
								String title = null;
								for (int m = 0; m < childrenOfCChild.getLength(); m++) {
									if (childrenOfCChild.item(m).getNodeType() == Node.ELEMENT_NODE &&
											childrenOfCChild.item(m).getNodeName().equals("WikiTitle"))
										title = childrenOfCChild.item(m).getTextContent().trim();
									if (childrenOfCChild.item(m).getNodeType() == Node.ELEMENT_NODE && 
											childrenOfCChild.item(m).getNodeName().equals("WikiTitleID"))
										id = Integer.parseInt(childrenOfCChild.item(m).getTextContent().trim());
									if (childrenOfCChild.item(m).getNodeType() == Node.ELEMENT_NODE && 
											childrenOfCChild.item(m).getNodeName().equals("RankerScore"))
										s = Float.parseFloat(childrenOfCChild.item(m).getTextContent().trim());
								}
								//填充candidate的pair
								if (id != -1 && title != null && s != -1)
									pairSet.add(new Pair<Entity, Float>(new Entity(id, title), s));
							}
								
						}
					} //if 
					
					if (mention != null && pairSet.isEmpty() == false)
						candidate = new Candidate(mention, pairSet);
				} //for
				mentionSet.add(mention);
				candidateSet.add(candidate);
				entitySet.add(entity);
				annotationSet.add(annotation);
			} //if 
		}//for
	}
	
	/**
	 * 把结果文件进行切分
	 * 
	 * @param text
	 * @return
	 */
	private String[] split(String text) {
		String title = text.substring(
				text.indexOf("<InputFilename>")+16,
				text.indexOf("</InputFilename>")-1); //-1 to filter \n
		String intputText = text.substring(
				text.indexOf("<InputText>")+12, 
				text.indexOf("</InputText>")-1);
		String entities = text.substring(
				text.indexOf("<WikifiedEntities>"), 
				text.indexOf("</WikifiedEntities>")+19);
		return new String[]{title, intputText, entities};
	}
	
	private String readDocument(File f)
	{
		StringBuffer document = new StringBuffer();
		String line = null;
		BufferedReader br = null;
		try {
			br = new BufferedReader(new InputStreamReader(new FileInputStream(f), "UTF-8"));
			while (true) {
				line = br.readLine();
				if (line == null) break;
				document.append(line + "\n");
			}
			br.close();
			return document.toString();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}	
}
