package com.arabbank.bpm.tesseract.controller;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.Calendar;

import javax.imageio.ImageIO;

import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.arabbank.bpm.tesseract.engine.TesseractEngine;
import com.arabbank.bpm.tesseract.model.TesseractForm;
import com.arabbank.bpm.tesseract.owp.OWPFormExtractor;
import com.arabbank.bpm.tesseract.services.OpenOCRService;
import com.arabbank.bpm.tesseract.util.FileUtility;

import net.sourceforge.tess4j.ITessAPI;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;


@RestController
@PropertySource("classpath:application.properties")
public class BPMFileUploadController {	
	
	private static final Logger logger = LoggerFactory.getLogger(TesseractFormDBController.class);
	@Autowired private Environment env;
		
	private String tesseractServiceReference = "";
			
	@Autowired private TesseractFormDBController tesseractFormDBController;
	@Autowired private TesseractEngine tesseractEngine;	
	@Autowired private OWPFormExtractor owpFormExtractor;
	@Autowired private FileUtility fileUtility;
	
	@CrossOrigin(origins = "http://localhost:3000")
	@RequestMapping(value="/tesseract/api/convert-file", method=RequestMethod.POST, consumes = org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE  )
    public @ResponseBody String  convertFile(@RequestParam("countryCode") String countryCode,@RequestParam("serviceCode") String serviceCode,
    		@RequestParam("processCode") String processCode, @RequestParam("file") MultipartFile file, @RequestParam("formType") String formType) 
    		    throws Exception {
		
		String fileContent =  "" ;
		String fileData = "";		
		String responseCode = "9999";
		
		logger.info("tessdata : "+env.getProperty("tesseract.tessdata.path"));
		logger.info("language : "+env.getProperty("tesseract.lang"));
		logger.info("File name: "+file.getOriginalFilename());
		logger.info("countryCode: "+countryCode);
		logger.info("serviceCode: "+serviceCode);
		logger.info("processCode: "+processCode);
				
		String ext =  FilenameUtils.getExtension(file.getOriginalFilename());
		BufferedImage bufferedImage = null;	
		
		String fileName = file.getOriginalFilename();
		String filePath = env.getProperty("bpm.fileUpload.path");
		
		try {	
			
			if ( "pdf".equals(ext)  || "tif".equals(ext) || "tiff".equals(ext)  ) {				
											
				File fileToSave = new File(filePath+fileName);
				fileToSave.createNewFile();			
				try(FileOutputStream fout= new FileOutputStream(fileToSave)) {
					fout.write(file.getBytes());
					logger.info("File successfully saved: " + fileName);
					
					if ("pdf".equals(ext)) {
						bufferedImage = fileUtility.generateImageFromPDF(fileToSave,0);
					}
					else {
						bufferedImage = fileUtility.splitFirstPageFromTIFF(fileToSave);
					}
				}
				catch (Exception e) {
					 logger.error("Exception in converting PDF to Img: ", fileName);
					e.printStackTrace();					
					responseCode = "9997";
					return "{\"responseCode\": \""+responseCode+"\", \"fileData\": \"Exception in converting Image\"}";
				}	
				
			}		
			else if ("png".equals(ext) ||  "jpg".equals(ext) ) {						
				bufferedImage =  ImageIO.read(file.getInputStream());	
			}
			else {				
				responseCode = "9998";
				return  "{\"responseCode\": \""+responseCode+"\", \"fileData\": \"png,jpg,tif & pdf file type is only allowed!\"}";
			}
       
	        Tesseract tesseract = tesseractEngine.getTesseract(formType);
			fileContent = tesseract.doOCR(bufferedImage);
			
			logger.info(" fileContent after OCR:" + fileContent );
			
			String[] words = fileContent.split(" ");
			StringBuffer sb = new StringBuffer();			
			for (String word : words) {

				word = word.replaceAll("\\r\\n|\\r|\\n", " ");
				word = owpFormExtractor.removeArabicAndSpecialCharacters(word);
				if (word == null || word.trim().length() == 0)
					continue;

				sb.append(word);
				sb.append(" ");
			}	
			fileContent = sb.toString();
			logger.info(" fileContent after removing ara:" + fileContent );
			
			fileData = owpFormExtractor.extractFormData(fileContent, formType);
			responseCode = "0000";
		    logger.info(" formJsonString:" + fileData );
			
		} catch ( IOException | TesseractException e) {
			e.printStackTrace();
			responseCode = "9999";
			return  "{\"responseCode\": \""+responseCode+"\", \"fileData\": \"Technical Exception\"}";
			
		}
		
		return  "{\"responseCode\": \""+responseCode+"\", \"fileData\": \""+fileData+"\"}";
		}
	
	
	@CrossOrigin(origins = "http://localhost:3001")
	@RequestMapping(value="/tesseract/api/submit-file", method=RequestMethod.POST, consumes = org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE  )
    public @ResponseBody String executeFile(@RequestParam("countryCode")String countryCode,@RequestParam("serviceCode")String serviceCode, 
    		@RequestParam("processCode")String processCode, @RequestParam("file") MultipartFile file ) throws IOException {
       
		 String bpmRequestId = "";
		 String status = "";
		 String responseCode = "9999";
		
		 String filePath = env.getProperty("bpm.fileUpload.path");
		 
		String fileName = file.getOriginalFilename();
		
		
		logger.info("fileName: " + fileName);
		logger.info("filePath: " + filePath);
					
		File convertFile = new File(filePath+fileName);
		convertFile.createNewFile();			
		try(FileOutputStream fout= new FileOutputStream(convertFile)) {
			fout.write(file.getBytes());
			logger.info("File successfully saved: " + fileName);
		}
		catch (Exception e) {
			 logger.error("File not saved: ", fileName);
			e.printStackTrace();
			responseCode = "9998";
			status = "File not saved";
			return "{\"bpmRequestId\": \""+bpmRequestId+"\", \"responseCode\": \""+responseCode+"\", \"status\": \""+status+ "\"}";
		}		
		
		
		try {
	        TesseractForm tesseractForm = new TesseractForm();	        
	        tesseractForm.setFileName(fileName);
	        tesseractForm.setFilePath(filePath);
	        tesseractForm.setCountryCode(countryCode);
	        tesseractForm.setProcessCode(processCode);
	        tesseractForm.setExecutionFlag(env.getProperty("bpm.tesseract.execution_flag.submitted"));
	        tesseractForm.setExecutionStatus(env.getProperty("bpm.tesseract.execution_status.submitted"));        
	        tesseractServiceReference = Long.toString(Calendar.getInstance().getTimeInMillis());
	        tesseractForm.setTesseractServiceReference(tesseractServiceReference);	       
	        
	        logger.info("tesseractForm :"+tesseractForm.toString());
	        
	        tesseractForm = tesseractFormDBController.createFormData(tesseractForm);
	        bpmRequestId = Long.toString(tesseractForm.getBpmRequestId());
	        status = tesseractForm.getExecutionStatus();
	        responseCode = "0000";
		}
		catch (Exception e) {
			logger.error("DB Exception " , e.getMessage());
			e.printStackTrace();
			responseCode = "9999";
			status = "DB Exception";
			return "{\"bpmRequestId\": \""+bpmRequestId+"\", \"responseCode\": \""+responseCode+"\", \"status\": \""+status+ "\"}";
		}	
		
        return "{\"bpmRequestId\": \""+bpmRequestId+"\", \"responseCode\": \""+responseCode+"\", \"status\": \""+status+ "\"}";
    }
	
