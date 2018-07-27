package com.atos.talentsbatch.beans;

import lombok.Data;

@Data
public class SettlementBean {
	
	 private String account;
	 private String	accountName;
	 private String	date;
	 private String	dateValue;
	 private String	invoiceReference;
	 private String	settlementId;
	 private String	transactionId;
	 private String	order;
	 private String	productName;
	 private String invoiced;
	 private String settled;
	 private String	originalAmount;
	 private String	originalCurrency;
	 private String	amountExclTax;// (Excl. Tax)
	 private String	taxAmount;
	 private String	amountInclTax; //(Incl. Tax)
	 private String	currency;
	 private String	operation;
	 private String	settledAmount;
	 private String	settlementCurrency;
	 private String originalSettlementId;
	 private String	customerId;
	 private String	merchantOperationId;
	 private String	reportingData1;
	 private String	reportingData2;
	 private String	reportingData3;
	 private String	reportingData4;
	 private String	reportingData5;
	 private String collectMode;
	 private String transfer;
	 private String operationDate;

}
