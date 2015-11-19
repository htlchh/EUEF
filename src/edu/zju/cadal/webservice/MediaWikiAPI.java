package edu.zju.cadal.webservice;

import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import edu.zju.cadal.utils.BidiObjectIntHashMap;

/**
 * @author:chenhui 
 * @email:chenhuicn@126.com
 * @date:2015年11月15日
 */
public class MediaWikiAPI {

	private static MediaWikiAPI api = new MediaWikiAPI();
	
	//默认的title到id的缓存文件路径
	private String title2widCachePath = "cache/title2wid.cache";
	//默认的redirect到非redirect的缓存文件路径，对于非redirect，则redirect是自身
	private String redirectCachePath = "cache/redirect.cache";
	
	private BidiObjectIntHashMap<String> title2wid = null;
	private File title2widFile = null;
	private Int2IntMap redirect = null;
	private File redirectFile = null;
	
	private static final String baseURL = "https://en.wikipedia.org/w/api.php";
	
	private int queries = 0;
	//每次查询的个数
	private final int countPerRequest = 50;
	/**
	 * 默认打开的缓存文件
	 */
	private MediaWikiAPI() {
		this.openCache(title2widCachePath, redirectCachePath);
	}
	
	public static MediaWikiAPI getInstance() {
		return api;
	}
	
	public void setTitle2WidCacheFilePath(String path) {
		this.title2widCachePath = path;
	}
	
	public void setRedirectCacheFilePath(String path) {
		this.redirectCachePath = path;
	}
	
	/**
	 * 
	 * @param title2widCacheFilePath
	 * @param redirectCacheFilePath
	 */
	@SuppressWarnings("unchecked")
	public void openCache(String title2widCacheFilePath, String redirectCacheFilePath) {
		this.title2widCachePath = title2widCacheFilePath;
		this.redirectCachePath = redirectCacheFilePath;
		title2widFile = new File(title2widCacheFilePath);
		if (title2widFile.exists() && title2widFile.length() > 0) {
			try {
				title2wid = (BidiObjectIntHashMap<String>)new ObjectInputStream(new FileInputStream(title2widFile)).readObject();
			} catch (ClassNotFoundException | IOException e) {
				throw new RuntimeException(
						"Could Not Load Cache File "
								+ title2widFile.getAbsolutePath()
								+ ". Try to Manually Delete The File to Clear The Cache. Message: "
								+ e.getMessage());
			}
		} else {
			title2wid = new BidiObjectIntHashMap<String>();
		}
		
		redirectFile = new File(redirectCacheFilePath);
		if (redirectFile.exists() && redirectFile.length() > 0) {
			try {
				redirect = (Int2IntOpenHashMap)new ObjectInputStream(new FileInputStream(redirectFile)).readObject();
			} catch (ClassNotFoundException | IOException e) {
				throw new RuntimeException(
						"Could Not Load Cache File "
								+ redirectFile.getAbsolutePath()
								+ ". Try to Manually Delete The File to Clear The Cache. Message: "
								+ e.getMessage());
			}
		} else {
			redirect = new Int2IntOpenHashMap();
		}
	}
	
