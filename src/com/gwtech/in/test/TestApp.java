package com.gwtech.in.test;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.aspose.words.Document;
import com.aspose.words.Node;
import com.aspose.words.NodeCollection;
import com.aspose.words.NodeType;
import com.aspose.words.Paragraph;
import com.gwtech.in.service.impl.FileWriterImpl;

public class TestApp {
	
	@SuppressWarnings({ "rawtypes", "unchecked", "unused" })
	public static void main(String[] args) {
		
		Document doc = null;
		NodeCollection nodes = null;
		List<String> paralist = new ArrayList<String>();
		StringBuffer buffer = new StringBuffer();
		
		try {
			
			File fileDir = new File("/Users/administrator/Desktop/Lussier_HRM_4e_TB_updated_AACSB/");
			if (fileDir.isDirectory()) {
				File[] files = fileDir.listFiles();
				
				Arrays.sort(files);
				
				for (File fileDoc : files) {
					
					if ((fileDoc.getName().toLowerCase()).endsWith(".docx")) {
						
						System.out.println(fileDoc.getName());
//						if (fileDoc.getName().equalsIgnoreCase("LussierHRM4e_TB_Ch2.docx"))
//							System.out.println("LussierHRM4e_TB_Ch2.docx");
						
						doc = new Document(fileDoc.getAbsolutePath());
						nodes = doc.getChildNodes(NodeType.PARAGRAPH, true);
						
						Paragraph paragraph = null;
						Integer paraIndex = 0;
						
						for (Node node : (Iterable<Node>) nodes) {
						
							paragraph = (Paragraph) doc.getChild(NodeType.PARAGRAPH, paraIndex, true);
							if (paragraph != null) {
								
								String paraText = paragraph.getText();
								
//								if (paraText.contains("Job Analysis"))
//									System.out.println(paraText);
								
								paraText = paraText.replaceAll("\r", "");
								paraText = removeStartEndSpaces(paraText);
								
								if (paraText.toLowerCase().startsWith("shrm:")) {
									
									if (paralist.contains(paraText) == false) {
//										System.out.println(paraText);
										buffer.append(paraText + "\n");
										paralist.add(paraText);
									}
								}
							} else {
								System.out.println(paragraph);
							}
							
							paraIndex++;
						}
					}
				}
			}
			
			new FileWriterImpl().write(buffer.toString(), "/Users/administrator/Desktop/Lussier_HRM_4e_TB_updated_AACSB/report.txt", false);
			
		}catch(Exception exception) {	exception.printStackTrace();	}
	}
	
	
	
	private static String removeStartEndSpaces(String paraText) {
		while (paraText.endsWith(" "))
			paraText = paraText.substring(0, (paraText.length() - 1));
		while (paraText.startsWith(" "))
			paraText = paraText.substring(1);
		
		while (paraText.endsWith("	"))
			paraText = paraText.substring(0, (paraText.length() - 1));
		while (paraText.startsWith("	"))
			paraText = paraText.substring(1);
		return paraText;
	}
}
