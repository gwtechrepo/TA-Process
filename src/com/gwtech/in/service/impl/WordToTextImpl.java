package com.gwtech.in.service.impl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.json.simple.JSONArray;

import com.aspose.words.Cell;
import com.aspose.words.Comment;
import com.aspose.words.ControlChar;
import com.aspose.words.ConvertUtil;
import com.aspose.words.Document;
import com.aspose.words.DocumentBuilder;
import com.aspose.words.Field;
import com.aspose.words.FindReplaceDirection;
import com.aspose.words.FindReplaceOptions;
import com.aspose.words.ListLabel;
import com.aspose.words.Node;
import com.aspose.words.NodeCollection;
import com.aspose.words.NodeType;
import com.aspose.words.Orientation;
import com.aspose.words.PaperSize;
import com.aspose.words.Paragraph;
import com.aspose.words.Row;
import com.aspose.words.Run;
import com.aspose.words.SaveFormat;
import com.aspose.words.Table;
import com.aspose.words.ViewType;
import com.gwtech.in.service.AlertsQuery;
import com.gwtech.in.service.FileWriterI;
import com.gwtech.in.service.FontProperties;
import com.gwtech.in.service.HeaderFooterOperation;
import com.gwtech.in.service.TAChapOutline;
import com.gwtech.in.service.TAFloatItemOrdering;
import com.gwtech.in.service.TALogReport;
import com.gwtech.in.service.WordToText;
import com.gwtech.in.utils.Constants;
import com.gwtech.in.utils.MiscUtility;



@SuppressWarnings({ "rawtypes", "unchecked" })
public class WordToTextImpl implements WordToText {

	private static final Logger logger = Logger.getLogger(WordToTextImpl.class);
	private FileWriterI fileWriterI;
	private MiscUtility miscUtility;
	private JSONArray array;
	private FontProperties fontProperties;
	private TAChapOutline taChapOutline;
	private TAFloatItemOrdering taFloatItemOrdering;
	private TALogReport taLogReport;
	private AlertsQuery alertsQuery;
	private HeaderFooterOperation headerFooterOperation;
	private ReplaceEvaluatorFindAndHighlight replaceEvaluatorFindAndHighlight;
	
	private StringBuffer commentsBuffer = new StringBuffer();
	
	public void init(String fileName) throws Exception {
		
		logger.info("*******************************************************************");
		logger.info("inside init operation WordToTextImpl : ------------------------>>" + fileName);
		logger.info("*******************************************************************");
		
		Constants.contentsLabel = false;
		Constants.fileName = "";
		Constants.rootPath = "";
		Constants.ISBN = "";
		Constants.chapterPrefix = "";
		Constants.chapOutlineBuff = new StringBuffer();
		Constants.floatFigureBuff = new StringBuffer();
		Constants.headLevelIndex = 0;
		Constants.figureCallOut = new ArrayList<Integer>(0);
		Constants.boxCallOut = new ArrayList<Integer>(0);
		Constants.tableCallOut = new ArrayList<Integer>(0);
		Constants.videoCallOut = new ArrayList<Integer>(0);
//		Constants.delimeters = {"and", "to", "through", "-", "–", ","};
		Constants.alertIndex = 0;
		Constants.displayParaStyle = "";
		Constants.calloutFigureFixingNote = new ArrayList<String>(0);
		Constants.calloutBoxFixingNote = new ArrayList<String>(0);
		Constants.calloutTableFixingNote = new ArrayList<String>(0);
		Constants.calloutVideoFixingNote = new ArrayList<String>(0);
	}
	
	public void createResources() throws Exception {
		
		miscUtility.DeleteFileFolder(Constants.outputPath + "/Log-Report/");
		miscUtility.DeleteFileFolder(Constants.outputPath + "/Images/");
		miscUtility.DeleteFileFolder(Constants.outputPath + "/Resource/");
		
		miscUtility.createDirIfNotExist(Constants.outputPath + "/Log-Report/float-log/");
		miscUtility.createDirIfNotExist(Constants.outputPath + "/Log-Report/float-item/");
		miscUtility.createDirIfNotExist(Constants.outputPath + "/Images/");
		miscUtility.createDirIfNotExist(Constants.outputPath + "/Resource/");
	}
	
	public void exit(String fileName) {
		
		logger.info("*******************************************************************");
		logger.info("FilterTAFace exit conversion done for: " + fileName);
		logger.info("*******************************************************************");
	}
	
	public void removeAllComments(Document doc) {
		
		NodeCollection comments = doc.getChildNodes(NodeType.COMMENT, true);
		for (int i = comments.getCount() - 1; i >= 0; i--) {
		    Comment comment = (Comment) comments.get(i);
		    comment.remove();
		}
	}
	
	public void collectComments(Document doc) throws Exception {
		
		NodeCollection comments = doc.getChildNodes(NodeType.COMMENT, true);
		
		for (int i = 0; i < comments.getCount(); i ++) {
			
			Comment comment = (Comment) comments.get(i);
			String commentData = comment.getText();
			
			NodeCollection runs = comment.getChildNodes(NodeType.RUN, true);
			StringBuffer commentBuffer = new StringBuffer();
			
        	for (Run run : (Iterable<Run>) runs) {
        		
        		String commentText = run.getText();
        		while(commentText.startsWith(" ")) commentText = commentText.substring(1);
        		commentText = fontProperties.fetchFontStyleScript(run, commentText, run.getFont().getStyle().getName(), "");
        		Boolean isValid = isAlphaInLine(commentText);
        		if (isValid)
        			commentBuffer.append(commentText);
        	}
			
        	commentData = commentBuffer.toString();
//        	commentData = HttpRequestImpl.checkSymbolWithUnicode(array, commentData);
        	
        	String data = commentData;
        	data = replaceExtraSpaces(data);
        	Boolean isValid = isAlphaInLine(data);
    		if (isValid)
    			commentsBuffer.append(data+"\n");
		}
	}
	
	
	public String deleteAllComments(String docFile) throws Exception {
		
		Document document = new Document(docFile);
		NodeCollection comments = document.getChildNodes(NodeType.COMMENT, true);
		comments.clear();
		document.save(docFile);
		return docFile;
	}
	
	private Boolean isAlphaInLine(String line) {
		
        String[] smalAlpha = {"q","w","e","r","t","y","u","i","o","p","a","s","d","f","g","h","j","k","l","z","x","c","v","b","n","m"};
        String[] smalnum = {"1", "2", "3", "4", "5", "6", "7" ,"8", "9", "0", "~", "!", "@", "#", "$", "%", "^", "&", "*", "(", ")", "_", "+", "=", ":", "'", ",", ".", "/", "\"", "<", ">", "?", "\\"};
        Boolean valid = false;
        
        for (String str : smalAlpha) {
        	
        	if (line.toLowerCase().contains(str)) {
        		valid = true;
        		break;
        	}
        }
        
        for (String str : smalnum) {
        	
        	if (line.toLowerCase().contains(str)) {
        		valid = true;
        		break;
        	}
        }
        
        return valid;
	}
	
	

