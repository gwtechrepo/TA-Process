package com.gwtech.in.utils;

import java.util.ArrayList;
import java.util.List;

import com.gwtech.in.model.TASettingForUser;

public class Constants {
	
	public static Boolean contentsLabel = false;
	public static String fileName = "";
	public static String rootPath = "";
	public static String outputPath = "";
	
	public static String ISBN = "";
	public static String chapterPrefix = "";
	public static StringBuffer chapOutlineBuff = new StringBuffer();
	public static StringBuffer floatFigureBuff = new StringBuffer();
	public static Integer headLevelIndex = 0;
	public static List<Integer> figureCallOut = new ArrayList<Integer>(0);
	public static List<Integer> boxCallOut = new ArrayList<Integer>(0);
	public static List<Integer> tableCallOut = new ArrayList<Integer>(0);
	public static List<Integer> videoCallOut = new ArrayList<Integer>(0);
	public static String[] delimeters = {"and", "to", "through", "-", "–", ",", "−"};
	public static Integer alertIndex = 0;
	public static String displayParaStyle = "";
	public static String projectAuthor = "";
	public static String projectChapter = "";
//	public static List<String> figRange = new ArrayList<String>(0);
	public static List<String> calloutFigureFixingNote = new ArrayList<String>(0);
	public static List<String> calloutTableFixingNote = new ArrayList<String>(0);
	public static List<String> calloutBoxFixingNote = new ArrayList<String>(0);
	public static List<String> calloutVideoFixingNote = new ArrayList<String>(0);
	
	public static List<String> styleTagStack = new ArrayList<>();
	public static TASettingForUser taSettingForUser;
	
	public static String[] spacesArray = {" ", " ", "	", "	", " ", " ", " ", " ", " ", " ", " ", " ", " ", " ", " ", " ", " ", " ", " ", " ", " ", " ", " "};
	
	public static Boolean isFilterTAFace = false;
	public static Boolean isReportingFace = false;
}
