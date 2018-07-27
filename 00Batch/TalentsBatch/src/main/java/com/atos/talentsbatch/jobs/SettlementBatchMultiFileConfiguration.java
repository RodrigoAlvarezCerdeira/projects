package com.atos.talentsbatch.jobs;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.step.builder.FaultTolerantStepBuilder;
import org.springframework.batch.core.step.skip.SkipPolicy;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.transform.BeanWrapperFieldExtractor;
import org.springframework.batch.item.file.transform.DelimitedLineAggregator;
import org.springframework.batch.item.support.ClassifierCompositeItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.classify.BackToBackPatternClassifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.UrlResource;

import com.atos.talentsbatch.beans.SettlementBean;
import com.atos.talentsbatch.commons.SettlementConfig;
import com.atos.talentsbatch.commons.SettlementConfig.FileOut;
import com.atos.talentsbatch.exceptions.FileVerificationSkipper;
import com.atos.talentsbatch.listeners.WriteListener;
import com.atos.talentsbatch.listeners.summary.JobSummaryNotificationListener;
import com.atos.talentsbatch.writer.MyFileItemWriter;

import lombok.extern.slf4j.Slf4j;

@Configuration
@EnableBatchProcessing
@Slf4j
public class SettlementBatchMultiFileConfiguration {

	 @Autowired
	 public JobBuilderFactory jobBuilderFactory;

	 @Autowired
	 public StepBuilderFactory stepBuilderFactory;
	 
	 public List<String> filesList = new ArrayList<>();
	 
	 public HashMap<String, String> hasMap = new HashMap<>();
	 
	 @Autowired
	 private SettlementConfig config;
	 
	 @Bean
	 public FlatFileItemReader<SettlementBean> reader() throws MalformedURLException {
	        return new FlatFileItemReaderBuilder<SettlementBean>()
	            .name("settlementItemReader")
	            .resource(new UrlResource(config.getFileInput()))
	            .delimited().delimiter(config.getDelimiter())
	            .names(config.getColunms())
	            .fieldSetMapper(new BeanWrapperFieldSetMapper<SettlementBean>() {{
	                setTargetType(SettlementBean.class);
	            }})
	            .build();
	 }
	 
	 @Bean
	 public SettlementItemProcessor processor(){
		 return new SettlementItemProcessor();
	 }
	 /**
	  * handing exception
	  * @return
	  */
	 @Bean
	 public SkipPolicy fileVerificationSkipper() {
	 	return new FileVerificationSkipper();
	 }
	 
	 @Bean
	 public JobSummaryNotificationListener jobSummaryNotificationListener(){
		 return new JobSummaryNotificationListener();
	 }
	 
	 @Bean
	 public WriteListener<SettlementBean> writeListener(){
		  return new WriteListener<SettlementBean>();
	 }
	 
	
	
	public MyFileItemWriter<SettlementBean> writerFileOutput(String fileName) throws MalformedURLException{
		 MyFileItemWriter<SettlementBean> writer = new MyFileItemWriter<SettlementBean>();
		 
		 // new file name 
		 writer.setResource(new FileSystemResource(fileName));
		 
		 DelimitedLineAggregator<SettlementBean> delLineAgg = new DelimitedLineAggregator<SettlementBean>();
		 delLineAgg.setDelimiter(config.getDelimiter());
		 
		 BeanWrapperFieldExtractor<SettlementBean> fieldExtractor = new BeanWrapperFieldExtractor<SettlementBean>();
		 fieldExtractor.setNames(config.getColunms());
		 
		 delLineAgg.setFieldExtractor(fieldExtractor);
		 
			 
		 writer.setLineAggregator(delLineAgg);
	     
		 return writer;
	 }
	 
	 
	 @Bean
	 @StepScope
	 public ClassifierCompositeItemWriter<SettlementBean> writer() throws MalformedURLException {

	        
	        BackToBackPatternClassifier classifier = new BackToBackPatternClassifier();
	        classifier.setRouterDelegate(new SettlementRouterClassifier());

	        HashMap<String, MyFileItemWriter<SettlementBean>> map = new HashMap<>();
	       
	        for (FileOut file : config.getFileOutput()) {
	        	
	        	//if(!filesList.contains(file.getName()))	filesList.add(file.getName());
	        	map.put(file.getApplication(), writerFileOutput(file.getPath()+file.getName()+"."+file.getType()));
	        }
	        //jobSummaryNotificationListener().setFilesList(filesList);
	        
	        jobSummaryNotificationListener().setFilesOutput( config.getFileOutput() );
	        classifier.setMatcherMap(map);
	        
	        ClassifierCompositeItemWriter<SettlementBean> writer = new ClassifierCompositeItemWriter<>();
	        writer.setClassifier(classifier);
	        return writer;

	 }

	 
	 
	 
	// tag::jobstep[]
	    @Bean
	    public Job cashin(Step step1) {
	        return jobBuilderFactory.get("cashin")
	            .incrementer(new RunIdIncrementer())
	            .listener(jobSummaryNotificationListener())
	            .flow(step1)
	            .end()
	            .build();
	    }

	    @Bean
	    public Step step1() throws MalformedURLException {
	        return ((FaultTolerantStepBuilder<SettlementBean, SettlementBean>) stepBuilderFactory.get("step1")
	            .<SettlementBean, SettlementBean> chunk(1000)
	            .reader(reader()).faultTolerant().skipPolicy(fileVerificationSkipper())
	            .processor(processor())
	            .writer(writer()))
	        	.skip(IllegalStateException.class)
                .noRetry(IllegalStateException.class)
                .noRollback(IllegalStateException.class)
                .skipLimit(10000)
                .listener(writeListener())
             //   .listener(skipListener())
             //   .listener(documentPackageReadyForProcessingListener())
             //   .listener(jobSummaryNotificationListener())
	            .build();
	    }
	// end::jobstep[]
	 
	 
}
