package com.gwtech.in.service.impl;

import com.gwtech.in.service.TAChapOutline;
import com.gwtech.in.utils.Constants;
import com.gwtech.in.utils.MiscUtility;

public class TAChapOutlineImpl implements TAChapOutline {
	
	private MiscUtility miscUtility;
	
	public static void main(String[] args) {
		
		new TAChapOutlineImpl().setOutlineByHeadLevels("<H1>Introduction", "");
	}
	
	@Override
	public String[] setOutlineByHeadLevels(String paraText, String paraStyle) {
		
		String[] headResult = {"false", paraStyle};
		
		if (paraText.indexOf("[[[SOFT-BREAK-ENTRY]]]") > -1)
			paraText = paraText.substring(0, (paraText.indexOf("[[[SOFT-BREAK-ENTRY]]]")));
		paraText = paraText.replaceAll("\r", "");
		if (paraText.length() == 0)	return headResult;
		
		String headLevel = "";
		
		String[] results = null;
		try {
			results = isHeadLevelConversion(paraStyle);
		} catch (Exception exception) {	exception.printStackTrace();	}
		
		if (Boolean.parseBoolean(results[1])) {
			
			paraStyle = results[0];
			paraStyle = miscUtility.formatParaStyle(paraStyle);
		}
		
		if ((paraText.toLowerCase().startsWith("<h1>")) ||
			(paraText.toLowerCase().startsWith("<h2>")) ||
			(paraText.toLowerCase().startsWith("<h3>")) ||
			(paraText.toLowerCase().startsWith("<h4>")) ||
			(paraText.toLowerCase().startsWith("<h5>")) ||
			(paraText.toLowerCase().startsWith("<h6>")) ||
			(paraText.toLowerCase().startsWith("<h7>"))	
			) {
			
			/**
			 * paragraph head level found
			 * 
			 */
			
//			if (Constants.headLevelIndex > 1) {
				
				String headlabel = paraText.substring((paraText.indexOf("<")), (paraText.indexOf(">") + 1));
				headLevel = headlabel.replace("h", "").replace("H", "").replace("<", "").replace(">", "");
				paraText = paraText.replace(headlabel, "");
				
				paraText = miscUtility.removeStartEndSpaces(paraText);
//				Constants.chapOutlineBuff.append("@"+(headLevel)+" hd-outline:<@alert-red-open><"+(headLevel)+" hd><@alert-red-close>" + paraText + "\n");
				Constants.chapOutlineBuff.append("<"+(headLevel)+" hd>" + paraText + "\n");
				
				paraStyle = headLevel + " hd";
				
				headResult[0] = "true";
				headResult[1] = paraStyle;
//			}
//			Constants.headLevelIndex++;
			
		} else if ((paraText.toLowerCase().startsWith("<1>")) ||
				(paraText.toLowerCase().startsWith("<2>")) ||
				(paraText.toLowerCase().startsWith("<3>")) ||
				(paraText.toLowerCase().startsWith("<4>")) ||
				(paraText.toLowerCase().startsWith("<5>")) ||
				(paraText.toLowerCase().startsWith("<6>")) ||
				(paraText.toLowerCase().startsWith("<7>"))	
				) {
				
				/**
				 * paragraph head level found
				 * 
				 */
				
//				if (Constants.headLevelIndex > 1) {
					
					String headlabel = paraText.substring((paraText.indexOf("<")), (paraText.indexOf(">") + 1));
					headLevel = headlabel.replace("<", "").replace(">", "");
					paraText = paraText.replace(headlabel, "");
					
					paraText = miscUtility.removeStartEndSpaces(paraText);
//					Constants.chapOutlineBuff.append("@"+(headLevel)+" hd-outline:<@alert-red-open><"+(headLevel)+" hd><@alert-red-close>" + paraText + "\n");
					Constants.chapOutlineBuff.append("<"+(headLevel)+" hd>" + paraText + "\n");
					
					paraStyle = headLevel + " hd";
					
					headResult[0] = "true";
					headResult[1] = paraStyle;
//				}
//				Constants.headLevelIndex++;
				
		} else if (Boolean.parseBoolean(results[1])) {
			
//			if (Constants.headLevelIndex > 1) {
				
				headLevel = miscUtility.fetchNumberFromText(paraStyle);
				if (paraText.contains("<@SOFT-BREAK/>") == false) {
					
					paraText = miscUtility.removeStartEndSpaces(paraText);
//					Constants.chapOutlineBuff.append("@"+(headLevel)+" hd-outline:<@alert-red-open><"+(headLevel)+" hd><@alert-red-close>" + paraText + "\n");
					Constants.chapOutlineBuff.append("<"+(headLevel)+" hd>" + paraText + "\n");
					
					paraStyle = headLevel + " hd";
					
					headResult[0] = "true";
					headResult[1] = paraStyle;
				}
//			}
//			Constants.headLevelIndex++;
			
		}
		
		return headResult;
	}
	
	
	@Override
	public String[] setFormatHeadLevels(String paraText, String paraStyle) {
		
		String[] headResult = {"false", paraStyle};
		
		if (paraText.indexOf("[[[SOFT-BREAK-ENTRY]]]") > -1)
			paraText = paraText.substring(0, (paraText.indexOf("[[[SOFT-BREAK-ENTRY]]]")));
		paraText = paraText.replaceAll("\r", "");
		if (paraText.length() == 0)	return headResult;
		
		String headLevel = "";
		
		String[] results = null;
		try {
			results = isHeadLevelConversion(paraStyle);
		} catch (Exception exception) {	exception.printStackTrace();	}
		
		if (Boolean.parseBoolean(results[1])) {
			
			paraStyle = results[0];
			paraStyle = miscUtility.formatParaStyle(paraStyle);
		}
		
		if ((paraText.toLowerCase().startsWith("<h1>")) ||
			(paraText.toLowerCase().startsWith("<h2>")) ||
			(paraText.toLowerCase().startsWith("<h3>")) ||
			(paraText.toLowerCase().startsWith("<h4>")) ||
			(paraText.toLowerCase().startsWith("<h5>")) ||
			(paraText.toLowerCase().startsWith("<h6>")) ||
			(paraText.toLowerCase().startsWith("<h7>"))	
			) {
			
			/**
			 * paragraph head level found
			 * 
			 */
			
//			if (Constants.headLevelIndex > 1) {
				
				String headlabel = paraText.substring((paraText.indexOf("<")), (paraText.indexOf(">") + 1));
				headLevel = headlabel.replace("h", "").replace("H", "").replace("<", "").replace(">", "");
				paraText = paraText.replace(headlabel, "");
				
				paraText = miscUtility.removeStartEndSpaces(paraText);
//				Constants.chapOutlineBuff.append("@"+(headLevel)+" hd-outline:<@alert-red-open><"+(headLevel)+" hd><@alert-red-close>" + paraText + "\n");
				
				paraStyle = headLevel + " hd";
				
				headResult[0] = "true";
				headResult[1] = paraStyle;
//			}
//			Constants.headLevelIndex++;
			
		} else if ((paraText.toLowerCase().startsWith("<1>")) ||
				(paraText.toLowerCase().startsWith("<2>")) ||
				(paraText.toLowerCase().startsWith("<3>")) ||
				(paraText.toLowerCase().startsWith("<4>")) ||
				(paraText.toLowerCase().startsWith("<5>")) ||
				(paraText.toLowerCase().startsWith("<6>")) ||
				(paraText.toLowerCase().startsWith("<7>"))	
				) {
				
				/**
				 * paragraph head level found
				 * 
				 */
				
//				if (Constants.headLevelIndex > 1) {
					
					String headlabel = paraText.substring((paraText.indexOf("<")), (paraText.indexOf(">") + 1));
					headLevel = headlabel.replace("<", "").replace(">", "");
					paraText = paraText.replace(headlabel, "");
					
					paraText = miscUtility.removeStartEndSpaces(paraText);
//					Constants.chapOutlineBuff.append("@"+(headLevel)+" hd-outline:<@alert-red-open><"+(headLevel)+" hd><@alert-red-close>" + paraText + "\n");
					
					paraStyle = headLevel + " hd";
					
					headResult[0] = "true";
					headResult[1] = paraStyle;
//				}
//				Constants.headLevelIndex++;
				
		} else if (Boolean.parseBoolean(results[1])) {
			
//			if (Constants.headLevelIndex > 1) {
				
				headLevel = miscUtility.fetchNumberFromText(paraStyle);
				if (paraText.contains("<@SOFT-BREAK/>") == false) {
					
					paraText = miscUtility.removeStartEndSpaces(paraText);
//					Constants.chapOutlineBuff.append("@"+(headLevel)+" hd-outline:<@alert-red-open><"+(headLevel)+" hd><@alert-red-close>" + paraText + "\n");
					
					paraStyle = headLevel + " hd";
					
					headResult[0] = "true";
					headResult[1] = paraStyle;
				}
//			}
//			Constants.headLevelIndex++;
			
		}
		
		return headResult;
	}
	
