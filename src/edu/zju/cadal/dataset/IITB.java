package edu.zju.cadal.dataset;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

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
 * @date:2015年11月26日
 */
public class IITB extends AbstractDataset{
	private Map<String, String> rawText = new HashMap<String, String>();
	private Map<String, Set<Annotation>> goldAnnotation = new HashMap<String, Set<Annotation>>();
	private Map<String, Set<Mention>> goldMention = new HashMap<String, Set<Mention>>();
	private Map<String, Set<NIL>> goldNIL = new HashMap<String, Set<NIL>>();
	private Map<String, Set<Candidate>> goldCandidate = new HashMap<String, Set<Candidate>>();
	private Map<String, Set<Entity>> goldEntity = new HashMap<String, Set<Entity>>();

	public IITB(String textsFolder, String problemFilePath) {
		try {
			loadRawText(textsFolder);
			Map<String, Set<IITBAnnotation>> problemMap = loadAnnotations(problemFilePath);
			filling(problemMap);
			deleteEmptyDocument();			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void filling(Map<String, Set<IITBAnnotation>> problemMap) throws XPathExpressionException, IOException, ParserConfigurationException, SAXException {
		for (String title : problemMap.keySet()) {
			Set<IITBAnnotation> problemSet = problemMap.get(title);
			Set<Annotation> annotationSet = new HashSet<Annotation>();
			Set<NIL> NILSet = new HashSet<NIL>();
			Set<Mention> mentionSet = new HashSet<Mention>();
			Set<Candidate> candidateSet = new HashSet<Candidate>();
			Set<Entity> entitySet = new HashSet<Entity>();
			
			for (IITBAnnotation p : problemSet) {
				Mention m = new Mention(p.surfaceForm, p.position, p.length);
				if (mentionSet.contains(m))
					continue;
				
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

	private void deleteEmptyDocument() {
		Set<String> empty = new HashSet<String>();
		
		for (String title : goldMention.keySet())
			if (goldMention.get(title).isEmpty() == true)
				empty.add(title);
		
		for (String title : empty) {
			rawText.remove(title);
			goldMention.remove(title);
			goldAnnotation.remove(title);
			goldCandidate.remove(title);
			goldEntity.remove(title);
			goldNIL.remove(title);
		}
	}
	
	public void loadRawText(String textsFolder) throws IOException {
		File[] textFiles = new File(textsFolder).listFiles();
		for (File file : textFiles)
			if (file.isFile() && file.getName().endsWith("allUrls.txt.txt") == false) {
				BufferedReader r = new BufferedReader(new InputStreamReader(new FileInputStream(file), Charset.forName("UTF-8")));
				String line;
				String text = "";
				while ((line = r.readLine()) != null)
					text += line + "\n";
				r.close();
				rawText.put(file.getName().trim(), text);
			}
	}

	public Map<String, Set<IITBAnnotation>> loadAnnotations(String annsPath) throws Exception {
		Map<String, Set<IITBAnnotation>> problemMap = new HashMap<String, Set<IITBAnnotation>>();
		File tf = new File(annsPath);
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
		Document doc = dBuilder.parse(tf);
		doc.getDocumentElement().normalize();
		List<String> titlesToPrefetch = new Vector<String>();

		NodeList nList = doc.getElementsByTagName("annotation");
		for (int i = 0; i < nList.getLength(); i++) {
			Node nNode = nList.item(i);
			if (nNode.getNodeType() == Node.ELEMENT_NODE){
				Element eElement = (Element) nNode;
				NodeList annData = eElement.getChildNodes();
				int position = -1;
				int length = -1;
				String wikiName = null;
				String docName = null;
				for (int j = 0; j < annData.getLength(); j++) {
					Node dataNode = annData.item(j);
					if (dataNode.getNodeType() == Node.ELEMENT_NODE){
						Element dataElement = (Element) dataNode;
						if (dataElement.getTagName().equals("offset"))
							position = Integer.parseInt(dataElement.getTextContent().trim());
						if (dataElement.getTagName().equals("length"))
							length = Integer.parseInt(dataElement.getTextContent().trim());
						if (dataElement.getTagName().equals("wikiName"))
							wikiName = dataElement.getTextContent().trim(); 
						if (dataElement.getTagName().equals("docName"))
							docName = dataElement.getTextContent().trim().toString();
					}
				}
				if (wikiName != null && length>0 && position>=0 && docName != null){
					if (!problemMap.containsKey(docName))
						problemMap.put(docName, new HashSet<IITBAnnotation>());
					
					if (wikiName.equals("") == false){//NIL
						problemMap.get(docName).add(
								new IITBAnnotation(
										rawText.get(docName).substring(position, position+length),
										position, 
										length, 
										wikiName));
						titlesToPrefetch.add(wikiName);
					} else {
						problemMap.get(docName).add(
								new IITBAnnotation(
										rawText.get(docName).substring(position, position+length),
										position, 
										length, 
										"*null*"));
					}
				}
				else
					throw new DatasetFormatErrorException(this.getName());
			}
		}
		/**prefetch all Wikipedia-ids for the titles found */
		api.prefetchTitle(titlesToPrefetch);
		api.flush();
		return problemMap;
	}

	private static class IITBAnnotation {
		public IITBAnnotation(String sorfaceForm, int position, int length, String title) {
			this.position = position;
			this.length = length;
			this.title = title;
		}
		public int position, length;
		public String title, surfaceForm;
	}
	
	@Override
	public String getName() {
		return "IITB";
	}

	@Override
	public int getSize() {
		return rawText.size();
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

}
