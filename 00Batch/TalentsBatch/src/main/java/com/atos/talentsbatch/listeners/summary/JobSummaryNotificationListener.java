package com.atos.talentsbatch.listeners.summary;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.listener.JobExecutionListenerSupport;

import com.atos.talentsbatch.commons.SettlementConfig.FileOut;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Data
@Slf4j
public class JobSummaryNotificationListener extends JobExecutionListenerSupport{
	
	
	private List<String> filesList = new ArrayList<>();
	
	private List<FileOut> filesOutput = new ArrayList<>();
	
	@Override
	public void afterJob(JobExecution jobExecution) {
		log.info("JOB FINISHED WHIT STATUS :" + jobExecution.getStatus());
		log.info("JOB START TIME :" + jobExecution.getStartTime());
		log.info("JOB PARAMETERS :" + jobExecution.getJobParameters());
		
		if(jobExecution.getStatus() == BatchStatus.COMPLETED) {
			jobExecution.getStepExecutions().forEach(arg0 -> { 	
	
				if(arg0.toString().contains("readCount"))log.info("Register reader..:"+arg0.getReadCount()); 
				if(arg0.toString().contains("writeCount"))log.info("Register writer..:"+arg0.getWriteCount());
			});
				
			filesOutput.forEach(file -> { 
				if(!filesList.contains(file.getName())){
					long lLines = 0;
					try{
						FileReader fr = new FileReader(file.getPath()+file.getName()+"."+file.getType());
						BufferedReader bf = new BufferedReader(fr);
						
						String sLine="";
						while ((sLine = bf.readLine())!=null) {
							lLines++;
						}
						
					} catch (FileNotFoundException fnfe){
						    log.error(fnfe.getMessage());
					} catch (IOException ioe){
							log.error(ioe.getMessage());
					}
					log.info("CREATE FILES..:"+file.getName()+" Registers..:"+lLines);
					filesList.add(file.getName());
				}
			});
			
		}
		log.info("JOB END TIME :" + jobExecution.getEndTime());
	}
}
