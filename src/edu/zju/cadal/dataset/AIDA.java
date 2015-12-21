package edu.zju.cadal.dataset;

import it.unimi.dsi.lang.MutableString;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.xml.sax.SAXException;

import edu.zju.cadal.exception.DatasetFormatErrorException;
import edu.zju.cadal.model.Annotation;
import edu.zju.cadal.model.Candidate;
import edu.zju.cadal.model.Entity;
import edu.zju.cadal.model.Mention;
import edu.zju.cadal.model.NIL;
import edu.zju.cadal.utils.Pair;

/**
 * AIDA the base class for loading AIDA-formatted datasets: AIDA/TestA, AIDA/TestB, AIDA/Training
 * 
 * @author:chenhui 
 * @email:chenhuicn@126.com
 * @date:2015年11月26日
 */
public abstract class AIDA extends AbstractDataset {
	private Map<String, String> rawText = new HashMap<String, String>();
	private Map<String, Set<Annotation>> goldAnnotation = new HashMap<String, Set<Annotation>>();
	private Map<String, Set<Mention>> goldMention = new HashMap<String, Set<Mention>>();
	private Map<String, Set<NIL>> goldNIL = new HashMap<String, Set<NIL>>();
	private Map<String, Set<Candidate>> goldCandidate = new HashMap<String, Set<Candidate>>();
	private Map<String, Set<Entity>> goldEntity = new HashMap<String, Set<Entity>>();
	
	private Map<String, MutableString> documents = new HashMap<String, MutableString>();
	Map<String, List<AidaAnnotation>> problemMap = new HashMap<String, List<AidaAnnotation>>();

