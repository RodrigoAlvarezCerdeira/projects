package com.atos.talentsbatch.exceptions;

import java.io.FileNotFoundException;

import org.springframework.batch.core.step.skip.SkipLimitExceededException;
import org.springframework.batch.core.step.skip.SkipPolicy;
import org.springframework.batch.item.file.FlatFileParseException;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;
@Slf4j
@Component
public class FileVerificationSkipper implements SkipPolicy {

	
	@Override
	public boolean shouldSkip(Throwable t, int skipCount) throws SkipLimitExceededException {
		
		if(t instanceof FileNotFoundException){
			log.error(":::::::The file not found::::::");
			log.error(t.getMessage());
			return false;
		} else  if ( t instanceof FlatFileParseException) {

            FlatFileParseException ffpe = (FlatFileParseException) t;
            StringBuilder errorMessage = new StringBuilder();
            errorMessage.append("An error occured while processing the " + ffpe.getLineNumber()
                    + " line of the file. Below was the faulty " + "input.\n");
            errorMessage.append(ffpe.getInput() + "\n");
            log.error("{}", errorMessage.toString());
            log.error( t.getMessage());
            return true;
        } else {
            return false;
        }

	}

}