	public void simpleWordText(String docFile, String bodyFile, String refsFile) throws Exception {
		
		array = HttpRequestImpl.get("auth/universal-char-request?type=universal");
		if (Constants.taSettingForUser.isRemoveHeadersFooters())	headerFooterOperation.removeFooterFromDocFile(docFile, docFile);
		
		Document doc = new Document(docFile);
		doc.getRange().getFormFields().clear(); // Unlink a field
		
		RemoveHiddenContentVisitor hiddenContentRemover = new RemoveHiddenContentVisitor();
	    doc.accept(hiddenContentRemover);
	    doc.acceptAllRevisions();
	    doc.removeMacros();
		
		docFile = coverTAFiltering(docFile, docFile, false);
		if (Constants.taSettingForUser.isRemoveHeadersFooters())
			headerFooterOperation.removeFooterFromDocFile(docFile, docFile);
		
//		collectComments(doc);
		
		StringBuffer bodyPart = new StringBuffer();
		StringBuffer refsPart = new StringBuffer();
		Integer paraIndex = 0, tableIndex = 0, paraIndexinTable = 0;
		
		NodeCollection nodes = doc.getChildNodes(NodeType.ANY, true);
		
		for (Node node : (Iterable<Node>) nodes) {
			
			if (node.getNodeType() == NodeType.TABLE) {
				
				Table table = (Table) doc.getChild(NodeType.TABLE, tableIndex, true);
				
			    for (Row row : table.getRows()) {
			    	
			        for (Cell cell : row.getCells()) {
			        	
			        	paraIndexinTable = cell.getParagraphs().getCount() + paraIndexinTable;
			        	String cellText = cell.getText();
//			        	String rowStyleStyle = cell.getFirstParagraph().getParagraphFormat().getStyle().getName();
			        	
			        	if ((cellText.length() > 0) & (isAlphaInLine(cellText))) {
			        		cellText = miscUtility.removeExtraSpaces(cellText);
			        		bodyPart.append(cellText + " ");
			        	}
			        }
			        bodyPart.append("\n");
			    }

				tableIndex++;
				
			} else if ((node.getNodeType() == NodeType.PARAGRAPH)) {
				
				Paragraph paragraph = (Paragraph) doc.getChild(NodeType.PARAGRAPH, paraIndex, true);
				if (paragraph != null) {
					
					if (paraIndexinTable > 0) {
						paraIndexinTable--;
						paraIndex++;
						continue;
					}
					
					String paraStyle = paragraph.getParagraphFormat().getStyle().getName();
					String paraText = paragraph.getText();
					String listLabelObj = "";
					
					if (paragraph.getListFormat().isListItem()) {
				        ListLabel label = paragraph.getListLabel();
				        listLabelObj = label.getLabelString();
				    }
					
					if (
							(paraStyle.toLowerCase().contains("refs")) 
							|| (paraStyle.toLowerCase().contains("bibliography"))
							|| (paraStyle.toLowerCase().contains("references"))
							) {
						if ((paraText.length() > 0) & (isAlphaInLine(paraText))) {
							
							paraText = fetchParagraphNormalTextFromDoc(paragraph, listLabelObj);
							paraText = miscUtility.replaceUnknownCharBox(paraText);
							paraText = miscUtility.removeExtraSpaces(paraText);
							
							List<String> splitList = miscUtility.createNewParaForSoftbreakentry(paraText);
							if (splitList.size() > 0) {
								
								for (String obj : splitList) {
									
									obj = obj.replace("[[[SOFT-BREAK-ENTRY]]]", "");
									refsPart.append(obj + "\n");
								}
							} else {
								
								refsPart.append(paraText + "\n");
							}
						}
					} else {
						if ((paraText.length() > 0) & (isAlphaInLine(paraText))) {
							
							paraText = fetchParagraphNormalTextFromDoc(paragraph, listLabelObj);
							paraText = miscUtility.replaceUnknownCharBox(paraText);
							paraText = miscUtility.removeExtraSpaces(paraText);
							
							List<String> splitList = miscUtility.createNewParaForSoftbreakentry(paraText);
							if (splitList.size() > 0) {
								
								for (String obj : splitList) {
									
									obj = obj.replace("[[[SOFT-BREAK-ENTRY]]]", "");
									bodyPart.append(obj + "\n");
								}
							} else {
								
								bodyPart.append(paraText + "\n");
							}
						}
					}
					paraIndex++;
				}
			} 
				
		}
		
		String bodyPartObj = bodyPart.toString();
//		if (commentsBuffer.toString().length() > 0)
//			bodyPartObj = bodyPartObj + commentsBuffer.toString();
		if (bodyPartObj.length() > 0)
			fileWriterI.write(bodyPartObj, bodyFile, false);
		
		String refsPartObj = refsPart.toString();
		if (refsPartObj.length() > 0)
		fileWriterI.write(refsPartObj, refsFile, false);
		
	}
	
	
	/**
	 * conversion word to text file with style tagging
	 * 
	 */
	public void conversion(String docFile, String outputFile, String outputFileDocx, Boolean isHyperlinksActive, Boolean isWordDownloadActive) 
			throws Exception {
		
		String fileName = "";
//		Boolean tableActive = false, paraActive = false;
		Constants.contentsLabel = false;
		String previousFloatFigLabel = "", previousFloatTableLabel = "", previousFloatBoxLabel = "", previousFloatVideoLabel = "";
		array = HttpRequestImpl.get("auth/universal-char-request?type=universal");
		
		if (Constants.taSettingForUser.isRemoveHeadersFooters())
			headerFooterOperation.removeFooterFromDocFile(docFile, docFile);
		
		Document doc = new Document(docFile);
//		doc.getRange().getFormFields().clear(); // Unlink a field
		
//		RemoveHiddenContentVisitor hiddenContentRemover = new RemoveHiddenContentVisitor();
//	    doc.accept(hiddenContentRemover);
//	    doc.acceptAllRevisions();
//	    doc.removeMacros();
		
//		docFile = coverTAFiltering(docFile, outputFileDocx, true);
	    docFile = coverTAFiltering(docFile, outputFileDocx, false);
		
//		Document document = new Document(docFile);
//		document.getPageCount();
//		File pdfFile = new File(docFile);
//		fileName = pdfFile.getName().substring(0, (pdfFile.getName().indexOf("."))) + ".pdf";
		
//		com.aspose.words.PdfSaveOptions options = new com.aspose.words.PdfSaveOptions();
//		options.setCompliance(PdfCompliance.PDF_15);
//		document.save((Constants.outputPath) + "/" + (fileName), options);
//		Set<Integer> refsPages = miscUtility.fetchPageNumberByTextSearch(Constants.outputPath + "/" + fileName, "@REFS-BEGINS@");
//		miscUtility.writesPagesOfRefsToTextFile(refsPages, Constants.outputPath+"/refspageinfo.txt");
//		document = miscUtility.applyRefsFiltering(document);
//		document.save(docFile);
//		miscUtility.DeleteFileFolder((Constants.outputPath) + "/" + (fileName));
		
		if (Constants.taSettingForUser.isRemoveHeadersFooters()) {
			headerFooterOperation.removeFooterFromDocFile(docFile, docFile);
			headerFooterOperation.addHeaderInfToDocument(docFile, docFile);
		}
		
		
		
//	    doc.getRange().replace("", "<@open-box>□<@$p>", new FindReplaceOptions(FindReplaceDirection.FORWARD));
		collectComments(doc);   // collect all comments
//		removeAllComments(doc); // remove comments
		
		
		
		StringBuffer buffer = new StringBuffer();
		Integer paraIndex = 0, tableIndex = 0, paraIndexinTable = 0, cellParaStyleCount = 0;
//		int imageIndex = 0;

		NodeCollection nodes = doc.getChildNodes(NodeType.ANY, true);
//		DocumentBuilder builder = new DocumentBuilder(doc);
		String rowStyle = "", rowStyleStyle = "";
		Boolean isFloatFigureItem = false, isFloatBoxItem = false, isFloatTableItem = false, isFloatVideoItem = false;
		Boolean isFloatFigureSrcItem = false, isFloatBoxSrcItem = false, isFloatTableSrcItem = false, isFloatVideoSrcItem = false;
		Boolean isFloatFigureAltItem = false, isFloatBoxAltItem = false, isFloatTableAltItem = false, isFloatVideoAltItem = false;
		Boolean isFloatFigureCapItem = false, isFloatBoxCapItem = false, isFloatTableCapItem = false, isFloatVideoCapItem = false;

		for (Node node : (Iterable<Node>) nodes) {
			
			if (node.getNodeType() == NodeType.TABLE) {
				
				Table table = (Table) doc.getChild(NodeType.TABLE, tableIndex, true);
				
				Integer rowIndex = 0;
				
				for (Row row : table.getRows()) {
			    	
					NodeCollection runs = null;
			    	
			    	for ( Cell cell: row.getCells()) {
			    		
			    		runs = cell.getChildNodes(NodeType.RUN, true);
			    		rowStyleStyle = cell.getFirstParagraph().getParagraphFormat().getStyle().getName();
			    		
			    		if (rowStyleStyle.contains("No Paragraph Style"))	rowStyleStyle = "";
			    		
			    		if (rowStyleStyle.length() > 0) {
					    	
			    			rowStyle = "@" + rowStyleStyle + ":";
			    			buffer.append(rowStyle);
			    			break;
					    }
			    	}
			    	
			    	
			    	
			    	StringBuffer cellBuffer = new StringBuffer();
			    	Integer cellParaindex = 0, tableCellIndex = 0;
			    	Boolean lineBreak = false;
			    	
			        for (Cell cell : row.getCells()) {
			        	
			        	Constants.styleTagStack = new ArrayList<String>();
			        	runs = cell.getChildNodes(NodeType.ANY, true);
			        	String cellText = cell.getText();
			        	cellText = cellText.replace("<@SOFT-BREAK/>", "<@BREAK/>");
			        	
			        	if (cellText.contains("Arthroconidia with or without budding yeast cells"))
							logger.debug("Arthroconidia with or without budding yeast cells");
			        	
			        	/**
						 * un-num/display start float items
						 * 
						 */
			        	String unNumCellText = "";
			        	unNumCellText = miscUtility. removeStartEndSpaces(cellText);
			        	unNumCellText = miscUtility. removeReturnUnit(cellText);
			        	unNumCellText = miscUtility. removeExtraSpaces(cellText);
			        	
			        	unNumCellText = unNumCellText.replaceAll("\r", "\n");
			        	unNumCellText = unNumCellText.replaceAll("\f", "\n");
			        	unNumCellText = unNumCellText.replaceAll("\t", "\n");

			        	
			        	if (Constants.isReportingFace) {
			        		
			        		String[] splitTableCell = unNumCellText.split("\n");
			        		for (String tableCellval: splitTableCell) {
			        			
						        if ((tableCellval.toLowerCase().contains("<insert ")) & (tableCellval.toLowerCase().contains("unn fig"))) {
									
									fileWriterI.write("\n"+tableCellval, Constants.outputPath + "/Resource/figure-unnum.txt", true);
								}
						        if ((tableCellval.toLowerCase().contains("<insert ")) & (tableCellval.toLowerCase().contains("unn table"))) {
									
									fileWriterI.write("\n"+tableCellval, Constants.outputPath + "/Resource/table-unnum.txt", true);
								}
						        if ((tableCellval.toLowerCase().contains("<insert ")) & (tableCellval.toLowerCase().contains("unn box"))) {
									
									fileWriterI.write("\n"+tableCellval, Constants.outputPath + "/Resource/box-unnum.txt", true);
								}
						        if ((tableCellval.toLowerCase().contains("<insert ")) & (tableCellval.toLowerCase().contains("unn video"))) {
									
									fileWriterI.write("\n"+tableCellval, Constants.outputPath + "/Resource/video-unnum.txt", true);
								}
			        		}
				        }
			        	
			        	/**
						 * un-num/display ends float items
						 * 
						 */
			        	
			        	if (isWordDownloadActive) {
			        	
			        		paraIndexinTable = cell.getParagraphs().getCount() + paraIndexinTable;
			        		cellParaStyleCount = cell.getParagraphs().getCount();
			        		
				        	Double width = cell.getCellFormat().getWidth(),
				        			rightPadding = cell.getCellFormat().getRightPadding(),
				        			rowHeight = row.getRowFormat().getHeight();
				        	
				        	Integer 
				        			verAlign = cell.getCellFormat().getVerticalAlignment(), 
				        			horMerge = cell.getCellFormat().getHorizontalMerge(), 
				        			VerMerge = cell.getCellFormat().getVerticalMerge(),
				        			orientation = cell.getCellFormat().getOrientation();
				        	
				        	String tableCellParaStyle = "";
				        	String tableCellParaLeftIndent = "0.0", tableCellParaFirstLineIndent = "0.0";
				        	cell.getFirstParagraph().getParagraphFormat().getStyle().getName();
				        	
				        	if (cell.getParagraphs().get(tableCellIndex) != null) {
				        		
				        		if (cell.getParagraphs().get(tableCellIndex).getParagraphFormat().getStyle() != null)
				        			tableCellParaStyle = cell.getParagraphs().get(tableCellIndex).getParagraphFormat().getStyle().getName();
				        		
					        	if (tableCellParaStyle.contains("No Paragraph Style"))	tableCellParaStyle = "";
					        	
					        	tableCellParaLeftIndent = cell.getParagraphs().get(tableCellIndex).getParagraphFormat().getLeftIndent() + "";
					        	tableCellParaFirstLineIndent = cell.getParagraphs().get(tableCellIndex).getParagraphFormat().getFirstLineIndent() + "";
					        	
				        	} else if (cell.getParagraphs().get(0) != null) {
				        		
				        		if (cell.getParagraphs().get(0).getParagraphFormat().getStyle() != null)
				        			tableCellParaStyle = cell.getParagraphs().get(0).getParagraphFormat().getStyle().getName();
				        		
					        	if (tableCellParaStyle.contains("No Paragraph Style"))	tableCellParaStyle = "";
					        	
					        	tableCellParaLeftIndent = cell.getParagraphs().get(0).getParagraphFormat().getLeftIndent() + "";
					        	tableCellParaFirstLineIndent = cell.getParagraphs().get(0).getParagraphFormat().getFirstLineIndent() + "";
				        	}
				        	
				        	
				        	
				        	if (tableCellParaStyle.length() > 0)								cellBuffer.append("<@cell-para-style-open>"+(tableCellParaStyle)+"<@cell-para-style-close>");
				        	if (tableCellParaLeftIndent.equalsIgnoreCase("0.0") == false)		cellBuffer.append("<@cell-para-left-indent-open>"+(tableCellParaLeftIndent)+"<@cell-para-left-indent-close>");
				        	if (tableCellParaFirstLineIndent.equalsIgnoreCase("0.0") == false)	cellBuffer.append("<@cell-para-first-line-indent>"+(tableCellParaFirstLineIndent)+"<@$p>");
				        	if (width > 0)														cellBuffer.append("<@cell-width-open>"+(width)+"<@cell-width-close>");
				        	if (verAlign > 0)													cellBuffer.append("<@cell-ver-align-open>"+(verAlign)+"<@cell-ver-align-close>");
				        	if (horMerge > 0)													cellBuffer.append("<@cell-hor-merge-open>"+(horMerge)+"<@cell-hor-merge-close>");
				        	if (VerMerge > 0)													cellBuffer.append("<@cell-ver-merge-open>"+(VerMerge)+"<@cell-ver-merge-close>");
				        	if (rightPadding > 0)												cellBuffer.append("<@cell-right-padding-open>"+(rightPadding)+"<@cell-right-padding-close>");
				        	if (orientation > 0)												cellBuffer.append("<@cell-orientation-open>"+(orientation)+"<@cell-orientation-close>");
				        	if (rowHeight > 0)													cellBuffer.append("<@row-height-open>"+(rowHeight)+"<@row-height-close>");
			        	}
			        	
			        	
			        	
			        	String tableCellText = "", hyperlink = "", listLabelObj = "";
			        	
			        	lineBreak = false;
			        	
			        	for (Node runNode : (Iterable<Node>) runs) {
			        		
			        		if (runNode.getNodeType() == NodeType.PARAGRAPH) {
			        			
			        			if (lineBreak)	cellBuffer.append("<@BREAK/>");
				        		if (lineBreak == false)	lineBreak = true;
				        		
			        		} else if (runNode.getNodeType() == NodeType.SPECIAL_CHAR) {
			        			
			        			tableCellText = runNode.getText();
			        			tableCellText = tableCellText.replace(".eps.jpg", ".jpg");
								
								try {
//									tableCellText = HttpRequestImpl.checkSymbolWithUnicode(array, tableCellText);
								} catch (Exception exception) {
									logger.error(exception.getMessage(), exception);
								}
								
								
								tableCellText = replaceExtraText(tableCellText);
								List<String> links = extractUrls(tableCellText);
								
								if (links.size() > 0) {

									StringBuffer bufferTemp = new StringBuffer();
									String data = tableCellText;
									for (String link: links) {
										
										String prefixData = data.substring(0, ((data.indexOf(link)) + (link.length())));
										data = data.substring(prefixData.length());
										prefixData = prefixData.replace(link, "<@link-text-open>" + link + "<@link-text-close>");
										bufferTemp.append(prefixData);
										
									}
									bufferTemp.append(data);
									tableCellText = bufferTemp.toString();
								
								}
								
//								tableCellText = HttpRequestImpl.checkSymbolWithUnicode(array, tableCellText);
								
								cellBuffer.append(tableCellText);
								cellParaindex++;
								
							} else if (runNode.getNodeType() == NodeType.RUN){
				        		
								Run run = (Run)runNode;
								
								tableCellText = run.getText();
								
								tableCellText = tableCellText.replace(".eps.jpg", ".jpg");
								tableCellText = tableCellText.replace("<@SOFT-BREAK/>", "<@BREAK/>");
								
								if (Constants.taSettingForUser.isSymbolMissing()) {
									tableCellText = tableCellText.replace("", "<@alert-red-symbolbox-open>[x]<@alert-red-symbolbox-close>");
//									tableCellText = tableCellText.replace("", "<@alert-red-symbolbox-open>[x]<@alert-red-symbolbox-close>");
								}
//								if (tableCellText.contains(""))
//									System.out.println("");
								
								tableCellText = replaceExtraText(tableCellText);
								List<String> links = extractUrls(tableCellText);
								
								if (links.size() > 0) {
									
									StringBuffer bufferTemp = new StringBuffer();
									String data = tableCellText;
									for (String link: links) {
										
										String prefixData = data.substring(0, ((data.indexOf(link)) + (link.length())));
										data = data.substring(prefixData.length());
										prefixData = prefixData.replace(link, "<@link-text-open>" + link + "<@link-text-close>");
										bufferTemp.append(prefixData);
										
									}
									bufferTemp.append(data);
									tableCellText = bufferTemp.toString();
								}
								
								if (isWordDownloadActive) {
									
//									Double leftIndent = run.getParentParagraph().getParagraphFormat().getLeftIndent();
//									if (leftIndent > 0)	cellBuffer.append("<@cell-left-indent-open>"+(leftIndent)+"<@cell-left-indent-close>");
								}
								
								if ((tableCellText.contains("HYPERLINK "))) {
									hyperlink = tableCellText.replace(" HYPERLINK \"", "").replace("\" ", "");
									continue;
								}
								
								String checkMathSymbol = run.getFont().getStyle().getName();
								
								if (checkMathSymbol.equalsIgnoreCase("Default Paragraph Font")) {
									checkMathSymbol = "";
								}
								
								
								if (checkMathSymbol.toLowerCase().contains("symbol")) {
									checkMathSymbol = run.getFont().getName().toLowerCase();
									checkMathSymbol = checkMathSymbol.replace(" ", "").replace("_", "").replace("-", "");
								} else if (run.getFont().getName().toLowerCase().contains("symbol")){
									
									checkMathSymbol = run.getFont().getName().toLowerCase();
									checkMathSymbol = checkMathSymbol.replace(" ", "").replace("_", "").replace("-", "");
								}
								
								if (tableCellText.contains("<@alert-red-symbolbox-open>") == false)
									tableCellText = fontProperties.fetchFontStyleScript(run, tableCellText, checkMathSymbol, hyperlink);
								
								tableCellText = tableCellText.replaceAll("\t", "    ");
								
								if (isWordDownloadActive & ((cellParaindex < cellParaStyleCount))) {
									String paraNestStyle = cell.getParagraphs().get(cellParaindex).getParagraphFormat().getStyle().getName();
									if ((paraNestStyle.length() > 0) & (rowStyleStyle.equalsIgnoreCase(paraNestStyle) == false)) {
										cellBuffer.append("<#$REMOVE-HEAD/>");
									}
								}
								
//								tableCellText = HttpRequestImpl.checkSymbolWithUnicode(array, tableCellText);
								
								if (cell.getFirstParagraph().getListFormat().isListItem()) {
							        
									ListLabel label = cell.getFirstParagraph().getListLabel();
							    	listLabelObj = label.getLabelString();
							        
							    	if (listLabelObj.length() > 0)
							    		tableCellText = listLabelObj + "\t" +tableCellText;
							    }
								
								cellBuffer.append(tableCellText);
								cellParaindex++;
							}
			        	}
			        	
			        	if (Constants.styleTagStack.size() > 0) {
			    			
			    			StringBuffer tagNameBuff = new StringBuffer();
			    			tagNameBuff.append("<@");
			    			
			    			for (int index = 0; index < Constants.styleTagStack.size(); index ++) {
			    				tagNameBuff.append(Constants.styleTagStack.get(index));
			    			}
			    			
			    			tagNameBuff.append("-close>");
			    			cellBuffer.append(tagNameBuff.toString());
			    		}
			        	
			        	tableCellIndex++;
			        	
			        	if ((row.getCells().getCount()) > tableCellIndex)
			        		cellBuffer.append("\t");
			        }
			        
			        String cellText = cellBuffer.toString().replace("", "");
			        String allcelldata = cellText;
			        buffer.append(allcelldata + "\n");
			        
			        rowIndex++;
			    }

				tableIndex++;
				
				
			} else if ((node.getNodeType() == NodeType.PARAGRAPH)) {
				
				Paragraph paragraph = (Paragraph) doc.getChild(NodeType.PARAGRAPH, paraIndex, true);
				
				if (paragraph != null) {
					
					String paraStyle = paragraph.getParagraphFormat().getStyle().getName();
					
					Double leftIndent = paragraph.getParagraphFormat().getLeftIndent();
					Double firstLineIndent = paragraph.getParagraphFormat().getFirstLineIndent();
					
					
					
					String leftIndentText = "", firstLineIndentText = "";
					
					leftIndentText = "<@begin-para-left-indent-open>" + (leftIndent) + "<@begin-para-left-indent-close>";
					firstLineIndentText = "<@begin-para-first-line-indent-open>" + (firstLineIndent) + "<@begin-para-first-line-indent-close>";
					
					if (paraStyle.contains(":"))
						paraStyle = paraStyle.replace(":", "™");
					
					
					String paraText = paragraph.getText();
					
					paraText = miscUtility.removeExtraSpaces(paraText);
					paraText = miscUtility.removeStartEndSpaces(paraText);
					paraText = miscUtility.removeReturnUnit(paraText);
					paraText = miscUtility.replaceUnknownCharBox(paraText);
					
					String listLabelObj = "";
					
					if (paraText.contains("consistently proved to be a stronger predictor of outcomes than creatinine"))
						logger.debug("Determining whether neurological abnormalities in the ICU patient ");
					
				    if (paragraph.getListFormat().isListItem()) {
				    	// This is the text we get when actually getting when we output this node to text format
				    	// The list labels are not included in this text output. Trim any paragraph formatting characters
//				    	paraText = paragraph.toString(SaveFormat.TEXT).trim();
				    	ListLabel label = paragraph.getListLabel();
				        // This gets the position of the paragraph in current level of the list. If we have a list with multiple level then this
				        // will tell us what position it is on that particular level
//				        System.out.println("Numerical Id: " + label.getLabelValue());
				        // Combine them together to include the list label with the text in the output
//				        System.out.println("List label combined with text: " + label.getLabelString() + " " + paragraph.getText());
				        listLabelObj = label.getLabelString();
				    }
					
					
					paraText = miscUtility.removeStartEndSpaces(paraText);
					String[] headResults = taChapOutline.setOutlineByHeadLevels(paraText, paraStyle);
					
					paraStyle = miscUtility.formatParaStyle(paraStyle);
					
					if ((paraStyle.toLowerCase().contains("fm title")) & (paraText.toLowerCase().contains("contents")))
						Constants.contentsLabel = true;
					
					if (paraIndexinTable > 0) {
						paraIndexinTable--;
						paraIndex++;
						continue;
					}
					
					if( (paraStyle.length() > 0) ) {
						
						if (paraText .length() > 0) {
							
							logger.debug(paraText);
							
							paraStyle = headResults[1];
							Boolean isFloatItem = false;
							String[] floatItemArray = {"", ""};

							if (Constants.isReportingFace) {
								
								/**
								 * un-num/display start float items
								 */
								
								if ((paraText.toLowerCase().contains("<insert ")) & (paraText.toLowerCase().contains("unn fig"))) {
									
									fileWriterI.write("\n"+paraText, Constants.outputPath + "/Resource/figure-unnum.txt", true);
								}
						        if ((paraText.toLowerCase().contains("<insert ")) & (paraText.toLowerCase().contains("unn table"))) {
									
									fileWriterI.write("\n"+paraText, Constants.outputPath + "/Resource/table-unnum.txt", true);
								}
						        if ((paraText.toLowerCase().contains("<insert ")) & (paraText.toLowerCase().contains("unn box"))) {
									
									fileWriterI.write("\n"+paraText, Constants.outputPath + "/Resource/box-unnum.txt", true);
								}
						        if ((paraText.toLowerCase().contains("<insert ")) & (paraText.toLowerCase().contains("unn video"))) {
									
									fileWriterI.write("\n"+paraText, Constants.outputPath + "/Resource/video-unnum.txt", true);
								}
								
						        /**
								 * un-num/display ends float items
								 */
						        
								if (paraText.toLowerCase().contains("<figure legends>"))
									logger.debug("<figure legends>");
								
								if (
										(paraText.toLowerCase().startsWith("<")) & 
										(isFloatFigureItem) & 
										((paraText.toLowerCase().startsWith("<src")) == false) & ((paraText.toLowerCase().startsWith("<##src")) == false) &
										((paraText.toLowerCase().startsWith("<alt")) == false) & ((paraText.toLowerCase().startsWith("<##alt")) == false) &
										((paraText.toLowerCase().startsWith("<cap")) == false) & ((paraText.toLowerCase().startsWith("<##cap")) == false)
										) {
									isFloatFigureItem = false;
									previousFloatFigLabel = "";
								}
								if (paraText.toLowerCase().startsWith("<figure legends>"))
									isFloatFigureItem = true;
								
								if (
										(paraText.toLowerCase().startsWith("<")) & 
										(isFloatVideoItem) & 
										((paraText.toLowerCase().startsWith("<src")) == false) & ((paraText.toLowerCase().startsWith("<##src")) == false) &
										((paraText.toLowerCase().startsWith("<alt")) == false) & ((paraText.toLowerCase().startsWith("<##alt")) == false) &
										((paraText.toLowerCase().startsWith("<cap")) == false) & ((paraText.toLowerCase().startsWith("<##cap")) == false)
										) {
									
									isFloatVideoItem = false;
									previousFloatVideoLabel = "";
								}
								if (paraText.toLowerCase().startsWith("<video"))
									isFloatVideoItem = true;
								
								if ((paraText.toLowerCase().startsWith("<## table>")) & (isFloatTableItem)) {
									isFloatTableItem = false;
									previousFloatTableLabel = "";
								}
								if ((paraText.toLowerCase().startsWith("<##table>")) & (isFloatTableItem)) {
									isFloatTableItem = false;
									previousFloatTableLabel = "";
								}
								if (paraText.toLowerCase().startsWith("<table>"))
									isFloatTableItem = true;
								
								if ((paraText.toLowerCase().startsWith("<## box>")) & (isFloatBoxItem)) {
									isFloatBoxItem = false;
									previousFloatBoxLabel = "";
								}
								if ((paraText.toLowerCase().startsWith("<##box>")) & (isFloatBoxItem)) {
									isFloatBoxItem = false;
									previousFloatBoxLabel = "";
								}
								if (paraText.toLowerCase().startsWith("<box>"))
									isFloatBoxItem = true;
								
								if (isFloatFigureItem)	{
									
									if (paraText.toLowerCase().startsWith("<alt")) isFloatFigureAltItem = true;
									if (paraText.toLowerCase().startsWith("<cap")) isFloatFigureCapItem = true;
									
									
									String paraFigText = fetchSourceTextFromDoc(paragraph);
									if ((isFloatFigureCapItem == false) & (isFloatFigureAltItem == false))
										floatItemArray = taFloatItemOrdering.floatFigureItemCheckLog(paraFigText, true);
									
									if (Boolean.parseBoolean(floatItemArray[0])) {
										
										previousFloatFigLabel = floatItemArray[1] + "";
									} else {
										
										// source part
										if (
												(previousFloatFigLabel.toLowerCase().startsWith("fig")) & 
												(previousFloatFigLabel.length() > 0) &
												(isFloatFigureAltItem == false) &
												(isFloatFigureCapItem == false)
												) {
											
											if (paraText.toLowerCase().startsWith("<src>")) {
												isFloatFigureSrcItem = true;
											}
											if (isFloatFigureSrcItem) {
												
												paraText = fetchSourceTextFromDoc(paragraph);
												if (paraText.toLowerCase().startsWith("<src>")) {
													paraText = paraText.substring(5);
													
												}
												if (paraText.toLowerCase().endsWith("<##src>")) {
													
													paraText = "";
													isFloatFigureSrcItem = false;
												}
												
												if (paraText.length() > 0)
												taFloatItemOrdering.floatFigureSourceItemCheckLog(paraText, true, previousFloatFigLabel, Constants.outputPath + "/Log-Report/float-item/figure-source.txt");

											} else {
												
												paraText = fetchSourceTextFromDoc(paragraph);
												taFloatItemOrdering.floatFigureSourceItemCheckLog(paraText, true, previousFloatFigLabel, Constants.outputPath + "/Log-Report/float-item/figure-main.txt");
											}
										}
									}
									
									if (paraText.toLowerCase().contains("<##cap")) isFloatFigureCapItem = false;
									if (paraText.toLowerCase().contains("<##alt")) isFloatFigureAltItem = false;
								}
								if (isFloatVideoItem)	{
									
									if (paraText.toLowerCase().startsWith("<alt")) isFloatVideoAltItem = true;
									if (paraText.toLowerCase().startsWith("<cap")) isFloatVideoCapItem = true;
									
									
									String paraFigText = fetchSourceTextFromDoc(paragraph);
									if ((isFloatVideoCapItem == false) & (isFloatVideoAltItem == false))
										floatItemArray = taFloatItemOrdering.floatVideoItemCheckLog(paraFigText, true);
									
									if (Boolean.parseBoolean(floatItemArray[0])) {
										
										previousFloatVideoLabel = floatItemArray[1] + "";
									} else {
										
										// source part
										if (
												(previousFloatVideoLabel.toLowerCase().startsWith("video")) & 
												(previousFloatVideoLabel.length() > 0) &
												(isFloatVideoAltItem == false) &
												(isFloatVideoCapItem == false)
												) {
											
											if (paraText.toLowerCase().startsWith("<src>")) {
												isFloatVideoSrcItem = true;
											}
											if (isFloatVideoSrcItem) {
												
												paraText = fetchSourceTextFromDoc(paragraph);
												if (paraText.toLowerCase().startsWith("<src>")) {
													paraText = paraText.substring(5);
													
												}
												if (paraText.toLowerCase().contains("<##src>")) {
													
													paraText = paraText.replace("<##src>", "");
													isFloatVideoSrcItem = false;
												}
												
												if (paraText.length() > 0)
												taFloatItemOrdering.floatFigureSourceItemCheckLog(paraText, true, previousFloatVideoLabel, Constants.outputPath + "/Log-Report/float-item/video-source.txt");

											} else {
												
												paraText = fetchSourceTextFromDoc(paragraph);
												taFloatItemOrdering.floatFigureSourceItemCheckLog(paraText, true, previousFloatVideoLabel, Constants.outputPath + "/Log-Report/float-item/video-main.txt");
											}
										}
									}
									
									if (paraText.toLowerCase().contains("<##cap")) isFloatVideoCapItem = false;
									if (paraText.toLowerCase().contains("<##alt")) isFloatVideoAltItem = false;
								}
								if (isFloatTableItem)	{
									
									if (paraText.toLowerCase().startsWith("<alt")) isFloatTableAltItem = true;
									if (paraText.toLowerCase().startsWith("<cap")) isFloatTableCapItem = true;
									
									
									String paraFigText = fetchSourceTextFromDoc(paragraph);
									if ((isFloatTableCapItem == false) & (isFloatTableAltItem == false))
										floatItemArray = taFloatItemOrdering.floatTableItemCheckLog(paraFigText, true);
									
									if (Boolean.parseBoolean(floatItemArray[0])) {
										
										previousFloatTableLabel = floatItemArray[1] + "";
									} else {
										
										// source part
										if (
												(previousFloatTableLabel.toLowerCase().startsWith("table")) & 
												(previousFloatTableLabel.length() > 0) &
												(isFloatTableAltItem == false) &
												(isFloatTableCapItem == false)
												) {
											
											if (paraText.toLowerCase().startsWith("<src>")) {
												isFloatTableSrcItem = true;
											}
											if (isFloatTableSrcItem) {
												
												paraText = fetchSourceTextFromDoc(paragraph);
												if (paraText.toLowerCase().startsWith("<src>")) {
													paraText = paraText.substring(5);
													
												}
												if (paraText.toLowerCase().contains("<##src>")) {
													
													paraText = paraText.replace("<##src>", "");
													isFloatTableSrcItem = false;
												}
												
												if (paraText.length() > 0)
												taFloatItemOrdering.floatFigureSourceItemCheckLog(paraText, true, previousFloatTableLabel, Constants.outputPath + "/Log-Report/float-item/table-source.txt");

											} else {
												
												paraText = fetchSourceTextFromDoc(paragraph);
												taFloatItemOrdering.floatFigureSourceItemCheckLog(paraText, true, previousFloatTableLabel, Constants.outputPath + "/Log-Report/float-item/table-main.txt");
											}
										}
									}
									
									if (paraText.toLowerCase().contains("<##cap")) isFloatTableCapItem = false;
									if (paraText.toLowerCase().contains("<##alt")) isFloatTableAltItem = false;
								}
								if (isFloatBoxItem)	{
									
									if (paraText.toLowerCase().startsWith("<alt")) isFloatBoxAltItem = true;
									if (paraText.toLowerCase().startsWith("<cap")) isFloatBoxCapItem = true;
									
									
									String paraFigText = fetchSourceTextFromDoc(paragraph);
									if ((isFloatBoxCapItem == false) & (isFloatBoxAltItem == false))
										floatItemArray = taFloatItemOrdering.floatBoxItemCheckLog(paraFigText, true);
									
									if (Boolean.parseBoolean(floatItemArray[0])) {
										
										previousFloatBoxLabel = floatItemArray[1] + "";
									} else {
										
										// source part
										if (
												(previousFloatBoxLabel.toLowerCase().startsWith("box")) & 
												(previousFloatBoxLabel.length() > 0) &
												(isFloatBoxAltItem == false) &
												(isFloatBoxCapItem == false)
												) {
											
											if (paraText.toLowerCase().startsWith("<src>")) {
												isFloatBoxSrcItem = true;
											}
											if (isFloatBoxSrcItem) {
												
												paraText = fetchSourceTextFromDoc(paragraph);
												if (paraText.toLowerCase().startsWith("<src>")) {
													paraText = paraText.substring(5);
													
												}
												if (paraText.toLowerCase().contains("<##src>")) {
													
													paraText = paraText.replace("<##src>", "");
													isFloatBoxSrcItem = false;
												}
												
												if (paraText.length() > 0)
												taFloatItemOrdering.floatFigureSourceItemCheckLog(paraText, true, previousFloatBoxLabel, Constants.outputPath + "/Log-Report/float-item/box-source.txt");
												
											}
										}
									}
									
									if (paraText.toLowerCase().contains("<##cap")) isFloatBoxCapItem = false;
									if (paraText.toLowerCase().contains("<##alt")) isFloatBoxAltItem = false;
								}
								
							} else if (Constants.isFilterTAFace) {
								
								isFloatItem = taFloatItemOrdering.floatItemCheckLog(paraText, true);
							}
							
							
							boolean isHeadPara = Boolean.parseBoolean(headResults[0]);
							paraText = fetchParagraphTextFromDoc(paraText, paraStyle, paragraph, isHyperlinksActive, isHeadPara, listLabelObj);
							
							List<String> splitList = miscUtility.createNewParaForSoftbreakentry(paraText);
							
							if (splitList.size() > 0) {
								int headindex = 0;
								for (String object : splitList) {
									
									object = miscUtility.replaceUnknownCharBox(object);
									
									boolean isValidParaHead = taChapOutline.isParaHeadInBegin(object);
									if (isValidParaHead){
										// <1> Chest Wall Anato
										if (object.startsWith("<")) {
											
											String headText = object.substring((object.indexOf("<")), (object.indexOf(">") + 1));
											String object1 = object.substring(object.indexOf(headText) + (headText.length()));
											object1 = ("<@alert-red-open>"+(headText)+"<@alert-red-close>") + object1;
											String[] headResultsSplit = taChapOutline.setFormatHeadLevels(object, headText);
											buffer.append(firstLineIndentText + leftIndentText + "@" + headResultsSplit[1] + ":" + object1 + "\n");
											
										} else if (object.startsWith("@")) {
											
											String headText = object.substring((object.indexOf("@") + 1), (object.indexOf(":") + 1));
											String object1 = object.substring(object.indexOf(headText) + (headText.length()));
											String[] headResultsSplit = taChapOutline.setFormatHeadLevels(object, headText.replace(":", ""));
											
											buffer.append(firstLineIndentText + leftIndentText + "@" + headResultsSplit[1] + ":" + object1 + "\n");
										} else {
											
											buffer.append(firstLineIndentText + leftIndentText + object + "\n");
										}
										isHeadPara = true;
										headindex = 0;
									} else {
										
										if (isHeadPara) {
											if (object.startsWith("@") == false) {
												if (headindex == 0) {
													
													buffer.append(firstLineIndentText + leftIndentText + "@Text flush:" + object + "\n");
												} else {
													
													buffer.append(firstLineIndentText + leftIndentText + "@Text:" + object + "\n");
												}
											} else {
												
												buffer.append(firstLineIndentText + leftIndentText + object + "\n");
											}
											headindex++;
											isHeadPara = false;
										} else {
											if (object.startsWith("@") == false) {
												
												if (headindex == 0) {
													
													buffer.append(firstLineIndentText + leftIndentText + "@Text flush:" + object + "\n");
												} else {
													
													buffer.append(firstLineIndentText + leftIndentText + "@Text:" + object + "\n");
												}
											} else {
												
												buffer.append(firstLineIndentText + leftIndentText + object + "\n");
											}
										}
									}
								}
							} else {
								
								paraText = miscUtility.replaceUnknownCharBox(paraText);
								buffer.append(firstLineIndentText + leftIndentText + paraText);
							}
						}
					}
					paraIndex++;
					
				}
			}
		}
		
		
		
		String bodyPart = buffer.toString();
		
		fileName = new File(outputFile).getName();
		String tempfile = outputFile.substring(0, outputFile.lastIndexOf("/") + 1);
		fileWriterI.write(bodyPart, tempfile + "TEMP.txt", false);
		String data = fileWriterI.readLineToCheckExtraLines(tempfile + "TEMP.txt", fileName);
		
		
		
		File fileToDel = new File (tempfile + "TEMP.txt");
		fileToDel.delete();
		fileWriterI.write(data, outputFile, false);
		
		
		/**
		 * float-log-report generation
		 * 
		 */
		
		taLogReport.floatItemsReport();
		
		/**
		 * generate alerts for float item missing
		 * 
		 */
		
		
		fileWriterI.write(data, outputFile, false);
		
		String alerts = alertsQuery.generateAlertsFloatItems(outputFile);
		if (alerts.length() > 0) {
			
			String alertFileName = new File(outputFile).getName();
			alertFileName = alertFileName.replace(".txt", "_alerts.txt");
			fileWriterI.write(alerts, Constants.outputPath + "/Resource/" + alertFileName, false);
		}
		String commentText = commentsBuffer.toString();
		if (commentText.length() > 0) {
			
			String commentsFileName = new File(outputFile).getName();
			commentsFileName = commentsFileName.replace(".txt", "_comments.txt");
			fileWriterI.write(commentText, Constants.outputPath + "/Resource/" + commentsFileName, false);
		}
	}
	
	
	
