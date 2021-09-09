package com.gwtech.in.test;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class ReadEps {
	
	public static void main(String[] args) {
		
		readLineToCheckExtraLines("/Users/administrator/Desktop/ue003-001-9780323654111.eps");
	}
	
	
	public static String readLineToCheckExtraLines(String path) {
		
		StringBuffer buffer = new StringBuffer();
		
		try {
			BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(path), StandardCharsets.UTF_8));
			
			String line = "";
			while((line = bufferedReader.readLine()) != null) {
				
				System.out.println(line);
			}
			
			bufferedReader.close();

		} catch (Exception exception) {
			exception.printStackTrace();
		}
		return buffer.toString();
	}
}
