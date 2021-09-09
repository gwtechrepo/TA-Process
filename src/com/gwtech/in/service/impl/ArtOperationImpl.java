package com.gwtech.in.service.impl;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.apache.log4j.Logger;

import com.gwtech.in.service.ArtOperation;

public class ArtOperationImpl implements ArtOperation {

	private static final Logger logger = Logger.getLogger(ArtOperationImpl.class);

	/**
	 * Gets image dimensions for given file
	 * 
	 * @param imgFile image file
	 * @return dimensions of image
	 * @throws IOException if the file is not a known image
	 */
	public String getImageDimension(File imgFile) {

		int width = 0;
		String type = "image";

		try {
			BufferedImage bimg = ImageIO.read(imgFile);
			if (bimg != null) {
				
				width = bimg.getWidth();
	//			height         = bimg.getHeight();
	//			logger.info("width: " + (width));
	//			logger.info("height: " + (height));
	
				if (width > 60)
					type = "image";
				else {
					type = "icon";
				}
			}

		} catch (Exception exception) {
			logger.error(exception.getMessage());
		}
		return type;
	}
	
	
	
	public double compareTwoImages(String imageOne, String imageSecond) {
		double percentage = 0.0;
		try {
		BufferedImage img1 = ImageIO.read(new File(imageOne));// "/Users/administrator/Documents/TA-pre-editing-check-list/operation-01/test/DOCX/images/icon-13-9780323826754.jpeg"
		BufferedImage img2 = ImageIO.read(new File(imageSecond));// "/Users/administrator/Documents/TA-pre-editing-check-list/operation-01/test/DOCX/images/icon-10-9780323826754.jpeg"
		int w1 = img1.getWidth();
		int w2 = img2.getWidth();
		int h1 = img1.getHeight();
		int h2 = img2.getHeight();
		if ((w1 != w2) || (h1 != h2)) {
			logger.debug("Both images should have same dimwnsions");
			percentage = 1.0;
		} else {
			long diff = 0;
			for (int j = 0; j < h1; j++) {
				for (int i = 0; i < w1; i++) {
					// Getting the RGB values of a pixel
					int pixel1 = img1.getRGB(i, j);
					Color color1 = new Color(pixel1, true);
					int r1 = color1.getRed();
					int g1 = color1.getGreen();
					int b1 = color1.getBlue();
					int pixel2 = img2.getRGB(i, j);
					Color color2 = new Color(pixel2, true);
					int r2 = color2.getRed();
					int g2 = color2.getGreen();
					int b2 = color2.getBlue();
					// sum of differences of RGB values of the two images
					long data = Math.abs(r1 - r2) + Math.abs(g1 - g2) + Math.abs(b1 - b2);
					diff = diff + data;
				}
			}
			double avg = diff / (w1 * h1 * 3);
			percentage = (avg / 255) * 100;
			logger.info("Difference: " + percentage);
		}
		}catch(Exception exception) {	logger.error(exception.getMessage());	};
		return percentage;
	}
}
