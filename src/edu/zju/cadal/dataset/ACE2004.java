package edu.zju.cadal.dataset;


/**
 * ACE's format is same with MSNBC 
 *
 */
public class ACE2004 extends MSNBC {

	@Override
	public String getName() {
		return "ACE2004";
	}
	
	public ACE2004(String rawTextFolder, String problemFolder) {
		super(rawTextFolder, problemFolder);
	}
}
