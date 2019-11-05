package com.arabbank.bpm.tesseract.services;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import com.arabbank.bpm.tesseract.util.MultipartUtility;

import java.io.File;
import java.io.IOException;


@Configuration
@PropertySource("classpath:application.properties")
@Service
public class OpenOCRService {
	private static final Logger logger = LoggerFactory.getLogger(OpenOCRService.class);
	private String responseText = "";

	 @Autowired
	 private Environment env;

	public String processImageFromUrl(String base64String, String language){
		logger.trace("BlueScanEngine:translateFileToText()");
		logger.info("Send request file to" + env.getProperty("openocr.restapi.fileBase64.api"));

		String json = "{\"" + env.getProperty("openocr.restapi.param.img_base64") + "\":\"" + base64String + "\",\"" + env.getProperty("openocr.restapi.param.engine")  + "\":\"" + env.getProperty("openocr.restapi.param.engine.value")
						+ "\",\"" +  env.getProperty("openocr.restapi.param.lang")  + "\":\"" + language + "\"}";

		int status = 0;
		CloseableHttpClient httpclient = HttpClients.createDefault();
		HttpPost httpPost = new HttpPost(env.getProperty("openocr.restapi.fileBase64.api"));
		HttpEntity stringEntity = new StringEntity(json, ContentType.APPLICATION_JSON);
		httpPost.setEntity(stringEntity);

		try {
			CloseableHttpResponse httpResponse = httpclient.execute(httpPost);
			status = httpResponse.getStatusLine().getStatusCode();
			logger.info("Returned OK status: " + status);
			responseText = EntityUtils.toString(httpResponse.getEntity());
		} catch (IOException e) {
			logger.error("Returned NON-OK status: " + status);
		}
		return responseText;
	}

	public String convertFileToText(String filePath, String language) {
		logger.trace("translateFileToText()");
		logger.info("Send request file to :" + env.getProperty("openocr.restapi.fileUpload.api"));

		long lStartTime = System.nanoTime();

		String json = "{\"" + env.getProperty("openocr.restapi.param.img_url") + "\":\"\",\"" +	env.getProperty("openocr.restapi.param.engine") + "\":0,\"" +
				env.getProperty("openocr.restapi.param.engineargs") +"\":{\"" + 
				//env.getProperty("openocr.restapi.param.psm") + "\":\"" + env.getProperty("openocr.restapi.param.psm.value") + "\",\"" +
				env.getProperty("openocr.restapi.param.lang") + "\":\"" + language + "\"},\"" +
				env.getProperty("openocr.restapi.param.inplace_decode") + "\":" + env.getProperty("openocr.restapi.param.inplace_decode.value") + "}";

		logger.info("input parameter :"+json);
		File file = new File(filePath);
		logger.info("filePath :"+filePath);

		try {
			MultipartUtility multipart = new MultipartUtility(env.getProperty("openocr.restapi.fileUpload.api"), "UTF-8");

			multipart.addFormField(json);
			multipart.addFilePart(file);

			responseText = multipart.finish();
		} catch (Exception ex) {
			logger.error("Exception while executing request", ex);
		}

		long lEndTime = System.nanoTime();
		long elapsedTime = (lEndTime - lStartTime) / 1000000000;

		logger.info("Request " + filePath + " returned in " + elapsedTime + " seconds");

		return responseText;
	}
}
