package com.krishagni.catissueplus.core.biospecimen.domain.factory;

import com.krishagni.catissueplus.core.common.errors.ErrorCode;

public enum CprErrorCode implements ErrorCode {
	NOT_FOUND,

	M_NOT_FOUND,
	
	INVALID_CP_AND_PPID,
	
	INVALID_CPE,
	
	DUP_PPID,
	
	INVALID_PPID,
	
	MANUAL_PPID_NOT_ALLOWED,
	
	DUP_BARCODE,
	
	DUP_REGISTRATION,
	
	REG_DATE_REQUIRED,
	
	CP_REQUIRED,
	
	PPID_REQUIRED,
	
	CONSENT_WITNESS_NOT_FOUND,
	
	PARTICIPANT_DETAIL_REQUIRED,
	
	CONSENT_FORM_NOT_FOUND,
	
	DUP_CONSENT_FORM_NAME,
	
	INVALID_CONSENT_STATEMENT,
	
	INVALID_CONSENT_RESPONSE,
	
	CANNOT_MERGE_PARTICIPANT,

	COLLECTION_SITE_REQUIRED,

	NOT_CP_SITE,

	MUL_REGS_FOR_PMIS,

	CP_CLOSED,

	NO_EMAIL_IDS,

	INVALID_FORM_TYPE;

	@Override
	public String code() {
		return "CPR_" + this.name();
	}
}
