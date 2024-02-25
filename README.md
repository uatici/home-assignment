# Home Assignment

## App Configurations 
* IGNORE_CELL : set the value for ignore cell level defect; default value is true
* IGNORE_LINE : set the value for ignore line level defect; default value is true 
* IGNORE_FILE : set the value for ignore file level defect; default value is true 

## Run app with default configurations
```
mvn spring-boot:run
```

## Run app with custom configurations
#### LINUX/MAC COMMAND PROMPT
```
export IGNORE_FILE=false && export IGNORE_CELL=false && export IGNORE_LINE=false && mvn spring-boot:run
```

#### WINDOWS COMMAND PROMPT
```
set IGNORE_FILE=false && set IGNORE_CELL=false && set IGNORE_LINE=false && mvn spring-boot:run
```

#### WINDOWS POWERSHELL COMMAND PROMPT
```
$env:IGNORE_FILE = false; $env:IGNORE_CELL = false; $env:IGNORE_LINE = false; mvn spring-boot:run
```

### Test Scenario
For test purposes, files under the resources/static folder can be used. Currently two example files exists(data.csv, data2.csv). 

These files can be reached http://localhost:8080/static/data.csv, http://localhost:8080/static/data2.csv. 

Application can be tested with these files using below command.
```
curl http://localhost:8080/evaluation?url1=http://localhost:8080/static/data.csv&url2=http://localhost:8080/static/data2.csv
```

