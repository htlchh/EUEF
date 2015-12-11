package edu.zju.cadal.dataset;

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
 * @date:2015年11月16日
 */
public class DatasetDumper {

	public static int getMentionCount(AbstractDataset ds) {
		Map<String, Set<Mention>> goldMentionMap = ds.getGoldMention();
		int count = 0;
		for (String title : goldMentionMap.keySet()) 
			count += goldMentionMap.get(title).size();
		return count;
	}
	
	public static int getCandidateCount(AbstractDataset ds)	{
		Map<String, Set<Candidate>> goldCandidate = ds.getGoldCandidate();
		int count = 0;
		for (String title : goldCandidate.keySet())
			count += goldCandidate.get(title).size();
		return count;
	}
	
	public static int getAnnotationCount(AbstractDataset ds) {
		Map<String, Set<Annotation>> goldAnnotation = ds.getGoldAnnotation();
		int count = 0;
		for (String title : goldAnnotation.keySet())
			count += goldAnnotation.get(title).size();
		return count;
	}
	
	public static int getNILCount(AbstractDataset ds) {
		Map<String, Set<NIL>> goldNIL = ds.getGoldNIL();
		int count = 0;
		for (String title : goldNIL.keySet())
			count += goldNIL.get(title).size();
		return count;
	}
	
	public static long getLongestText(AbstractDataset ds) {
		long longest = 0;
		for (String s : ds.getRawText().values())
			if (s.length() > longest)
				longest = s.length();
		return longest;
	}
	
	public static long getLength(AbstractDataset ds) {
		long length = 0;
		for (String s : ds.getRawText().values())
			length += s.length();
		return length;
	}
	
	public static int getEntityCount(AbstractDataset ds) {
		Map<String, Set<Entity>> goldEntity = ds.getGoldEntity();
		int count = 0;
		for (String title : goldEntity.keySet())
			count += goldEntity.get(title).size();
		return count;
	}
	
	public static int getDistinctEntity(AbstractDataset ds) {
		Set<Entity> entitySet = new HashSet<Entity>();
		for (Set<Entity> es : ds.getGoldEntity().values())
			entitySet.addAll(es);
		return entitySet.size();
	}
	
	public static int getEmptyDocumentCount(AbstractDataset ds) {
		int count = 0;
		Map<String, Set<Mention>> goldMention = ds.getGoldMention();
		for (String title : goldMention.keySet()) {
			if (goldMention.get(title).isEmpty() == true)
				count ++;
		}
		return count;
	}
	
	public static void statistics(AbstractDataset ds) {
		int size = ds.getSize();
		int aCount = getAnnotationCount(ds);
		int mCount = getMentionCount(ds);
		int eCount = getEntityCount(ds);
		int nCount = getNILCount(ds);
		int cCount = getCandidateCount(ds);
		int diseCount = getDistinctEntity(ds);
		long longest = getLongestText(ds);
		long length = getLength(ds);
		int emptyDocCount = getEmptyDocumentCount(ds);
		
		System.out.println(
				"Dataset Size: " + size
				+ "\nAnnotation Count: " + aCount
				+ "\nMention Count: " + mCount
				+ "\nEntity Count: " + eCount
				+ "\nNIL Count: " + nCount
				+ "\nCandidate Count: " + cCount
				+ "\nDistinct Entity Count: " + diseCount
				+ "\nLongest Text Length: " + longest
				+ "\nAverage Text Length: " + length/(float)size
				+ "\nEmtpy Document Count: " + emptyDocCount
		);
	}
}
