package edu.zju.cadal.priorer;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import edu.stanford.nlp.ie.AbstractSequenceClassifier;
import edu.stanford.nlp.ie.crf.CRFClassifier;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.util.Triple;
import edu.zju.cadal.model.Mention;
import edu.zju.cadal.utils.Timer;

/**
 * @author:chenhui 
 * @email:chenhuicn@126.com
 * @date:2015年11月18日
 */
public class NER extends Recognizer{
	
	private AbstractSequenceClassifier<CoreLabel> stanford;

	public NER() {
		try {
			this.stanford = CRFClassifier.getClassifier(
					"/home/chenhui/F/Stanford/stanford-ner-2015-04-20/classifiers/english.conll.4class.distsim.crf.ser.gz");
		} catch (ClassCastException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public String getName() {
		return "NER";
	}

	@Override
	public Set<Mention> recognize(String text, Timer timer) {
		Set<Mention> mentionSet = new HashSet<Mention>();
		try {
			List<Triple<String,Integer,Integer>> triples = stanford.classifyToCharacterOffsets(text);
	        for (Triple<String,Integer,Integer> trip : triples) {
	        	Mention mention = new Mention(trip.second, trip.third-trip.second, 1.0f);
	        	mentionSet.add(mention);
	        }			
		} catch (Exception e) {
			e.printStackTrace();
		}
		return mentionSet;		
	}

}
