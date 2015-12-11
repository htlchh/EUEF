package edu.zju.cadal.cache;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
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
	
	//存储annotation任务的结果，Map<系统名字+数据集名字,Map<文档title,title对应的annotation集合>>
	private Map<String, Map<String, Set<Annotation>>> annotationCache 
					= new HashMap<String, Map<String,Set<Annotation>>>();
	private Map<String, Map<String, Set<Mention>>> mentionCache 
					= new HashMap<String, Map<String,Set<Mention>>>();
	private Map<String, Map<String, Set<NIL>>> NILCache 
					= new HashMap<String, Map<String,Set<NIL>>>();
	private Map<String, Map<String, Set<Candidate>>> candidateCache 
					= new HashMap<String, Map<String,Set<Candidate>>>();
	private Map<String, Map<String, Set<Entity>>> entityCache 
					= new HashMap<String, Map<String,Set<Entity>>>();
	private Map<String, Map<String, Long>> costTime 
					= new HashMap<String, Map<String,Long>>();
	
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
				this.annotationCache.get(systemName+datasetName) == null)
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
		return annotationCache.get(systemName + datasetName);
	}
	
	/**
	 * 返回特定系统大于给定threshold的annotation结果
	 * 
	 * @param systemName
	 * @param datasetName
	 * @param threshold
	 * @return
	 */
	public Map<String, Set<Annotation>> getAnnotationCache(String systemName, String datasetName, float threshold) {
		Map<String, Set<Annotation>> retMap = new HashMap<String, Set<Annotation>>();
		Map<String, Set<Annotation>> resultMap = this.annotationCache.get(systemName + datasetName);
		for (String title : resultMap.keySet()) 
		{
			for (Annotation a : resultMap.get(title)) 
			{
				if (retMap.containsKey(title) == false)
					retMap.put(title, new HashSet<Annotation>());
				
				if (a.getScore() >= threshold)
					retMap.get(title).add(a);
			}
		}
		return retMap;
	}

	public void setAnnotationCache(String systemName, String datasetName, Map<String, Set<Annotation>> annotationMap) {
		this.annotationCache.put(systemName+datasetName, annotationMap);
		
	}

	public Map<String, Set<Mention>> getMentionCache(String systemName, String datasetName) {
		return mentionCache.get(systemName + datasetName);
	}

	public Map<String, Set<Mention>> getMentionCache(String systemName, String datasetName, float threshold) {
		Map<String, Set<Mention>> retMap = new HashMap<String, Set<Mention>>();
		Map<String, Set<Mention>> resultMap = this.mentionCache.get(systemName + datasetName);
		for (String title : resultMap.keySet()) {
			for (Mention m : resultMap.get(title)) {
				if (retMap.containsKey(title) == false)
					retMap.put(title, new HashSet<Mention>());
				
				if (m.getScore() >= threshold) 
					retMap.get(title).add(m);
			}
		}
		return retMap;
	}
	
	
	public void setMentionCache(String systemName, String datasetName, Map<String, Set<Mention>> mentionCache) {
		this.mentionCache.put(systemName + datasetName, mentionCache);
	}

	public Map<String, Set<NIL>> getNILCache(String systemName, String datasetName) {
		return NILCache.get(systemName + datasetName);
	}
	
	public Map<String, Set<NIL>> getNILCache(String systemName, String datasetName, float threshold) {
		Map<String, Set<NIL>> retMap = new HashMap<String, Set<NIL>>();
		Map<String, Set<NIL>> resultMap = this.NILCache.get(systemName + datasetName);
		
		for (String title : resultMap.keySet()) { 
			if (retMap.containsKey(title) == false) {
				retMap.put(title, new HashSet<NIL>());
			}
			for (NIL n : resultMap.get(title)) {
				if (n.getScore() >= threshold)
					retMap.get(title).add(n);
			}
		}
		return retMap;
	}

	public void setNILCache(String systemName, String datasetName, Map<String, Set<NIL>> nILCache) {
		NILCache.put(systemName + datasetName, nILCache);
	}

	public Map<String, Set<Candidate>> getCandidateCache(String systemName, String datasetName) {
		return candidateCache.get(systemName + datasetName);
	}
	
	public void setCandidateCache(String systemName, String datasetName, Map<String, Set<Candidate>> candidateCache) {
		this.candidateCache.put(systemName + datasetName, candidateCache);
	}
	
	public Map<String, Set<Entity>> getEntityCache(String systeName, String datasetName) {
		return this.entityCache.get(systeName + datasetName);
	}
	
	public void setEntityCache(String systemName, String datasetName, Map<String, Set<Entity>> entityMap) {
		this.entityCache.put(systemName + datasetName, entityMap);
	}

	public Map<String, Long> getCostTime(String systemName, String datasetName) {
		return costTime.get(systemName + datasetName);
	}

	public void setCostTime(String systemName, String datasetName, Map<String, Long> costTime) {
		this.costTime.put(systemName + datasetName, costTime);
	}
	
	public void remove(String systemName, String datasetName) {
		if (this.mentionCache.containsKey(systemName+datasetName))
			this.mentionCache.remove(systemName+datasetName);
		if (this.candidateCache.containsKey(systemName+datasetName))
			this.candidateCache.remove(systemName+datasetName);
		if (this.entityCache.containsKey(systemName+datasetName))
			this.entityCache.remove(systemName+datasetName);
		if (this.annotationCache.containsKey(systemName+datasetName))
			this.annotationCache.remove(systemName+datasetName);
		if (this.NILCache.containsKey(systemName+datasetName))
			this.NILCache.remove(systemName+datasetName);
		if (this.costTime.containsKey(systemName+datasetName))
			this.costTime.remove(systemName+datasetName);
	}
	
}
