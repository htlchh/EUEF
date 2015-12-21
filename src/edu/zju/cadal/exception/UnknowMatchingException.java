package edu.zju.cadal.exception;

public class UnknowMatchingException extends RuntimeException{
	private static final long serialVersionUID = 1L;

	public UnknowMatchingException() {
		super("If You Add a New Matching Metric, It is Recommened to Add Some Code Here Referring to The Above Template.");
	}
	
	public UnknowMatchingException(String msg) {
		super(msg);
	}	
}
