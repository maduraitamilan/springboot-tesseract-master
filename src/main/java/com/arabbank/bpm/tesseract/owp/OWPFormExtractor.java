package com.arabbank.bpm.tesseract.owp;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.io.FileReader;
import java.io.IOException;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.arabbank.bpm.tesseract.controller.TesseractFormDBController;

@Component
@PropertySource("classpath:application.properties")
public class OWPFormExtractor {
	
	private static final Logger logger = LoggerFactory.getLogger(TesseractFormDBController.class);
	@Autowired private Environment env;
	@Autowired OWPFormDictionary owpFormDictionary;
	
	JSONObject jsonObject = null;
	
	public String extractFormData(String contents, String formType) {
		
		try {	
			JSONParser parser = new JSONParser();
			Object obj = parser.parse(new FileReader(env.getProperty("bpm.owp.form_pattern.file_path")));
			jsonObject = (JSONObject) obj;			
		}
		 catch (IOException | ParseException e) {			
				e.printStackTrace();
		}	
			
		
		//contents = correctContents(contents);
		//logger.info("contents after correction :"+contents);
		
		
		contents = contents.toUpperCase();
		contents = contents.replaceAll("\n", " ");	

		JSONArray patternArray = (JSONArray) jsonObject.get("regexPattern");
	
		JSONObject formObj = new JSONObject();
		
		for (Object pObj : patternArray) {
			JSONObject patternObj = (JSONObject) pObj;
			String key = (String) patternObj.get("key");
			String regexString = (String) patternObj.get("regexString");

			String extractedValue = "";
			Pattern pattern = Pattern.compile(regexString, Pattern.CASE_INSENSITIVE);
			Matcher matcher = pattern.matcher(contents);
			if (matcher.find()) {
				extractedValue = matcher.group(key);
				extractedValue = extractedValue.trim();
				if (extractedValue != "") {
					if ((key.equals("DebitAccount") || key.equals("ChargeAccount")
							|| key.equals("BeneficiaryAccount"))) {
						extractedValue = extractedValue.replaceAll("[^0-9]", "");
						extractedValue = extractedValue.substring(0,extractedValue.length() > 50 ? 50 : extractedValue.length());
					}
					else if ( key.equals("IBAN")) {
						extractedValue = extractedValue.replace("IBAN", "");	
						extractedValue = extractedValue.substring(0,extractedValue.length() > 50 ? 50 : extractedValue.length());
					}
					else if ( key.equals("BeneficiaryBankName")) {
						extractedValue = extractedValue.replace("ADDRESS", "");	
						extractedValue = extractedValue.substring(0,extractedValue.length() > 150 ? 150 : extractedValue.length());
					}
					else {
						extractedValue = extractedValue.substring(0,extractedValue.length() > 100 ? 100 : extractedValue.length());
					}
					
					extractedValue = extractedValue.trim();					
					extractedValue = extractedValue.replaceAll("[^.,a-zA-Z0-9\\s]", "");
				}
			}

			
			formObj.put(key, extractedValue);
			
		}

		return formObj.toJSONString();

	}
	
	private String correctContents(String contents) {		

		StringTokenizer st = new StringTokenizer(contents, " ");
		StringBuffer sb = new StringBuffer();
		StringBuffer userFilledData = new StringBuffer();
		while (st.hasMoreTokens()) {
			String word = st.nextToken();
			String suggestedWord = null;
			// System.out.println("word :"+word);
			Map<String, Double> suggestedWords = owpFormDictionary.findSuggestions(word);
			if (suggestedWords.size() > 0) {
				suggestedWord = suggestedWords.entrySet().iterator().next().getKey();

			}

			//System.out.println("suggestedWord :"+suggestedWord);
			if (suggestedWord != null ) {
				sb.append(suggestedWord).append(" ");
			} else {
				sb.append(word).append(" ");
				userFilledData.append(word).append("|");
				
			}

		}
		return sb.toString();
	}	
	

	public String removeArabicAndSpecialCharacters(String s) {
		
		StringBuffer sb=new StringBuffer();
		for (int i = 0; i < s.length();i++) {
			String character=String.valueOf(s.charAt(i));
			if(env.getProperty("tesseract.whitelist_characters").contains(character))
				sb.append(character);
			//i += Character.charCount(c);
		}
		
		return sb.toString();
	}	

}
