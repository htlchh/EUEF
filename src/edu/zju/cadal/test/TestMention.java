package edu.zju.cadal.test;


import org.junit.Assert;
import org.junit.Test;

import edu.zju.cadal.model.Mention;

/**
 * @author:chenhui 
 * @email:chenhuicn@126.com
 * @date:2015年11月14日
 */
public class TestMention {

	@Test
	public void test_equal() {
		
		Mention m3 = new Mention(5, 4, 0.5f);
		Mention m4 = new Mention(5, 4, 0.6f);
		Assert.assertEquals(m3.equals(m4), true);
		Assert.assertEquals(m3.hashCode() == m4.hashCode(), true);
		
		Mention m5 = new Mention(5, 4, 0.5f);
		Mention m6 = new Mention(5, 2, 0.6f);
		Assert.assertEquals(m5.equals(m6), false);		
	}
	
	@Test
	public void test_compare() {
		
		Mention m3 = new Mention(2, 4, 0.5f);
		Mention m4 = new Mention(5, 4, 0.6f);
		Assert.assertEquals(m3.compareTo(m4), -3);
		
		Mention m5 = new Mention(5, 4, 0.5f);
		Mention m6 = new Mention(3, 2, 0.6f);
		Assert.assertEquals(m5.compareTo(m6), 2);		
	}

}
