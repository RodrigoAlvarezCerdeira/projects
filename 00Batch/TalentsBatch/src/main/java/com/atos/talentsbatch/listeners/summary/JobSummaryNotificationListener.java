package com.atos.talentsbatch.listeners.summary;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.listener.JobExecutionListenerSupport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;

import com.atos.talentsbatch.commons.SettlementConfig;
import com.atos.talentsbatch.commons.SettlementConfig.FileOut;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Data
@Slf4j
public class JobSummaryNotificationListener extends JobExecutionListenerSupport {

    @Autowired
    private SettlementConfig config;

    private List<String> filesList = new ArrayList<>();

    private String sHeaders = null;

    private int totalLines = 0;

    @Override
    public void beforeJob(JobExecution jobExecution) {
        for (FileOut file : config.getFileOutput()) {
            File fileSys = new FileSystemResource(file.getPath() + file.getName() + "." + file.getType()).getFile();
            if (fileSys.exists() && !fileSys.delete()) {
                log.error("Error to delete initil files");
            }
        }
    }

    @Override
    public void afterJob(JobExecution jobExecution) {

        log.info("JOB FINISHED WHIT STATUS :" + jobExecution.getStatus());
        log.info("JOB START TIME :" + jobExecution.getStartTime());
        log.info("JOB PARAMETERS :" + jobExecution.getJobParameters());

        if (jobExecution.getStatus() == BatchStatus.COMPLETED) {
            jobExecution.getJobParameters().getParameters().forEach((k, v) -> log.error("Item : " + k + " Count : " + v));
            stepExecutionsLog(jobExecution);
            fileOutputLog();
        }

        log.info("JOB END TIME :" + jobExecution.getEndTime());
    }

    private void stepExecutionsLog(JobExecution jobExecution) {
        jobExecution.getStepExecutions().forEach(arg0 -> {

            int iRead = arg0.getReadCount();
            int iWrite = arg0.getWriteCount();
            int iSkipCount = arg0.getSkipCount();
            log.debug("Register Skip..:" + iSkipCount);
            if (iRead <= iSkipCount) {
                jobExecution.setStatus(BatchStatus.FAILED);
                log.error("All records are error.");
            } else if (iRead > 0 && iWrite == 0) {
                jobExecution.setStatus(BatchStatus.FAILED);
            }

            if (arg0.toString().contains("readCount"))
                log.info("Register reader..:" + iRead);

        });

    }

    private void fileOutputLog() {

        config.getFileOutput().forEach(file -> {

            if (!filesList.contains(file.getName())) {

                int iLines = -1;
                try {
                    FileReader fr = new FileReader(file.getPath() + file.getName() + "." + file.getType());
                    BufferedReader bf = new BufferedReader(fr);

                    String sLine = "";
                    while ((sLine = bf.readLine()) != null) {
                        iLines++;
                    }

                    log.info("CREATE FILES..:" + file.getName() + " Registers..:" + iLines);
                    if (iLines > 0)
                        totalLines = totalLines + iLines;

                } catch (FileNotFoundException fnfe) {
                    createEmptyFileIfException(file, fnfe);
                } catch (IOException ioe) {
                    log.error("IOException ", ioe);
                }

                filesList.add(file.getName());
            }
        });

        log.info("Total Register writer..:" + totalLines);
    }

    private void createEmptyFileIfException(FileOut file, Exception e) {
        File fileSys = new FileSystemResource(file.getPath() + file.getName() + "." + file.getType()).getFile();
        try {
            if (fileSys.createNewFile()) {
                FileWriter wr = new FileWriter(fileSys);
                wr.write(getSHeaders());
                wr.close();
            } else {
                log.error("File not found ", e);
            }

        } catch (IOException ie) {
            log.error("IOException ", ie);
        }
    }
}