	public String[] isHeadLevelConversion(String head) throws Exception {
		String[] result = { head, "false", "false" };

		if (head.toLowerCase().startsWith("b") || (head.toLowerCase().startsWith("t")) || (head.toLowerCase().contains("list"))) {
			return result;
		}

		if (head.contains("/"))
			head = head.substring(0, head.indexOf("/"));
		
		
		
		if (head.toLowerCase().contains("9 hd")) {
			result[0] = "Heading 9";
			result[1] = "true";
//			builder.getParagraphFormat().setStyleIdentifier(9);
		}
		if (head.toLowerCase().contains("8 hd")) {
			result[0] = "Heading 8";
			result[1] = "true";
//			builder.getParagraphFormat().setStyleIdentifier(8);
		}
		if (head.toLowerCase().contains("7 hd")) {
			result[0] = "Heading 7";
			result[1] = "true";
//			builder.getParagraphFormat().setStyleIdentifier(7);
		}
		if (head.toLowerCase().contains("6 hd")) {
			result[0] = "Heading 6";
			result[1] = "true";
//			builder.getParagraphFormat().setStyleIdentifier(6);
		}
		if (head.toLowerCase().contains("5 hd")) {
			result[0] = "Heading 5";
			result[1] = "true";
//			builder.getParagraphFormat().setStyleIdentifier(5);
		}
		if (head.toLowerCase().contains("4 hd")) {
			result[0] = "Heading 4";
			result[1] = "true";
//			builder.getParagraphFormat().setStyleIdentifier(4);
		}
		if (head.toLowerCase().contains("3 hd")) {
			result[0] = "Heading 3";
			result[1] = "true";
//			builder.getParagraphFormat().setStyleIdentifier(3);
		}
		if (head.toLowerCase().contains("2 hd")) {
			result[0] = "Heading 2";
			result[1] = "true";
//			builder.getParagraphFormat().setStyleIdentifier(2);
		}
		if (head.toLowerCase().contains("1 hd")) {
			result[0] = "Heading 1";
			result[1] = "true";
//			builder.getParagraphFormat().setStyleIdentifier(2);
		}
		
		if (head.toLowerCase().contains("9 a hd")) {
			result[0] = "Heading 9";
			result[1] = "true";
//			builder.getParagraphFormat().setStyleIdentifier(9);
		}
		if (head.toLowerCase().contains("8 a hd")) {
			result[0] = "Heading 8";
			result[1] = "true";
//			builder.getParagraphFormat().setStyleIdentifier(8);
		}
		if (head.toLowerCase().contains("7 a hd")) {
			result[0] = "Heading 7";
			result[1] = "true";
//			builder.getParagraphFormat().setStyleIdentifier(7);
		}
		if (head.toLowerCase().contains("6 a hd")) {
			result[0] = "Heading 6";
			result[1] = "true";
//			builder.getParagraphFormat().setStyleIdentifier(6);
		}
		if (head.toLowerCase().contains("5 a hd")) {
			result[0] = "Heading 5";
			result[1] = "true";
//			builder.getParagraphFormat().setStyleIdentifier(5);
		}
		if (head.toLowerCase().contains("4 a hd")) {
			result[0] = "Heading 4";
			result[1] = "true";
//			builder.getParagraphFormat().setStyleIdentifier(4);
		}
		if (head.toLowerCase().contains("3 a hd")) {
			result[0] = "Heading 3";
			result[1] = "true";
//			builder.getParagraphFormat().setStyleIdentifier(3);
		}
		if (head.toLowerCase().contains("2 a hd")) {
			result[0] = "Heading 2";
			result[1] = "true";
//			builder.getParagraphFormat().setStyleIdentifier(2);
		}
		if (head.toLowerCase().contains("1 a hd")) {
			result[0] = "Heading 1";
			result[1] = "true";
//			builder.getParagraphFormat().setStyleIdentifier(1);
		}
		
		
		/**
		 * 
		 */
		
		if (head.toLowerCase().contains("9a hd")) {
			result[0] = "Heading 9";
			result[1] = "true";
//			builder.getParagraphFormat().setStyleIdentifier(9);
		}
		if (head.toLowerCase().contains("8a hd")) {
			result[0] = "Heading 8";
			result[1] = "true";
//			builder.getParagraphFormat().setStyleIdentifier(8);
		}
		if (head.toLowerCase().contains("7a hd")) {
			result[0] = "Heading 7";
			result[1] = "true";
//			builder.getParagraphFormat().setStyleIdentifier(7);
		}
		if (head.toLowerCase().contains("6a hd")) {
			result[0] = "Heading 6";
			result[1] = "true";
//			builder.getParagraphFormat().setStyleIdentifier(6);
		}
		if (head.toLowerCase().contains("5a hd")) {
			result[0] = "Heading 5";
			result[1] = "true";
//			builder.getParagraphFormat().setStyleIdentifier(5);
		}
		if (head.toLowerCase().contains("4a hd")) {
			result[0] = "Heading 4";
			result[1] = "true";
//			builder.getParagraphFormat().setStyleIdentifier(4);
		}
		if (head.toLowerCase().contains("3a hd")) {
			result[0] = "Heading 3";
			result[1] = "true";
//			builder.getParagraphFormat().setStyleIdentifier(3);
		}
		if (head.toLowerCase().contains("2a hd")) {
			result[0] = "Heading 2";
			result[1] = "true";
//			builder.getParagraphFormat().setStyleIdentifier(2);
		}
		if (head.toLowerCase().contains("1a hd")) {
			result[0] = "Heading 1";
			result[1] = "true";
//			builder.getParagraphFormat().setStyleIdentifier(1);
		}
		
		
		/**
		 * 
		 */
		
		if (head.toLowerCase().contains("9 ahd")) {
			result[0] = "Heading 9";
			result[1] = "true";
//			builder.getParagraphFormat().setStyleIdentifier(9);
		}
		if (head.toLowerCase().contains("8 ahd")) {
			result[0] = "Heading 8";
			result[1] = "true";
//			builder.getParagraphFormat().setStyleIdentifier(8);
		}
		if (head.toLowerCase().contains("7 ahd")) {
			result[0] = "Heading 7";
			result[1] = "true";
//			builder.getParagraphFormat().setStyleIdentifier(7);
		}
		if (head.toLowerCase().contains("6 ahd")) {
			result[0] = "Heading 6";
			result[1] = "true";
//			builder.getParagraphFormat().setStyleIdentifier(6);
		}
		if (head.toLowerCase().contains("5 ahd")) {
			result[0] = "Heading 5";
			result[1] = "true";
//			builder.getParagraphFormat().setStyleIdentifier(5);
		}
		if (head.toLowerCase().contains("4 ahd")) {
			result[0] = "Heading 4";
			result[1] = "true";
//			builder.getParagraphFormat().setStyleIdentifier(4);
		}
		if (head.toLowerCase().contains("3 ahd")) {
			result[0] = "Heading 3";
			result[1] = "true";
//			builder.getParagraphFormat().setStyleIdentifier(3);
		}
		if (head.toLowerCase().contains("2 ahd")) {
			result[0] = "Heading 2";
			result[1] = "true";
//			builder.getParagraphFormat().setStyleIdentifier(2);
		}
		if (head.toLowerCase().contains("1 ahd")) {
			result[0] = "Heading 1";
			result[1] = "true";
//			builder.getParagraphFormat().setStyleIdentifier(1);
		}
		
		/**
		 * 
		 */
		
		if (head.toLowerCase().contains("9a hd")) {
			result[0] = "Heading 9";
			result[1] = "true";
//			builder.getParagraphFormat().setStyleIdentifier(9);
		}
		if (head.toLowerCase().contains("8a hd")) {
			result[0] = "Heading 8";
			result[1] = "true";
//			builder.getParagraphFormat().setStyleIdentifier(8);
		}
		if (head.toLowerCase().contains("7a hd")) {
			result[0] = "Heading 7";
			result[1] = "true";
//			builder.getParagraphFormat().setStyleIdentifier(7);
		}
		if (head.toLowerCase().contains("6a hd")) {
			result[0] = "Heading 6";
			result[1] = "true";
//			builder.getParagraphFormat().setStyleIdentifier(6);
		}
		if (head.toLowerCase().contains("5a hd")) {
			result[0] = "Heading 5";
			result[1] = "true";
//			builder.getParagraphFormat().setStyleIdentifier(5);
		}
		if (head.toLowerCase().contains("4a hd")) {
			result[0] = "Heading 4";
			result[1] = "true";
//			builder.getParagraphFormat().setStyleIdentifier(4);
		}
		if (head.toLowerCase().contains("3a hd")) {
			result[0] = "Heading 3";
			result[1] = "true";
//			builder.getParagraphFormat().setStyleIdentifier(3);
		}
		if (head.toLowerCase().contains("2a hd")) {
			result[0] = "Heading 2";
			result[1] = "true";
//			builder.getParagraphFormat().setStyleIdentifier(2);
		}
		if (head.toLowerCase().contains("1a hd")) {
			result[0] = "Heading 1";
			result[1] = "true";
//			builder.getParagraphFormat().setStyleIdentifier(1);
		}
		
		
		
		if (head.toLowerCase().contains("chap num")) {
			result[0] = "Heading 1";
			result[1] = "true";
//			builder.getParagraphFormat().setStyleIdentifier(1);
		}
		if (head.toLowerCase().contains("chap title")) {
			result[0] = "Heading 1";
			result[1] = "true";
//			builder.getParagraphFormat().setStyleIdentifier(1);
		}
		if (head.toLowerCase().contains("1 hd")) {
			result[0] = "Heading 1";
			result[1] = "true";
//			builder.getParagraphFormat().setStyleIdentifier(1);
		}
		
		
//		
		if (
				(head.toLowerCase().contains("run-in")) 
				|| (head.toLowerCase().contains("run in")) 
				|| (head.toLowerCase().contains("runin")) 
				|| (head.toLowerCase().startsWith("run"))
				|| (head.toLowerCase().endsWith("run"))
			) {
			result[2] = "true";
		}
		
		/**
		 * BUCK
		 * Sec1Title
		 */
		
		if (head.toLowerCase().contains("sec9title")) {
			result[0] = "Heading 9";
			result[1] = "true";
		}
		if (head.toLowerCase().contains("sec8title")) {
			result[0] = "Heading 8";
			result[1] = "true";
		}
		if (head.toLowerCase().contains("sec7title")) {
			result[0] = "Heading 7";
			result[1] = "true";
		}
		if (head.toLowerCase().contains("sec6title")) {
			result[0] = "Heading 6";
			result[1] = "true";
		}
		if (head.toLowerCase().contains("sec5title")) {
			result[0] = "Heading 5";
			result[1] = "true";
		}
		if (head.toLowerCase().contains("sec4title")) {
			result[0] = "Heading 4";
			result[1] = "true";
		}
		if (head.toLowerCase().contains("sec3title")) {
			result[0] = "Heading 3";
			result[1] = "true";
		}
		if (head.toLowerCase().contains("sec2title")) {
			result[0] = "Heading 2";
			result[1] = "true";
		}
		if (head.toLowerCase().contains("sec1title")) {
			result[0] = "Heading 1";
			result[1] = "true";
		}
		
		
		if (head.toLowerCase().contains("heading 9")) {
			result[0] = "hd 9";
			result[1] = "true";
		}
		if (head.toLowerCase().contains("heading 8")) {
			result[0] = "hd 8";
			result[1] = "true";
		}
		if (head.toLowerCase().contains("heading 7")) {
			result[0] = "hd 7";
			result[1] = "true";
		}
		if (head.toLowerCase().contains("heading 6")) {
			result[0] = "hd 6";
			result[1] = "true";
		}
		if (head.toLowerCase().contains("heading 5")) {
			result[0] = "hd 5";
			result[1] = "true";
		}
		if (head.toLowerCase().contains("heading 4")) {
			result[0] = "hd 4";
			result[1] = "true";
		}
		if (head.toLowerCase().contains("heading 3")) {
			result[0] = "hd 3";
			result[1] = "true";
		}
		if (head.toLowerCase().contains("heading 2")) {
			result[0] = "hd 2";
			result[1] = "true";
		}
		if (head.toLowerCase().contains("heading 1")) {
			result[0] = "hd 1";
			result[1] = "true";
		}
		
		return result;
	}
	
	
	public void setMiscUtility(MiscUtility miscUtility) {
		this.miscUtility = miscUtility;
	}

