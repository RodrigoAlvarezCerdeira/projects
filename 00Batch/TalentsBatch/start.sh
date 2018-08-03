#!/bin/bash

export BDH_LOG=/userdev/talent/log

/opt/jdk1.8.0_77/bin/java -jar /userdev/talent/j2ee/bdh-talents-0.0.1.jar  --spring.config.location=/usersdev/talent/j2ee/configuration.yml --spring.batch.job.names=cashin 

STATUS=$?
echo $STATUS