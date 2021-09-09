package com.gwtech.in.service.impl;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;

import org.apache.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.gwtech.in.service.FileWriterI;
import com.gwtech.in.service.TAFloatItemOrdering;
import com.gwtech.in.utils.Constants;
import com.gwtech.in.utils.MiscUtility;

public class FileWriterImpl implements FileWriterI {
	
	private MiscUtility miscUtility;
	private TAFloatItemOrdering taFloatItemOrdering;
	private static final Logger logger = Logger.getLogger(FileWriterImpl.class);
	
	/**
	 * write
	 * @param content
	 * @param path
	 * @param append
	 */
	public void write(String content, String path, Boolean append) {

//      String content = "This is the content to write into file\n";

		// If the file doesn't exists, create and write to it
		// If the file exists, truncate (remove all content) and write to it
		try {
			File file = new File(path);
			file.getParentFile().mkdirs();
			if (file.exists() == false) {
				file.createNewFile();
			}
			BufferedWriter bw = new BufferedWriter(
					new OutputStreamWriter(new FileOutputStream(path, append), StandardCharsets.UTF_8));
			bw.write(content);
			bw.close();

		} catch (Exception exception) {
			exception.printStackTrace();
		}

	}
	
	/**
	 * readLineToCheckBr
	 * @param path
	 * @param tableCellText
	 */
	@Override
	public int readLineToCheckBr(String path, String tableCellText, Integer lineNum) {
		
		
		Boolean valid = false;
		
		if ((tableCellText.equalsIgnoreCase("\t"))
//				|| (tableCellText.equalsIgnoreCase("•\t"))
				|| (tableCellText.length() == 1)
				)
			return lineNum;
		
		try {
			BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(path), StandardCharsets.UTF_8));
			
			String line = "";
			while((line = bufferedReader.readLine()) != null) {
				
				if (line.endsWith("[read]"))	continue;
				
				if (line.contains(tableCellText)) {
					valid = true;
					lineNum ++;
					break;
				}
				
			}
			
