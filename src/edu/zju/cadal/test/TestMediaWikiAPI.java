package edu.zju.cadal.test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.junit.Test;
import org.xml.sax.SAXException;

import edu.zju.cadal.webservice.MediaWikiAPI;

/**
 * @author:chenhui 
 * @email:chenhuicn@126.com
 * @date:2015年11月16日
 */
public class TestMediaWikiAPI {
	
	MediaWikiAPI api = MediaWikiAPI.getInstance();
	
	@Test
	public void test_prefetchTitle() throws XPathExpressionException, IOException, ParserConfigurationException, SAXException {
		List<String> titleList = new ArrayList<String>();
		titleList.add("Jordan");
		titleList.add("U.S.");
		titleList.add("China");
		titleList.add("ACDADDADDD");
		api.prefetchTitle(titleList);
	}
	
	@Test
	public void test_getIdByTitle() throws XPathExpressionException, IOException, ParserConfigurationException, SAXException {
		System.out.println(api.getIdByTitle("Jordan"));
		System.out.println(api.getIdByTitle("U.S."));
		System.out.println(api.getIdByTitle("China"));
		System.out.println(api.getIdByTitle("ACDADDADDD"));
		System.out.println(api.getIdByTitle("DJLS"));
		api.flush();
	}
}
