package com.arabbank.bpm.tesseract.controller;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.arabbank.bpm.tesseract.engine.TesseractEngine;
import com.arabbank.bpm.tesseract.util.FileUtility;

import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;

@RestController
@PropertySource("classpath:application.properties")
public class TesseractOCRController {
	
    private static final Logger logger = LoggerFactory.getLogger(TesseractFormDBController.class);
    private Environment env;
    @Autowired private FileUtility fileUtility;    
    @Autowired private TesseractEngine tesseractEngine;
    
    @RequestMapping(value = "/tesseract/api/ping", method = RequestMethod.GET)
    public String ping() throws Exception {
        return "OK";
    }
    
    @RequestMapping(value="/tesseract/api/ocr", method=RequestMethod.POST, consumes = org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE  )
    public @ResponseBody String  doOCR( @RequestParam("file") MultipartFile file) 
    		    throws Exception {    	
    	
    	String fileContent = "";    	
    	BufferedImage bufferedImage = null;      	
  	   	
    	String ext =  FilenameUtils.getExtension(file.getOriginalFilename());
	
		String fileName = file.getOriginalFilename();
		//String filePath = env.getProperty("bpm.fileUpload.path");
		String filePath = "C:\\Tessearact\\owp-uploaded\\";
		
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
					
					return "Exception in converting Image";
				}	
				
			}		
			else if ("png".equals(ext) ||  "jpg".equals(ext) ) {						
				bufferedImage =  ImageIO.read(file.getInputStream());	
			}
			else {				
				return  "png,jpg,tif & pdf file type is only allowed!";
			}
       
	        Tesseract tesseract = tesseractEngine.getTesseract("");
			fileContent = tesseract.doOCR(bufferedImage);
			
			logger.info(" fileContent after OCR:" + fileContent );
		
		} catch ( IOException | TesseractException e) {
			e.printStackTrace();			
			return  "Technical Exception";
			
		}
			
			
    	return fileContent;    	
    
    }

}