			bufferedReader.close();
			
			
			if (valid == false) {
				
				lineNum = 0;
				bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(path), StandardCharsets.UTF_8));
				while((line = bufferedReader.readLine()) != null) {
					
	//				if (line.endsWith("[read]"))	continue;
					if (line.endsWith("[read]")) {
						if (line.contains(tableCellText)) {
							valid = true;
							lineNum ++;
							break;
						}
						lineNum ++;
					}
				}
				bufferedReader.close();
			}
			
			
			if (valid) 
				valid = updateLineinTextFile(path, line);
				
			
		} catch (Exception exception) {
			exception.printStackTrace();
		}
		return lineNum;
	}
	
	
	/**
	 * updateLineinTextFile
	 * @param path
	 * @param lineToEdit
	 * @return
	 */
	private Boolean updateLineinTextFile(String path, String lineToEdit) {
		Boolean valid = false;
		try {
			BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(path), StandardCharsets.UTF_8));
			
			String line = "";
			StringBuffer buffer = new StringBuffer();
			
			while((line = bufferedReader.readLine()) != null) {
				
//				if (line.endsWith("[read]"))	continue;
				
				if (line.equalsIgnoreCase(lineToEdit)) {
					if (line.endsWith("[read]") == false)
						line = line + "\t[read]";
					valid = true;
				}
				buffer.append(line+"\n");
			}
			
			bufferedReader.close();
			write(buffer.toString(), path, false);

		} catch (Exception exception) {
			exception.printStackTrace();
		}
		return valid;
	}
	
	public static void main(String[] args) {
		
		new FileWriterImpl().readLineToCheckExtraLines("/Users/administrator/Documents/TA-pre-editing-check-list/corss/TEMP.txt", "Chapter_1_Health_and_Wellness.txt");
	}
	
	/**
	 * readLineToCheckExtraLines
	 * @param path
	 */
	@Override
	public String readLineToCheckExtraLines(String path, String fileName) {
		
		StringBuffer buffer = new StringBuffer();
		
		try {
			
			softEnterHandling(path);
			
			BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(path), StandardCharsets.UTF_8));
			JSONArray array = HttpRequestImpl.get("auth/universal-char-request?type=universal");
			
			String line = "";
			
			while((line = bufferedReader.readLine()) != null) {
				
//				filter for TA assistance
//				delete double spaces
				
				if (line.contains("Specific Details of the Chest Pain History"))
					logger.debug(line);

				
				while (line.contains("  "))	line = line.replace("  ", " ");
				line = line.replace("[[[SOFT-BREAK-ENTRY]]][[[SOFT-BREAK-ENTRY]]]", "[[[SOFT-BREAK-ENTRY]]]");
//				line = line.replace("@Normal:", "@Text flush:");
//				delete para start and end spaces
				line = miscUtility.removeStartEndSpaces(line);
				
				//cross-ref handling
				Boolean isFloatItem = false;
				
				String lineWithoutMarginPadding = removeMarginPading(line);
				lineWithoutMarginPadding = removeParaStyle(lineWithoutMarginPadding);
				isFloatItem = taFloatItemOrdering.floatItemCheckLog(lineWithoutMarginPadding, false);
				if ( ! isFloatItem)
					line = handleCrossRefsOccurances(line);
				
				buffer.append(line+"\n");
			}
			
			bufferedReader.close();

		} catch (Exception exception) {
			exception.printStackTrace();
		}
		return buffer.toString();
	}
	
	private String removeParaStyle(String lineText) {
		
		if ((lineText.startsWith("@")) && (lineText.indexOf(":") > 0 )) {
			
			String paraStyleNameOld = "";
			paraStyleNameOld = lineText.substring(lineText.indexOf("@"), lineText.indexOf(":") + 1);
			lineText = lineText.replace(paraStyleNameOld, "");
			
			if (paraStyleNameOld.startsWith("@"))	paraStyleNameOld = paraStyleNameOld.substring(1);
			if (paraStyleNameOld.endsWith(":"))	paraStyleNameOld = paraStyleNameOld.substring(0, (paraStyleNameOld.length() - 1));
			
			lineText = lineText.replace("<@alert-red-open><"+(paraStyleNameOld)+"><@alert-red-close>", "");
		}
		return lineText;
	}

	public String figureCrossMarking(String line) {
		
		//		Fig. 1-5
		
//		Figs. 28-1 and 28-2
		
		boolean isFound = false;
		
		line = handleFigRangesDelimeters(line);
		
		
		/**
		 * multiple occurance handling
		 * 
		 */
		
		line = line.replaceAll("(?i)Fig\\. ([0-9]+)\\-([0-9]+)", "<@cross-ref-fig-open>Fig\\. $1\\-$2<@cross-ref-fig-close>"); // simple dash
		line = line.replaceAll("(?i)Fig\\. ([0-9]+)\\—([0-9]+)", "<@cross-ref-fig-open>Fig\\. $1\\—$2<@cross-ref-fig-close>"); // em-dash
		line = line.replaceAll("(?i)Fig\\. ([0-9]+)\\–([0-9]+)", "<@cross-ref-fig-open>Fig\\. $1\\–$2<@cross-ref-fig-close>"); // en-dash
		line = line.replaceAll("(?i)Fig\\. ([0-9]+)\\.([0-9]+)", "<@cross-ref-fig-open>Fig\\. $1\\.$2<@cross-ref-fig-close>"); // simple period
		
		line = line.replaceAll("(?i)Fig ([0-9]+)\\-([0-9]+)", "<@cross-ref-fig-open>Fig $1\\-$2<@cross-ref-fig-close>"); // simple dash
		line = line.replaceAll("(?i)Fig ([0-9]+)\\—([0-9]+)", "<@cross-ref-fig-open>Fig $1\\—$2<@cross-ref-fig-close>"); // em-dash
		line = line.replaceAll("(?i)Fig ([0-9]+)\\–([0-9]+)", "<@cross-ref-fig-open>Fig $1\\–$2<@cross-ref-fig-close>"); // en-dash
		line = line.replaceAll("(?i)Fig ([0-9]+)\\.([0-9]+)", "<@cross-ref-fig-open>Fig $1\\.$2<@cross-ref-fig-close>"); // simple period
		
		
		line = line.replaceAll("(?i)Figure\\. ([0-9]+)\\-([0-9]+)", "<@cross-ref-fig-open>Figure\\. $1\\-$2<@cross-ref-fig-close>"); // simple dash
		line = line.replaceAll("(?i)Figure\\. ([0-9]+)\\—([0-9]+)", "<@cross-ref-fig-open>Figure\\. $1\\—$2<@cross-ref-fig-close>"); // em-dash
		line = line.replaceAll("(?i)Figure\\. ([0-9]+)\\–([0-9]+)", "<@cross-ref-fig-open>Figure\\. $1\\–$2<@cross-ref-fig-close>"); // en-dash
		line = line.replaceAll("(?i)Figure\\. ([0-9]+)\\.([0-9]+)", "<@cross-ref-fig-open>Figure\\. $1\\.$2<@cross-ref-fig-close>"); // simple period
		
		line = line.replaceAll("(?i)Figure ([0-9]+)\\-([0-9]+)", "<@cross-ref-fig-open>Figure $1\\-$2<@cross-ref-fig-close>"); // simple dash
		line = line.replaceAll("(?i)Figure ([0-9]+)\\—([0-9]+)", "<@cross-ref-fig-open>Figure $1\\—$2<@cross-ref-fig-close>"); // em-dash
		line = line.replaceAll("(?i)Figure ([0-9]+)\\–([0-9]+)", "<@cross-ref-fig-open>Figure $1\\–$2<@cross-ref-fig-close>"); // en-dash
		line = line.replaceAll("(?i)Figure ([0-9]+)\\.([0-9]+)", "<@cross-ref-fig-open>Figure $1\\.$2<@cross-ref-fig-close>"); // simple period
		
		
		/**
		 * 
		 * single space handling
		 * 
		 */
		
		line = line.replaceAll("(?i)Fig\\. ([0-9]+)\\- ([0-9]+)", "<@cross-ref-fig-open>Fig\\. $1\\-$2<@cross-ref-fig-close>"); // simple dash
		line = line.replaceAll("(?i)Fig\\. ([0-9]+)\\— ([0-9]+)", "<@cross-ref-fig-open>Fig\\. $1\\—$2<@cross-ref-fig-close>"); // em-dash
		line = line.replaceAll("(?i)Fig\\. ([0-9]+)\\– ([0-9]+)", "<@cross-ref-fig-open>Fig\\. $1\\–$2<@cross-ref-fig-close>"); // en-dash
		line = line.replaceAll("(?i)Fig\\. ([0-9]+)\\. ([0-9]+)", "<@cross-ref-fig-open>Fig\\. $1\\.$2<@cross-ref-fig-close>"); // simple period
		
		line = line.replaceAll("(?i)Fig ([0-9]+)\\- ([0-9]+)", "<@cross-ref-fig-open>Fig $1\\-$2<@cross-ref-fig-close>"); // simple dash
		line = line.replaceAll("(?i)Fig ([0-9]+)\\— ([0-9]+)", "<@cross-ref-fig-open>Fig $1\\—$2<@cross-ref-fig-close>"); // em-dash
		line = line.replaceAll("(?i)Fig ([0-9]+)\\– ([0-9]+)", "<@cross-ref-fig-open>Fig $1\\–$2<@cross-ref-fig-close>"); // en-dash
		line = line.replaceAll("(?i)Fig ([0-9]+)\\. ([0-9]+)", "<@cross-ref-fig-open>Fig $1\\.$2<@cross-ref-fig-close>"); // simple period
		
		
		line = line.replaceAll("(?i)Figure\\. ([0-9]+)\\- ([0-9]+)", "<@cross-ref-fig-open>Figure\\. $1\\-$2<@cross-ref-fig-close>"); // simple dash
		line = line.replaceAll("(?i)Figure\\. ([0-9]+)\\— ([0-9]+)", "<@cross-ref-fig-open>Figure\\. $1\\—$2<@cross-ref-fig-close>"); // em-dash
		line = line.replaceAll("(?i)Figure\\. ([0-9]+)\\– ([0-9]+)", "<@cross-ref-fig-open>Figure\\. $1\\–$2<@cross-ref-fig-close>"); // en-dash
		line = line.replaceAll("(?i)Figure\\. ([0-9]+)\\. ([0-9]+)", "<@cross-ref-fig-open>Figure\\. $1\\.$2<@cross-ref-fig-close>"); // simple period
		
		line = line.replaceAll("(?i)Figure ([0-9]+)\\- ([0-9]+)", "<@cross-ref-fig-open>Figure $1\\-$2<@cross-ref-fig-close>"); // simple dash
		line = line.replaceAll("(?i)Figure ([0-9]+)\\— ([0-9]+)", "<@cross-ref-fig-open>Figure $1\\—$2<@cross-ref-fig-close>"); // em-dash
		line = line.replaceAll("(?i)Figure ([0-9]+)\\– ([0-9]+)", "<@cross-ref-fig-open>Figure $1\\–$2<@cross-ref-fig-close>"); // en-dash
		line = line.replaceAll("(?i)Figure ([0-9]+)\\. ([0-9]+)", "<@cross-ref-fig-open>Figure $1\\.$2<@cross-ref-fig-close>"); // simple period
		
		
		/**
		 * 
		 * ELECTRONIC
		 */
		
		/**
		 * ELECTRONIC multiple occurance handling
		 * 
		 */
		
		line = line.replaceAll("(?i)Fig\\. E([0-9]+)\\-([0-9]+)", "<@cross-ref-fig-open>Fig\\. E$1\\-$2<@cross-ref-fig-close>"); // simple dash
		line = line.replaceAll("(?i)Fig\\. E([0-9]+)\\—([0-9]+)", "<@cross-ref-fig-open>Fig\\. E$1\\—$2<@cross-ref-fig-close>"); // em-dash
		line = line.replaceAll("(?i)Fig\\. E([0-9]+)\\–([0-9]+)", "<@cross-ref-fig-open>Fig\\. E$1\\–$2<@cross-ref-fig-close>"); // en-dash
		line = line.replaceAll("(?i)Fig\\. E([0-9]+)\\.([0-9]+)", "<@cross-ref-fig-open>Fig\\. E$1\\.$2<@cross-ref-fig-close>"); // simple period
		
		line = line.replaceAll("(?i)Fig E([0-9]+)\\-([0-9]+)", "<@cross-ref-fig-open>Fig E$1\\-$2<@cross-ref-fig-close>"); // simple dash
		line = line.replaceAll("(?i)Fig E([0-9]+)\\—([0-9]+)", "<@cross-ref-fig-open>Fig E$1\\—$2<@cross-ref-fig-close>"); // em-dash
		line = line.replaceAll("(?i)Fig E([0-9]+)\\–([0-9]+)", "<@cross-ref-fig-open>Fig E$1\\–$2<@cross-ref-fig-close>"); // en-dash
		line = line.replaceAll("(?i)Fig E([0-9]+)\\.([0-9]+)", "<@cross-ref-fig-open>Fig E$1\\.$2<@cross-ref-fig-close>"); // simple period
		
		
		line = line.replaceAll("(?i)Figure\\. E([0-9]+)\\-([0-9]+)", "<@cross-ref-fig-open>Figure\\. E$1\\-$2<@cross-ref-fig-close>"); // simple dash
		line = line.replaceAll("(?i)Figure\\. E([0-9]+)\\—([0-9]+)", "<@cross-ref-fig-open>Figure\\. E$1\\—$2<@cross-ref-fig-close>"); // em-dash
		line = line.replaceAll("(?i)Figure\\. E([0-9]+)\\–([0-9]+)", "<@cross-ref-fig-open>Figure\\. E$1\\–$2<@cross-ref-fig-close>"); // en-dash
		line = line.replaceAll("(?i)Figure\\. E([0-9]+)\\.([0-9]+)", "<@cross-ref-fig-open>Figure\\. E$1\\.$2<@cross-ref-fig-close>"); // simple period
		
		line = line.replaceAll("(?i)Figure E([0-9]+)\\-([0-9]+)", "<@cross-ref-fig-open>Figure E$1\\-$2<@cross-ref-fig-close>"); // simple dash
		line = line.replaceAll("(?i)Figure E([0-9]+)\\—([0-9]+)", "<@cross-ref-fig-open>Figure E$1\\—$2<@cross-ref-fig-close>"); // em-dash
		line = line.replaceAll("(?i)Figure E([0-9]+)\\–([0-9]+)", "<@cross-ref-fig-open>Figure E$1\\–$2<@cross-ref-fig-close>"); // en-dash
		line = line.replaceAll("(?i)Figure E([0-9]+)\\.([0-9]+)", "<@cross-ref-fig-open>Figure E$1\\.$2<@cross-ref-fig-close>"); // simple period
		
		
		/**
		 * 
		 * ELECTRONIC single space handling
		 * 
		 */
		
		line = line.replaceAll("(?i)Fig\\. E([0-9]+)\\- ([0-9]+)", "<@cross-ref-fig-open>Fig\\. E$1\\-$2<@cross-ref-fig-close>"); // simple dash
		line = line.replaceAll("(?i)Fig\\. E([0-9]+)\\— ([0-9]+)", "<@cross-ref-fig-open>Fig\\. E$1\\—$2<@cross-ref-fig-close>"); // em-dash
		line = line.replaceAll("(?i)Fig\\. E([0-9]+)\\– ([0-9]+)", "<@cross-ref-fig-open>Fig\\. E$1\\–$2<@cross-ref-fig-close>"); // en-dash
		line = line.replaceAll("(?i)Fig\\. E([0-9]+)\\. ([0-9]+)", "<@cross-ref-fig-open>Fig\\. E$1\\.$2<@cross-ref-fig-close>"); // simple period
		
		line = line.replaceAll("(?i)Fig E([0-9]+)\\- ([0-9]+)", "<@cross-ref-fig-open>Fig E$1\\-$2<@cross-ref-fig-close>"); // simple dash
		line = line.replaceAll("(?i)Fig E([0-9]+)\\— ([0-9]+)", "<@cross-ref-fig-open>Fig E$1\\—$2<@cross-ref-fig-close>"); // em-dash
		line = line.replaceAll("(?i)Fig E([0-9]+)\\– ([0-9]+)", "<@cross-ref-fig-open>Fig E$1\\–$2<@cross-ref-fig-close>"); // en-dash
		line = line.replaceAll("(?i)Fig E([0-9]+)\\. ([0-9]+)", "<@cross-ref-fig-open>Fig E$1\\.$2<@cross-ref-fig-close>"); // simple period
		
		
		line = line.replaceAll("(?i)Figure\\. E([0-9]+)\\- ([0-9]+)", "<@cross-ref-fig-open>Figure\\. E$1\\-$2<@cross-ref-fig-close>"); // simple dash
		line = line.replaceAll("(?i)Figure\\. E([0-9]+)\\— ([0-9]+)", "<@cross-ref-fig-open>Figure\\. E$1\\—$2<@cross-ref-fig-close>"); // em-dash
		line = line.replaceAll("(?i)Figure\\. E([0-9]+)\\– ([0-9]+)", "<@cross-ref-fig-open>Figure\\. E$1\\–$2<@cross-ref-fig-close>"); // en-dash
		line = line.replaceAll("(?i)Figure\\. E([0-9]+)\\. ([0-9]+)", "<@cross-ref-fig-open>Figure\\. E$1\\.$2<@cross-ref-fig-close>"); // simple period
		
		line = line.replaceAll("(?i)Figure E([0-9]+)\\- ([0-9]+)", "<@cross-ref-fig-open>Figure E$1\\-$2<@cross-ref-fig-close>"); // simple dash
		line = line.replaceAll("(?i)Figure E([0-9]+)\\— ([0-9]+)", "<@cross-ref-fig-open>Figure E$1\\—$2<@cross-ref-fig-close>"); // em-dash
		line = line.replaceAll("(?i)Figure E([0-9]+)\\– ([0-9]+)", "<@cross-ref-fig-open>Figure E$1\\–$2<@cross-ref-fig-close>"); // en-dash
		line = line.replaceAll("(?i)Figure E([0-9]+)\\. ([0-9]+)", "<@cross-ref-fig-open>Figure E$1\\.$2<@cross-ref-fig-close>"); // simple period
		

		
		
		if (line.contains("<@cross-ref-fig-open>"))	isFound = true;
		if (!isFound) {
			
			/**
			 * single occurance handling
			 * 
			 */
			
			line = line.replaceAll("(?i)Fig\\. ([0-9]+)", "<@cross-ref-fig-open>Fig\\. $1<@cross-ref-fig-close>");
			line = line.replaceAll("(?i)Fig ([0-9]+)", "<@cross-ref-fig-open>Fig $1<@cross-ref-fig-close>");
			line = line.replaceAll("(?i)Figure\\. ([0-9]+)", "<@cross-ref-fig-open>Figure\\. $1<@cross-ref-fig-close>");
			line = line.replaceAll("(?i)Figure ([0-9]+)", "<@cross-ref-fig-open>Figure $1<@cross-ref-fig-close>");
			
			
			/**
			 * 
			 * ELECTRONIC
			 */
			
			line = line.replaceAll("(?i)Fig\\. E([0-9]+)", "<@cross-ref-fig-open>Fig\\. E$1<@cross-ref-fig-close>");
			line = line.replaceAll("(?i)Fig E([0-9]+)", "<@cross-ref-fig-open>Fig E$1<@cross-ref-fig-close>");
			line = line.replaceAll("(?i)Figure\\. E([0-9]+)", "<@cross-ref-fig-open>Figure\\. E$1<@cross-ref-fig-close>");
			line = line.replaceAll("(?i)Figure E([0-9]+)", "<@cross-ref-fig-open>Figure E$1<@cross-ref-fig-close>");

		}
		return line;
	}
	
	
	
	private String handleFigRangesDelimeters(String line) {
		
		for (int index = 0; index < Constants.delimeters.length; index ++) {
			
			boolean isFound = false;
			
			line = line.replaceAll("(?i)Fig\\. ([0-9]+)\\-([0-9]+) "+(Constants.delimeters[index])+" ([0-9]+)\\-([0-9]+)", "<@cross-ref-fig-open>Fig\\. $1\\-$2<@cross-ref-fig-close> "+(Constants.delimeters[index])+" <@cross-ref-fig-open>$3\\-$4<@cross-ref-fig-close>"); // simple dash
			line = line.replaceAll("(?i)Fig\\. ([0-9]+)\\—([0-9]+) "+(Constants.delimeters[index])+" ([0-9]+)\\—([0-9]+)", "<@cross-ref-fig-open>Fig\\. $1\\-$2<@cross-ref-fig-close> "+(Constants.delimeters[index])+" <@cross-ref-fig-open>$3\\-$4<@cross-ref-fig-close>"); // em-dash
			line = line.replaceAll("(?i)Fig\\. ([0-9]+)\\–([0-9]+) "+(Constants.delimeters[index])+" ([0-9]+)\\–([0-9]+)", "<@cross-ref-fig-open>Fig\\. $1\\-$2<@cross-ref-fig-close> "+(Constants.delimeters[index])+" <@cross-ref-fig-open>$3\\-$4<@cross-ref-fig-close>"); // en-dash
			line = line.replaceAll("(?i)Fig\\. ([0-9]+)\\.([0-9]+) "+(Constants.delimeters[index])+" ([0-9]+)\\.([0-9]+)", "<@cross-ref-fig-open>Fig\\. $1\\.$2<@cross-ref-fig-close> "+(Constants.delimeters[index])+" <@cross-ref-fig-open>$3\\.$4<@cross-ref-fig-close>"); // en-dash
			
			line = line.replaceAll("(?i)Fig ([0-9]+)\\-([0-9]+) "+(Constants.delimeters[index])+" ([0-9]+)\\-([0-9]+)", "<@cross-ref-fig-open>Fig $1\\-$2<@cross-ref-fig-close> "+(Constants.delimeters[index])+" <@cross-ref-fig-open>$3\\-$4<@cross-ref-fig-close>"); // simple dash
			line = line.replaceAll("(?i)Fig ([0-9]+)\\—([0-9]+) "+(Constants.delimeters[index])+" ([0-9]+)\\—([0-9]+)", "<@cross-ref-fig-open>Fig $1\\-$2<@cross-ref-fig-close> "+(Constants.delimeters[index])+" <@cross-ref-fig-open>$3\\-$4<@cross-ref-fig-close>"); // em-dash
			line = line.replaceAll("(?i)Fig ([0-9]+)\\–([0-9]+) "+(Constants.delimeters[index])+" ([0-9]+)\\–([0-9]+)", "<@cross-ref-fig-open>Fig $1\\-$2<@cross-ref-fig-close> "+(Constants.delimeters[index])+" <@cross-ref-fig-open>$3\\-$4<@cross-ref-fig-close>"); // en-dash
			line = line.replaceAll("(?i)Fig ([0-9]+)\\.([0-9]+) "+(Constants.delimeters[index])+" ([0-9]+)\\.([0-9]+)", "<@cross-ref-fig-open>Fig $1\\.$2<@cross-ref-fig-close> "+(Constants.delimeters[index])+" <@cross-ref-fig-open>$3\\.$4<@cross-ref-fig-close>"); // en-dash
			
			line = line.replaceAll("(?i)Figure\\. ([0-9]+)\\-([0-9]+) "+(Constants.delimeters[index])+" ([0-9]+)\\-([0-9]+)", "<@cross-ref-fig-open>Figure\\. $1\\-$2<@cross-ref-fig-close> "+(Constants.delimeters[index])+" <@cross-ref-fig-open>$3\\-$4<@cross-ref-fig-close>"); // simple dash
			line = line.replaceAll("(?i)Figure\\. ([0-9]+)\\—([0-9]+) "+(Constants.delimeters[index])+" ([0-9]+)\\—([0-9]+)", "<@cross-ref-fig-open>Figure\\. $1\\-$2<@cross-ref-fig-close> "+(Constants.delimeters[index])+" <@cross-ref-fig-open>$3\\-$4<@cross-ref-fig-close>"); // em-dash
			line = line.replaceAll("(?i)Figure\\. ([0-9]+)\\–([0-9]+) "+(Constants.delimeters[index])+" ([0-9]+)\\–([0-9]+)", "<@cross-ref-fig-open>Figure\\. $1\\-$2<@cross-ref-fig-close> "+(Constants.delimeters[index])+" <@cross-ref-fig-open>$3\\-$4<@cross-ref-fig-close>"); // en-dash
			line = line.replaceAll("(?i)Figure\\. ([0-9]+)\\.([0-9]+) "+(Constants.delimeters[index])+" ([0-9]+)\\.([0-9]+)", "<@cross-ref-fig-open>Figure\\. $1\\.$2<@cross-ref-fig-close> "+(Constants.delimeters[index])+" <@cross-ref-fig-open>$3\\.$4<@cross-ref-fig-close>"); // en-dash
			
			line = line.replaceAll("(?i)Figure ([0-9]+)\\-([0-9]+) "+(Constants.delimeters[index])+" ([0-9]+)\\-([0-9]+)", "<@cross-ref-fig-open>Figure $1\\-$2<@cross-ref-fig-close> "+(Constants.delimeters[index])+" <@cross-ref-fig-open>$3\\-$4<@cross-ref-fig-close>"); // simple dash
			line = line.replaceAll("(?i)Figure ([0-9]+)\\—([0-9]+) "+(Constants.delimeters[index])+" ([0-9]+)\\—([0-9]+)", "<@cross-ref-fig-open>Figure $1\\-$2<@cross-ref-fig-close> "+(Constants.delimeters[index])+" <@cross-ref-fig-open>$3\\-$4<@cross-ref-fig-close>"); // em-dash
			line = line.replaceAll("(?i)Figure ([0-9]+)\\–([0-9]+) "+(Constants.delimeters[index])+" ([0-9]+)\\–([0-9]+)", "<@cross-ref-fig-open>Figure $1\\-$2<@cross-ref-fig-close> "+(Constants.delimeters[index])+" <@cross-ref-fig-open>$3\\-$4<@cross-ref-fig-close>"); // en-dash
			line = line.replaceAll("(?i)Figure ([0-9]+)\\.([0-9]+) "+(Constants.delimeters[index])+" ([0-9]+)\\.([0-9]+)", "<@cross-ref-fig-open>Figure $1\\.$2<@cross-ref-fig-close> "+(Constants.delimeters[index])+" <@cross-ref-fig-open>$3\\.$4<@cross-ref-fig-close>"); // en-dash
			
			
			
			line = line.replaceAll("(?i)Figs\\. ([0-9]+)\\-([0-9]+) "+(Constants.delimeters[index])+" ([0-9]+)\\-([0-9]+)", "<@cross-ref-fig-open>Figs\\. $1\\-$2<@cross-ref-fig-close> "+(Constants.delimeters[index])+" <@cross-ref-fig-open>$3\\-$4<@cross-ref-fig-close>"); // simple dash
			line = line.replaceAll("(?i)Figs\\. ([0-9]+)\\—([0-9]+) "+(Constants.delimeters[index])+" ([0-9]+)\\—([0-9]+)", "<@cross-ref-fig-open>Figs\\. $1\\-$2<@cross-ref-fig-close> "+(Constants.delimeters[index])+" <@cross-ref-fig-open>$3\\-$4<@cross-ref-fig-close>"); // em-dash
			line = line.replaceAll("(?i)Figs\\. ([0-9]+)\\–([0-9]+) "+(Constants.delimeters[index])+" ([0-9]+)\\–([0-9]+)", "<@cross-ref-fig-open>Figs\\. $1\\-$2<@cross-ref-fig-close> "+(Constants.delimeters[index])+" <@cross-ref-fig-open>$3\\-$4<@cross-ref-fig-close>"); // en-dash
			line = line.replaceAll("(?i)Figs\\. ([0-9]+)\\.([0-9]+) "+(Constants.delimeters[index])+" ([0-9]+)\\.([0-9]+)", "<@cross-ref-fig-open>Figs\\. $1\\.$2<@cross-ref-fig-close> "+(Constants.delimeters[index])+" <@cross-ref-fig-open>$3\\.$4<@cross-ref-fig-close>"); // en-dash
			
			line = line.replaceAll("(?i)Figs ([0-9]+)\\-([0-9]+) "+(Constants.delimeters[index])+" ([0-9]+)\\-([0-9]+)", "<@cross-ref-fig-open>Figs $1\\-$2<@cross-ref-fig-close> "+(Constants.delimeters[index])+" <@cross-ref-fig-open>$3\\-$4<@cross-ref-fig-close>"); // simple dash
			line = line.replaceAll("(?i)Figs ([0-9]+)\\—([0-9]+) "+(Constants.delimeters[index])+" ([0-9]+)\\—([0-9]+)", "<@cross-ref-fig-open>Figs $1\\-$2<@cross-ref-fig-close> "+(Constants.delimeters[index])+" <@cross-ref-fig-open>$3\\-$4<@cross-ref-fig-close>"); // em-dash
			line = line.replaceAll("(?i)Figs ([0-9]+)\\–([0-9]+) "+(Constants.delimeters[index])+" ([0-9]+)\\–([0-9]+)", "<@cross-ref-fig-open>Figs $1\\-$2<@cross-ref-fig-close> "+(Constants.delimeters[index])+" <@cross-ref-fig-open>$3\\-$4<@cross-ref-fig-close>"); // en-dash
			line = line.replaceAll("(?i)Figs ([0-9]+)\\.([0-9]+) "+(Constants.delimeters[index])+" ([0-9]+)\\.([0-9]+)", "<@cross-ref-fig-open>Figs $1\\.$2<@cross-ref-fig-close> "+(Constants.delimeters[index])+" <@cross-ref-fig-open>$3\\.$4<@cross-ref-fig-close>"); // en-dash
			
			line = line.replaceAll("(?i)Figures\\. ([0-9]+)\\-([0-9]+) "+(Constants.delimeters[index])+" ([0-9]+)\\-([0-9]+)", "<@cross-ref-fig-open>Figures\\. $1\\-$2<@cross-ref-fig-close> "+(Constants.delimeters[index])+" <@cross-ref-fig-open>$3\\-$4<@cross-ref-fig-close>"); // simple dash
			line = line.replaceAll("(?i)Figures\\. ([0-9]+)\\—([0-9]+) "+(Constants.delimeters[index])+" ([0-9]+)\\—([0-9]+)", "<@cross-ref-fig-open>Figures\\. $1\\-$2<@cross-ref-fig-close> "+(Constants.delimeters[index])+" <@cross-ref-fig-open>$3\\-$4<@cross-ref-fig-close>"); // em-dash
			line = line.replaceAll("(?i)Figures\\. ([0-9]+)\\–([0-9]+) "+(Constants.delimeters[index])+" ([0-9]+)\\–([0-9]+)", "<@cross-ref-fig-open>Figures\\. $1\\-$2<@cross-ref-fig-close> "+(Constants.delimeters[index])+" <@cross-ref-fig-open>$3\\-$4<@cross-ref-fig-close>"); // en-dash
			line = line.replaceAll("(?i)Figures\\. ([0-9]+)\\.([0-9]+) "+(Constants.delimeters[index])+" ([0-9]+)\\.([0-9]+)", "<@cross-ref-fig-open>Figures\\. $1\\.$2<@cross-ref-fig-close> "+(Constants.delimeters[index])+" <@cross-ref-fig-open>$3\\.$4<@cross-ref-fig-close>"); // en-dash
			
			line = line.replaceAll("(?i)Figures ([0-9]+)\\-([0-9]+) "+(Constants.delimeters[index])+" ([0-9]+)\\-([0-9]+)", "<@cross-ref-fig-open>Figures $1\\-$2<@cross-ref-fig-close> "+(Constants.delimeters[index])+" <@cross-ref-fig-open>$3\\-$4<@cross-ref-fig-close>"); // simple dash
			line = line.replaceAll("(?i)Figures ([0-9]+)\\—([0-9]+) "+(Constants.delimeters[index])+" ([0-9]+)\\—([0-9]+)", "<@cross-ref-fig-open>Figures $1\\-$2<@cross-ref-fig-close> "+(Constants.delimeters[index])+" <@cross-ref-fig-open>$3\\-$4<@cross-ref-fig-close>"); // em-dash
			line = line.replaceAll("(?i)Figures ([0-9]+)\\–([0-9]+) "+(Constants.delimeters[index])+" ([0-9]+)\\–([0-9]+)", "<@cross-ref-fig-open>Figures $1\\-$2<@cross-ref-fig-close> "+(Constants.delimeters[index])+" <@cross-ref-fig-open>$3\\-$4<@cross-ref-fig-close>"); // en-dash
			line = line.replaceAll("(?i)Figures ([0-9]+)\\.([0-9]+) "+(Constants.delimeters[index])+" ([0-9]+)\\.([0-9]+)", "<@cross-ref-fig-open>Figures $1\\.$2<@cross-ref-fig-close> "+(Constants.delimeters[index])+" <@cross-ref-fig-open>$3\\.$4<@cross-ref-fig-close>"); // en-dash
			
			
			/**
			 * 
			 * single space both side
			 */
			line = line.replaceAll("(?i)Figs\\. ([0-9]+)\\- ([0-9]+) "+(Constants.delimeters[index])+" ([0-9]+)\\- ([0-9]+)", "<@cross-ref-fig-open>Figs\\. $1\\-$2<@cross-ref-fig-close> "+(Constants.delimeters[index])+" <@cross-ref-fig-open>$3\\-$4<@cross-ref-fig-close>"); // simple dash
			line = line.replaceAll("(?i)Figs\\. ([0-9]+)\\— ([0-9]+) "+(Constants.delimeters[index])+" ([0-9]+)\\— ([0-9]+)", "<@cross-ref-fig-open>Figs\\. $1\\-$2<@cross-ref-fig-close> "+(Constants.delimeters[index])+" <@cross-ref-fig-open>$3\\-$4<@cross-ref-fig-close>"); // em-dash
			line = line.replaceAll("(?i)Figs\\. ([0-9]+)\\– ([0-9]+) "+(Constants.delimeters[index])+" ([0-9]+)\\– ([0-9]+)", "<@cross-ref-fig-open>Figs\\. $1\\-$2<@cross-ref-fig-close> "+(Constants.delimeters[index])+" <@cross-ref-fig-open>$3\\-$4<@cross-ref-fig-close>"); // en-dash
			line = line.replaceAll("(?i)Figs\\. ([0-9]+)\\. ([0-9]+) "+(Constants.delimeters[index])+" ([0-9]+)\\. ([0-9]+)", "<@cross-ref-fig-open>Figs\\. $1\\.$2<@cross-ref-fig-close> "+(Constants.delimeters[index])+" <@cross-ref-fig-open>$3\\.$4<@cross-ref-fig-close>"); // en-dash
			
			line = line.replaceAll("(?i)Figs ([0-9]+)\\- ([0-9]+) "+(Constants.delimeters[index])+" ([0-9]+)\\- ([0-9]+)", "<@cross-ref-fig-open>Figs $1\\-$2<@cross-ref-fig-close> "+(Constants.delimeters[index])+" <@cross-ref-fig-open>$3\\-$4<@cross-ref-fig-close>"); // simple dash
			line = line.replaceAll("(?i)Figs ([0-9]+)\\— ([0-9]+) "+(Constants.delimeters[index])+" ([0-9]+)\\— ([0-9]+)", "<@cross-ref-fig-open>Figs $1\\-$2<@cross-ref-fig-close> "+(Constants.delimeters[index])+" <@cross-ref-fig-open>$3\\-$4<@cross-ref-fig-close>"); // em-dash
			line = line.replaceAll("(?i)Figs ([0-9]+)\\– ([0-9]+) "+(Constants.delimeters[index])+" ([0-9]+)\\– ([0-9]+)", "<@cross-ref-fig-open>Figs $1\\-$2<@cross-ref-fig-close> "+(Constants.delimeters[index])+" <@cross-ref-fig-open>$3\\-$4<@cross-ref-fig-close>"); // en-dash
			line = line.replaceAll("(?i)Figs ([0-9]+)\\. ([0-9]+) "+(Constants.delimeters[index])+" ([0-9]+)\\. ([0-9]+)", "<@cross-ref-fig-open>Figs $1\\.$2<@cross-ref-fig-close> "+(Constants.delimeters[index])+" <@cross-ref-fig-open>$3\\.$4<@cross-ref-fig-close>"); // en-dash
			
			line = line.replaceAll("(?i)Figures\\. ([0-9]+)\\- ([0-9]+) "+(Constants.delimeters[index])+" ([0-9]+)\\- ([0-9]+)", "<@cross-ref-fig-open>Figures\\. $1\\-$2<@cross-ref-fig-close> "+(Constants.delimeters[index])+" <@cross-ref-fig-open>$3\\-$4<@cross-ref-fig-close>"); // simple dash
			line = line.replaceAll("(?i)Figures\\. ([0-9]+)\\— ([0-9]+) "+(Constants.delimeters[index])+" ([0-9]+)\\— ([0-9]+)", "<@cross-ref-fig-open>Figures\\. $1\\-$2<@cross-ref-fig-close> "+(Constants.delimeters[index])+" <@cross-ref-fig-open>$3\\-$4<@cross-ref-fig-close>"); // em-dash
			line = line.replaceAll("(?i)Figures\\. ([0-9]+)\\– ([0-9]+) "+(Constants.delimeters[index])+" ([0-9]+)\\– ([0-9]+)", "<@cross-ref-fig-open>Figures\\. $1\\-$2<@cross-ref-fig-close> "+(Constants.delimeters[index])+" <@cross-ref-fig-open>$3\\-$4<@cross-ref-fig-close>"); // en-dash
			line = line.replaceAll("(?i)Figures\\. ([0-9]+)\\. ([0-9]+) "+(Constants.delimeters[index])+" ([0-9]+)\\. ([0-9]+)", "<@cross-ref-fig-open>Figures\\. $1\\.$2<@cross-ref-fig-close> "+(Constants.delimeters[index])+" <@cross-ref-fig-open>$3\\.$4<@cross-ref-fig-close>"); // en-dash
			
			line = line.replaceAll("(?i)Figures ([0-9]+)\\- ([0-9]+) "+(Constants.delimeters[index])+" ([0-9]+)\\- ([0-9]+)", "<@cross-ref-fig-open>Figures $1\\-$2<@cross-ref-fig-close> "+(Constants.delimeters[index])+" <@cross-ref-fig-open>$3\\-$4<@cross-ref-fig-close>"); // simple dash
			line = line.replaceAll("(?i)Figures ([0-9]+)\\— ([0-9]+) "+(Constants.delimeters[index])+" ([0-9]+)\\— ([0-9]+)", "<@cross-ref-fig-open>Figures $1\\-$2<@cross-ref-fig-close> "+(Constants.delimeters[index])+" <@cross-ref-fig-open>$3\\-$4<@cross-ref-fig-close>"); // em-dash
			line = line.replaceAll("(?i)Figures ([0-9]+)\\– ([0-9]+) "+(Constants.delimeters[index])+" ([0-9]+)\\– ([0-9]+)", "<@cross-ref-fig-open>Figures $1\\-$2<@cross-ref-fig-close> "+(Constants.delimeters[index])+" <@cross-ref-fig-open>$3\\-$4<@cross-ref-fig-close>"); // en-dash
			line = line.replaceAll("(?i)Figures ([0-9]+)\\. ([0-9]+) "+(Constants.delimeters[index])+" ([0-9]+)\\. ([0-9]+)", "<@cross-ref-fig-open>Figures $1\\.$2<@cross-ref-fig-close> "+(Constants.delimeters[index])+" <@cross-ref-fig-open>$3\\.$4<@cross-ref-fig-close>"); // en-dash
			
			/**
			 * 
			 * single space left side
			 */
			line = line.replaceAll("(?i)Figs\\. ([0-9]+)\\- ([0-9]+) "+(Constants.delimeters[index])+" ([0-9]+)\\-([0-9]+)", "<@cross-ref-fig-open>Figs\\. $1\\-$2<@cross-ref-fig-close> "+(Constants.delimeters[index])+" <@cross-ref-fig-open>$3\\-$4<@cross-ref-fig-close>"); // simple dash
			line = line.replaceAll("(?i)Figs\\. ([0-9]+)\\— ([0-9]+) "+(Constants.delimeters[index])+" ([0-9]+)\\—([0-9]+)", "<@cross-ref-fig-open>Figs\\. $1\\-$2<@cross-ref-fig-close> "+(Constants.delimeters[index])+" <@cross-ref-fig-open>$3\\-$4<@cross-ref-fig-close>"); // em-dash
			line = line.replaceAll("(?i)Figs\\. ([0-9]+)\\– ([0-9]+) "+(Constants.delimeters[index])+" ([0-9]+)\\–([0-9]+)", "<@cross-ref-fig-open>Figs\\. $1\\-$2<@cross-ref-fig-close> "+(Constants.delimeters[index])+" <@cross-ref-fig-open>$3\\-$4<@cross-ref-fig-close>"); // en-dash
			line = line.replaceAll("(?i)Figs\\. ([0-9]+)\\. ([0-9]+) "+(Constants.delimeters[index])+" ([0-9]+)\\.([0-9]+)", "<@cross-ref-fig-open>Figs\\. $1\\.$2<@cross-ref-fig-close> "+(Constants.delimeters[index])+" <@cross-ref-fig-open>$3\\.$4<@cross-ref-fig-close>"); // en-dash
			
			line = line.replaceAll("(?i)Figs ([0-9]+)\\- ([0-9]+) "+(Constants.delimeters[index])+" ([0-9]+)\\-([0-9]+)", "<@cross-ref-fig-open>Figs $1\\-$2<@cross-ref-fig-close> "+(Constants.delimeters[index])+" <@cross-ref-fig-open>$3\\-$4<@cross-ref-fig-close>"); // simple dash
			line = line.replaceAll("(?i)Figs ([0-9]+)\\— ([0-9]+) "+(Constants.delimeters[index])+" ([0-9]+)\\—([0-9]+)", "<@cross-ref-fig-open>Figs $1\\-$2<@cross-ref-fig-close> "+(Constants.delimeters[index])+" <@cross-ref-fig-open>$3\\-$4<@cross-ref-fig-close>"); // em-dash
			line = line.replaceAll("(?i)Figs ([0-9]+)\\– ([0-9]+) "+(Constants.delimeters[index])+" ([0-9]+)\\–([0-9]+)", "<@cross-ref-fig-open>Figs $1\\-$2<@cross-ref-fig-close> "+(Constants.delimeters[index])+" <@cross-ref-fig-open>$3\\-$4<@cross-ref-fig-close>"); // en-dash
			line = line.replaceAll("(?i)Figs ([0-9]+)\\. ([0-9]+) "+(Constants.delimeters[index])+" ([0-9]+)\\.([0-9]+)", "<@cross-ref-fig-open>Figs $1\\.$2<@cross-ref-fig-close> "+(Constants.delimeters[index])+" <@cross-ref-fig-open>$3\\.$4<@cross-ref-fig-close>"); // en-dash
			
			line = line.replaceAll("(?i)Figures\\. ([0-9]+)\\- ([0-9]+) "+(Constants.delimeters[index])+" ([0-9]+)\\-([0-9]+)", "<@cross-ref-fig-open>Figures\\. $1\\-$2<@cross-ref-fig-close> "+(Constants.delimeters[index])+" <@cross-ref-fig-open>$3\\-$4<@cross-ref-fig-close>"); // simple dash
			line = line.replaceAll("(?i)Figures\\. ([0-9]+)\\— ([0-9]+) "+(Constants.delimeters[index])+" ([0-9]+)\\—([0-9]+)", "<@cross-ref-fig-open>Figures\\. $1\\-$2<@cross-ref-fig-close> "+(Constants.delimeters[index])+" <@cross-ref-fig-open>$3\\-$4<@cross-ref-fig-close>"); // em-dash
			line = line.replaceAll("(?i)Figures\\. ([0-9]+)\\– ([0-9]+) "+(Constants.delimeters[index])+" ([0-9]+)\\–([0-9]+)", "<@cross-ref-fig-open>Figures\\. $1\\-$2<@cross-ref-fig-close> "+(Constants.delimeters[index])+" <@cross-ref-fig-open>$3\\-$4<@cross-ref-fig-close>"); // en-dash
			line = line.replaceAll("(?i)Figures\\. ([0-9]+)\\. ([0-9]+) "+(Constants.delimeters[index])+" ([0-9]+)\\.([0-9]+)", "<@cross-ref-fig-open>Figures\\. $1\\.$2<@cross-ref-fig-close> "+(Constants.delimeters[index])+" <@cross-ref-fig-open>$3\\.$4<@cross-ref-fig-close>"); // en-dash
			
			line = line.replaceAll("(?i)Figures ([0-9]+)\\- ([0-9]+) "+(Constants.delimeters[index])+" ([0-9]+)\\-([0-9]+)", "<@cross-ref-fig-open>Figures $1\\-$2<@cross-ref-fig-close> "+(Constants.delimeters[index])+" <@cross-ref-fig-open>$3\\-$4<@cross-ref-fig-close>"); // simple dash
			line = line.replaceAll("(?i)Figures ([0-9]+)\\— ([0-9]+) "+(Constants.delimeters[index])+" ([0-9]+)\\—([0-9]+)", "<@cross-ref-fig-open>Figures $1\\-$2<@cross-ref-fig-close> "+(Constants.delimeters[index])+" <@cross-ref-fig-open>$3\\-$4<@cross-ref-fig-close>"); // em-dash
			line = line.replaceAll("(?i)Figures ([0-9]+)\\– ([0-9]+) "+(Constants.delimeters[index])+" ([0-9]+)\\–([0-9]+)", "<@cross-ref-fig-open>Figures $1\\-$2<@cross-ref-fig-close> "+(Constants.delimeters[index])+" <@cross-ref-fig-open>$3\\-$4<@cross-ref-fig-close>"); // en-dash
			line = line.replaceAll("(?i)Figures ([0-9]+)\\. ([0-9]+) "+(Constants.delimeters[index])+" ([0-9]+)\\.([0-9]+)", "<@cross-ref-fig-open>Figures $1\\.$2<@cross-ref-fig-close> "+(Constants.delimeters[index])+" <@cross-ref-fig-open>$3\\.$4<@cross-ref-fig-close>"); // en-dash
			
			/**
			 * 
			 * single space right side
			 */
			line = line.replaceAll("(?i)Figs\\. ([0-9]+)\\-([0-9]+) "+(Constants.delimeters[index])+" ([0-9]+)\\- ([0-9]+)", "<@cross-ref-fig-open>Figs\\. $1\\-$2<@cross-ref-fig-close> "+(Constants.delimeters[index])+" <@cross-ref-fig-open>$3\\-$4<@cross-ref-fig-close>"); // simple dash
			line = line.replaceAll("(?i)Figs\\. ([0-9]+)\\—([0-9]+) "+(Constants.delimeters[index])+" ([0-9]+)\\— ([0-9]+)", "<@cross-ref-fig-open>Figs\\. $1\\-$2<@cross-ref-fig-close> "+(Constants.delimeters[index])+" <@cross-ref-fig-open>$3\\-$4<@cross-ref-fig-close>"); // em-dash
			line = line.replaceAll("(?i)Figs\\. ([0-9]+)\\–([0-9]+) "+(Constants.delimeters[index])+" ([0-9]+)\\– ([0-9]+)", "<@cross-ref-fig-open>Figs\\. $1\\-$2<@cross-ref-fig-close> "+(Constants.delimeters[index])+" <@cross-ref-fig-open>$3\\-$4<@cross-ref-fig-close>"); // en-dash
			line = line.replaceAll("(?i)Figs\\. ([0-9]+)\\.([0-9]+) "+(Constants.delimeters[index])+" ([0-9]+)\\. ([0-9]+)", "<@cross-ref-fig-open>Figs\\. $1\\.$2<@cross-ref-fig-close> "+(Constants.delimeters[index])+" <@cross-ref-fig-open>$3\\.$4<@cross-ref-fig-close>"); // en-dash
			
			line = line.replaceAll("(?i)Figs ([0-9]+)\\-([0-9]+) "+(Constants.delimeters[index])+" ([0-9]+)\\- ([0-9]+)", "<@cross-ref-fig-open>Figs $1\\-$2<@cross-ref-fig-close> "+(Constants.delimeters[index])+" <@cross-ref-fig-open>$3\\-$4<@cross-ref-fig-close>"); // simple dash
			line = line.replaceAll("(?i)Figs ([0-9]+)\\—([0-9]+) "+(Constants.delimeters[index])+" ([0-9]+)\\— ([0-9]+)", "<@cross-ref-fig-open>Figs $1\\-$2<@cross-ref-fig-close> "+(Constants.delimeters[index])+" <@cross-ref-fig-open>$3\\-$4<@cross-ref-fig-close>"); // em-dash
			line = line.replaceAll("(?i)Figs ([0-9]+)\\–([0-9]+) "+(Constants.delimeters[index])+" ([0-9]+)\\– ([0-9]+)", "<@cross-ref-fig-open>Figs $1\\-$2<@cross-ref-fig-close> "+(Constants.delimeters[index])+" <@cross-ref-fig-open>$3\\-$4<@cross-ref-fig-close>"); // en-dash
			line = line.replaceAll("(?i)Figs ([0-9]+)\\.([0-9]+) "+(Constants.delimeters[index])+" ([0-9]+)\\. ([0-9]+)", "<@cross-ref-fig-open>Figs $1\\.$2<@cross-ref-fig-close> "+(Constants.delimeters[index])+" <@cross-ref-fig-open>$3\\.$4<@cross-ref-fig-close>"); // en-dash
			
			line = line.replaceAll("(?i)Figures\\. ([0-9]+)\\-([0-9]+) "+(Constants.delimeters[index])+" ([0-9]+)\\- ([0-9]+)", "<@cross-ref-fig-open>Figures\\. $1\\-$2<@cross-ref-fig-close> "+(Constants.delimeters[index])+" <@cross-ref-fig-open>$3\\-$4<@cross-ref-fig-close>"); // simple dash
			line = line.replaceAll("(?i)Figures\\. ([0-9]+)\\—([0-9]+) "+(Constants.delimeters[index])+" ([0-9]+)\\— ([0-9]+)", "<@cross-ref-fig-open>Figures\\. $1\\-$2<@cross-ref-fig-close> "+(Constants.delimeters[index])+" <@cross-ref-fig-open>$3\\-$4<@cross-ref-fig-close>"); // em-dash
			line = line.replaceAll("(?i)Figures\\. ([0-9]+)\\–([0-9]+) "+(Constants.delimeters[index])+" ([0-9]+)\\– ([0-9]+)", "<@cross-ref-fig-open>Figures\\. $1\\-$2<@cross-ref-fig-close> "+(Constants.delimeters[index])+" <@cross-ref-fig-open>$3\\-$4<@cross-ref-fig-close>"); // en-dash
			line = line.replaceAll("(?i)Figures\\. ([0-9]+)\\.([0-9]+) "+(Constants.delimeters[index])+" ([0-9]+)\\. ([0-9]+)", "<@cross-ref-fig-open>Figures\\. $1\\.$2<@cross-ref-fig-close> "+(Constants.delimeters[index])+" <@cross-ref-fig-open>$3\\.$4<@cross-ref-fig-close>"); // en-dash
			
			line = line.replaceAll("(?i)Figures ([0-9]+)\\-([0-9]+) "+(Constants.delimeters[index])+" ([0-9]+)\\- ([0-9]+)", "<@cross-ref-fig-open>Figures $1\\-$2<@cross-ref-fig-close> "+(Constants.delimeters[index])+" <@cross-ref-fig-open>$3\\-$4<@cross-ref-fig-close>"); // simple dash
			line = line.replaceAll("(?i)Figures ([0-9]+)\\—([0-9]+) "+(Constants.delimeters[index])+" ([0-9]+)\\— ([0-9]+)", "<@cross-ref-fig-open>Figures $1\\-$2<@cross-ref-fig-close> "+(Constants.delimeters[index])+" <@cross-ref-fig-open>$3\\-$4<@cross-ref-fig-close>"); // em-dash
			line = line.replaceAll("(?i)Figures ([0-9]+)\\–([0-9]+) "+(Constants.delimeters[index])+" ([0-9]+)\\– ([0-9]+)", "<@cross-ref-fig-open>Figures $1\\-$2<@cross-ref-fig-close> "+(Constants.delimeters[index])+" <@cross-ref-fig-open>$3\\-$4<@cross-ref-fig-close>"); // en-dash
			line = line.replaceAll("(?i)Figures ([0-9]+)\\.([0-9]+) "+(Constants.delimeters[index])+" ([0-9]+)\\. ([0-9]+)", "<@cross-ref-fig-open>Figures $1\\.$2<@cross-ref-fig-close> "+(Constants.delimeters[index])+" <@cross-ref-fig-open>$3\\.$4<@cross-ref-fig-close>"); // en-dash
			
			
			/**
			 * ELECTRONIC
			 */
			
			line = line.replaceAll("(?i)Fig\\. E([0-9]+)\\-([0-9]+) "+(Constants.delimeters[index])+" E([0-9]+)\\-([0-9]+)", "<@cross-ref-fig-open>Fig\\. $1\\-$2<@cross-ref-fig-close> "+(Constants.delimeters[index])+" <@cross-ref-fig-open>$3\\-$4<@cross-ref-fig-close>"); // simple dash
			line = line.replaceAll("(?i)Fig\\. E([0-9]+)\\—([0-9]+) "+(Constants.delimeters[index])+" E([0-9]+)\\—([0-9]+)", "<@cross-ref-fig-open>Fig\\. $1\\-$2<@cross-ref-fig-close> "+(Constants.delimeters[index])+" <@cross-ref-fig-open>$3\\-$4<@cross-ref-fig-close>"); // em-dash
			line = line.replaceAll("(?i)Fig\\. E([0-9]+)\\–([0-9]+) "+(Constants.delimeters[index])+" E([0-9]+)\\–([0-9]+)", "<@cross-ref-fig-open>Fig\\. $1\\-$2<@cross-ref-fig-close> "+(Constants.delimeters[index])+" <@cross-ref-fig-open>$3\\-$4<@cross-ref-fig-close>"); // en-dash
			line = line.replaceAll("(?i)Fig\\. E([0-9]+)\\.([0-9]+) "+(Constants.delimeters[index])+" E([0-9]+)\\.([0-9]+)", "<@cross-ref-fig-open>Fig\\. $1\\.$2<@cross-ref-fig-close> "+(Constants.delimeters[index])+" <@cross-ref-fig-open>$3\\.$4<@cross-ref-fig-close>"); // en-dash
			
			line = line.replaceAll("(?i)Fig E([0-9]+)\\-([0-9]+) "+(Constants.delimeters[index])+" E([0-9]+)\\-([0-9]+)", "<@cross-ref-fig-open>Fig $1\\-$2<@cross-ref-fig-close> "+(Constants.delimeters[index])+" <@cross-ref-fig-open>$3\\-$4<@cross-ref-fig-close>"); // simple dash
			line = line.replaceAll("(?i)Fig E([0-9]+)\\—([0-9]+) "+(Constants.delimeters[index])+" E([0-9]+)\\—([0-9]+)", "<@cross-ref-fig-open>Fig $1\\-$2<@cross-ref-fig-close> "+(Constants.delimeters[index])+" <@cross-ref-fig-open>$3\\-$4<@cross-ref-fig-close>"); // em-dash
			line = line.replaceAll("(?i)Fig E([0-9]+)\\–([0-9]+) "+(Constants.delimeters[index])+" E([0-9]+)\\–([0-9]+)", "<@cross-ref-fig-open>Fig $1\\-$2<@cross-ref-fig-close> "+(Constants.delimeters[index])+" <@cross-ref-fig-open>$3\\-$4<@cross-ref-fig-close>"); // en-dash
			line = line.replaceAll("(?i)Fig E([0-9]+)\\.([0-9]+) "+(Constants.delimeters[index])+" E([0-9]+)\\.([0-9]+)", "<@cross-ref-fig-open>Fig $1\\.$2<@cross-ref-fig-close> "+(Constants.delimeters[index])+" <@cross-ref-fig-open>$3\\.$4<@cross-ref-fig-close>"); // en-dash
			
			line = line.replaceAll("(?i)Figure\\. E([0-9]+)\\-([0-9]+) "+(Constants.delimeters[index])+" E([0-9]+)\\-([0-9]+)", "<@cross-ref-fig-open>Figure\\. $1\\-$2<@cross-ref-fig-close> "+(Constants.delimeters[index])+" <@cross-ref-fig-open>$3\\-$4<@cross-ref-fig-close>"); // simple dash
			line = line.replaceAll("(?i)Figure\\. E([0-9]+)\\—([0-9]+) "+(Constants.delimeters[index])+" E([0-9]+)\\—([0-9]+)", "<@cross-ref-fig-open>Figure\\. $1\\-$2<@cross-ref-fig-close> "+(Constants.delimeters[index])+" <@cross-ref-fig-open>$3\\-$4<@cross-ref-fig-close>"); // em-dash
			line = line.replaceAll("(?i)Figure\\. E([0-9]+)\\–([0-9]+) "+(Constants.delimeters[index])+" E([0-9]+)\\–([0-9]+)", "<@cross-ref-fig-open>Figure\\. $1\\-$2<@cross-ref-fig-close> "+(Constants.delimeters[index])+" <@cross-ref-fig-open>$3\\-$4<@cross-ref-fig-close>"); // en-dash
			line = line.replaceAll("(?i)Figure\\. E([0-9]+)\\.([0-9]+) "+(Constants.delimeters[index])+" E([0-9]+)\\.([0-9]+)", "<@cross-ref-fig-open>Figure\\. $1\\.$2<@cross-ref-fig-close> "+(Constants.delimeters[index])+" <@cross-ref-fig-open>$3\\.$4<@cross-ref-fig-close>"); // en-dash
			
			line = line.replaceAll("(?i)Figure E([0-9]+)\\-([0-9]+) "+(Constants.delimeters[index])+" E([0-9]+)\\-([0-9]+)", "<@cross-ref-fig-open>Figure $1\\-$2<@cross-ref-fig-close> "+(Constants.delimeters[index])+" <@cross-ref-fig-open>$3\\-$4<@cross-ref-fig-close>"); // simple dash
			line = line.replaceAll("(?i)Figure E([0-9]+)\\—([0-9]+) "+(Constants.delimeters[index])+" E([0-9]+)\\—([0-9]+)", "<@cross-ref-fig-open>Figure $1\\-$2<@cross-ref-fig-close> "+(Constants.delimeters[index])+" <@cross-ref-fig-open>$3\\-$4<@cross-ref-fig-close>"); // em-dash
			line = line.replaceAll("(?i)Figure E([0-9]+)\\–([0-9]+) "+(Constants.delimeters[index])+" E([0-9]+)\\–([0-9]+)", "<@cross-ref-fig-open>Figure $1\\-$2<@cross-ref-fig-close> "+(Constants.delimeters[index])+" <@cross-ref-fig-open>$3\\-$4<@cross-ref-fig-close>"); // en-dash
			line = line.replaceAll("(?i)Figure E([0-9]+)\\.([0-9]+) "+(Constants.delimeters[index])+" E([0-9]+)\\.([0-9]+)", "<@cross-ref-fig-open>Figure $1\\.$2<@cross-ref-fig-close> "+(Constants.delimeters[index])+" <@cross-ref-fig-open>$3\\.$4<@cross-ref-fig-close>"); // en-dash
			
			
			
			line = line.replaceAll("(?i)Figs\\. E([0-9]+)\\-([0-9]+) "+(Constants.delimeters[index])+" E([0-9]+)\\-([0-9]+)", "<@cross-ref-fig-open>Figs\\. $1\\-$2<@cross-ref-fig-close> "+(Constants.delimeters[index])+" <@cross-ref-fig-open>$3\\-$4<@cross-ref-fig-close>"); // simple dash
			line = line.replaceAll("(?i)Figs\\. E([0-9]+)\\—([0-9]+) "+(Constants.delimeters[index])+" E([0-9]+)\\—([0-9]+)", "<@cross-ref-fig-open>Figs\\. $1\\-$2<@cross-ref-fig-close> "+(Constants.delimeters[index])+" <@cross-ref-fig-open>$3\\-$4<@cross-ref-fig-close>"); // em-dash
			line = line.replaceAll("(?i)Figs\\. E([0-9]+)\\–([0-9]+) "+(Constants.delimeters[index])+" E([0-9]+)\\–([0-9]+)", "<@cross-ref-fig-open>Figs\\. $1\\-$2<@cross-ref-fig-close> "+(Constants.delimeters[index])+" <@cross-ref-fig-open>$3\\-$4<@cross-ref-fig-close>"); // en-dash
			line = line.replaceAll("(?i)Figs\\. E([0-9]+)\\.([0-9]+) "+(Constants.delimeters[index])+" E([0-9]+)\\.([0-9]+)", "<@cross-ref-fig-open>Figs\\. $1\\.$2<@cross-ref-fig-close> "+(Constants.delimeters[index])+" <@cross-ref-fig-open>$3\\.$4<@cross-ref-fig-close>"); // en-dash
			
			line = line.replaceAll("(?i)Figs E([0-9]+)\\-([0-9]+) "+(Constants.delimeters[index])+" E([0-9]+)\\-([0-9]+)", "<@cross-ref-fig-open>Figs $1\\-$2<@cross-ref-fig-close> "+(Constants.delimeters[index])+" <@cross-ref-fig-open>$3\\-$4<@cross-ref-fig-close>"); // simple dash
			line = line.replaceAll("(?i)Figs E([0-9]+)\\—([0-9]+) "+(Constants.delimeters[index])+" E([0-9]+)\\—([0-9]+)", "<@cross-ref-fig-open>Figs $1\\-$2<@cross-ref-fig-close> "+(Constants.delimeters[index])+" <@cross-ref-fig-open>$3\\-$4<@cross-ref-fig-close>"); // em-dash
			line = line.replaceAll("(?i)Figs E([0-9]+)\\–([0-9]+) "+(Constants.delimeters[index])+" E([0-9]+)\\–([0-9]+)", "<@cross-ref-fig-open>Figs $1\\-$2<@cross-ref-fig-close> "+(Constants.delimeters[index])+" <@cross-ref-fig-open>$3\\-$4<@cross-ref-fig-close>"); // en-dash
			line = line.replaceAll("(?i)Figs E([0-9]+)\\.([0-9]+) "+(Constants.delimeters[index])+" E([0-9]+)\\.([0-9]+)", "<@cross-ref-fig-open>Figs $1\\.$2<@cross-ref-fig-close> "+(Constants.delimeters[index])+" <@cross-ref-fig-open>$3\\.$4<@cross-ref-fig-close>"); // en-dash
			
			line = line.replaceAll("(?i)Figures\\. E([0-9]+)\\-([0-9]+) "+(Constants.delimeters[index])+" E([0-9]+)\\-([0-9]+)", "<@cross-ref-fig-open>Figures\\. $1\\-$2<@cross-ref-fig-close> "+(Constants.delimeters[index])+" <@cross-ref-fig-open>$3\\-$4<@cross-ref-fig-close>"); // simple dash
			line = line.replaceAll("(?i)Figures\\. E([0-9]+)\\—([0-9]+) "+(Constants.delimeters[index])+" E([0-9]+)\\—([0-9]+)", "<@cross-ref-fig-open>Figures\\. $1\\-$2<@cross-ref-fig-close> "+(Constants.delimeters[index])+" <@cross-ref-fig-open>$3\\-$4<@cross-ref-fig-close>"); // em-dash
			line = line.replaceAll("(?i)Figures\\. E([0-9]+)\\–([0-9]+) "+(Constants.delimeters[index])+" E([0-9]+)\\–([0-9]+)", "<@cross-ref-fig-open>Figures\\. $1\\-$2<@cross-ref-fig-close> "+(Constants.delimeters[index])+" <@cross-ref-fig-open>$3\\-$4<@cross-ref-fig-close>"); // en-dash
			line = line.replaceAll("(?i)Figures\\. E([0-9]+)\\.([0-9]+) "+(Constants.delimeters[index])+" E([0-9]+)\\.([0-9]+)", "<@cross-ref-fig-open>Figures\\. $1\\.$2<@cross-ref-fig-close> "+(Constants.delimeters[index])+" <@cross-ref-fig-open>$3\\.$4<@cross-ref-fig-close>"); // en-dash
			
			line = line.replaceAll("(?i)Figures E([0-9]+)\\-([0-9]+) "+(Constants.delimeters[index])+" E([0-9]+)\\-([0-9]+)", "<@cross-ref-fig-open>Figures $1\\-$2<@cross-ref-fig-close> "+(Constants.delimeters[index])+" <@cross-ref-fig-open>$3\\-$4<@cross-ref-fig-close>"); // simple dash
			line = line.replaceAll("(?i)Figures E([0-9]+)\\—([0-9]+) "+(Constants.delimeters[index])+" E([0-9]+)\\—([0-9]+)", "<@cross-ref-fig-open>Figures $1\\-$2<@cross-ref-fig-close> "+(Constants.delimeters[index])+" <@cross-ref-fig-open>$3\\-$4<@cross-ref-fig-close>"); // em-dash
			line = line.replaceAll("(?i)Figures E([0-9]+)\\–([0-9]+) "+(Constants.delimeters[index])+" E([0-9]+)\\–([0-9]+)", "<@cross-ref-fig-open>Figures $1\\-$2<@cross-ref-fig-close> "+(Constants.delimeters[index])+" <@cross-ref-fig-open>$3\\-$4<@cross-ref-fig-close>"); // en-dash
			line = line.replaceAll("(?i)Figures E([0-9]+)\\.([0-9]+) "+(Constants.delimeters[index])+" E([0-9]+)\\.([0-9]+)", "<@cross-ref-fig-open>Figures $1\\.$2<@cross-ref-fig-close> "+(Constants.delimeters[index])+" <@cross-ref-fig-open>$3\\.$4<@cross-ref-fig-close>"); // en-dash
			
			
			/**
			 * 
			 * ELECTRONIC single space both side
			 */
			line = line.replaceAll("(?i)Figs\\. E([0-9]+)\\- ([0-9]+) "+(Constants.delimeters[index])+" E([0-9]+)\\- ([0-9]+)", "<@cross-ref-fig-open>Figs\\. $1\\-$2<@cross-ref-fig-close> "+(Constants.delimeters[index])+" <@cross-ref-fig-open>$3\\-$4<@cross-ref-fig-close>"); // simple dash
			line = line.replaceAll("(?i)Figs\\. E([0-9]+)\\— ([0-9]+) "+(Constants.delimeters[index])+" E([0-9]+)\\— ([0-9]+)", "<@cross-ref-fig-open>Figs\\. $1\\-$2<@cross-ref-fig-close> "+(Constants.delimeters[index])+" <@cross-ref-fig-open>$3\\-$4<@cross-ref-fig-close>"); // em-dash
			line = line.replaceAll("(?i)Figs\\. E([0-9]+)\\– ([0-9]+) "+(Constants.delimeters[index])+" E([0-9]+)\\– ([0-9]+)", "<@cross-ref-fig-open>Figs\\. $1\\-$2<@cross-ref-fig-close> "+(Constants.delimeters[index])+" <@cross-ref-fig-open>$3\\-$4<@cross-ref-fig-close>"); // en-dash
			line = line.replaceAll("(?i)Figs\\. E([0-9]+)\\. ([0-9]+) "+(Constants.delimeters[index])+" E([0-9]+)\\. ([0-9]+)", "<@cross-ref-fig-open>Figs\\. $1\\.$2<@cross-ref-fig-close> "+(Constants.delimeters[index])+" <@cross-ref-fig-open>$3\\.$4<@cross-ref-fig-close>"); // en-dash
			
			line = line.replaceAll("(?i)Figs E([0-9]+)\\- ([0-9]+) "+(Constants.delimeters[index])+" E([0-9]+)\\- ([0-9]+)", "<@cross-ref-fig-open>Figs $1\\-$2<@cross-ref-fig-close> "+(Constants.delimeters[index])+" <@cross-ref-fig-open>$3\\-$4<@cross-ref-fig-close>"); // simple dash
			line = line.replaceAll("(?i)Figs E([0-9]+)\\— ([0-9]+) "+(Constants.delimeters[index])+" E([0-9]+)\\— ([0-9]+)", "<@cross-ref-fig-open>Figs $1\\-$2<@cross-ref-fig-close> "+(Constants.delimeters[index])+" <@cross-ref-fig-open>$3\\-$4<@cross-ref-fig-close>"); // em-dash
			line = line.replaceAll("(?i)Figs E([0-9]+)\\– ([0-9]+) "+(Constants.delimeters[index])+" E([0-9]+)\\– ([0-9]+)", "<@cross-ref-fig-open>Figs $1\\-$2<@cross-ref-fig-close> "+(Constants.delimeters[index])+" <@cross-ref-fig-open>$3\\-$4<@cross-ref-fig-close>"); // en-dash
			line = line.replaceAll("(?i)Figs E([0-9]+)\\. ([0-9]+) "+(Constants.delimeters[index])+" E([0-9]+)\\. ([0-9]+)", "<@cross-ref-fig-open>Figs $1\\.$2<@cross-ref-fig-close> "+(Constants.delimeters[index])+" <@cross-ref-fig-open>$3\\.$4<@cross-ref-fig-close>"); // en-dash
			
			line = line.replaceAll("(?i)Figures\\. E([0-9]+)\\- ([0-9]+) "+(Constants.delimeters[index])+" E([0-9]+)\\- ([0-9]+)", "<@cross-ref-fig-open>Figures\\. $1\\-$2<@cross-ref-fig-close> "+(Constants.delimeters[index])+" <@cross-ref-fig-open>$3\\-$4<@cross-ref-fig-close>"); // simple dash
			line = line.replaceAll("(?i)Figures\\. E([0-9]+)\\— ([0-9]+) "+(Constants.delimeters[index])+" E([0-9]+)\\— ([0-9]+)", "<@cross-ref-fig-open>Figures\\. $1\\-$2<@cross-ref-fig-close> "+(Constants.delimeters[index])+" <@cross-ref-fig-open>$3\\-$4<@cross-ref-fig-close>"); // em-dash
			line = line.replaceAll("(?i)Figures\\. E([0-9]+)\\– ([0-9]+) "+(Constants.delimeters[index])+" E([0-9]+)\\– ([0-9]+)", "<@cross-ref-fig-open>Figures\\. $1\\-$2<@cross-ref-fig-close> "+(Constants.delimeters[index])+" <@cross-ref-fig-open>$3\\-$4<@cross-ref-fig-close>"); // en-dash
			line = line.replaceAll("(?i)Figures\\. E([0-9]+)\\. ([0-9]+) "+(Constants.delimeters[index])+" E([0-9]+)\\. ([0-9]+)", "<@cross-ref-fig-open>Figures\\. $1\\.$2<@cross-ref-fig-close> "+(Constants.delimeters[index])+" <@cross-ref-fig-open>$3\\.$4<@cross-ref-fig-close>"); // en-dash
			
			line = line.replaceAll("(?i)Figures E([0-9]+)\\- ([0-9]+) "+(Constants.delimeters[index])+" E([0-9]+)\\- ([0-9]+)", "<@cross-ref-fig-open>Figures $1\\-$2<@cross-ref-fig-close> "+(Constants.delimeters[index])+" <@cross-ref-fig-open>$3\\-$4<@cross-ref-fig-close>"); // simple dash
			line = line.replaceAll("(?i)Figures E([0-9]+)\\— ([0-9]+) "+(Constants.delimeters[index])+" E([0-9]+)\\— ([0-9]+)", "<@cross-ref-fig-open>Figures $1\\-$2<@cross-ref-fig-close> "+(Constants.delimeters[index])+" <@cross-ref-fig-open>$3\\-$4<@cross-ref-fig-close>"); // em-dash
			line = line.replaceAll("(?i)Figures E([0-9]+)\\– ([0-9]+) "+(Constants.delimeters[index])+" E([0-9]+)\\– ([0-9]+)", "<@cross-ref-fig-open>Figures $1\\-$2<@cross-ref-fig-close> "+(Constants.delimeters[index])+" <@cross-ref-fig-open>$3\\-$4<@cross-ref-fig-close>"); // en-dash
			line = line.replaceAll("(?i)Figures E([0-9]+)\\. ([0-9]+) "+(Constants.delimeters[index])+" E([0-9]+)\\. ([0-9]+)", "<@cross-ref-fig-open>Figures $1\\.$2<@cross-ref-fig-close> "+(Constants.delimeters[index])+" <@cross-ref-fig-open>$3\\.$4<@cross-ref-fig-close>"); // en-dash
			
			/**
			 * 
			 * ELECTRONIC single space left side
			 */
			line = line.replaceAll("(?i)Figs\\. E([0-9]+)\\- ([0-9]+) "+(Constants.delimeters[index])+" E([0-9]+)\\-([0-9]+)", "<@cross-ref-fig-open>Figs\\. $1\\-$2<@cross-ref-fig-close> "+(Constants.delimeters[index])+" <@cross-ref-fig-open>$3\\-$4<@cross-ref-fig-close>"); // simple dash
			line = line.replaceAll("(?i)Figs\\. E([0-9]+)\\— ([0-9]+) "+(Constants.delimeters[index])+" E([0-9]+)\\—([0-9]+)", "<@cross-ref-fig-open>Figs\\. $1\\-$2<@cross-ref-fig-close> "+(Constants.delimeters[index])+" <@cross-ref-fig-open>$3\\-$4<@cross-ref-fig-close>"); // em-dash
			line = line.replaceAll("(?i)Figs\\. E([0-9]+)\\– ([0-9]+) "+(Constants.delimeters[index])+" E([0-9]+)\\–([0-9]+)", "<@cross-ref-fig-open>Figs\\. $1\\-$2<@cross-ref-fig-close> "+(Constants.delimeters[index])+" <@cross-ref-fig-open>$3\\-$4<@cross-ref-fig-close>"); // en-dash
			line = line.replaceAll("(?i)Figs\\. E([0-9]+)\\. ([0-9]+) "+(Constants.delimeters[index])+" E([0-9]+)\\.([0-9]+)", "<@cross-ref-fig-open>Figs\\. $1\\.$2<@cross-ref-fig-close> "+(Constants.delimeters[index])+" <@cross-ref-fig-open>$3\\.$4<@cross-ref-fig-close>"); // en-dash
			
			line = line.replaceAll("(?i)Figs E([0-9]+)\\- ([0-9]+) "+(Constants.delimeters[index])+" E([0-9]+)\\-([0-9]+)", "<@cross-ref-fig-open>Figs $1\\-$2<@cross-ref-fig-close> "+(Constants.delimeters[index])+" <@cross-ref-fig-open>$3\\-$4<@cross-ref-fig-close>"); // simple dash
			line = line.replaceAll("(?i)Figs E([0-9]+)\\— ([0-9]+) "+(Constants.delimeters[index])+" E([0-9]+)\\—([0-9]+)", "<@cross-ref-fig-open>Figs $1\\-$2<@cross-ref-fig-close> "+(Constants.delimeters[index])+" <@cross-ref-fig-open>$3\\-$4<@cross-ref-fig-close>"); // em-dash
			line = line.replaceAll("(?i)Figs E([0-9]+)\\– ([0-9]+) "+(Constants.delimeters[index])+" E([0-9]+)\\–([0-9]+)", "<@cross-ref-fig-open>Figs $1\\-$2<@cross-ref-fig-close> "+(Constants.delimeters[index])+" <@cross-ref-fig-open>$3\\-$4<@cross-ref-fig-close>"); // en-dash
			line = line.replaceAll("(?i)Figs E([0-9]+)\\. ([0-9]+) "+(Constants.delimeters[index])+" E([0-9]+)\\.([0-9]+)", "<@cross-ref-fig-open>Figs $1\\.$2<@cross-ref-fig-close> "+(Constants.delimeters[index])+" <@cross-ref-fig-open>$3\\.$4<@cross-ref-fig-close>"); // en-dash
			
			line = line.replaceAll("(?i)Figures\\. E([0-9]+)\\- ([0-9]+) "+(Constants.delimeters[index])+" E([0-9]+)\\-([0-9]+)", "<@cross-ref-fig-open>Figures\\. $1\\-$2<@cross-ref-fig-close> "+(Constants.delimeters[index])+" <@cross-ref-fig-open>$3\\-$4<@cross-ref-fig-close>"); // simple dash
			line = line.replaceAll("(?i)Figures\\. E([0-9]+)\\— ([0-9]+) "+(Constants.delimeters[index])+" E([0-9]+)\\—([0-9]+)", "<@cross-ref-fig-open>Figures\\. $1\\-$2<@cross-ref-fig-close> "+(Constants.delimeters[index])+" <@cross-ref-fig-open>$3\\-$4<@cross-ref-fig-close>"); // em-dash
			line = line.replaceAll("(?i)Figures\\. E([0-9]+)\\– ([0-9]+) "+(Constants.delimeters[index])+" E([0-9]+)\\–([0-9]+)", "<@cross-ref-fig-open>Figures\\. $1\\-$2<@cross-ref-fig-close> "+(Constants.delimeters[index])+" <@cross-ref-fig-open>$3\\-$4<@cross-ref-fig-close>"); // en-dash
			line = line.replaceAll("(?i)Figures\\. E([0-9]+)\\. ([0-9]+) "+(Constants.delimeters[index])+" E([0-9]+)\\.([0-9]+)", "<@cross-ref-fig-open>Figures\\. $1\\.$2<@cross-ref-fig-close> "+(Constants.delimeters[index])+" <@cross-ref-fig-open>$3\\.$4<@cross-ref-fig-close>"); // en-dash
			
			line = line.replaceAll("(?i)Figures E([0-9]+)\\- ([0-9]+) "+(Constants.delimeters[index])+" E([0-9]+)\\-([0-9]+)", "<@cross-ref-fig-open>Figures $1\\-$2<@cross-ref-fig-close> "+(Constants.delimeters[index])+" <@cross-ref-fig-open>$3\\-$4<@cross-ref-fig-close>"); // simple dash
			line = line.replaceAll("(?i)Figures E([0-9]+)\\— ([0-9]+) "+(Constants.delimeters[index])+" E([0-9]+)\\—([0-9]+)", "<@cross-ref-fig-open>Figures $1\\-$2<@cross-ref-fig-close> "+(Constants.delimeters[index])+" <@cross-ref-fig-open>$3\\-$4<@cross-ref-fig-close>"); // em-dash
			line = line.replaceAll("(?i)Figures E([0-9]+)\\– ([0-9]+) "+(Constants.delimeters[index])+" E([0-9]+)\\–([0-9]+)", "<@cross-ref-fig-open>Figures $1\\-$2<@cross-ref-fig-close> "+(Constants.delimeters[index])+" <@cross-ref-fig-open>$3\\-$4<@cross-ref-fig-close>"); // en-dash
			line = line.replaceAll("(?i)Figures E([0-9]+)\\. ([0-9]+) "+(Constants.delimeters[index])+" E([0-9]+)\\.([0-9]+)", "<@cross-ref-fig-open>Figures $1\\.$2<@cross-ref-fig-close> "+(Constants.delimeters[index])+" <@cross-ref-fig-open>$3\\.$4<@cross-ref-fig-close>"); // en-dash
			
			/**
			 * 
			 * ELECTRONIC single space right side
			 */
			line = line.replaceAll("(?i)Figs\\. E([0-9]+)\\-([0-9]+) "+(Constants.delimeters[index])+" E([0-9]+)\\- ([0-9]+)", "<@cross-ref-fig-open>Figs\\. $1\\-$2<@cross-ref-fig-close> "+(Constants.delimeters[index])+" <@cross-ref-fig-open>$3\\-$4<@cross-ref-fig-close>"); // simple dash
			line = line.replaceAll("(?i)Figs\\. E([0-9]+)\\—([0-9]+) "+(Constants.delimeters[index])+" E([0-9]+)\\— ([0-9]+)", "<@cross-ref-fig-open>Figs\\. $1\\-$2<@cross-ref-fig-close> "+(Constants.delimeters[index])+" <@cross-ref-fig-open>$3\\-$4<@cross-ref-fig-close>"); // em-dash
			line = line.replaceAll("(?i)Figs\\. E([0-9]+)\\–([0-9]+) "+(Constants.delimeters[index])+" E([0-9]+)\\– ([0-9]+)", "<@cross-ref-fig-open>Figs\\. $1\\-$2<@cross-ref-fig-close> "+(Constants.delimeters[index])+" <@cross-ref-fig-open>$3\\-$4<@cross-ref-fig-close>"); // en-dash
			line = line.replaceAll("(?i)Figs\\. E([0-9]+)\\.([0-9]+) "+(Constants.delimeters[index])+" E([0-9]+)\\. ([0-9]+)", "<@cross-ref-fig-open>Figs\\. $1\\.$2<@cross-ref-fig-close> "+(Constants.delimeters[index])+" <@cross-ref-fig-open>$3\\.$4<@cross-ref-fig-close>"); // en-dash
			
			line = line.replaceAll("(?i)Figs E([0-9]+)\\-([0-9]+) "+(Constants.delimeters[index])+" E([0-9]+)\\- ([0-9]+)", "<@cross-ref-fig-open>Figs $1\\-$2<@cross-ref-fig-close> "+(Constants.delimeters[index])+" <@cross-ref-fig-open>$3\\-$4<@cross-ref-fig-close>"); // simple dash
			line = line.replaceAll("(?i)Figs E([0-9]+)\\—([0-9]+) "+(Constants.delimeters[index])+" E([0-9]+)\\— ([0-9]+)", "<@cross-ref-fig-open>Figs $1\\-$2<@cross-ref-fig-close> "+(Constants.delimeters[index])+" <@cross-ref-fig-open>$3\\-$4<@cross-ref-fig-close>"); // em-dash
			line = line.replaceAll("(?i)Figs E([0-9]+)\\–([0-9]+) "+(Constants.delimeters[index])+" E([0-9]+)\\– ([0-9]+)", "<@cross-ref-fig-open>Figs $1\\-$2<@cross-ref-fig-close> "+(Constants.delimeters[index])+" <@cross-ref-fig-open>$3\\-$4<@cross-ref-fig-close>"); // en-dash
			line = line.replaceAll("(?i)Figs E([0-9]+)\\.([0-9]+) "+(Constants.delimeters[index])+" E([0-9]+)\\. ([0-9]+)", "<@cross-ref-fig-open>Figs $1\\.$2<@cross-ref-fig-close> "+(Constants.delimeters[index])+" <@cross-ref-fig-open>$3\\.$4<@cross-ref-fig-close>"); // en-dash
			
			line = line.replaceAll("(?i)Figures\\. E([0-9]+)\\-([0-9]+) "+(Constants.delimeters[index])+" E([0-9]+)\\- ([0-9]+)", "<@cross-ref-fig-open>Figures\\. $1\\-$2<@cross-ref-fig-close> "+(Constants.delimeters[index])+" <@cross-ref-fig-open>$3\\-$4<@cross-ref-fig-close>"); // simple dash
			line = line.replaceAll("(?i)Figures\\. E([0-9]+)\\—([0-9]+) "+(Constants.delimeters[index])+" E([0-9]+)\\— ([0-9]+)", "<@cross-ref-fig-open>Figures\\. $1\\-$2<@cross-ref-fig-close> "+(Constants.delimeters[index])+" <@cross-ref-fig-open>$3\\-$4<@cross-ref-fig-close>"); // em-dash
			line = line.replaceAll("(?i)Figures\\. E([0-9]+)\\–([0-9]+) "+(Constants.delimeters[index])+" E([0-9]+)\\– ([0-9]+)", "<@cross-ref-fig-open>Figures\\. $1\\-$2<@cross-ref-fig-close> "+(Constants.delimeters[index])+" <@cross-ref-fig-open>$3\\-$4<@cross-ref-fig-close>"); // en-dash
			line = line.replaceAll("(?i)Figures\\. E([0-9]+)\\.([0-9]+) "+(Constants.delimeters[index])+" E([0-9]+)\\. ([0-9]+)", "<@cross-ref-fig-open>Figures\\. $1\\.$2<@cross-ref-fig-close> "+(Constants.delimeters[index])+" <@cross-ref-fig-open>$3\\.$4<@cross-ref-fig-close>"); // en-dash
			
			line = line.replaceAll("(?i)Figures E([0-9]+)\\-([0-9]+) "+(Constants.delimeters[index])+" E([0-9]+)\\- ([0-9]+)", "<@cross-ref-fig-open>Figures $1\\-$2<@cross-ref-fig-close> "+(Constants.delimeters[index])+" <@cross-ref-fig-open>$3\\-$4<@cross-ref-fig-close>"); // simple dash
			line = line.replaceAll("(?i)Figures E([0-9]+)\\—([0-9]+) "+(Constants.delimeters[index])+" E([0-9]+)\\— ([0-9]+)", "<@cross-ref-fig-open>Figures $1\\-$2<@cross-ref-fig-close> "+(Constants.delimeters[index])+" <@cross-ref-fig-open>$3\\-$4<@cross-ref-fig-close>"); // em-dash
			line = line.replaceAll("(?i)Figures E([0-9]+)\\–([0-9]+) "+(Constants.delimeters[index])+" E([0-9]+)\\– ([0-9]+)", "<@cross-ref-fig-open>Figures $1\\-$2<@cross-ref-fig-close> "+(Constants.delimeters[index])+" <@cross-ref-fig-open>$3\\-$4<@cross-ref-fig-close>"); // en-dash
			line = line.replaceAll("(?i)Figures E([0-9]+)\\.([0-9]+) "+(Constants.delimeters[index])+" E([0-9]+)\\. ([0-9]+)", "<@cross-ref-fig-open>Figures $1\\.$2<@cross-ref-fig-close> "+(Constants.delimeters[index])+" <@cross-ref-fig-open>$3\\.$4<@cross-ref-fig-close>"); // en-dash

			
			if (line.contains("<@cross-ref-fig-open>"))	isFound = true;
			if ( ! isFound) {
				
				line = line.replaceAll("(?i)Fig\\. ([0-9]+) "+(Constants.delimeters[index])+" ([0-9]+)", "<@cross-ref-fig-open>Fig\\. $1<@cross-ref-fig-close> "+(Constants.delimeters[index])+" <@cross-ref-fig-open>$2<@cross-ref-fig-close>"); // simple dash
				line = line.replaceAll("(?i)Fig ([0-9]+) "+(Constants.delimeters[index])+" ([0-9]+)", "<@cross-ref-fig-open>Fig $1<@cross-ref-fig-close> "+(Constants.delimeters[index])+" <@cross-ref-fig-open>$2<@cross-ref-fig-close>"); // simple dash
				line = line.replaceAll("(?i)Figure\\. ([0-9]+) "+(Constants.delimeters[index])+" ([0-9]+)", "<@cross-ref-fig-open>Figure\\. $1<@cross-ref-fig-close> "+(Constants.delimeters[index])+" <@cross-ref-fig-open>$2<@cross-ref-fig-close>"); // simple dash
				line = line.replaceAll("(?i)Figure ([0-9]+) "+(Constants.delimeters[index])+" ([0-9]+)", "<@cross-ref-fig-open>Figure $1<@cross-ref-fig-close> "+(Constants.delimeters[index])+" <@cross-ref-fig-open>$2<@cross-ref-fig-close>"); // simple dash
				line = line.replaceAll("(?i)Figs\\. ([0-9]+) "+(Constants.delimeters[index])+" ([0-9]+)", "<@cross-ref-fig-open>Figs\\. $1<@cross-ref-fig-close> "+(Constants.delimeters[index])+" <@cross-ref-fig-open>$2<@cross-ref-fig-close>"); // simple dash
				line = line.replaceAll("(?i)Figs ([0-9]+) "+(Constants.delimeters[index])+" ([0-9]+)", "<@cross-ref-fig-open>Figs $1<@cross-ref-fig-close> "+(Constants.delimeters[index])+" <@cross-ref-fig-open>$2<@cross-ref-fig-close>"); // simple dash
				line = line.replaceAll("(?i)Figures\\. ([0-9]+) "+(Constants.delimeters[index])+" ([0-9]+)", "<@cross-ref-fig-open>Figures\\. $1<@cross-ref-fig-close> "+(Constants.delimeters[index])+" <@cross-ref-fig-open>$2<@cross-ref-fig-close>"); // simple dash
				line = line.replaceAll("(?i)Figures ([0-9]+) "+(Constants.delimeters[index])+" ([0-9]+)", "<@cross-ref-fig-open>Figures $1<@cross-ref-fig-close> "+(Constants.delimeters[index])+" <@cross-ref-fig-open>$2<@cross-ref-fig-close>"); // simple dash
				
				
				/**
				 * 
				 * ELECTRONIC
				 */
				
				line = line.replaceAll("(?i)Fig\\. E([0-9]+) "+(Constants.delimeters[index])+" E([0-9]+)", "<@cross-ref-fig-open>Fig\\. $1<@cross-ref-fig-close> "+(Constants.delimeters[index])+" <@cross-ref-fig-open>$2<@cross-ref-fig-close>"); // simple dash
				line = line.replaceAll("(?i)Fig E([0-9]+) "+(Constants.delimeters[index])+" E([0-9]+)", "<@cross-ref-fig-open>Fig $1<@cross-ref-fig-close> "+(Constants.delimeters[index])+" <@cross-ref-fig-open>$2<@cross-ref-fig-close>"); // simple dash
				line = line.replaceAll("(?i)Figure\\. E([0-9]+) "+(Constants.delimeters[index])+" E([0-9]+)", "<@cross-ref-fig-open>Figure\\. $1<@cross-ref-fig-close> "+(Constants.delimeters[index])+" <@cross-ref-fig-open>$2<@cross-ref-fig-close>"); // simple dash
				line = line.replaceAll("(?i)Figure E([0-9]+) "+(Constants.delimeters[index])+" E([0-9]+)", "<@cross-ref-fig-open>Figure $1<@cross-ref-fig-close> "+(Constants.delimeters[index])+" <@cross-ref-fig-open>$2<@cross-ref-fig-close>"); // simple dash
				line = line.replaceAll("(?i)Figs\\. E([0-9]+) "+(Constants.delimeters[index])+" E([0-9]+)", "<@cross-ref-fig-open>Figs\\. $1<@cross-ref-fig-close> "+(Constants.delimeters[index])+" <@cross-ref-fig-open>$2<@cross-ref-fig-close>"); // simple dash
				line = line.replaceAll("(?i)Figs E([0-9]+) "+(Constants.delimeters[index])+" E([0-9]+)", "<@cross-ref-fig-open>Figs $1<@cross-ref-fig-close> "+(Constants.delimeters[index])+" <@cross-ref-fig-open>$2<@cross-ref-fig-close>"); // simple dash
				line = line.replaceAll("(?i)Figures\\. E([0-9]+) "+(Constants.delimeters[index])+" E([0-9]+)", "<@cross-ref-fig-open>Figures\\. $1<@cross-ref-fig-close> "+(Constants.delimeters[index])+" <@cross-ref-fig-open>$2<@cross-ref-fig-close>"); // simple dash
				line = line.replaceAll("(?i)Figures E([0-9]+) "+(Constants.delimeters[index])+" E([0-9]+)", "<@cross-ref-fig-open>Figures $1<@cross-ref-fig-close> "+(Constants.delimeters[index])+" <@cross-ref-fig-open>$2<@cross-ref-fig-close>"); // simple dash

			}
			
		}
		return line;
	}

	public String tableCrossMarking(String line) {
		
		boolean isFound = false;
		
		line = handleTableRangesDelimeters(line);
		
		/**
		 * multiple occurance handling
		 * 
		 */
		
		line = line.replaceAll("(?i)Table\\. ([0-9]+)\\-([0-9]+)", "<@cross-ref-table-open>Table\\. $1\\-$2<@cross-ref-table-close>"); // simple dash
		line = line.replaceAll("(?i)Table\\. ([0-9]+)\\—([0-9]+)", "<@cross-ref-table-open>Table\\. $1\\—$2<@cross-ref-table-close>"); // em-dash
		line = line.replaceAll("(?i)Table\\. ([0-9]+)\\–([0-9]+)", "<@cross-ref-table-open>Table\\. $1\\–$2<@cross-ref-table-close>"); // en-dash
		line = line.replaceAll("(?i)Table\\. ([0-9]+)\\.([0-9]+)", "<@cross-ref-table-open>Table\\. $1\\.$2<@cross-ref-table-close>"); // simple period
		
		line = line.replaceAll("(?i)Table ([0-9]+)\\-([0-9]+)", "<@cross-ref-table-open>Table $1\\-$2<@cross-ref-table-close>"); // simple dash
		line = line.replaceAll("(?i)Table ([0-9]+)\\—([0-9]+)", "<@cross-ref-table-open>Table $1\\—$2<@cross-ref-table-close>"); // em-dash
		line = line.replaceAll("(?i)Table ([0-9]+)\\–([0-9]+)", "<@cross-ref-table-open>Table $1\\–$2<@cross-ref-table-close>"); // en-dash
		line = line.replaceAll("(?i)Table ([0-9]+)\\.([0-9]+)", "<@cross-ref-table-open>Table $1\\.$2<@cross-ref-table-close>"); // simple period
		
		/**
		 * single space both side
		 * 
		 */
		line = line.replaceAll("(?i)Table\\. ([0-9]+)\\- ([0-9]+)", "<@cross-ref-table-open>Table\\. $1\\-$2<@cross-ref-table-close>"); // simple dash
		line = line.replaceAll("(?i)Table\\. ([0-9]+)\\— ([0-9]+)", "<@cross-ref-table-open>Table\\. $1\\—$2<@cross-ref-table-close>"); // em-dash
		line = line.replaceAll("(?i)Table\\. ([0-9]+)\\– ([0-9]+)", "<@cross-ref-table-open>Table\\. $1\\–$2<@cross-ref-table-close>"); // en-dash
		line = line.replaceAll("(?i)Table\\. ([0-9]+)\\. ([0-9]+)", "<@cross-ref-table-open>Table\\. $1\\.$2<@cross-ref-table-close>"); // simple period
		
		line = line.replaceAll("(?i)Table ([0-9]+)\\- ([0-9]+)", "<@cross-ref-table-open>Table $1\\-$2<@cross-ref-table-close>"); // simple dash
		line = line.replaceAll("(?i)Table ([0-9]+)\\— ([0-9]+)", "<@cross-ref-table-open>Table $1\\—$2<@cross-ref-table-close>"); // em-dash
		line = line.replaceAll("(?i)Table ([0-9]+)\\– ([0-9]+)", "<@cross-ref-table-open>Table $1\\–$2<@cross-ref-table-close>"); // en-dash
		line = line.replaceAll("(?i)Table ([0-9]+)\\. ([0-9]+)", "<@cross-ref-table-open>Table $1\\.$2<@cross-ref-table-close>"); // simple period
		
		/**
		 * ELECTRONIC
		 */
		
		/**
		 * ELECTRONIC multiple occurance handling
		 * 
		 */
		
		line = line.replaceAll("(?i)Table\\. E([0-9]+)\\-([0-9]+)", "<@cross-ref-table-open>Table\\. E$1\\-$2<@cross-ref-table-close>"); // simple dash
		line = line.replaceAll("(?i)Table\\. E([0-9]+)\\—([0-9]+)", "<@cross-ref-table-open>Table\\. E$1\\—$2<@cross-ref-table-close>"); // em-dash
		line = line.replaceAll("(?i)Table\\. E([0-9]+)\\–([0-9]+)", "<@cross-ref-table-open>Table\\. E$1\\–$2<@cross-ref-table-close>"); // en-dash
		line = line.replaceAll("(?i)Table\\. E([0-9]+)\\.([0-9]+)", "<@cross-ref-table-open>Table\\. E$1\\.$2<@cross-ref-table-close>"); // simple period
		
		line = line.replaceAll("(?i)Table E([0-9]+)\\-([0-9]+)", "<@cross-ref-table-open>Table E$1\\-$2<@cross-ref-table-close>"); // simple dash
		line = line.replaceAll("(?i)Table E([0-9]+)\\—([0-9]+)", "<@cross-ref-table-open>Table E$1\\—$2<@cross-ref-table-close>"); // em-dash
		line = line.replaceAll("(?i)Table E([0-9]+)\\–([0-9]+)", "<@cross-ref-table-open>Table E$1\\–$2<@cross-ref-table-close>"); // en-dash
		line = line.replaceAll("(?i)Table E([0-9]+)\\.([0-9]+)", "<@cross-ref-table-open>Table E$1\\.$2<@cross-ref-table-close>"); // simple period
		
		/**
		 * ELECTRONIC single space both side
		 * 
		 */
		line = line.replaceAll("(?i)Table\\. E([0-9]+)\\- ([0-9]+)", "<@cross-ref-table-open>Table\\. E$1\\-$2<@cross-ref-table-close>"); // simple dash
		line = line.replaceAll("(?i)Table\\. E([0-9]+)\\— ([0-9]+)", "<@cross-ref-table-open>Table\\. E$1\\—$2<@cross-ref-table-close>"); // em-dash
		line = line.replaceAll("(?i)Table\\. E([0-9]+)\\– ([0-9]+)", "<@cross-ref-table-open>Table\\. E$1\\–$2<@cross-ref-table-close>"); // en-dash
		line = line.replaceAll("(?i)Table\\. E([0-9]+)\\. ([0-9]+)", "<@cross-ref-table-open>Table\\. E$1\\.$2<@cross-ref-table-close>"); // simple period
		
		line = line.replaceAll("(?i)Table E([0-9]+)\\- ([0-9]+)", "<@cross-ref-table-open>Table E$1\\-$2<@cross-ref-table-close>"); // simple dash
		line = line.replaceAll("(?i)Table E([0-9]+)\\— ([0-9]+)", "<@cross-ref-table-open>Table E$1\\—$2<@cross-ref-table-close>"); // em-dash
		line = line.replaceAll("(?i)Table E([0-9]+)\\– ([0-9]+)", "<@cross-ref-table-open>Table E$1\\–$2<@cross-ref-table-close>"); // en-dash
		line = line.replaceAll("(?i)Table E([0-9]+)\\. ([0-9]+)", "<@cross-ref-table-open>Table E$1\\.$2<@cross-ref-table-close>"); // simple period

		
		if (line.contains("<@cross-ref-table-open>"))	isFound = true;
		if ( ! isFound) {
			/**
			 * single occurance handling
			 * 
			 */
			line = line.replaceAll("(?i)Table\\. ([0-9]+)", "<@cross-ref-table-open>Table\\. $1<@cross-ref-table-close>");
			line = line.replaceAll("(?i)Table ([0-9]+)", "<@cross-ref-table-open>Table $1<@cross-ref-table-close>");
			
			/**
			 * 
			 * ELECTRONIC
			 */
			
			line = line.replaceAll("(?i)Table\\. E([0-9]+)", "<@cross-ref-table-open>Table\\. E$1<@cross-ref-table-close>");
			line = line.replaceAll("(?i)Table E([0-9]+)", "<@cross-ref-table-open>Table E$1<@cross-ref-table-close>");
		}
		return line;
	}
	
	private String handleTableRangesDelimeters(String line) {
		
		for (int index = 0; index < Constants.delimeters.length; index ++) {
			
			boolean isFound = false;
			
			line = line.replaceAll("(?i)Table\\. ([0-9]+)\\-([0-9]+) "+(Constants.delimeters[index])+" ([0-9]+)\\-([0-9]+)", "<@cross-ref-table-open>Table\\. $1\\-$2<@cross-ref-table-close> "+(Constants.delimeters[index])+" <@cross-ref-table-open>$3\\-$4<@cross-ref-table-close>"); // simple dash
			line = line.replaceAll("(?i)Table\\. ([0-9]+)\\—([0-9]+) "+(Constants.delimeters[index])+" ([0-9]+)\\—([0-9]+)", "<@cross-ref-table-open>Table\\. $1\\—$2<@cross-ref-table-close> "+(Constants.delimeters[index])+" <@cross-ref-table-open>$3\\-$4<@cross-ref-table-close>"); // em-dash
			line = line.replaceAll("(?i)Table\\. ([0-9]+)\\–([0-9]+) "+(Constants.delimeters[index])+" ([0-9]+)\\–([0-9]+)", "<@cross-ref-table-open>Table\\. $1\\–$2<@cross-ref-table-close> "+(Constants.delimeters[index])+" <@cross-ref-table-open>$3\\-$4<@cross-ref-table-close>"); // en-dash
			line = line.replaceAll("(?i)Table\\. ([0-9]+)\\.([0-9]+) "+(Constants.delimeters[index])+" ([0-9]+)\\.([0-9]+)", "<@cross-ref-table-open>Table\\. $1\\.$2<@cross-ref-table-close> "+(Constants.delimeters[index])+" <@cross-ref-table-open>$3\\.$4<@cross-ref-table-close>"); // simple period
			
			line = line.replaceAll("(?i)Table ([0-9]+)\\-([0-9]+) "+(Constants.delimeters[index])+" ([0-9]+)\\-([0-9]+)", "<@cross-ref-table-open>Table $1\\-$2<@cross-ref-table-close> "+(Constants.delimeters[index])+" <@cross-ref-table-open>$3\\-$4<@cross-ref-table-close>"); // simple dash
			line = line.replaceAll("(?i)Table ([0-9]+)\\—([0-9]+) "+(Constants.delimeters[index])+" ([0-9]+)\\—([0-9]+)", "<@cross-ref-table-open>Table $1\\—$2<@cross-ref-table-close> "+(Constants.delimeters[index])+" <@cross-ref-table-open>$3\\-$4<@cross-ref-table-close>"); // em-dash
			line = line.replaceAll("(?i)Table ([0-9]+)\\–([0-9]+) "+(Constants.delimeters[index])+" ([0-9]+)\\–([0-9]+)", "<@cross-ref-table-open>Table $1\\–$2<@cross-ref-table-close> "+(Constants.delimeters[index])+" <@cross-ref-table-open>$3\\-$4<@cross-ref-table-close>"); // en-dash
			line = line.replaceAll("(?i)Table ([0-9]+)\\.([0-9]+) "+(Constants.delimeters[index])+" ([0-9]+)\\.([0-9]+)", "<@cross-ref-table-open>Table $1\\.$2<@cross-ref-table-close> "+(Constants.delimeters[index])+" <@cross-ref-table-open>$3\\.$4<@cross-ref-table-close>"); // simple period
			
			
			line = line.replaceAll("(?i)Tables\\. ([0-9]+)\\-([0-9]+) "+(Constants.delimeters[index])+" ([0-9]+)\\-([0-9]+)", "<@cross-ref-table-open>Tables\\. $1\\-$2<@cross-ref-table-close> "+(Constants.delimeters[index])+" <@cross-ref-table-open>$3\\-$4<@cross-ref-table-close>"); // simple dash
			line = line.replaceAll("(?i)Tables\\. ([0-9]+)\\—([0-9]+) "+(Constants.delimeters[index])+" ([0-9]+)\\—([0-9]+)", "<@cross-ref-table-open>Tables\\. $1\\—$2<@cross-ref-table-close> "+(Constants.delimeters[index])+" <@cross-ref-table-open>$3\\-$4<@cross-ref-table-close>"); // em-dash
			line = line.replaceAll("(?i)Tables\\. ([0-9]+)\\–([0-9]+) "+(Constants.delimeters[index])+" ([0-9]+)\\–([0-9]+)", "<@cross-ref-table-open>Tables\\. $1\\–$2<@cross-ref-table-close> "+(Constants.delimeters[index])+" <@cross-ref-table-open>$3\\-$4<@cross-ref-table-close>"); // en-dash
			line = line.replaceAll("(?i)Tables\\. ([0-9]+)\\.([0-9]+) "+(Constants.delimeters[index])+" ([0-9]+)\\.([0-9]+)", "<@cross-ref-table-open>Tables\\. $1\\.$2<@cross-ref-table-close> "+(Constants.delimeters[index])+" <@cross-ref-table-open>$3\\.$4<@cross-ref-table-close>"); // simple period
			
			line = line.replaceAll("(?i)Tables ([0-9]+)\\-([0-9]+) "+(Constants.delimeters[index])+" ([0-9]+)\\-([0-9]+)", "<@cross-ref-table-open>Tables $1\\-$2<@cross-ref-table-close> "+(Constants.delimeters[index])+" <@cross-ref-table-open>$3\\-$4<@cross-ref-table-close>"); // simple dash
			line = line.replaceAll("(?i)Tables ([0-9]+)\\—([0-9]+) "+(Constants.delimeters[index])+" ([0-9]+)\\—([0-9]+)", "<@cross-ref-table-open>Tables $1\\—$2<@cross-ref-table-close> "+(Constants.delimeters[index])+" <@cross-ref-table-open>$3\\-$4<@cross-ref-table-close>"); // em-dash
			line = line.replaceAll("(?i)Tables ([0-9]+)\\–([0-9]+) "+(Constants.delimeters[index])+" ([0-9]+)\\–([0-9]+)", "<@cross-ref-table-open>Tables $1\\–$2<@cross-ref-table-close> "+(Constants.delimeters[index])+" <@cross-ref-table-open>$3\\-$4<@cross-ref-table-close>"); // en-dash
			line = line.replaceAll("(?i)Tables ([0-9]+)\\.([0-9]+) "+(Constants.delimeters[index])+" ([0-9]+)\\.([0-9]+)", "<@cross-ref-table-open>Tables $1\\.$2<@cross-ref-table-close> "+(Constants.delimeters[index])+" <@cross-ref-table-open>$3\\.$4<@cross-ref-table-close>"); // simple period
			
			
			/**
			 * with single space both side
			 * 
			 */
			line = line.replaceAll("(?i)Table\\. ([0-9]+)\\- ([0-9]+) "+(Constants.delimeters[index])+" ([0-9]+)\\- ([0-9]+)", "<@cross-ref-table-open>Table\\. $1\\-$2<@cross-ref-table-close> "+(Constants.delimeters[index])+" <@cross-ref-table-open>$3\\-$4<@cross-ref-table-close>"); // simple dash
			line = line.replaceAll("(?i)Table\\. ([0-9]+)\\— ([0-9]+) "+(Constants.delimeters[index])+" ([0-9]+)\\— ([0-9]+)", "<@cross-ref-table-open>Table\\. $1\\—$2<@cross-ref-table-close> "+(Constants.delimeters[index])+" <@cross-ref-table-open>$3\\-$4<@cross-ref-table-close>"); // em-dash
			line = line.replaceAll("(?i)Table\\. ([0-9]+)\\– ([0-9]+) "+(Constants.delimeters[index])+" ([0-9]+)\\– ([0-9]+)", "<@cross-ref-table-open>Table\\. $1\\–$2<@cross-ref-table-close> "+(Constants.delimeters[index])+" <@cross-ref-table-open>$3\\-$4<@cross-ref-table-close>"); // en-dash
			line = line.replaceAll("(?i)Table\\. ([0-9]+)\\. ([0-9]+) "+(Constants.delimeters[index])+" ([0-9]+)\\. ([0-9]+)", "<@cross-ref-table-open>Table\\. $1\\.$2<@cross-ref-table-close> "+(Constants.delimeters[index])+" <@cross-ref-table-open>$3\\.$4<@cross-ref-table-close>"); // simple period
			
			line = line.replaceAll("(?i)Table ([0-9]+)\\- ([0-9]+) "+(Constants.delimeters[index])+" ([0-9]+)\\- ([0-9]+)", "<@cross-ref-table-open>Table $1\\-$2<@cross-ref-table-close> "+(Constants.delimeters[index])+" <@cross-ref-table-open>$3\\-$4<@cross-ref-table-close>"); // simple dash
			line = line.replaceAll("(?i)Table ([0-9]+)\\— ([0-9]+) "+(Constants.delimeters[index])+" ([0-9]+)\\— ([0-9]+)", "<@cross-ref-table-open>Table $1\\—$2<@cross-ref-table-close> "+(Constants.delimeters[index])+" <@cross-ref-table-open>$3\\-$4<@cross-ref-table-close>"); // em-dash
			line = line.replaceAll("(?i)Table ([0-9]+)\\– ([0-9]+) "+(Constants.delimeters[index])+" ([0-9]+)\\– ([0-9]+)", "<@cross-ref-table-open>Table $1\\–$2<@cross-ref-table-close> "+(Constants.delimeters[index])+" <@cross-ref-table-open>$3\\-$4<@cross-ref-table-close>"); // en-dash
			line = line.replaceAll("(?i)Table ([0-9]+)\\. ([0-9]+) "+(Constants.delimeters[index])+" ([0-9]+)\\. ([0-9]+)", "<@cross-ref-table-open>Table $1\\.$2<@cross-ref-table-close> "+(Constants.delimeters[index])+" <@cross-ref-table-open>$3\\.$4<@cross-ref-table-close>"); // simple period
			
			
			line = line.replaceAll("(?i)Tables\\. ([0-9]+)\\- ([0-9]+) "+(Constants.delimeters[index])+" ([0-9]+)\\- ([0-9]+)", "<@cross-ref-table-open>Tables\\. $1\\-$2<@cross-ref-table-close> "+(Constants.delimeters[index])+" <@cross-ref-table-open>$3\\-$4<@cross-ref-table-close>"); // simple dash
			line = line.replaceAll("(?i)Tables\\. ([0-9]+)\\— ([0-9]+) "+(Constants.delimeters[index])+" ([0-9]+)\\— ([0-9]+)", "<@cross-ref-table-open>Tables\\. $1\\—$2<@cross-ref-table-close> "+(Constants.delimeters[index])+" <@cross-ref-table-open>$3\\-$4<@cross-ref-table-close>"); // em-dash
			line = line.replaceAll("(?i)Tables\\. ([0-9]+)\\– ([0-9]+) "+(Constants.delimeters[index])+" ([0-9]+)\\– ([0-9]+)", "<@cross-ref-table-open>Tables\\. $1\\–$2<@cross-ref-table-close> "+(Constants.delimeters[index])+" <@cross-ref-table-open>$3\\-$4<@cross-ref-table-close>"); // en-dash
			line = line.replaceAll("(?i)Tables\\. ([0-9]+)\\. ([0-9]+) "+(Constants.delimeters[index])+" ([0-9]+)\\. ([0-9]+)", "<@cross-ref-table-open>Tables\\. $1\\.$2<@cross-ref-table-close> "+(Constants.delimeters[index])+" <@cross-ref-table-open>$3\\.$4<@cross-ref-table-close>"); // simple period
			
			line = line.replaceAll("(?i)Tables ([0-9]+)\\- ([0-9]+) "+(Constants.delimeters[index])+" ([0-9]+)\\- ([0-9]+)", "<@cross-ref-table-open>Tables $1\\-$2<@cross-ref-table-close> "+(Constants.delimeters[index])+" <@cross-ref-table-open>$3\\-$4<@cross-ref-table-close>"); // simple dash
			line = line.replaceAll("(?i)Tables ([0-9]+)\\— ([0-9]+) "+(Constants.delimeters[index])+" ([0-9]+)\\— ([0-9]+)", "<@cross-ref-table-open>Tables $1\\—$2<@cross-ref-table-close> "+(Constants.delimeters[index])+" <@cross-ref-table-open>$3\\-$4<@cross-ref-table-close>"); // em-dash
			line = line.replaceAll("(?i)Tables ([0-9]+)\\– ([0-9]+) "+(Constants.delimeters[index])+" ([0-9]+)\\– ([0-9]+)", "<@cross-ref-table-open>Tables $1\\–$2<@cross-ref-table-close> "+(Constants.delimeters[index])+" <@cross-ref-table-open>$3\\-$4<@cross-ref-table-close>"); // en-dash
			line = line.replaceAll("(?i)Tables ([0-9]+)\\. ([0-9]+) "+(Constants.delimeters[index])+" ([0-9]+)\\. ([0-9]+)", "<@cross-ref-table-open>Tables $1\\.$2<@cross-ref-table-close> "+(Constants.delimeters[index])+" <@cross-ref-table-open>$3\\.$4<@cross-ref-table-close>"); // simple period
			
			
			/**
			 * with single space left side
			 * 
			 */
			line = line.replaceAll("(?i)Table\\. ([0-9]+)\\- ([0-9]+) "+(Constants.delimeters[index])+" ([0-9]+)\\-([0-9]+)", "<@cross-ref-table-open>Table\\. $1\\-$2<@cross-ref-table-close> "+(Constants.delimeters[index])+" <@cross-ref-table-open>$3\\-$4<@cross-ref-table-close>"); // simple dash
			line = line.replaceAll("(?i)Table\\. ([0-9]+)\\— ([0-9]+) "+(Constants.delimeters[index])+" ([0-9]+)\\—([0-9]+)", "<@cross-ref-table-open>Table\\. $1\\—$2<@cross-ref-table-close> "+(Constants.delimeters[index])+" <@cross-ref-table-open>$3\\-$4<@cross-ref-table-close>"); // em-dash
			line = line.replaceAll("(?i)Table\\. ([0-9]+)\\– ([0-9]+) "+(Constants.delimeters[index])+" ([0-9]+)\\–([0-9]+)", "<@cross-ref-table-open>Table\\. $1\\–$2<@cross-ref-table-close> "+(Constants.delimeters[index])+" <@cross-ref-table-open>$3\\-$4<@cross-ref-table-close>"); // en-dash
			line = line.replaceAll("(?i)Table\\. ([0-9]+)\\. ([0-9]+) "+(Constants.delimeters[index])+" ([0-9]+)\\.([0-9]+)", "<@cross-ref-table-open>Table\\. $1\\.$2<@cross-ref-table-close> "+(Constants.delimeters[index])+" <@cross-ref-table-open>$3\\.$4<@cross-ref-table-close>"); // simple period
			
			line = line.replaceAll("(?i)Table ([0-9]+)\\- ([0-9]+) "+(Constants.delimeters[index])+" ([0-9]+)\\-([0-9]+)", "<@cross-ref-table-open>Table $1\\-$2<@cross-ref-table-close> "+(Constants.delimeters[index])+" <@cross-ref-table-open>$3\\-$4<@cross-ref-table-close>"); // simple dash
			line = line.replaceAll("(?i)Table ([0-9]+)\\— ([0-9]+) "+(Constants.delimeters[index])+" ([0-9]+)\\—([0-9]+)", "<@cross-ref-table-open>Table $1\\—$2<@cross-ref-table-close> "+(Constants.delimeters[index])+" <@cross-ref-table-open>$3\\-$4<@cross-ref-table-close>"); // em-dash
			line = line.replaceAll("(?i)Table ([0-9]+)\\– ([0-9]+) "+(Constants.delimeters[index])+" ([0-9]+)\\–([0-9]+)", "<@cross-ref-table-open>Table $1\\–$2<@cross-ref-table-close> "+(Constants.delimeters[index])+" <@cross-ref-table-open>$3\\-$4<@cross-ref-table-close>"); // en-dash
			line = line.replaceAll("(?i)Table ([0-9]+)\\. ([0-9]+) "+(Constants.delimeters[index])+" ([0-9]+)\\.([0-9]+)", "<@cross-ref-table-open>Table $1\\.$2<@cross-ref-table-close> "+(Constants.delimeters[index])+" <@cross-ref-table-open>$3\\.$4<@cross-ref-table-close>"); // simple period
			
			
			line = line.replaceAll("(?i)Tables\\. ([0-9]+)\\- ([0-9]+) "+(Constants.delimeters[index])+" ([0-9]+)\\-([0-9]+)", "<@cross-ref-table-open>Tables\\. $1\\-$2<@cross-ref-table-close> "+(Constants.delimeters[index])+" <@cross-ref-table-open>$3\\-$4<@cross-ref-table-close>"); // simple dash
			line = line.replaceAll("(?i)Tables\\. ([0-9]+)\\— ([0-9]+) "+(Constants.delimeters[index])+" ([0-9]+)\\—([0-9]+)", "<@cross-ref-table-open>Tables\\. $1\\—$2<@cross-ref-table-close> "+(Constants.delimeters[index])+" <@cross-ref-table-open>$3\\-$4<@cross-ref-table-close>"); // em-dash
			line = line.replaceAll("(?i)Tables\\. ([0-9]+)\\– ([0-9]+) "+(Constants.delimeters[index])+" ([0-9]+)\\–([0-9]+)", "<@cross-ref-table-open>Tables\\. $1\\–$2<@cross-ref-table-close> "+(Constants.delimeters[index])+" <@cross-ref-table-open>$3\\-$4<@cross-ref-table-close>"); // en-dash
			line = line.replaceAll("(?i)Tables\\. ([0-9]+)\\. ([0-9]+) "+(Constants.delimeters[index])+" ([0-9]+)\\.([0-9]+)", "<@cross-ref-table-open>Tables\\. $1\\.$2<@cross-ref-table-close> "+(Constants.delimeters[index])+" <@cross-ref-table-open>$3\\.$4<@cross-ref-table-close>"); // simple period
			
			line = line.replaceAll("(?i)Tables ([0-9]+)\\- ([0-9]+) "+(Constants.delimeters[index])+" ([0-9]+)\\-([0-9]+)", "<@cross-ref-table-open>Tables $1\\-$2<@cross-ref-table-close> "+(Constants.delimeters[index])+" <@cross-ref-table-open>$3\\-$4<@cross-ref-table-close>"); // simple dash
			line = line.replaceAll("(?i)Tables ([0-9]+)\\— ([0-9]+) "+(Constants.delimeters[index])+" ([0-9]+)\\—([0-9]+)", "<@cross-ref-table-open>Tables $1\\—$2<@cross-ref-table-close> "+(Constants.delimeters[index])+" <@cross-ref-table-open>$3\\-$4<@cross-ref-table-close>"); // em-dash
			line = line.replaceAll("(?i)Tables ([0-9]+)\\– ([0-9]+) "+(Constants.delimeters[index])+" ([0-9]+)\\–([0-9]+)", "<@cross-ref-table-open>Tables $1\\–$2<@cross-ref-table-close> "+(Constants.delimeters[index])+" <@cross-ref-table-open>$3\\-$4<@cross-ref-table-close>"); // en-dash
			line = line.replaceAll("(?i)Tables ([0-9]+)\\. ([0-9]+) "+(Constants.delimeters[index])+" ([0-9]+)\\.([0-9]+)", "<@cross-ref-table-open>Tables $1\\.$2<@cross-ref-table-close> "+(Constants.delimeters[index])+" <@cross-ref-table-open>$3\\.$4<@cross-ref-table-close>"); // simple period
			
			/**
			 * with single space right side
			 * 
			 */
			line = line.replaceAll("(?i)Table\\. ([0-9]+)\\-([0-9]+) "+(Constants.delimeters[index])+" ([0-9]+)\\- ([0-9]+)", "<@cross-ref-table-open>Table\\. $1\\-$2<@cross-ref-table-close> "+(Constants.delimeters[index])+" <@cross-ref-table-open>$3\\-$4<@cross-ref-table-close>"); // simple dash
			line = line.replaceAll("(?i)Table\\. ([0-9]+)\\—([0-9]+) "+(Constants.delimeters[index])+" ([0-9]+)\\— ([0-9]+)", "<@cross-ref-table-open>Table\\. $1\\—$2<@cross-ref-table-close> "+(Constants.delimeters[index])+" <@cross-ref-table-open>$3\\-$4<@cross-ref-table-close>"); // em-dash
			line = line.replaceAll("(?i)Table\\. ([0-9]+)\\–([0-9]+) "+(Constants.delimeters[index])+" ([0-9]+)\\– ([0-9]+)", "<@cross-ref-table-open>Table\\. $1\\–$2<@cross-ref-table-close> "+(Constants.delimeters[index])+" <@cross-ref-table-open>$3\\-$4<@cross-ref-table-close>"); // en-dash
			line = line.replaceAll("(?i)Table\\. ([0-9]+)\\.([0-9]+) "+(Constants.delimeters[index])+" ([0-9]+)\\. ([0-9]+)", "<@cross-ref-table-open>Table\\. $1\\.$2<@cross-ref-table-close> "+(Constants.delimeters[index])+" <@cross-ref-table-open>$3\\.$4<@cross-ref-table-close>"); // simple period
			
			line = line.replaceAll("(?i)Table ([0-9]+)\\-([0-9]+) "+(Constants.delimeters[index])+" ([0-9]+)\\- ([0-9]+)", "<@cross-ref-table-open>Table $1\\-$2<@cross-ref-table-close> "+(Constants.delimeters[index])+" <@cross-ref-table-open>$3\\-$4<@cross-ref-table-close>"); // simple dash
			line = line.replaceAll("(?i)Table ([0-9]+)\\—([0-9]+) "+(Constants.delimeters[index])+" ([0-9]+)\\— ([0-9]+)", "<@cross-ref-table-open>Table $1\\—$2<@cross-ref-table-close> "+(Constants.delimeters[index])+" <@cross-ref-table-open>$3\\-$4<@cross-ref-table-close>"); // em-dash
			line = line.replaceAll("(?i)Table ([0-9]+)\\–([0-9]+) "+(Constants.delimeters[index])+" ([0-9]+)\\– ([0-9]+)", "<@cross-ref-table-open>Table $1\\–$2<@cross-ref-table-close> "+(Constants.delimeters[index])+" <@cross-ref-table-open>$3\\-$4<@cross-ref-table-close>"); // en-dash
			line = line.replaceAll("(?i)Table ([0-9]+)\\.([0-9]+) "+(Constants.delimeters[index])+" ([0-9]+)\\. ([0-9]+)", "<@cross-ref-table-open>Table $1\\.$2<@cross-ref-table-close> "+(Constants.delimeters[index])+" <@cross-ref-table-open>$3\\.$4<@cross-ref-table-close>"); // simple period
			
			
			line = line.replaceAll("(?i)Tables\\. ([0-9]+)\\-([0-9]+) "+(Constants.delimeters[index])+" ([0-9]+)\\- ([0-9]+)", "<@cross-ref-table-open>Tables\\. $1\\-$2<@cross-ref-table-close> "+(Constants.delimeters[index])+" <@cross-ref-table-open>$3\\-$4<@cross-ref-table-close>"); // simple dash
			line = line.replaceAll("(?i)Tables\\. ([0-9]+)\\—([0-9]+) "+(Constants.delimeters[index])+" ([0-9]+)\\— ([0-9]+)", "<@cross-ref-table-open>Tables\\. $1\\—$2<@cross-ref-table-close> "+(Constants.delimeters[index])+" <@cross-ref-table-open>$3\\-$4<@cross-ref-table-close>"); // em-dash
			line = line.replaceAll("(?i)Tables\\. ([0-9]+)\\–([0-9]+) "+(Constants.delimeters[index])+" ([0-9]+)\\– ([0-9]+)", "<@cross-ref-table-open>Tables\\. $1\\–$2<@cross-ref-table-close> "+(Constants.delimeters[index])+" <@cross-ref-table-open>$3\\-$4<@cross-ref-table-close>"); // en-dash
			line = line.replaceAll("(?i)Tables\\. ([0-9]+)\\.([0-9]+) "+(Constants.delimeters[index])+" ([0-9]+)\\. ([0-9]+)", "<@cross-ref-table-open>Tables\\. $1\\.$2<@cross-ref-table-close> "+(Constants.delimeters[index])+" <@cross-ref-table-open>$3\\.$4<@cross-ref-table-close>"); // simple period
			
			line = line.replaceAll("(?i)Tables ([0-9]+)\\-([0-9]+) "+(Constants.delimeters[index])+" ([0-9]+)\\- ([0-9]+)", "<@cross-ref-table-open>Tables $1\\-$2<@cross-ref-table-close> "+(Constants.delimeters[index])+" <@cross-ref-table-open>$3\\-$4<@cross-ref-table-close>"); // simple dash
			line = line.replaceAll("(?i)Tables ([0-9]+)\\—([0-9]+) "+(Constants.delimeters[index])+" ([0-9]+)\\— ([0-9]+)", "<@cross-ref-table-open>Tables $1\\—$2<@cross-ref-table-close> "+(Constants.delimeters[index])+" <@cross-ref-table-open>$3\\-$4<@cross-ref-table-close>"); // em-dash
			line = line.replaceAll("(?i)Tables ([0-9]+)\\–([0-9]+) "+(Constants.delimeters[index])+" ([0-9]+)\\– ([0-9]+)", "<@cross-ref-table-open>Tables $1\\–$2<@cross-ref-table-close> "+(Constants.delimeters[index])+" <@cross-ref-table-open>$3\\-$4<@cross-ref-table-close>"); // en-dash
			line = line.replaceAll("(?i)Tables ([0-9]+)\\.([0-9]+) "+(Constants.delimeters[index])+" ([0-9]+)\\. ([0-9]+)", "<@cross-ref-table-open>Tables $1\\.$2<@cross-ref-table-close> "+(Constants.delimeters[index])+" <@cross-ref-table-open>$3\\.$4<@cross-ref-table-close>"); // simple period
			
			/**
			 * 
			 * ELECTRONIC
			 */
			
			line = line.replaceAll("(?i)Table\\. E([0-9]+)\\-([0-9]+) "+(Constants.delimeters[index])+" E([0-9]+)\\-([0-9]+)", "<@cross-ref-table-open>Table\\. $1\\-$2<@cross-ref-table-close> "+(Constants.delimeters[index])+" <@cross-ref-table-open>$3\\-$4<@cross-ref-table-close>"); // simple dash
			line = line.replaceAll("(?i)Table\\. E([0-9]+)\\—([0-9]+) "+(Constants.delimeters[index])+" E([0-9]+)\\—([0-9]+)", "<@cross-ref-table-open>Table\\. $1\\—$2<@cross-ref-table-close> "+(Constants.delimeters[index])+" <@cross-ref-table-open>$3\\-$4<@cross-ref-table-close>"); // em-dash
			line = line.replaceAll("(?i)Table\\. E([0-9]+)\\–([0-9]+) "+(Constants.delimeters[index])+" E([0-9]+)\\–([0-9]+)", "<@cross-ref-table-open>Table\\. $1\\–$2<@cross-ref-table-close> "+(Constants.delimeters[index])+" <@cross-ref-table-open>$3\\-$4<@cross-ref-table-close>"); // en-dash
			line = line.replaceAll("(?i)Table\\. E([0-9]+)\\.([0-9]+) "+(Constants.delimeters[index])+" E([0-9]+)\\.([0-9]+)", "<@cross-ref-table-open>Table\\. $1\\.$2<@cross-ref-table-close> "+(Constants.delimeters[index])+" <@cross-ref-table-open>$3\\.$4<@cross-ref-table-close>"); // simple period
			
			line = line.replaceAll("(?i)Table E([0-9]+)\\-([0-9]+) "+(Constants.delimeters[index])+" E([0-9]+)\\-([0-9]+)", "<@cross-ref-table-open>Table $1\\-$2<@cross-ref-table-close> "+(Constants.delimeters[index])+" <@cross-ref-table-open>$3\\-$4<@cross-ref-table-close>"); // simple dash
			line = line.replaceAll("(?i)Table E([0-9]+)\\—([0-9]+) "+(Constants.delimeters[index])+" E([0-9]+)\\—([0-9]+)", "<@cross-ref-table-open>Table $1\\—$2<@cross-ref-table-close> "+(Constants.delimeters[index])+" <@cross-ref-table-open>$3\\-$4<@cross-ref-table-close>"); // em-dash
			line = line.replaceAll("(?i)Table E([0-9]+)\\–([0-9]+) "+(Constants.delimeters[index])+" E([0-9]+)\\–([0-9]+)", "<@cross-ref-table-open>Table $1\\–$2<@cross-ref-table-close> "+(Constants.delimeters[index])+" <@cross-ref-table-open>$3\\-$4<@cross-ref-table-close>"); // en-dash
			line = line.replaceAll("(?i)Table E([0-9]+)\\.([0-9]+) "+(Constants.delimeters[index])+" E([0-9]+)\\.([0-9]+)", "<@cross-ref-table-open>Table $1\\.$2<@cross-ref-table-close> "+(Constants.delimeters[index])+" <@cross-ref-table-open>$3\\.$4<@cross-ref-table-close>"); // simple period
			
			
			line = line.replaceAll("(?i)Tables\\. E([0-9]+)\\-([0-9]+) "+(Constants.delimeters[index])+" E([0-9]+)\\-([0-9]+)", "<@cross-ref-table-open>Tables\\. $1\\-$2<@cross-ref-table-close> "+(Constants.delimeters[index])+" <@cross-ref-table-open>$3\\-$4<@cross-ref-table-close>"); // simple dash
			line = line.replaceAll("(?i)Tables\\. E([0-9]+)\\—([0-9]+) "+(Constants.delimeters[index])+" E([0-9]+)\\—([0-9]+)", "<@cross-ref-table-open>Tables\\. $1\\—$2<@cross-ref-table-close> "+(Constants.delimeters[index])+" <@cross-ref-table-open>$3\\-$4<@cross-ref-table-close>"); // em-dash
			line = line.replaceAll("(?i)Tables\\. E([0-9]+)\\–([0-9]+) "+(Constants.delimeters[index])+" E([0-9]+)\\–([0-9]+)", "<@cross-ref-table-open>Tables\\. $1\\–$2<@cross-ref-table-close> "+(Constants.delimeters[index])+" <@cross-ref-table-open>$3\\-$4<@cross-ref-table-close>"); // en-dash
			line = line.replaceAll("(?i)Tables\\. E([0-9]+)\\.([0-9]+) "+(Constants.delimeters[index])+" E([0-9]+)\\.([0-9]+)", "<@cross-ref-table-open>Tables\\. $1\\.$2<@cross-ref-table-close> "+(Constants.delimeters[index])+" <@cross-ref-table-open>$3\\.$4<@cross-ref-table-close>"); // simple period
			
			line = line.replaceAll("(?i)Tables E([0-9]+)\\-([0-9]+) "+(Constants.delimeters[index])+" E([0-9]+)\\-([0-9]+)", "<@cross-ref-table-open>Tables $1\\-$2<@cross-ref-table-close> "+(Constants.delimeters[index])+" <@cross-ref-table-open>$3\\-$4<@cross-ref-table-close>"); // simple dash
			line = line.replaceAll("(?i)Tables E([0-9]+)\\—([0-9]+) "+(Constants.delimeters[index])+" E([0-9]+)\\—([0-9]+)", "<@cross-ref-table-open>Tables $1\\—$2<@cross-ref-table-close> "+(Constants.delimeters[index])+" <@cross-ref-table-open>$3\\-$4<@cross-ref-table-close>"); // em-dash
			line = line.replaceAll("(?i)Tables E([0-9]+)\\–([0-9]+) "+(Constants.delimeters[index])+" E([0-9]+)\\–([0-9]+)", "<@cross-ref-table-open>Tables $1\\–$2<@cross-ref-table-close> "+(Constants.delimeters[index])+" <@cross-ref-table-open>$3\\-$4<@cross-ref-table-close>"); // en-dash
			line = line.replaceAll("(?i)Tables E([0-9]+)\\.([0-9]+) "+(Constants.delimeters[index])+" E([0-9]+)\\.([0-9]+)", "<@cross-ref-table-open>Tables $1\\.$2<@cross-ref-table-close> "+(Constants.delimeters[index])+" <@cross-ref-table-open>$3\\.$4<@cross-ref-table-close>"); // simple period
			
			
			/**
			 * ELECTRONIC with single space both side
			 * 
			 */
			line = line.replaceAll("(?i)Table\\. E([0-9]+)\\- ([0-9]+) "+(Constants.delimeters[index])+" E([0-9]+)\\- ([0-9]+)", "<@cross-ref-table-open>Table\\. $1\\-$2<@cross-ref-table-close> "+(Constants.delimeters[index])+" <@cross-ref-table-open>$3\\-$4<@cross-ref-table-close>"); // simple dash
			line = line.replaceAll("(?i)Table\\. E([0-9]+)\\— ([0-9]+) "+(Constants.delimeters[index])+" E([0-9]+)\\— ([0-9]+)", "<@cross-ref-table-open>Table\\. $1\\—$2<@cross-ref-table-close> "+(Constants.delimeters[index])+" <@cross-ref-table-open>$3\\-$4<@cross-ref-table-close>"); // em-dash
			line = line.replaceAll("(?i)Table\\. E([0-9]+)\\– ([0-9]+) "+(Constants.delimeters[index])+" E([0-9]+)\\– ([0-9]+)", "<@cross-ref-table-open>Table\\. $1\\–$2<@cross-ref-table-close> "+(Constants.delimeters[index])+" <@cross-ref-table-open>$3\\-$4<@cross-ref-table-close>"); // en-dash
			line = line.replaceAll("(?i)Table\\. E([0-9]+)\\. ([0-9]+) "+(Constants.delimeters[index])+" E([0-9]+)\\. ([0-9]+)", "<@cross-ref-table-open>Table\\. $1\\.$2<@cross-ref-table-close> "+(Constants.delimeters[index])+" <@cross-ref-table-open>$3\\.$4<@cross-ref-table-close>"); // simple period
			
			line = line.replaceAll("(?i)Table E([0-9]+)\\- ([0-9]+) "+(Constants.delimeters[index])+" E([0-9]+)\\- ([0-9]+)", "<@cross-ref-table-open>Table $1\\-$2<@cross-ref-table-close> "+(Constants.delimeters[index])+" <@cross-ref-table-open>$3\\-$4<@cross-ref-table-close>"); // simple dash
			line = line.replaceAll("(?i)Table E([0-9]+)\\— ([0-9]+) "+(Constants.delimeters[index])+" E([0-9]+)\\— ([0-9]+)", "<@cross-ref-table-open>Table $1\\—$2<@cross-ref-table-close> "+(Constants.delimeters[index])+" <@cross-ref-table-open>$3\\-$4<@cross-ref-table-close>"); // em-dash
			line = line.replaceAll("(?i)Table E([0-9]+)\\– ([0-9]+) "+(Constants.delimeters[index])+" E([0-9]+)\\– ([0-9]+)", "<@cross-ref-table-open>Table $1\\–$2<@cross-ref-table-close> "+(Constants.delimeters[index])+" <@cross-ref-table-open>$3\\-$4<@cross-ref-table-close>"); // en-dash
			line = line.replaceAll("(?i)Table E([0-9]+)\\. ([0-9]+) "+(Constants.delimeters[index])+" E([0-9]+)\\. ([0-9]+)", "<@cross-ref-table-open>Table $1\\.$2<@cross-ref-table-close> "+(Constants.delimeters[index])+" <@cross-ref-table-open>$3\\.$4<@cross-ref-table-close>"); // simple period
			
			
			line = line.replaceAll("(?i)Tables\\. E([0-9]+)\\- ([0-9]+) "+(Constants.delimeters[index])+" E([0-9]+)\\- ([0-9]+)", "<@cross-ref-table-open>Tables\\. $1\\-$2<@cross-ref-table-close> "+(Constants.delimeters[index])+" <@cross-ref-table-open>$3\\-$4<@cross-ref-table-close>"); // simple dash
			line = line.replaceAll("(?i)Tables\\. E([0-9]+)\\— ([0-9]+) "+(Constants.delimeters[index])+" E([0-9]+)\\— ([0-9]+)", "<@cross-ref-table-open>Tables\\. $1\\—$2<@cross-ref-table-close> "+(Constants.delimeters[index])+" <@cross-ref-table-open>$3\\-$4<@cross-ref-table-close>"); // em-dash
			line = line.replaceAll("(?i)Tables\\. E([0-9]+)\\– ([0-9]+) "+(Constants.delimeters[index])+" E([0-9]+)\\– ([0-9]+)", "<@cross-ref-table-open>Tables\\. $1\\–$2<@cross-ref-table-close> "+(Constants.delimeters[index])+" <@cross-ref-table-open>$3\\-$4<@cross-ref-table-close>"); // en-dash
			line = line.replaceAll("(?i)Tables\\. E([0-9]+)\\. ([0-9]+) "+(Constants.delimeters[index])+" E([0-9]+)\\. ([0-9]+)", "<@cross-ref-table-open>Tables\\. $1\\.$2<@cross-ref-table-close> "+(Constants.delimeters[index])+" <@cross-ref-table-open>$3\\.$4<@cross-ref-table-close>"); // simple period
			
			line = line.replaceAll("(?i)Tables E([0-9]+)\\- ([0-9]+) "+(Constants.delimeters[index])+" E([0-9]+)\\- ([0-9]+)", "<@cross-ref-table-open>Tables $1\\-$2<@cross-ref-table-close> "+(Constants.delimeters[index])+" <@cross-ref-table-open>$3\\-$4<@cross-ref-table-close>"); // simple dash
			line = line.replaceAll("(?i)Tables E([0-9]+)\\— ([0-9]+) "+(Constants.delimeters[index])+" E([0-9]+)\\— ([0-9]+)", "<@cross-ref-table-open>Tables $1\\—$2<@cross-ref-table-close> "+(Constants.delimeters[index])+" <@cross-ref-table-open>$3\\-$4<@cross-ref-table-close>"); // em-dash
			line = line.replaceAll("(?i)Tables E([0-9]+)\\– ([0-9]+) "+(Constants.delimeters[index])+" E([0-9]+)\\– ([0-9]+)", "<@cross-ref-table-open>Tables $1\\–$2<@cross-ref-table-close> "+(Constants.delimeters[index])+" <@cross-ref-table-open>$3\\-$4<@cross-ref-table-close>"); // en-dash
			line = line.replaceAll("(?i)Tables E([0-9]+)\\. ([0-9]+) "+(Constants.delimeters[index])+" E([0-9]+)\\. ([0-9]+)", "<@cross-ref-table-open>Tables $1\\.$2<@cross-ref-table-close> "+(Constants.delimeters[index])+" <@cross-ref-table-open>$3\\.$4<@cross-ref-table-close>"); // simple period
			
			
			/**
			 * ELECTRONIC with single space left side
			 * 
			 */
			line = line.replaceAll("(?i)Table\\. E([0-9]+)\\- ([0-9]+) "+(Constants.delimeters[index])+" E([0-9]+)\\-([0-9]+)", "<@cross-ref-table-open>Table\\. $1\\-$2<@cross-ref-table-close> "+(Constants.delimeters[index])+" <@cross-ref-table-open>$3\\-$4<@cross-ref-table-close>"); // simple dash
			line = line.replaceAll("(?i)Table\\. E([0-9]+)\\— ([0-9]+) "+(Constants.delimeters[index])+" E([0-9]+)\\—([0-9]+)", "<@cross-ref-table-open>Table\\. $1\\—$2<@cross-ref-table-close> "+(Constants.delimeters[index])+" <@cross-ref-table-open>$3\\-$4<@cross-ref-table-close>"); // em-dash
			line = line.replaceAll("(?i)Table\\. E([0-9]+)\\– ([0-9]+) "+(Constants.delimeters[index])+" E([0-9]+)\\–([0-9]+)", "<@cross-ref-table-open>Table\\. $1\\–$2<@cross-ref-table-close> "+(Constants.delimeters[index])+" <@cross-ref-table-open>$3\\-$4<@cross-ref-table-close>"); // en-dash
			line = line.replaceAll("(?i)Table\\. E([0-9]+)\\. ([0-9]+) "+(Constants.delimeters[index])+" E([0-9]+)\\.([0-9]+)", "<@cross-ref-table-open>Table\\. $1\\.$2<@cross-ref-table-close> "+(Constants.delimeters[index])+" <@cross-ref-table-open>$3\\.$4<@cross-ref-table-close>"); // simple period
			
			line = line.replaceAll("(?i)Table E([0-9]+)\\- ([0-9]+) "+(Constants.delimeters[index])+" E([0-9]+)\\-([0-9]+)", "<@cross-ref-table-open>Table $1\\-$2<@cross-ref-table-close> "+(Constants.delimeters[index])+" <@cross-ref-table-open>$3\\-$4<@cross-ref-table-close>"); // simple dash
			line = line.replaceAll("(?i)Table E([0-9]+)\\— ([0-9]+) "+(Constants.delimeters[index])+" E([0-9]+)\\—([0-9]+)", "<@cross-ref-table-open>Table $1\\—$2<@cross-ref-table-close> "+(Constants.delimeters[index])+" <@cross-ref-table-open>$3\\-$4<@cross-ref-table-close>"); // em-dash
			line = line.replaceAll("(?i)Table E([0-9]+)\\– ([0-9]+) "+(Constants.delimeters[index])+" E([0-9]+)\\–([0-9]+)", "<@cross-ref-table-open>Table $1\\–$2<@cross-ref-table-close> "+(Constants.delimeters[index])+" <@cross-ref-table-open>$3\\-$4<@cross-ref-table-close>"); // en-dash
			line = line.replaceAll("(?i)Table E([0-9]+)\\. ([0-9]+) "+(Constants.delimeters[index])+" E([0-9]+)\\.([0-9]+)", "<@cross-ref-table-open>Table $1\\.$2<@cross-ref-table-close> "+(Constants.delimeters[index])+" <@cross-ref-table-open>$3\\.$4<@cross-ref-table-close>"); // simple period
			
			
			line = line.replaceAll("(?i)Tables\\. E([0-9]+)\\- ([0-9]+) "+(Constants.delimeters[index])+" E([0-9]+)\\-([0-9]+)", "<@cross-ref-table-open>Tables\\. $1\\-$2<@cross-ref-table-close> "+(Constants.delimeters[index])+" <@cross-ref-table-open>$3\\-$4<@cross-ref-table-close>"); // simple dash
			line = line.replaceAll("(?i)Tables\\. E([0-9]+)\\— ([0-9]+) "+(Constants.delimeters[index])+" E([0-9]+)\\—([0-9]+)", "<@cross-ref-table-open>Tables\\. $1\\—$2<@cross-ref-table-close> "+(Constants.delimeters[index])+" <@cross-ref-table-open>$3\\-$4<@cross-ref-table-close>"); // em-dash
			line = line.replaceAll("(?i)Tables\\. E([0-9]+)\\– ([0-9]+) "+(Constants.delimeters[index])+" E([0-9]+)\\–([0-9]+)", "<@cross-ref-table-open>Tables\\. $1\\–$2<@cross-ref-table-close> "+(Constants.delimeters[index])+" <@cross-ref-table-open>$3\\-$4<@cross-ref-table-close>"); // en-dash
			line = line.replaceAll("(?i)Tables\\. E([0-9]+)\\. ([0-9]+) "+(Constants.delimeters[index])+" E([0-9]+)\\.([0-9]+)", "<@cross-ref-table-open>Tables\\. $1\\.$2<@cross-ref-table-close> "+(Constants.delimeters[index])+" <@cross-ref-table-open>$3\\.$4<@cross-ref-table-close>"); // simple period
			
			line = line.replaceAll("(?i)Tables E([0-9]+)\\- ([0-9]+) "+(Constants.delimeters[index])+" E([0-9]+)\\-([0-9]+)", "<@cross-ref-table-open>Tables $1\\-$2<@cross-ref-table-close> "+(Constants.delimeters[index])+" <@cross-ref-table-open>$3\\-$4<@cross-ref-table-close>"); // simple dash
			line = line.replaceAll("(?i)Tables E([0-9]+)\\— ([0-9]+) "+(Constants.delimeters[index])+" E([0-9]+)\\—([0-9]+)", "<@cross-ref-table-open>Tables $1\\—$2<@cross-ref-table-close> "+(Constants.delimeters[index])+" <@cross-ref-table-open>$3\\-$4<@cross-ref-table-close>"); // em-dash
			line = line.replaceAll("(?i)Tables E([0-9]+)\\– ([0-9]+) "+(Constants.delimeters[index])+" E([0-9]+)\\–([0-9]+)", "<@cross-ref-table-open>Tables $1\\–$2<@cross-ref-table-close> "+(Constants.delimeters[index])+" <@cross-ref-table-open>$3\\-$4<@cross-ref-table-close>"); // en-dash
			line = line.replaceAll("(?i)Tables E([0-9]+)\\. ([0-9]+) "+(Constants.delimeters[index])+" E([0-9]+)\\.([0-9]+)", "<@cross-ref-table-open>Tables $1\\.$2<@cross-ref-table-close> "+(Constants.delimeters[index])+" <@cross-ref-table-open>$3\\.$4<@cross-ref-table-close>"); // simple period
			
			/**
			 * ELECTRONIC with single space right side
			 * 
			 */
			line = line.replaceAll("(?i)Table\\. E([0-9]+)\\-([0-9]+) "+(Constants.delimeters[index])+" E([0-9]+)\\- ([0-9]+)", "<@cross-ref-table-open>Table\\. $1\\-$2<@cross-ref-table-close> "+(Constants.delimeters[index])+" <@cross-ref-table-open>$3\\-$4<@cross-ref-table-close>"); // simple dash
			line = line.replaceAll("(?i)Table\\. E([0-9]+)\\—([0-9]+) "+(Constants.delimeters[index])+" E([0-9]+)\\— ([0-9]+)", "<@cross-ref-table-open>Table\\. $1\\—$2<@cross-ref-table-close> "+(Constants.delimeters[index])+" <@cross-ref-table-open>$3\\-$4<@cross-ref-table-close>"); // em-dash
			line = line.replaceAll("(?i)Table\\. E([0-9]+)\\–([0-9]+) "+(Constants.delimeters[index])+" E([0-9]+)\\– ([0-9]+)", "<@cross-ref-table-open>Table\\. $1\\–$2<@cross-ref-table-close> "+(Constants.delimeters[index])+" <@cross-ref-table-open>$3\\-$4<@cross-ref-table-close>"); // en-dash
			line = line.replaceAll("(?i)Table\\. E([0-9]+)\\.([0-9]+) "+(Constants.delimeters[index])+" E([0-9]+)\\. ([0-9]+)", "<@cross-ref-table-open>Table\\. $1\\.$2<@cross-ref-table-close> "+(Constants.delimeters[index])+" <@cross-ref-table-open>$3\\.$4<@cross-ref-table-close>"); // simple period
			
			line = line.replaceAll("(?i)Table E([0-9]+)\\-([0-9]+) "+(Constants.delimeters[index])+" E([0-9]+)\\- ([0-9]+)", "<@cross-ref-table-open>Table $1\\-$2<@cross-ref-table-close> "+(Constants.delimeters[index])+" <@cross-ref-table-open>$3\\-$4<@cross-ref-table-close>"); // simple dash
			line = line.replaceAll("(?i)Table E([0-9]+)\\—([0-9]+) "+(Constants.delimeters[index])+" E([0-9]+)\\— ([0-9]+)", "<@cross-ref-table-open>Table $1\\—$2<@cross-ref-table-close> "+(Constants.delimeters[index])+" <@cross-ref-table-open>$3\\-$4<@cross-ref-table-close>"); // em-dash
			line = line.replaceAll("(?i)Table E([0-9]+)\\–([0-9]+) "+(Constants.delimeters[index])+" E([0-9]+)\\– ([0-9]+)", "<@cross-ref-table-open>Table $1\\–$2<@cross-ref-table-close> "+(Constants.delimeters[index])+" <@cross-ref-table-open>$3\\-$4<@cross-ref-table-close>"); // en-dash
			line = line.replaceAll("(?i)Table E([0-9]+)\\.([0-9]+) "+(Constants.delimeters[index])+" E([0-9]+)\\. ([0-9]+)", "<@cross-ref-table-open>Table $1\\.$2<@cross-ref-table-close> "+(Constants.delimeters[index])+" <@cross-ref-table-open>$3\\.$4<@cross-ref-table-close>"); // simple period
			
			
			line = line.replaceAll("(?i)Tables\\. E([0-9]+)\\-([0-9]+) "+(Constants.delimeters[index])+" E([0-9]+)\\- ([0-9]+)", "<@cross-ref-table-open>Tables\\. $1\\-$2<@cross-ref-table-close> "+(Constants.delimeters[index])+" <@cross-ref-table-open>$3\\-$4<@cross-ref-table-close>"); // simple dash
			line = line.replaceAll("(?i)Tables\\. E([0-9]+)\\—([0-9]+) "+(Constants.delimeters[index])+" E([0-9]+)\\— ([0-9]+)", "<@cross-ref-table-open>Tables\\. $1\\—$2<@cross-ref-table-close> "+(Constants.delimeters[index])+" <@cross-ref-table-open>$3\\-$4<@cross-ref-table-close>"); // em-dash
			line = line.replaceAll("(?i)Tables\\. E([0-9]+)\\–([0-9]+) "+(Constants.delimeters[index])+" E([0-9]+)\\– ([0-9]+)", "<@cross-ref-table-open>Tables\\. $1\\–$2<@cross-ref-table-close> "+(Constants.delimeters[index])+" <@cross-ref-table-open>$3\\-$4<@cross-ref-table-close>"); // en-dash
			line = line.replaceAll("(?i)Tables\\. E([0-9]+)\\.([0-9]+) "+(Constants.delimeters[index])+" E([0-9]+)\\. ([0-9]+)", "<@cross-ref-table-open>Tables\\. $1\\.$2<@cross-ref-table-close> "+(Constants.delimeters[index])+" <@cross-ref-table-open>$3\\.$4<@cross-ref-table-close>"); // simple period
			
			line = line.replaceAll("(?i)Tables E([0-9]+)\\-([0-9]+) "+(Constants.delimeters[index])+" E([0-9]+)\\- ([0-9]+)", "<@cross-ref-table-open>Tables $1\\-$2<@cross-ref-table-close> "+(Constants.delimeters[index])+" <@cross-ref-table-open>$3\\-$4<@cross-ref-table-close>"); // simple dash
			line = line.replaceAll("(?i)Tables E([0-9]+)\\—([0-9]+) "+(Constants.delimeters[index])+" E([0-9]+)\\— ([0-9]+)", "<@cross-ref-table-open>Tables $1\\—$2<@cross-ref-table-close> "+(Constants.delimeters[index])+" <@cross-ref-table-open>$3\\-$4<@cross-ref-table-close>"); // em-dash
			line = line.replaceAll("(?i)Tables E([0-9]+)\\–([0-9]+) "+(Constants.delimeters[index])+" E([0-9]+)\\– ([0-9]+)", "<@cross-ref-table-open>Tables $1\\–$2<@cross-ref-table-close> "+(Constants.delimeters[index])+" <@cross-ref-table-open>$3\\-$4<@cross-ref-table-close>"); // en-dash
			line = line.replaceAll("(?i)Tables E([0-9]+)\\.([0-9]+) "+(Constants.delimeters[index])+" E([0-9]+)\\. ([0-9]+)", "<@cross-ref-table-open>Tables $1\\.$2<@cross-ref-table-close> "+(Constants.delimeters[index])+" <@cross-ref-table-open>$3\\.$4<@cross-ref-table-close>"); // simple period
			
			
			
			if (line.contains("<@cross-ref-table-open>"))	isFound = true;
			if ( ! isFound ) {
				
				line = line.replaceAll("(?i)Table\\. ([0-9]+) "+(Constants.delimeters[index])+" ([0-9]+)", "<@cross-ref-table-open>Table\\. $1<@cross-ref-table-close> "+(Constants.delimeters[index])+" <@cross-ref-table-open>$2<@cross-ref-table-close>"); // simple dash
				line = line.replaceAll("(?i)Table ([0-9]+) "+(Constants.delimeters[index])+" ([0-9]+)", "<@cross-ref-table-open>Table $1<@cross-ref-table-close> "+(Constants.delimeters[index])+" <@cross-ref-table-open>$2<@cross-ref-table-close>"); // simple dash
				line = line.replaceAll("(?i)Tables\\. ([0-9]+) "+(Constants.delimeters[index])+" ([0-9]+)", "<@cross-ref-table-open>Tables\\. $1<@cross-ref-table-close> "+(Constants.delimeters[index])+" <@cross-ref-table-open>$2<@cross-ref-table-close>"); // simple dash
				line = line.replaceAll("(?i)Tables ([0-9]+) "+(Constants.delimeters[index])+" ([0-9]+)", "<@cross-ref-table-open>Tables $1<@cross-ref-table-close> "+(Constants.delimeters[index])+" <@cross-ref-table-open>$2<@cross-ref-table-close>"); // simple dash
				
				
				/**
				 * Electronic
				 * 
				 */
				
				line = line.replaceAll("(?i)Table\\. E([0-9]+) "+(Constants.delimeters[index])+" E([0-9]+)", "<@cross-ref-table-open>Table\\. $1<@cross-ref-table-close> "+(Constants.delimeters[index])+" <@cross-ref-table-open>$2<@cross-ref-table-close>"); // simple dash
				line = line.replaceAll("(?i)Table E([0-9]+) "+(Constants.delimeters[index])+" E([0-9]+)", "<@cross-ref-table-open>Table $1<@cross-ref-table-close> "+(Constants.delimeters[index])+" <@cross-ref-table-open>$2<@cross-ref-table-close>"); // simple dash
				line = line.replaceAll("(?i)Tables\\. E([0-9]+) "+(Constants.delimeters[index])+" E([0-9]+)", "<@cross-ref-table-open>Tables\\. $1<@cross-ref-table-close> "+(Constants.delimeters[index])+" <@cross-ref-table-open>$2<@cross-ref-table-close>"); // simple dash
				line = line.replaceAll("(?i)Tables E([0-9]+) "+(Constants.delimeters[index])+" E([0-9]+)", "<@cross-ref-table-open>Tables $1<@cross-ref-table-close> "+(Constants.delimeters[index])+" <@cross-ref-table-open>$2<@cross-ref-table-close>"); // simple dash
			}
		}
		return line;
	}

	public String boxCrossMarking(String line) {
		
		boolean isFound = false;
		
		line = handleBoxRangesDelimeters(line);
		
		/**
		 * multiple occurance handling
		 * 
		 */
		
		line = line.replaceAll("(?i)Box\\. ([0-9]+)\\-([0-9]+)", "<@cross-ref-box-open>Box\\. $1\\-$2<@cross-ref-box-close>"); // simple dash
		line = line.replaceAll("(?i)Box\\. ([0-9]+)\\—([0-9]+)", "<@cross-ref-box-open>Box\\. $1\\—$2<@cross-ref-box-close>"); // em-dash
		line = line.replaceAll("(?i)Box\\. ([0-9]+)\\–([0-9]+)", "<@cross-ref-box-open>Box\\. $1\\–$2<@cross-ref-box-close>"); // en-dash
		line = line.replaceAll("(?i)Box\\. ([0-9]+)\\.([0-9]+)", "<@cross-ref-box-open>Box\\. $1\\.$2<@cross-ref-box-close>"); // simple period

		
		line = line.replaceAll("(?i)Box ([0-9]+)\\-([0-9]+)", "<@cross-ref-box-open>Box $1\\-$2<@cross-ref-box-close>"); // simple dash
		line = line.replaceAll("(?i)Box ([0-9]+)\\—([0-9]+)", "<@cross-ref-box-open>Box $1\\—$2<@cross-ref-box-close>"); // em-dash
		line = line.replaceAll("(?i)Box ([0-9]+)\\–([0-9]+)", "<@cross-ref-box-open>Box $1\\–$2<@cross-ref-box-close>"); // en-dash
		line = line.replaceAll("(?i)Box ([0-9]+)\\.([0-9]+)", "<@cross-ref-box-open>Box $1\\.$2<@cross-ref-box-close>"); // simple period
		
		/**
		 * single space handling
		 * 
		 */
		line = line.replaceAll("(?i)Box\\. ([0-9]+)\\- ([0-9]+)", "<@cross-ref-box-open>Box\\. $1\\-$2<@cross-ref-box-close>"); // simple dash
		line = line.replaceAll("(?i)Box\\. ([0-9]+)\\— ([0-9]+)", "<@cross-ref-box-open>Box\\. $1\\—$2<@cross-ref-box-close>"); // em-dash
		line = line.replaceAll("(?i)Box\\. ([0-9]+)\\– ([0-9]+)", "<@cross-ref-box-open>Box\\. $1\\–$2<@cross-ref-box-close>"); // en-dash
		line = line.replaceAll("(?i)Box\\. ([0-9]+)\\. ([0-9]+)", "<@cross-ref-box-open>Box\\. $1\\.$2<@cross-ref-box-close>"); // simple period

		
		line = line.replaceAll("(?i)Box ([0-9]+)\\- ([0-9]+)", "<@cross-ref-box-open>Box $1\\-$2<@cross-ref-box-close>"); // simple dash
		line = line.replaceAll("(?i)Box ([0-9]+)\\— ([0-9]+)", "<@cross-ref-box-open>Box $1\\—$2<@cross-ref-box-close>"); // em-dash
		line = line.replaceAll("(?i)Box ([0-9]+)\\– ([0-9]+)", "<@cross-ref-box-open>Box $1\\–$2<@cross-ref-box-close>"); // en-dash
		line = line.replaceAll("(?i)Box ([0-9]+)\\. ([0-9]+)", "<@cross-ref-box-open>Box $1\\.$2<@cross-ref-box-close>"); // simple period
		
		
		/**
		 * 
		 * ELECTRONIC
		 */
		
		/**
		 * ELECTRONIC multiple occurance handling
		 * 
		 */
		
		line = line.replaceAll("(?i)Box\\. E([0-9]+)\\-([0-9]+)", "<@cross-ref-box-open>Box\\. E$1\\-$2<@cross-ref-box-close>"); // simple dash
		line = line.replaceAll("(?i)Box\\. E([0-9]+)\\—([0-9]+)", "<@cross-ref-box-open>Box\\. E$1\\—$2<@cross-ref-box-close>"); // em-dash
		line = line.replaceAll("(?i)Box\\. E([0-9]+)\\–([0-9]+)", "<@cross-ref-box-open>Box\\. E$1\\–$2<@cross-ref-box-close>"); // en-dash
		line = line.replaceAll("(?i)Box\\. E([0-9]+)\\.([0-9]+)", "<@cross-ref-box-open>Box\\. E$1\\.$2<@cross-ref-box-close>"); // simple period

		
		line = line.replaceAll("(?i)Box E([0-9]+)\\-([0-9]+)", "<@cross-ref-box-open>Box E$1\\-$2<@cross-ref-box-close>"); // simple dash
		line = line.replaceAll("(?i)Box E([0-9]+)\\—([0-9]+)", "<@cross-ref-box-open>Box E$1\\—$2<@cross-ref-box-close>"); // em-dash
		line = line.replaceAll("(?i)Box E([0-9]+)\\–([0-9]+)", "<@cross-ref-box-open>Box E$1\\–$2<@cross-ref-box-close>"); // en-dash
		line = line.replaceAll("(?i)Box E([0-9]+)\\.([0-9]+)", "<@cross-ref-box-open>Box E$1\\.$2<@cross-ref-box-close>"); // simple period
		
		/**
		 * ELECTRONIC single space handling
		 * 
		 */
		line = line.replaceAll("(?i)Box\\. E([0-9]+)\\- ([0-9]+)", "<@cross-ref-box-open>Box\\. E$1\\-$2<@cross-ref-box-close>"); // simple dash
		line = line.replaceAll("(?i)Box\\. E([0-9]+)\\— ([0-9]+)", "<@cross-ref-box-open>Box\\. E$1\\—$2<@cross-ref-box-close>"); // em-dash
		line = line.replaceAll("(?i)Box\\. E([0-9]+)\\– ([0-9]+)", "<@cross-ref-box-open>Box\\. E$1\\–$2<@cross-ref-box-close>"); // en-dash
		line = line.replaceAll("(?i)Box\\. E([0-9]+)\\. ([0-9]+)", "<@cross-ref-box-open>Box\\. E$1\\.$2<@cross-ref-box-close>"); // simple period

		
		line = line.replaceAll("(?i)Box E([0-9]+)\\- ([0-9]+)", "<@cross-ref-box-open>Box E$1\\-$2<@cross-ref-box-close>"); // simple dash
		line = line.replaceAll("(?i)Box E([0-9]+)\\— ([0-9]+)", "<@cross-ref-box-open>Box E$1\\—$2<@cross-ref-box-close>"); // em-dash
		line = line.replaceAll("(?i)Box E([0-9]+)\\– ([0-9]+)", "<@cross-ref-box-open>Box E$1\\–$2<@cross-ref-box-close>"); // en-dash
		line = line.replaceAll("(?i)Box E([0-9]+)\\. ([0-9]+)", "<@cross-ref-box-open>Box E$1\\.$2<@cross-ref-box-close>"); // simple period

		
		if (line.contains("<@cross-ref-box-open>"))	isFound = true;
		if (! isFound) {
			/**
			 * single occurance handling
			 * 
			 */
			
			line = line.replaceAll("(?i)Box\\. ([0-9]+)", "<@cross-ref-box-open>Box\\. $1<@cross-ref-box-close>");
			line = line.replaceAll("(?i)Box ([0-9]+)", "<@cross-ref-box-open>Box $1<@cross-ref-box-close>");
			/**
			 * ELECTRONIC
			 */
			line = line.replaceAll("(?i)Box\\. E([0-9]+)", "<@cross-ref-box-open>Box\\. E$1<@cross-ref-box-close>");
			line = line.replaceAll("(?i)Box E([0-9]+)", "<@cross-ref-box-open>Box E$1<@cross-ref-box-close>");
		}
		return line;
	}
	
	private String handleBoxRangesDelimeters(String line) {
		
		for (int index = 0; index < Constants.delimeters.length; index ++) {
			
			boolean isFound = false;
			
			line = line.replaceAll("(?i)Box\\. ([0-9]+)\\-([0-9]+) "+(Constants.delimeters[index])+" ([0-9]+)\\-([0-9]+)", "<@cross-ref-box-open>Box\\. $1\\-$2<@cross-ref-box-close> "+(Constants.delimeters[index])+" <@cross-ref-box-open>$3\\-$4<@cross-ref-box-close>"); // simple dash
			line = line.replaceAll("(?i)Box\\. ([0-9]+)\\—([0-9]+) "+(Constants.delimeters[index])+" ([0-9]+)\\—([0-9]+)", "<@cross-ref-box-open>Box\\. $1\\—$2<@cross-ref-box-close> "+(Constants.delimeters[index])+" <@cross-ref-box-open>$3\\-$4<@cross-ref-box-close>"); // em-dash
			line = line.replaceAll("(?i)Box\\. ([0-9]+)\\–([0-9]+) "+(Constants.delimeters[index])+" ([0-9]+)\\–([0-9]+)", "<@cross-ref-box-open>Box\\. $1\\–$2<@cross-ref-box-close> "+(Constants.delimeters[index])+" <@cross-ref-box-open>$3\\-$4<@cross-ref-box-close>"); // en-dash
			line = line.replaceAll("(?i)Box\\. ([0-9]+)\\.([0-9]+) "+(Constants.delimeters[index])+" ([0-9]+)\\.([0-9]+)", "<@cross-ref-box-open>Box\\. $1\\.$2<@cross-ref-box-close> "+(Constants.delimeters[index])+" <@cross-ref-box-open>$3\\.$4<@cross-ref-box-close>"); // simple period
			
			line = line.replaceAll("(?i)Box ([0-9]+)\\-([0-9]+) "+(Constants.delimeters[index])+" ([0-9]+)\\-([0-9]+)", "<@cross-ref-box-open>Box $1\\-$2<@cross-ref-box-close> "+(Constants.delimeters[index])+" <@cross-ref-box-open>$3\\-$4<@cross-ref-box-close>"); // simple dash
			line = line.replaceAll("(?i)Box ([0-9]+)\\—([0-9]+) "+(Constants.delimeters[index])+" ([0-9]+)\\—([0-9]+)", "<@cross-ref-box-open>Box $1\\—$2<@cross-ref-box-close> "+(Constants.delimeters[index])+" <@cross-ref-box-open>$3\\-$4<@cross-ref-box-close>"); // em-dash
			line = line.replaceAll("(?i)Box ([0-9]+)\\–([0-9]+) "+(Constants.delimeters[index])+" ([0-9]+)\\–([0-9]+)", "<@cross-ref-box-open>Box $1\\–$2<@cross-ref-box-close> "+(Constants.delimeters[index])+" <@cross-ref-box-open>$3\\-$4<@cross-ref-box-close>"); // en-dash
			line = line.replaceAll("(?i)Box ([0-9]+)\\.([0-9]+) "+(Constants.delimeters[index])+" ([0-9]+)\\.([0-9]+)", "<@cross-ref-box-open>Box $1\\.$2<@cross-ref-box-close> "+(Constants.delimeters[index])+" <@cross-ref-box-open>$3\\.$4<@cross-ref-box-close>"); // simple period
			
			
			
			line = line.replaceAll("(?i)Boxs\\. ([0-9]+)\\-([0-9]+) "+(Constants.delimeters[index])+" ([0-9]+)\\-([0-9]+)", "<@cross-ref-box-open>Boxs\\. $1\\-$2<@cross-ref-box-close> "+(Constants.delimeters[index])+" <@cross-ref-box-open>$3\\-$4<@cross-ref-box-close>"); // simple dash
			line = line.replaceAll("(?i)Boxs\\. ([0-9]+)\\—([0-9]+) "+(Constants.delimeters[index])+" ([0-9]+)\\—([0-9]+)", "<@cross-ref-box-open>Boxs\\. $1\\—$2<@cross-ref-box-close> "+(Constants.delimeters[index])+" <@cross-ref-box-open>$3\\-$4<@cross-ref-box-close>"); // em-dash
			line = line.replaceAll("(?i)Boxs\\. ([0-9]+)\\–([0-9]+) "+(Constants.delimeters[index])+" ([0-9]+)\\–([0-9]+)", "<@cross-ref-box-open>Boxs\\. $1\\–$2<@cross-ref-box-close> "+(Constants.delimeters[index])+" <@cross-ref-box-open>$3\\-$4<@cross-ref-box-close>"); // en-dash
			line = line.replaceAll("(?i)Boxs\\. ([0-9]+)\\.([0-9]+) "+(Constants.delimeters[index])+" ([0-9]+)\\.([0-9]+)", "<@cross-ref-box-open>Boxs\\. $1\\.$2<@cross-ref-box-close> "+(Constants.delimeters[index])+" <@cross-ref-box-open>$3\\.$4<@cross-ref-box-close>"); // simple period
			
			line = line.replaceAll("(?i)Boxs ([0-9]+)\\-([0-9]+) "+(Constants.delimeters[index])+" ([0-9]+)\\-([0-9]+)", "<@cross-ref-box-open>Boxs $1\\-$2<@cross-ref-box-close> "+(Constants.delimeters[index])+" <@cross-ref-box-open>$3\\-$4<@cross-ref-box-close>"); // simple dash
			line = line.replaceAll("(?i)Boxs ([0-9]+)\\—([0-9]+) "+(Constants.delimeters[index])+" ([0-9]+)\\—([0-9]+)", "<@cross-ref-box-open>Boxs $1\\—$2<@cross-ref-box-close> "+(Constants.delimeters[index])+" <@cross-ref-box-open>$3\\-$4<@cross-ref-box-close>"); // em-dash
			line = line.replaceAll("(?i)Boxs ([0-9]+)\\–([0-9]+) "+(Constants.delimeters[index])+" ([0-9]+)\\–([0-9]+)", "<@cross-ref-box-open>Boxs $1\\–$2<@cross-ref-box-close> "+(Constants.delimeters[index])+" <@cross-ref-box-open>$3\\-$4<@cross-ref-box-close>"); // en-dash
			line = line.replaceAll("(?i)Boxs ([0-9]+)\\.([0-9]+) "+(Constants.delimeters[index])+" ([0-9]+)\\.([0-9]+)", "<@cross-ref-box-open>Boxs $1\\.$2<@cross-ref-box-close> "+(Constants.delimeters[index])+" <@cross-ref-box-open>$3\\.$4<@cross-ref-box-close>"); // simple period
			
			
			/**
			 * space both side
			 * 
			 */
			line = line.replaceAll("(?i)Boxs\\. ([0-9]+)\\- ([0-9]+) "+(Constants.delimeters[index])+" ([0-9]+)\\- ([0-9]+)", "<@cross-ref-box-open>Boxs\\. $1\\-$2<@cross-ref-box-close> "+(Constants.delimeters[index])+" <@cross-ref-box-open>$3\\-$4<@cross-ref-box-close>"); // simple dash
			line = line.replaceAll("(?i)Boxs\\. ([0-9]+)\\— ([0-9]+) "+(Constants.delimeters[index])+" ([0-9]+)\\— ([0-9]+)", "<@cross-ref-box-open>Boxs\\. $1\\—$2<@cross-ref-box-close> "+(Constants.delimeters[index])+" <@cross-ref-box-open>$3\\-$4<@cross-ref-box-close>"); // em-dash
			line = line.replaceAll("(?i)Boxs\\. ([0-9]+)\\– ([0-9]+) "+(Constants.delimeters[index])+" ([0-9]+)\\– ([0-9]+)", "<@cross-ref-box-open>Boxs\\. $1\\–$2<@cross-ref-box-close> "+(Constants.delimeters[index])+" <@cross-ref-box-open>$3\\-$4<@cross-ref-box-close>"); // en-dash
			line = line.replaceAll("(?i)Boxs\\. ([0-9]+)\\. ([0-9]+) "+(Constants.delimeters[index])+" ([0-9]+)\\. ([0-9]+)", "<@cross-ref-box-open>Boxs\\. $1\\.$2<@cross-ref-box-close> "+(Constants.delimeters[index])+" <@cross-ref-box-open>$3\\.$4<@cross-ref-box-close>"); // simple period
			
			line = line.replaceAll("(?i)Boxs ([0-9]+)\\- ([0-9]+) "+(Constants.delimeters[index])+" ([0-9]+)\\- ([0-9]+)", "<@cross-ref-box-open>Boxs $1\\-$2<@cross-ref-box-close> "+(Constants.delimeters[index])+" <@cross-ref-box-open>$3\\-$4<@cross-ref-box-close>"); // simple dash
			line = line.replaceAll("(?i)Boxs ([0-9]+)\\— ([0-9]+) "+(Constants.delimeters[index])+" ([0-9]+)\\— ([0-9]+)", "<@cross-ref-box-open>Boxs $1\\—$2<@cross-ref-box-close> "+(Constants.delimeters[index])+" <@cross-ref-box-open>$3\\-$4<@cross-ref-box-close>"); // em-dash
			line = line.replaceAll("(?i)Boxs ([0-9]+)\\– ([0-9]+) "+(Constants.delimeters[index])+" ([0-9]+)\\– ([0-9]+)", "<@cross-ref-box-open>Boxs $1\\–$2<@cross-ref-box-close> "+(Constants.delimeters[index])+" <@cross-ref-box-open>$3\\-$4<@cross-ref-box-close>"); // en-dash
			line = line.replaceAll("(?i)Boxs ([0-9]+)\\. ([0-9]+) "+(Constants.delimeters[index])+" ([0-9]+)\\. ([0-9]+)", "<@cross-ref-box-open>Boxs $1\\.$2<@cross-ref-box-close> "+(Constants.delimeters[index])+" <@cross-ref-box-open>$3\\.$4<@cross-ref-box-close>"); // simple period
			
			/**
			 * space left side
			 * 
			 */
			line = line.replaceAll("(?i)Boxs\\. ([0-9]+)\\-([0-9]+) "+(Constants.delimeters[index])+" ([0-9]+)\\- ([0-9]+)", "<@cross-ref-box-open>Boxs\\. $1\\-$2<@cross-ref-box-close> "+(Constants.delimeters[index])+" <@cross-ref-box-open>$3\\-$4<@cross-ref-box-close>"); // simple dash
			line = line.replaceAll("(?i)Boxs\\. ([0-9]+)\\—([0-9]+) "+(Constants.delimeters[index])+" ([0-9]+)\\— ([0-9]+)", "<@cross-ref-box-open>Boxs\\. $1\\—$2<@cross-ref-box-close> "+(Constants.delimeters[index])+" <@cross-ref-box-open>$3\\-$4<@cross-ref-box-close>"); // em-dash
			line = line.replaceAll("(?i)Boxs\\. ([0-9]+)\\–([0-9]+) "+(Constants.delimeters[index])+" ([0-9]+)\\– ([0-9]+)", "<@cross-ref-box-open>Boxs\\. $1\\–$2<@cross-ref-box-close> "+(Constants.delimeters[index])+" <@cross-ref-box-open>$3\\-$4<@cross-ref-box-close>"); // en-dash
			line = line.replaceAll("(?i)Boxs\\. ([0-9]+)\\.([0-9]+) "+(Constants.delimeters[index])+" ([0-9]+)\\. ([0-9]+)", "<@cross-ref-box-open>Boxs\\. $1\\.$2<@cross-ref-box-close> "+(Constants.delimeters[index])+" <@cross-ref-box-open>$3\\.$4<@cross-ref-box-close>"); // simple period
			
			line = line.replaceAll("(?i)Boxs ([0-9]+)\\-([0-9]+) "+(Constants.delimeters[index])+" ([0-9]+)\\- ([0-9]+)", "<@cross-ref-box-open>Boxs $1\\-$2<@cross-ref-box-close> "+(Constants.delimeters[index])+" <@cross-ref-box-open>$3\\-$4<@cross-ref-box-close>"); // simple dash
			line = line.replaceAll("(?i)Boxs ([0-9]+)\\—([0-9]+) "+(Constants.delimeters[index])+" ([0-9]+)\\— ([0-9]+)", "<@cross-ref-box-open>Boxs $1\\—$2<@cross-ref-box-close> "+(Constants.delimeters[index])+" <@cross-ref-box-open>$3\\-$4<@cross-ref-box-close>"); // em-dash
			line = line.replaceAll("(?i)Boxs ([0-9]+)\\–([0-9]+) "+(Constants.delimeters[index])+" ([0-9]+)\\– ([0-9]+)", "<@cross-ref-box-open>Boxs $1\\–$2<@cross-ref-box-close> "+(Constants.delimeters[index])+" <@cross-ref-box-open>$3\\-$4<@cross-ref-box-close>"); // en-dash
			line = line.replaceAll("(?i)Boxs ([0-9]+)\\.([0-9]+) "+(Constants.delimeters[index])+" ([0-9]+)\\. ([0-9]+)", "<@cross-ref-box-open>Boxs $1\\.$2<@cross-ref-box-close> "+(Constants.delimeters[index])+" <@cross-ref-box-open>$3\\.$4<@cross-ref-box-close>"); // simple period
			
			/**
			 * space right side
			 * 
			 */
			line = line.replaceAll("(?i)Boxs\\. ([0-9]+)\\- ([0-9]+) "+(Constants.delimeters[index])+" ([0-9]+)\\-([0-9]+)", "<@cross-ref-box-open>Boxs\\. $1\\-$2<@cross-ref-box-close> "+(Constants.delimeters[index])+" <@cross-ref-box-open>$3\\-$4<@cross-ref-box-close>"); // simple dash
			line = line.replaceAll("(?i)Boxs\\. ([0-9]+)\\— ([0-9]+) "+(Constants.delimeters[index])+" ([0-9]+)\\—([0-9]+)", "<@cross-ref-box-open>Boxs\\. $1\\—$2<@cross-ref-box-close> "+(Constants.delimeters[index])+" <@cross-ref-box-open>$3\\-$4<@cross-ref-box-close>"); // em-dash
			line = line.replaceAll("(?i)Boxs\\. ([0-9]+)\\– ([0-9]+) "+(Constants.delimeters[index])+" ([0-9]+)\\–([0-9]+)", "<@cross-ref-box-open>Boxs\\. $1\\–$2<@cross-ref-box-close> "+(Constants.delimeters[index])+" <@cross-ref-box-open>$3\\-$4<@cross-ref-box-close>"); // en-dash
			line = line.replaceAll("(?i)Boxs\\. ([0-9]+)\\. ([0-9]+) "+(Constants.delimeters[index])+" ([0-9]+)\\.([0-9]+)", "<@cross-ref-box-open>Boxs\\. $1\\.$2<@cross-ref-box-close> "+(Constants.delimeters[index])+" <@cross-ref-box-open>$3\\.$4<@cross-ref-box-close>"); // simple period
			
			line = line.replaceAll("(?i)Boxs ([0-9]+)\\- ([0-9]+) "+(Constants.delimeters[index])+" ([0-9]+)\\-([0-9]+)", "<@cross-ref-box-open>Boxs $1\\-$2<@cross-ref-box-close> "+(Constants.delimeters[index])+" <@cross-ref-box-open>$3\\-$4<@cross-ref-box-close>"); // simple dash
			line = line.replaceAll("(?i)Boxs ([0-9]+)\\— ([0-9]+) "+(Constants.delimeters[index])+" ([0-9]+)\\—([0-9]+)", "<@cross-ref-box-open>Boxs $1\\—$2<@cross-ref-box-close> "+(Constants.delimeters[index])+" <@cross-ref-box-open>$3\\-$4<@cross-ref-box-close>"); // em-dash
			line = line.replaceAll("(?i)Boxs ([0-9]+)\\– ([0-9]+) "+(Constants.delimeters[index])+" ([0-9]+)\\–([0-9]+)", "<@cross-ref-box-open>Boxs $1\\–$2<@cross-ref-box-close> "+(Constants.delimeters[index])+" <@cross-ref-box-open>$3\\-$4<@cross-ref-box-close>"); // en-dash
			line = line.replaceAll("(?i)Boxs ([0-9]+)\\. ([0-9]+) "+(Constants.delimeters[index])+" ([0-9]+)\\.([0-9]+)", "<@cross-ref-box-open>Boxs $1\\.$2<@cross-ref-box-close> "+(Constants.delimeters[index])+" <@cross-ref-box-open>$3\\.$4<@cross-ref-box-close>"); // simple period
			
			
			/**
			 * Electronic
			 * 
			 */
			
			line = line.replaceAll("(?i)Box\\. E([0-9]+)\\-([0-9]+) "+(Constants.delimeters[index])+" E([0-9]+)\\-([0-9]+)", "<@cross-ref-box-open>Box\\. $1\\-$2<@cross-ref-box-close> "+(Constants.delimeters[index])+" <@cross-ref-box-open>$3\\-$4<@cross-ref-box-close>"); // simple dash
			line = line.replaceAll("(?i)Box\\. E([0-9]+)\\—([0-9]+) "+(Constants.delimeters[index])+" E([0-9]+)\\—([0-9]+)", "<@cross-ref-box-open>Box\\. $1\\—$2<@cross-ref-box-close> "+(Constants.delimeters[index])+" <@cross-ref-box-open>$3\\-$4<@cross-ref-box-close>"); // em-dash
			line = line.replaceAll("(?i)Box\\. E([0-9]+)\\–([0-9]+) "+(Constants.delimeters[index])+" E([0-9]+)\\–([0-9]+)", "<@cross-ref-box-open>Box\\. $1\\–$2<@cross-ref-box-close> "+(Constants.delimeters[index])+" <@cross-ref-box-open>$3\\-$4<@cross-ref-box-close>"); // en-dash
			line = line.replaceAll("(?i)Box\\. E([0-9]+)\\.([0-9]+) "+(Constants.delimeters[index])+" E([0-9]+)\\.([0-9]+)", "<@cross-ref-box-open>Box\\. $1\\.$2<@cross-ref-box-close> "+(Constants.delimeters[index])+" <@cross-ref-box-open>$3\\.$4<@cross-ref-box-close>"); // simple period
			
			line = line.replaceAll("(?i)Box E([0-9]+)\\-([0-9]+) "+(Constants.delimeters[index])+" E([0-9]+)\\-([0-9]+)", "<@cross-ref-box-open>Box $1\\-$2<@cross-ref-box-close> "+(Constants.delimeters[index])+" <@cross-ref-box-open>$3\\-$4<@cross-ref-box-close>"); // simple dash
			line = line.replaceAll("(?i)Box E([0-9]+)\\—([0-9]+) "+(Constants.delimeters[index])+" E([0-9]+)\\—([0-9]+)", "<@cross-ref-box-open>Box $1\\—$2<@cross-ref-box-close> "+(Constants.delimeters[index])+" <@cross-ref-box-open>$3\\-$4<@cross-ref-box-close>"); // em-dash
			line = line.replaceAll("(?i)Box E([0-9]+)\\–([0-9]+) "+(Constants.delimeters[index])+" E([0-9]+)\\–([0-9]+)", "<@cross-ref-box-open>Box $1\\–$2<@cross-ref-box-close> "+(Constants.delimeters[index])+" <@cross-ref-box-open>$3\\-$4<@cross-ref-box-close>"); // en-dash
			line = line.replaceAll("(?i)Box E([0-9]+)\\.([0-9]+) "+(Constants.delimeters[index])+" E([0-9]+)\\.([0-9]+)", "<@cross-ref-box-open>Box $1\\.$2<@cross-ref-box-close> "+(Constants.delimeters[index])+" <@cross-ref-box-open>$3\\.$4<@cross-ref-box-close>"); // simple period
			
			
			
			line = line.replaceAll("(?i)Boxs\\. E([0-9]+)\\-([0-9]+) "+(Constants.delimeters[index])+" E([0-9]+)\\-([0-9]+)", "<@cross-ref-box-open>Boxs\\. $1\\-$2<@cross-ref-box-close> "+(Constants.delimeters[index])+" <@cross-ref-box-open>$3\\-$4<@cross-ref-box-close>"); // simple dash
			line = line.replaceAll("(?i)Boxs\\. E([0-9]+)\\—([0-9]+) "+(Constants.delimeters[index])+" E([0-9]+)\\—([0-9]+)", "<@cross-ref-box-open>Boxs\\. $1\\—$2<@cross-ref-box-close> "+(Constants.delimeters[index])+" <@cross-ref-box-open>$3\\-$4<@cross-ref-box-close>"); // em-dash
			line = line.replaceAll("(?i)Boxs\\. E([0-9]+)\\–([0-9]+) "+(Constants.delimeters[index])+" E([0-9]+)\\–([0-9]+)", "<@cross-ref-box-open>Boxs\\. $1\\–$2<@cross-ref-box-close> "+(Constants.delimeters[index])+" <@cross-ref-box-open>$3\\-$4<@cross-ref-box-close>"); // en-dash
			line = line.replaceAll("(?i)Boxs\\. E([0-9]+)\\.([0-9]+) "+(Constants.delimeters[index])+" E([0-9]+)\\.([0-9]+)", "<@cross-ref-box-open>Boxs\\. $1\\.$2<@cross-ref-box-close> "+(Constants.delimeters[index])+" <@cross-ref-box-open>$3\\.$4<@cross-ref-box-close>"); // simple period
			
			line = line.replaceAll("(?i)Boxs E([0-9]+)\\-([0-9]+) "+(Constants.delimeters[index])+" E([0-9]+)\\-([0-9]+)", "<@cross-ref-box-open>Boxs $1\\-$2<@cross-ref-box-close> "+(Constants.delimeters[index])+" <@cross-ref-box-open>$3\\-$4<@cross-ref-box-close>"); // simple dash
			line = line.replaceAll("(?i)Boxs E([0-9]+)\\—([0-9]+) "+(Constants.delimeters[index])+" E([0-9]+)\\—([0-9]+)", "<@cross-ref-box-open>Boxs $1\\—$2<@cross-ref-box-close> "+(Constants.delimeters[index])+" <@cross-ref-box-open>$3\\-$4<@cross-ref-box-close>"); // em-dash
			line = line.replaceAll("(?i)Boxs E([0-9]+)\\–([0-9]+) "+(Constants.delimeters[index])+" E([0-9]+)\\–([0-9]+)", "<@cross-ref-box-open>Boxs $1\\–$2<@cross-ref-box-close> "+(Constants.delimeters[index])+" <@cross-ref-box-open>$3\\-$4<@cross-ref-box-close>"); // en-dash
			line = line.replaceAll("(?i)Boxs E([0-9]+)\\.([0-9]+) "+(Constants.delimeters[index])+" E([0-9]+)\\.([0-9]+)", "<@cross-ref-box-open>Boxs $1\\.$2<@cross-ref-box-close> "+(Constants.delimeters[index])+" <@cross-ref-box-open>$3\\.$4<@cross-ref-box-close>"); // simple period
			
			
			/**
			 * Electronic space both side
			 * 
			 */
			line = line.replaceAll("(?i)Boxs\\. E([0-9]+)\\- ([0-9]+) "+(Constants.delimeters[index])+" E([0-9]+)\\- ([0-9]+)", "<@cross-ref-box-open>Boxs\\. $1\\-$2<@cross-ref-box-close> "+(Constants.delimeters[index])+" <@cross-ref-box-open>$3\\-$4<@cross-ref-box-close>"); // simple dash
			line = line.replaceAll("(?i)Boxs\\. E([0-9]+)\\— ([0-9]+) "+(Constants.delimeters[index])+" E([0-9]+)\\— ([0-9]+)", "<@cross-ref-box-open>Boxs\\. $1\\—$2<@cross-ref-box-close> "+(Constants.delimeters[index])+" <@cross-ref-box-open>$3\\-$4<@cross-ref-box-close>"); // em-dash
			line = line.replaceAll("(?i)Boxs\\. E([0-9]+)\\– ([0-9]+) "+(Constants.delimeters[index])+" E([0-9]+)\\– ([0-9]+)", "<@cross-ref-box-open>Boxs\\. $1\\–$2<@cross-ref-box-close> "+(Constants.delimeters[index])+" <@cross-ref-box-open>$3\\-$4<@cross-ref-box-close>"); // en-dash
			line = line.replaceAll("(?i)Boxs\\. E([0-9]+)\\. ([0-9]+) "+(Constants.delimeters[index])+" E([0-9]+)\\. ([0-9]+)", "<@cross-ref-box-open>Boxs\\. $1\\.$2<@cross-ref-box-close> "+(Constants.delimeters[index])+" <@cross-ref-box-open>$3\\.$4<@cross-ref-box-close>"); // simple period
			
			line = line.replaceAll("(?i)Boxs E([0-9]+)\\- ([0-9]+) "+(Constants.delimeters[index])+" E([0-9]+)\\- ([0-9]+)", "<@cross-ref-box-open>Boxs $1\\-$2<@cross-ref-box-close> "+(Constants.delimeters[index])+" <@cross-ref-box-open>$3\\-$4<@cross-ref-box-close>"); // simple dash
			line = line.replaceAll("(?i)Boxs E([0-9]+)\\— ([0-9]+) "+(Constants.delimeters[index])+" E([0-9]+)\\— ([0-9]+)", "<@cross-ref-box-open>Boxs $1\\—$2<@cross-ref-box-close> "+(Constants.delimeters[index])+" <@cross-ref-box-open>$3\\-$4<@cross-ref-box-close>"); // em-dash
			line = line.replaceAll("(?i)Boxs E([0-9]+)\\– ([0-9]+) "+(Constants.delimeters[index])+" E([0-9]+)\\– ([0-9]+)", "<@cross-ref-box-open>Boxs $1\\–$2<@cross-ref-box-close> "+(Constants.delimeters[index])+" <@cross-ref-box-open>$3\\-$4<@cross-ref-box-close>"); // en-dash
			line = line.replaceAll("(?i)Boxs E([0-9]+)\\. ([0-9]+) "+(Constants.delimeters[index])+" E([0-9]+)\\. ([0-9]+)", "<@cross-ref-box-open>Boxs $1\\.$2<@cross-ref-box-close> "+(Constants.delimeters[index])+" <@cross-ref-box-open>$3\\.$4<@cross-ref-box-close>"); // simple period
			
			/**
			 * Electronic space left side
			 * 
			 */
			line = line.replaceAll("(?i)Boxs\\. E([0-9]+)\\-([0-9]+) "+(Constants.delimeters[index])+" E([0-9]+)\\- ([0-9]+)", "<@cross-ref-box-open>Boxs\\. $1\\-$2<@cross-ref-box-close> "+(Constants.delimeters[index])+" <@cross-ref-box-open>$3\\-$4<@cross-ref-box-close>"); // simple dash
			line = line.replaceAll("(?i)Boxs\\. E([0-9]+)\\—([0-9]+) "+(Constants.delimeters[index])+" E([0-9]+)\\— ([0-9]+)", "<@cross-ref-box-open>Boxs\\. $1\\—$2<@cross-ref-box-close> "+(Constants.delimeters[index])+" <@cross-ref-box-open>$3\\-$4<@cross-ref-box-close>"); // em-dash
			line = line.replaceAll("(?i)Boxs\\. E([0-9]+)\\–([0-9]+) "+(Constants.delimeters[index])+" E([0-9]+)\\– ([0-9]+)", "<@cross-ref-box-open>Boxs\\. $1\\–$2<@cross-ref-box-close> "+(Constants.delimeters[index])+" <@cross-ref-box-open>$3\\-$4<@cross-ref-box-close>"); // en-dash
			line = line.replaceAll("(?i)Boxs\\. E([0-9]+)\\.([0-9]+) "+(Constants.delimeters[index])+" E([0-9]+)\\. ([0-9]+)", "<@cross-ref-box-open>Boxs\\. $1\\.$2<@cross-ref-box-close> "+(Constants.delimeters[index])+" <@cross-ref-box-open>$3\\.$4<@cross-ref-box-close>"); // simple period
			
			line = line.replaceAll("(?i)Boxs E([0-9]+)\\-([0-9]+) "+(Constants.delimeters[index])+" E([0-9]+)\\- ([0-9]+)", "<@cross-ref-box-open>Boxs $1\\-$2<@cross-ref-box-close> "+(Constants.delimeters[index])+" <@cross-ref-box-open>$3\\-$4<@cross-ref-box-close>"); // simple dash
			line = line.replaceAll("(?i)Boxs E([0-9]+)\\—([0-9]+) "+(Constants.delimeters[index])+" E([0-9]+)\\— ([0-9]+)", "<@cross-ref-box-open>Boxs $1\\—$2<@cross-ref-box-close> "+(Constants.delimeters[index])+" <@cross-ref-box-open>$3\\-$4<@cross-ref-box-close>"); // em-dash
			line = line.replaceAll("(?i)Boxs E([0-9]+)\\–([0-9]+) "+(Constants.delimeters[index])+" E([0-9]+)\\– ([0-9]+)", "<@cross-ref-box-open>Boxs $1\\–$2<@cross-ref-box-close> "+(Constants.delimeters[index])+" <@cross-ref-box-open>$3\\-$4<@cross-ref-box-close>"); // en-dash
			line = line.replaceAll("(?i)Boxs E([0-9]+)\\.([0-9]+) "+(Constants.delimeters[index])+" E([0-9]+)\\. ([0-9]+)", "<@cross-ref-box-open>Boxs $1\\.$2<@cross-ref-box-close> "+(Constants.delimeters[index])+" <@cross-ref-box-open>$3\\.$4<@cross-ref-box-close>"); // simple period
			
			/**
			 * Electronic space right side
			 * 
			 */
			line = line.replaceAll("(?i)Boxs\\. E([0-9]+)\\- ([0-9]+) "+(Constants.delimeters[index])+" E([0-9]+)\\-([0-9]+)", "<@cross-ref-box-open>Boxs\\. $1\\-$2<@cross-ref-box-close> "+(Constants.delimeters[index])+" <@cross-ref-box-open>$3\\-$4<@cross-ref-box-close>"); // simple dash
			line = line.replaceAll("(?i)Boxs\\. E([0-9]+)\\— ([0-9]+) "+(Constants.delimeters[index])+" E([0-9]+)\\—([0-9]+)", "<@cross-ref-box-open>Boxs\\. $1\\—$2<@cross-ref-box-close> "+(Constants.delimeters[index])+" <@cross-ref-box-open>$3\\-$4<@cross-ref-box-close>"); // em-dash
			line = line.replaceAll("(?i)Boxs\\. E([0-9]+)\\– ([0-9]+) "+(Constants.delimeters[index])+" E([0-9]+)\\–([0-9]+)", "<@cross-ref-box-open>Boxs\\. $1\\–$2<@cross-ref-box-close> "+(Constants.delimeters[index])+" <@cross-ref-box-open>$3\\-$4<@cross-ref-box-close>"); // en-dash
			line = line.replaceAll("(?i)Boxs\\. E([0-9]+)\\. ([0-9]+) "+(Constants.delimeters[index])+" E([0-9]+)\\.([0-9]+)", "<@cross-ref-box-open>Boxs\\. $1\\.$2<@cross-ref-box-close> "+(Constants.delimeters[index])+" <@cross-ref-box-open>$3\\.$4<@cross-ref-box-close>"); // simple period
			
			line = line.replaceAll("(?i)Boxs E([0-9]+)\\- ([0-9]+) "+(Constants.delimeters[index])+" E([0-9]+)\\-([0-9]+)", "<@cross-ref-box-open>Boxs $1\\-$2<@cross-ref-box-close> "+(Constants.delimeters[index])+" <@cross-ref-box-open>$3\\-$4<@cross-ref-box-close>"); // simple dash
			line = line.replaceAll("(?i)Boxs E([0-9]+)\\— ([0-9]+) "+(Constants.delimeters[index])+" E([0-9]+)\\—([0-9]+)", "<@cross-ref-box-open>Boxs $1\\—$2<@cross-ref-box-close> "+(Constants.delimeters[index])+" <@cross-ref-box-open>$3\\-$4<@cross-ref-box-close>"); // em-dash
			line = line.replaceAll("(?i)Boxs E([0-9]+)\\– ([0-9]+) "+(Constants.delimeters[index])+" E([0-9]+)\\–([0-9]+)", "<@cross-ref-box-open>Boxs $1\\–$2<@cross-ref-box-close> "+(Constants.delimeters[index])+" <@cross-ref-box-open>$3\\-$4<@cross-ref-box-close>"); // en-dash
			line = line.replaceAll("(?i)Boxs E([0-9]+)\\. ([0-9]+) "+(Constants.delimeters[index])+" E([0-9]+)\\.([0-9]+)", "<@cross-ref-box-open>Boxs $1\\.$2<@cross-ref-box-close> "+(Constants.delimeters[index])+" <@cross-ref-box-open>$3\\.$4<@cross-ref-box-close>"); // simple period

			
			if (line.contains("<@cross-ref-box-open>"))	isFound = true;
			if ( ! isFound ) {
				
				line = line.replaceAll("(?i)Box\\. ([0-9]+) "+(Constants.delimeters[index])+" ([0-9]+)", "<@cross-ref-box-open>Box\\. $1<@cross-ref-box-close> "+(Constants.delimeters[index])+" <@cross-ref-box-open>$2<@cross-ref-box-close>"); // simple dash
				line = line.replaceAll("(?i)Box ([0-9]+) "+(Constants.delimeters[index])+" ([0-9]+)", "<@cross-ref-box-open>Box $1<@cross-ref-box-close> "+(Constants.delimeters[index])+" <@cross-ref-box-open>$2<@cross-ref-box-close>"); // simple dash
				line = line.replaceAll("(?i)Boxs\\. ([0-9]+) "+(Constants.delimeters[index])+" ([0-9]+)", "<@cross-ref-box-open>Boxs\\. $1<@cross-ref-box-close> "+(Constants.delimeters[index])+" <@cross-ref-box-open>$2<@cross-ref-box-close>"); // simple dash
				line = line.replaceAll("(?i)Boxs ([0-9]+) "+(Constants.delimeters[index])+" ([0-9]+)", "<@cross-ref-box-open>Boxs $1<@cross-ref-box-close> "+(Constants.delimeters[index])+" <@cross-ref-box-open>$2<@cross-ref-box-close>"); // simple dash
				
				
				/**
				 * ELECTRONIC
				 */
				
				line = line.replaceAll("(?i)Box\\. E([0-9]+) "+(Constants.delimeters[index])+" E([0-9]+)", "<@cross-ref-box-open>Box\\. $1<@cross-ref-box-close> "+(Constants.delimeters[index])+" <@cross-ref-box-open>$2<@cross-ref-box-close>"); // simple dash
				line = line.replaceAll("(?i)Box E([0-9]+) "+(Constants.delimeters[index])+" E([0-9]+)", "<@cross-ref-box-open>Box $1<@cross-ref-box-close> "+(Constants.delimeters[index])+" <@cross-ref-box-open>$2<@cross-ref-box-close>"); // simple dash
				line = line.replaceAll("(?i)Boxs\\. E([0-9]+) "+(Constants.delimeters[index])+" E([0-9]+)", "<@cross-ref-box-open>Boxs\\. $1<@cross-ref-box-close> "+(Constants.delimeters[index])+" <@cross-ref-box-open>$2<@cross-ref-box-close>"); // simple dash
				line = line.replaceAll("(?i)Boxs E([0-9]+) "+(Constants.delimeters[index])+" E([0-9]+)", "<@cross-ref-box-open>Boxs $1<@cross-ref-box-close> "+(Constants.delimeters[index])+" <@cross-ref-box-open>$2<@cross-ref-box-close>"); // simple dash
			}
		}
		
		return line;
	}

	public String videoCrossMarking(String line) {
		
		boolean isFound = false;
		
		line = handleVideoRangesDelimeters(line);
		
		/**
		 * multiple occurance handling
		 * 
		 */
		
		line = line.replaceAll("(?i)Video\\. ([0-9]+)\\-([0-9]+)", "<@cross-ref-video-open>Video\\. $1\\-$2<@cross-ref-video-close>"); // simple dash
		line = line.replaceAll("(?i)Video\\. ([0-9]+)\\—([0-9]+)", "<@cross-ref-video-open>Video\\. $1\\—$2<@cross-ref-video-close>"); // em-dash
		line = line.replaceAll("(?i)Video\\. ([0-9]+)\\–([0-9]+)", "<@cross-ref-video-open>Video\\. $1\\–$2<@cross-ref-video-close>"); // en-dash
		line = line.replaceAll("(?i)Video\\. ([0-9]+)\\.([0-9]+)", "<@cross-ref-video-open>Video\\. $1\\.$2<@cross-ref-video-close>"); // simple period
		
		line = line.replaceAll("(?i)Video ([0-9]+)\\-([0-9]+)", "<@cross-ref-video-open>Video $1\\-$2<@cross-ref-video-close>"); // simple dash
		line = line.replaceAll("(?i)Video ([0-9]+)\\—([0-9]+)", "<@cross-ref-video-open>Video $1\\—$2<@cross-ref-video-close>"); // em-dash
		line = line.replaceAll("(?i)Video ([0-9]+)\\–([0-9]+)", "<@cross-ref-video-open>Video $1\\–$2<@cross-ref-video-close>"); // en-dash
		line = line.replaceAll("(?i)Video ([0-9]+)\\.([0-9]+)", "<@cross-ref-video-open>Video $1\\.$2<@cross-ref-video-close>"); // simple period
		
		/**
		 * single space both side
		 * 
		 */
		line = line.replaceAll("(?i)Video\\. ([0-9]+)\\- ([0-9]+)", "<@cross-ref-video-open>Video\\. $1\\-$2<@cross-ref-video-close>"); // simple dash
		line = line.replaceAll("(?i)Video\\. ([0-9]+)\\— ([0-9]+)", "<@cross-ref-video-open>Video\\. $1\\—$2<@cross-ref-video-close>"); // em-dash
		line = line.replaceAll("(?i)Video\\. ([0-9]+)\\– ([0-9]+)", "<@cross-ref-video-open>Video\\. $1\\–$2<@cross-ref-video-close>"); // en-dash
		line = line.replaceAll("(?i)Video\\. ([0-9]+)\\. ([0-9]+)", "<@cross-ref-video-open>Video\\. $1\\.$2<@cross-ref-video-close>"); // simple period
		
		line = line.replaceAll("(?i)Video ([0-9]+)\\- ([0-9]+)", "<@cross-ref-video-open>Video $1\\-$2<@cross-ref-video-close>"); // simple dash
		line = line.replaceAll("(?i)Video ([0-9]+)\\— ([0-9]+)", "<@cross-ref-video-open>Video $1\\—$2<@cross-ref-video-close>"); // em-dash
		line = line.replaceAll("(?i)Video ([0-9]+)\\– ([0-9]+)", "<@cross-ref-video-open>Video $1\\–$2<@cross-ref-video-close>"); // en-dash
		line = line.replaceAll("(?i)Video ([0-9]+)\\. ([0-9]+)", "<@cross-ref-video-open>Video $1\\.$2<@cross-ref-video-close>"); // simple period
		
		/**
		 * electronic single space both side
		 */
		line = line.replaceAll("(?i)Video\\. E([0-9]+)\\- ([0-9]+)", "<@cross-ref-video-open>Video\\. E$1\\-$2<@cross-ref-video-close>"); // simple dash
		line = line.replaceAll("(?i)Video\\. E([0-9]+)\\— ([0-9]+)", "<@cross-ref-video-open>Video\\. E$1\\—$2<@cross-ref-video-close>"); // em-dash
		line = line.replaceAll("(?i)Video\\. E([0-9]+)\\– ([0-9]+)", "<@cross-ref-video-open>Video\\. E$1\\–$2<@cross-ref-video-close>"); // en-dash
		line = line.replaceAll("(?i)Video\\. E([0-9]+)\\. ([0-9]+)", "<@cross-ref-video-open>Video\\. E$1\\.$2<@cross-ref-video-close>"); // simple period
		
		line = line.replaceAll("(?i)Video E([0-9]+)\\- ([0-9]+)", "<@cross-ref-video-open>Video E$1\\-$2<@cross-ref-video-close>"); // simple dash
		line = line.replaceAll("(?i)Video E([0-9]+)\\— ([0-9]+)", "<@cross-ref-video-open>Video E$1\\—$2<@cross-ref-video-close>"); // em-dash
		line = line.replaceAll("(?i)Video E([0-9]+)\\– ([0-9]+)", "<@cross-ref-video-open>Video E$1\\–$2<@cross-ref-video-close>"); // en-dash
		line = line.replaceAll("(?i)Video E([0-9]+)\\. ([0-9]+)", "<@cross-ref-video-open>Video E$1\\.$2<@cross-ref-video-close>"); // simple period
		
		
		/**
		 * multiple occurance handling
		 * 
		 */
		
		line = line.replaceAll("(?i)Video\\. E([0-9]+)\\-([0-9]+)", "<@cross-ref-video-open>Video\\. E$1\\-$2<@cross-ref-video-close>"); // simple dash
		line = line.replaceAll("(?i)Video\\. E([0-9]+)\\—([0-9]+)", "<@cross-ref-video-open>Video\\. E$1\\—$2<@cross-ref-video-close>"); // em-dash
		line = line.replaceAll("(?i)Video\\. E([0-9]+)\\–([0-9]+)", "<@cross-ref-video-open>Video\\. E$1\\–$2<@cross-ref-video-close>"); // en-dash
		line = line.replaceAll("(?i)Video\\. E([0-9]+)\\.([0-9]+)", "<@cross-ref-video-open>Video\\. E$1\\.$2<@cross-ref-video-close>"); // simple period
		
		line = line.replaceAll("(?i)Video E([0-9]+)\\-([0-9]+)", "<@cross-ref-video-open>Video E$1\\-$2<@cross-ref-video-close>"); // simple dash
		line = line.replaceAll("(?i)Video E([0-9]+)\\—([0-9]+)", "<@cross-ref-video-open>Video E$1\\—$2<@cross-ref-video-close>"); // em-dash
		line = line.replaceAll("(?i)Video E([0-9]+)\\–([0-9]+)", "<@cross-ref-video-open>Video E$1\\–$2<@cross-ref-video-close>"); // en-dash
		line = line.replaceAll("(?i)Video E([0-9]+)\\.([0-9]+)", "<@cross-ref-video-open>Video E$1\\.$2<@cross-ref-video-close>"); // simple period

		
		if (line.contains("<@cross-ref-video-open>"))	isFound = true;
		if (! isFound) {
			/**
			 * single occurance handling
			 * 
			 */
			
			line = line.replaceAll("(?i)Video\\. ([0-9]+)", "<@cross-ref-video-open>Video\\. $1<@cross-ref-video-close>");
			line = line.replaceAll("(?i)Video ([0-9]+)", "<@cross-ref-video-open>Video $1<@cross-ref-video-close>");
			
			line = line.replaceAll("(?i)Video\\. E([0-9]+)", "<@cross-ref-video-open>Video\\. E$1<@cross-ref-video-close>");
			line = line.replaceAll("(?i)Video E([0-9]+)", "<@cross-ref-video-open>Video E$1<@cross-ref-video-close>");
		}
		return line;
	}
	
	private String handleVideoRangesDelimeters(String line) {
		
		for (int index = 0; index < Constants.delimeters.length; index ++) {
			
			boolean isFound = false;
			
			line = line.replaceAll("(?i)Video\\. ([0-9]+)\\-([0-9]+) "+(Constants.delimeters[index])+" ([0-9]+)\\-([0-9]+)", "<@cross-ref-video-open>Video\\. $1\\-$2<@cross-ref-video-close> "+(Constants.delimeters[index])+" <@cross-ref-video-open>$3\\-$4<@cross-ref-video-close>"); // simple dash
			line = line.replaceAll("(?i)Video\\. ([0-9]+)\\—([0-9]+) "+(Constants.delimeters[index])+" ([0-9]+)\\—([0-9]+)", "<@cross-ref-video-open>Video\\. $1\\—$2<@cross-ref-video-close> "+(Constants.delimeters[index])+" <@cross-ref-video-open>$3\\-$4<@cross-ref-video-close>"); // em-dash
			line = line.replaceAll("(?i)Video\\. ([0-9]+)\\–([0-9]+) "+(Constants.delimeters[index])+" ([0-9]+)\\–([0-9]+)", "<@cross-ref-video-open>Video\\. $1\\–$2<@cross-ref-video-close> "+(Constants.delimeters[index])+" <@cross-ref-video-open>$3\\-$4<@cross-ref-video-close>"); // en-dash
			line = line.replaceAll("(?i)Video\\. ([0-9]+)\\.([0-9]+) "+(Constants.delimeters[index])+" ([0-9]+)\\.([0-9]+)", "<@cross-ref-video-open>Video\\. $1\\.$2<@cross-ref-video-close> "+(Constants.delimeters[index])+" <@cross-ref-video-open>$3\\.$4<@cross-ref-video-close>"); // simple period
			
			line = line.replaceAll("(?i)Video ([0-9]+)\\-([0-9]+) "+(Constants.delimeters[index])+" ([0-9]+)\\-([0-9]+)", "<@cross-ref-video-open>Video $1\\-$2<@cross-ref-video-close> "+(Constants.delimeters[index])+" <@cross-ref-video-open>$3\\-$4<@cross-ref-video-close>"); // simple dash
			line = line.replaceAll("(?i)Video ([0-9]+)\\—([0-9]+) "+(Constants.delimeters[index])+" ([0-9]+)\\—([0-9]+)", "<@cross-ref-video-open>Video $1\\—$2<@cross-ref-video-close> "+(Constants.delimeters[index])+" <@cross-ref-video-open>$3\\-$4<@cross-ref-video-close>"); // em-dash
			line = line.replaceAll("(?i)Video ([0-9]+)\\–([0-9]+) "+(Constants.delimeters[index])+" ([0-9]+)\\–([0-9]+)", "<@cross-ref-video-open>Video $1\\–$2<@cross-ref-video-close> "+(Constants.delimeters[index])+" <@cross-ref-video-open>$3\\-$4<@cross-ref-video-close>"); // en-dash
			line = line.replaceAll("(?i)Video ([0-9]+)\\.([0-9]+) "+(Constants.delimeters[index])+" ([0-9]+)\\.([0-9]+)", "<@cross-ref-video-open>Video $1\\.$2<@cross-ref-video-close> "+(Constants.delimeters[index])+" <@cross-ref-video-open>$3\\.$4<@cross-ref-video-close>"); // simple period
			
			/**
			 * 
			 * space handle both side
			 */
			line = line.replaceAll("(?i)Video\\. ([0-9]+)\\- ([0-9]+) "+(Constants.delimeters[index])+" ([0-9]+)\\- ([0-9]+)", "<@cross-ref-video-open>Video\\. $1\\-$2<@cross-ref-video-close> "+(Constants.delimeters[index])+" <@cross-ref-video-open>$3\\-$4<@cross-ref-video-close>"); // simple dash
			line = line.replaceAll("(?i)Video\\. ([0-9]+)\\— ([0-9]+) "+(Constants.delimeters[index])+" ([0-9]+)\\— ([0-9]+)", "<@cross-ref-video-open>Video\\. $1\\—$2<@cross-ref-video-close> "+(Constants.delimeters[index])+" <@cross-ref-video-open>$3\\-$4<@cross-ref-video-close>"); // em-dash
			line = line.replaceAll("(?i)Video\\. ([0-9]+)\\– ([0-9]+) "+(Constants.delimeters[index])+" ([0-9]+)\\– ([0-9]+)", "<@cross-ref-video-open>Video\\. $1\\–$2<@cross-ref-video-close> "+(Constants.delimeters[index])+" <@cross-ref-video-open>$3\\-$4<@cross-ref-video-close>"); // en-dash
			line = line.replaceAll("(?i)Video\\. ([0-9]+)\\. ([0-9]+) "+(Constants.delimeters[index])+" ([0-9]+)\\. ([0-9]+)", "<@cross-ref-video-open>Video\\. $1\\.$2<@cross-ref-video-close> "+(Constants.delimeters[index])+" <@cross-ref-video-open>$3\\.$4<@cross-ref-video-close>"); // simple period
			
			line = line.replaceAll("(?i)Video ([0-9]+)\\- ([0-9]+) "+(Constants.delimeters[index])+" ([0-9]+)\\- ([0-9]+)", "<@cross-ref-video-open>Video $1\\-$2<@cross-ref-video-close> "+(Constants.delimeters[index])+" <@cross-ref-video-open>$3\\-$4<@cross-ref-video-close>"); // simple dash
			line = line.replaceAll("(?i)Video ([0-9]+)\\— ([0-9]+) "+(Constants.delimeters[index])+" ([0-9]+)\\— ([0-9]+)", "<@cross-ref-video-open>Video $1\\—$2<@cross-ref-video-close> "+(Constants.delimeters[index])+" <@cross-ref-video-open>$3\\-$4<@cross-ref-video-close>"); // em-dash
			line = line.replaceAll("(?i)Video ([0-9]+)\\– ([0-9]+) "+(Constants.delimeters[index])+" ([0-9]+)\\– ([0-9]+)", "<@cross-ref-video-open>Video $1\\–$2<@cross-ref-video-close> "+(Constants.delimeters[index])+" <@cross-ref-video-open>$3\\-$4<@cross-ref-video-close>"); // en-dash
			line = line.replaceAll("(?i)Video ([0-9]+)\\. ([0-9]+) "+(Constants.delimeters[index])+" ([0-9]+)\\. ([0-9]+)", "<@cross-ref-video-open>Video $1\\.$2<@cross-ref-video-close> "+(Constants.delimeters[index])+" <@cross-ref-video-open>$3\\.$4<@cross-ref-video-close>"); // simple period
			
			/**
			 * 
			 * space handle left side
			 */
			line = line.replaceAll("(?i)Video\\. ([0-9]+)\\- ([0-9]+) "+(Constants.delimeters[index])+" ([0-9]+)\\-([0-9]+)", "<@cross-ref-video-open>Video\\. $1\\-$2<@cross-ref-video-close> "+(Constants.delimeters[index])+" <@cross-ref-video-open>$3\\-$4<@cross-ref-video-close>"); // simple dash
			line = line.replaceAll("(?i)Video\\. ([0-9]+)\\— ([0-9]+) "+(Constants.delimeters[index])+" ([0-9]+)\\—([0-9]+)", "<@cross-ref-video-open>Video\\. $1\\—$2<@cross-ref-video-close> "+(Constants.delimeters[index])+" <@cross-ref-video-open>$3\\-$4<@cross-ref-video-close>"); // em-dash
			line = line.replaceAll("(?i)Video\\. ([0-9]+)\\– ([0-9]+) "+(Constants.delimeters[index])+" ([0-9]+)\\–([0-9]+)", "<@cross-ref-video-open>Video\\. $1\\–$2<@cross-ref-video-close> "+(Constants.delimeters[index])+" <@cross-ref-video-open>$3\\-$4<@cross-ref-video-close>"); // en-dash
			line = line.replaceAll("(?i)Video\\. ([0-9]+)\\. ([0-9]+) "+(Constants.delimeters[index])+" ([0-9]+)\\.([0-9]+)", "<@cross-ref-video-open>Video\\. $1\\.$2<@cross-ref-video-close> "+(Constants.delimeters[index])+" <@cross-ref-video-open>$3\\.$4<@cross-ref-video-close>"); // simple period
			
			line = line.replaceAll("(?i)Video ([0-9]+)\\- ([0-9]+) "+(Constants.delimeters[index])+" ([0-9]+)\\-([0-9]+)", "<@cross-ref-video-open>Video $1\\-$2<@cross-ref-video-close> "+(Constants.delimeters[index])+" <@cross-ref-video-open>$3\\-$4<@cross-ref-video-close>"); // simple dash
			line = line.replaceAll("(?i)Video ([0-9]+)\\— ([0-9]+) "+(Constants.delimeters[index])+" ([0-9]+)\\—([0-9]+)", "<@cross-ref-video-open>Video $1\\—$2<@cross-ref-video-close> "+(Constants.delimeters[index])+" <@cross-ref-video-open>$3\\-$4<@cross-ref-video-close>"); // em-dash
			line = line.replaceAll("(?i)Video ([0-9]+)\\– ([0-9]+) "+(Constants.delimeters[index])+" ([0-9]+)\\–([0-9]+)", "<@cross-ref-video-open>Video $1\\–$2<@cross-ref-video-close> "+(Constants.delimeters[index])+" <@cross-ref-video-open>$3\\-$4<@cross-ref-video-close>"); // en-dash
			line = line.replaceAll("(?i)Video ([0-9]+)\\. ([0-9]+) "+(Constants.delimeters[index])+" ([0-9]+)\\.([0-9]+)", "<@cross-ref-video-open>Video $1\\.$2<@cross-ref-video-close> "+(Constants.delimeters[index])+" <@cross-ref-video-open>$3\\.$4<@cross-ref-video-close>"); // simple period
			
			/**
			 * 
			 * space handle right side
			 */
			line = line.replaceAll("(?i)Video\\. ([0-9]+)\\-([0-9]+) "+(Constants.delimeters[index])+" ([0-9]+)\\- ([0-9]+)", "<@cross-ref-video-open>Video\\. $1\\-$2<@cross-ref-video-close> "+(Constants.delimeters[index])+" <@cross-ref-video-open>$3\\-$4<@cross-ref-video-close>"); // simple dash
			line = line.replaceAll("(?i)Video\\. ([0-9]+)\\—([0-9]+) "+(Constants.delimeters[index])+" ([0-9]+)\\— ([0-9]+)", "<@cross-ref-video-open>Video\\. $1\\—$2<@cross-ref-video-close> "+(Constants.delimeters[index])+" <@cross-ref-video-open>$3\\-$4<@cross-ref-video-close>"); // em-dash
			line = line.replaceAll("(?i)Video\\. ([0-9]+)\\–([0-9]+) "+(Constants.delimeters[index])+" ([0-9]+)\\– ([0-9]+)", "<@cross-ref-video-open>Video\\. $1\\–$2<@cross-ref-video-close> "+(Constants.delimeters[index])+" <@cross-ref-video-open>$3\\-$4<@cross-ref-video-close>"); // en-dash
			line = line.replaceAll("(?i)Video\\. ([0-9]+)\\.([0-9]+) "+(Constants.delimeters[index])+" ([0-9]+)\\. ([0-9]+)", "<@cross-ref-video-open>Video\\. $1\\.$2<@cross-ref-video-close> "+(Constants.delimeters[index])+" <@cross-ref-video-open>$3\\.$4<@cross-ref-video-close>"); // simple period
			
			line = line.replaceAll("(?i)Video ([0-9]+)\\-([0-9]+) "+(Constants.delimeters[index])+" ([0-9]+)\\- ([0-9]+)", "<@cross-ref-video-open>Video $1\\-$2<@cross-ref-video-close> "+(Constants.delimeters[index])+" <@cross-ref-video-open>$3\\-$4<@cross-ref-video-close>"); // simple dash
			line = line.replaceAll("(?i)Video ([0-9]+)\\—([0-9]+) "+(Constants.delimeters[index])+" ([0-9]+)\\— ([0-9]+)", "<@cross-ref-video-open>Video $1\\—$2<@cross-ref-video-close> "+(Constants.delimeters[index])+" <@cross-ref-video-open>$3\\-$4<@cross-ref-video-close>"); // em-dash
			line = line.replaceAll("(?i)Video ([0-9]+)\\–([0-9]+) "+(Constants.delimeters[index])+" ([0-9]+)\\– ([0-9]+)", "<@cross-ref-video-open>Video $1\\–$2<@cross-ref-video-close> "+(Constants.delimeters[index])+" <@cross-ref-video-open>$3\\-$4<@cross-ref-video-close>"); // en-dash
			line = line.replaceAll("(?i)Video ([0-9]+)\\.([0-9]+) "+(Constants.delimeters[index])+" ([0-9]+)\\. ([0-9]+)", "<@cross-ref-video-open>Video $1\\.$2<@cross-ref-video-close> "+(Constants.delimeters[index])+" <@cross-ref-video-open>$3\\.$4<@cross-ref-video-close>"); // simple period
			
			/**
			 * Electronic
			 * 
			 */
			
			line = line.replaceAll("(?i)Video\\. E([0-9]+)\\-([0-9]+) "+(Constants.delimeters[index])+" E([0-9]+)\\-([0-9]+)", "<@cross-ref-video-open>Video\\. $1\\-$2<@cross-ref-video-close> "+(Constants.delimeters[index])+" <@cross-ref-video-open>$3\\-$4<@cross-ref-video-close>"); // simple dash
			line = line.replaceAll("(?i)Video\\. E([0-9]+)\\—([0-9]+) "+(Constants.delimeters[index])+" E([0-9]+)\\—([0-9]+)", "<@cross-ref-video-open>Video\\. $1\\—$2<@cross-ref-video-close> "+(Constants.delimeters[index])+" <@cross-ref-video-open>$3\\-$4<@cross-ref-video-close>"); // em-dash
			line = line.replaceAll("(?i)Video\\. E([0-9]+)\\–([0-9]+) "+(Constants.delimeters[index])+" E([0-9]+)\\–([0-9]+)", "<@cross-ref-video-open>Video\\. $1\\–$2<@cross-ref-video-close> "+(Constants.delimeters[index])+" <@cross-ref-video-open>$3\\-$4<@cross-ref-video-close>"); // en-dash
			line = line.replaceAll("(?i)Video\\. E([0-9]+)\\.([0-9]+) "+(Constants.delimeters[index])+" E([0-9]+)\\.([0-9]+)", "<@cross-ref-video-open>Video\\. $1\\.$2<@cross-ref-video-close> "+(Constants.delimeters[index])+" <@cross-ref-video-open>$3\\.$4<@cross-ref-video-close>"); // simple period
			
			line = line.replaceAll("(?i)Video E([0-9]+)\\-([0-9]+) "+(Constants.delimeters[index])+" E([0-9]+)\\-([0-9]+)", "<@cross-ref-video-open>Video $1\\-$2<@cross-ref-video-close> "+(Constants.delimeters[index])+" <@cross-ref-video-open>$3\\-$4<@cross-ref-video-close>"); // simple dash
			line = line.replaceAll("(?i)Video E([0-9]+)\\—([0-9]+) "+(Constants.delimeters[index])+" E([0-9]+)\\—([0-9]+)", "<@cross-ref-video-open>Video $1\\—$2<@cross-ref-video-close> "+(Constants.delimeters[index])+" <@cross-ref-video-open>$3\\-$4<@cross-ref-video-close>"); // em-dash
			line = line.replaceAll("(?i)Video E([0-9]+)\\–([0-9]+) "+(Constants.delimeters[index])+" E([0-9]+)\\–([0-9]+)", "<@cross-ref-video-open>Video $1\\–$2<@cross-ref-video-close> "+(Constants.delimeters[index])+" <@cross-ref-video-open>$3\\-$4<@cross-ref-video-close>"); // en-dash
			line = line.replaceAll("(?i)Video E([0-9]+)\\.([0-9]+) "+(Constants.delimeters[index])+" E([0-9]+)\\.([0-9]+)", "<@cross-ref-video-open>Video $1\\.$2<@cross-ref-video-close> "+(Constants.delimeters[index])+" <@cross-ref-video-open>$3\\.$4<@cross-ref-video-close>"); // simple period
			
			/**
			 * 
			 * Electronic space handle both side
			 */
			line = line.replaceAll("(?i)Video\\. E([0-9]+)\\- ([0-9]+) "+(Constants.delimeters[index])+" E([0-9]+)\\- ([0-9]+)", "<@cross-ref-video-open>Video\\. $1\\-$2<@cross-ref-video-close> "+(Constants.delimeters[index])+" <@cross-ref-video-open>$3\\-$4<@cross-ref-video-close>"); // simple dash
			line = line.replaceAll("(?i)Video\\. E([0-9]+)\\— ([0-9]+) "+(Constants.delimeters[index])+" E([0-9]+)\\— ([0-9]+)", "<@cross-ref-video-open>Video\\. $1\\—$2<@cross-ref-video-close> "+(Constants.delimeters[index])+" <@cross-ref-video-open>$3\\-$4<@cross-ref-video-close>"); // em-dash
			line = line.replaceAll("(?i)Video\\. E([0-9]+)\\– ([0-9]+) "+(Constants.delimeters[index])+" E([0-9]+)\\– ([0-9]+)", "<@cross-ref-video-open>Video\\. $1\\–$2<@cross-ref-video-close> "+(Constants.delimeters[index])+" <@cross-ref-video-open>$3\\-$4<@cross-ref-video-close>"); // en-dash
			line = line.replaceAll("(?i)Video\\. E([0-9]+)\\. ([0-9]+) "+(Constants.delimeters[index])+" E([0-9]+)\\. ([0-9]+)", "<@cross-ref-video-open>Video\\. $1\\.$2<@cross-ref-video-close> "+(Constants.delimeters[index])+" <@cross-ref-video-open>$3\\.$4<@cross-ref-video-close>"); // simple period
			
			line = line.replaceAll("(?i)Video E([0-9]+)\\- ([0-9]+) "+(Constants.delimeters[index])+" E([0-9]+)\\- ([0-9]+)", "<@cross-ref-video-open>Video $1\\-$2<@cross-ref-video-close> "+(Constants.delimeters[index])+" <@cross-ref-video-open>$3\\-$4<@cross-ref-video-close>"); // simple dash
			line = line.replaceAll("(?i)Video E([0-9]+)\\— ([0-9]+) "+(Constants.delimeters[index])+" E([0-9]+)\\— ([0-9]+)", "<@cross-ref-video-open>Video $1\\—$2<@cross-ref-video-close> "+(Constants.delimeters[index])+" <@cross-ref-video-open>$3\\-$4<@cross-ref-video-close>"); // em-dash
			line = line.replaceAll("(?i)Video E([0-9]+)\\– ([0-9]+) "+(Constants.delimeters[index])+" E([0-9]+)\\– ([0-9]+)", "<@cross-ref-video-open>Video $1\\–$2<@cross-ref-video-close> "+(Constants.delimeters[index])+" <@cross-ref-video-open>$3\\-$4<@cross-ref-video-close>"); // en-dash
			line = line.replaceAll("(?i)Video E([0-9]+)\\. ([0-9]+) "+(Constants.delimeters[index])+" E([0-9]+)\\. ([0-9]+)", "<@cross-ref-video-open>Video $1\\.$2<@cross-ref-video-close> "+(Constants.delimeters[index])+" <@cross-ref-video-open>$3\\.$4<@cross-ref-video-close>"); // simple period
			
			/**
			 * 
			 * Electronic space handle left side
			 */
			line = line.replaceAll("(?i)Video\\. E([0-9]+)\\- ([0-9]+) "+(Constants.delimeters[index])+" E([0-9]+)\\-([0-9]+)", "<@cross-ref-video-open>Video\\. $1\\-$2<@cross-ref-video-close> "+(Constants.delimeters[index])+" <@cross-ref-video-open>$3\\-$4<@cross-ref-video-close>"); // simple dash
			line = line.replaceAll("(?i)Video\\. E([0-9]+)\\— ([0-9]+) "+(Constants.delimeters[index])+" E([0-9]+)\\—([0-9]+)", "<@cross-ref-video-open>Video\\. $1\\—$2<@cross-ref-video-close> "+(Constants.delimeters[index])+" <@cross-ref-video-open>$3\\-$4<@cross-ref-video-close>"); // em-dash
			line = line.replaceAll("(?i)Video\\. E([0-9]+)\\– ([0-9]+) "+(Constants.delimeters[index])+" E([0-9]+)\\–([0-9]+)", "<@cross-ref-video-open>Video\\. $1\\–$2<@cross-ref-video-close> "+(Constants.delimeters[index])+" <@cross-ref-video-open>$3\\-$4<@cross-ref-video-close>"); // en-dash
			line = line.replaceAll("(?i)Video\\. E([0-9]+)\\. ([0-9]+) "+(Constants.delimeters[index])+" E([0-9]+)\\.([0-9]+)", "<@cross-ref-video-open>Video\\. $1\\.$2<@cross-ref-video-close> "+(Constants.delimeters[index])+" <@cross-ref-video-open>$3\\.$4<@cross-ref-video-close>"); // simple period
			
			line = line.replaceAll("(?i)Video E([0-9]+)\\- ([0-9]+) "+(Constants.delimeters[index])+" E([0-9]+)\\-([0-9]+)", "<@cross-ref-video-open>Video $1\\-$2<@cross-ref-video-close> "+(Constants.delimeters[index])+" <@cross-ref-video-open>$3\\-$4<@cross-ref-video-close>"); // simple dash
			line = line.replaceAll("(?i)Video E([0-9]+)\\— ([0-9]+) "+(Constants.delimeters[index])+" E([0-9]+)\\—([0-9]+)", "<@cross-ref-video-open>Video $1\\—$2<@cross-ref-video-close> "+(Constants.delimeters[index])+" <@cross-ref-video-open>$3\\-$4<@cross-ref-video-close>"); // em-dash
			line = line.replaceAll("(?i)Video E([0-9]+)\\– ([0-9]+) "+(Constants.delimeters[index])+" E([0-9]+)\\–([0-9]+)", "<@cross-ref-video-open>Video $1\\–$2<@cross-ref-video-close> "+(Constants.delimeters[index])+" <@cross-ref-video-open>$3\\-$4<@cross-ref-video-close>"); // en-dash
			line = line.replaceAll("(?i)Video E([0-9]+)\\. ([0-9]+) "+(Constants.delimeters[index])+" E([0-9]+)\\.([0-9]+)", "<@cross-ref-video-open>Video $1\\.$2<@cross-ref-video-close> "+(Constants.delimeters[index])+" <@cross-ref-video-open>$3\\.$4<@cross-ref-video-close>"); // simple period
			
			/**
			 * 
			 * Electronic space handle right side
			 */
			line = line.replaceAll("(?i)Video\\. E([0-9]+)\\-([0-9]+) "+(Constants.delimeters[index])+" E([0-9]+)\\- ([0-9]+)", "<@cross-ref-video-open>Video\\. $1\\-$2<@cross-ref-video-close> "+(Constants.delimeters[index])+" <@cross-ref-video-open>$3\\-$4<@cross-ref-video-close>"); // simple dash
			line = line.replaceAll("(?i)Video\\. E([0-9]+)\\—([0-9]+) "+(Constants.delimeters[index])+" E([0-9]+)\\— ([0-9]+)", "<@cross-ref-video-open>Video\\. $1\\—$2<@cross-ref-video-close> "+(Constants.delimeters[index])+" <@cross-ref-video-open>$3\\-$4<@cross-ref-video-close>"); // em-dash
			line = line.replaceAll("(?i)Video\\. E([0-9]+)\\–([0-9]+) "+(Constants.delimeters[index])+" E([0-9]+)\\– ([0-9]+)", "<@cross-ref-video-open>Video\\. $1\\–$2<@cross-ref-video-close> "+(Constants.delimeters[index])+" <@cross-ref-video-open>$3\\-$4<@cross-ref-video-close>"); // en-dash
			line = line.replaceAll("(?i)Video\\. E([0-9]+)\\.([0-9]+) "+(Constants.delimeters[index])+" E([0-9]+)\\. ([0-9]+)", "<@cross-ref-video-open>Video\\. $1\\.$2<@cross-ref-video-close> "+(Constants.delimeters[index])+" <@cross-ref-video-open>$3\\.$4<@cross-ref-video-close>"); // simple period
			
			line = line.replaceAll("(?i)Video E([0-9]+)\\-([0-9]+) "+(Constants.delimeters[index])+" E([0-9]+)\\- ([0-9]+)", "<@cross-ref-video-open>Video $1\\-$2<@cross-ref-video-close> "+(Constants.delimeters[index])+" <@cross-ref-video-open>$3\\-$4<@cross-ref-video-close>"); // simple dash
			line = line.replaceAll("(?i)Video E([0-9]+)\\—([0-9]+) "+(Constants.delimeters[index])+" E([0-9]+)\\— ([0-9]+)", "<@cross-ref-video-open>Video $1\\—$2<@cross-ref-video-close> "+(Constants.delimeters[index])+" <@cross-ref-video-open>$3\\-$4<@cross-ref-video-close>"); // em-dash
			line = line.replaceAll("(?i)Video E([0-9]+)\\–([0-9]+) "+(Constants.delimeters[index])+" E([0-9]+)\\– ([0-9]+)", "<@cross-ref-video-open>Video $1\\–$2<@cross-ref-video-close> "+(Constants.delimeters[index])+" <@cross-ref-video-open>$3\\-$4<@cross-ref-video-close>"); // en-dash
			line = line.replaceAll("(?i)Video E([0-9]+)\\.([0-9]+) "+(Constants.delimeters[index])+" E([0-9]+)\\. ([0-9]+)", "<@cross-ref-video-open>Video $1\\.$2<@cross-ref-video-close> "+(Constants.delimeters[index])+" <@cross-ref-video-open>$3\\.$4<@cross-ref-video-close>"); // simple period

			
			if (line.contains(""))	isFound = true;
			if ( ! isFound ) {
				
				line = line.replaceAll("(?i)Video\\. ([0-9]+) "+(Constants.delimeters[index])+" ([0-9]+)", "<@cross-ref-video-open>Video\\. $1<@cross-ref-video-close> "+(Constants.delimeters[index])+" <@cross-ref-video-open>$2<@cross-ref-video-close>"); // simple dash
				line = line.replaceAll("(?i)Video ([0-9]+) "+(Constants.delimeters[index])+" ([0-9]+)", "<@cross-ref-video-open>Video $1<@cross-ref-video-close> "+(Constants.delimeters[index])+" <@cross-ref-video-open>$2<@cross-ref-video-close>"); // simple dash
				
				line = line.replaceAll("(?i)Video\\. E([0-9]+) "+(Constants.delimeters[index])+" ([0-9]+)", "<@cross-ref-video-open>Video\\. $1<@cross-ref-video-close> "+(Constants.delimeters[index])+" <@cross-ref-video-open>$2<@cross-ref-video-close>"); // simple dash
				line = line.replaceAll("(?i)Video E([0-9]+) "+(Constants.delimeters[index])+" ([0-9]+)", "<@cross-ref-video-open>Video $1<@cross-ref-video-close> "+(Constants.delimeters[index])+" <@cross-ref-video-open>$2<@cross-ref-video-close>"); // simple dash
			}
		}
		return line;
	}

	private String handleCrossRefsOccurances(String line) throws Exception {
		
		/**
		 * figure
		 * 
		 */
		
//		Fig\. ([0-9]+)\.([0-9]+)//-
		
		line = figureCrossMarking(line);
		if (line.contains("consistently proved to be a stronger predictor of outcomes than creatinine"))
			logger.debug(line);
		
		String mainLine = line;
		while (mainLine.contains("<@cross-ref-fig-open>")) {
			
			int indexCrossRefOpen = mainLine.indexOf("<@cross-ref-fig-open>");
			int indexCrossRefClose = mainLine.indexOf("<@cross-ref-fig-close>");
			
			if ((indexCrossRefClose) > (indexCrossRefOpen)) {
				
				String floatLabel = mainLine.substring((mainLine.indexOf("<@cross-ref-fig-open>") + "<@cross-ref-fig-open>".length()), mainLine.indexOf("<@cross-ref-fig-close>"));
				
//				String itemNum = miscUtility.fetchItemNumber(floatLabel);
//				if (Constants.calloutFigureFixingNote .contains(itemNum) == false) {
//					line = line + "\n" + "@alert:<Insert " + floatLabel.toLowerCase() +" here>";
//					Constants.calloutFigureFixingNote.add(itemNum);
//				}
				
				String tagText = "<@cross-ref-fig-open>" + floatLabel + "<@cross-ref-fig-close>";
				String prefixMainLine = mainLine.substring(0, mainLine.indexOf(tagText));
				String suffixMainLine = mainLine.substring(mainLine.indexOf(tagText) + (tagText.length()));
				
				mainLine = prefixMainLine + suffixMainLine;
			}
		}
		/**
		 * table
		 * 
		 */
		
		line = tableCrossMarking(line);
		
		mainLine = line;
		while (mainLine.contains("<@cross-ref-table-open>")) {
			
			int indexCrossRefOpen = mainLine.indexOf("<@cross-ref-table-open>");
			int indexCrossRefClose = mainLine.indexOf("<@cross-ref-table-close>");
			if ((indexCrossRefClose) > (indexCrossRefOpen)) {
				
				String floatLabel = mainLine.substring((mainLine.indexOf("<@cross-ref-table-open>") + "<@cross-ref-table-open>".length()), mainLine.indexOf("<@cross-ref-table-close>"));
				
//				String itemNum = miscUtility.fetchItemNumber(floatLabel);
//				if (Constants.calloutTableFixingNote .contains(itemNum) == false) {
//					line = line + "\n" + "@alert:<Insert " + floatLabel.toLowerCase()+" here>";
//					Constants.calloutTableFixingNote.add(itemNum);
//				}
				
				String tagText = "<@cross-ref-table-open>" + floatLabel + "<@cross-ref-table-close>";
				String prefixMainLine = mainLine.substring(0, mainLine.indexOf(tagText));
				String suffixMainLine = mainLine.substring(mainLine.indexOf(tagText) + (tagText.length()));
				
				mainLine = prefixMainLine + suffixMainLine;
			}
		}
		
		/**
		 * box
		 * 
		 */
		
		line = boxCrossMarking(line);
		
		mainLine = line;
		while (mainLine.contains("<@cross-ref-box-open>")) {
			
			int indexCrossRefOpen = mainLine.indexOf("<@cross-ref-box-open>");
			int indexCrossRefClose = mainLine.indexOf("<@cross-ref-box-close>");
			if ((indexCrossRefClose) > (indexCrossRefOpen)) {
				
				String floatLabel = mainLine.substring((mainLine.indexOf("<@cross-ref-box-open>") + "<@cross-ref-box-open>".length()), mainLine.indexOf("<@cross-ref-box-close>"));
				
//				String itemNum = miscUtility.fetchItemNumber(floatLabel);
//				if (Constants.calloutBoxFixingNote .contains(itemNum) == false) {
//					line = line + "\n" + "@alert:<Insert " + floatLabel.toLowerCase()+" here>";
//					Constants.calloutBoxFixingNote.add(itemNum);
//				}
				
				String tagText = "<@cross-ref-box-open>" + floatLabel + "<@cross-ref-box-close>";
				String prefixMainLine = mainLine.substring(0, mainLine.indexOf(tagText));
				String suffixMainLine = mainLine.substring(mainLine.indexOf(tagText) + (tagText.length()));
				
				mainLine = prefixMainLine + suffixMainLine;
			}
		}
		
		/**
		 * video
		 * 
		 */
		
		line = videoCrossMarking(line);
		
		mainLine = line;
		while (mainLine.contains("<@cross-ref-video-open>")) {
			
			int indexCrossRefOpen = mainLine.indexOf("<@cross-ref-video-open>");
			int indexCrossRefClose = mainLine.indexOf("<@cross-ref-video-close>");
			if ((indexCrossRefClose) > (indexCrossRefOpen)) {
				
				String floatLabel = mainLine.substring((mainLine.indexOf("<@cross-ref-video-open>") + "<@cross-ref-video-open>".length()), mainLine.indexOf("<@cross-ref-video-close>"));
				
//				String itemNum = miscUtility.fetchItemNumber(floatLabel);
//				if (Constants.calloutVideoFixingNote .contains(itemNum) == false) {
//					line = line + "\n" + "@alert:<Insert " + floatLabel.toLowerCase()+" here>";
//					Constants.calloutVideoFixingNote.add(itemNum);
//				}
				
				String tagText = "<@cross-ref-video-open>" + floatLabel + "<@cross-ref-video-close>";
				String prefixMainLine = mainLine.substring(0, mainLine.indexOf(tagText));
				String suffixMainLine = mainLine.substring(mainLine.indexOf(tagText) + (tagText.length()));
				
				mainLine = prefixMainLine + suffixMainLine;
			}
		}
		
		
		
		line = line.replaceAll("(?i)Chapter ([0-9]+)", "<@inter-ref-open>Chapter $1<@inter-ref-close>");
		
		
		//float item order checking
		if (line.contains("<@cross-ref"))
			taFloatItemOrdering.crossRefsCallOutCheckLog(line);
		
//		line = line.replace("<@inter-ref-close>", "<@$p>");
//		line = line.replace("<@cross-ref-fig-close>", "<@$p>");
//		line = line.replace("<@cross-ref-box-close>", "<@$p>");
//		line = line.replace("<@cross-ref-table-close>", "<@$p>");
//		line = line.replace("<@cross-ref-video-close>", "<@$p>");
		
		
//		line = cleanCrossRefWithButtonizeFunction(line);
		
		return line;
	}
	
	private String cleanCrossRefWithButtonizeFunction(String charText) {
		
		charText = charText.replace("<@bold><@cross-ref-fig-open>", "<@bold-cross-ref-fig>");
		charText = charText.replace("<@bold><@cross-ref-table-open>", "<@bold-cross-ref-table>");
		charText = charText.replace("<@bold><@cross-ref-box-open>", "<@bold-cross-ref-box>");
		charText = charText.replace("<@bold><@cross-ref-video-open>", "<@bold-cross-ref-video>");
		
		charText = charText.replace("<@italic><@cross-ref-fig-open>", "<@italic-cross-ref-fig>");
		charText = charText.replace("<@italic><@cross-ref-table-open>", "<@italic-cross-ref-table>");
		charText = charText.replace("<@italic><@cross-ref-box-open>", "<@italic-cross-ref-box>");
		charText = charText.replace("<@italic><@cross-ref-video-open>", "<@italic-cross-ref-video>");
		
		
		charText = charText.replace("<@caps><@cross-ref-fig-open>", "<@caps-cross-ref-fig>");
		charText = charText.replace("<@caps><@cross-ref-table-open>", "<@caps-cross-ref-table>");
		charText = charText.replace("<@caps><@cross-ref-box-open>", "<@caps-cross-ref-box>");
		charText = charText.replace("<@caps><@cross-ref-video-open>", "<@caps-cross-ref-video>");
		
		
		charText = charText.replace("<@smallcaps><@cross-ref-fig-open>", "<@smallcaps-cross-ref-fig>");
		charText = charText.replace("<@smallcaps><@cross-ref-table-open>", "<@smallcaps-cross-ref-table>");
		charText = charText.replace("<@smallcaps><@cross-ref-box-open>", "<@smallcaps-cross-ref-box>");
		charText = charText.replace("<@smallcaps><@cross-ref-video-open>", "<@smallcaps-cross-ref-video>");
		
		
		charText = charText.replace("<@subscript><@cross-ref-fig-open>", "<@subscript-cross-ref-fig>");
		charText = charText.replace("<@subscript><@cross-ref-table-open>", "<@subscript-cross-ref-table>");
		charText = charText.replace("<@subscript><@cross-ref-box-open>", "<@subscript-cross-ref-box>");
		charText = charText.replace("<@subscript><@cross-ref-video-open>", "<@subscript-cross-ref-video>");
		
		
		charText = charText.replace("<@superscript><@cross-ref-fig-open>", "<@superscript-cross-ref-fig>");
		charText = charText.replace("<@superscript><@cross-ref-table-open>", "<@superscript-cross-ref-table>");
		charText = charText.replace("<@superscript><@cross-ref-box-open>", "<@superscript-cross-ref-box>");
		charText = charText.replace("<@superscript><@cross-ref-video-open>", "<@superscript-cross-ref-video>");
		
		
		
		charText = charText.replace("<@underline><@cross-ref-fig-open>", "<@underline-cross-ref-fig>");
		charText = charText.replace("<@underline><@cross-ref-table-open>", "<@underline-cross-ref-table>");
		charText = charText.replace("<@underline><@cross-ref-box-open>", "<@underline-cross-ref-box>");
		charText = charText.replace("<@underline><@cross-ref-video-open>", "<@underline-cross-ref-video>");
		
		charText = charText.replace("<@$p><@$p>", "<@$p>");
		
		return charText;
	}

	/**
	 * soft-Enter Handling
	 * 
	 */
	
	private void softEnterHandling(String path) {
		
		BufferedReader bufferedReader = null;
		try {
			bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(path), StandardCharsets.UTF_8));
			
			String line = "";
