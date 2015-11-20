package edu.zju.cadal.dataset;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import edu.zju.cadal.exception.DatasetFormatErrorException;
import edu.zju.cadal.model.Annotation;
import edu.zju.cadal.model.Candidate;
import edu.zju.cadal.model.Entity;
import edu.zju.cadal.model.Mention;
import edu.zju.cadal.model.NIL;
import edu.zju.cadal.utils.Pair;

/**
 * @author:chenhui 
 * @email:chenhuicn@126.com
 * @date:2015年11月15日
 */
public class MSNBC extends AbstractDataset{
	private static Pattern wikiUrlPattern = Pattern.compile("http://en.wikipedia.org/wiki/(.*?)\"?");
	private Map<String, String> rawText = new HashMap<String, String>();
	private Map<String, Set<Annotation>> goldAnnotation = new HashMap<String, Set<Annotation>>();
	private Map<String, Set<Mention>> goldMention = new HashMap<String, Set<Mention>>();
	private Map<String, Set<NIL>> goldNIL = new HashMap<String, Set<NIL>>();
	private Map<String, Set<Candidate>> goldCandidate = new HashMap<String, Set<Candidate>>();
	private Map<String, Set<Entity>> goldEntity = new HashMap<String, Set<Entity>>();
	
	public MSNBC(String rawTextFolder, String problemFolder) {
		try {
			loadRawText(rawTextFolder);
			Map<String, Set<Problem>> problemMap = loadProblem(problemFolder);
			filling(problemMap);
		} catch (IOException | DOMException | XPathExpressionException | ParserConfigurationException | SAXException | DatasetFormatErrorException e) {
			e.printStackTrace();
		}
	}
	
