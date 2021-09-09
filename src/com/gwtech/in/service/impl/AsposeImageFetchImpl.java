package com.gwtech.in.service.impl;

import com.gwtech.in.service.ArtOperation;
import com.gwtech.in.service.AsposeImageFetch;
import com.gwtech.in.utils.Constants;

import java.awt.image.BufferedImage;
import java.io.File;
import java.text.DecimalFormat;

import javax.imageio.ImageIO;

import org.apache.log4j.Logger;

import com.aspose.words.Document;
import com.aspose.words.FileFormatUtil;
import com.aspose.words.NodeCollection;
import com.aspose.words.NodeType;
import com.aspose.words.Shape;

@SuppressWarnings({"rawtypes", "unchecked"})
public class AsposeImageFetchImpl implements AsposeImageFetch {
	
	private DecimalFormat numberFormating = null;
	private ArtOperation artOperation;
	private static final Logger logger = Logger.getLogger(AsposeImageFetchImpl.class);
	
	
	public void iterateImg(String docPath, String destPath) {
		try {
			
			Document doc = new Document(docPath);//"/Users/administrator/Documents/TA-pre-editing-check-list/TA-inputs/v1/DOCX/Ortho_topic_13.docx"
			numberFormating = new DecimalFormat("00.#");
			
			NodeCollection shapes = doc.getChildNodes(NodeType.SHAPE, true);
			
			if (shapes.getCount() > 0) {
				
				File imgFile = new File(Constants.outputPath + "/Images/"); 
				if (imgFile.exists() == false)	imgFile.mkdirs();
			}
			
			int imageIndex = 1;
			for (Shape shape : (Iterable<Shape>) shapes)
			{
			    if (shape.hasImage())
			    {
			    	String chapInfo = Constants.chapterPrefix; // CH0021
			    	if (chapInfo.toLowerCase().contains("ch"))
			    		chapInfo = chapInfo.replace("ch", "").replace("CH", "");
			    	
			    	String ext = FileFormatUtil.imageTypeToExtension(shape.getImageData().getImageType());
			    	String imageFileName = "f" + chapInfo + "-" + (numberFormating.format((imageIndex++))) + "-" + Constants.ISBN + ext;
			    	shape.getImageData().save(Constants.outputPath + "/Images/" + (imageFileName.toLowerCase()));
			    	
			    	// if image is readable
			    	BufferedImage bimg = ImageIO.read(new File(Constants.outputPath + "/Images/" + (imageFileName.toLowerCase())));
					if (bimg != null) {
						
			    		String type = artOperation.getImageDimension(new File(Constants.outputPath + "/Images/" + (imageFileName.toLowerCase())));
					   	if (type.equalsIgnoreCase("icon") == false) {
					   		shape.remove();
				    	}
					}
			    }
			}
			
			doc.save(docPath);
			
		}catch(Exception exception) {	exception.printStackTrace();	}
	}



	public void setArtOperation(ArtOperation artOperation) {
		this.artOperation = artOperation;
	}
	
}
