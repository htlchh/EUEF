package edu.zju.cadal.crosswiki;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.util.CharTokenizer;

/**
 * 
 * split titles according to '_'
 * transform titles into lower case 
 * @author:chenhui 
 * @email:chenhuicn@126.com
 * @date:2015年9月30日
 */
public class TitleAnalyzer extends Analyzer{

	@Override
	protected TokenStreamComponents createComponents(String fieldName) {
		return new TokenStreamComponents(new TitleTokenizer());
	}
	
	
	private class TitleTokenizer extends CharTokenizer {	

		@Override
		protected int normalize(int c) {
			return Character.toLowerCase(c);
		}
		  
		@Override
		protected boolean isTokenChar(int c) {
			char ch = (char)c;
			if (ch == '_')
				return false;
			if (Character.isWhitespace(c))
				return false;
			return true;
		}
	}
	
}
