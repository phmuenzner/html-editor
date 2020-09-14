#!/bin/bash

timestamp=$(date +"%Y%m%d%H%M%S")

mvn clean package
rm -r ../eclipse-committers-2020-06-OH-Test/eclipse/dropins/*

cp onlinehilfe-eclipse-plugin/target/onlinehilfe-eclipse-plugin-1.0.0.jar ../eclipse-committers-2020-06-OH-Test/eclipse/dropins/onlinehilfe-eclipse-plugin-1.0.0.$timestamp.jar

# ziptool -n ../eclipse-committers-2020-06-OH-Test/eclipse/dropins/onlinehilfe-plugin-1.0.0.$timestamp.zip add_dir features add_dir plugins
# ziptool -n ../eclipse-committers-2020-06-OH-Test/eclipse/dropins/onlinehilfe-plugin-1.0.0.$timestamp.zip add_file features/onlinehilfe-eclipse-feature-1.0.0.jar onlinehilfe-eclipse-feature/target/onlinehilfe-eclipse-feature-1.0.0.jar 0 0
# ziptool -n ../eclipse-committers-2020-06-OH-Test/eclipse/dropins/onlinehilfe-plugin-1.0.0.$timestamp.zip add_file plugins/onlinehilfe-eclipse-plugin-1.0.0.jar onlinehilfe-eclipse-plugin/target/onlinehilfe-eclipse-plugin-1.0.0.jar 0 0

../eclipse-committers-2020-06-OH-Test/eclipse/eclipse.exe -consoleLog
