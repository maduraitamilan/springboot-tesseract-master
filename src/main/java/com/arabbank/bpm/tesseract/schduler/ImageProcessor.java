package com.arabbank.bpm.tesseract.schduler;

import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;

import com.arabbank.bpm.tesseract.controller.TesseractFormDBController;
import com.arabbank.bpm.tesseract.engine.TesseractEngine;
import com.arabbank.bpm.tesseract.model.TesseractForm;
import com.arabbank.bpm.tesseract.owp.OWPFormExtractor;
import com.arabbank.bpm.tesseract.services.OpenOCRService;
import com.arabbank.bpm.tesseract.util.FileUtility;

import net.sourceforge.tess4j.Tesseract;

import java.awt.image.BufferedImage;
import java.io.File;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.imageio.ImageIO;

@EnableAsync
@Component
public class ImageProcessor {

	@Autowired private TesseractFormDBController tesseractFormDBController;
	@Autowired private OpenOCRService openOcrService;
	@Autowired private OWPFormExtractor owpFormExtractor;
	@Autowired private DBPersistance dbPersistance;
	@Autowired private FileUtility fileUtility;	
	@Autowired private TesseractEngine tesseractEngine;	

	@Autowired
	private Environment env;
	private static final Logger logger = LoggerFactory.getLogger(ImageProcessor.class);
	static File inputFile = null;
	private String tesseractServiceReference = "";
	private int noOfRecords = 0;
	private String status = "";
	private String flag = "";

	@Async
	@Scheduled(cron = "${openocr.restapi.scheduler.cron}")
	public void initiateImageProcessor() {

		final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
		logger.info(
				"Current Thread : {" + Thread.currentThread().getName() + "} {" + dateFormat.format(new Date()) + "}");
		tesseractServiceReference = Long.toString(Calendar.getInstance().getTimeInMillis());
		logger.info("tesseractServiceReference :" + tesseractServiceReference);
		noOfRecords = dbPersistance.updateTesseractReferene(tesseractServiceReference);
		String formType = "";

		if (noOfRecords > 0) {

			List<TesseractForm> formList = tesseractFormDBController.getFormDataByReference(tesseractServiceReference);
			for (int i = 0; i < formList.size(); i++) {
				TesseractForm tesseractForm = formList.get(i);
				String absoluteFilePath = tesseractForm.getFilePath() + tesseractForm.getFileName();

				logger.info("tesseractForm.getFileName() :" + tesseractForm.getFileName());

				String fileName = FilenameUtils.getBaseName(tesseractForm.getFileName());
				String ext = FilenameUtils.getExtension(tesseractForm.getFileName());

				String fileContent = "";

				if ("pdf".equals(ext)) {
					try {
						BufferedImage bufferedImage = null;
						String imgPath = tesseractForm.getFilePath() + fileName + ".png";
						File imageToSave = new File(imgPath);
						bufferedImage = fileUtility.generateImageFromPDF(new File(absoluteFilePath), 0);
						ImageIO.write(bufferedImage, "png", imageToSave);
						fileContent = openOcrService.convertFileToText(imgPath,	env.getProperty("openocr.restapi.param.lang.value"));
						
//						Tesseract tesseract = tesseractEngine.getTesseract(formType);
//						fileContent = tesseract.doOCR(imageToSave);
						
						//logger.info(" fileContent after OCR:" + fileContent );
						
					} catch (Exception e) {
						e.printStackTrace();
					}
				} else {
					
					try {
						
					fileContent = openOcrService.convertFileToText(absoluteFilePath,env.getProperty("openocr.restapi.param.lang.value"));
											
//					File imageFile = new File(absoluteFilePath);
//					Tesseract tesseract = tesseractEngine.getTesseract(formType);
//					fileContent = tesseract.doOCR(imageFile);
					
					//logger.info(" fileContent after OCR:" + fileContent );
					}
					catch(Exception e) {
						e.printStackTrace();	
					}
				}			
				

				StringBuffer sb = new StringBuffer();
				String[] words = fileContent.split(" ");
				for (String word : words) {

					word = word.replaceAll("\\r\\n|\\r|\\n", " ");
					word = owpFormExtractor.removeArabicAndSpecialCharacters(word);
					if (word == null || word.trim().length() == 0)
						continue;

					sb.append(word);
					sb.append(" ");
				}

				fileContent = sb.toString();
				//logger.info("contents after removing arabic:" + fileContent);

				String formDataJson = owpFormExtractor.extractFormData(fileContent, "");
				logger.info(" formJsonString:" + formDataJson);

				status = env.getProperty("bpm.tesseract.execution_status.completed");
				flag = env.getProperty("bpm.tesseract.execution_flag.completed");

				int updatedCnt = dbPersistance.updateFormData(formDataJson, tesseractForm.getBpmRequestId());

			}
		}

	}

}