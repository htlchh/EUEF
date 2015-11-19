package edu.zju.cadal.system;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
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
 * @date:2015年11月16日
 */
public class WikiMiner extends AbstractERDSystem{

	private String baseURL = "http://wikipedia-miner.cms.waikato.ac.nz/services/wikify";
	private boolean useCache = false;
	
	public WikiMiner(boolean useCache) {
		this.useCache = useCache;
	}
	
	@Override
	public String getName() {
		return "WikiMiner";
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
			executor(rawTextMap.get(title), timer, mentionSet, candidateSet, entitySet, annotationSet, NILSet);
			
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
	
	private void executor(String text, 
							Timer timer, 
							Set<Mention> mentionSet,
							Set<Candidate> candidateSet,
							Set<Entity> entitySet,
							Set<Annotation> annotationSet,
							Set<NIL> NILSet
							) 
	{
		try {
			URL url = new URL(baseURL);
			long currentTime = Calendar.getInstance().getTimeInMillis();
			String parameters = "references=true&repeatMode=all&minProbability=0.0&source=" + URLEncoder.encode(text, "UTF-8");
			HttpURLConnection connection = (HttpURLConnection)url.openConnection();
			connection.setRequestProperty("accept", "text/xml");
			connection.setDoOutput(true);
			connection.setDoInput(true);
			connection.setRequestMethod("GET");
			connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded"); 
			connection.setRequestProperty("charset", "utf-8");
			connection.setRequestProperty("Content-Length", "" + Integer.toString(parameters.getBytes().length));
			connection.setUseCaches (false);
			DataOutputStream wr = new DataOutputStream(connection.getOutputStream ());
			wr.writeBytes(parameters);
			wr.flush();
			wr.close();			
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document doc = builder.parse(connection.getInputStream());
			
			//返回结果，计时结束
			timer.setCostTime(Calendar.getInstance().getTimeInMillis()-currentTime);
			
			XPathFactory xPathfactory = XPathFactory.newInstance();
			XPath xpath = xPathfactory.newXPath();
			XPathExpression idExpr = xpath.compile("//detectedTopic/@id");
			XPathExpression titleExpr = xpath.compile("//detectedTopic/@title");
			XPathExpression weightExpr = xpath.compile("//detectedTopic/@weight");
			XPathExpression referenceExpr = xpath.compile("//detectedTopic/references");

			NodeList ids = (NodeList) idExpr.evaluate(doc, XPathConstants.NODESET);
			NodeList titles = (NodeList) titleExpr.evaluate(doc, XPathConstants.NODESET);
			NodeList weights = (NodeList) weightExpr.evaluate(doc, XPathConstants.NODESET);
			NodeList references = (NodeList) referenceExpr.evaluate(doc, XPathConstants.NODESET);

			for (int i = 0; i < weights.getLength(); i++) 
			{
				if (weights.item(i).getNodeType() != Node.TEXT_NODE) 
				{
					int id = Integer.parseInt(ids.item(i).getNodeValue());
					String title = titles.item(i).getNodeValue();
					float weight = Float.parseFloat(weights.item(i).getNodeValue());
					XPathExpression startExpr = xpath.compile("//detectedTopic[@id="+id+"]/references/reference/@start");
					XPathExpression endExpr = xpath.compile("//detectedTopic[@id="+id+"]/references/reference/@end");
					NodeList starts = (NodeList) startExpr.evaluate(references.item(i), XPathConstants.NODESET);
					NodeList ends = (NodeList) endExpr.evaluate(references.item(i), XPathConstants.NODESET);
					for (int j = 0; j < starts.getLength(); j++) 
					{
						int start = Integer.parseInt(starts.item(j).getNodeValue());
						int end = Integer.parseInt(ends.item(j).getNodeValue());
						int length = end - start;
						//填充变量
						Mention m = new Mention(start, length, weight);
						Entity e = new Entity(id, title);
						Set<Pair<Entity, Float>> pairSet = new HashSet<Pair<Entity, Float>>();
						pairSet.add(new Pair<Entity, Float>(e, weight));
						Candidate c = new Candidate(m, pairSet);
						Annotation a = new Annotation(m, e, weight);
						
						mentionSet.add(m);
						candidateSet.add(c);
						entitySet.add(e);
						annotationSet.add(a);
					}
				}

			}			
		} catch (IOException | ParserConfigurationException | SAXException | XPathExpressionException e) {
			e.printStackTrace();
		}
	}
}
