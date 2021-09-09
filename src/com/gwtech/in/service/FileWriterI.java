package com.gwtech.in.service;

public interface FileWriterI {
	
	public void write(String content, String Path, Boolean append);
	public int readLineToCheckBr(String path, String tableCellText, Integer lineNum);
	public String readLineToCheckExtraLines(String path, String fileName);
	public String boxCrossMarking(String line);
	public String figureCrossMarking(String line);
	public String tableCrossMarking(String line);
	public String videoCrossMarking(String line);
}