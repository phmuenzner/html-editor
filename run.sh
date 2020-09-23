#!/bin/bash

timestamp=$(date +"%Y%m%d%H%M%S")

testeclipse=../../eclipse-jee-2020-06-R-win32-x86_64-test/eclipse

# mvn clean package
rm -r $testeclipse/dropins/*

cp onlinehilfe-eclipse-plugin/target/onlinehilfe-eclipse-plugin-1.0.0.jar $testeclipse/dropins/onlinehilfe-eclipse-plugin-1.0.0.$timestamp.jar

$testeclipse/eclipse.exe -clean -console -consoleLog 
