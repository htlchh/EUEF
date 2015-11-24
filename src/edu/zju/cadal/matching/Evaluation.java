package edu.zju.cadal.matching;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import edu.zju.cadal.cache.EvaluationResult;

/**
 * @author:chenhui 
 * @email:chenhuicn@126.com
 * @date:2015年11月16日
 */
public class Evaluation<T> {

	public static <T> EvaluationResult getResult(
			Map<String, Set<T>> systemResult, 
			Map<String, Set<T>> goldStandard,
			Matching<T> m)
	{
		m.preProcessing(systemResult, goldStandard);
		Map<String, Integer> tpMap = tp(systemResult, goldStandard, m);
		Map<String, Integer> fpMap = fp(systemResult, goldStandard, m);
		Map<String, Integer> fnMap = fn(systemResult, goldStandard, m);
		int tpCount = countTP(tpMap);
		int fpCount = countFP(fpMap);
		int fnCount = countFN(fnMap);
		Map<String, Float> precisionMap = precision(tpMap, fpMap);
		Map<String, Float> recallMap = recall(tpMap, fnMap);
		Map<String, Float> f1Map = f1(precisionMap, recallMap);
		float microPrecision = microPrecision(tpCount, fpCount);
		float microRecall = microRecall(tpCount, fnCount);
		float microF1 = microF1(microRecall, microPrecision);
		float macroPrecision = macroPrecision(precisionMap);
		float macroRecall = macroRecall(recallMap);
		float macroF1 = macroF1(macroPrecision, macroRecall);
		
		return new EvaluationResult(
				microF1, microRecall, microPrecision,
				macroF1, macroRecall, macroPrecision, 
				tpCount, fnCount, fpCount, 
				precisionMap, recallMap, f1Map, 
				tpMap, fpMap, fnMap);		
	}

	
	/**
	 * 对每篇文档统计fn个数，存在放一个map中
	 * @param filteredSystemResult
	 * @param filteredGoldStandard
	 * @param m
	 * @return
	 */
	private static <T> Map<String, Integer> fn(Map<String, Set<T>> systemResult, Map<String, Set<T>> goldStandard, Matching<T> m) {
		Map<String, Integer> fnMap = new HashMap<String, Integer>();
		for (String title : goldStandard.keySet()) {
			int fn = getSingleFN(systemResult.get(title), goldStandard.get(title), m);
			fnMap.put(title, fn);
		}
		return fnMap;
	}

	private static <T> int getSingleFN(Set<T> systemResult, Set<T> goldStandard, Matching<T> m) {
		int fn = 0;
		for (T g : goldStandard) {
			boolean matched = false;
			for (T s : systemResult) {
				if (m.match(s, g) == true) {
					matched = true;
					break;
				}
			}
			if (matched == false) {
				fn++;
			}
		}
		return fn;
	}

	/**
	 * 对每篇文档统计fp个数，存在map中
	 * @param filteredSystemResult
	 * @param filteredGoldStandard
	 * @param m
	 * @return
	 */
	private static <T> Map<String, Integer> fp(Map<String, Set<T>> systemResult, Map<String, Set<T>> goldStandard, Matching<T> m) {
		Map<String, Integer> fpMap = new HashMap<String, Integer>();
		for (String title : goldStandard.keySet()) {
			int fp = getSingleFP(systemResult.get(title), goldStandard.get(title), m);
			fpMap.put(title, fp);
		}
		return fpMap;
	}

	private static <T> int getSingleFP(Set<T> systemResult, Set<T> goldStandard, Matching<T> m) {
		int fp = 0;
		for (T s : systemResult) {
			boolean matched = false;
			for (T g : goldStandard)
				if (m.match(s, g) == true) {
//					System.out.println(s);
					matched = true;
					break;
				}
			if (matched == false)
				fp++;
		}
		return fp;
	}

	/**
	 * 对每篇文档统计tp个数，存在map中
	 * @param filteredSystemResult
	 * @param filteredGoldStandard
	 * @param m
	 * @return
	 */
	private static <T> Map<String, Integer> tp(Map<String, Set<T>> systemResult, Map<String, Set<T>> goldStandard, Matching<T> m) {
		Map<String, Integer> tpMap = new HashMap<String, Integer>();
		for (String title : systemResult.keySet()) {
			int tp = getSingleTP(systemResult.get(title), goldStandard.get(title), m);
			tpMap.put(title, tp);
		}
		return tpMap;
	}

