package com.gwtech.in.service.impl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

@SuppressWarnings("deprecation")
public class HttpRequestImpl {
	
//	public static String apiUrl = "http://localhost:8089/";
	public static String apiUrl = "http://192.168.1.56/";
	
	public static void main(String[] a) {
        try {
            JSONArray array = get("auth/universal-char-request?type=universal");
            String symbol = checkSymbolWithUnicode(array, "Talking With Parents");
            System.out.println("symbol: "+symbol);
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }
	
	@SuppressWarnings("resource")
	public static JSONArray get(String url) throws ClientProtocolException, IOException, ParseException {
        
		HttpClient client = new DefaultHttpClient();
        HttpGet request = new HttpGet(apiUrl + url); // "http://localhost:8081/auth/special-char-request?category=math&operation=ind2word"
        HttpResponse response = client.execute(request);
        BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
        String line = "";

        JSONParser parser = new JSONParser();
        JSONObject json = null;
        JSONArray array = null;
        while ((line = rd.readLine()) != null) {
            //System.out.println(line);
            json = (JSONObject) parser.parse(line);
        }
        
        array = (JSONArray) json.get("body");
        
        return array;
    }
    
//    public static String fetchSymbolFromExpresion(JSONArray array, String expresion){
//        String symbol = "";
//        for (int index = 0; index < array.size(); index ++){
//            
//            JSONObject obj = (JSONObject) array.get(index);
//            String expresionObj = (String) obj.get("characterSymbol");
////            System.out.println(expresionObj);
//            if (expresionObj.equalsIgnoreCase(expresion)) {
//                symbol = (String) obj.get("hexadecimalUniCode");
//                break;
//            }
//        }
//        return symbol;
//    }
    
    
    public static String checkSymbolWithUnicode(JSONArray array, String paraText){
        String symbol = "";
        for (int index = 0; index < array.size(); index ++){
//        	<\#U2026>
            JSONObject obj = (JSONObject) array.get(index);
            String expresionObj = (String) obj.get("characterSymbol");
            if (
            		(expresionObj.equalsIgnoreCase("<") == false) &
            		(expresionObj.equalsIgnoreCase("\\") == false) &
            		(expresionObj.equalsIgnoreCase("#") == false) &
            		(expresionObj.equalsIgnoreCase(">") == false) &
            		(expresionObj.equalsIgnoreCase("(") == false) &
            		(expresionObj.equalsIgnoreCase(")") == false) &
            		(expresionObj.equalsIgnoreCase("&") == false) &
            		(expresionObj.equalsIgnoreCase(",") == false) &
            		(expresionObj.equalsIgnoreCase(":") == false) &
            		(expresionObj.equalsIgnoreCase(".") == false) &
            		(expresionObj.equalsIgnoreCase("?") == false) &
            		(expresionObj.equalsIgnoreCase("@") == false) &
            		(expresionObj.equalsIgnoreCase("/") == false) &
            		(expresionObj.equalsIgnoreCase("$") == false) &
            		(expresionObj.equalsIgnoreCase("+") == false) &
            		(expresionObj.equalsIgnoreCase("-") == false) &
            		(expresionObj.equalsIgnoreCase("[") == false) &
            		(expresionObj.equalsIgnoreCase("]") == false)  
            		)
            if (paraText.contains(expresionObj)) {
                symbol = (String) obj.get("hexadecimalUniCode");
                symbol = symbol.replace("\\", "").replace("u", "U");
                symbol = "<@symbol-open><\\#"+(symbol)+"><@symbol-close>";
                paraText = paraText.replace(expresionObj, symbol);
            }
        }
        return paraText;
    }
    
    public static String lineStartWithSymbols(JSONArray array, String paraText){
    	
    	paraText = paraText.trim();
        for (int index = 0; index < array.size(); index ++){
//        	<\#U2026>
            JSONObject obj = (JSONObject) array.get(index);
            String expresionObj = (String) obj.get("characterSymbol");
            if (
            		(expresionObj.equalsIgnoreCase("<") == false) &
            		(expresionObj.equalsIgnoreCase("\\") == false) &
            		(expresionObj.equalsIgnoreCase("#") == false) &
            		(expresionObj.equalsIgnoreCase(">") == false) &
            		(expresionObj.equalsIgnoreCase("(") == false) &
            		(expresionObj.equalsIgnoreCase(")") == false) &
            		(expresionObj.equalsIgnoreCase("&") == false) &
            		(expresionObj.equalsIgnoreCase(",") == false) &
            		(expresionObj.equalsIgnoreCase(":") == false) &
            		(expresionObj.equalsIgnoreCase(".") == false) &
            		(expresionObj.equalsIgnoreCase("?") == false) &
            		(expresionObj.equalsIgnoreCase("@") == false) &
            		(expresionObj.equalsIgnoreCase("/") == false) &
            		(expresionObj.equalsIgnoreCase("$") == false) &
            		(expresionObj.equalsIgnoreCase("+") == false) &
            		(expresionObj.equalsIgnoreCase("-") == false) &
            		(expresionObj.equalsIgnoreCase("[") == false) &
            		(expresionObj.equalsIgnoreCase("]") == false)  
            		)
            if (paraText.startsWith(expresionObj)) {
            	
            	paraText = paraText.substring((paraText.indexOf(expresionObj)) + (expresionObj.length()));
            	paraText = paraText.trim();
            	break;
            }
        }
        return paraText;
    }

    
    public JSONObject getTaSettings(String url) throws ClientProtocolException, IOException, ParseException {
        
		HttpClient client = new DefaultHttpClient();
        HttpGet request = new HttpGet(apiUrl + url); // "http://localhost:8081/auth/special-char-request?category=math&operation=ind2word"
        HttpResponse response = client.execute(request);
        BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
        String line = "";

        JSONParser parser = new JSONParser();
        JSONObject json = null;
        JSONObject jsonBody = null;
        while ((line = rd.readLine()) != null) {
            
            json = (JSONObject) parser.parse(line);
            jsonBody = (JSONObject) json.get("body");
        }
        
        return jsonBody;
    }
}
