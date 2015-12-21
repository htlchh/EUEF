package edu.zju.cadal.system;

import edu.zju.cadal.cache.Prediction;
import edu.zju.cadal.dataset.AbstractDataset;


/**
 * Systems to do ERD task
 * */
public abstract class AbstractERDSystem {

	abstract public String getName();
	
	abstract public Prediction erd(AbstractDataset ds);
	
	
}
