package com.gwtech.in.report;

import com.gwtech.in.service.WordToText;

public interface ReportingService {

	public void init(String fileName);
	public String[] fetchChapterDescInfo(String docxPath, WordToText wordToText) throws Exception;
	public void cleanFile(String docxPath) throws Exception;
	public String[] wordToPdf(String docFile, String[] chapInfoArray) throws Exception;
	public void exit(String fileName);
	public void saveChapInfoArrayToFile(String data);
}
