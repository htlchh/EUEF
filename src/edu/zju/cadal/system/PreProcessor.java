package edu.zju.cadal.system;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.xml.sax.SAXException;

import edu.zju.cadal.matching.MentionFuzzyMatching;
import edu.zju.cadal.model.Annotation;
import edu.zju.cadal.model.Candidate;
import edu.zju.cadal.model.Mention;
import edu.zju.cadal.webservice.MediaWikiAPI;

/**
 * @author:chenhui 
 * @email:chenhuicn@126.com
 * @date:2015年11月16日
 */
public class PreProcessor {

	private static MediaWikiAPI api = MediaWikiAPI.getInstance();
	
	/**
	 * 对给定的mention集合进行消解
	 * 
	 * 选择长度最大的mention
	 * 
	 * @param mentionSet
	 * @return
	 */
	public static Set<Mention> coreference(Set<Mention> mentionSet) {
		MentionFuzzyMatching mwm = new MentionFuzzyMatching();
		Set<Mention> result = new HashSet<Mention>();
		List<Mention> mentionList = new ArrayList<Mention>(mentionSet);
		Collections.sort(mentionList);
		for (int i = 0; i < mentionList.size(); i++) {
			Mention bestMention = mentionList.get(i);
			int j = i + 1;
			//如果两个mentino重叠，则取长度最大的那一个
			while (j < mentionList.size() && mwm.match(bestMention, mentionList.get(j))) {
				if (bestMention.getLength() < mentionList.get(j).getLength())
					bestMention = mentionList.get(j);
				j++;
			}
			i = j - 1;
			result.add(bestMention);
		}
		return result;		
	}
	
	/**
	 * 把重定向的id转成非重定向的id
	 * @param wid
	 * @return
	 */
	public static int dereference(int wid) {
		return api.dereference(wid);
	}
	
	/**
	 * 把系统输出结果中的含有重叠的mention的annotation进行过滤
	 * @param annotationSet
	 * @return
	 */
	public static Set<Annotation> filterOverlapAnnotation(Set<Annotation> annotationSet) {
		MentionFuzzyMatching mwm = new MentionFuzzyMatching();
		Set<Annotation> result = new HashSet<Annotation>();
		List<Annotation> annotationList = new ArrayList<Annotation>(annotationSet);
		for (int i = 0; i < annotationList.size(); i++) {
			Annotation bestAnnotation = annotationList.get(i);
			for (int j = 0; j < annotationList.size(); j++) {
				Annotation a = annotationList.get(j);
				if (mwm.match(bestAnnotation.getMention(), a.getMention()) == true)
					if (bestAnnotation.getMention().getLength() < a.getMention().getLength())
						bestAnnotation = a;
			}
			result.add(bestAnnotation);
		}
		return result;
	}
	
	/**
	 * 把系统输出结果中的含有重叠的mention的candidate进行过滤
	 * @param candidateSet
	 * @return
	 */
	public static Set<Candidate> filterOverlapCandidate(Set<Candidate> candidateSet) {
		MentionFuzzyMatching mwm = new MentionFuzzyMatching();
		Set<Candidate> result = new HashSet<Candidate>();
		List<Candidate> candidateList = new ArrayList<Candidate>(candidateSet);
		for (int i = 0; i < candidateList.size(); i++) {
			Candidate bestCandidate = candidateList.get(i);
			for (int j = 0; j < candidateList.size(); j++) {
				Candidate c = candidateList.get(j);
				if (mwm.match(bestCandidate.getMention(), c.getMention()) == true)
					if (bestCandidate.getMention().getLength() < c.getMention().getLength())
						bestCandidate = c;
					else if (bestCandidate.getPairSet().size() < c.getPairSet().size())
						bestCandidate = c;
					else {
						;
					}
			}
			result.add(bestCandidate);
		}
		return result;
	}
	
	public static void prefetchWId(List<Integer> idList) {
		try {
			api.prefetchWId(idList);
			api.flush();
		} catch (XPathExpressionException | IOException
				| ParserConfigurationException | SAXException e) {
			e.printStackTrace();
		}
	}
	
}
