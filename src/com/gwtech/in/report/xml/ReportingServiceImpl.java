package com.gwtech.in.report.xml;

import java.io.File;

import org.apache.log4j.Logger;

import com.aspose.words.Document;
import com.aspose.words.DocumentBuilder;
import com.aspose.words.ListLabel;
import com.aspose.words.Node;
import com.aspose.words.NodeCollection;
import com.aspose.words.NodeType;
import com.aspose.words.Paragraph;
import com.aspose.words.PdfCompliance;
import com.gwtech.in.report.ReportingService;
import com.gwtech.in.service.FileWriterI;
import com.gwtech.in.service.WordToText;
import com.gwtech.in.service.impl.RemoveHiddenContentVisitor;
import com.gwtech.in.utils.Constants;
import com.gwtech.in.utils.MiscUtility;

@SuppressWarnings({ "rawtypes", "unchecked" })
public class ReportingServiceImpl implements ReportingService {
	
	private static final Logger logger = Logger.getLogger(ReportingServiceImpl.class);
	private MiscUtility miscUtility;
	private FileWriterI fileWriterI;
	
	
	@Override
	public void init(String fileName) {
		
		logger.info("*******************************************************************");
		logger.info(":inside init operation reporting service: " + fileName);
		logger.info("*******************************************************************");
	}
	
	
	
