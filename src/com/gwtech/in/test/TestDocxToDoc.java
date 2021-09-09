package com.gwtech.in.test;

import com.aspose.words.Document;

public class TestDocxToDoc {
	
	public static void main(String[] args) {
		Document document = null;
		try {
			document = new Document("/Users/administrator/Documents/PROJECTS/TOOL_SYSTEM/IQA-TA-PROCESS/rajesh.gupta@gwtech.in/1627935531869/INPUT/CH0028_Silverstein.clean.docx");
			System.out.println(document.getPageCount());
			
			for (int i = 0; i < document.getPageCount(); i++)
		    {
				
		    }
			
		}catch(Exception exception) {	exception.printStackTrace();	}
	}
}
