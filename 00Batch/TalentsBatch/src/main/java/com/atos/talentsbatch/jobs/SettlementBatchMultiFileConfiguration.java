package com.atos.talentsbatch.jobs;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.step.builder.FaultTolerantStepBuilder;
import org.springframework.batch.core.step.skip.SkipPolicy;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.transform.BeanWrapperFieldExtractor;
import org.springframework.batch.item.file.transform.DelimitedLineAggregator;
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
import com.atos.talentsbatch.listeners.summary.JobSummaryNotificationListener;
import com.atos.talentsbatch.writer.MyClassifierCompositeItemWriter;
import com.atos.talentsbatch.writer.MyFileItemWriter;
import com.atos.talentsbatch.writer.StringHeaderWriter;

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

    public Map<String, String> hasMap = new HashMap<>();

    private String sHeaders = null;

    @Autowired
    private SettlementConfig config;

    @Bean
    public FlatFileItemReader<SettlementBean> reader() throws MalformedURLException {

        try (FileReader fr = new FileReader(new UrlResource(config.getFileInput()).getFile()); BufferedReader bf = new BufferedReader(fr);) {
            sHeaders = bf.readLine();
        } catch (IOException e) {
            log.error("IOException ", e);
        }

        BeanWrapperFieldSetMapper<SettlementBean> beenMapper = new BeanWrapperFieldSetMapper<>();
        beenMapper.setTargetType(SettlementBean.class);

        return new FlatFileItemReaderBuilder<SettlementBean>().name("settlementItemReader").resource(new UrlResource(config.getFileInput()))
                .delimited().delimiter(config.getDelimiter()).names(config.getColunms()).fieldSetMapper(beenMapper).build();

    }

    @Bean
    public SettlementItemProcessor processor() {
        return new SettlementItemProcessor();
    }

    /**
     * handing exception
     * 
     * @return
     */
    @Bean
    public SkipPolicy fileVerificationSkipper() {
        return new FileVerificationSkipper();
    }

    @Bean
    public JobSummaryNotificationListener jobSummaryNotificationListener() {
        return new JobSummaryNotificationListener();
    }

    public MyFileItemWriter<SettlementBean> writerFileOutput(String fileName) throws MalformedURLException {
        MyFileItemWriter<SettlementBean> writer = new MyFileItemWriter<>();
        StringHeaderWriter headerWriter = null;
        if (sHeaders == null) {
            String exportFileHeader = String.join(",", config.getColunms());
            headerWriter = new StringHeaderWriter(exportFileHeader);

        } else {
            headerWriter = new StringHeaderWriter(sHeaders);

        }
        writer.setHeaderCallback(headerWriter);
        // new file name
        writer.setResource(new FileSystemResource(fileName));

        DelimitedLineAggregator<SettlementBean> delLineAgg = new DelimitedLineAggregator<>();
        delLineAgg.setDelimiter(config.getDelimiter());

        BeanWrapperFieldExtractor<SettlementBean> fieldExtractor = new BeanWrapperFieldExtractor<>();
        fieldExtractor.setNames(config.getColunms());

        delLineAgg.setFieldExtractor(fieldExtractor);

        writer.setLineAggregator(delLineAgg);

        return writer;
    }

    @Bean
    @StepScope
    public MyClassifierCompositeItemWriter<SettlementBean> writer() throws MalformedURLException {

        BackToBackPatternClassifier classifier = new BackToBackPatternClassifier();
        classifier.setRouterDelegate(new SettlementRouterClassifier());

        HashMap<String, MyFileItemWriter<SettlementBean>> map = new HashMap<>();

        for (FileOut file : config.getFileOutput()) {
            if (file.getApplication().contains(config.getDelimiter())) {
                for (String sApp : file.getApplication().split(config.getDelimiter()))
                    map.put(sApp, writerFileOutput(file.getPath() + file.getName() + "." + file.getType()));

            } else
                map.put(file.getApplication(), writerFileOutput(file.getPath() + file.getName() + "." + file.getType()));
        }

        jobSummaryNotificationListener().setSHeaders(sHeaders);
        classifier.setMatcherMap(map);

        MyClassifierCompositeItemWriter<SettlementBean> writer = new MyClassifierCompositeItemWriter<>();
        writer.setClassifier(classifier);
        return writer;

    }

    // tag::jobstep[]
    @Bean
    public Job cashin(Step step1) {
        return jobBuilderFactory.get("cashin").incrementer(new RunIdIncrementer()).listener(jobSummaryNotificationListener()).flow(step1).end()
                .build();
    }

    @Bean
    public Step step1() throws MalformedURLException {
        return ((FaultTolerantStepBuilder<SettlementBean, SettlementBean>) stepBuilderFactory.get("step1").<SettlementBean, SettlementBean>chunk(1000)
                .reader(reader()).faultTolerant().skipPolicy(fileVerificationSkipper()).processor(processor()).writer(writer()))
                        .skip(IllegalStateException.class).noRetry(IllegalStateException.class).noRollback(IllegalStateException.class)
                        .skipLimit(10000)
                        // .listener(skipListener())
                        // .listener(documentPackageReadyForProcessingListener())
                        // .listener(jobSummaryNotificationListener())
                        .build();
    }
    // end::jobstep[]

}
