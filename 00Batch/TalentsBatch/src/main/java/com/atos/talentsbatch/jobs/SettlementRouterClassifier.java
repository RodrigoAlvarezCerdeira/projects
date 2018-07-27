package com.atos.talentsbatch.jobs;

import org.springframework.batch.support.annotation.Classifier;

import com.atos.talentsbatch.beans.SettlementBean;

public class SettlementRouterClassifier {

	@Classifier
	public String classify(SettlementBean classifiable) {
		return classifiable.getAccount();
	}
}