	@SuppressWarnings("resource")
	@CrossOrigin(origins = "http://localhost:3001")
	@RequestMapping(value="/tesseract/api/submit-file2", method=RequestMethod.POST, consumes = org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE  )
    public @ResponseBody String submitFile(@RequestParam("countryCode")String countryCode,@RequestParam("serviceCode")String serviceCode, 
    		@RequestParam("processCode")String processCode, @RequestParam("file") MultipartFile file, @RequestParam("fileBase64") String fileBase64, @RequestParam("fileName") String fileName) throws IOException {
       
		 String bpmRequestId = "";
		 String status = "";
		 String responseCode = "9999";
		
		
		String filePath = env.getProperty("bpm.fileUpload.path");
		logger.info("filePath: " + filePath);	
		
		// if File object is passed in request
		if (!file.isEmpty()) {
			
			fileName = file.getOriginalFilename();			
			logger.info("fileName: " + fileName);
								
			File convertFile = new File(filePath+fileName);
			convertFile.createNewFile();			
			try(FileOutputStream fout= new FileOutputStream(convertFile)) {
				fout.write(file.getBytes());
				logger.info("File successfully saved: " + fileName);
			}
			catch (Exception e) {
				 logger.error("File not saved: ", fileName);
				e.printStackTrace();
				responseCode = "9998";
				status = "File not saved";
				return "{\"bpmRequestId\": \""+bpmRequestId+"\", \"responseCode\": \""+responseCode+"\", \"status\": \""+status+ "\"}";
			}	
			
		}
		else { // if Base64string is passed in request
			
			try
	        {
				logger.info("fileName: " + fileName);
				  byte imgByte[] = Base64.getDecoder().decode(fileBase64);
				    FileOutputStream fos = new FileOutputStream(new File(filePath+fileName)); 
				    fos.write(imgByte); 
				    fos.close();
	        }
			catch (Exception e) {
				 logger.error("File not saved: ", fileName);
				e.printStackTrace();
				responseCode = "9998";
				status = "File not saved";
				return "{\"bpmRequestId\": \""+bpmRequestId+"\", \"responseCode\": \""+responseCode+"\", \"status\": \""+status+ "\"}";
			}	
			
		}
							
		try {
	        TesseractForm tesseractForm = new TesseractForm();	        
	        tesseractForm.setFileName(fileName);
	        tesseractForm.setFilePath(filePath);
	        tesseractForm.setCountryCode(countryCode);
	        tesseractForm.setProcessCode(processCode);
	        tesseractForm.setExecutionFlag(env.getProperty("bpm.tesseract.execution_flag.submitted"));
	        tesseractForm.setExecutionStatus(env.getProperty("bpm.tesseract.execution_status.submitted"));        
	        tesseractServiceReference = Long.toString(Calendar.getInstance().getTimeInMillis());
	        tesseractForm.setTesseractServiceReference(tesseractServiceReference);	       
	        
	        logger.info("tesseractForm :"+tesseractForm.toString());
	        
	        tesseractForm = tesseractFormDBController.createFormData(tesseractForm);
	        bpmRequestId = Long.toString(tesseractForm.getBpmRequestId());
	        status = tesseractForm.getExecutionStatus();
	        responseCode = "0000";
		}
		catch (Exception e) {
			logger.error("DB Exception " , e.getMessage());
			e.printStackTrace();
			responseCode = "9999";
			status = "DB Exception";
			return "{\"bpmRequestId\": \""+bpmRequestId+"\", \"responseCode\": \""+responseCode+"\", \"status\": \""+status+ "\"}";
		}	
		
        return "{\"bpmRequestId\": \""+bpmRequestId+"\", \"responseCode\": \""+responseCode+"\", \"status\": \""+status+ "\"}";
    }

}