	public String coverTAFiltering(String docFile, String outputFileDocx, boolean isRefs) throws Exception {
		
//		Pattern regex = null;
//		Double leftIndent = new Double(0);
//		Double firstLineIndent = new Double(0);
		
		Document doc = new Document(docFile);
		
		doc.updateListLabels();
		doc = documentLayoutSettings(doc);
		
		if (Constants.taSettingForUser.isDeleteExtraLines())
			doc = removeAllEmptyLInesFromDoc(doc);
		
		
		FindReplaceOptions options = new FindReplaceOptions(FindReplaceDirection.FORWARD);
		
 		doc.getRange().replace(" ", " ", options);
		doc.getRange().replace(" .", ". ", options);
		doc.getRange().replace(" ,", ", ", options);
		doc.getRange().replace(" /", "/", options);
		doc.getRange().replace("/ ", "/", options);
		doc.getRange().replace("( ", "(", options);
		doc.getRange().replace(" )", ")", options);
		doc.getRange().replace("..", ".", options);
		doc.getRange().replace(",,", ",", options);
		doc.getRange().replace("::", ":", options);
		doc.getRange().replace(" ...", "…", options);
		doc.getRange().replace("...", "…", options);
		
		
		doc = documentBasicFilters(doc, options);
		
//		NodeCollection nodes = doc.getChildNodes(NodeType.ANY, true);
//		Integer paraIndex = 0;
//		Paragraph para = null;
		
//		for (Node node : (Iterable<Node>) nodes) {
//			
//			if ((node.getNodeType() == NodeType.PARAGRAPH)) {
//				
//				para = (Paragraph) doc.getChild(NodeType.PARAGRAPH, paraIndex, true);
//				para.getParagraphFormat().setLineSpacing(24);
//				
////				String paraStyle = para.getParagraphFormat().getStyle().getName();
//				
//				if (para != null)
//				if ((para.getText() != null)) {
//					
//					para.getParagraphFormat().setAlignment(ParagraphAlignment.LEFT);
//					
//					String paraText = para.getText();
//					paraText = paraText.replaceAll("\r", "");
//					
//					if (paraText.contains("pulmonary perfusion are matched")) {
//						logger.debug(paraText);
//					}
//					
//					String listLabelObj = "";
//					
//					leftIndent = para.getParagraphFormat().getLeftIndent();
//					firstLineIndent = para.getParagraphFormat().getFirstLineIndent();
//					
//					if (para.getListFormat().isListItem()) {
//						
//				    	ListLabel label = para.getListLabel();
//				        listLabelObj = label.getLabelString();
//				        
//				    	if (listLabelObj.equalsIgnoreCase(""))	 {
//				    		listLabelObj = "•";
//				    	}
//				        para.getListFormat().removeNumbers();
//				    }
//					if (listLabelObj.length() > 0) {
//						
//						String previousParaText = paraText;
//						paraText = listLabelObj + "\t" +paraText;
//						para.getRange().replace(previousParaText, paraText, options);
//						
//						para.getParagraphFormat().setFirstLineIndent(firstLineIndent);
//						para.getParagraphFormat().setLeftIndent(leftIndent);
//					}
//				}
//				
//				paraIndex++;
//			}
//		}
		
		
		
//		options = new FindReplaceOptions(FindReplaceDirection.FORWARD);
//		doc = documentBasicFilters(doc, options);
//		doc = documentLayoutSettings(doc);
		
		doc.getText().trim();
		doc.save(outputFileDocx);
		
		return outputFileDocx;
	}

