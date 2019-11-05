package com.arabbank.bpm.tesseract.schduler;

import java.util.Calendar;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.arabbank.bpm.tesseract.controller.TesseractFormDBController;

@Component
public class DBPersistance {
	
	@Autowired private TesseractFormDBController tesseractFormDBController;
	
	@Autowired private Environment env;
    private static final Logger logger = LoggerFactory.getLogger(ImageProcessor.class);
   
    private int noOfRecords = 0;
    private String status = "";
    private String flag = "";
	
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public int updateTesseractReferene(String tesseractServiceReference) {    	
    	 
    	noOfRecords = 0;
    	status = env.getProperty("bpm.tesseract.execution_status.processing");
    	flag =  env.getProperty("bpm.tesseract.execution_flag.processing");
    	String submittedFlag = env.getProperty("bpm.tesseract.execution_flag.submitted");
    	noOfRecords = tesseractFormDBController.updateTesseractServiceReference(status,flag, tesseractServiceReference, submittedFlag);
    	logger.info("noOfRecords :"+ noOfRecords );
    	return noOfRecords;
    	
    }
    
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public int updateFormData(String formDataJson, long bpmRequestId) {
    	
    	noOfRecords = 0;
    	status = env.getProperty("bpm.tesseract.execution_status.completed");
    	flag =  env.getProperty("bpm.tesseract.execution_flag.completed");
    	noOfRecords = tesseractFormDBController.updateFormData(status,flag,formDataJson, bpmRequestId);
    	return noOfRecords;
    }

}
