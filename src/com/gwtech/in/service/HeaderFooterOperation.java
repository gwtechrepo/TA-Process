package com.gwtech.in.service;

public interface HeaderFooterOperation {
	
	public void removeFooterFromDocFile(String inDoc, String outDoc) throws Exception;
	public void addHeaderInfToDocument(String inDoc, String outDoc) throws Exception;
}