	/** pattern to match a Wikipedia url*/
	private Pattern wikiUrlPattern = Pattern.compile("http://en.wikipedia.org/wiki/(.*)");
	/** pattern to match a mention*/
	private Pattern mentionPattern = Pattern.compile("^(.*?)\t([BI]?)\t(.*?)\t(.*?)\t(.*?)(?:\t(.*))?$");
	/** pattern to match a NIL*/
	private Pattern nmePattern = Pattern.compile("^(.*)\t([BI])\t(.*)\t(.*)--NME--$");
	/** pattern to match a punctuation*/
	private Pattern punctuationPattern = Pattern.compile("^\\W.*$");
	
	
	public AIDA(String file) {
		try {
			load(file);
			fill();
			filter();
			unify();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Fill the member variables gold***
	 * 
	 * @throws XPathExpressionException
	 * @throws IOException
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 */
	private void fill() throws XPathExpressionException, IOException, ParserConfigurationException, SAXException {
		for (String title : documents.keySet()) {
			this.rawText.put(title, documents.get(title).toString());
			Set<Mention> mentionSet = new HashSet<Mention>();
			Set<Candidate> candidateSet = new HashSet<Candidate>();
			Set<Entity> entitySet = new HashSet<Entity>();
			Set<Annotation> annotationSet = new HashSet<Annotation>();
			Set<NIL> nILSet = new HashSet<NIL>();
			
			for (AidaAnnotation aa : problemMap.get(title)) {
				Mention m = new Mention(aa.surfaceForm, aa.position, aa.length);
				mentionSet.add(m);
				int wid = api.getIdByTitle(aa.title);
				if (wid == -1) { // NIL
					nILSet.add(new NIL(m));
					Entity e = new Entity(0, "*null*");
					entitySet.add(e);
					Set<Pair<Entity, Float>> pairSet = new HashSet<Pair<Entity, Float>>();
					pairSet.add(new Pair<Entity, Float>(e, 1.0f));
					candidateSet.add(new Candidate(m, pairSet));
				}
				else {
					annotationSet.add(new Annotation(m, new Entity(wid, aa.title)));
					Entity e = new Entity(wid, aa.title);
					entitySet.add(e);
					Set<Pair<Entity, Float>> pairSet = new HashSet<Pair<Entity, Float>>();
					pairSet.add(new Pair<Entity, Float>(e, 1.0f));
					candidateSet.add(new Candidate(m, pairSet));
				}
			}
			
			this.goldAnnotation.put(title, annotationSet);
			this.goldMention.put(title, mentionSet);
			this.goldCandidate.put(title, candidateSet);
			this.goldEntity.put(title, entitySet);
			this.goldNIL.put(title, nILSet);			
		}
	}

	/**
	 * Delete the documents which contain no annotations from all gold*** member variables
	 */
	private void filter() {
		Set<String> remove = new HashSet<String>();
		
		for (String title : goldMention.keySet())
			if (goldMention.get(title).isEmpty() == true)
				remove.add(title);
		
		for (String title : remove) {
			rawText.remove(title);
			goldMention.remove(title);
			goldAnnotation.remove(title);
			goldCandidate.remove(title);
			goldEntity.remove(title);
			goldNIL.remove(title);
		}
	}
	
	/**
	 * Check if the size of rawText is same with the size of goldMention
	 */
	private void unify() {
		Set<String> remove = new HashSet<String>();
		for (String title : rawText.keySet())
			if (goldMention.containsKey(title) == false)
				remove.add(title);
		
		for (String title : remove)
			rawText.remove(title);
		
		remove.clear();
		for (String title : goldMention.keySet())
			if (rawText.containsKey(title) == false)
				remove.add(title);
		
		for (String title : remove) {
			goldMention.remove(title);
			goldCandidate.remove(title);
			goldAnnotation.remove(title);
			goldEntity.remove(title);
		}
	}
	
	@Override
	abstract public String getName();

	
	/**
	 * Load and parse dataset
	 * @param file
	 * @throws Exception
	 */
	private void load(String file) throws Exception {
		List<String> titlesToPrefetch = new ArrayList<String>();
		BufferedReader r = new BufferedReader( new InputStreamReader(new FileInputStream(file), Charset.forName("UTF-8")));
		String line = null;
		String docTitle = null;
		
		int currentPos = -1, currentLen = 0;
		String currentTitle = null;
		String surfaceForm = null;
		
		int nmePos = -1, nmeLen = 0;
		String nmeSurfaceForm = null;
		
		MutableString currentDoc = null;
		List<AidaAnnotation> aaList = null;
		
		while ((line = r.readLine()) != null) 
		{
			Matcher m = mentionPattern.matcher(line);
			Matcher nmeMatch = nmePattern.matcher(line);
			MutableString append = new MutableString();
			
			/** if not a nme or another nme begins */
			if ((!nmeMatch.matches() || nmeMatch.matches() && nmeMatch.group(2).equals("B")) && nmePos != -1) {
				aaList.add(new AidaAnnotation(nmeSurfaceForm, nmePos, nmeLen, "*null*"));
				nmePos = -1;
				nmeLen = 0;
				nmeSurfaceForm = null;
			}
			
			/** if not a mention or another mention begins*/
			if ((!m.matches() || m.matches() && m.group(2).equals("B")) && currentPos != -1){
				aaList.add(new AidaAnnotation(surfaceForm, currentPos, currentLen, currentTitle));
				currentPos = -1;
				currentLen = 0;
				currentTitle = null;
				surfaceForm = null;
			}
			if (line.startsWith("-DOCSTART-")){ // a new document
				aaList = new ArrayList<AIDA.AidaAnnotation>();
				currentDoc = new MutableString();
				docTitle = line.substring(
								line.indexOf('(')+1,
								line.indexOf(')')
							);
				documents.put(docTitle, currentDoc);
				problemMap.put(docTitle, aaList);
			}
			else if (line.equals("")){ // the end of a sentence
				append.replace("\n");
			}
			else if (!m.matches() && !nmeMatch.matches()){ // a word not part of a mention
				append.replace(line + " ");
			}
			else if (nmeMatch.matches()){ // a word part of a non-recognized mention
				if (nmeMatch.group(2).equals("B")) {
					nmeSurfaceForm = nmeMatch.group(3);
					nmePos = currentDoc.length();
					nmeLen = nmeMatch.group(1).length();
				} else {
					if (!nmeMatch.group(2).equals("I") && !nmeMatch.group(2).equals("B")) {
						r.close();
						throw new DatasetFormatErrorException(this.getName());
					}
					nmeLen += nmeMatch.group(1).length() + 1;
				}
				append.replace(nmeMatch.group(1) + " ");
			}
			else { // a word with a recognized mention.
				if (m.group(2).equals("B")){
					surfaceForm = m.group(3);
					Matcher m2 = wikiUrlPattern.matcher(m.group(5));
					if (m2.matches()){
						currentTitle = m2.group(1);
						currentPos = currentDoc.length();
						currentLen = m.group(1).length();
						titlesToPrefetch.add(currentTitle);
					}
					else {
						r.close();
						throw new DatasetFormatErrorException(this.getName());
					}
				}
				else {
					if (!m.group(2).equals("B") && !m.group(2).equals("I")){
						r.close();
						throw new DatasetFormatErrorException(this.getName());
					 }// found mention is a continuation
					currentLen += m.group(1).length()+1;
				}
				append.replace(m.group(1) + " ");
			}

			//* Should the last whitespace be removed? */
			Matcher punctuationMatch = punctuationPattern.matcher(append);
			if (punctuationMatch.matches())
				currentDoc.trimRight();
			
			currentDoc.append(append);
		}
		r.close();

		/** Prefetch titles */
		api.prefetchTitle(titlesToPrefetch);
		api.flush();
	}
	
	private class AidaAnnotation{
		public AidaAnnotation(String surfaceForm, int pos, int len, String title) {
			this.surfaceForm = surfaceForm;
			this.length = len;
			this.position = pos;
			this.title = title;
		}
		public int length, position;
		public String title;
		public String surfaceForm;
	}

	@Override
	public int getSize() {
		return rawText.size();
	}

	@Override
	public Map<String, String> getRawText() {
		return this.rawText;
	}

	@Override
	public Map<String, Set<Mention>> getGoldMention() {
		return goldMention;
	}

	@Override
	public Map<String, Set<Annotation>> getGoldAnnotation() {
		return goldAnnotation;
	}

	@Override
	public Map<String, Set<NIL>> getGoldNIL() {
		return goldNIL;
	}

	@Override
	public Map<String, Set<Candidate>> getGoldCandidate() {
		return goldCandidate;
	}

	@Override
	public Map<String, Set<Entity>> getGoldEntity() {
		return goldEntity;
	}

}
