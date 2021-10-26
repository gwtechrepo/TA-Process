package com.gwtech.in.service;

public interface TAFloatItemOrdering {

	public void crossRefsCallOutCheckLog(String line) throws Exception;
	public Boolean floatItemCheckLog(String line, boolean status) throws Exception;
	
	public String[] floatFigureItemCheckLog(String lineTxt, boolean writeStatus) throws Exception;
	public Boolean floatFigureSourceItemCheckLog(String lineTxt, boolean writeStatus, String figureLabel, String sourceFilePath) throws Exception;
	
	public String[] floatBoxItemCheckLog(String lineTxt, boolean writeStatus) throws Exception;
	public String[] floatTableItemCheckLog(String lineTxt, boolean writeStatus) throws Exception;
	public String[] floatVideoItemCheckLog(String lineTxt, boolean writeStatus) throws Exception;
	public String removeStyleFromFloat(String paraText);
}
