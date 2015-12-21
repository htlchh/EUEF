package edu.zju.cadal.matching;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.xml.sax.SAXException;

import edu.zju.cadal.main.Filter;
import edu.zju.cadal.model.Candidate;
import edu.zju.cadal.model.Entity;
import edu.zju.cadal.utils.Pair;
import edu.zju.cadal.webservice.MediaWikiAPI;

public class CandidateMatching implements Matching<Candidate>{

	private MentionMatching mm;
	private MediaWikiAPI api = MediaWikiAPI.getInstance();
	private Filter<Candidate> filter;
	
	public CandidateMatching(MentionMatching mm) {
		this.mm = mm;
		this.filter = new Filter<Candidate>(this);
	}
	
	@Override
	public boolean match(Candidate c1, Candidate c2) {
		if (mm.match(c1.getMention(), c2.getMention()) == false)
			return false;
		Set<Pair<Entity, Float>> s1 = c1.getPairSet();
		Set<Pair<Entity, Float>> s2 = c2.getPairSet();
		
		Set<Integer> s3 = new HashSet<Integer>();
		Set<Integer> s4 = new HashSet<Integer>();
		for (Pair<Entity, Float> p : s1)
			s3.add(api.dereference(p.first.getId()));
		for (Pair<Entity, Float> p : s2)
			s4.add(api.dereference(p.first.getId()));
		s3.retainAll(s4);
		if (s3.size() == 0)
			return false;
		return true;
	}


	@Override
	public void preProcessing(Map<String, Set<Candidate>> prediction, Map<String, Set<Candidate>> goldStandard) {
		/** pre-fetch titles to accelerate computing*/
		List<Integer> idList = new ArrayList<Integer>();
		for (String title : prediction.keySet()) {
			for (Candidate c : prediction.get(title))
				for (Pair<Entity, Float> p : c.getPairSet())
					idList.add(p.first.getId());
		}
		
		for (String title : goldStandard.keySet()) {
			for (Candidate c : goldStandard.get(title))
				for (Pair<Entity, Float> p : c.getPairSet())
					idList.add(p.first.getId());
		}
		
		try {
			api.prefetchWId(idList);
			api.flush();
		} catch (XPathExpressionException | IOException	| ParserConfigurationException | SAXException e) {
			e.printStackTrace();
		}
		
		filter.coreference(prediction);
		filter.coreference(goldStandard);
		filter.filterMany2One(prediction, goldStandard);
	}
	
	@Override
	public String getName() {
		return "Candidate Matching";
	}

	@Override
	public MentionMatching getBaseMentionMatching() {
		return this.mm;
	}

}
