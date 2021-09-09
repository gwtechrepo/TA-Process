package com.gwtech.in.service.impl;

import java.util.ArrayList;
import java.util.List;

public class ConsecutiveFlow {
	
	// Function to find consecutive ranges
    
    
	
	public List<String> fetchConsecutiveRanges(Integer[] a)
    {
        int length = 1;
        List<String> list
            = new ArrayList<String>();
 
        // If the array is empty,
        // return the list
        if (a.length == 0) {
            return list;
        }
 
        // Traverse the array from first position
        for (int i = 1; i <= a.length; i++) {
 
            // Check the difference between the
            // current and the previous elements
            // If the difference doesn't equal to 1
            // just increment the length variable.
            if (i == a.length
                || a[i] - a[i - 1] != 1) {
 
                // If the range contains
                // only one element.
                // add it into the list.
                if (length == 1) {
//                    list.add(
//                        String.valueOf(a[i - length]));
                }
                else {
 
                    // Build the range between the first
                    // element of the range and the
                    // current previous element as the
                    // last range.
                    list.add(a[i - length]
                             + " -> " + a[i - 1]);
                }
 
                // After finding the first range
                // initialize the length by 1 to
                // build the next range.
                length = 1;
            }
            else {
                length++;
            }
        }
 
        return list;
    }
	
    public List<String> fetchNonConsecutiveRanges(Integer[] a)
    {
        int length = 1;
        
        List<String> listResult = new ArrayList<String>();
        // If the array is empty,
        // return the list
        if (a.length == 0) {
            return listResult;
        }
        
        int counter = 0;
        
        // Traverse the array from first position
        for (int i = 1; i <= a.length; i++) {
 
            // Check the difference between the current and the previous elements
            // If the difference doesn't equal to 1 just increment the length variable.
            if (i == a.length || a[i] - a[i - 1] != 1) {
 
                // If the range contains only one element.
                // add it into the list.
                if (length == 1) {
//                    list.add(String.valueOf(a[i - length]));
                	listResult.add(a[i - length] + "");
                }
                else {
 
                    // Build the range between the first element of the range and the
                    // current previous element as the last range.
                	
                	if (counter != 0) {
                		listResult.add(a[i - length] + "");
                	}
//                    list.add(a[i - length] + " -> " + a[i - 1]);
                    counter++;
                }
 
                // After finding the first range initialize the length by 1 to
                // build the next range.
                length = 1;
            }
            else {
                length++;
            }
        }
 
        return listResult;
    }
    
	
    
    public static void main(String args[]) {
 
    	List<String> list = null;
        // Test Case 1:
        Integer[] arr1 = { 1, 3, 2 };
        list = new ConsecutiveFlow(). fetchConsecutiveRanges(arr1);
        list.forEach(object -> {
        	System.out.println(object);
        });
        System.out.println("************************");
 
        // Test Case 2:
//        Integer[] arr2 = { -1, 0, 1, 2, 5, 6, 8 };
//        list = new ConsecutiveFlow(). fetchConsecutiveRanges(arr2);
//        list.forEach(object -> {
//        	System.out.println(object);
//        });
//        System.out.println("************************");
// 
//        // Test Case 3:
//        Integer[] arr3 = { -1, 3, 4, 5, 20, 21, 25 };
//        list = new ConsecutiveFlow(). fetchConsecutiveRanges(arr3);
//        list.forEach(object -> {
//        	System.out.println(object);
//        });
    }
    
}
