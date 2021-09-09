package com.gwtech.in.service;

import java.io.File;

public interface ArtOperation {

	public String getImageDimension(File imgFile);
	public double compareTwoImages(String imageOne, String imageSecond);
}
