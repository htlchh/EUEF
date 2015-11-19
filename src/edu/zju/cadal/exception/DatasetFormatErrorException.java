package edu.zju.cadal.exception;

public class DatasetFormatErrorException extends Exception {
	
	private static final long serialVersionUID = 1L;

	public DatasetFormatErrorException() {
		super("The dataset's annotations have wrong format.");
	}
	
	public DatasetFormatErrorException(String msg) {
		super(msg);
	}
}
