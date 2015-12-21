package edu.zju.cadal.dataset;
/**
 * AQUAINT's format is same with MSNBC 
 *
 */
public class AQUAINT extends MSNBC {

	@Override
	public String getName()	 {
		return "AQUAINT";
	}
	
	public AQUAINT(String rawTextFolder, String problemFolder) {
		super(rawTextFolder, problemFolder);
	}
}
