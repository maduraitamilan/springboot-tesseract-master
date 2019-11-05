package com.arabbank.bpm.tesseract.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.arabbank.bpm.tesseract.model.TesseractForm;


@Repository
public interface TesseractFormRepository extends JpaRepository<TesseractForm, Long> {
	
	public final static String UPDATE_TESSERACT_REFERENCE = "UPDATE TesseractForm " +
            "SET EXECUTION_STATUS = :processingStatus,  EXECUTION_FLAG = :processingFlag, TESSERACT_SERVICE_REFERENCE = :tesseractServiceReference "+
			" WHERE EXECUTION_FLAG = :submittedFlag ";
	
	public final static String UPDATE_FORM_DATA = "UPDATE TesseractForm " +
            "SET EXECUTION_STATUS = :completedStatus,  EXECUTION_FLAG = :completedFlag, FORM_JSON_OUTPUT = :formJsonOutput "+
			" WHERE BPM_REQUEST_ID = :bpmRequestId ";
	
	
	public List<TesseractForm> findByTesseractServiceReference(String tesseractServiceReference);
	
	public TesseractForm findByBpmRequestId(Long bpmRequestId);
	
	@Modifying
	@Query(UPDATE_TESSERACT_REFERENCE)
	public int updateTesseractServiceReference(@Param("processingStatus") String completedStatus,@Param("processingFlag") String completedFlag,
			@Param("tesseractServiceReference") String tesseractServiceReference, @Param("submittedFlag") String submittedFlag);
	
	
	@Modifying
	@Query(UPDATE_FORM_DATA)
	public int updateFormData(@Param("completedStatus") String completedStatus,@Param("completedFlag") String completedFlag,
			@Param("formJsonOutput") String formJsonOutput, @Param("bpmRequestId") long bpmRequestId );
	

}
