package edu.zju.cadal.system;

import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import edu.zju.cadal.cache.Prediction;
import edu.zju.cadal.dataset.AbstractDataset;
import edu.zju.cadal.model.Annotation;
import edu.zju.cadal.model.Candidate;
import edu.zju.cadal.model.Entity;
import edu.zju.cadal.model.Mention;
import edu.zju.cadal.model.NIL;
import edu.zju.cadal.priorer.CrossWikiLinker;
import edu.zju.cadal.priorer.NER;
import edu.zju.cadal.utils.Pair;
import edu.zju.cadal.utils.Timer;

/**
 * @author:chenhui 
 * @email:chenhuicn@126.com
 * @date:2015年11月18日
 */
public class Priorer extends AbstractERDSystem {

	private boolean useCache = false;
	private NER ner = new NER();
	private CrossWikiLinker linker = new CrossWikiLinker();
	
	public Priorer(boolean useCache) {
		this.useCache = useCache;
	}
	
	@Override
	public String getName() {
		return "Priorer";
	}

	@Override
	public Prediction erd(AbstractDataset ds) {
		Prediction result = Prediction.getInstance(useCache);
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
			
			executor(
					rawTextMap.get(title), 
					timer, 
					mentionSet, 
					candidateSet, 
					entitySet, 
					annotationSet, 
					NILSet);
			
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

	private void executor(
			String text, 
			Timer timer, 
			Set<Mention> mentionSet,
			Set<Candidate> candidateSet, 
			Set<Entity> entitySet,
			Set<Annotation> annotationSet, 
			Set<NIL> nILSet) 
	{
		Timer t = new Timer();
		long costtime = 0;
		Set<Mention> mSet = ner.recognize(text, t);
		costtime += t.getCostTime();
		Set<Candidate> cSet = linker.link(mSet, text, t);
		costtime += t.getCostTime();
		
		long currentTime = Calendar.getInstance().getTimeInMillis();
		//填充结果
		mentionSet.addAll(mSet);
		candidateSet.addAll(cSet);
		
		for (Candidate c : cSet) {
			Annotation res = createAnnotation(c);
			if (res != null) {
				annotationSet.add(res);
				entitySet.add(res.getEntity());
			}
			else 
				nILSet.add(new NIL(c.getMention()));
		}
		t.setCostTime(Calendar.getInstance().getTimeInMillis()-currentTime+costtime);
	}
	
	private Annotation createAnnotation(Candidate c) {
		Set<Pair<Entity, Float>> pairSet = c.getPairSet();
		if (pairSet.size() == 1) {
			for (Pair<Entity, Float> pair : pairSet) {
				if (pair.first.getId() == 0)
					return null;
				else 
					return new Annotation(c.getMention(), new Entity(pair.first.getId(), pair.first.getTitle()), pair.second);
			}
		}
		float maxscore = -1;
		Pair<Entity, Float> bestPair = null;
		for (Pair<Entity, Float> pair : pairSet) {
			if (pair.second > maxscore) {
				bestPair = pair;
				maxscore = bestPair.second;
			}
		}
		return new Annotation(c.getMention(), new Entity(bestPair.first.getId(), bestPair.first.getTitle()), bestPair.second);
	}

}
