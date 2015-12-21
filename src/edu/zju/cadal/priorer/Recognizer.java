package edu.zju.cadal.priorer;

import java.util.Set;

import edu.zju.cadal.model.Mention;
import edu.zju.cadal.utils.Timer;

/**
 * Class for mention recognition.
 * */
public abstract class Recognizer {

	abstract public String getName();
	
	abstract public Set<Mention> recognize(String text, Timer timer);
}
