package edu.zju.cadal.system;

import edu.zju.cadal.cache.SystemResult;
import edu.zju.cadal.dataset.AbstractDataset;

/**
 * @author:chenhui 
 * @email:chenhuicn@126.com
 * @date:2015年11月14日
 */
public abstract class AbstractERDSystem {

	abstract public String getName();
	
	
	/**
	 * entity recognization and disambiguation
	 * 
	 * 对整个数据集进行ERD任务
	 * 
	 * @param ds
	 */
	abstract public SystemResult erd(AbstractDataset ds);
	
	
}
