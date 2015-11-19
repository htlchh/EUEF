package edu.zju.cadal.system;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import edu.zju.cadal.cache.SystemResult;
import edu.zju.cadal.dataset.AbstractDataset;
import edu.zju.cadal.model.Annotation;
import edu.zju.cadal.model.Candidate;
import edu.zju.cadal.model.Entity;
import edu.zju.cadal.model.Mention;
import edu.zju.cadal.model.NIL;
import edu.zju.cadal.utils.Pair;
import edu.zju.cadal.utils.Timer;
import edu.zju.cadal.webservice.MediaWikiAPI;

/**
 * @author:chenhui 
 * @email:chenhuicn@126.com
 * @date:2015年11月18日
 */
public class Spotlight extends AbstractERDSystem{

	private boolean useCache = false;
	private final DisambiguationPolicy disambiguator;
	private DBPediaAPI dbpediaApi = new DBPediaAPI();
	private MediaWikiAPI mediaWikiApi = MediaWikiAPI.getInstance();
	private final String host;
	private final int port;
	
	public Spotlight(
			boolean useCache, 
			DisambiguationPolicy disambiguator, 
			String host, 
			int port) 
	{
		this.useCache = useCache;
		this.disambiguator = disambiguator;
		this.host = host;
		this.port = port;
	}
	
	public Spotlight(boolean useCache) {
		this(useCache, DisambiguationPolicy.Default, "spotlight.dbpedia.org", 80);
	}
	
