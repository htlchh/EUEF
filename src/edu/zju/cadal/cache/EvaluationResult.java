package edu.zju.cadal.cache;

import java.io.Serializable;
import java.util.Map;

/**
 * @author:chenhui 
 * @email:chenhuicn@126.com
 * @date:2015年11月16日
 */
public class EvaluationResult implements Serializable {

	private static final long serialVersionUID = 1L;
	private float microF1, microRecall, microPrecision, macroF1, macroRecall, macroPrecision;
	private int tpCount, fnCount, fpCount;
	private Map<String, Float> precisionMap, recallMap, f1Map;
	private Map<String, Integer> tpMap, fnMap, fpMap;
	
	public EvaluationResult(
			float microF1, 
			float microRecall,
			float microPrecision, 
			float macroF1, 
			float macroRecall,
			float macroPrecision, 
			int tpCount, 
			int fnCount, 
			int fpCount, 
			Map<String, Float> precisionMap,
			Map<String, Float> recallMap, 
			Map<String, Float> f1Map, 
			Map<String, Integer> tpMap, 
			Map<String, Integer> fpMap,
			Map<String, Integer> fnMap) 
	{
		this.microF1 = microF1;
		this.microRecall = microRecall;
		this.microPrecision = microPrecision;
		this.macroF1 = macroF1;
		this.macroRecall = macroRecall;
		this.macroPrecision = macroPrecision;
		this.tpCount = tpCount;
		this.fnCount = fnCount;
		this.fpCount = fpCount;
		this.precisionMap = precisionMap;
		this.recallMap = recallMap;
		this.f1Map = f1Map;
		this.tpMap = tpMap;
		this.fpMap = fpMap;		
		this.fnMap = fnMap;
	}

	public String toString() {
		return String
				.format("Micro P/R/F1: %.3f/%.3f/%.3f%n"
						+ "Macro P/R/F1: %.3f/%.3f/%.3f%n"
						+ "Global TP/FP/FN: %d/%d/%d",
						this.getMicroPrecision(), this.getMicroRecall(), this.getMicroF1(), 
						this.getMacroPrecision(), this.getMacroRecall(), this.getMacroF1(),
						this.getTpCount(), this.getFpCount(), this.getFnCount());
	}

	public void detailPRF() {
		for (String title : precisionMap.keySet()) {
			System.out.printf("title:%s; presicion:%.3f; recall:%.3f; f1:%.3f; tpCount:%d; fpCount:%d; fnCount:%d%n", 
					title, precisionMap.get(title), recallMap.get(title), f1Map.get(title), tpMap.get(title), fpMap.get(title), fnMap.get(title));
		}
	}
	
	public float getMicroRecall() {
		return microRecall;
	}

	public float getMicroPrecision() {
		return microPrecision;
	}

	public float getMicroF1() {
		return microF1;
	}

	public float getMacroRecall() {
		return macroRecall;
	}

	public float getMacroPrecision() {
		return macroPrecision;
	}

	public float getMacroF1() {
		return macroF1;
	}

	public int getTpCount() {
		return tpCount;
	}

	public int getFpCount() {
		return fpCount;
	}

	public int getFnCount() {
		return fnCount;
	}

	public Map<String, Float> getPrecisionMap() {
		return precisionMap;
	}

	public Map<String, Float> getRecallMap() {
		return recallMap;
	}

	public Map<String, Float> getF1Map() {
		return f1Map;
	}
	
	public Map<String, Integer> getTPMap() {
		return tpMap;
	}

	public Map<String, Integer> getFPMap() {
		return fpMap;
	}

	public Map<String, Integer> getFNMap() {
		return fnMap;
	}
}