	public static void main(Node node, Document document) {
		try {
//			Document document = new Document("/Users/administrator/Documents/PROJECTS/TOOL_SYSTEM/IQA-TA-PROCESS/rajesh.gupta@gwtech.in/1627935531869/INPUT/CH0028_Silverstein.clean.docx");
			DocumentBuilder builder = new DocumentBuilder(document);
			builder.moveTo(node);
			Field page = builder.insertField("PAGE", "");
			page.update();
			System.out.println(page.getResult());
		}catch(Exception exception) {	exception.printStackTrace();	}
	}

	
	private Document removeAllEmptyLInesFromDoc(Document doc) throws Exception {

		ArrayList nodes = new ArrayList();
		for (Paragraph  paragraph : (Iterable<Paragraph>) doc.getChildNodes(NodeType.PARAGRAPH, true))
		{
		    if(paragraph.toString(SaveFormat.TEXT).trim().length() == 0 && paragraph.getChildNodes(NodeType.SHAPE, true).getCount() == 0)
		    {
		        paragraph.remove();
		    }
		    else if(paragraph.toString(SaveFormat.TEXT).trim().length() == 0 && paragraph.getChildNodes(NodeType.SHAPE, true).getCount() > 1 )
		    {
		        nodes.add((paragraph));
		    }
		}

		for (Paragraph  paragraph : (Iterable<Paragraph>) nodes)
		{
		    Paragraph nextPara = (Paragraph)paragraph.getNextSibling();
		    if(nextPara.toString(SaveFormat.TEXT).trim().startsWith("Figure"))
		    {
		        // Move all content from the nextPara paragraph into the first.
		        while (nextPara.hasChildNodes())
		            paragraph.appendChild(nextPara.getFirstChild());

		        nextPara.remove();
		    }
		}
		
		return doc;
	}

	
	private Document documentBasicFilters(Document doc, FindReplaceOptions options) throws Exception {
		
		Pattern regex = null;
		
		
		if (Constants.taSettingForUser.isSymbolMissing())
			doc = findAndHighlightText(doc);
		
		
		regex = Pattern.compile("&l", Pattern.CASE_INSENSITIVE);
		doc.getRange().replace(regex, "&p", options);
		
		regex = Pattern.compile("&l&l", Pattern.CASE_INSENSITIVE);
		doc.getRange().replace(regex, "&p", options);
		
		regex = Pattern.compile("&p&p", Pattern.CASE_INSENSITIVE);
		doc.getRange().replace(regex, "&p", options);
		
		doc.getRange().replace(ControlChar.PAGE_BREAK, "&p", options);
		
		regex = Pattern.compile(" &l", Pattern.CASE_INSENSITIVE);
		doc.getRange().replace(regex, "&p", options);
		
		regex = Pattern.compile(" &p", Pattern.CASE_INSENSITIVE);
		doc.getRange().replace(regex, "&p", options);
		
		regex = Pattern.compile("&l ", Pattern.CASE_INSENSITIVE);
		doc.getRange().replace(regex, "&p", options);
		
		regex = Pattern.compile("&p ", Pattern.CASE_INSENSITIVE);
		doc.getRange().replace(regex, "&p", options);
		
		for (int index = 0; index < 50; index++) {
			
			doc.getRange().replace(ControlChar.NON_BREAKING_SPACE, " ", options);
			doc.getRange().replace(ControlChar.LINE_BREAK, "&p");
			doc.getRange().replace(ControlChar.PARAGRAPH_BREAK, "&p");
		
			regex = Pattern.compile("&l&l", Pattern.CASE_INSENSITIVE);
			doc.getRange().replace(regex, "&p", options);
			
			regex = Pattern.compile("&p&p", Pattern.CASE_INSENSITIVE);
			doc.getRange().replace(regex, "&p", options);
		}
		
		doc.getText().trim();
		
		return doc;
	}
	

