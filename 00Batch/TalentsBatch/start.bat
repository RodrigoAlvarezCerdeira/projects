set BDH_LOG=C://log
java -jar target/bdh-talents-0.0.1-SNAPSHOT.jar  --spring.config.location=src\main\resources\configuration.yml --spring.batch.job.names=cashin
set exitcode=%ERRORLEVEL%
echo %exitcode%