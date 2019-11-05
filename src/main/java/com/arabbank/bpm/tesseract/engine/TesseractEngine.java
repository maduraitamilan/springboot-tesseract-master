package com.arabbank.bpm.tesseract.engine;

import java.util.Arrays;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import net.sourceforge.tess4j.ITessAPI;
import net.sourceforge.tess4j.Tesseract;

@Component
@PropertySource("classpath:application.properties")
public class TesseractEngine {	

	@Autowired private Environment env;
		
	public Tesseract getTesseract(String formType) {		
		
		Tesseract tesseract =  new  Tesseract();	      
        
//        if ( formType.equals(env.getProperty("bpm.owp.form_type.form")) ) {
//        	tesseract.setLanguage("eng+ara");
//        }
//        else if ( formType.equals(env.getProperty("bpm.owp.form_type.letterhead")) ) {
//        	tesseract.setLanguage("eng");
//        }
//        else if ( formType.equals(env.getProperty("bpm.owp.form_type.formv2")) ) {
//        	tesseract.setLanguage("eng");
//        }
		tesseract.setDatapath (env.getProperty("tesseract.tessdata.path"));
        tesseract.setLanguage (env.getProperty("tesseract.lang")); 
        tesseract.setLanguage("eng");
        tesseract.setPageSegMode(ITessAPI.TessPageSegMode.PSM_AUTO);
        tesseract.setOcrEngineMode(ITessAPI.TessOcrEngineMode.OEM_DEFAULT);    
      
        tesseract.setTessVariable("tessedit_char_whitelist", env.getProperty("tesseract.whitelist_characters"));
        tesseract.setConfigs(Arrays.asList("config"));
        return tesseract;
	}	
}
