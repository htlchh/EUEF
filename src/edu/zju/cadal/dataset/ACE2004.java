package edu.zju.cadal.dataset;


/**
 * @author:chenhui 
 * @email:chenhuicn@126.com
 * @date:2015年11月26日
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
