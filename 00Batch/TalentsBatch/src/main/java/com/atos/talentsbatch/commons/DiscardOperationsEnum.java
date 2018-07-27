package com.atos.talentsbatch.commons;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@ToString
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum DiscardOperationsEnum {
	
		FEE 		("Fee"),
		FIXED_FEE 	("Fixed fee"),
		MONTHLY_FEE ("Monthly fee"),
	    SCHEME_FEES ("Scheme fees"),
	    VARIABLE 	("Variable"),
	    INTERCHANGE ("Interchange");

	 @Getter
	 private String description;
	 
	 
	 static private Map<String, DiscardOperationsEnum> valuesMap = Arrays.asList(DiscardOperationsEnum.values())
				.stream().collect(Collectors.toMap(DiscardOperationsEnum::getDescription,Function.identity()));

	static public DiscardOperationsEnum findOperation(String description){
			return valuesMap.get(description);
	}
}
