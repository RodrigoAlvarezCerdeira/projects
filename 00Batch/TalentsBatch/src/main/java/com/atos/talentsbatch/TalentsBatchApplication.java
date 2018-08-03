package com.atos.talentsbatch;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ConfigurableApplicationContext;

import com.atos.talentsbatch.commons.SettlementConfig;

import lombok.extern.slf4j.Slf4j;

@SpringBootApplication
@EnableAutoConfiguration(exclude = { DataSourceAutoConfiguration.class, DataSourceTransactionManagerAutoConfiguration.class,
        HibernateJpaAutoConfiguration.class })
@EnableConfigurationProperties(SettlementConfig.class)
@Slf4j
public class TalentsBatchApplication {
    public static void main(String[] args) {

        log.debug("--Application Started--");

        ConfigurableApplicationContext context = SpringApplication.run(TalentsBatchApplication.class, args);

        int exitCode = SpringApplication.exit(context);

        System.exit(exitCode);
    }

}
