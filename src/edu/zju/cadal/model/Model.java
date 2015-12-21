package edu.zju.cadal.model;

public interface Model {

	
	public boolean equals(Object object);
	
	
	public int hashCode();
	
	/**
	 * Get embedded mention in various models
	 * */
	public Mention getMention();
	
	
	public int getLength();
}
