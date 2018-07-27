package com.atos.talentsbatch.jobs;

import org.springframework.batch.item.ItemProcessor;

import com.atos.talentsbatch.beans.SettlementBean;
import com.atos.talentsbatch.commons.DiscardOperationsEnum;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SettlementItemProcessor implements ItemProcessor<SettlementBean, SettlementBean> {

	@Override
	public SettlementBean process(SettlementBean item) throws Exception {
		if(DiscardOperationsEnum.findOperation(item.getOperation())!=null) return null; 
		log.info("Process SettlementBean :" + item);
		return item;
	}

}
