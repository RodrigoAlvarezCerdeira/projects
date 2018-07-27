package com.atos.talentsbatch.commons;

import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Data;


@ConfigurationProperties(prefix = "settlement")
@Data
public class SettlementConfig {
	
	String fileInput;
	String delimiter;
	String[] colunms;
	List<FileOut> fileOutput = new ArrayList<>();
	
	@Data	
	public static class FileOut {
		String application;
		String path;
		String name;
		String type;
	}
}
