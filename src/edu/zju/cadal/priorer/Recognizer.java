package edu.zju.cadal.priorer;

import java.util.Set;

import edu.zju.cadal.model.Mention;
import edu.zju.cadal.utils.Timer;

/**
 * @author:chenhui 
 * @email:chenhuicn@126.com
 * @date:2015年11月18日
 */
public abstract class Recognizer {

	
	
	abstract public String getName();
	
	
	
	abstract public Set<Mention> recognize(String text, Timer timer);
}
