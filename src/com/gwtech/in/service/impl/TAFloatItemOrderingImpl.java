package com.gwtech.in.service.impl;

import org.apache.log4j.Logger;

import com.gwtech.in.service.FileWriterI;
import com.gwtech.in.service.TAFloatItemOrdering;
import com.gwtech.in.utils.Constants;
import com.gwtech.in.utils.MiscUtility;

public class TAFloatItemOrderingImpl implements TAFloatItemOrdering {
	
	private static final Logger logger = Logger.getLogger(TAFloatItemOrderingImpl.class);
	private MiscUtility miscUtility;
	private FileWriterI fileWriterI;
	
	
	public static void main(String[] args) {
		
		try {
			new TAFloatItemOrderingImpl().crossRefsCallOutCheckLog("<@begin-para-first-line-indent>0.0<@$p><@begin-para-left-indent>0.0<@$p>@TXT1:Health and wellness has several dimensions, as mentioned below (<@cross-ref>Fig. 1.1<@cross-ref-close>).");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	
	
	@Override
	public String[] floatFigureItemCheckLog(String lineTxt, boolean writeStatus) throws Exception {
		
		String line = lineTxt;
		String[] floatItemArray = {"", ""};
		Boolean isFloatItem = false;
		String boxLabel = "";
		
		if ((line.toLowerCase().startsWith("fig")) || (line.toLowerCase().startsWith("<@bold-open>fig"))) { //Fig. 1.1
			
			if ((line.toLowerCase().startsWith("fig")) || (line.toLowerCase().startsWith("<@bold-open>fig"))) {
				if (writeStatus) {
					
					line = fileWriterI.figureCrossMarking(line);
//					String boxLabel = "";
					
					int indexCrossRefOpen = line.indexOf("<@cross-ref-fig-open>");
					int indexCrossRefClose = line.indexOf("<@cross-ref-fig-close>");
					
					if ((indexCrossRefClose) > (indexCrossRefOpen)) {
						
						boxLabel = line.substring((line.indexOf("<@cross-ref-fig-open>") + "<@cross-ref-fig-open>".length()), line.indexOf("<@cross-ref-fig-close>"));
						boxLabel = miscUtility.removeStartEndSpaces(boxLabel);
						
						fileWriterI.write(lineTxt + "\n", Constants.outputPath + "/Log-Report/float-item/figure-main.txt", true);
						isFloatItem = true;
					}
				}
//				isFloatItem = true;
			}
		}
		
		floatItemArray[0] = isFloatItem + "";
		floatItemArray[1] = boxLabel + "";
		
		return floatItemArray;
	}
	
	
	
	@Override
	public Boolean floatFigureSourceItemCheckLog(String lineTxt, boolean writeStatus, String figureLabel, String sourceFilePath)
			throws Exception {

		Boolean isFloatItem = false;

		if (writeStatus) {

			lineTxt = miscUtility.removeStartEndSpaces(lineTxt);
			lineTxt = miscUtility.removeExtraSpaces(lineTxt);
			lineTxt = miscUtility.removeReturnUnit(lineTxt);
			
			if (lineTxt.length() > 0) {
				fileWriterI.write(figureLabel + "=" + lineTxt + "\n", sourceFilePath, true);
				isFloatItem = true;
			}
		}

		return isFloatItem;
	}	
	
	
	@Override
	public String[] floatBoxItemCheckLog(String lineTxt, boolean writeStatus) throws Exception {
		
		String line = lineTxt;
		Boolean isFloatItem = false;
		String[] floatItemArray = {"", ""};
		String boxLabel = "";
		
		if ((line.toLowerCase().startsWith("box")) || (line.toLowerCase().startsWith("<@bold-open>box"))) { //Box. 1.1
			
			if ((line.toLowerCase().startsWith("box")) || (line.toLowerCase().startsWith("<@bold-open>box"))) {
				if (writeStatus) {
					
					
					line = fileWriterI.boxCrossMarking(line);
//					String boxLabel = "";
					
					int indexCrossRefOpen = line.indexOf("<@cross-ref-box-open>");
					int indexCrossRefClose = line.indexOf("<@cross-ref-box-close>");
					
					if ((indexCrossRefClose) > (indexCrossRefOpen)) {
						
						boxLabel = line.substring((line.indexOf("<@cross-ref-box-open>") + "<@cross-ref-box-open>".length()), line.indexOf("<@cross-ref-box-close>"));
						boxLabel = miscUtility.removeStartEndSpaces(boxLabel);
						
						fileWriterI.write(lineTxt + "\n", Constants.outputPath + "/Log-Report/float-item/box-main.txt", true);
						isFloatItem = true;
					}
				}
					
//					fileWriterI.write(line + "\n", Constants.outputPath + "/Log-Report/float-item/box-main.txt", true);
//				isFloatItem = true;
			}
		}
		
		floatItemArray[0] = isFloatItem + "";
		floatItemArray[1] = boxLabel + "";
		
		return floatItemArray;
	}

	
	@Override
	public String[] floatTableItemCheckLog(String lineTxt, boolean writeStatus) throws Exception {
		
		String line = lineTxt;
		Boolean isFloatItem = false;
		String[] floatItemArray = {"", ""};
		String boxLabel = "";
		
		if ((line.toLowerCase().startsWith("table")) || (line.toLowerCase().startsWith("<@bold-open>table"))) { //Table. 1.1
			
			if ((line.toLowerCase().startsWith("table")) || (line.toLowerCase().startsWith("<@bold-open>table"))) {
				if (writeStatus) {
					
					line = fileWriterI.tableCrossMarking(line);
//					String boxLabel = "";
					
					int indexCrossRefOpen = line.indexOf("<@cross-ref-table-open>");
					int indexCrossRefClose = line.indexOf("<@cross-ref-table-close>");
					
					if ((indexCrossRefClose) > (indexCrossRefOpen)) {
						
						boxLabel = line.substring((line.indexOf("<@cross-ref-table-open>") + "<@cross-ref-table-open>".length()), line.indexOf("<@cross-ref-table-close>"));
						boxLabel = miscUtility.removeStartEndSpaces(boxLabel);
						
						fileWriterI.write(lineTxt + "\n", Constants.outputPath + "/Log-Report/float-item/table-main.txt", true);
						isFloatItem = true;
					}
				}
//					fileWriterI.write(line + "\n", Constants.outputPath + "/Log-Report/float-item/table-main.txt", true);
//				isFloatItem = true;
			}
		}
		
		floatItemArray[0] = isFloatItem + "";
		floatItemArray[1] = boxLabel + "";
		
		return floatItemArray;
	}

	
	@Override
	public String[] floatVideoItemCheckLog(String lineTxt, boolean writeStatus) throws Exception {
		
		String line = lineTxt;
		Boolean isFloatItem = false;
		String[] floatItemArray = {"", ""};
		String boxLabel = "";
		
		if ((line.toLowerCase().startsWith("video")) || (line.toLowerCase().startsWith("<@bold-open>video"))) { //Video. 1.1
			
			if ((line.toLowerCase().startsWith("video")) || (line.toLowerCase().startsWith("<@bold-open>video"))) {
				if (writeStatus) {
					
					line = fileWriterI.videoCrossMarking(line);
//					String boxLabel = "";
					
					int indexCrossRefOpen = line.indexOf("<@cross-ref-video-open>");
					int indexCrossRefClose = line.indexOf("<@cross-ref-video-close>");
					
					if ((indexCrossRefClose) > (indexCrossRefOpen)) {
						
						boxLabel = line.substring((line.indexOf("<@cross-ref-video-open>") + "<@cross-ref-video-open>".length()), line.indexOf("<@cross-ref-video-close>"));
						boxLabel = miscUtility.removeStartEndSpaces(boxLabel);
						
						fileWriterI.write(lineTxt + "\n", Constants.outputPath + "/Log-Report/float-item/video-main.txt", true);
						isFloatItem = true;
					}
				}
//				fileWriterI.write(line + "\n", Constants.outputPath + "/Log-Report/float-item/video-main.txt", true);
//				isFloatItem = true;
			}
		}
		
		floatItemArray[0] = isFloatItem + "";
		floatItemArray[1] = boxLabel + "";
		
		return floatItemArray;
	}

	
	
	@Override
	public Boolean floatItemCheckLog(String lineTxt, boolean writeStatus) throws Exception {
		
		String line = lineTxt;
		Boolean isFloatItem = false;
		
		
		if ((line.toLowerCase().startsWith("fig")) || (line.toLowerCase().startsWith("<@bold-open>fig"))) { //Fig. 1.1
			
			if ((line.toLowerCase().startsWith("fig")) || (line.toLowerCase().startsWith("<@bold-open>fig"))) {
				if (writeStatus) {
					
					line = fileWriterI.figureCrossMarking(line);
//					String boxLabel = "";
					
					int indexCrossRefOpen = line.indexOf("<@cross-ref-fig-open>");
					int indexCrossRefClose = line.indexOf("<@cross-ref-fig-close>");
					
					if ((indexCrossRefClose) > (indexCrossRefOpen)) {
						
//						boxLabel = line.substring((line.indexOf("<@cross-ref-fig-open>") + "<@cross-ref-fig-open>".length()), line.indexOf("<@cross-ref-fig-close>"));
//						boxLabel = miscUtility.removeStartEndSpaces(boxLabel);
						
						fileWriterI.write(lineTxt + "\n", Constants.outputPath + "/Log-Report/float-item/figure-main.txt", true);
						isFloatItem = true;
					}
				}
//				isFloatItem = true;
			}
		}
		if ((line.toLowerCase().startsWith("box")) || (line.toLowerCase().startsWith("<@bold-open>box"))) { //Box. 1.1
			
			if ((line.toLowerCase().startsWith("box")) || (line.toLowerCase().startsWith("<@bold-open>box"))) {
				if (writeStatus) {
					
					
					line = fileWriterI.boxCrossMarking(line);
//					String boxLabel = "";
					
					int indexCrossRefOpen = line.indexOf("<@cross-ref-box-open>");
					int indexCrossRefClose = line.indexOf("<@cross-ref-box-close>");
					
					if ((indexCrossRefClose) > (indexCrossRefOpen)) {
						
						fileWriterI.write(lineTxt + "\n", Constants.outputPath + "/Log-Report/float-item/box-main.txt", true);
						isFloatItem = true;
					}
				}
					
//					fileWriterI.write(line + "\n", Constants.outputPath + "/Log-Report/float-item/box-main.txt", true);
//				isFloatItem = true;
			}
		}
		if ((line.toLowerCase().startsWith("table")) || (line.toLowerCase().startsWith("<@bold-open>table"))) { //Table. 1.1
			
			if ((line.toLowerCase().startsWith("table")) || (line.toLowerCase().startsWith("<@bold-open>table"))) {
				if (writeStatus) {
					
					line = fileWriterI.tableCrossMarking(line);
//					String boxLabel = "";
					
					int indexCrossRefOpen = line.indexOf("<@cross-ref-table-open>");
					int indexCrossRefClose = line.indexOf("<@cross-ref-table-close>");
					
					if ((indexCrossRefClose) > (indexCrossRefOpen)) {
						
						fileWriterI.write(lineTxt + "\n", Constants.outputPath + "/Log-Report/float-item/table-main.txt", true);
						isFloatItem = true;
					}
				}
//					fileWriterI.write(line + "\n", Constants.outputPath + "/Log-Report/float-item/table-main.txt", true);
//				isFloatItem = true;
			}
		}
		if ((line.toLowerCase().startsWith("video")) || (line.toLowerCase().startsWith("<@bold-open>video"))) { //Video. 1.1
			
			if ((line.toLowerCase().startsWith("video")) || (line.toLowerCase().startsWith("<@bold-open>video"))) {
				if (writeStatus) {
					
					line = fileWriterI.videoCrossMarking(line);
//					String boxLabel = "";
					
					int indexCrossRefOpen = line.indexOf("<@cross-ref-video-open>");
					int indexCrossRefClose = line.indexOf("<@cross-ref-video-close>");
					
					if ((indexCrossRefClose) > (indexCrossRefOpen)) {
						
						fileWriterI.write(lineTxt + "\n", Constants.outputPath + "/Log-Report/float-item/video-main.txt", true);
						isFloatItem = true;
					}
				}
//				fileWriterI.write(line + "\n", Constants.outputPath + "/Log-Report/float-item/video-main.txt", true);
//				isFloatItem = true;
			}
		}
		return isFloatItem;
	}
	
	
	@Override
	public void crossRefsCallOutCheckLog(String line) throws Exception {
		
		miscUtility = new MiscUtility();
		
		if (line.contains("Specific Details of the Chest Pain History"))
			logger.debug(line);

		
		if (line.toLowerCase().contains("<@cross-ref-fig-open>")) { //Fig. 1.1
			
			String mainLine = line;
			while(mainLine.toLowerCase().contains("<@cross-ref-fig-open>")) {
				
				String floatItemLabel = mainLine.substring((mainLine.indexOf("<@cross-ref-fig-open>") + ("<@cross-ref-fig-open>".length())), (mainLine.indexOf("<@cross-ref-fig-close>")));
 				String label = floatItemLabel;
				
// 				String[] result = isDelimeterFound(floatItemLabel);
//				if (Boolean.parseBoolean(result[0]))	floatItemLabel = floatItemLabel.replace(result[1], "");
 				
				floatItemLabel = miscUtility.removeStartEndSpaces(floatItemLabel);
				
				if (floatItemLabel.lastIndexOf(".") > 0)
					floatItemLabel = floatItemLabel.substring(floatItemLabel.lastIndexOf(".") + 1);
				else
					floatItemLabel = floatItemLabel.substring(floatItemLabel.lastIndexOf(" ") + 1);
				
				floatItemLabel = miscUtility.removeStartEndSpaces(floatItemLabel);
				
				if (floatItemLabel.contains("-"))	floatItemLabel = floatItemLabel.substring(floatItemLabel.lastIndexOf("-") + 1);
				if (floatItemLabel.contains("—"))	floatItemLabel = floatItemLabel.substring(floatItemLabel.lastIndexOf("—") + 1);
				if (floatItemLabel.contains("–"))	floatItemLabel = floatItemLabel.substring(floatItemLabel.lastIndexOf("–") + 1);
				if (floatItemLabel.contains("–"))	floatItemLabel = floatItemLabel.substring(floatItemLabel.lastIndexOf("–") + 1);
				
				if (miscUtility.isNumber(floatItemLabel)) {
					
					Integer val = Integer.parseInt(floatItemLabel);
					if (Constants.figureCallOut.contains(val) == false) {
						Constants.figureCallOut.add(val);
						fileWriterI.write(label + "\n", Constants.outputPath + "/Log-Report/float-item/figure-callout.txt", true);
					}
				}
				
				String tagText = "<@cross-ref-fig-open>" + label + "<@cross-ref-fig-close>";
				String prefixMainLine = mainLine.substring(0, mainLine.indexOf(tagText));
				String suffixMainLine = mainLine.substring(mainLine.indexOf(tagText) + (tagText.length()));
				
				mainLine = prefixMainLine + suffixMainLine;
			}
		}
		
		if (line.toLowerCase().contains("<@cross-ref-box-open>")) { //Box. 1.1
			
			String mainLine = line;
			while(mainLine.toLowerCase().contains("<@cross-ref-box-open>")) {
				
				String floatItemLabel = mainLine.substring((mainLine.indexOf("<@cross-ref-box-open>") + ("<@cross-ref-box-open>".length())), (mainLine.indexOf("<@cross-ref-box-close>")));
				String label = floatItemLabel;
				
//				String[] result = isDelimeterFound(floatItemLabel);
//				if (Boolean.parseBoolean(result[0]))	floatItemLabel = floatItemLabel.replace(result[1], "");
				
				floatItemLabel = miscUtility.removeStartEndSpaces(floatItemLabel);
				
				if (floatItemLabel.lastIndexOf(".") > 0)
					floatItemLabel = floatItemLabel.substring(floatItemLabel.lastIndexOf(".") + 1);
				else
					floatItemLabel = floatItemLabel.substring(floatItemLabel.lastIndexOf(" ") + 1);
				
				floatItemLabel = miscUtility.removeStartEndSpaces(floatItemLabel);
				
				if (floatItemLabel.contains("-"))	floatItemLabel = floatItemLabel.substring(floatItemLabel.lastIndexOf("-") + 1);
				if (floatItemLabel.contains("—"))	floatItemLabel = floatItemLabel.substring(floatItemLabel.lastIndexOf("—") + 1);
				if (floatItemLabel.contains("–"))	floatItemLabel = floatItemLabel.substring(floatItemLabel.lastIndexOf("–") + 1);
				if (floatItemLabel.contains("–"))	floatItemLabel = floatItemLabel.substring(floatItemLabel.lastIndexOf("–") + 1);
				
				if (miscUtility.isNumber(floatItemLabel)) {
					
					Integer val = Integer.parseInt(floatItemLabel);
					if (Constants.boxCallOut.contains(val) == false) {
						Constants.boxCallOut.add(val);
						fileWriterI.write(label + "\n", Constants.outputPath + "/Log-Report/float-item/box-callout.txt", true);
					}
				}
				
				String tagText = "<@cross-ref-box-open>" + label + "<@cross-ref-box-close>";
				String prefixMainLine = mainLine.substring(0, mainLine.indexOf(tagText));
				String suffixMainLine = mainLine.substring(mainLine.indexOf(tagText) + (tagText.length()));
				
				mainLine = prefixMainLine + suffixMainLine;
			}
		}
		
		
		if (line.toLowerCase().contains("<@cross-ref-table-open>")) { //Table. 1.1
			
			String mainLine = line;
			while(mainLine.toLowerCase().contains("<@cross-ref-table-open>")) {
			
				String floatItemLabel = mainLine.substring((mainLine.indexOf("<@cross-ref-table-open>") + ("<@cross-ref-table-open>".length())), (mainLine.indexOf("<@cross-ref-table-close>")));
				String label = floatItemLabel;
				
//				String[] result = isDelimeterFound(floatItemLabel);
//				if (Boolean.parseBoolean(result[0]))	floatItemLabel = floatItemLabel.replace(result[1], "");
				
				floatItemLabel = miscUtility.removeStartEndSpaces(floatItemLabel);
				
				if (floatItemLabel.lastIndexOf(".") > 0)
					floatItemLabel = floatItemLabel.substring(floatItemLabel.lastIndexOf(".") + 1);
				else
					floatItemLabel = floatItemLabel.substring(floatItemLabel.lastIndexOf(" ") + 1);
				
				floatItemLabel = miscUtility.removeStartEndSpaces(floatItemLabel);
				
				if (floatItemLabel.contains("-"))	floatItemLabel = floatItemLabel.substring(floatItemLabel.lastIndexOf("-") + 1);
				if (floatItemLabel.contains("—"))	floatItemLabel = floatItemLabel.substring(floatItemLabel.lastIndexOf("—") + 1);
				if (floatItemLabel.contains("–"))	floatItemLabel = floatItemLabel.substring(floatItemLabel.lastIndexOf("–") + 1);
				if (floatItemLabel.contains("–"))	floatItemLabel = floatItemLabel.substring(floatItemLabel.lastIndexOf("–") + 1);
				
				if (miscUtility.isNumber(floatItemLabel)) {
					
					Integer val = Integer.parseInt(floatItemLabel);
					if (Constants.tableCallOut.contains(val) == false) {
						Constants.tableCallOut.add(val);
						fileWriterI.write(label + "\n", Constants.outputPath + "/Log-Report/float-item/table-callout.txt", true);
					}
				}
				
				String tagText = "<@cross-ref-table-open>" + label + "<@cross-ref-table-close>";
				String prefixMainLine = mainLine.substring(0, mainLine.indexOf(tagText));
				String suffixMainLine = mainLine.substring(mainLine.indexOf(tagText) + (tagText.length()));

				mainLine = prefixMainLine + suffixMainLine;
			}
		}
		
		if (line.toLowerCase().contains("<@cross-ref-video-open>")) { //Video. 1.1
			
			String mainLine = line;
			while(mainLine.toLowerCase().contains("<@cross-ref-video-open>")) {
				
				String floatItemLabel = mainLine.substring((mainLine.indexOf("<@cross-ref-video-open>") + ("<@cross-ref-video-open>".length())), (mainLine.indexOf("<@cross-ref-video-close>")));
				String label = floatItemLabel;
				
//				String[] result = isDelimeterFound(floatItemLabel);
//				if (Boolean.parseBoolean(result[0]))	floatItemLabel = floatItemLabel.replace(result[1], "");
				
				floatItemLabel = miscUtility.removeStartEndSpaces(floatItemLabel);
				
				if (floatItemLabel.lastIndexOf(".") > 0)
					floatItemLabel = floatItemLabel.substring(floatItemLabel.lastIndexOf(".") + 1);
				else
					floatItemLabel = floatItemLabel.substring(floatItemLabel.lastIndexOf(" ") + 1);
				
				floatItemLabel = miscUtility.removeStartEndSpaces(floatItemLabel);
				
				if (floatItemLabel.contains("-"))	floatItemLabel = floatItemLabel.substring(floatItemLabel.lastIndexOf("-") + 1);
				if (floatItemLabel.contains("—"))	floatItemLabel = floatItemLabel.substring(floatItemLabel.lastIndexOf("—") + 1);
				if (floatItemLabel.contains("–"))	floatItemLabel = floatItemLabel.substring(floatItemLabel.lastIndexOf("–") + 1);
				if (floatItemLabel.contains("–"))	floatItemLabel = floatItemLabel.substring(floatItemLabel.lastIndexOf("–") + 1);
				
				if (miscUtility.isNumber(floatItemLabel)) {
					
					Integer val = Integer.parseInt(floatItemLabel);
					if (Constants.videoCallOut.contains(val) == false) {
						Constants.videoCallOut.add(val);
						fileWriterI.write(label + "\n", Constants.outputPath + "/Log-Report/float-item/video-callout.txt", true);
					}
				}
				
				String tagText = "<@cross-ref-video-open>" + label + "<@cross-ref-video-close>";
				String prefixMainLine = mainLine.substring(0, mainLine.indexOf(tagText));
				String suffixMainLine = mainLine.substring(mainLine.indexOf(tagText) + (tagText.length()));

				mainLine = prefixMainLine + suffixMainLine;
			}
		}
	
	}
	
	
	/**
	private String[] isDelimeterFound(String floatItemLabel) {
		
		String[] isDelimeter = {"false", "false"};
		try {
			for (String delimeter: Constants.delimeters) {
				
				if (floatItemLabel.toLowerCase().contains(delimeter)) {
					isDelimeter[0] = "true";
					isDelimeter[1] = delimeter;
					break;
				}
			}
		} catch(Exception exception) {	exception.printStackTrace();	}
		return isDelimeter;
	}
	*/
	
	
	public void setMiscUtility(MiscUtility miscUtility) {
		this.miscUtility = miscUtility;
	}

	public void setFileWriterI(FileWriterI fileWriterI) {
		this.fileWriterI = fileWriterI;
	}

}
