package com.arabbank.bpm.tesseract.model;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import javax.validation.constraints.NotBlank;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;


@Component
@Entity
@Table(name = "AB_TESSERACT_FORM")
@EntityListeners(AuditingEntityListener.class)
public class TesseractForm {
	
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private long bpmRequestId;
	@NotBlank
	private String countryCode;
	@NotBlank
	private String processCode;
	@NotBlank
	private String fileName;

	private String filePath;
	
	@NotBlank
	private String executionFlag;
	@NotBlank
	private String executionStatus;
	@NotBlank
	private String tesseractServiceReference;
	
	@Column(length = 4000)
	private String formJsonOutput;
	
//	@Column(name = "CREATED_TIME", nullable = false, updatable = false,  columnDefinition="TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
//	@Temporal(TemporalType.TIMESTAMP)
//    private Date createdTime;
//
//
//    @Column(name = "UPDATED_TIME", nullable = false, updatable = false, columnDefinition="TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
//    @Temporal(TemporalType.TIMESTAMP)
//    private Date updatedTime;
    
       
	@CreationTimestamp
    private Date createdTime;

	@UpdateTimestamp
    private Date updatedTime;

	@Override
	public String toString() {
		return "TesseractForm [bpmRequestId=" + bpmRequestId + ", countryCode=" + countryCode + ", processCode="
				+ processCode + ", fileName=" + fileName + ", filePath=" + filePath 
				+ ", executionFlag=" + executionFlag + ", executionStatus=" + executionStatus
				+ ", tesseractServiceReference=" + tesseractServiceReference + ", formJsonOutput=" + formJsonOutput
				+ ", createdTime=" + createdTime + ", updatedTime=" + updatedTime + "]";
	}

	public long getBpmRequestId() {
		return bpmRequestId;
	}

	public void setBpmRequestId(long bpmRequestId) {
		this.bpmRequestId = bpmRequestId;
	}

	public String getCountryCode() {
		return countryCode;
	}

	public void setCountryCode(String countryCode) {
		this.countryCode = countryCode;
	}

	public String getProcessCode() {
		return processCode;
	}

	public void setProcessCode(String processCode) {
		this.processCode = processCode;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public String getFilePath() {
		return filePath;
	}

	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}
	
	public String getExecutionFlag() {
		return executionFlag;
	}

	public void setExecutionFlag(String executionFlag) {
		this.executionFlag = executionFlag;
	}

	public String getExecutionStatus() {
		return executionStatus;
	}

	public void setExecutionStatus(String executionStatus) {
		this.executionStatus = executionStatus;
	}

	public String getTesseractServiceReference() {
		return tesseractServiceReference;
	}

	public void setTesseractServiceReference(String tesseractServiceReference) {
		this.tesseractServiceReference = tesseractServiceReference;
	}

	public String getFormJsonOutput() {
		return formJsonOutput;
	}

	public void setFormJsonOutput(String formJsonOutput) {
		this.formJsonOutput = formJsonOutput;
	}

	public Date getCreatedTime() {
		return createdTime;
	}

	public void setCreatedTime(Date createdTime) {
		this.createdTime = createdTime;
	}

	public Date getUpdatedTime() {
		return updatedTime;
	}

	public void setUpdatedTime(Date updatedTime) {
		this.updatedTime = updatedTime;
	}

}
