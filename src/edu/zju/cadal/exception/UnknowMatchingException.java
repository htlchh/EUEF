package edu.zju.cadal.exception;
/**
 * @author:chenhui 
 * @email:chenhuicn@126.com
 * @date:2015年11月19日
 */
public class UnknowMatchingException extends RuntimeException{
	private static final long serialVersionUID = 1L;

	public UnknowMatchingException() {
		super("If You Add a New Matching Metric, It is Recommened to Add Some Code Here Referring to The Above Template.");
	}
	
	public UnknowMatchingException(String msg) {
		super(msg);
	}	
}
