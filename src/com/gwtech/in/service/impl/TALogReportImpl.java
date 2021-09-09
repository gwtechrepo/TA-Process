package com.gwtech.in.service.impl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import com.gwtech.in.service.FileWriterI;
import com.gwtech.in.service.TALogReport;
import com.gwtech.in.utils.Constants;
import com.gwtech.in.utils.MiscUtility;

public class TALogReportImpl implements TALogReport {
	
	private static final Logger logger = Logger.getLogger(TALogReportImpl.class);
	private FileWriterI fileWriterI;
	private MiscUtility miscUtility;
	private ConsecutiveFlow consecutiveFlow;
	
	@Override
	public void floatItemsReport() {
		
		fileWriterI.write("\n", Constants.outputPath + "/Log-Report/float-log/float.txt", true);
		fileWriterI.write("**********************float-main**********************", Constants.outputPath + "/Log-Report/float-log/float.txt", true);
		fileWriterI.write("\n", Constants.outputPath + "/Log-Report/float-log/float.txt", true);
		
		
		fileWriterI.write("\n", Constants.outputPath + "/Log-Report/float-log/consecutive.txt", true);
		fileWriterI.write("**********************float-callout**********************", Constants.outputPath + "/Log-Report/float-log/consecutive.txt", true);
		fileWriterI.write("\n", Constants.outputPath + "/Log-Report/float-log/consecutive.txt", true);
		
		String[] isMissingBoxFloatCon = boxCalloutLog();
		String[] isMissingFigFloatCon = figureCalloutLog();
		String[] isMissingTableFloatCon = tableCalloutLog();
		String[] isMissingVideoFloatCon = videoCalloutLog();
		
		
		fileWriterI.write("\n", Constants.outputPath + "/Log-Report/float-log/float.txt", true);
		fileWriterI.write("**********************cross-ref**********************", Constants.outputPath + "/Log-Report/float-log/float.txt", true);
		fileWriterI.write("\n", Constants.outputPath + "/Log-Report/float-log/float.txt", true);
		
		fileWriterI.write("\n", Constants.outputPath + "/Log-Report/float-log/consecutive.txt", true);
		fileWriterI.write("**********************float-main**********************", Constants.outputPath + "/Log-Report/float-log/consecutive.txt", true);
		fileWriterI.write("\n", Constants.outputPath + "/Log-Report/float-log/consecutive.txt", true);
		
		String[] isMissingBoxConFloat = boxConsecutiveFloatLabels();
		String[] isMissingFigureConFloat = figureConsecutiveFloatLabels();
		String[] isMissingTableConFloat = tableConsecutiveFloatLabels();
		String[] isMissingVideoConFloat = videoConsecutiveFloatLabels();
		
		if ( ! (Boolean.parseBoolean(isMissingBoxFloatCon[0])) &! (Boolean.parseBoolean(isMissingFigFloatCon[0])) &! (Boolean.parseBoolean(isMissingTableFloatCon[0])) &! (Boolean.parseBoolean(isMissingVideoFloatCon[0]))  
			&! (Boolean.parseBoolean(isMissingBoxConFloat[0])) &! (Boolean.parseBoolean(isMissingFigureConFloat[0])) &! (Boolean.parseBoolean(isMissingTableConFloat[0])) &! (Boolean.parseBoolean(isMissingVideoConFloat[0]))) {
			
			File file = new File(Constants.outputPath + "/Log-Report/float-log/float.txt");
			file.delete();
		}
		if ( ! (Boolean.parseBoolean(isMissingBoxFloatCon[1])) &! (Boolean.parseBoolean(isMissingFigFloatCon[1])) &! (Boolean.parseBoolean(isMissingTableFloatCon[1])) &! (Boolean.parseBoolean(isMissingVideoFloatCon[1]))  
				&! (Boolean.parseBoolean(isMissingBoxConFloat[1])) &! (Boolean.parseBoolean(isMissingFigureConFloat[1])) &! (Boolean.parseBoolean(isMissingTableConFloat[1])) &! (Boolean.parseBoolean(isMissingVideoConFloat[1]))) {
			
			File file = new File(Constants.outputPath + "/Log-Report/float-log/consecutive.txt");
			file.delete();
		}
	}
	
	
	/**
	 * videoFloatLabels
	 * 
	 */
	private String[] videoConsecutiveFloatLabels() {
		
		BufferedReader bufferedReader = null;
		String[] isMissing = {"false", "false"};
		
		File file = new File(Constants.outputPath + "/Log-Report/float-item/video-main.txt");
		if (file.exists())
		try {
			
			FileInputStream fileInputStream = new FileInputStream(file);
			InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream, "UTF-8");
			bufferedReader = new BufferedReader(inputStreamReader);
			
			String line = "", mainLine = "";
			List<Integer> consecutiveArray = new ArrayList<Integer>(0);
			
			while( (line = bufferedReader.readLine()) != null ) {
				
				if (line.length() > 0) {
					
					mainLine = line;
					line = fileWriterI.videoCrossMarking(line);
					
					int indexCrossRefOpen = line.indexOf("<@cross-ref-video-open>");
					int indexCrossRefClose = line.indexOf("<@cross-ref-video-close>");
					if ((indexCrossRefClose) > (indexCrossRefOpen)) {
					
						String boxLabel = line.substring((line.indexOf("<@cross-ref-video-open>") + "<@cross-ref-video-open>".length()), line.indexOf("<@cross-ref-video-close>"));
						
						String itemNum = "";
						
						boxLabel = miscUtility.removeStartEndSpaces(boxLabel);
						
						if (boxLabel.endsWith("."))
							boxLabel = boxLabel.substring(0, (boxLabel.lastIndexOf(".")));
						
						if (boxLabel.lastIndexOf(".") > 0)
							itemNum = boxLabel.substring(boxLabel.lastIndexOf(".") + 1);
						else
							itemNum = boxLabel.substring(boxLabel.lastIndexOf(" ") + 1);
						
						itemNum = miscUtility.removeStartEndSpaces(itemNum);
						
						boolean isValid = checkEntryInCalloutFile(itemNum, Constants.outputPath + "/Log-Report/float-item/video-callout.txt", "video");
						
						if (! isValid) {
							
							fileWriterI.write("{CALLOUT VIDEO MISSING}=" + mainLine + "\n", Constants.outputPath + "/Log-Report/float-log/float.txt", true);
							isMissing[0] = "true";
						}						
						
						if (itemNum.contains("-"))	itemNum = itemNum.substring(itemNum.lastIndexOf("-") + 1);
						if (itemNum.contains("—"))	itemNum = itemNum.substring(itemNum.lastIndexOf("—") + 1);
						if (itemNum.contains("–"))	itemNum = itemNum.substring(itemNum.lastIndexOf("–") + 1);
						if (itemNum.contains("–"))	itemNum = itemNum.substring(itemNum.lastIndexOf("–") + 1);
						
						if (miscUtility.isNumber(itemNum)) {
							
							Integer val = Integer.parseInt(itemNum);
							consecutiveArray.add(val);
						}
					}
				}
			}
			
			Integer[] array = new Integer[consecutiveArray.size()];
			
			if (array.length > 0) {
				
				isMissing[1] = "true";
				
				for(int i = 0; i < consecutiveArray.size(); i++) array[i] = consecutiveArray.get(i);
				List<String> videoNonConsecutiveList = consecutiveFlow.fetchNonConsecutiveRanges(array);
				List<String> videoConsecutiveList = consecutiveFlow.fetchConsecutiveRanges(array);
				
				if (videoConsecutiveList.size() > 0) {
					
					fileWriterI.write("\n", Constants.outputPath + "/Log-Report/float-log/consecutive.txt", true);
					fileWriterI.write("Video Consecutive entry\n", Constants.outputPath + "/Log-Report/float-log/consecutive.txt", true);
					
					videoConsecutiveList.forEach(object -> {
						fileWriterI.write(object + "\n", Constants.outputPath + "/Log-Report/float-log/consecutive.txt", true);
					});
				}
				
				
				if (videoNonConsecutiveList.size() > 0) {
					
					fileWriterI.write("Video Non-Consecutive entry\n", Constants.outputPath + "/Log-Report/float-log/consecutive.txt", true);
					
					videoNonConsecutiveList.forEach(object -> {
						fileWriterI.write("<Video-Callout-Entry> "+object + "\n", Constants.outputPath + "/Log-Report/float-log/consecutive.txt", true);
					});
					fileWriterI.write("\n", Constants.outputPath + "/Log-Report/float-log/consecutive.txt", true);
				}
			}
			
		} catch(Exception exception) {	logger.error(exception.getMessage(), exception);	}
		finally {
			if (bufferedReader != null)
				try{	bufferedReader.close();	}catch(Exception ext) {	logger.error(ext.getMessage(), ext);	}
		}
		return isMissing;
	}
	
	/**
	 * tableFloatLabels
	 * 
	 */
	private String[] tableConsecutiveFloatLabels() {
		
		BufferedReader bufferedReader = null;
		String[] isMissing = {"false", "false"};
		
		File file = new File(Constants.outputPath + "/Log-Report/float-item/table-main.txt");
		if (file.exists())
		try {
			
			FileInputStream fileInputStream = new FileInputStream(file);
			InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream, "UTF-8");
			bufferedReader = new BufferedReader(inputStreamReader);
			
			String line = "", mainLine = "";
			List<Integer> consecutiveArray = new ArrayList<Integer>(0);
			
			while( (line = bufferedReader.readLine()) != null ) {
				
				if (line.length() > 0) {
					
					mainLine = line;
					line = fileWriterI.tableCrossMarking(line);
					
					int indexCrossRefOpen = line.indexOf("<@cross-ref-table-open>");
					int indexCrossRefClose = line.indexOf("<@cross-ref-table-close>");
					if ((indexCrossRefClose) > (indexCrossRefOpen)) {
					
						String boxLabel = line.substring((line.indexOf("<@cross-ref-table-open>") + "<@cross-ref-table-open>".length()), line.indexOf("<@cross-ref-table-close>"));
						
						String itemNum = "";
						
						boxLabel = miscUtility.removeStartEndSpaces(boxLabel);
						
						if (boxLabel.endsWith("."))
							boxLabel = boxLabel.substring(0, (boxLabel.lastIndexOf(".")));
						
						if (boxLabel.lastIndexOf(".") > 0)
							itemNum = boxLabel.substring(boxLabel.lastIndexOf(".") + 1);
						else
							itemNum = boxLabel.substring(boxLabel.lastIndexOf(" ") + 1);
						
						itemNum = miscUtility.removeStartEndSpaces(itemNum);
						
						boolean isValid = checkEntryInCalloutFile(itemNum, Constants.outputPath + "/Log-Report/float-item/table-callout.txt", "table");
						
						if (! isValid) {
							
							fileWriterI.write("{CALLOUT TABLE MISSING}=" + mainLine + "\n", Constants.outputPath + "/Log-Report/float-log/float.txt", true);
							isMissing[0] = "true";
						}						
						if (itemNum.contains("-"))	itemNum = itemNum.substring(itemNum.lastIndexOf("-") + 1);
						if (itemNum.contains("—"))	itemNum = itemNum.substring(itemNum.lastIndexOf("—") + 1);
						if (itemNum.contains("–"))	itemNum = itemNum.substring(itemNum.lastIndexOf("–") + 1);
						if (itemNum.contains("–"))	itemNum = itemNum.substring(itemNum.lastIndexOf("–") + 1);
						
						if (miscUtility.isNumber(itemNum)) {
							
							Integer val = Integer.parseInt(itemNum);
							consecutiveArray.add(val);
						}
					}
				}
			}
			
			Integer[] array = new Integer[consecutiveArray.size()];
			
			if (array.length > 1) {
				
				isMissing[1] = "true";
				
				for(int i = 0; i < consecutiveArray.size(); i++) array[i] = consecutiveArray.get(i);
				List<String> tableNonConsecutiveList = consecutiveFlow.fetchNonConsecutiveRanges(array);
				List<String> tableConsecutiveList = consecutiveFlow.fetchConsecutiveRanges(array);
				
				if (tableConsecutiveList.size() > 0) {
					
					fileWriterI.write("\n", Constants.outputPath + "/Log-Report/float-log/consecutive.txt", true);
					fileWriterI.write("Table Consecutive entry\n", Constants.outputPath + "/Log-Report/float-log/consecutive.txt", true);
					
					tableConsecutiveList.forEach(object -> {
						fileWriterI.write(object + "\n", Constants.outputPath + "/Log-Report/float-log/consecutive.txt", true);
					});
				}
				
				
				if (tableNonConsecutiveList.size() > 0) {
					
					fileWriterI.write("Table Non-Consecutive entry\n", Constants.outputPath + "/Log-Report/float-log/consecutive.txt", true);
					
					tableNonConsecutiveList.forEach(object -> {
						fileWriterI.write("<Table-Callout-Entry> "+object + "\n", Constants.outputPath + "/Log-Report/float-log/consecutive.txt", true);
					});
					fileWriterI.write("\n", Constants.outputPath + "/Log-Report/float-log/consecutive.txt", true);
				}
			}
			
		} catch(Exception exception) {	logger.error(exception.getMessage(), exception);	}
		finally {
			if (bufferedReader != null)
				try{	bufferedReader.close();	}catch(Exception ext) {	logger.error(ext.getMessage(), ext);	}
		}
		return isMissing;
	}
	
	/**
	 * boxFloatLabels
	 * 
	 */
	private String[] figureConsecutiveFloatLabels() {
		
		BufferedReader bufferedReader = null;
		String[] isMissing = {"false", "false"};
		
		File file = new File(Constants.outputPath + "/Log-Report/float-item/figure-main.txt");
		if (file.exists())
		try {
			
			FileInputStream fileInputStream = new FileInputStream(file);
			InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream, "UTF-8");
			bufferedReader = new BufferedReader(inputStreamReader);
			
			String line = "", mainLine = "";
			List<Integer> consecutiveArray = new ArrayList<Integer>(0);
			
			while( (line = bufferedReader.readLine()) != null ) {
				
				if (line.length() > 0) {
					
					mainLine = line;
					
					line = fileWriterI.figureCrossMarking(line);
					String boxLabel = "";
					
					int indexCrossRefOpen = line.indexOf("<@cross-ref-fig-open>");
					int indexCrossRefClose = line.indexOf("<@cross-ref-fig-close>");
					
					if ((indexCrossRefClose) > (indexCrossRefOpen)) {
						
						boxLabel = line.substring((line.indexOf("<@cross-ref-fig-open>") + "<@cross-ref-fig-open>".length()), line.indexOf("<@cross-ref-fig-close>"));
						
						String itemNum = "";
						
						boxLabel = miscUtility.removeStartEndSpaces(boxLabel);
						
						if (boxLabel.endsWith("."))
							boxLabel = boxLabel.substring(0, (boxLabel.lastIndexOf(".")));
						
						if (boxLabel.lastIndexOf(".") > 0)
							itemNum = boxLabel.substring(boxLabel.lastIndexOf(".") + 1);
						else
							itemNum = boxLabel.substring(boxLabel.lastIndexOf(" ") + 1);
						
						itemNum = miscUtility.removeStartEndSpaces(itemNum);
						
						boolean isValid = checkEntryInCalloutFile(itemNum, Constants.outputPath + "/Log-Report/float-item/figure-callout.txt", "fig");
						
						if (! isValid) {
							
							fileWriterI.write("{CALLOUT FIGURE MISSING}=" + mainLine + "\n", Constants.outputPath + "/Log-Report/float-log/float.txt", true);
							isMissing[0] = "true";
						}
						
						if (itemNum.contains("-"))	itemNum = itemNum.substring(itemNum.lastIndexOf("-") + 1);
						if (itemNum.contains("—"))	itemNum = itemNum.substring(itemNum.lastIndexOf("—") + 1);
						if (itemNum.contains("–"))	itemNum = itemNum.substring(itemNum.lastIndexOf("–") + 1);
						if (itemNum.contains("–"))	itemNum = itemNum.substring(itemNum.lastIndexOf("–") + 1);
						
						if (miscUtility.isNumber(itemNum)) {
							
							Integer val = Integer.parseInt(itemNum);
							consecutiveArray.add(val);
						}
					}
				}
			}
			
			Integer[] array = new Integer[consecutiveArray.size()];
			
			if (array.length > 1) {
				
				isMissing[1] = "true";
				
				for(int i = 0; i < consecutiveArray.size(); i++) array[i] = consecutiveArray.get(i);
				List<String> figureNonConsecutiveList = consecutiveFlow.fetchNonConsecutiveRanges(array);
				List<String> figureConsecutiveList = consecutiveFlow.fetchConsecutiveRanges(array);
				
				if (figureConsecutiveList.size() > 0) {
					
					fileWriterI.write("\n", Constants.outputPath + "/Log-Report/float-log/consecutive.txt", true);
					fileWriterI.write("Figure Consecutive entry\n", Constants.outputPath + "/Log-Report/float-log/consecutive.txt", true);
					
					figureConsecutiveList.forEach(object -> {
						fileWriterI.write(object + "\n", Constants.outputPath + "/Log-Report/float-log/consecutive.txt", true);
					});
				}
				
				
				if (figureNonConsecutiveList.size() > 0) {
					
					fileWriterI.write("Figure Non-Consecutive entry\n", Constants.outputPath + "/Log-Report/float-log/consecutive.txt", true);
					figureNonConsecutiveList.forEach(object -> {
						fileWriterI.write("<Figure-Callout-Entry> "+object + "\n", Constants.outputPath + "/Log-Report/float-log/consecutive.txt", true);
					});
					fileWriterI.write("\n", Constants.outputPath + "/Log-Report/float-log/consecutive.txt", true);
				}
			}
			
		} catch(Exception exception) {	logger.error(exception.getMessage(), exception);	}
		finally {
			if (bufferedReader != null)
				try{	bufferedReader.close();	}catch(Exception ext) {	logger.error(ext.getMessage(), ext);	}
		}
		return isMissing;
	}
	
	/**
	 * boxFloatLabels
	 * 
	 */
	private String[] boxConsecutiveFloatLabels() {
		
		BufferedReader bufferedReader = null;
		String[] isMissing = {"false", "false"};
		
		File file = new File(Constants.outputPath + "/Log-Report/float-item/box-main.txt");
		if (file.exists())
		try {
			
			FileInputStream fileInputStream = new FileInputStream(file);
			InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream, "UTF-8");
			bufferedReader = new BufferedReader(inputStreamReader);
			
			String line = "", mainLine = "";
			List<Integer> consecutiveArray = new ArrayList<Integer>(0);
			
			while( (line = bufferedReader.readLine()) != null ) {
				
				if (line.length() > 0) {
					
					mainLine = line;
					line = fileWriterI.boxCrossMarking(line);
					
					int indexCrossRefOpen = line.indexOf("<@cross-ref-box-open>");
					int indexCrossRefClose = line.indexOf("<@cross-ref-box-close>");
					if ((indexCrossRefClose) > (indexCrossRefOpen)) {
						
						String boxLabel = line.substring((line.indexOf("<@cross-ref-box-open>") + "<@cross-ref-box-open>".length()), line.indexOf("<@cross-ref-box-close>"));
						
						String itemNum = "";
						
						boxLabel = miscUtility.removeStartEndSpaces(boxLabel);
						
						if (boxLabel.endsWith("."))
							boxLabel = boxLabel.substring(0, (boxLabel.lastIndexOf(".")));
						
						if (boxLabel.lastIndexOf(".") > 0)
							itemNum = boxLabel.substring(boxLabel.lastIndexOf(".") + 1);
						else
							itemNum = boxLabel.substring(boxLabel.lastIndexOf(" ") + 1);
						
						itemNum = miscUtility.removeStartEndSpaces(itemNum);
						
						boolean isValid = checkEntryInCalloutFile(itemNum, Constants.outputPath + "/Log-Report/float-item/box-callout.txt", "box");
						
						if (! isValid) {
							
							fileWriterI.write("{CALLOUT BOX MISSING}=" + mainLine + "\n", Constants.outputPath + "/Log-Report/float-log/float.txt", true);
							isMissing[0] = "true";
						}
						
						if (itemNum.contains("-"))	itemNum = itemNum.substring(itemNum.lastIndexOf("-") + 1);
						if (itemNum.contains("—"))	itemNum = itemNum.substring(itemNum.lastIndexOf("—") + 1);
						if (itemNum.contains("–"))	itemNum = itemNum.substring(itemNum.lastIndexOf("–") + 1);
						if (itemNum.contains("–"))	itemNum = itemNum.substring(itemNum.lastIndexOf("–") + 1);
						
						if (miscUtility.isNumber(itemNum)) {
							
							Integer val = Integer.parseInt(itemNum);
							consecutiveArray.add(val);
						}
					}
				}
			}
			
			Integer[] array = new Integer[consecutiveArray.size()];
			if (array.length > 1) {
				
				isMissing[1] = "true";
				
				for(int i = 0; i < consecutiveArray.size(); i++) array[i] = consecutiveArray.get(i);
				List<String> boxNonConsecutiveList = consecutiveFlow.fetchNonConsecutiveRanges(array);
				List<String> boxConsecutiveList = consecutiveFlow.fetchConsecutiveRanges(array);
				
				if (boxConsecutiveList.size() > 0) {
					fileWriterI.write("\n", Constants.outputPath + "/Log-Report/float-log/consecutive.txt", true);
					fileWriterI.write("Box Consecutive entry\n", Constants.outputPath + "/Log-Report/float-log/consecutive.txt", true);
					
					boxConsecutiveList.forEach(object -> {
						fileWriterI.write(object + "\n", Constants.outputPath + "/Log-Report/float-log/consecutive.txt", true);
					});
				}
				
				if (boxNonConsecutiveList.size() > 0) {
					
					fileWriterI.write("Box Non-Consecutive entry\n", Constants.outputPath + "/Log-Report/float-log/consecutive.txt", true);
					boxNonConsecutiveList.forEach(object -> {
						fileWriterI.write("<Box-Callout-Entry> "+object + "\n", Constants.outputPath + "/Log-Report/float-log/consecutive.txt", true);
					});
					fileWriterI.write("\n", Constants.outputPath + "/Log-Report/float-log/consecutive.txt", true);
				}
			}
		} catch(Exception exception) {	logger.error(exception.getMessage(), exception);	}
		finally {
			if (bufferedReader != null)
				try{	bufferedReader.close();	}catch(Exception ext) {	logger.error(ext.getMessage(), ext);	}
		}
		return isMissing;
	}
	
	


	private String[] boxCalloutLog() {
		
		BufferedReader bufferedReader = null;
		String[] isMissing = {"false", "false"};
		
		File file = new File(Constants.outputPath + "/Log-Report/float-item/box-callout.txt");
		if (file.exists())
		try {
			
			FileInputStream fileInputStream = new FileInputStream(file);
			InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream, "UTF-8");
			bufferedReader = new BufferedReader(inputStreamReader);
			
			List<Integer> consecutiveArray = new ArrayList<Integer>(0);
			String line = "";
			
			while( (line = bufferedReader.readLine()) != null ){
				
				String itemLabel = "";
				
				itemLabel = miscUtility.removeStartEndSpaces(line);
				
				if (itemLabel.endsWith("."))
					itemLabel = itemLabel.substring(0, (itemLabel.lastIndexOf(".")));
				
				if (itemLabel.lastIndexOf(".") > 0)
					itemLabel = itemLabel.substring(itemLabel.lastIndexOf(".") + 1);
				else
					itemLabel = itemLabel.substring(itemLabel.lastIndexOf(" ") + 1);
				
				itemLabel = miscUtility.removeStartEndSpaces(itemLabel);
				
				boolean isValid = checkEntryInMainFile(itemLabel, Constants.outputPath + "/Log-Report/float-item/box-main.txt", "box");
				
				if (! isValid) {
					
					fileWriterI.write("{FLOAT MAIN BOX MISSING}="+line + "\n", Constants.outputPath + "/Log-Report/float-log/float.txt", true);
					isMissing[0] = "true";
				}
				
				
				if (itemLabel.contains("-"))	itemLabel = itemLabel.substring(itemLabel.lastIndexOf("-") + 1);
				if (itemLabel.contains("—"))	itemLabel = itemLabel.substring(itemLabel.lastIndexOf("—") + 1);
				if (itemLabel.contains("–"))	itemLabel = itemLabel.substring(itemLabel.lastIndexOf("–") + 1);
				if (itemLabel.contains("–"))	itemLabel = itemLabel.substring(itemLabel.lastIndexOf("–") + 1);
				
				if (miscUtility.isNumber(itemLabel)) {
					
					Integer val = Integer.parseInt(itemLabel);
					consecutiveArray.add(val);
				}
			}
			
			
			Integer[] array = new Integer[consecutiveArray.size()];
			if (array.length > 1) {
				
				isMissing[1] = "true";
				
				for(int i = 0; i < consecutiveArray.size(); i++) array[i] = consecutiveArray.get(i);
				List<String> boxNonConsecutiveList = consecutiveFlow.fetchNonConsecutiveRanges(array);
				List<String> boxConsecutiveList = consecutiveFlow.fetchConsecutiveRanges(array);
				
				if (boxConsecutiveList.size() > 0) {
					fileWriterI.write("\n", Constants.outputPath + "/Log-Report/float-log/consecutive.txt", true);
					fileWriterI.write("\nBox Consecutive entry\n", Constants.outputPath + "/Log-Report/float-log/consecutive.txt", true);
					
					boxConsecutiveList.forEach(object -> {
						fileWriterI.write(object + "\n", Constants.outputPath + "/Log-Report/float-log/consecutive.txt", true);
					});
				}
				
				if (boxNonConsecutiveList.size() > 0) {
					
					fileWriterI.write("\nBox Non-Consecutive entry\n", Constants.outputPath + "/Log-Report/float-log/consecutive.txt", true);
					boxNonConsecutiveList.forEach(object -> {
						fileWriterI.write("<Box-Float-Entry> "+object + "\n", Constants.outputPath + "/Log-Report/float-log/consecutive.txt", true);
					});
					fileWriterI.write("\n", Constants.outputPath + "/Log-Report/float-log/consecutive.txt", true);
				}
			}
			
		} catch(Exception exception) {	logger.error(exception.getMessage(), exception);	}
		finally {
			if (bufferedReader != null)
				try{	bufferedReader.close();	}catch(Exception ext) {	logger.error(ext.getMessage(), ext);	}
		}
		return isMissing;
	}
	
	
	/**
	 * figureCalloutLog
	 * @return String[]{}
	 */
	private String[] figureCalloutLog() {
		
		BufferedReader bufferedReader = null;
		String[] isMissing = {"false", "false"};
		
		File file = new File(Constants.outputPath + "/Log-Report/float-item/figure-callout.txt");
		if (file.exists())
		try {
			
			FileInputStream fileInputStream = new FileInputStream(file);
			InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream, "UTF-8");
			bufferedReader = new BufferedReader(inputStreamReader);
			
			String line = "";
			List<Integer> consecutiveArray = new ArrayList<Integer>(0);
			
			while( (line = bufferedReader.readLine()) != null ){
				
				String itemLabel = "";
				
				itemLabel = miscUtility.removeStartEndSpaces(line);
				
				
				if (itemLabel.endsWith("."))
					itemLabel = itemLabel.substring(0, (itemLabel.lastIndexOf(".")));
				
				if (itemLabel.lastIndexOf(".") > 0)
					itemLabel = itemLabel.substring(itemLabel.lastIndexOf(".") + 1);
				else
					itemLabel = itemLabel.substring(itemLabel.lastIndexOf(" ") + 1);
				
				itemLabel = miscUtility.removeStartEndSpaces(itemLabel);
				
				boolean isValid = checkEntryInMainFile(itemLabel, Constants.outputPath + "/Log-Report/float-item/figure-main.txt", "fig");
				
				if (! isValid) {
					
					fileWriterI.write("{FLOAT MAIN FIGURE MISSING}=" + line + "\n", Constants.outputPath + "/Log-Report/float-log/float.txt", true);
					isMissing[0] = "true";
				}
				
				
				if (itemLabel.contains("-"))	itemLabel = itemLabel.substring(itemLabel.lastIndexOf("-") + 1);
				if (itemLabel.contains("—"))	itemLabel = itemLabel.substring(itemLabel.lastIndexOf("—") + 1);
				if (itemLabel.contains("–"))	itemLabel = itemLabel.substring(itemLabel.lastIndexOf("–") + 1);
				if (itemLabel.contains("–"))	itemLabel = itemLabel.substring(itemLabel.lastIndexOf("–") + 1);
				
				if (miscUtility.isNumber(itemLabel)) {
					
					Integer val = Integer.parseInt(itemLabel);
					consecutiveArray.add(val);
				}
			}
			
			
			Integer[] array = new Integer[consecutiveArray.size()];
			if (array.length > 1) {
				
				isMissing[1] = "true";
				
				for(int i = 0; i < consecutiveArray.size(); i++) array[i] = consecutiveArray.get(i);
				List<String> boxNonConsecutiveList = consecutiveFlow.fetchNonConsecutiveRanges(array);
				List<String> boxConsecutiveList = consecutiveFlow.fetchConsecutiveRanges(array);
				
				if (boxConsecutiveList.size() > 0) {
					fileWriterI.write("\n", Constants.outputPath + "/Log-Report/float-log/consecutive.txt", true);
					fileWriterI.write("\nFIGURE Consecutive entry\n", Constants.outputPath + "/Log-Report/float-log/consecutive.txt", true);
					
					boxConsecutiveList.forEach(object -> {
						fileWriterI.write(object + "\n", Constants.outputPath + "/Log-Report/float-log/consecutive.txt", true);
					});
				}
				
				if (boxNonConsecutiveList.size() > 0) {
					
					fileWriterI.write("\nFIGURE Non-Consecutive entry\n", Constants.outputPath + "/Log-Report/float-log/consecutive.txt", true);
					boxNonConsecutiveList.forEach(object -> {
						fileWriterI.write("<Figure-Float-Entry> "+object + "\n", Constants.outputPath + "/Log-Report/float-log/consecutive.txt", true);
					});
					fileWriterI.write("\n", Constants.outputPath + "/Log-Report/float-log/consecutive.txt", true);
				}
			}
			
		} catch(Exception exception) {	logger.error(exception.getMessage(), exception);	}
		finally {
			if (bufferedReader != null)
				try{	bufferedReader.close();	}catch(Exception ext) {	logger.error(ext.getMessage(), ext);	}
		}
		return isMissing;
	}
	
	private String[] tableCalloutLog() {
		
		BufferedReader bufferedReader = null;
		String[] isMissing = {"false", "false"};
		
		File file = new File(Constants.outputPath + "/Log-Report/float-item/table-callout.txt");
		if (file.exists())
		try {
			
			FileInputStream fileInputStream = new FileInputStream(file);
			InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream, "UTF-8");
			bufferedReader = new BufferedReader(inputStreamReader);
			
			String line = "";
			List<Integer> consecutiveArray = new ArrayList<Integer>(0);
			
			while( (line = bufferedReader.readLine()) != null ){
				
				String itemLabel = "";
				
				itemLabel = miscUtility.removeStartEndSpaces(line);
				
				if (itemLabel.endsWith("."))
					itemLabel = itemLabel.substring(0, (itemLabel.lastIndexOf(".")));
				
				if (itemLabel.lastIndexOf(".") > 0)
					itemLabel = itemLabel.substring(itemLabel.lastIndexOf(".") + 1);
				else
					itemLabel = itemLabel.substring(itemLabel.lastIndexOf(" ") + 1);
				
				itemLabel = miscUtility.removeStartEndSpaces(itemLabel);
				
				boolean isValid = checkEntryInMainFile(itemLabel, Constants.outputPath + "/Log-Report/float-item/table-main.txt", "table");
				
				if (! isValid) {
					
					fileWriterI.write("{FLOAT MAIN TABLE MISSING}=" + line + "\n", Constants.outputPath + "/Log-Report/float-log/float.txt", true);
					isMissing[0] = "true";
				}
				
				if (itemLabel.contains("-"))	itemLabel = itemLabel.substring(itemLabel.lastIndexOf("-") + 1);
				if (itemLabel.contains("—"))	itemLabel = itemLabel.substring(itemLabel.lastIndexOf("—") + 1);
				if (itemLabel.contains("–"))	itemLabel = itemLabel.substring(itemLabel.lastIndexOf("–") + 1);
				if (itemLabel.contains("–"))	itemLabel = itemLabel.substring(itemLabel.lastIndexOf("–") + 1);
				
				if (miscUtility.isNumber(itemLabel)) {
					
					Integer val = Integer.parseInt(itemLabel);
					consecutiveArray.add(val);
				}
			}
			
			
			Integer[] array = new Integer[consecutiveArray.size()];
			if (array.length > 1) {
				
				isMissing[1] = "true";
				
				for(int i = 0; i < consecutiveArray.size(); i++) array[i] = consecutiveArray.get(i);
				List<String> boxNonConsecutiveList = consecutiveFlow.fetchNonConsecutiveRanges(array);
				List<String> boxConsecutiveList = consecutiveFlow.fetchConsecutiveRanges(array);
				
				if (boxConsecutiveList.size() > 0) {
					fileWriterI.write("\n", Constants.outputPath + "/Log-Report/float-log/consecutive.txt", true);
					fileWriterI.write("\nTABLE Consecutive entry\n", Constants.outputPath + "/Log-Report/float-log/consecutive.txt", true);
					
					boxConsecutiveList.forEach(object -> {
						fileWriterI.write(object + "\n", Constants.outputPath + "/Log-Report/float-log/consecutive.txt", true);
					});
				}
				
				if (boxNonConsecutiveList.size() > 0) {
					
					fileWriterI.write("\nTABLE Non-Consecutive entry\n", Constants.outputPath + "/Log-Report/float-log/consecutive.txt", true);
					boxNonConsecutiveList.forEach(object -> {
						fileWriterI.write("<Table-Float-Entry> "+object + "\n", Constants.outputPath + "/Log-Report/float-log/consecutive.txt", true);
					});
					fileWriterI.write("\n", Constants.outputPath + "/Log-Report/float-log/consecutive.txt", true);
				}
			}
			
		} catch(Exception exception) {	logger.error(exception.getMessage(), exception);	}
		finally {
			if (bufferedReader != null)
				try{	bufferedReader.close();	}catch(Exception ext) {	logger.error(ext.getMessage(), ext);	}
		}
		return isMissing;
	}
	
	private String[] videoCalloutLog() {
		
		BufferedReader bufferedReader = null;
		String[] isMissing = {"false", "false"};
		
		File file = new File(Constants.outputPath + "/Log-Report/float-item/video-callout.txt");
		if (file.exists())
		try {
			
			FileInputStream fileInputStream = new FileInputStream(file);
			InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream, "UTF-8");
			bufferedReader = new BufferedReader(inputStreamReader);
			
			String line = "";
			List<Integer> consecutiveArray = new ArrayList<Integer>(0);
			
			while( (line = bufferedReader.readLine()) != null ){
				
				String itemLabel = "";
				
				itemLabel = miscUtility.removeStartEndSpaces(line);
				
				if (itemLabel.endsWith("."))
					itemLabel = itemLabel.substring(0, (itemLabel.lastIndexOf(".")));
				
				if (itemLabel.lastIndexOf(".") > 0)
					itemLabel = itemLabel.substring(itemLabel.lastIndexOf(".") + 1);
				else
					itemLabel = itemLabel.substring(itemLabel.lastIndexOf(" ") + 1);
				
				itemLabel = miscUtility.removeStartEndSpaces(itemLabel);
				
				boolean isValid = checkEntryInMainFile(itemLabel, Constants.outputPath + "/Log-Report/float-item/video-main.txt", "video");
				
				if (! isValid) {
					
					fileWriterI.write("{FLOAT MAIN VIDEO MISSING}="+line + "\n", Constants.outputPath + "/Log-Report/float-log/float.txt", true);
					isMissing[0] = "true";
				}
				
				
				if (itemLabel.contains("-"))	itemLabel = itemLabel.substring(itemLabel.lastIndexOf("-") + 1);
				if (itemLabel.contains("—"))	itemLabel = itemLabel.substring(itemLabel.lastIndexOf("—") + 1);
				if (itemLabel.contains("–"))	itemLabel = itemLabel.substring(itemLabel.lastIndexOf("–") + 1);
				if (itemLabel.contains("–"))	itemLabel = itemLabel.substring(itemLabel.lastIndexOf("–") + 1);
				
				if (miscUtility.isNumber(itemLabel)) {
					
					Integer val = Integer.parseInt(itemLabel);
					consecutiveArray.add(val);
				}
			}
			
			
			Integer[] array = new Integer[consecutiveArray.size()];
			if (array.length > 0) {
				
				isMissing[1] = "true";
				
				for(int i = 0; i < consecutiveArray.size(); i++) array[i] = consecutiveArray.get(i);
				List<String> boxNonConsecutiveList = consecutiveFlow.fetchNonConsecutiveRanges(array);
				List<String> boxConsecutiveList = consecutiveFlow.fetchConsecutiveRanges(array);
				
				if (boxConsecutiveList.size() > 0) {
					fileWriterI.write("\n", Constants.outputPath + "/Log-Report/float-log/consecutive.txt", true);
					fileWriterI.write("\nVIDEO Consecutive entry\n", Constants.outputPath + "/Log-Report/float-log/consecutive.txt", true);
					
					boxConsecutiveList.forEach(object -> {
						fileWriterI.write(object + "\n", Constants.outputPath + "/Log-Report/float-log/consecutive.txt", true);
					});
				}
				
				if (boxNonConsecutiveList.size() > 0) {
					
					fileWriterI.write("\nVIDEO Non-Consecutive entry\n", Constants.outputPath + "/Log-Report/float-log/consecutive.txt", true);
					boxNonConsecutiveList.forEach(object -> {
						fileWriterI.write("<Video-Float-Entry> "+object + "\n", Constants.outputPath + "/Log-Report/float-log/consecutive.txt", true);
					});
					fileWriterI.write("\n", Constants.outputPath + "/Log-Report/float-log/consecutive.txt", true);
				}
			}
			
		} catch(Exception exception) {	logger.error(exception.getMessage(), exception);	}
		finally {
			if (bufferedReader != null)
				try{	bufferedReader.close();	}catch(Exception ext) {	logger.error(ext.getMessage(), ext);	}
		}
		return isMissing;
	}	
	
	
	
	private Boolean checkEntryInCalloutFile(String lineCall, String filePath, String type) {
		
		BufferedReader bufferedReader = null;
		Boolean isValid = false;
		
		
		File file = new File(filePath);
		if (file.exists())
		try {
			
			FileInputStream fileInputStream = new FileInputStream(new File(filePath));
			InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream, "UTF-8");
			bufferedReader = new BufferedReader(inputStreamReader);
			
			String mainLine = "";
			while( (mainLine = bufferedReader.readLine()) != null ){
				
//				if (type.equalsIgnoreCase("box"))
//					mainLine = fileWriterI.boxCrossMarking(mainLine);
//				else if (type.equalsIgnoreCase("fig"))
//					mainLine = fileWriterI.figureCrossMarking(mainLine);
//				else if (type.equalsIgnoreCase("table"))
//					mainLine = fileWriterI.tableCrossMarking(mainLine);
//				else if (type.equalsIgnoreCase("video"))
//					mainLine = fileWriterI.videoCrossMarking(mainLine);
				
				String boxLabel = mainLine;
//				int indexCrossRefOpen = mainLine.indexOf("<@cross-ref-"+(type)+">");
//				int indexCrossRefClose = mainLine.indexOf("<@cross-ref-"+(type)+"-close>");
				
//				if ((indexCrossRefClose) > (indexCrossRefOpen)) {
					
//					String startTag = "<@cross-ref-"+(type)+">", endTag = "<@cross-ref-"+(type)+"-close>";
//					boxLabel = mainLine.substring((mainLine.indexOf(startTag) + startTag.length()), mainLine.indexOf(endTag));
				
					boxLabel = miscUtility.removeStartEndSpaces(boxLabel);
					
					if (boxLabel.endsWith("."))
						boxLabel = boxLabel.substring(0, (boxLabel.lastIndexOf(".")));
					
					if (boxLabel.lastIndexOf(".") > 0)
						boxLabel = boxLabel.substring(boxLabel.lastIndexOf(".") + 1);
					else
						boxLabel = boxLabel.substring(boxLabel.lastIndexOf(" ") + 1);
					
					
					boxLabel = miscUtility.removeStartEndSpaces(boxLabel);
					
					if (boxLabel.toLowerCase().startsWith(lineCall.toLowerCase())) {
						isValid = true;
						break;
					}
				}
//			}
			
		} catch(Exception exception) {	logger.error(exception.getMessage(), exception);	}
		finally {
			if (bufferedReader != null)
				try{	bufferedReader.close();	}catch(Exception ext) {	logger.error(ext.getMessage(), ext);	}
		}
		
		return isValid;
	}
	
	
	private Boolean checkEntryInMainFile(String lineCall, String filePath, String type) {
		
		BufferedReader bufferedReader = null;
		Boolean isValid = false;
		
		
		File file = new File(filePath);
		if (file.exists())
		try {
			
			FileInputStream fileInputStream = new FileInputStream(new File(filePath));
			InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream, "UTF-8");
			bufferedReader = new BufferedReader(inputStreamReader);
			
			String mainLine = "";
			while( (mainLine = bufferedReader.readLine()) != null ){
				
				if (type.equalsIgnoreCase("box"))
					mainLine = fileWriterI.boxCrossMarking(mainLine);
				else if (type.equalsIgnoreCase("fig"))
					mainLine = fileWriterI.figureCrossMarking(mainLine);
				else if (type.equalsIgnoreCase("table"))
					mainLine = fileWriterI.tableCrossMarking(mainLine);
				else if (type.equalsIgnoreCase("video"))
					mainLine = fileWriterI.videoCrossMarking(mainLine);
				
				String boxLabel = "";
				int indexCrossRefOpen = mainLine.indexOf("<@cross-ref-"+(type)+"-open>");
				int indexCrossRefClose = mainLine.indexOf("<@cross-ref-"+(type)+"-close>");
				
				if ((indexCrossRefClose) > (indexCrossRefOpen)) {
					
					String startTag = "<@cross-ref-"+(type)+"-open>", endTag = "<@cross-ref-"+(type)+"-close>";
					boxLabel = mainLine.substring((mainLine.indexOf(startTag) + startTag.length()), mainLine.indexOf(endTag));
				
					boxLabel = miscUtility.removeStartEndSpaces(boxLabel);
					
					
					if (boxLabel.endsWith("."))
						boxLabel = boxLabel.substring(0, (boxLabel.lastIndexOf(".")));
					
					if (boxLabel.lastIndexOf(".") > 0)
						boxLabel = boxLabel.substring(boxLabel.lastIndexOf(".") + 1);
					else
						boxLabel = boxLabel.substring(boxLabel.lastIndexOf(" ") + 1);
					
					
					boxLabel = miscUtility.removeStartEndSpaces(boxLabel);
					
					if (boxLabel.toLowerCase().startsWith(lineCall.toLowerCase())) {
						isValid = true;
						break;
					}
				}
			}
			
		} catch(Exception exception) {	logger.error(exception.getMessage(), exception);	}
		finally {
			if (bufferedReader != null)
				try{	bufferedReader.close();	}catch(Exception ext) {	logger.error(ext.getMessage(), ext);	}
		}
		
		return isValid;
	}
	
	
	
	/**
	 * setter of injections
	 * @param fileWriterI
	 */
	public void setFileWriterI(FileWriterI fileWriterI) {
		this.fileWriterI = fileWriterI;
	}

	public void setMiscUtility(MiscUtility miscUtility) {
		this.miscUtility = miscUtility;
	}

	public void setConsecutiveFlow(ConsecutiveFlow consecutiveFlow) {
		this.consecutiveFlow = consecutiveFlow;
	}
	
}
