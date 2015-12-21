package edu.zju.cadal.matching;

import java.util.Map;
import java.util.Set;

public interface Matching<T> {

	/**
	 * 判断给定的两个T的实例是否匹配
	 * @param t1
	 * @param t2
	 * @return
	 */
	public boolean match(T t1, T t2);
	
	
	public void preProcessing(Map<String, Set<T>> prediction, Map<String, Set<T>> goldStandard);	
	
	public String getName();
	
	public MentionMatching getBaseMentionMatching();
}
