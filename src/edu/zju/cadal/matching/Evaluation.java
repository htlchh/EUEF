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
		Map<String, Set<T>> filteredSystemResult = m.preprocessSystemResult(systemResult);
		Map<String, Set<T>> filteredGoldStandard = m.preprocessGoldStandard(goldStandard);
		Map<String, Integer> tpMap = tp(filteredSystemResult, filteredGoldStandard, m);
		Map<String, Integer> fpMap = fp(filteredSystemResult, filteredGoldStandard, m);
		Map<String, Integer> fnMap = fn(filteredSystemResult, filteredGoldStandard, m);
		int tp = countTP(tpMap);
		int fp = countFP(fpMap);
		int fn = countFN(fnMap);
		Map<String, Float> precisionMap = precision(tpMap, fpMap);
		Map<String, Float> recallMap = recall(tpMap, fnMap);
		Map<String, Float> f1Map = f1(precisionMap, recallMap);
		float microPrecision = microPrecision(tp, fp);
		float microRecall = microRecall(tp, fn);
		float microF1 = microF1(microRecall, microPrecision);
		float macroPrecision = macroPrecision(precisionMap);
		float macroRecall = macroRecall(recallMap);
		float macroF1 = macroF1(macroPrecision, macroRecall);
		
		return new EvaluationResult(
				microF1, microRecall, microPrecision,
				macroF1, macroRecall, macroPrecision, 
				tp, fn, fp, 
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
	private static <T> Map<String, Integer> fn(Map<String, Set<T>> filteredSystemResult, Map<String, Set<T>> filteredGoldStandard, Matching<T> m) {
		Map<String, Integer> fnMap = new HashMap<String, Integer>();
		for (String title : filteredGoldStandard.keySet()) {
			int fn = getSingleFN(filteredSystemResult.get(title), filteredGoldStandard.get(title), m);
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
			if (matched == false)
				fn++;
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
	private static <T> Map<String, Integer> fp(Map<String, Set<T>> filteredSystemResult, Map<String, Set<T>> filteredGoldStandard, Matching<T> m) {
		Map<String, Integer> fpMap = new HashMap<String, Integer>();
		for (String title : filteredGoldStandard.keySet()) {
			int fp = getSingleFP(filteredSystemResult.get(title), filteredGoldStandard.get(title), m);
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
	private static <T> Map<String, Integer> tp(Map<String, Set<T>> filteredSystemResult,	Map<String, Set<T>> filteredGoldStandard, Matching<T> m) {
		Map<String, Integer> tpMap = new HashMap<String, Integer>();
		for (String title : filteredSystemResult.keySet()) {
			int tp = getSingleTP(filteredSystemResult.get(title), filteredGoldStandard.get(title), m);
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

	
	/*
	private int similarityIntersection(Set<T> set1, Set<T> set2, Matching<T> m) {
		int intersectionI = 0;
		for (T obj1 : set1)
			for (T obj2 : set2)
				if (m.match(obj1, obj2)) {
					intersectionI++;
					break;
				}
		for (T obj2 : set2)
			for (T obj1 : set1)
				if (m.match(obj1, obj2)) {
					intersectionI++;
					break;
				}
		return intersectionI;
	}
	
	private int dissimilaritySet(Set<T> set1, Set<T> set2, Matching<T> m) {
		int diss = 0;
		for (T obj1 : set1) {
			boolean found = false;
			for (T obj2 : set2)
				if (m.match(obj1, obj2)) {
					found = true;
					break;
				}
			if (!found)
				diss++;
		}
		return diss;
	}
	
	private int similarityUnion(Set<T> set1, Set<T> set2) {
		return set1.size() + set2.size();
	}
	
	public float singleSimilarity(Set<T> set1, Set<T> set2, Matching<T> m) {
		int intersectionI = similarityIntersection(set1, set2, m);
		int unionI = similarityUnion(set1, set2);
		return (unionI == 0) ? 1 : (float) intersectionI / (float) unionI;
	}
	
	public float macroSimilarity(Map<String, Set<T>> systemResult, Map<String, Set<T>> goldStandard, Matching<T> m) {

		float avg = 0;
		for (String title : systemResult.keySet()) {
			Set<T> set1 = systemResult.get(title);
			Set<T> set2 = goldStandard.get(title);
			avg += singleSimilarity(set1, set2, m);
		}
		return avg / (float) systemResult.size();
	}
	
	public float microSimilarity(Map<String, Set<T>> systemResult, Map<String, Set<T>> goldStandard, Matching<T> m) {

		long intersections = 0;
		long unions = 0;
		for (String title : systemResult.keySet()) {
			Set<T> set1 = systemResult.get(title);
			Set<T> set2 = goldStandard.get(title);
			intersections += similarityIntersection(set1, set2, m);
			unions += similarityUnion(set1, set2);			
		}
		return intersections / unions;
	}
	
	public int dissimilarityListCount(Map<String, Set<T>> systemResult, Map<String, Set<T>> goldStandard, Matching<T> m) {

		int dissim = 0;
		for (String title : systemResult.keySet()) {
			Set<T> set1 = systemResult.get(title);
			Set<T> set2 = goldStandard.get(title);
			dissim += dissimilaritySet(set1, set2, m);
		}
		return dissim;
	}
	
	public int similarityListCount(Map<String, Set<T>> systemResult, Map<String, Set<T>> goldStandard, Matching<T> m) {

		int intersect = 0;
		for (String title : systemResult.keySet()) {
			Set<T> set1 = systemResult.get(title);
			Set<T> set2 = goldStandard.get(title);	
			intersect += similarityIntersection(set1, set2, m);
		}
		return intersect;
	}
	
	public long listUnion(Map<String, Set<T>> systemResult, Map<String, Set<T>> goldStandard, Matching<T> m) {

		long union = 0;
		for (String title : systemResult.keySet()) {
			Set<T> set1 = systemResult.get(title);
			Set<T> set2 = goldStandard.get(title);	
			union += similarityUnion(set1, set2);
		}
		return union;
	}
	
	public static float precision(int tp, int fp) {
		return tp + fp == 0 ? 1 : (float) tp / (float) (tp + fp);
	}
	
	public static float recall(int tp, int fn) {
		return fn == 0 ? 1 : (float) tp / (float) (fn + tp);
	}
	
	public static float microF1(float recall, float precision) {
		return (recall + precision == 0) ? 0 : 2 * recall * precision / (recall + precision);
	}
	
	private Map<String, Set<T>> getTpPreprocessed(Map<String, Set<T>> systemResult, Map<String, Set<T>> goldStandard, Matching<T> m) {
		Map<String, Set<T>> tp = new HashMap<String, Set<T>>();
		for (String title : systemResult.keySet()) {
			Set<T> exp = systemResult.get(title);
			Set<T> comp = goldStandard.get(title);			
			tp.put(title, getSingleTp(exp, comp, m));
		}
		return tp;
	}
	
	public Set<T> getSingleTp(Set<T> systemResult, Set<T> goldStandard, Matching<T> m) {
		Set<T> tpsi = new HashSet<T>();
		for (T a1 : goldStandard)
			for (T a2 : systemResult)
				if (m.match(a1, a2)) {
					tpsi.add(a2);
					break;
				}
		return tpsi;
	}
	
	private Map<String, Set<T>> getFpPreprocessed(Map<String, Set<T>> systemResult, Map<String, Set<T>> goldStandard, Matching<T> m) {
		Map<String, Set<T>> fp = new HashMap<String, Set<T>>();
		for (String title : systemResult.keySet()) {
			Set<T> exp = systemResult.get(title);
			Set<T> comp = goldStandard.get(title);			
			fp.put(title, getSingleFp(exp, comp, m));
		}
		return fp;
	}
	
	public Set<T> getSingleFp(Set<T> systemResult, Set<T> goldStandard,	Matching<T> m) {
		Set<T> fpsi = new HashSet<T>();
		for (T a1 : systemResult) {
			boolean found = false;
			for (T a2 : goldStandard)
				if (m.match(a1, a2)) {
					found = true;
					break;
				}
			if (!found)
				fpsi.add(a1);
		}
		return fpsi;
	}
	
	private Map<String, Set<T>> getFnPreprocessed(Map<String, Set<T>> systemResult, Map<String, Set<T>> goldStandard, Matching<T> m) {
		Map<String, Set<T>> fn = new HashMap<String, Set<T>>();
		for (String title : systemResult.keySet()) {
			Set<T> exp = systemResult.get(title);
			Set<T> comp = goldStandard.get(title);			
			fn.put(title, getSingleFn(exp, comp, m));
		}
		return fn;
	}
	
	public Set<T> getSingleFn(Set<T> systemResult, Set<T> goldStandard,	Matching<T> m) {
		Set<T> fnsi = new HashSet<T>();
		for (T a1 : systemResult) {
			boolean found = false;
			for (T a2 : goldStandard)
				if (m.match(a1, a2)) {
					found = true;
					break;
				}
			if (!found)
				fnsi.add(a1);
		}
		return fnsi;
	}
	
	public float macroPrecision(int[] tps, int[] fps) {
		float macroPrec = 0;
		float precisions[] = precisions(tps, fps);
		for (int i = 0; i < tps.length; i++)
			macroPrec += precisions[i];
		macroPrec /= tps.length;
		return macroPrec;
	}
	
	public float[] precisions(int[] tps, int[] fps) {
		float[] precisions = new float[tps.length];
		for (int i = 0; i < tps.length; i++)
			precisions[i] = precision(tps[i], fps[i]);
		return precisions;
	}
	
	public float macroRecall(int[] tps, int[] fps, int[] fns) {
		float macroRec = 0;
		float[] recalls = recalls(tps, fps, fns);
		for (int i = 0; i < tps.length; i++)
			macroRec += recalls[i];
		macroRec /= tps.length;
		return macroRec;
	}

	public float[] recalls(int[] tps, int[] fps, int[] fns) {
		float[] recalls = new float[tps.length];
		for (int i = 0; i < tps.length; i++)
			recalls[i] = recall(tps[i], fns[i]);
		return recalls;
	}

	public float macroF1(int[] tps, int[] fps, int[] fns) {
		float macroF1 = 0;
		float[] f1s = f1s(tps, fps, fns);
		for (int i = 0; i < tps.length; i++)
			macroF1 += f1s[i];
		macroF1 /= tps.length;
		return macroF1;
	}

	public float[] f1s(int[] tps, int[] fps, int[] fns) {
		float[] f1s = new float[tps.length];
		for (int i = 0; i < tps.length; i++)
			f1s[i] = F1(recall(tps[i], fns[i]),
					precision(tps[i], fps[i]));
		return f1s;
	}
	
	private int countTP(Map<String, Set<T>> systemResult, Map<String, Set<T>> goldStandard, Matching<T> m) {
		int tp = 0;
		for (int tpi : tpArray(systemResult, goldStandard, m))
			tp += tpi;
		return tp;
	}

	private int countFP(Map<String, Set<T>> systemResult, Map<String, Set<T>> goldStandard, Matching<T> m) {
		int fp = 0;
		for (int fpi : fpArray(systemResult, goldStandard, m))
			fp += fpi;
		return fp;
	}	
	
	private int countFN(Map<String, Set<T>> systemResult, Map<String, Set<T>> goldStandard, Matching<T> m) {
		int fn = 0;
		for (int fni : fnArray(systemResult, goldStandard, m))
			fn += fni;
		return fn;
	}
	
	private int[] tpArray(Map<String, Set<T>> systemResult, Map<String, Set<T>> goldStandard, Matching<T> m) {
		int[] tps = new int[systemResult.size()];
		for (int i = 0; i < systemResult.size(); i++)
			tps[i] = getSingleTp(expectedResult.get(i), computedResult.get(i), m).size();
		return tps;
	}	
	
	private int[] fpArray(List<Set<T>> expectedResult,
			List<Set<T>> computedResult, MatchRelation<T> m) {
		int[] fps = new int[computedResult.size()];
		for (int i = 0; i < computedResult.size(); i++)
			fps[i] = getSingleFp(expectedResult.get(i), computedResult.get(i),
					m).size();
		return fps;
	}	

	private int[] fnArray(List<Set<T>> expectedResult,
			List<Set<T>> computedResult, MatchRelation<T> m) {
		int[] fns = new int[expectedResult.size()];
		for (int i = 0; i < expectedResult.size(); i++)
			fns[i] += getSingleFn(expectedResult.get(i), computedResult.get(i),
					m).size();
		return fns;
	}
	*/
}