	@Override
	public String getName() {
		return "Spotlight";
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
			executor(rawTextMap.get(title), Service.ANNOTATE, timer, mentionSet, candidateSet, entitySet, annotationSet, NILSet);
			
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
			Service service, 
			Timer timer, 
			Set<Mention> mentionSet,
			Set<Candidate> candidateSet,
			Set<Entity> entitySet,
			Set<Annotation> annotationSet,
			Set<NIL> nILSet
			) {
		
		//dbpedia spotlight cannot handle documents made only of whitespaces...
		//过滤全是空格的text
		int i = 0;
		while (i < text.length()
				&& (text.charAt(i) == ' ' || text.charAt(i) == '\n'))
			i++;
		if (i == text.length())
			return;

		Pattern dbPediaUri = Pattern.compile("^http://dbpedia.org/resource/(.*)$");
		HashSet<String> toPrefetch = new HashSet<String>();
		HashSet<SpotLightAnnotation> res = new HashSet<SpotLightAnnotation>();
		try {
			long currentTime = Calendar.getInstance().getTimeInMillis();
			URL url = new URL("http://" + host + ":" + port + "/rest/" + service);
			String parameters = "disambiguator=" + disambiguator + "&confidence=0&support=0&text=" + URLEncoder.encode(text, "UTF-8");
//			System.out.println(url.toString());
			HttpURLConnection slConnection = (HttpURLConnection) url.openConnection();
			slConnection.setRequestProperty("accept", "text/xml");
			slConnection.setDoOutput(true);
			slConnection.setDoInput(true);
			slConnection.setRequestMethod("POST");
			slConnection.setRequestProperty("Content-Type",	"application/x-www-form-urlencoded");
			slConnection.setRequestProperty("charset", "utf-8");
			slConnection.setRequestProperty("Content-Length", "" + Integer.toString(parameters.getBytes().length));
			slConnection.setUseCaches(false);

			DataOutputStream wr = new DataOutputStream(slConnection.getOutputStream());
			wr.writeBytes(parameters);
			wr.flush();
			wr.close();

			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document doc = null;
			try {
				doc = builder.parse(slConnection.getInputStream());
			} catch (IOException e) {
				System.out.println("Got Error While Querying: " + url + "?" + parameters);
				e.printStackTrace();
			}
			timer.setCostTime(Calendar.getInstance().getTimeInMillis() - currentTime);

			XPathFactory xPathfactory = XPathFactory.newInstance();
			XPath xpath = xPathfactory.newXPath();
			XPathExpression resourceExpr = xpath.compile("//Resource");

			NodeList resources = (NodeList) resourceExpr.evaluate(doc, XPathConstants.NODESET);
			for (int j = 0; j < resources.getLength(); j++) {
				Element currRes = (Element) resources.item(j);
				String uri = currRes.getAttribute("URI");
				int offset = Integer.parseInt(currRes.getAttribute("offset"));
				float score = Float.parseFloat(currRes.getAttribute("similarityScore"));
				String surface = currRes.getAttribute("surfaceForm");
				Matcher m = dbPediaUri.matcher(uri);
				if (m.matches()) {
					String resource = URLDecoder.decode(m.group(1), "UTF-8");
					res.add(new SpotLightAnnotation(surface, offset, surface.length(), resource, score));
					toPrefetch.add(resource);
				}

			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		// query DBPedia API and prefetch wikipedia title
		dbpediaApi.prefetch(toPrefetch);

		// query Wikipedia API and prefetch wikipedia id
		HashSet<String> wikiTitles = new HashSet<String>();
		for (String dbpTitle : toPrefetch)
			wikiTitles.add(dbpediaApi.dbpediaToWikipedia(dbpTitle));
		try {
			mediaWikiApi.prefetchTitle(new Vector<String>(wikiTitles));
		} catch (Exception e) {
			e.printStackTrace();
		}

		for (SpotLightAnnotation a : res) {
			String wikipediaTitle = dbpediaApi.dbpediaToWikipedia(a.resource);
			if (wikipediaTitle == null)
				continue;
			int wikipediaArticle = -1;
			try {
				//查询对应的wikiepdia的ID
				wikipediaArticle = mediaWikiApi.getIdByTitle(wikipediaTitle);
			} catch (Exception e) {
				e.printStackTrace();
			}
			if (wikipediaArticle < 0) {
				System.out.println("ERROR: wrong binding: " + a.resource + " -> " + wikipediaTitle + " -> " + wikipediaArticle);
				continue;
			}
			//填充各个变量
			Mention m = new Mention(a.position, a.length);
			Entity e = new Entity(wikipediaArticle, a.resource);
			Annotation annotation = new Annotation(m, e, a.score);
			Set<Pair<Entity, Float>> pairSet = new HashSet<Pair<Entity, Float>>();
			Pair<Entity, Float> pair = new Pair<Entity, Float>(e, a.score);
			pairSet.add(pair);
			Candidate c = new Candidate(m, pairSet);
			
			mentionSet.add(m);
			entitySet.add(e);
			annotationSet.add(annotation);
			candidateSet.add(c);
		}
		
	}
	
	
	private static class SpotLightAnnotation {
		public SpotLightAnnotation(String surfaceForm, int position, int length, String resource, float score) {
			this.surfaceForm = surfaceForm;
			this.position = position;
			this.length = length;
			this.resource = resource;
			this.score = score;
		}
		public String surfaceForm;
		public float score;
		public int position, length;
		public String resource;
	}	
	
	private enum Service {
		ANNOTATE("annotate"), DISAMBIGUATE("disambiguate");

		private final String name;

		Service(String name) {
			this.name = name;
		}

		@Override
		public String toString() {
			return name;
		}
	}
	
	public enum DisambiguationPolicy {
		Document, Occurrences, CuttingEdge, Default, GraphBased;
		public static List<DisambiguationPolicy> parsePoliciesFromArgs(String[] args) {
			List<DisambiguationPolicy> policies = new ArrayList<>();
			for (String arg : args) {
				try {
					policies.add(DisambiguationPolicy.valueOf(arg));
				} catch (IllegalArgumentException e) {
					e.printStackTrace();
				}
			}
			if (policies.isEmpty())
				throw new IllegalArgumentException("Provide one or more valid disambiguation policies as argument. Valid policies: "
								+ DisambiguationPolicy.values());
			System.out.println("Parsed the following disambiguators: "	+ policies);
			return policies;
		}
	}
	
	private class DBPediaAPI {

		/**
		 * DBpedia和wikipedia的title是相同的
		 * @param dbpTitle
		 * @return
		 */
		public String dbpediaToWikipedia(String dbpTitle) {
			return dbpTitle;
		}

		public void prefetch(HashSet<String> resourcesToPrefetch) {
		}

	}
	
}
