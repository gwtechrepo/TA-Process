package com.gwtech.in.service;

public interface TAChapOutline {
	
	public String[] setOutlineByHeadLevels(String paraText, String paraStyle);
	public boolean isParaHeadInBegin(String paraText);
	public String[] setFormatHeadLevels(String paraText, String paraStyle);
}
