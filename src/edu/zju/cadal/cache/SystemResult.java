package edu.zju.cadal.cache;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import edu.zju.cadal.model.Annotation;
import edu.zju.cadal.model.Candidate;
import edu.zju.cadal.model.Entity;
import edu.zju.cadal.model.Mention;
import edu.zju.cadal.model.NIL;

/**
 * @author:chenhui 
 * @email:chenhuicn@126.com
 * @date:2015年11月15日
 */
public class SystemResult implements Serializable {

	private static final long serialVersionUID = 1L;

	private static SystemResult result = new SystemResult();
	private static String cachePath = "cache/result.cache";
	private static File cacheFile = null;
	private static boolean cacheOpened = false;
	
	//存储annotation任务的结果，Map<系统名字,Map<数据集名字,Map<文档title,title对应的annotation集合>>>
	private Map<String, Map<String, Map<String, Set<Annotation>>>> annotationCache 
					= new HashMap<String, Map<String, Map<String,Set<Annotation>>>>();
	private Map<String, Map<String, Map<String, Set<Mention>>>> mentionCache 
					= new HashMap<String, Map<String, Map<String,Set<Mention>>>>();
	private Map<String, Map<String, Map<String, Set<NIL>>>> NILCache 
					= new HashMap<String, Map<String, Map<String,Set<NIL>>>>();
	private Map<String, Map<String, Map<String, Set<Candidate>>>> candidateCache 
					= new HashMap<String, Map<String, Map<String,Set<Candidate>>>>();
	private Map<String, Map<String, Map<String, Set<Entity>>>> entityCache 
					= new HashMap<String, Map<String, Map<String,Set<Entity>>>>();
	private Map<String, Map<String, Map<String, Long>>> costTime 
					= new HashMap<String, Map<String, Map<String,Long>>>();
	
	private SystemResult() { }
	
	public void flush()	{
		if (cacheOpened) {
			try {
				cacheFile.createNewFile();
				ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(cacheFile));
				oos.writeObject(result);
				oos.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
	}
	
	/**
	 * 加载结果，cache为true表示加载缓存结果
	 * 
	 * @param cached
	 * @return
	 */
	public static SystemResult getInstance(boolean cached) {
		cacheOpened = cached;
		if (cached == true) {
			cacheFile = new File(cachePath);
			if (cacheFile.exists() && cacheFile.length() > 0) {
				try {
					result = (SystemResult)new ObjectInputStream(new FileInputStream(cacheFile)).readObject();
				} catch (ClassNotFoundException | IOException e) {
					throw new RuntimeException(
							"Could Not Load Cache File "
									+ cacheFile.getAbsolutePath()
									+ ". Try to Manually Delete The File to Clear The Cache. Message: "
									+ e.getMessage());
				}
			}				
		}
		return result;
	}
	
	/**
	 * 是否缓存通过annotationCache来判断
	 * 
	 * @param systemName
	 * @param datasetName
	 * @return
	 */
	public boolean isCached(String systemName, String datasetName) {
		if (cacheOpened == false ||
				this.annotationCache.get(systemName) == null ||
					this.annotationCache.get(systemName).get(datasetName) == null)
			return false;
		return true;
	}
	
	/**
	 * 返回特定某个系统的annotation结果
	 * 
	 * @param systemName
	 * 
	 * @return
	 */
	public Map<String, Set<Annotation>> getAnnotationCache(String systemName, String datasetName) {
		return annotationCache.get(systemName).get(datasetName);
	}

	public void setAnnotationCache(String systemName, String datasetName, Map<String, Set<Annotation>> annotationMap) {
		Map<String, Map<String, Set<Annotation>>> map = new HashMap<String, Map<String,Set<Annotation>>>();
		map.put(datasetName, annotationMap);
		this.annotationCache.put(systemName, map);
		
	}

	public Map<String, Set<Mention>> getMentionCache(String systemName, String datasetName) {
		return mentionCache.get(systemName).get(datasetName);
	}

	public void setMentionCache(String systemName, String datasetName, Map<String, Set<Mention>> mentionCache) {
		Map<String, Map<String, Set<Mention>>> map = new HashMap<String, Map<String,Set<Mention>>>();
		map.put(datasetName, mentionCache);
		this.mentionCache.put(systemName, map);
	}

	public Map<String, Set<NIL>> getNILCache(String systemName, String datasetName) {
		return NILCache.get(systemName).get(datasetName);
	}

	public void setNILCache(String systemName, String datasetName, Map<String, Set<NIL>> nILCache) {
		Map<String, Map<String, Set<NIL>>> map = new HashMap<String, Map<String,Set<NIL>>>();
		map.put(datasetName, nILCache);
		NILCache.put(systemName, map);
	}

	public Map<String, Set<Candidate>> getCandidateCache(String systemName, String datasetName) {
		return candidateCache.get(systemName).get(datasetName);
	}

	public void setCandidateCache(String systemName, String datasetName, Map<String, Set<Candidate>> candidateCache) {
		Map<String, Map<String, Set<Candidate>>> map = new HashMap<String, Map<String,Set<Candidate>>>();
		map.put(datasetName, candidateCache);
		this.candidateCache.put(systemName, map);
	}
	
	public Map<String, Set<Entity>> getEntityCache(String systeName, String datasetName) {
		return this.entityCache.get(systeName).get(datasetName);
	}
	
	public void setEntityCache(String systemName, String datasetName, Map<String, Set<Entity>> entityMap) {
		Map<String, Map<String, Set<Entity>>> map = new HashMap<String, Map<String,Set<Entity>>>();
		map.put(datasetName, entityMap);
		this.entityCache.put(systemName, map);
	}

	public Map<String, Long> getCostTime(String systemName, String datasetName) {
		return costTime.get(systemName).get(datasetName);
	}

	public void setCostTime(String systemName, String datasetName, Map<String, Long> costTime) {
		Map<String, Map<String, Long>> map = new HashMap<String, Map<String,Long>>();
		map.put(datasetName, costTime);
		this.costTime.put(systemName, map);
	}
	
	
}
