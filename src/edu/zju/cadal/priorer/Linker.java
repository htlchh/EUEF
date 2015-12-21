package edu.zju.cadal.priorer;

import java.util.Set;

import edu.zju.cadal.model.Candidate;
import edu.zju.cadal.model.Mention;
import edu.zju.cadal.utils.Timer;

/**
 * Generate candidates for mentions
 * */
public abstract class Linker {

	abstract public Set<Candidate> link(Set<Mention> mentionSet, String text, Timer timer);
	
	abstract public String getName();	
	
	
	
}
