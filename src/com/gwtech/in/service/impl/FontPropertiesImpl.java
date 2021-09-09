package com.gwtech.in.service.impl;

import com.aspose.words.Run;
import com.gwtech.in.service.FontProperties;
import com.gwtech.in.utils.Constants;

public class FontPropertiesImpl implements FontProperties {
	
	
	@Override
	public String fetchFontStyleScript(Run run, String charText, String charStyleApply, String hyperlink) {
		
		if (charStyleApply.equalsIgnoreCase("Default Paragraph Font"))	charStyleApply = "";
		if (charStyleApply.contains(" "))	charStyleApply = charStyleApply.replace(" ", "-");
		StringBuffer bufferText = new StringBuffer();
		
		String closingTag = findClosingTag(run, charStyleApply, charText);
		if (closingTag.length() > 0)	bufferText.append("<@" + closingTag + "-close>"); 
		
		String currentTagName = fetchTagNameByProperty(run, charStyleApply);
		if (currentTagName.length() > 0) {
			
			if (Constants.styleTagStack.contains(currentTagName) == false) {
				Constants.styleTagStack.add(currentTagName);
//				if (hyperlink.length() > 0)
//					bufferText.append("<@"+currentTagName+"-[link]"+hyperlink+"[/link]-open>" + charText);
//				else
					bufferText.append("<@"+currentTagName+"-open>" + charText);
			} else {
				
				bufferText.append(charText);
			}
		} else {
			
			bufferText.append(charText);
		}
		return bufferText.toString();
	}
	
	
	private String findClosingTag(Run run, String charStyleApply, String charText) {
		
		String closingTag = "";
		
		if (Constants.styleTagStack.size() > 0) {
			
			String lastTagName = Constants.styleTagStack.get((Constants.styleTagStack.size() - 1));
			String currentTagName = fetchTagNameByProperty(run, charStyleApply);
			if (lastTagName.equalsIgnoreCase(currentTagName) == false) {
				
				closingTag = lastTagName;
				Constants.styleTagStack.remove(closingTag);
			}
		}
		return closingTag;
	}


	private String fetchTagNameByProperty(Run run, String charStyleApply) {
		
		StringBuffer buffer = new StringBuffer();
		String tagName = "";
		if (
//				(charStyleApply.length() > 0) || 
				(run.getFont().getUnderline() > 0) ||
				(run.getFont().getBold()) || (run.getFont().getItalic()) || 
//				(run.getFont().getAllCaps()) || (run.getFont().getSmallCaps()) || 
				(run.getFont().getSubscript()) || (run.getFont().getSuperscript()) ) {
			
			buffer.append("<@");
			
			if (run.getFont().getBold())			if (buffer.toString().length() < 3) {	buffer.append("bold");	} else	{	buffer.append("-bold");	}
			if (run.getFont().getBoldBi())			if (buffer.toString().length() < 3) {	buffer.append("bold-italic");	} else	{	buffer.append("-bold-italic");	}
			if (run.getFont().getItalic())			if (buffer.toString().length() < 3) {	buffer.append("italic");	} else	{	buffer.append("-italic");	}
//			if (run.getFont().getAllCaps())			if (buffer.toString().length() < 3) {	buffer.append("caps");	} else	{	buffer.append("-caps");	}
//			if (run.getFont().getSmallCaps())		if (buffer.toString().length() < 3) {	buffer.append("smallcaps");	} else	{	buffer.append("-smallcaps");	}
			if (run.getFont().getSubscript())		if (buffer.toString().length() < 3) {	buffer.append("subscript");	} else	{	buffer.append("-subscript");	}
			if (run.getFont().getSuperscript())		if (buffer.toString().length() < 3) {	buffer.append("superscript");	} else	{	buffer.append("-superscript");	}
			if (run.getFont().getUnderline() > 0)	if (buffer.toString().length() < 3) {	buffer.append("underline");	} else	{	buffer.append("-underline");	}
//			if (charStyleApply.length() > 0)		if (buffer.toString().length() < 3) {	buffer.append(charStyleApply);	} else	{	buffer.append("-"+charStyleApply);	}
			
			tagName = buffer.toString();
		}
		return tagName.replace("<@", "");
	}


	/**
	 * applyStyle
	 * @param charStyle
	 * @param buffer
	 * @return
	 */
	private String applyStyle(String charText, StringBuffer buffer) {
		
//		StringTokenizer tokenizer = new StringTokenizer(charText);
//		StringBuffer data = new StringBuffer();
//		
//		if ((charText.startsWith(" ") || charText.startsWith("\t")) && (tokenizer.countTokens() > 1))	data.append(" ");
//		
//		if (tokenizer.countTokens() > 1) {
//			
//			Integer counter = tokenizer.countTokens();
//			int index = 1;
//			while (tokenizer.hasMoreElements()) {
//				
//				String object = (String) tokenizer.nextElement();
//				data.append(buffer.toString()+">" + object + "<@$p>");
//				
//				if (index < (counter))	data.append(buffer.toString()+">" + " " + "<@$p>");
//				
//				index++;
//			}
//			
//			if ((charText.endsWith(" ") || charText.endsWith("\t")))	data.append(" ");
//			charText = data.toString();
//			
//		} else {
			String data = buffer.toString();
			if (data.length() > 0) {
				
				charText = data+"-open>" + charText;// + (data) + "-close>";
			}
//		}
		return charText;
	}

}