	private void loadRawText(String textFolder) throws IOException {
		File folder = new File(textFolder);
		if (folder.isDirectory() == false)
			throw new RuntimeException("The Parameter Must Be a Folder");
		File[] files = folder.listFiles();
		for (File f : files) {
			if (f.isFile() == true) {
				BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(f), Charset.forName("UTF-8")));
				String line = null;
				StringBuffer text = new StringBuffer();
				while ((line = br.readLine()) != null)
					text.append(line + '\n');
				br.close();
				this.rawText.put(f.getName(), text.toString());
			}
		}
	}
	
	private Map<String, Set<Problem>> loadProblem(String problemFolder) 
			throws DOMException, ParserConfigurationException, SAXException, IOException, DatasetFormatErrorException, XPathExpressionException {
		
		Map<String, Set<Problem>> problemMap = new HashMap<String, Set<Problem>>();
		File folder = new File(problemFolder);
		if (folder.isDirectory() == false)
			throw new RuntimeException("The Parameter Must Be a Folder");
		
		File[] files = folder.listFiles();
		for (File f : files) {
			if (f.isFile() == true) {
				Set<Problem> problemSet = new HashSet<Problem>();
				DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
				DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
				Document doc = dBuilder.parse(f);
				doc.getDocumentElement().normalize();
				String docName = doc.getElementsByTagName("ReferenceFileName").item(0).getTextContent().trim(); 
				NodeList nList = doc.getElementsByTagName("ReferenceInstance");
				for (int i = 0; i < nList.getLength(); i++) {
					Node nNode = nList.item(i);
					if (nNode.getNodeType() == Node.ELEMENT_NODE) {
						Element eElement = (Element) nNode;
						NodeList annData = eElement.getChildNodes();
						String surfaceForm = null;
						int position = -1;
						int length = -1;
						String title = null;
						for (int j = 0; j < annData.getLength(); j++) 
						{
							Node dataNode = annData.item(j);
							if (dataNode.getNodeType() == Node.ELEMENT_NODE) 
							{
								Element dataElement = (Element) dataNode;
								if (dataElement.getTagName().equals("SurfaceForm"))
									surfaceForm = dataElement.getTextContent().trim();
								if (dataElement.getTagName().equals("Offset"))
									position = Integer.parseInt(dataElement.getTextContent().trim());
								if (dataElement.getTagName().equals("Length"))
									length = Integer.parseInt(dataElement.getTextContent().trim());
								if (dataElement.getTagName().equals("ChosenAnnotation")) 
								{
									String concept = URLDecoder.decode(dataElement.getTextContent().trim().replace('_', ' '), "UTF-8");
									Matcher m = wikiUrlPattern.matcher(concept);
									if (m.matches())
										title = m.group(1);
									else if (concept.equals("*null*"))
										title = "*null*";
									else
										throw new DatasetFormatErrorException(this.getName());
								}
							}
						}
						if (title != null)
							problemSet.add(new Problem(surfaceForm, position, length, title));
					} 
				} // for
				problemMap.put(docName, problemSet);
			} //if 
		}//for
		
		//查询title对应的wid，填入缓存
		List<String> titleToPrefetch = new ArrayList<String>();
		for(Set<Problem> problemSet : problemMap.values())
			for (Problem p : problemSet) 
				titleToPrefetch.add(p.title);
		api.prefetchTitle(titleToPrefetch);
		api.flush();
		return problemMap;
	}
	
	/**
	 * 通过problemMap，把各个成员变量的值进行填充
	 * @param problemMap
	 * @throws XPathExpressionException
	 * @throws IOException
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 */
	private void filling(Map<String, Set<Problem>> problemMap) throws XPathExpressionException, IOException, ParserConfigurationException, SAXException {
		for (String title : problemMap.keySet()) {
			Set<Problem> problemSet = problemMap.get(title);
			Set<Annotation> annotationSet = new HashSet<Annotation>();
			Set<NIL> NILSet = new HashSet<NIL>();
			Set<Mention> mentionSet = new HashSet<Mention>();
			Set<Candidate> candidateSet = new HashSet<Candidate>();
			Set<Entity> entitySet = new HashSet<Entity>();
			
			for (Problem p : problemSet) {
				Mention m = new Mention(p.surfaceForm, p.position, p.length);
				mentionSet.add(m);
				int wid = api.getIdByTitle(p.title);
				if (wid == -1) {
					NILSet.add(new NIL(m));
					Entity e = new Entity(0, "*null*");
					entitySet.add(e);
					Set<Pair<Entity, Float>> pairSet = new HashSet<Pair<Entity, Float>>();
					pairSet.add(new Pair<Entity, Float>(e, 1.0f));
					candidateSet.add(new Candidate(m, pairSet));
				}
				else {
					annotationSet.add(new Annotation(m, new Entity(wid, p.title)));
					Entity e = new Entity(wid, p.title);
					entitySet.add(e);
					Set<Pair<Entity, Float>> pairSet = new HashSet<Pair<Entity, Float>>();
					pairSet.add(new Pair<Entity, Float>(e, 1.0f));
					candidateSet.add(new Candidate(m, pairSet));
				}
			}
			
			this.goldAnnotation.put(title, annotationSet);
			this.goldMention.put(title, mentionSet);
			this.goldCandidate.put(title, candidateSet);
			this.goldEntity.put(title, entitySet);
			this.goldNIL.put(title, NILSet);
		}
	}
	
	
	@Override
	public String getName() {
		return "MSNBC";
	}

	@Override
	public int getSize() {
		return this.rawText.size();
	}

	@Override
	public Map<String, String> getRawText() {
		return this.rawText;
	}

	@Override
	public Map<String, Set<Mention>> getGoldMention() {
		return this.goldMention;
	}

	@Override
	public Map<String, Set<Annotation>> getGoldAnnotation() {
		return this.goldAnnotation;
	}

	@Override
	public Map<String, Set<NIL>> getGoldNIL() {
		return this.goldNIL;
	}

	@Override
	public Map<String, Set<Candidate>> getGoldCandidate() {
		return this.goldCandidate;
	}

	@Override
	public Map<String, Set<Entity>> getGoldEntity() {
		return this.goldEntity;
	}
	
	private class Problem {
		public String surfaceForm;
		public int position, length;
		public String title;
		
		public Problem(String surfaceForm, int position, int length, String title) {
			this.surfaceForm = surfaceForm;
			this.position = position;
			this.length = length;
			this.title = title;
		}
	}
}
