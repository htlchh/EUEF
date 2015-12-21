package edu.zju.cadal.crosswiki;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.util.CharTokenizer;

/**
 * 
 * CrossWikis索引分析器,统一转换成小写,不做其他处理
 * Analyzer, transform the characters into lowercase 
 * 
 * @author:chenhui 
 * @email:chenhuicn@126.com
 * @date:2015年9月30日
 */
public class CrossWikiAnalyzer extends Analyzer{

	@Override
	protected TokenStreamComponents createComponents(String fieldName) {
		return new TokenStreamComponents(new TitleTokenizer());
	}
	
	
	private class TitleTokenizer extends CharTokenizer {	

		@Override
		protected int normalize(int c) {
			return Character.toLowerCase(c);
		}
		  
		/**
		 * 根据下划线和空格分割
		 */
		@Override
		protected boolean isTokenChar(int c) {
			return true;
		}
	}
	
}
