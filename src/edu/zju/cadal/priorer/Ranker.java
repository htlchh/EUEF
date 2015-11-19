package edu.zju.cadal.priorer;

import java.util.Set;

import edu.zju.cadal.model.Annotation;
import edu.zju.cadal.model.Candidate;
import edu.zju.cadal.utils.Timer;

/**
 * @author:chenhui 
 * @email:chenhuicn@126.com
 * @date:2015年11月18日
 */
public abstract class Ranker {

	abstract public Set<Annotation> rank(
			Set<Candidate> candidateSet, 
			String text, 
			Timer timer);	
	
	
	abstract public String getName();
	
}
