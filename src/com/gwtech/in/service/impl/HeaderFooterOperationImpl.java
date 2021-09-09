package com.gwtech.in.service.impl;

import org.apache.log4j.Logger;

import com.aspose.words.Document;
import com.aspose.words.DocumentBuilder;
import com.aspose.words.FieldType;
import com.aspose.words.FootnoteType;
import com.aspose.words.HeaderFooter;
import com.aspose.words.HeaderFooterType;
import com.aspose.words.ParagraphAlignment;
import com.aspose.words.Section;
import com.gwtech.in.service.HeaderFooterOperation;
import com.gwtech.in.utils.Constants;

public class HeaderFooterOperationImpl implements HeaderFooterOperation {
	
	private static final Logger logger = Logger.getLogger(HeaderFooterOperationImpl.class);
	
	public static void main(String[] args) {
		
		try {
			new HeaderFooterOperationImpl().addHeaderInfToDocument("/Users/administrator/Documents/TA-pre-editing-check-list/v1/DOCX/OUT-PUT/Ortho_topic_10.docx", 
					"/Users/administrator/Documents/TA-pre-editing-check-list/v1/DOCX/OUT-PUT-FINAL/Ortho_topic_10_op.docx");
		}catch(Exception exception) {	logger.error(exception.getMessage(), exception);	}
	}
	
	public void removeFooterFromDocFile(String inDoc, String outDoc) throws Exception {
		
		Document doc = new Document(inDoc);

		for (Section section : doc.getSections()) {
		    // Up to three different footers are possible in a section (for first, even and odd pages).
		    // We check and delete all of them.
		    HeaderFooter footer;

		    footer = section.getHeadersFooters().getByHeaderFooterType(HeaderFooterType.FOOTER_FIRST);
		    if (footer != null)
		        footer.remove();

		    // Primary footer is the footer used for odd pages.
		    footer = section.getHeadersFooters().getByHeaderFooterType(HeaderFooterType.FOOTER_PRIMARY);
		    if (footer != null)
		        footer.remove();

		    footer = section.getHeadersFooters().getByHeaderFooterType(HeaderFooterType.FOOTER_EVEN);
		    if (footer != null)
		        footer.remove();
		    

		    // Up to three different footers are possible in a section (for first, even and odd pages).
		    // We check and delete all of them.

		    footer = section.getHeadersFooters().getByHeaderFooterType(HeaderFooterType.FOOTER_FIRST);
		    if (footer != null)
		        footer.remove();

		    // Primary footer is the footer used for odd pages.
		    footer = section.getHeadersFooters().getByHeaderFooterType(HeaderFooterType.FOOTER_PRIMARY);
		    if (footer != null)
		        footer.remove();

		    footer = section.getHeadersFooters().getByHeaderFooterType(HeaderFooterType.FOOTER_EVEN);
		    if (footer != null)
		        footer.remove();
		    
		    footer = section.getHeadersFooters().getByHeaderFooterType(HeaderFooterType.HEADER_EVEN);
		    if (footer != null)
		        footer.remove();
		    
		    footer = section.getHeadersFooters().getByHeaderFooterType(HeaderFooterType.HEADER_FIRST);
		    if (footer != null)
		        footer.remove();
		    
		    footer = section.getHeadersFooters().getByHeaderFooterType(HeaderFooterType.HEADER_PRIMARY);
		    if (footer != null)
		        footer.remove();
		    
		    footer = section.getHeadersFooters().getByHeaderFooterType(FootnoteType.ENDNOTE);
		    if (footer != null)
		        footer.remove();
		    footer = section.getHeadersFooters().getByHeaderFooterType(FootnoteType.FOOTNOTE);
		    if (footer != null)
		        footer.remove();
		
		}

		doc.save(outDoc);
	}
	
	
	
	public void addHeaderInfToDocument(String inDoc, String outDoc) throws Exception {
		
		Document doc = new Document(inDoc);
		DocumentBuilder builder = new DocumentBuilder(doc);
		
		builder.moveToHeaderFooter(HeaderFooterType.HEADER_PRIMARY);
		builder.getParagraphFormat().setAlignment(ParagraphAlignment.RIGHT);
		builder.write(Constants.projectAuthor + " "+(Constants.projectChapter)+"-");
		builder.insertField(FieldType.FIELD_PAGE, true);
		
		doc.save(outDoc);
	}
}