	@Override
	public String[] fetchChapterDescInfo(String docFile, WordToText wordToText) throws Exception {
		
		String[] result = new String[8];
		Document doc = new Document(docFile);
		Integer paraIndex = 0;
		NodeCollection nodes = doc.getChildNodes(NodeType.ANY, true);
//		String paraStyletag = "", previousParaStyletag = "";
		Boolean isReferencesActive = false, isBodyActive = false;
		StringBuffer refsBuffer = new StringBuffer();
		StringBuffer bodyBuffer = new StringBuffer();
		
		DocumentBuilder builder = new DocumentBuilder(doc);
		builder.moveToDocumentStart();
		
		for (Node node : (Iterable<Node>) nodes) {
			
			if ((node.getNodeType() == NodeType.PARAGRAPH)) {
				
				Paragraph paragraph = (Paragraph) doc.getChild(NodeType.PARAGRAPH, paraIndex, true);
				if (paragraph != null) {
					
					String paraText = paragraph.getText();
					
					if (paraText.toLowerCase().contains("<chap au>"))
						logger.debug(paraText);
					
					String listLabelObj = "";
					
					if (paragraph.getListFormat().isListItem()) {
				        ListLabel label = paragraph.getListLabel();
				        listLabelObj = label.getLabelString();
				    }
					
					paraText = miscUtility.fetchParagraphNormalTextFromDoc(paragraph, listLabelObj);
					paraText = miscUtility.removeExtraSpaces(paraText);
					paraText = miscUtility.removeStartEndSpaces(paraText);
					paraText = miscUtility.removeReturnUnit(paraText);
					paraText = miscUtility.replaceUnknownCharBox(paraText);
					paraText = miscUtility.removeExtraSpaces(paraText);
					
					
					if (paraText.contains("References"))
						logger.debug(paraText);
					
					
					if (paraText.toLowerCase().startsWith("<chap title>")) {
						
						//EXITING THE ABDOMEN AND CLOSURE TECHNIQUES
						
						paraText = wordToText.fetchChapterTitleSourceTextFromDoc(paragraph);
						
						
						
						paraText = miscUtility.removeExtraSpaces(paraText);
						paraText = miscUtility.removeStartEndSpaces(paraText);
						paraText = miscUtility.removeReturnUnit(paraText);
						paraText = miscUtility.replaceUnknownCharBox(paraText);
						paraText = miscUtility.removeExtraSpaces(paraText);
						
						if (paraText.indexOf("<CHAP TITLE>") > -1)
							paraText = paraText.substring((paraText.indexOf("<CHAP TITLE>") + "<CHAP TITLE>".length()));
						else if (paraText.indexOf("<chap title>") > -1)
							paraText = paraText.substring((paraText.indexOf("<chap title>") + "<chap title>".length()));
						
						if (paraText.toLowerCase().contains("<@su"))
							paraText = paraText.substring(0, (paraText.indexOf("<@su")));
						
						
						
						result[0] = paraText;
						isBodyActive = true;
						
					} else if (paraText.toLowerCase().startsWith("<chap au>")) {
						
						if (paraText.indexOf("<CHAP AU>") > -1)
							paraText = paraText.substring((paraText.indexOf("<CHAP AU>") + "<CHAP AU>".length()));
						else if (paraText.indexOf("<chap au>") > -1)
							paraText = paraText.substring((paraText.indexOf("<chap au>") + "<chap au>".length()));
						result[1] = paraText;
						
					} else if ((paraText.toLowerCase().startsWith("<chap ")) & ((paraText.toLowerCase().contains("outline")) == false)) {
						
						paraText = paraText.substring(0, (paraText.indexOf(">") + 1));
						String chapNoInfo = "";
						if (paraText.startsWith("<chap "))
							chapNoInfo = paraText.substring((paraText.indexOf("<chap ") + 6), (paraText.indexOf(">")));
						else if (paraText.startsWith("<CHAP "))
							chapNoInfo = paraText.substring((paraText.indexOf("<CHAP ") + 6), (paraText.indexOf(">")));
						
						result[2] = chapNoInfo;
						
						isBodyActive = true;
					}
					
					/**
					 * <REFERENCES>
					 * 
					 */
					
					if ((paraText.toLowerCase().startsWith("<")) & isReferencesActive) {
						
						isReferencesActive = false;
						String referenceCloseTag = "";
						referenceCloseTag = paraText.substring(0, (paraText.indexOf(">") + 1));
						if (referenceCloseTag.equalsIgnoreCase("<references>") == false)
							result[4] = referenceCloseTag;
					}
					
					if (isReferencesActive) {
						
						paraText = miscUtility.removeExtraSpaces(paraText);
						paraText = miscUtility.removeStartEndSpaces(paraText);
						paraText = miscUtility.removeReturnUnit(paraText);
						paraText = miscUtility.replaceUnknownCharBox(paraText);
						paraText = miscUtility.removeExtraSpaces(paraText);
						
						refsBuffer.append(paraText + "\n");
					} 
					
					
					if (
							(paraText.toLowerCase().startsWith("<references>")) ||
							(paraText.toLowerCase().startsWith("<suggested reading")) ||
							(paraText.toLowerCase().startsWith("<bibliography")) ||
							(paraText.toLowerCase().startsWith("<further reading")) ||
							(paraText.toLowerCase().startsWith("<selected reference"))
							) {
						
						isReferencesActive = true;
						String referenceStartTag = "";
						referenceStartTag = paraText.substring(0, (paraText.indexOf(">") + 1));
						result[3] = referenceStartTag;
					}
					
					if (isBodyActive &! isReferencesActive) {
						
						if (paraText.startsWith("<"))
							paraText = paraText.substring(paraText.indexOf(">") + 1);
						
						paraText = miscUtility.removeExtraSpaces(paraText);
						paraText = miscUtility.removeStartEndSpaces(paraText);
						paraText = miscUtility.removeReturnUnit(paraText);
						paraText = miscUtility.replaceUnknownCharBox(paraText);
						paraText = miscUtility.removeExtraSpaces(paraText);
						
						bodyBuffer.append(paraText + "\n");
					}

					paraIndex++;
				}
			}
		}
		
		fileWriterI.write(refsBuffer.toString(), Constants.outputPath + "/refs-text.txt", false);
		fileWriterI.write(bodyBuffer.toString(), Constants.outputPath + "/body-text.txt", false);
		
		result[5] = Constants.outputPath + "/refs-text.txt";
		result[6] = Constants.outputPath + "/body-text.txt";
		
		return result;
	}
	
	
	@Override
	public String[] wordToPdf(String docFile, String[] chapInfoArray) throws Exception {
		
//		#########
		
		Document document = new Document(docFile);
		
		com.aspose.words.PdfSaveOptions options = new com.aspose.words.PdfSaveOptions();
		options.setCompliance(PdfCompliance.PDF_15);
		
		String pdfFileName = "";
		File file = new File(docFile);
		pdfFileName = file.getName();
		pdfFileName = pdfFileName.substring(0, pdfFileName.lastIndexOf("."));
		
		document.save((Constants.outputPath) + "/" + (pdfFileName+".pdf"), options);
		chapInfoArray[7] = (Constants.outputPath) + "/" + (pdfFileName+".pdf");
		
//		#########
		
		return chapInfoArray;
	}
	
	
	
	@Override
	public void saveChapInfoArrayToFile(String data) {
		
		fileWriterI.write(data, Constants.outputPath + "/out-results.txt", false);
	}
	
	
	
	@Override
	public void exit(String fileName) {
		
		logger.info("*******************************************************************");
		logger.info(":inside exit operation reporting service: " + fileName);
		logger.info("*******************************************************************");
	}

	public void setMiscUtility(MiscUtility miscUtility) {
		this.miscUtility = miscUtility;
	}

	public void setFileWriterI(FileWriterI fileWriterI) {
		this.fileWriterI = fileWriterI;
	}

	

	@Override
	public void cleanFile(String docFile) throws Exception {
		
		Document doc = new Document(docFile);
		doc.getRange().getFormFields().clear(); // Unlink a field
		
		RemoveHiddenContentVisitor hiddenContentRemover = new RemoveHiddenContentVisitor();
	    doc.accept(hiddenContentRemover);
	    doc.acceptAllRevisions();
	    doc.removeMacros();
	    
	    doc.save(docFile);
	}

}
