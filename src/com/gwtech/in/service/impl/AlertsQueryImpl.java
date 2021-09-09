package com.gwtech.in.service.impl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;

import org.apache.log4j.Logger;

import com.gwtech.in.service.AlertsQuery;
import com.gwtech.in.service.FileWriterI;
import com.gwtech.in.utils.Constants;

public class AlertsQueryImpl implements AlertsQuery {
	
	private static final Logger logger = Logger.getLogger(AlertsQueryImpl.class);
	private FileWriterI fileWriterI;
	
	@Override
	public String generateAlertsFloatItems(String outputFile) {
		
		String alertKeywordsAbstract = "";
		if (Constants.taSettingForUser.isAbstractKeywordsQuery())
			alertKeywordsAbstract = alertsKeywordsAndAbstractFetch(outputFile);
		
		
		
		String alertFloatmain = "";
		if (Constants.taSettingForUser.isVerifyAllElementsCalledout())
			alertFloatmain = alertsFloatMainFetch(Constants.outputPath + "/Log-Report/float-log/float.txt");
		
		
		
		String alertFloatConsecutiveEntry = "";
		if (Constants.taSettingForUser.isVerifyAllElementsConsecutiveOrder())
			alertFloatConsecutiveEntry = alertFloatConsecutiveEntryFetch(Constants.outputPath + "/Log-Report/float-log/consecutive.txt");
		
		return "\n" + alertKeywordsAbstract + "\n" + alertFloatmain + "\n" + alertFloatConsecutiveEntry + "\n\n";
	}
	
	
	private String alertFloatConsecutiveEntryFetch(String consecutiveLogPath) {
		
		BufferedReader bufferedReader =  null;
		String line = "";
		StringBuffer bufferMain = new StringBuffer();
		
		StringBuffer bufferFloatFigure = new StringBuffer();
		StringBuffer bufferFloatBox = new StringBuffer();
		StringBuffer bufferFloatTable = new StringBuffer();
		StringBuffer bufferFloatVideo = new StringBuffer();
		
		StringBuffer bufferCalloutFigure = new StringBuffer();
		StringBuffer bufferCalloutBox = new StringBuffer();
		StringBuffer bufferCalloutTable = new StringBuffer();
		StringBuffer bufferCalloutVideo = new StringBuffer();
		
		
		Integer floatmainFigureCount = 0, floatmainBoxCount = 0, floatmainTableCount = 0, floatmainVideoCount = 0, 
				floatcalloutFigureCount = 0, floatcalloutBoxCount = 0, floatcalloutTableCount = 0, floatcalloutVideoCount = 0;
		if (new File(consecutiveLogPath).exists())
		try {
			
			bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(new File(consecutiveLogPath))));
			while ((line = bufferedReader.readLine()) != null) {
				
				/**
				 * float-items
				 * 
				 */
				
				if (line.startsWith("<Figure-Float-Entry> ")) { // {FLOAT MAIN FIGURE MISSING}=Fig. 36.32
					
					String label = line.substring(line.indexOf("<Figure-Float-Entry> ") + ("<Figure-Float-Entry> ".length()));
					
					if (floatmainFigureCount == 0) {
						
						bufferFloatFigure.append("\nAlert "+(++Constants.alertIndex)+": AU: Please verify, data placement of figure sequence-order for ");
						bufferFloatFigure.append("\"" + label + "\"");
					}  
						
					if (floatmainFigureCount > 0) {
						
						bufferFloatFigure.append(", \"" + label + "\"");
					}
					
					floatmainFigureCount ++;
				}
				if (line.startsWith("<Box-Float-Entry> ")) { // {FLOAT MAIN FIGURE MISSING}=Fig. 36.32
					
					String label = line.substring((line.indexOf("<Box-Float-Entry> ") + "<Box-Float-Entry> ".length()));
//					bufferFloatBox.append("\nAlert "+(++Constants.alertIndex)+": AU: Please verify, float item box is missing for \"" + boxLabel+"\"");
					
					if (floatmainBoxCount == 0) {
						
						bufferFloatBox.append("\nAlert "+(++Constants.alertIndex)+": AU: Please verify, data placement of box sequence-order for ");
						bufferFloatBox.append("\"" + label + "\"");
					}  
						
					if (floatmainBoxCount > 0) {
						
						bufferFloatBox.append(", \"" + label + "\"");
					}
					
					floatmainBoxCount ++;
				}
				if (line.startsWith("<Table-Float-Entry> ")) { // {FLOAT MAIN FIGURE MISSING}=Fig. 36.32
					
						
					String label = line.substring((line.indexOf("<Table-Float-Entry> ") + "<Table-Float-Entry> ".length()));
//					bufferFloatTable.append("\nAlert "+(++Constants.alertIndex)+": AU: Please verify, float item table is missing for \"" + boxLabel+"\"");
					
					if (floatmainTableCount == 0) {
						
						bufferFloatTable.append("\nAlert "+(++Constants.alertIndex)+": AU: Please verify, data placement of table sequence-order for ");
						bufferFloatTable.append("\"" + label + "\"");
					}  
						
					if (floatmainTableCount > 0) {
						
						bufferFloatTable.append(", \"" + label + "\"");
					}
					
					floatmainTableCount ++;
				}
				if (line.startsWith("<Video-Float-Entry> ")) { // {FLOAT MAIN FIGURE MISSING}=Fig. 36.32
					
					String label = line.substring((line.indexOf("<Video-Float-Entry> ") + "<Video-Float-Entry> ".length()));
//					bufferFloatVideo.append("\nAlert "+(++Constants.alertIndex)+": AU: Please verify, float item video is missing for \"" + boxLabel+"\"");
					
					if (floatmainVideoCount == 0) {
						
						bufferFloatVideo.append("\nAlert "+(++Constants.alertIndex)+": AU: Please verify data placement of video sequence-order for ");
						bufferFloatVideo.append("\"" + label + "\"");
					}  
						
					if (floatmainVideoCount > 0) {
						
						bufferFloatVideo.append(", \"" + label + "\"");
					}
					
					floatmainVideoCount ++;
				}
				
				/**
				 * 
				 * call-outs
				 * 
				 */
				
				
				if (line.startsWith("<Figure-Callout-Entry> ")) {
					
					String label = line.substring((line.indexOf("<Figure-Callout-Entry> ") + "<Figure-Callout-Entry> ".length()));
//					bufferCalloutFigure.append("\nAlert "+(++Constants.alertIndex)+": AU: Please verify, in-text callout of figure \""+(boxLabel)+"\" is it correct?");
					
					if (floatcalloutFigureCount == 0) {
						
						bufferCalloutFigure.append("\nAlert "+(++Constants.alertIndex)+": AU: Please verify, in-text callout of figure sequence-order for ");
						bufferCalloutFigure.append("\"" + label + "\"");
					}  
						
					if (floatcalloutFigureCount > 0) {
						
						bufferCalloutFigure.append(", \"" + label + "\"");
					}
					
					floatcalloutFigureCount ++;
				}
				if (line.startsWith("<Box-Callout-Entry> ")) {
					
						
					String label = line.substring((line.indexOf("<Box-Callout-Entry> ") + "<Box-Callout-Entry> ".length()));
//					bufferCalloutBox.append("\nAlert "+(++Constants.alertIndex)+": AU: Please verify, in-text callout of box \""+(boxLabel)+"\" is it correct?");
					
					if (floatcalloutBoxCount == 0) {
						
						bufferCalloutBox.append("\nAlert "+(++Constants.alertIndex)+": AU: Please verify, in-text callout of box sequence-order for ");
						bufferCalloutBox.append("\"" + label + "\"");
					}  
						
					if (floatcalloutBoxCount > 0) {
						
						bufferCalloutBox.append(", \"" + label + "\"");
					}
					
					floatcalloutBoxCount ++;
				}
				if (line.startsWith("<Table-Callout-Entry> ")) {
					
					String label = line.substring((line.indexOf("<Table-Callout-Entry> ") + "<Table-Callout-Entry> ".length()));
//					bufferCalloutTable.append("\nAlert "+(++Constants.alertIndex)+": AU: Please verify, in-text callout of table \""+(boxLabel)+"\" is it correct?");
					
					if (floatcalloutTableCount == 0) {
						
						bufferCalloutTable.append("\nAlert "+(++Constants.alertIndex)+": AU: Please verify, in-text callout of table sequence-order for ");
						bufferCalloutTable.append("\"" + label + "\"");
					}  
						
					if (floatcalloutTableCount > 0) {
						
						bufferCalloutTable.append(", \"" + label + "\"");
					}
					
					floatcalloutTableCount ++;
				}
				if (line.startsWith("<Video-Callout-Entry> ")) {
					
					String label = line.substring((line.indexOf("<Video-Callout-Entry> ") + "<Video-Callout-Entry> ".length()));
//					bufferCalloutVideo.append("\nAlert "+(++Constants.alertIndex)+": AU: Please verify, in-text callout of video \""+(boxLabel)+"\" is it correct?");
					
					if (floatcalloutVideoCount == 0) {
						
						bufferCalloutVideo.append("\nAlert "+(++Constants.alertIndex)+": AU: Please verify, in-text callout of video sequence-order for ");
						bufferCalloutVideo.append("\"" + label + "\"");
					}  
						
					if (floatcalloutVideoCount > 0) {
						
						bufferCalloutVideo.append(", \"" + label + "\"");
					}
					
					floatcalloutVideoCount ++;
				}

			}
			
			
			String dataFloatBox = bufferFloatBox.toString();
			if (dataFloatBox.length() > 0)	dataFloatBox = dataFloatBox + ".";
			String dataFloatFigure = bufferFloatFigure.toString();
			if (dataFloatFigure.length() > 0)	dataFloatFigure = dataFloatFigure + ".";
			String dataFloatTable = bufferFloatTable.toString();
			if (dataFloatTable.length() > 0)	dataFloatTable = dataFloatTable + ".";
			String dataFloatVideo = bufferFloatVideo.toString();
			if (dataFloatVideo.length() > 0)	dataFloatVideo = dataFloatVideo + ".";
			
			
			
