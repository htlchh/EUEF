package edu.zju.cadal.dataset;

import java.util.Map;
import java.util.Set;

import edu.zju.cadal.model.Annotation;
import edu.zju.cadal.model.Candidate;
import edu.zju.cadal.model.Entity;
import edu.zju.cadal.model.Mention;
import edu.zju.cadal.model.NIL;
import edu.zju.cadal.webservice.MediaWikiAPI;

public abstract class AbstractDataset {
	//使用默认的存储文件
	protected MediaWikiAPI api = MediaWikiAPI.getInstance();
	abstract public String getName();
	
	abstract public int getSize();
	
	/**
	 * 获取数据集的原始文档
	 * Map<String, String>，第一个参数是文档title，第二个参数是title对应的文档内容
	 * Get the raw texts of a dataset
	 * 
	 * @return Map<document title, text>
	 */
	abstract public Map<String, String> getRawText();
	
	/**
	 * 获取数据集的mention信息
	 * Map<String, Set<Mention>>，第一个参数是文档title，第二个参数是title文档对应的mention集合
	 * Get gold mentions in a dataset
	 * 
	 * @return Map<document title, Set<Mention>>
	 */
	abstract public Map<String, Set<Mention>> getGoldMention();
	
	
	/**
	 * 获取数据集的annotation信息
	 * Get gold Annotations
	 * @return
	 */
	abstract public Map<String, Set<Annotation>> getGoldAnnotation();
	
	
	
	abstract public Map<String, Set<NIL>> getGoldNIL();
	
	
	abstract public Map<String, Set<Candidate>> getGoldCandidate();
	
	
	abstract public Map<String, Set<Entity>> getGoldEntity();
	
}