	private Document findAndHighlightText(Document doc) {
		try {
			FindReplaceOptions options = new FindReplaceOptions();
	        options.setReplacingCallback(replaceEvaluatorFindAndHighlight);
	        options.setDirection(FindReplaceDirection.BACKWARD);
	
	        // We want the "your document" phrase to be highlighted.
	        doc.getRange().replace(Pattern.compile("", Pattern.CASE_INSENSITIVE), "", options);
	        doc.getRange().replace(Pattern.compile("", Pattern.CASE_INSENSITIVE), "", options);
	        doc.getRange().replace(Pattern.compile("", Pattern.CASE_INSENSITIVE), "", options);
	        doc.getRange().replace(Pattern.compile("", Pattern.CASE_INSENSITIVE), "", options);
	        doc.getRange().replace(Pattern.compile("", Pattern.CASE_INSENSITIVE), "", options);
	        
		} catch (Exception exception) {	exception.printStackTrace();	}
        return doc;
	}

	private Document documentLayoutSettings(Document doc) throws Exception {
		
		doc.getViewOptions().setViewType(ViewType.PAGE_LAYOUT);
	    doc.getViewOptions().setZoomPercent(100);
		doc.updateListLabels();
		DocumentBuilder builder = new DocumentBuilder(doc);
		
		if (Constants.taSettingForUser.isMarginsLeftPortraitOrientation()) {
			
			builder.getPageSetup().setOrientation(Orientation.PORTRAIT);
			builder.getPageSetup().setTopMargin(ConvertUtil.inchToPoint(1.0));
			builder.getPageSetup().setBottomMargin(ConvertUtil.inchToPoint(1.0));
			builder.getPageSetup().setLeftMargin(ConvertUtil.inchToPoint(1.0));
			builder.getPageSetup().setRightMargin(ConvertUtil.inchToPoint(1.0));
			
			builder.getPageSetup().setPaperSize(PaperSize.CUSTOM);
			
			builder.getFont().setName("Times New Roman");
			builder.getFont().setSize(12);
		}
		
		if (Constants.taSettingForUser.isFontColorDoubleSpace()) {
			builder.getCurrentParagraph().getParagraphFormat().setLineSpacing(24);
			builder.getCurrentParagraph().getParagraphFormat().setAddSpaceBetweenFarEastAndAlpha(true);
			builder.getCurrentParagraph().getParagraphFormat().setAddSpaceBetweenFarEastAndDigit(true);
			builder.getCurrentParagraph().getParagraphFormat().setKeepTogether(false);
		}
		
		return doc;
	}