			String dataCalloutBox = bufferCalloutBox.toString();
			if (dataCalloutBox.length() > 0)	dataCalloutBox = dataCalloutBox + ".";
			String dataCalloutFigure = bufferCalloutFigure.toString();
			if (dataCalloutFigure.length() > 0)	dataCalloutFigure = dataCalloutFigure + ".";
			String dataCalloutTable = bufferCalloutTable.toString();
			if (dataCalloutTable.length() > 0)	dataCalloutTable = dataCalloutTable + ".";
			String dataCalloutVideo = bufferCalloutVideo.toString();
			if (dataCalloutVideo.length() > 0)	dataCalloutVideo = dataCalloutVideo + ".";
			
			bufferMain.append(dataFloatBox);
			bufferMain.append(dataFloatFigure);
			bufferMain.append(dataFloatTable);
			bufferMain.append(dataFloatVideo);
			
			bufferMain.append(dataCalloutBox);
			bufferMain.append(dataCalloutFigure);
			bufferMain.append(dataCalloutTable);
			bufferMain.append(dataCalloutVideo);
			
			
		} catch(Exception exception)	{	logger.error(exception.getMessage(), exception);	}
		finally {
			if (bufferedReader != null)
				try {	bufferedReader.close();	bufferedReader = null;	}catch(Exception exp) {exp.getMessage(); }
		}
		return bufferMain.toString();
	}


	/**
	 * alertsFloatMainFetch
	 * @param FloatmainPath
	 * @return
	 */
	private String alertsFloatMainFetch(String FloatmainPath) {
		
		BufferedReader bufferedReader =  null;
		String line = "";
		StringBuffer bufferMain = new StringBuffer();
		
		StringBuffer bufferFloatFigure = new StringBuffer();
		StringBuffer bufferFloatBox = new StringBuffer();
		StringBuffer bufferFloatTable = new StringBuffer();
		StringBuffer bufferFloatVideo = new StringBuffer();
		
		StringBuffer bufferCalloutFigure = new StringBuffer();
		StringBuffer bufferCalloutBox = new StringBuffer();
		StringBuffer bufferCalloutTable = new StringBuffer();
		StringBuffer bufferCalloutVideo = new StringBuffer();
		
		
		Integer floatmainFigureCount = 0, floatmainBoxCount = 0, floatmainTableCount = 0, floatmainVideoCount = 0, 
				floatcalloutFigureCount = 0, floatcalloutBoxCount = 0, floatcalloutTableCount = 0, floatcalloutVideoCount = 0;
		if (new File(FloatmainPath).exists())
		try {
			
			bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(new File(FloatmainPath))));
			while ((line = bufferedReader.readLine()) != null) {
				
				/**
				 * float-items
				 * 
				 */
				
				if (line.startsWith("{float main figure")) { // {FLOAT MAIN FIGURE MISSING}=Fig. 36.32
					
					line = fileWriterI.figureCrossMarking(line);
					int indexCrossRefOpen = line.indexOf("<@cross-ref-fig-open>");
					int indexCrossRefClose = line.indexOf("<@cross-ref-fig-close>");
					if ((indexCrossRefClose) > (indexCrossRefOpen)) {
						
						String boxLabel = line.substring((line.indexOf("<@cross-ref-fig-open>") + "<@cross-ref-fig-open>".length()), line.indexOf("<@cross-ref-fig-close>"));
						
						if (floatmainFigureCount == 0) {
							
							bufferFloatFigure.append("\nAlert "+(++Constants.alertIndex)+": AU: Please verify, data placement of figure is missing for ");
							bufferFloatFigure.append("\"" + boxLabel + "\"");
						}  
							
						if (floatmainFigureCount > 0) {
							
							bufferFloatFigure.append(", \"" + boxLabel + "\"");
						}
						
						floatmainFigureCount ++;
					}
				}
				if (line.startsWith("{FLOAT MAIN BOX")) { // {FLOAT MAIN FIGURE MISSING}=Fig. 36.32
					
					line = fileWriterI.boxCrossMarking(line);
					int indexCrossRefOpen = line.indexOf("<@cross-ref-box-open>");
					int indexCrossRefClose = line.indexOf("<@cross-ref-box-close>");
					if ((indexCrossRefClose) > (indexCrossRefOpen)) {
						
						String boxLabel = line.substring((line.indexOf("<@cross-ref-box-open>") + "<@cross-ref-box-open>".length()), line.indexOf("<@cross-ref-box-close>"));
//						bufferFloatBox.append("\nAlert "+(++Constants.alertIndex)+": AU: Please verify, float item box is missing for \"" + boxLabel+"\"");
						
						if (floatmainBoxCount == 0) {
							
							bufferFloatBox.append("\nAlert "+(++Constants.alertIndex)+": AU: Please verify, data placement of box is missing for ");
							bufferFloatBox.append("\"" + boxLabel + "\"");
						}  
							
						if (floatmainBoxCount > 0) {
							
							bufferFloatBox.append(", \"" + boxLabel + "\"");
						}
						
						floatmainBoxCount ++;
					}
				}
				if (line.startsWith("{FLOAT MAIN TABLE")) { // {FLOAT MAIN FIGURE MISSING}=Fig. 36.32
					
					line = fileWriterI.tableCrossMarking(line);
					int indexCrossRefOpen = line.indexOf("<@cross-ref-table-open>");
					int indexCrossRefClose = line.indexOf("<@cross-ref-table-close>");
					if ((indexCrossRefClose) > (indexCrossRefOpen)) {
						
						String boxLabel = line.substring((line.indexOf("<@cross-ref-table-open>") + "<@cross-ref-table-open>".length()), line.indexOf("<@cross-ref-table-close>"));
//						bufferFloatTable.append("\nAlert "+(++Constants.alertIndex)+": AU: Please verify, float item table is missing for \"" + boxLabel+"\"");
						
						if (floatmainTableCount == 0) {
							
							bufferFloatTable.append("\nAlert "+(++Constants.alertIndex)+": AU: Please verify, data placement of table is missing for ");
							bufferFloatTable.append("\"" + boxLabel + "\"");
						}  
							
						if (floatmainTableCount > 0) {
							
							bufferFloatTable.append(", \"" + boxLabel + "\"");
						}
						
						floatmainTableCount ++;
					}
				}
				if (line.startsWith("{FLOAT MAIN VIDEO")) { // {FLOAT MAIN FIGURE MISSING}=Fig. 36.32
					
					line = fileWriterI.videoCrossMarking(line);
					int indexCrossRefOpen = line.indexOf("<@cross-ref-video-open>");
					int indexCrossRefClose = line.indexOf("<@cross-ref-video-close>");
					if ((indexCrossRefClose) > (indexCrossRefOpen)) {
						
						String boxLabel = line.substring((line.indexOf("<@cross-ref-video-open>") + "<@cross-ref-video-open>".length()), line.indexOf("<@cross-ref-video-close>"));
//						bufferFloatVideo.append("\nAlert "+(++Constants.alertIndex)+": AU: Please verify, float item video is missing for \"" + boxLabel+"\"");
						
						if (floatmainVideoCount == 0) {
							
							bufferFloatVideo.append("\nAlert "+(++Constants.alertIndex)+": AU: Please verify, data placement of video is missing for ");
							bufferFloatVideo.append("\"" + boxLabel + "\"");
						}  
							
						if (floatmainVideoCount > 0) {
							
							bufferFloatVideo.append(", \"" + boxLabel + "\"");
						}
						
						floatmainVideoCount ++;
					}
				}
				
				/**
				 * 
				 * call-outs
				 * 
				 */
				
				
				if (line.startsWith("{CALLOUT FIGURE")) {
					
					line = fileWriterI.figureCrossMarking(line);
					int indexCrossRefOpen = line.indexOf("<@cross-ref-fig-open>");
					int indexCrossRefClose = line.indexOf("<@cross-ref-fig-close>");
					if ((indexCrossRefClose) > (indexCrossRefOpen)) {
						
						String boxLabel = line.substring((line.indexOf("<@cross-ref-fig-open>") + "<@cross-ref-fig-open>".length()), line.indexOf("<@cross-ref-fig-close>"));
//						bufferCalloutFigure.append("\nAlert "+(++Constants.alertIndex)+": AU: Please verify, in-text callout of figure \""+(boxLabel)+"\" is it correct?");
						
						if (floatcalloutFigureCount == 0) {
							
							bufferCalloutFigure.append("\nAlert "+(++Constants.alertIndex)+": AU: Please verify, in-text callout of figure is missing for ");
							bufferCalloutFigure.append("\"" + boxLabel + "\"");
						}  
							
						if (floatcalloutFigureCount > 0) {
							
							bufferCalloutFigure.append(", \"" + boxLabel + "\"");
						}
						
						floatcalloutFigureCount ++;
					}
				}
				if (line.startsWith("{CALLOUT BOX")) {
					
					line = fileWriterI.boxCrossMarking(line);
					int indexCrossRefOpen = line.indexOf("<@cross-ref-box-open>");
					int indexCrossRefClose = line.indexOf("<@cross-ref-box-close>");
					if ((indexCrossRefClose) > (indexCrossRefOpen)) {
						
						String boxLabel = line.substring((line.indexOf("<@cross-ref-box-open>") + "<@cross-ref-box-open>".length()), line.indexOf("<@cross-ref-box-close>"));
//						bufferCalloutBox.append("\nAlert "+(++Constants.alertIndex)+": AU: Please verify, in-text callout of box \""+(boxLabel)+"\" is it correct?");
						
						if (floatcalloutBoxCount == 0) {
							
							bufferCalloutBox.append("\nAlert "+(++Constants.alertIndex)+": AU: Please verify, in-text callout of box is missing for ");
							bufferCalloutBox.append("\"" + boxLabel + "\"");
						}  
							
						if (floatcalloutBoxCount > 0) {
							
							bufferCalloutBox.append(", \"" + boxLabel + "\"");
						}
						
						floatcalloutBoxCount ++;
					}
				}
				if (line.startsWith("{CALLOUT TABLE")) {
					
					line = fileWriterI.tableCrossMarking(line);
					int indexCrossRefOpen = line.indexOf("<@cross-ref-table-open>");
					int indexCrossRefClose = line.indexOf("<@cross-ref-table-close>");
					if ((indexCrossRefClose) > (indexCrossRefOpen)) {
						
						String boxLabel = line.substring((line.indexOf("<@cross-ref-table-open>") + "<@cross-ref-table-open>".length()), line.indexOf("<@cross-ref-table-close>"));
//						bufferCalloutTable.append("\nAlert "+(++Constants.alertIndex)+": AU: Please verify, in-text callout of table \""+(boxLabel)+"\" is it correct?");
						
						if (floatcalloutTableCount == 0) {
							
							bufferCalloutTable.append("\nAlert "+(++Constants.alertIndex)+": AU: Please verify, in-text callout of table is missing for ");
							bufferCalloutTable.append("\"" + boxLabel + "\"");
						}  
							
						if (floatcalloutTableCount > 0) {
							
							bufferCalloutTable.append(", \"" + boxLabel + "\"");
						}
						
						floatcalloutTableCount ++;
					}
				}
				if (line.startsWith("{CALLOUT VIDEO")) {
					
					line = fileWriterI.videoCrossMarking(line);
					int indexCrossRefOpen = line.indexOf("<@cross-ref-video-open>");
					int indexCrossRefClose = line.indexOf("<@cross-ref-video-close>");
					if ((indexCrossRefClose) > (indexCrossRefOpen)) {
						
						String boxLabel = line.substring((line.indexOf("<@cross-ref-video-open>") + "<@cross-ref-video-open>".length()), line.indexOf("<@cross-ref-video-close>"));
//						bufferCalloutVideo.append("\nAlert "+(++Constants.alertIndex)+": AU: Please verify, in-text callout of video \""+(boxLabel)+"\" is it correct?");
						
						if (floatcalloutVideoCount == 0) {
							
							bufferCalloutVideo.append("\nAlert "+(++Constants.alertIndex)+": AU: Please verify, in-text callout of video is missing for ");
							bufferCalloutVideo.append("\"" + boxLabel + "\"");
						}  
							
						if (floatcalloutVideoCount > 0) {
							
							bufferCalloutVideo.append(", \"" + boxLabel + "\"");
						}
						
						floatcalloutVideoCount ++;
					}
				}

			}
			
			
			String dataFloatBox = bufferFloatBox.toString();
			if (dataFloatBox.length() > 0)	dataFloatBox = dataFloatBox + ".";
			String dataFloatFigure = bufferFloatFigure.toString();
			if (dataFloatFigure.length() > 0)	dataFloatFigure = dataFloatFigure + ".";
			String dataFloatTable = bufferFloatTable.toString();
			if (dataFloatTable.length() > 0)	dataFloatTable = dataFloatTable + ".";
			String dataFloatVideo = bufferFloatVideo.toString();
			if (dataFloatVideo.length() > 0)	dataFloatVideo = dataFloatVideo + ".";
			
			
			
			String dataCalloutBox = bufferCalloutBox.toString();
			if (dataCalloutBox.length() > 0)	dataCalloutBox = dataCalloutBox + ".";
			String dataCalloutFigure = bufferCalloutFigure.toString();
			if (dataCalloutFigure.length() > 0)	dataCalloutFigure = dataCalloutFigure + ".";
			String dataCalloutTable = bufferCalloutTable.toString();
			if (dataCalloutTable.length() > 0)	dataCalloutTable = dataCalloutTable + ".";
			String dataCalloutVideo = bufferCalloutVideo.toString();
			if (dataCalloutVideo.length() > 0)	dataCalloutVideo = dataCalloutVideo + ".";
			
			bufferMain.append(dataFloatBox);
			bufferMain.append(dataFloatFigure);
			bufferMain.append(dataFloatTable);
			bufferMain.append(dataFloatVideo);
			
			bufferMain.append(dataCalloutBox);
			bufferMain.append(dataCalloutFigure);
			bufferMain.append(dataCalloutTable);
			bufferMain.append(dataCalloutVideo);
			
			
		} catch(Exception exception)	{	logger.error(exception.getMessage(), exception);	}
		finally {
			if (bufferedReader != null)
				try {	bufferedReader.close();	bufferedReader = null;	}catch(Exception exp) {exp.getMessage(); }
		}
		return bufferMain.toString();
	}
	
	
	/**
	 * alertsKeywordsAndAbstractFetch
	 * @param floatPath
	 * @return
	 */
	private String alertsKeywordsAndAbstractFetch(String floatPath) {
		
		BufferedReader bufferedReader =  null;
		String line = "";
		boolean isAbstract = false, isKeywords = false;
		StringBuffer buffer = new StringBuffer();
		
		if (new File(floatPath).exists())
		try {
			
			bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(new File(floatPath))));
			while ((line = bufferedReader.readLine()) != null) {
				
				if (line.toLowerCase().contains("abstract"))	{	isAbstract = true;	}
				if (line.toLowerCase().contains("keywords"))	{	isKeywords = true;	}
			}
			
			if (!isAbstract &! isKeywords) {
				buffer.append("\nAlert "+(++Constants.alertIndex)+": AU: Please provide “Abstract & Keywords” as applicable.");
			} else if (isAbstract &! isKeywords) {
				buffer.append("\nAlert "+(++Constants.alertIndex)+": AU: Please provide “Keywords” as applicable.");
			} else if (!isAbstract & isKeywords) {
				buffer.append("\nAlert "+(++Constants.alertIndex)+": AU: Please provide “Abstract” as applicable.");
			}
			
		} catch(Exception exception)	{	logger.error(exception.getMessage(), exception);	}
		finally {
			if (bufferedReader != null)
				try {	bufferedReader.close();	bufferedReader = null;	}catch(Exception exp) {exp.getMessage(); }
		}
		return buffer.toString();
	}
	
	
	
	public void setFileWriterI(FileWriterI fileWriterI) {
		this.fileWriterI = fileWriterI;
	}
}