	private static <T> int getSingleTP(Set<T> systemResult, Set<T> goldStandard, Matching<T> m) {
		int tp = 0;
		for (T s : systemResult)
			for (T g : goldStandard)
				if (m.match(s, g) == true) {
					tp++;
					break;
				}
		return tp;
	}

	/**
	 * 数据集的fp个数
	 * @param filteredSystemResult
	 * @param filteredGoldStandard
	 * @param m
	 * @return
	 */
	private static <T> int countFP(Map<String, Integer> fpMap) {
		int fp = 0;
		for (String title : fpMap.keySet()) 
			fp += fpMap.get(title);
		return fp;
	}

	/**
	 * 数据集的fn个数
	 * @param filteredSystemResult
	 * @param filteredGoldStandard
	 * @param m
	 * @return
	 */
	private static <T> int countFN(Map<String, Integer> fnMap) {
		int fn = 0;
		for (String title : fnMap.keySet()) 
			fn += fnMap.get(title);
		return fn;
	}

	/**
	 * 数据集的tp个数
	 * @param filteredSystemResult
	 * @param filteredGoldStandard
	 * @param m
	 * @return
	 */
	private static <T> int countTP(Map<String, Integer> tpMap) {
		int tp = 0;
		for (String title : tpMap.keySet()) 
			tp += tpMap.get(title);
		return tp;
	}
	
	
	/**
	 * 对每篇文档计算recall，存放在一个map中
	 * @param tpMap
	 * @param fnMap
	 * @return
	 */
	private static <T> Map<String, Float> recall(Map<String, Integer> tpMap, Map<String, Integer> fnMap) {
		Map<String, Float> recallMap = new HashMap<String, Float>();
		for (String title : tpMap.keySet()) {
			float recall = tpMap.get(title) + fnMap.get(title) == 0
							? 1 
							: (float)tpMap.get(title) / (tpMap.get(title) + fnMap.get(title));
			recallMap.put(title, recall);
		}
		return recallMap;
	}
	
	/**
	 * 对每篇文档计算precision，存放在一个map中
	 * @param tpMap
	 * @param fpMap
	 * @return
	 */
	private static <T> Map<String, Float> precision(Map<String, Integer> tpMap, Map<String, Integer> fpMap) {
		Map<String, Float> precisionMap = new HashMap<String, Float>();
		for (String title : tpMap.keySet()) {
			float precision = tpMap.get(title) + fpMap.get(title) == 0 
							  ? 1
							  : (float)tpMap.get(title) / (tpMap.get(title) + fpMap.get(title));
			precisionMap.put(title, precision);
		}
		return precisionMap;
	}
	
	private static <T> Map<String, Float> f1(Map<String, Float> precisionMap, Map<String, Float> recallMap) {
		Map<String, Float> f1Map = new HashMap<String, Float>();
		for (String title :precisionMap.keySet()) {
			float f1 = (precisionMap.get(title) + recallMap.get(title)) == 0
					? 0
					: 2 * precisionMap.get(title) * recallMap.get(title) / (precisionMap.get(title) + recallMap.get(title));
			f1Map.put(title, f1);
		}
		return f1Map;
	}
	
	private static <T> float macroRecall(Map<String, Float> recallMap) {
		float recall = 0.0f;
		for (String title : recallMap.keySet())
			recall += recallMap.get(title);
		return recall / recallMap.size();
	}
	
	private static <T> float macroPrecision(Map<String, Float> precisionMap) {
		float precision = 0.0f;
		for (String title : precisionMap.keySet())
			precision += precisionMap.get(title);
		return precision / precisionMap.size();
	}

	private static <T> float macroF1(float macroPrecision, float macroRecall) {
		return (macroPrecision + macroRecall == 0) 
					? 0 
					: 2 * macroPrecision * macroRecall / (macroPrecision + macroRecall);
	}

	private static <T> float microF1(float recall, float precision) {
		return (recall + precision == 0) 
				? 0 
				: 2 * recall * precision / (recall + precision);
	}

	private static <T> float microRecall(int tp, int fn) {
		return fn + tp == 0 
				? 1 
				: (float) tp / (float) (fn + tp);
	}

	private static <T> float microPrecision(int tp, int fp) {
		return tp + fp == 0 
				? 1 
				: (float) tp / (float) (tp + fp);
	}

}
