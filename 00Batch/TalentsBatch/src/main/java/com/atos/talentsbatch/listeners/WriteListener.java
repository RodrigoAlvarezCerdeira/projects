package com.atos.talentsbatch.listeners;

import java.util.HashMap;
import java.util.List;

import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.ItemWriteListener;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class WriteListener<S> implements ItemWriteListener<S> {
	
		
	@Override
	public void afterWrite(List<? extends S> items) {
		log.info("AfterWrite SettlementBean :" + items);	
	}

	@Override
	public void beforeWrite(List<? extends S> items) {
			
	}

	@Override
	public void onWriteError(Exception exception, List<? extends S> items) {
			
	}

	

}
