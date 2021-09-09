package com.gwtech.in;

import org.apache.log4j.Logger;
import org.json.simple.JSONObject;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.aspose.words.FontSettings;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gwtech.in.model.TASettingForUser;
import com.gwtech.in.report.ReportingService;
import com.gwtech.in.report.xml.ReportingServiceImpl;
import com.gwtech.in.service.AsposeImageFetch;
import com.gwtech.in.service.HeaderFooterOperation;
import com.gwtech.in.service.WordToText;
import com.gwtech.in.service.impl.AsposeImageFetchImpl;
import com.gwtech.in.service.impl.HeaderFooterOperationImpl;
import com.gwtech.in.service.impl.HttpRequestImpl;
import com.gwtech.in.service.impl.WordToTextImpl;
import com.gwtech.in.utils.Constants;
import com.gwtech.in.utils.MiscUtility;

public class MainAppTADocTxt {
	
	private static final Logger logger = Logger.getLogger(MainAppTADocTxt.class);
	private static String rootPath = "";
	private static String input = "";
	private static String fileName = "";
	private static String outputTxt = "";
	private static String outputDoc = "";
	private static ReportingService reportingService;
	private static HeaderFooterOperation headerFooterOperation;
	
	public static void main(String args[]) {
		try {
			
			ApplicationContext context = new ClassPathXmlApplicationContext("com/gwtech/in/service/xml/SpringBeans.xml");
			WordToText wordToText = (WordToTextImpl) context.getBean("wordtotext_");
			AsposeImageFetch asposeImageFetch = (AsposeImageFetchImpl) context.getBean("asposeImageFetch_");
			
			Constants.rootPath = args[0];
			
			JSONObject jsonObjResponse = new HttpRequestImpl().getTaSettings("/auth/fetch-ta-setting-auth?loginUser="+args[4]);
			ObjectMapper mapper = new ObjectMapper();
			Constants.taSettingForUser = mapper.readValue(jsonObjResponse.toString(), TASettingForUser.class);
			
			if (Boolean.parseBoolean(args[5]))	wordToText.init(fileName);
			
			
//			debug mode
//			args[2] = "CH0069_Fleisher_v3.docx";
			
			rootPath = args[0];
			Constants.rootPath = rootPath;
			Constants.outputPath = args[1];
			
			input = args[2];
			Constants.ISBN = args[3];
			Constants.projectAuthor = args[3];
			
			if (Constants.ISBN.contains("-")) {
				Constants.ISBN = Constants.ISBN.substring((Constants.ISBN.indexOf("-")) + 1);
				Constants.projectAuthor = (Constants.projectAuthor).substring(0, (Constants.projectAuthor.lastIndexOf("-")));
			}
			
			Constants.isFilterTAFace = Boolean.parseBoolean(args[5]);
			Constants.isReportingFace = Boolean.parseBoolean(args[6]);
			
			fileName = wordToText.fetchFileName(input);
			
			Constants.fileName = fileName;
			outputTxt = fileName + ".txt";
			outputDoc = fileName + "_final.docx";
			
			Constants.projectChapter = fetchChapterName(fileName);
			Constants.chapterPrefix = fetchChapterName(fileName);
			
			if (input.endsWith(".doc")) {
				wordToText.convertToDocx(rootPath + input, rootPath + fileName + ".docx");
				new MiscUtility().DeleteFileFolder(rootPath + input);
			}
			
			if (Constants.isFilterTAFace) {
				
				Constants.outputPath = Constants.outputPath + "/" + fileName + "/";
				wordToText.createResources();
				
				asposeImageFetch.iterateImg(rootPath + fileName + ".docx", rootPath + fileName + ".docx");
				FontSettings.getDefaultInstance().setFontsFolder("/Volumes/Shivalik_Volume/_TL_CS/_Shivalik\\ Fonts/All_START_UP_FONTS", true);
				wordToText.simpleWordText(rootPath + fileName + ".docx", Constants.outputPath + "/Resource/source-code.txt", Constants.outputPath + "/Resource/refs-code.txt");
				wordToText.conversion(rootPath + fileName + ".docx", Constants.outputPath + outputTxt, Constants.outputPath + outputDoc, false, true);
				
				wordToText.exit(fileName);
				
			} else if (Constants.isReportingFace) {
				
				context = new ClassPathXmlApplicationContext("com/gwtech/in/service/xml/reporting-application-context.xml");
				reportingService = (ReportingServiceImpl) context.getBean("reportingService");
				headerFooterOperation = (HeaderFooterOperationImpl) context.getBean("headerFooterOperation_");
				
				reportingService.init(fileName);
				
				String docFile = rootPath + fileName + ".docx";
				reportingService.cleanFile(docFile);
				docFile = wordToText.coverTAFiltering(docFile, docFile, false);
				docFile = wordToText.deleteAllComments(docFile);
				headerFooterOperation.removeFooterFromDocFile(docFile, docFile);
				
				String[] chapInfoArray = reportingService.fetchChapterDescInfo(docFile);
				chapInfoArray = reportingService.wordToPdf(docFile, chapInfoArray);
				
				StringBuffer buffer = new StringBuffer();
				buffer.append("[CHAPTER-TITLE]" + chapInfoArray[0] + "[/CHAPTER-TITLE]\n");
				buffer.append("[CHAPTER-AUTH]" + chapInfoArray[1] + "[/CHAPTER-AUTH]" + "\n");
				buffer.append("[CHAPTER-NO]" + chapInfoArray[2] + "[/CHAPTER-NO]" + "\n");
				buffer.append("[REFS-START-TAG]" + chapInfoArray[3] + "[/REFS-START-TAG]" + "\n");
				buffer.append("[REFS-CLOSE-TAG]" + chapInfoArray[4] + "[/REFS-CLOSE-TAG]" + "\n");
				buffer.append("[REFS-WORDS]" + chapInfoArray[5] + "[/REFS-WORDS]" + "\n");
				buffer.append("[BODY-WORDS]" + chapInfoArray[6] + "[/BODY-WORDS]" + "\n");
				buffer.append("[WORD-PDF]" + chapInfoArray[7] + "[/WORD-PDF]" + "\n");
				buffer.append("[ISBN]" + Constants.ISBN + "[/ISBN]" + "\n");
				
				String data = buffer.toString();
				logger.info("\n" + data);
				
				reportingService.saveChapInfoArrayToFile(data);
				
				wordToText.conversion(rootPath + fileName + ".docx", Constants.outputPath + outputTxt, Constants.outputPath + outputDoc, false, true);
				reportingService.exit(fileName);
			}
			
//			Constants.outputPath + outputTxt
			new MiscUtility().DeleteFileFolder(Constants.outputPath + outputTxt);
			
		} catch(Exception exception) {
			logger.error("[-:ERROR:-]" + exception.getMessage()+"[/-:ERROR:-]");
			logger.error(exception.getMessage(), exception);
		}
	}
	
	
	
	private static boolean isNum(String projectChapter) {
		
		Boolean isValid = true;
		
		try {
			Integer.parseInt(projectChapter);
		}catch(Exception exception) {	isValid = false;	}
		
		return isValid;
	}
	
	
	
	private static String fetchChapterName(String fileName) {
		
		String chapterName = "";
		if ((fileName.contains("-")) & (fileName.startsWith("-") == false))
			chapterName = fileName.substring(0, (fileName.indexOf("-")));
		else if ((fileName.contains("_")) & (fileName.startsWith("_") == false))
			chapterName = fileName.substring(0, (fileName.indexOf("_")));
		else if ((fileName.contains(" ")) & (fileName.startsWith(" ") == false))
			chapterName = fileName.substring(0, (fileName.indexOf(" ")));
		else
			chapterName = fileName;
		
		if ((chapterName.length() > 0) & ((chapterName.toLowerCase()).startsWith("ch")))
			chapterName = chapterName.replace("ch", "").replace("CH", "");
		
		if (isNum(chapterName)) {
			chapterName = (Integer.parseInt(chapterName)) + "";
		}
		
		return chapterName;
	}
}
