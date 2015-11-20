package edu.zju.cadal.matching;

import java.util.Map;
import java.util.Set;

/**
 * @author:chenhui 
 * @email:chenhuicn@126.com
 * @date:2015年11月14日
 */
public interface Matching<T> {

	/**
	 * 判断给定的两个T的实例是否匹配
	 * @param t1
	 * @param t2
	 * @return
	 */
	public boolean match(T t1, T t2);
	
	
	/**
	 * 过滤系统产生的输出结果
	 * 
	 * @param systemResult
	 * @return
	 */
	public void preProcessSystemResult(Map<String, Set<T>> systemResult);
	
	
	/**
	 * 过滤数据集的标注
	 * 
	 * @param goldStandard
	 * @return
	 */
	public void preProcessGoldStandard(Map<String, Set<T>> goldStandard);
	
	
	
	public String getName();
}