//			Integer paraHeadIndex = 0;
			StringBuffer buffer = new StringBuffer();
			
			
			if (Constants.chapOutlineBuff.toString().length() > 0) {
				
				String outlineText = "";
				outlineText = "<1 hd>CHAPTER OUTLINE\n";
				outlineText = outlineText + Constants.chapOutlineBuff.toString();
				
				write(outlineText, Constants.outputPath + "/Resource/" + (Constants.fileName) + "_Outline.txt", false);
			}
			
			while((line = bufferedReader.readLine()) != null) {
				
				
				if (Constants.taSettingForUser.isConvertReferencesEndnotes_to_StandardReferenceCitations()) {
				
					if ((line.contains("ADDIN EN.CITE <EndNote>")) & (line.contains("</EndNote>"))) {
						if ((line.indexOf("ADDIN EN.CITE <EndNote>")) < (line.indexOf("</EndNote>"))) {
							
							String prefixData = "", suffixData = "";
							prefixData = line.substring(0, line.indexOf("ADDIN EN.CITE <EndNote>"));
							suffixData = line.substring(line.indexOf("</EndNote>") + ("</EndNote>".length()));
							
							line = prefixData + suffixData;
						}
					}
				}
//				String lineParaStyle = "";
//				lineParaStyle = fetchParaStyle(line);
//				
//				if ((lineParaStyle.toLowerCase().contains("hd")) & (paraHeadIndex == 2)) 
//					buffer.append(outlineText);
//				
//				if (lineParaStyle.toLowerCase().contains("hd"))	paraHeadIndex ++;
//				
//				String lineBreak = line;
//				while (lineBreak.contains("<@SOFT-BREAK/>")) {
//				
//					String previousOpentag = "";//<@bold-open>
//					String prefixdata = lineBreak.substring(lineBreak.indexOf("@"+(lineParaStyle)+":"), lineBreak.lastIndexOf("<@SOFT-BREAK/>"));
//					
//					if (prefixdata.contains("-open")) {
//						
//						String lastOpenTag = prefixdata.substring(prefixdata.lastIndexOf("<@"), prefixdata.lastIndexOf("-open>"));
//					}
//					
//					line = line.replace("<@SOFT-BREAK/><@$p>", "<@SOFT-BREAK/>");
//					line = line.replace("<@SOFT-BREAK/>", "\n@"+lineParaStyle+":");
//				}
				
				
				
				buffer.append(line+"\n");
			}
			
			String data = buffer.toString();
			
			new FileWriterImpl().write(data, path, false);
			
		}catch(Exception exception) {	exception.printStackTrace();	}
		finally {
			if (bufferedReader!= null) {
				try {
					bufferedReader.close();
				}catch(Exception exception2) {	System.out.println(exception2.getMessage());	}
			}
		}
	}

	public String removeMarginPading(String lineText) {
		
		String firstLineIndentText = "0.0";
		if (lineText.startsWith("<@begin-para-first-line-indent-open>")) {
			firstLineIndentText = lineText.substring((lineText.indexOf("<@begin-para-first-line-indent-open>")) + ("<@begin-para-first-line-indent-open>".length()), lineText.indexOf("<@begin-para-first-line-indent-close>"));
			lineText = lineText.replace("<@begin-para-first-line-indent-open>" + firstLineIndentText + "<@begin-para-first-line-indent-close>", "");
		}
		
		String leftIndentText = "0.0";
		if (lineText.contains("<@begin-para-left-indent-open>")) {
			leftIndentText = lineText.substring((lineText.indexOf("<@begin-para-left-indent-open>")) + ("<@begin-para-left-indent-open>".length()), lineText.indexOf("<@begin-para-left-indent-close>"));
			lineText = lineText.replace("<@begin-para-left-indent-open>" + leftIndentText + "<@begin-para-left-indent-close>", "");
		}
		return lineText;
	}
	
	private String fetchParaStyle(String lineText) {
		
		String paraStyleName = "";
		lineText = removeMarginPading(lineText);
		
		if ((lineText.startsWith("@")) && (lineText.indexOf(":") > 0 )) {
			
			paraStyleName = lineText.substring(lineText.indexOf("@"), lineText.indexOf(":") + 1);
			lineText = lineText.replace(paraStyleName, "");
			paraStyleName = paraStyleName.replace("@", "").replace(":", "");
			paraStyleName = paraStyleName.replace("™", ":");
		}
		return paraStyleName;
	}

	/**
	 * check if special char found 
	 * if yes then return special symbol else return ""
	 */
	private String convertUnicodeToSymbol(JSONArray array, String paraText) throws Exception {
		
        String symbol = "";
        String expresionObj = "";
        
        for (int index = 0; index < array.size(); index ++) {
        	
            JSONObject obj = (JSONObject) array.get(index);
            expresionObj = (String) obj.get("hexadecimalUniCode");
            expresionObj = expresionObj.replace("\\", "");
            
            if (paraText.toLowerCase().contains(expresionObj.toLowerCase())) {
            	
            	symbol = (String) obj.get("characterSymbol");
                expresionObj = expresionObj.replace("u", "U");
                
                paraText = paraText.replace("<\\#"+expresionObj+">", symbol);
            }
        }
        return paraText;
    }
	
	
	/**
	private Boolean isAlphaInLine(String line) {
		
        String[] smalAlpha = {"q","w","e","r","t","y","u","i","o","p","a","s","d","f","g","h","j","k","l","z","x","c","v","b","n","m"};
        //String[] smalnum = {"1", "2", "3", "4", "5", "6", "7" ,"8", "9", "0", "~", "!", "@", "#", "$", "%", "^", "&", "*", "(", ")", "_", "+", "=", ":", "'", ",", ".", "/", "\"", "<", ">", "?", "\\"};
        Boolean valid = false;
        
        for (String str : smalAlpha) {
        	
        	if (line.toLowerCase().contains(str)) {
        		valid = true;
        		break;
        	}
        }
        
        return valid;
	}
	*/
	public void setMiscUtility(MiscUtility miscUtility) {
		this.miscUtility = miscUtility;
	}

	public void setTaFloatItemOrdering(TAFloatItemOrdering taFloatItemOrdering) {
		this.taFloatItemOrdering = taFloatItemOrdering;
	}
}
