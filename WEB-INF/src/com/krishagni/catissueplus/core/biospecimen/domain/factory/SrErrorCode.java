package com.krishagni.catissueplus.core.biospecimen.domain.factory;

import com.krishagni.catissueplus.core.common.errors.ErrorCode;

public enum SrErrorCode implements ErrorCode {
	NOT_FOUND,
	
	DUP_CODE,
	
	PARENT_NOT_FOUND,
	
	PARENT_REQ_REQUIRED,
	
	INVALID_ALIQUOT_CNT,
	
	INVALID_QTY,
	
	INSUFFICIENT_QTY,
	
	SPECIMEN_CLASS_REQUIRED,
	
	INVALID_SPECIMEN_CLASS,
	
	SPECIMEN_TYPE_REQUIRED,
	
	INVALID_SPECIMEN_TYPE,
	
	ANATOMIC_SITE_REQUIRED,
	
	INVALID_ANATOMIC_SITE,
	
	LATERALITY_REQUIRED,
	
	INVALID_LATERALITY,
	
	PATHOLOGY_STATUS_REQUIRED,
	
	INVALID_PATHOLOGY_STATUS,
	
	STORAGE_TYPE_REQUIRED,
	
	CONCENTRATION_MUST_BE_POSITIVE,
	
	COLL_PROC_REQUIRED,
	
	INVALID_COLL_PROC,
	
	COLL_CONT_REQUIRED,
	
	INVALID_COLL_CONT,
	
	COLLECTOR_NOT_FOUND,
	
	RECEIVER_NOT_FOUND,
	
	INVALID_LABEL_FMT,
	
	CPE_REQUIRED,
	
	CANNOT_CHANGE_CLASS_OR_TYPE,
	
	POOLED_SPMN_REQ,
	
	POOLED_SPMN_REQ_NOT_FOUND,
	
	INVALID_POOLED_SPMN,
	
	INVALID_LINEAGE,

	CLOSED;
	
	@Override
	public String code() {
		return "SR_" + this.name();
	}

}
