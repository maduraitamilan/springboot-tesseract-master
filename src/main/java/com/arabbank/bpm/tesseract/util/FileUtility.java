package com.arabbank.bpm.tesseract.util;

import org.apache.commons.io.FileUtils;
import org.apache.pdfbox.multipdf.Splitter;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.tools.imageio.ImageIOUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.multipart.MultipartFile;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
@PropertySource("classpath:application.properties")
public class FileUtility {
    private static final Logger logger = LoggerFactory.getLogger(FileUtility.class);    
    @Autowired 	private Environment env;

    public boolean save(MultipartFile file) {
        Path path = null;
        try {
            byte[] bytes = file.getBytes();
            path = Paths.get(env.getProperty("bpm.fileUpload.path") + file.getOriginalFilename());
            Files.write(path, bytes);
        } catch (IOException e) {
            logger.error("Failed to save file by path: " + path, e.getMessage());
        }

        return true;
    }
    
	public static String readContents(String filePath) {	
		
		String fileContent ="";
		try {
			fileContent = FileUtils.readFileToString(new File(filePath), StandardCharsets.UTF_8);
		} 
		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
		return fileContent;
		/*
		StringBuilder contentBuilder = new StringBuilder();
		try (Stream<String> stream = Files.lines(Paths.get(filePath), StandardCharsets.UTF_8)) {			
			stream.forEach(s -> contentBuilder.append(s));
		} catch (IOException e) {
			e.printStackTrace();
		}
		return contentBuilder.toString();
		*/
	}
	
	public BufferedImage splitFirstPageFromTIFF(File file) {

		BufferedImage bufferedImage = null;			
		
		try {
			ImageInputStream is = ImageIO.createImageInputStream(file);
			if (is == null || is.length() == 0){
				logger.error("File not found");
			}
			else {
				Iterator<ImageReader> iterator = ImageIO.getImageReaders(is);
				if (iterator == null || !iterator.hasNext()) {			  
				  logger.error("Image file format not supported by ImageIO");
				}
				else {
				// We are just looking for the first reader compatible:
				ImageReader reader = (ImageReader) iterator.next();
				iterator = null;
				reader.setInput(is);
				bufferedImage = reader.read(0);
				}
			}
		}
		
			
		/*
		try {
			ImageInputStream iis = ImageIO.createImageInputStream(file);
			ImageReader reader = getTiffImageReader();
			reader.setInput(iis);
			bufferedImage = reader.read(0);
		
		} 
		*/
		catch (Exception ex) {
			ex.printStackTrace();
		}
				
		return bufferedImage;
	}
	
	private static ImageReader getTiffImageReader() {
		Iterator<ImageReader> imageReaders = ImageIO.getImageReadersByFormatName("TIFF");
		if (!imageReaders.hasNext()) {
			throw new UnsupportedOperationException("No TIFF Reader found!");
		}
		return imageReaders.next();
	}
	
	public BufferedImage generateImageFromPDF(File pdfFile, int pagenumber)  {   
		
		BufferedImage bufferedImage = null;
		try {
		PDDocument document = PDDocument.load(pdfFile);
	    PDFRenderer pdfRenderer = new PDFRenderer(document);	
	     	
	    bufferedImage = pdfRenderer.renderImageWithDPI(pagenumber, 300, ImageType.GRAY); 	    
	
	    document.close();
	  
	    }
	    catch(Exception e) {	    	
	    	e.printStackTrace();
	    }
	
	    
	    return bufferedImage;
	}
}