	/**
	private String fetchTableStyle(Document doc, String cellText) throws Exception {
		
		NodeCollection paragraphs = doc.getChildNodes(NodeType.PARAGRAPH, true);
		String rowStyle = "";
		
		cellText = replaceExtraSpaces(cellText);
		
		if (cellText.length() > 0) {
			for (Paragraph paragraph : (Iterable<Paragraph>) paragraphs) {
		    	
		    	String paraStyle = paragraph.getParagraphFormat().getStyle().getName();
		    	String paraText = paragraph.getText().replace("​", " ").replace(" ", " ").replace(" ", " ");
		    	
		    	if ((paraStyle.toLowerCase().startsWith("t ")) && (paraStyle.toLowerCase().contains("t title") == false) && (paraStyle.toLowerCase().contains("t ftnote") == false)) {
		    		
		    		if (paraText.contains(cellText)) {
		    			
		    			rowStyle = paraStyle;
//		    			if (rowStyle.length() > 0)
//		    				break;
		    		}
		    	}
			}
		}
		return rowStyle;
	}
	*/
	
	
	
	/**
	private String fetchTableRun(Document doc, String cellText) throws Exception {
		
		NodeCollection paragraphs = doc.getChildNodes(NodeType.PARAGRAPH, true);
		String rowStyle = "";
		
		cellText = replaceExtraSpaces(cellText);
		
		if (cellText.length() > 0) {
			for (Paragraph paragraph : (Iterable<Paragraph>) paragraphs) {
		    	
		    	String paraStyle = paragraph.getParagraphFormat().getStyle().getName();
		    	String paraText = paragraph.getText();
		    	
		    	if ((paraStyle.toLowerCase().contains("t ")) && (paraStyle.toLowerCase().contains("t title") == false) && (paraStyle.toLowerCase().contains("t ftnote") == false) ) {
		    		if (paraText.contains(cellText)) {
		    			rowStyle = paraStyle;
		    			break;
		    		}
		    	}
			}
		}
		return rowStyle;
	}
	 * @param splitStringText 
	*/
	
