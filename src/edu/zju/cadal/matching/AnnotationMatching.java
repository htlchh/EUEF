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

import edu.zju.cadal.main.Filter;
import edu.zju.cadal.model.Annotation;
import edu.zju.cadal.model.Entity;
import edu.zju.cadal.webservice.MediaWikiAPI;

/**
 * AnnotationMatching is the class to check if two annotations matched
 * 
 * @author:chenhui 
 * @email:chenhuicn@126.com
 * @date:2015年11月17日
 */
public class AnnotationMatching implements Matching<Annotation>{
	
	/** Embedded mention matching */
	private MentionMatching mm;
	private Filter<Annotation> filter;
	private MediaWikiAPI api = MediaWikiAPI.getInstance();
	
	public AnnotationMatching(MentionMatching mm) {
		this.mm = mm;
		this.filter = new Filter<Annotation>(this);
	}
	
	@Override
	public boolean match(Annotation a1, Annotation a2) {
		if (mm.match(a1.getMention(), a2.getMention()) == false)
			return false;
		
		Entity e1 = a1.getEntity();
		Entity e2 = a2.getEntity();
		if (api.dereference(e1.getId()) != api.dereference(e2.getId()))
			return false;
		
		return true;
	}


	@Override
	public String getName() {
		return "Annotation Matching";
	}

	@Override
	public void preProcessing(Map<String, Set<Annotation>> systemResult, Map<String, Set<Annotation>> goldStandard) {
		List<Integer> idList = new ArrayList<Integer>();
		
		//预先查询结果中的id，加快比较速度
		/**  */
		for (Set<Annotation> aSet : systemResult.values()) 
			for (Annotation a : aSet)
				idList.add(a.getEntity().getId());
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
		
		filter.coreference(systemResult);
		filter.coreference(goldStandard);
		filter.filterMany2One(systemResult, goldStandard);
	}

	@Override
	public MentionMatching getBaseMentionMatching() {
		return this.mm;
	}

}
