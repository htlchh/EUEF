package edu.zju.cadal.utils;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;

/**
 * Read bz2 compressed files
 */
public class BZ2 {
	
	InputStream is = null;
	BufferedReader reader;

	public BZ2(String dump) {
		try {
			is = new BZip2CompressorInputStream(new FileInputStream(new File(dump)));
			reader = new BufferedReader(new InputStreamReader(is));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public String readLine() throws IOException {
		return reader.readLine();
	}
	
	public void close() throws IOException {
		is.close();
		reader.close();
	}
}