	/**
	private String fetchOperator(String splitStringText) {
		
		String operator = "";
		if (splitStringText.contains("to"))	operator = splitStringText.substring(splitStringText.indexOf("to"));
		if (splitStringText.contains("through"))	operator = splitStringText.substring(splitStringText.indexOf("through"));
		if (splitStringText.contains("-"))	operator = splitStringText.substring(splitStringText.indexOf("-"));
		if (splitStringText.contains("–"))	operator = splitStringText.substring(splitStringText.indexOf("–"));
		if (splitStringText.contains(","))	operator = splitStringText.substring(splitStringText.indexOf(","));
		return operator;
	}
	*/
	/**
	 * private String fetchCommentsStyle(Document doc, String cellText) throws Exception {
	
		
		NodeCollection paragraphs = doc.getChildNodes(NodeType.PARAGRAPH, true);
		String rowStyle = "";
		
		cellText = replaceExtraSpaces(cellText);
		
		if (cellText.length() > 0) {
			for (Paragraph paragraph : (Iterable<Paragraph>) paragraphs) {
		    	
		    	String paraStyle = paragraph.getParagraphFormat().getStyle().getName();
		    	String paraText = paragraph.getText();
		    	
//		    	if ((paraStyle.toLowerCase().startsWith("t 1 hd")) || (paraStyle.toLowerCase().startsWith("t body")) || (paraStyle.toLowerCase().equalsIgnoreCase("t col hd"))) {
		    		if (paraText.contains(cellText)) {
		    			rowStyle = paraStyle;
		    			break;
		    		}
//		    	}
			}
		}
		return rowStyle;
	}
	*/
	
	
	private String replaceExtraSpaces(String line) throws Exception {
		while(line.startsWith(" ")) line = line.substring(1);
		while(line.startsWith(" ")) line = line.substring(1);
		while(line.endsWith(" ")) line = line.substring(0, (line.length() - 1));
		while(line.contains("")) line = line.replace("", "");
		return line;
	}

	
	
	
	
	//Topic	1	Osteology	of	Head	and	Neck	3
	

