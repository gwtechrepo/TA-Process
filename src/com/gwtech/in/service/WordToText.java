package com.gwtech.in.service;

public interface WordToText {

	public void init(String fileName) throws Exception;
	public void conversion(String docFile, String outputTxt, String outputDoc, Boolean isHyperlinksActive, Boolean isWordDownloadActive) throws Exception;
	public void convertToDocx(String inDocFile, String outDocFile);
	public String fetchFileName(String input) throws Exception;
	public void simpleWordText(String docFile, String bodyFile, String refsFile) throws Exception;
	public void exit(String fileName);
	public String coverTAFiltering(String docFile, String outputFileDocx, boolean isRefs) throws Exception;
	public String deleteAllComments(String docFile) throws Exception;
	public void createResources() throws Exception;
}
