# coverageAnalyser

this tool is analysed percentage of disabled JDBC requests in JMeter file. calculated value will be displayed in output.

## build:

`mvn clean install`

## usage:

`java -jar coverage-analyser-1.0-SNAPSHOT-jar-with-dependencies.jar -j pathToJmxFile`

## to scan all *.jmx files in directory:

`java -jar coverage-analyser-1.0-SNAPSHOT-jar-with-dependencies.jar -j pathToJmxDir`
