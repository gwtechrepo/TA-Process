package com.gwtech.in.test;

import java.text.DecimalFormat;

public class AsposeComments {

	public static void main(String[] args) {
		try {

//			String pattern = "0000.#";
//			int number = 1;
//
//			System.out.println(new DecimalFormat(pattern).format(number));
			
			System.out.println(new DecimalFormat("000.#").format(8));

		} catch (Exception exception) {
			exception.printStackTrace();
		}
	}
}
