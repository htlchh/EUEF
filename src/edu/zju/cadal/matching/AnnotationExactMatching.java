package edu.zju.cadal.matching;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.xml.sax.SAXException;

import edu.zju.cadal.model.Annotation;
import edu.zju.cadal.model.Entity;
import edu.zju.cadal.model.Mention;
import edu.zju.cadal.webservice.MediaWikiAPI;

/**
 * @author:chenhui 
 * @email:chenhuicn@126.com
 * @date:2015年11月17日
 */
public class AnnotationExactMatching implements Matching<Annotation>{
	private MediaWikiAPI api = MediaWikiAPI.getInstance();
	
	@Override
	public boolean match(Annotation a1, Annotation a2) {
		Mention m1 = a1.getMention();
		Mention m2 = a2.getMention();
		if (m1.equals(m2) == false)
			return false;
		
		Entity e1 = a1.getEntity();
		Entity e2 = a2.getEntity();
		if (api.dereference(e1.getId()) != api.dereference(e2.getId()))
			return false;
		
		return true;
	}

	@Override
	public void preProcessSystemResult(Map<String, Set<Annotation>> systemResult) {
		//预先查询结果中的id，加快比较速度
		List<Integer> idList = new ArrayList<Integer>();
		for (Set<Annotation> aSet : systemResult.values()) 
			for (Annotation a : aSet)
				idList.add(a.getEntity().getId());
		try {
			api.prefetchWId(idList);
			api.flush();
		} catch (XPathExpressionException | IOException
				| ParserConfigurationException | SAXException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void preProcessGoldStandard(Map<String, Set<Annotation>> goldStandard) {
		List<Integer> idList = new ArrayList<Integer>();
		for (Set<Annotation> aSet : goldStandard.values()) 
			for (Annotation a : aSet)
				idList.add(a.getEntity().getId());
		try {
			api.prefetchWId(idList);
			api.flush();
		} catch (XPathExpressionException | IOException
				| ParserConfigurationException | SAXException e) {
			e.printStackTrace();
		}
	}

	@Override
	public String getName() {
		return "Annotation Exact Matching";
	}

	
}