	/**
	 * 把redirect和title2wid写入到文件中
	 */
	public void flush()	{
		try {
			title2widFile.createNewFile();
			ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(title2widFile));
			oos.writeObject(title2wid);
			oos.close();
			
			redirectFile.createNewFile();
			oos = new ObjectOutputStream(new FileOutputStream(redirectFile));
			oos.writeObject(redirect);
			oos.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	public int dereference(int wid) {
		if (redirect.containsKey(wid))
			return redirect.get(wid);
		try {
			List<Integer> v = new ArrayList<Integer>();
			v.add(wid);
			prefetchWId(v);
		} catch (XPathExpressionException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return redirect.get(wid);
	}	
	
	public int getIdByTitle(String title) throws XPathExpressionException, IOException, ParserConfigurationException, SAXException {
		title = normalize(title);
		//如果已经缓存，则直接返回
		if (title2wid.hasObject(title))
			return title2wid.getByObject(title);
		
		//查询title
		List<String> titleList = new ArrayList<String>();
		titleList.add(title);
		prefetchTitle(titleList);
		
		//无效的title，返回-1
		if (title2wid.hasObject(title) == false)
			return -1;
		
		return title2wid.getByObject(title);
	}
	
	/**
	 * 查询缓存给定的title的信息
	 * @throws SAXException 
	 * @throws ParserConfigurationException 
	 * @throws IOException 
	 * @throws XPathExpressionException 
	 */
	public void prefetchTitle(List<String> titleList) throws XPathExpressionException, IOException, ParserConfigurationException, SAXException {
		List<String> titleToActuallyPrefetchList = new ArrayList<String>();
		for (String title : titleList) {
			title = normalize(title);
			if (title2wid.hasObject(title) == false)
				titleToActuallyPrefetchList.add(title);
		}
		
		for (int i = 0; i < titleToActuallyPrefetchList.size(); i += countPerRequest) {
			String query = "";
			for (int j = i; j < titleToActuallyPrefetchList.size() && j < i + countPerRequest; j++)
				query +=  (j == i ? "" : "|") + URLEncoder.encode(titleToActuallyPrefetchList.get(j), "UTF-8");
			URL url = new URL(baseURL + "?format=xml&action=query&prop=info&titles=" + query);
			System.out.println("Querying " + url);
			processQueryResult(url);
		}
	}
	
	public void prefetchWId(List<Integer> idList) throws XPathExpressionException, IOException, ParserConfigurationException, SAXException {
		
		List<Integer> widsToActuallyPrefetchList = new ArrayList<Integer>();
		for (int wid : idList)
			if (wid != -1 && !title2wid.hasInt(wid))
				widsToActuallyPrefetchList.add(wid);

		Integer[] widToActuallyPrefetchList = widsToActuallyPrefetchList.toArray(new Integer[0]);
		for (int i = 0; i < widToActuallyPrefetchList.length; i += countPerRequest) {
			String widsQuery = "";
			for (int j = i; j < widToActuallyPrefetchList.length && j < i + countPerRequest; j++) {
				widsQuery += (j == i ? "" : "|") + widToActuallyPrefetchList[j];
			}
			URL url = new URL(baseURL + "?format=xml&action=query&prop=info&pageids=" + widsQuery);
			System.out.println("Querying " + url);
			processQueryResult(url);	
		}
	}
		
		
	/**
	 * 查询50次，自动刷新缓存
	 */
	private void autoFlushCounter() {
		if (queries++ % 50 == 0)
			this.flush();
	}	

	
	
	/**
	 * 处理MediaWiki返回的查询结果
	 * 示例：
<api batchcomplete="">
	<query>
		<!-- for title, it may be normalized
		<normalized>
			<n from="obama" to="Obama"/>
		</normalized>
		-->
		<pages>
			<page _idx="1" pageid="1" missing=""/>
			<!-- this page is a redirect -->
			<page _idx="31643" pageid="31643" ns="0" title="US" contentmodel="wikitext" pagelanguage="en" touched="2015-10-11T02:59:00Z" lastrevid="653735833" length="50" redirect=""/>
			<page _idx="3434750" pageid="3434750" ns="0" title="United States" contentmodel="wikitext" pagelanguage="en" touched="2015-10-11T02:59:00Z" lastrevid="685143185" length="344473"/>
			<page _idx="12736609" pageid="12736609" ns="0" title="Obama" contentmodel="wikitext" pagelanguage="en" touched="2015-10-03T18:52:24Z" lastrevid="608505859" length="109" redirect=""/>
		</pages>
	</query>
</api>
	 * @param url
	 * @throws IOException 
	 * @throws ParserConfigurationException 
	 * @throws SAXException 
	 * @throws XPathExpressionException 
	 */
	private void processQueryResult(URL url) 
			throws IOException, ParserConfigurationException, SAXException, XPathExpressionException {
		autoFlushCounter();
		
		URLConnection connection = url.openConnection();
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		InputStream retValue = connection.getInputStream();
		connection.setConnectTimeout(30000);
		connection.setReadTimeout(30000);
		Document doc = builder.parse(retValue);
		
		XPathFactory xPathFactory = XPathFactory.newInstance();
		XPath xpath = xPathFactory.newXPath();
		
		//处理MediaWiki对传入的title进行规范化的结果
		Map<String, String> normalization = new HashMap<String, String>();
		XPathExpression expr = xpath.compile("//normalized/n/@from");
		NodeList normalizedFromNodes = (NodeList)expr.evaluate(doc, XPathConstants.NODESET);
		for (int j = 0; j < normalizedFromNodes.getLength(); j++) {
			String normalizedFrom = normalizedFromNodes.item(j).getNodeValue();
			expr = xpath.compile("//normalized/n[@from=" + escape(normalizedFrom) + "]/@to");
			String normalizedTo = (String) expr.evaluate(doc, XPathConstants.STRING);
			normalization.put(normalizedTo, normalizedFrom);
		}	
		
		//查询不到信息的title，对应的id是-1
		expr = xpath.compile("//page[@missing][@title]/@title");
		NodeList missingTitles = (NodeList) expr.evaluate(doc, XPathConstants.NODESET);
		for (int j = 0; j < missingTitles.getLength(); j++)
			title2wid.put(missingTitles.item(j).getNodeValue(), -1);
		
		
		//查询不到的id，对应的redirect是-1 */
		expr = xpath.compile("//page[@missing][@pageid]/@pageid");
		NodeList missingIds = (NodeList) expr.evaluate(doc,	XPathConstants.NODESET);
		for (int j = 0; j < missingIds.getLength(); j++)
			redirect.put(Integer.parseInt(missingIds.item(j).getNodeValue()), -1);
		
		//对有效的title，存入对应的id到缓存中
		expr = xpath.compile("//page/@pageid");
		NodeList idNodes = (NodeList) expr.evaluate(doc, XPathConstants.NODESET);
		for (int j = 0; j < idNodes.getLength(); j++) {
			String pageid = idNodes.item(j).getNodeValue();
			expr = xpath.compile("//page[@pageid=" + escape(pageid)	+ "]/@title");
			String title = expr.evaluate(doc);
			title2wid.put(title, Integer.parseInt(pageid));
		}		

		//把normalization之前的title放入缓存
		for (String to : normalization.keySet()) {
			String from = normalization.get(to);
			try {
				title2wid.put(from, title2wid.getByObject(to));
			} catch (Exception e) {
				System.out.println("Normalized Title " + to + " Has no Corresponding Page Id");
			}
		}
		
		//不是redirects的id存入缓存，redirect是其自身
		expr = xpath.compile("//page[not(@redirect)]/@pageid");
		NodeList nonRedirectIdNodes = (NodeList) expr.evaluate(doc,	XPathConstants.NODESET);
		for (int j = 0; j < nonRedirectIdNodes.getLength(); j++) {
			int pageid = Integer.parseInt(nonRedirectIdNodes.item(j).getNodeValue());
			redirect.put(pageid, pageid);
		}
		
		//处理redirects
		expr = xpath.compile("//page[@redirect]/@pageid");
		NodeList redirectIdNodes = (NodeList) expr.evaluate(doc, XPathConstants.NODESET);
		List<Integer> idsToDereference = new ArrayList<Integer>();
		for (int j = 0; j < redirectIdNodes.getLength(); j++)
			idsToDereference.add(Integer.parseInt(redirectIdNodes.item(j).getNodeValue()));
		prefetchRedirect(idsToDereference);		
	}
	
	private void prefetchRedirect(List<Integer> redirectList) 
			throws IOException, ParserConfigurationException, SAXException, XPathExpressionException {
		if (redirectList.isEmpty() == true)
			return;
		String query = "";
		for (int j = 0; j < redirectList.size(); j++)
			query += (j == 0 ? redirectList.get(j) : ("|" + redirectList.get(j)));
		autoFlushCounter();
		
		URL url = new URL(baseURL + "?format=xml&action=query&prop=info&redirects=&pageids=" + query);
		System.out.println("Querying Redricts: " + url);
		URLConnection wikiConnection = url.openConnection();
		wikiConnection.setConnectTimeout(30000);
		wikiConnection.setReadTimeout(30000);
		InputStream retValue = wikiConnection.getInputStream();
				
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		Document doc = builder.parse(retValue);

		XPathFactory xPathfactory = XPathFactory.newInstance();
		XPath xpath = xPathfactory.newXPath();

		//不存在的redirect，则填入-1
		XPathExpression expr = xpath.compile("//page[@missing]/pageid");
		NodeList missing = (NodeList) expr.evaluate(doc, XPathConstants.NODESET);
		for (int j = 0; j < missing.getLength(); j++) {
			String idMissing = missing.item(j).getNodeValue();
			redirect.put(Integer.parseInt(idMissing), -1);
		}
		
		//有效的id
		expr = xpath.compile("//page/@title");
		NodeList toTitlesNodes = (NodeList) expr.evaluate(doc, XPathConstants.NODESET);

		for (int i = 0; i < toTitlesNodes.getLength(); i++) {
			String toTitle = toTitlesNodes.item(i).getNodeValue();

			expr = xpath.compile("//page[@title=" + escape(toTitle)	+ "]/@pageid");
			int toId = Integer.parseInt((String) expr.evaluate(doc,	XPathConstants.STRING));
			expr = xpath.compile("//r[@to=" + escape(toTitle) + "]/@from");
			NodeList fromTitleNodes = (NodeList) expr.evaluate(doc,	XPathConstants.NODESET);	
			
			if (fromTitleNodes.getLength() > 0)
				for (int j = 0; j < fromTitleNodes.getLength(); j++) {
					String fromTitle = fromTitleNodes.item(j).getNodeValue();
					int fromId = getIdByTitle(fromTitle);
					redirect.put(fromId, toId);
					redirect.put(toId, toId);
					title2wid.put(toTitle, toId);
				}
			/** Wids that are not redirects */
			else {
				redirect.put(toId, toId);
				title2wid.put(toTitle, toId);
			}
		}
	}
	
	
	/**
	 * 把title中的下划线换成空格
	 * @param title
	 * @return
	 */
	private String normalize(String title) {
		return title.replaceAll("_+", " ");
	}

	
	private static String escape(String s) {
		Matcher matcher = Pattern.compile("['\"]").matcher(s);
		StringBuilder buffer = new StringBuilder("concat(");
		int start = 0;
		while (matcher.find()) {
			buffer.append("'").append(s.substring(start, matcher.start())).append("',");
			buffer.append("'".equals(matcher.group()) ? "\"'\"," : "'\"',");
			start = matcher.end();
		}
		if (start == 0) {
			return "'" + s + "'";
		}
		return buffer.append("'").append(s.substring(start)).append("'").append(")").toString();
	}
		
	
}
