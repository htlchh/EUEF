package edu.zju.cadal.priorer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.apache.lucene.document.Document;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.xml.sax.SAXException;

import edu.zju.cadal.crosswiki.CoreDictSearcher;
import edu.zju.cadal.model.Candidate;
import edu.zju.cadal.model.Entity;
import edu.zju.cadal.model.Mention;
import edu.zju.cadal.utils.Pair;
import edu.zju.cadal.utils.Timer;
import edu.zju.cadal.webservice.MediaWikiAPI;

/**
 * Generate candidates by searching CrossWikis.
 * The CrossWikis is indexed by Lucene.
 * CrossWikis: http://www-nlp.stanford.edu/pubs/crosswikis-data.tar.bz2/
 * 
 */
public class CrossWikiLinker extends Linker {
	
	private CoreDictSearcher coreDictSearcher;
	private MediaWikiAPI api = MediaWikiAPI.getInstance();
	
	public CrossWikiLinker() {
		this.coreDictSearcher = 
				new CoreDictSearcher("/home/chenhui/Data/lucene_index/crosswikis_index/core_dict_index");
	}

	@Override
	public Set<Candidate> link(Set<Mention> mentionSet, String text, Timer timer) {
		Set<Candidate> cSet = generateCadidate(mentionSet, text, timer);
		return cSet;
	}
	
	/**
	 * Generate a set of candidates for mentions
	 * 
	 * @param mentionSet
	 * 		A set of scored mentions
	 * @param text
	 * 		The raw text where scored mentions located
	 * @param timer
	 * 		For recording cost time
	 * @return
	 * 		A set of mention candidates
	 */
	private Set<Candidate> generateCadidate(Set<Mention> mentionSet, String text, Timer timer) {
		long costTime = Calendar.getInstance().getTimeInMillis();
		Set<Candidate> candidateSet = new HashSet<Candidate>();
		for (Mention mention : mentionSet) {
			String title = text.substring(mention.getPosition(), mention.getPosition()+mention.getLength());
			TopDocs topDocs = coreDictSearcher.search("mention", title);
			candidateSet.add(parseSearchResult(topDocs, mention));
		}
		timer.setCostTime(Calendar.getInstance().getTimeInMillis() - costTime);
		return candidateSet;
	}
	
	/**
	 * Parse TopDocs which is returned by a Lucene query.
	 * Get candidate for the given mention
	 * 
	 * @param topDocs
	 * 		Returned value by Lucene
	 * @param mention
	 * 		One query scored mentions
	 * @return
	 * 		
	 * @throws IOException
	 */
	private Candidate parseSearchResult(TopDocs topDocs, Mention mention) {
		ScoreDoc[] scoreDocs = topDocs.scoreDocs;
		Set<Pair<Entity, Float>> pairSet = new HashSet<Pair<Entity,Float>>();
		Map<String, Float> searchMap = new HashMap<String, Float>();
		
		try {
			/** If two entities are returned for a same mention with different confidence scores, then choose the one with maximum score*/
			for (int i = 0; i < scoreDocs.length; i++) 
			{
				Document document = coreDictSearcher.getSearcher().doc(scoreDocs[i].doc);
				
				String wikiTitle = document.getField("url").stringValue();
				float score = Float.parseFloat(document.getField("cprob").stringValue());
				
				if (searchMap.containsKey(wikiTitle) == true) {
					float s = score > searchMap.get(wikiTitle) ? score : searchMap.get(wikiTitle);
					searchMap.put(wikiTitle, s);
				} else
					searchMap.put(wikiTitle, score);
			}	
			
			Set<String> titleSet = new HashSet<String>();
			for (String title : searchMap.keySet())
				titleSet.add(title);
			
			List<String> titleToPrefetch = new ArrayList<String>();
			titleToPrefetch.addAll(titleSet);
			api.prefetchTitle(titleToPrefetch);
			
			for (String title : searchMap.keySet()) {
				int wid = api.getIdByTitle(title);
				if (wid != -1)
					pairSet.add(new Pair<Entity, Float>(new Entity(wid, title), searchMap.get(title)));
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (XPathExpressionException e) {
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		}
		
		/** If no corresponding entities, it is a NIL*/
		if (pairSet.isEmpty() == true)
			pairSet.add(new Pair<Entity, Float>(new Entity(0), 1.0f));
		return new Candidate(mention, pairSet);
	}

	
	@Override
	public String getName() {
		return "CrossWiki Linker";
	}

	
}
