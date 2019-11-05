package com.arabbank.bpm.tesseract.controller;

import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.arabbank.bpm.tesseract.exception.ResourceNotFoundException;
import com.arabbank.bpm.tesseract.model.TesseractForm;
import com.arabbank.bpm.tesseract.repository.TesseractFormRepository;

@RestController
@PropertySource("classpath:application.properties")
public class TesseractFormDBController {
	
    private static final Logger logger = LoggerFactory.getLogger(TesseractFormDBController.class);
    private Environment env;
    
    @Autowired TesseractFormRepository tesseractFormRepository;
    @Autowired TesseractForm tesseractForm;

       
    //Get a Single Form Data - Complete
    @CrossOrigin(origins = "http://localhost:3001")
    @RequestMapping(value="/tesseract/api/formdata", method=RequestMethod.GET)
	public TesseractForm getFormDataById(@RequestParam(value = "bpmRequestId") String bpmRequestId) {
	    return tesseractFormRepository.findById(Long.parseLong(bpmRequestId))
	            .orElseThrow(() -> new ResourceNotFoundException("TesseractForm", "bpmRequestId", bpmRequestId));
	}

 	public List<TesseractForm> getFormDataByReference(@PathVariable(value = "tesseractReference") String tesseractReference) {
	    return tesseractFormRepository.findByTesseractServiceReference(tesseractReference);
	}
    
    // Create a new Form Data
	public TesseractForm createFormData(TesseractForm formData) {
		return tesseractFormRepository.save(formData);
	}
    
	public int updateTesseractServiceReference(String status, String flag,String tesseractServiceReference, String submittedFlag) {
		return tesseractFormRepository.updateTesseractServiceReference(status,flag,tesseractServiceReference, submittedFlag);
	}
	
	public int updateFormData(String status, String flag, String formJsonOutput, long bpmRequestId ) {
		return tesseractFormRepository.updateFormData(status,flag,formJsonOutput,bpmRequestId);
	}

	// Get a Single Form Data - extracted output
	@CrossOrigin(origins = "http://localhost:3001")
	@RequestMapping(value="/tesseract/api/get-filedata", method=RequestMethod.GET)
	public String getFormData(@RequestParam("countryCode") String countryCode,@RequestParam("serviceCode") String serviceCode,
    		@RequestParam("processCode") String processCode, @RequestParam("bpmRequestId") String bpmRequestId ) {		
		
		 String status = "";
		 String responseCode = "9999";
		 String fileData = "";
		 try {
			 tesseractForm = tesseractFormRepository.findByBpmRequestId(Long.parseLong(bpmRequestId));
		   	 status = tesseractForm.getExecutionStatus();
		   	 fileData = tesseractForm.getFormJsonOutput();
		   	 responseCode = "0000";
		 }
		 catch(Exception e) {
			 e.printStackTrace();
			 responseCode = "9999";
			status = "Exception";
		   	fileData = "";
		 }
	   
	   return "{\"bpmRequestId\": \""+bpmRequestId+"\", \"responseCode\": \""+responseCode+"\", \"status\": \""+status+ "\", \"fileData\": "+fileData+ "}";
	}
	
}