	@Override
	public boolean isParaHeadInBegin(String paraText) {
		boolean isValid = false;
		if ((paraText.toLowerCase().startsWith("<h1>")) ||
				(paraText.toLowerCase().startsWith("<h2>")) ||
				(paraText.toLowerCase().startsWith("<h3>")) ||
				(paraText.toLowerCase().startsWith("<h4>")) ||
				(paraText.toLowerCase().startsWith("<h5>")) ||
				(paraText.toLowerCase().startsWith("<h6>")) ||
				(paraText.toLowerCase().startsWith("<h7>"))	||
				(paraText.toLowerCase().startsWith("<1>")) ||
				(paraText.toLowerCase().startsWith("<2>")) ||
				(paraText.toLowerCase().startsWith("<3>")) ||
				(paraText.toLowerCase().startsWith("<4>")) ||
				(paraText.toLowerCase().startsWith("<5>")) ||
				(paraText.toLowerCase().startsWith("<6>")) ||
				(paraText.toLowerCase().startsWith("<7>")) ||
				(paraText.toLowerCase().startsWith("@1 hd:")) ||
				(paraText.toLowerCase().startsWith("@2 hd:")) ||
				(paraText.toLowerCase().startsWith("@3 hd:")) ||
				(paraText.toLowerCase().startsWith("@4 hd:")) ||
				(paraText.toLowerCase().startsWith("@5 hd:")) ||
				(paraText.toLowerCase().startsWith("@6 hd:")) ||
				(paraText.toLowerCase().startsWith("@7 hd:")) 
				
				) {
			
			isValid = true;
		}
		return isValid;
	}
	
}