	private Boolean isNumberChar(String splitText) {
		Boolean valid = true;
		try {
			Integer.parseInt(splitText);
		}catch(Exception integerException) {	valid = false;	}
		
		try {
			Float.parseFloat(splitText);
		}catch(Exception floatException) {	valid = false;	}
		
//		logger.warn(exception.getMessage());	
		
		return valid;
	}
 	
	
	private String fetchParagraphNormalTextFromDoc(Paragraph paragraph, String listLabelObj) throws Exception {
		
		StringBuffer buffer = new StringBuffer();
		
		NodeCollection runs = paragraph.getChildNodes(NodeType.ANY, true);
		Constants.styleTagStack = new ArrayList<String>();
		
		for (Node runNode : (Iterable<Node>) runs) {
			
			String charText = "";
			
			if (runNode != null) {
				
				if (runNode.getNodeType() == NodeType.SPECIAL_CHAR) {
					charText = runNode.getText();
					buffer.append(charText);
				}
				else if (runNode.getNodeType() == NodeType.RUN){
					
					Run run = (Run)runNode;
					{
						
						if (run != null) {
							
							charText = run.getText();
							if (charText.contains("HYPERLINK"))	continue;
							
							charText = replaceExtraText(charText);
							charText = removePreSuffixExtraSpaces(charText);
							
							if (listLabelObj.length() > 0)
								charText = listLabelObj + " " + charText;
							buffer.append(charText);
						}
					}
				}
			}
		}
		buffer.append("\n");
		String data = buffer.toString();
		return data;
	}
	
	
	private String fetchParagraphTextFromDoc(String paraText, String paraStyle, Paragraph paragraph, Boolean isHyperlinksActive, 
			boolean isHeadResult, String listLabelObj) throws Exception {
		
//		paragraph.toString(SaveFormat.TEXT).trim();
		
		StringBuffer buffer = new StringBuffer();
		paraText = removePreSuffixExtraSpaces(paraText);
		
		buffer.append("@" + paraStyle + ":");
		
		if (
				(paraStyle.toLowerCase().contains("refs")) 
				|| (paraStyle.toLowerCase().contains("bibliography"))
				|| (paraStyle.toLowerCase().contains("references"))
				) {
			if ((paraText.length() > 0) & (isAlphaInLine(paraText))) {
				
				buffer.append("@REFS-BEGINS@");
			}
		}
		
		
		if ((paraText.startsWith("<") == false)) {
			
			if (isHeadResult) {
				
//				if (Constants.displayParaStyle.equalsIgnoreCase(paraStyle) == false) {
					
					if (paraStyle.equalsIgnoreCase("Normal") == false)
						buffer.append("<@alert-red-open><"+(paraStyle.toUpperCase())+"><@alert-red-close>");
					else
						buffer.append("<@alert-red-open><TXT><@alert-red-close>");
					
//					Constants.displayParaStyle = paraStyle;
//				}
			}
		}
		
		
		
		NodeCollection runs = paragraph.getChildNodes(NodeType.ANY, true);
		Constants.styleTagStack = new ArrayList<String>();
		int indexCharLoop = 0;
		String hyperlink = "";
		
		
		for (Node runNode : (Iterable<Node>) runs) {
			
			String charText = "";
			
			if (runNode != null) {
				
				if (runNode.getNodeType() == NodeType.SPECIAL_CHAR) {
					charText = runNode.getText();
					
					try {
//						charText = HttpRequestImpl.checkSymbolWithUnicode(array, charText);
					} catch (Exception exception) {
						logger.error(exception.getMessage(), exception);
					}
					buffer.append(charText);
				}
				else if (runNode.getNodeType() == NodeType.RUN){
					
					Run run = (Run)runNode;
					{
						
						if (run != null) {
							
							String charStyle = run.getFont().getStyle().getName();
							charText = run.getText();
//							charText = run.toString(SaveFormat.TEXT).trim();
							
							if (indexCharLoop == 0) {
								
								boolean isValidParaHead = taChapOutline.isParaHeadInBegin(charText);
								if (isValidParaHead){
									// <1> Chest Wall Anato
									String headText = charText.substring((charText.indexOf("<")), (charText.indexOf(">") + 1));
									charText = charText.substring(charText.indexOf(headText) + (headText.length()));
									charText = ("<@alert-red-open>"+(headText)+"<@alert-red-close>") + charText;
								} else {
									if (listLabelObj.length() > 0)
									charText = listLabelObj + "\t" + charText;
								}
								indexCharLoop++;
							}
							
							if (Constants.taSettingForUser.isSymbolMissing()) {
								charText = charText.replace("", "<@alert-red-symbolbox-open>[x]<@alert-red-symbolbox-close>");
//								charText = charText.replace("", "<@alert-red-symbolbox-open>[x]<@alert-red-symbolbox-close>");
								charText = charText.replace("", "<@alert-red-symbolbox-open>[x]<@alert-red-symbolbox-close>");
								charText = charText.replace("", "<@alert-red-symbolbox-open>[x]<@alert-red-symbolbox-close>");
							}
							
							if (charText.contains("HYPERLINK")) {
								
								hyperlink = charText.replace(" HYPERLINK \"", "").replace("\" ", "");
								continue;
							}
							
							charText = replaceExtraText(charText);
							paraText = replaceExtraText(paraText);
							
							List<String> links = extractUrls(charText);
							
							if (links.size() > 0) {
								StringBuffer bufferTemp = new StringBuffer();
								String data = charText;
							 	
								for (String link: links) {
									
									String prefixData = data.substring(0, ((data.indexOf(link)) + (link.length())));
									data = data.substring(prefixData.length());
									prefixData = prefixData.replace(link, "<@link-text-open>" + link + "<@link-text-close>");
									bufferTemp.append(prefixData);
									
								}
								bufferTemp.append(data);
								
								charText = bufferTemp.toString();
							} else if (charStyle.equalsIgnoreCase("inter-ref")) {
								
								charText = "<@link-text-open>" + charText + "<@link-text-close>";
							}
							
							
							if ((charText.contains("HYPERLINK ")) & (isHyperlinksActive == false))
								continue;
							
							charText = removePreSuffixExtraSpaces(charText);
							
							try {
//								charText = HttpRequestImpl.checkSymbolWithUnicode(array, charText);
							} catch (Exception exception) {
								logger.error(exception.getMessage(), exception);
							}
							
							charText = fontProperties.fetchFontStyleScript(run, charText, charStyle, hyperlink);
							buffer.append(charText);
						}
					}
				}
			}
		}
		
		
		
		if (Constants.styleTagStack.size() > 0) {
			
			StringBuffer tagNameBuff = new StringBuffer();
			tagNameBuff.append("<@");
			
			for (int index = 0; index < Constants.styleTagStack.size(); index ++) {
				tagNameBuff.append(Constants.styleTagStack.get(index));
			}
			
			tagNameBuff.append("-close>");
			
			buffer.append(tagNameBuff.toString());
		}
		buffer.append("\n");
		
		String data = buffer.toString();
		return data;
	}
	
	
	private String fetchSourceTextFromDoc(Paragraph paragraph) throws Exception {
		
		StringBuffer buffer = new StringBuffer();
		NodeCollection runs = paragraph.getChildNodes(NodeType.ANY, true);
		Constants.styleTagStack = new ArrayList<String>();
		String hyperlink = "";
		
		for (Node runNode : (Iterable<Node>) runs) {
			
			String charText = "";
			
			if (runNode != null) {
				
				if (runNode.getNodeType() == NodeType.SPECIAL_CHAR) {
					charText = runNode.getText();
					
					try {
//						charText = HttpRequestImpl.checkSymbolWithUnicode(array, charText);
					} catch (Exception exception) {
						logger.error(exception.getMessage(), exception);
					}
					buffer.append(charText);
				}
				else if (runNode.getNodeType() == NodeType.RUN){
					
					Run run = (Run)runNode;
					{
						
						if (run != null) {
							
							charText = run.getText();
							charText = replaceExtraText(charText);
							
							if ((charText.contains("HYPERLINK ")))
								continue;
							
							charText = removePreSuffixExtraSpaces(charText);
							charText = fontProperties.fetchFontStyleScript(run, charText, "", hyperlink);
							buffer.append(charText);
						}
					}
				}
			}
		}
		
		
		
		if (Constants.styleTagStack.size() > 0) {
			
			StringBuffer tagNameBuff = new StringBuffer();
			tagNameBuff.append("<@");
			
			for (int index = 0; index < Constants.styleTagStack.size(); index ++) {
				tagNameBuff.append(Constants.styleTagStack.get(index));
			}
			
			tagNameBuff.append("-close>");
			
			buffer.append(tagNameBuff.toString());
		}
		buffer.append("\n");
		
		String data = buffer.toString();
		
		data = miscUtility.removeExtraSpaces(data);
		data = miscUtility.removeStartEndSpaces(data);
		data = miscUtility.removeReturnUnit(data);
		data = miscUtility.replaceUnknownCharBox(data);
		
		return data;
	}

	
	
	/**
	 * Returns a list with all links contained in the input
	 */
	public static List<String> extractUrls(String text) {
		
	    List<String> containedUrls = new ArrayList<String>();
	    String urlRegex = "((https?|ftp|gopher|telnet|file):((//)|(\\\\))+[\\w\\d:#@%/;$()~_?\\+-=\\\\\\.&]*)";
	    Pattern pattern = Pattern.compile(urlRegex, Pattern.CASE_INSENSITIVE);
	    Matcher urlMatcher = pattern.matcher(text);

	    while (urlMatcher.find()) {
	    	String link = text.substring(urlMatcher.start(0), urlMatcher.end(0));
	    	link = link.replace("(", "").replace(")", "").replace("[", "").replace("]", "").replace(";", "");
	        containedUrls.add(link);
	    }
	    
	    if ((containedUrls.size() == 0) & (text.contains("www."))) {
	    	
	    	String[] splitText = text.split(" ");
	    	for(String split: splitText) {
	    		
	    		if (split.contains("www."))
	    			containedUrls.add(split.replace("(", "").replace(")", "").replace("[", "").replace("]", "").replace(";", ""));
	    	}
	    }
	    
	    return containedUrls;
	}
	
	
	
	/**
	private boolean isStyleInReferancesList(String charStyle) {
		
		Boolean valid = false;
		BufferedReader reader = null;
		
		try {
			
			reader = new BufferedReader(new InputStreamReader(new FileInputStream(new File("Log-Report/referances.txt")), "UTF-8"));
			String line = "";
			
			while(	(line = reader.readLine()) != null	){
				if(line.toLowerCase().contains(charStyle.toLowerCase())){	valid = true;	break;	}
			}
			
		}catch (Exception exception) {
			logger.error(exception.getMessage(), exception);
		} finally{
				if(reader != null)
				try {	reader.close();	} catch (IOException e) {	logger.error(e);	}
		}
		return valid;
	}
	*/
	public Boolean isOperators_match(String data) {
		
		Boolean isoperators_match = new Boolean(false);
		BufferedReader reader = null;
		
		try{
		
			reader = new BufferedReader(new InputStreamReader(new FileInputStream(new File("Log-Report/operators_symbols_cross_refs.txt")), "UTF-8"));
			String line = "";
			
			while(	(line = reader.readLine()) != null	){
				if(data.contains(line)){	isoperators_match = true;	break;	}
			}
			
		}catch (Exception exception) {
			logger.error(exception.getMessage(), exception);  //new MainClassWriter().LogWriterLogInfo(exception.getMessage());
		} finally{
				if(reader != null)
				try {	reader.close();	} catch (IOException e) {	logger.error(e);	}
		}
		return isoperators_match;
	}
	
	private String removePreSuffixExtraSpaces(String charText) {

		charText = charText.replaceAll("\\R$", "");

		return charText;
	}

	@Override
	public String fetchFileName(String input) throws Exception {

		if (input.lastIndexOf(".") > 0)
			input = input.substring(0, input.lastIndexOf("."));
		return input;
	}

	public void setFileWriterI(FileWriterI fileWriterI) {
		this.fileWriterI = fileWriterI;
	}

	public void setFontProperties(FontProperties fontProperties) {
		this.fontProperties = fontProperties;
	}

	public void setTaChapOutline(TAChapOutline taChapOutline) {
		this.taChapOutline = taChapOutline;
	}

	public void setMiscUtility(MiscUtility miscUtility) {
		this.miscUtility = miscUtility;
	}

	public void setTaFloatItemOrdering(TAFloatItemOrdering taFloatItemOrdering) {
		this.taFloatItemOrdering = taFloatItemOrdering;
	}

	public void setTaLogReport(TALogReport taLogReport) {
		this.taLogReport = taLogReport;
	}

	public void convertToDocx(String inDocFile, String outDocFile) {
		try {
			Document doc = new Document(inDocFile);
			doc.save(outDocFile, SaveFormat.DOCX);
		}catch(Exception exception) {	System.out.println(exception.getMessage());	}
	}
	public void setHeaderFooterOperation(HeaderFooterOperation headerFooterOperation) {
		this.headerFooterOperation = headerFooterOperation;
	}

	public void setReplaceEvaluatorFindAndHighlight(ReplaceEvaluatorFindAndHighlight replaceEvaluatorFindAndHighlight) {
		this.replaceEvaluatorFindAndHighlight = replaceEvaluatorFindAndHighlight;
	}

	public void setAlertsQuery(AlertsQuery alertsQuery) {
		this.alertsQuery = alertsQuery;
	}

	private String replaceExtraText(String charText) {
		charText = charText.replace(" ", " ").replace(" ", " ").replace("", "");
		charText = charText.replace(" ", " ").replace(" ", " ").replace("", "");
		return charText;
		}
}
