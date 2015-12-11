package edu.zju.cadal.dataset;
/**
 * @author:chenhui 
 * @email:chenhuicn@126.com
 * @date:2015年11月26日
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
