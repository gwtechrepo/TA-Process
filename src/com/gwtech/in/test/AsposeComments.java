package com.gwtech.in.test;

import com.aspose.words.Document;
import com.aspose.words.HeaderFooter;
import com.aspose.words.NodeCollection;
import com.aspose.words.NodeType;

public class AsposeComments {
	
	public static void main(String[] args) {
		try {
			Document doc = new Document("/Users/techadmin/Documents/TA_Process/OUTPUT/CH0083_Knipe_v6/CH0083_Knipe_v6_final.docx");
			// Collect all comments in the document
			NodeCollection comments = doc.getChildNodes(NodeType.HEADER_FOOTER, true);
			// Look through all comments and remove those written by the authorName author.
			for (int i = comments.getCount() - 1; i >= 0; i--)
			{
				HeaderFooter comment = (HeaderFooter) comments.get(i);
				System.out.println(comment.getText());
			}
		}catch(Exception exception) {	exception.printStackTrace();	}
	}
}
