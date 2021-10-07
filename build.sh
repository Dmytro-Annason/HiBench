#!/usr/bin/env bash

case "$1" in
    spark)
      /home/b/work/projects/HiBench/apache-maven-3.3.9/bin/mvn -P sql,sparkbench,websearch,micro,streaming,spark2.4.5.0,ml,structuredStreaming,graph,scala2.12,!defaultScalaVersion,!allModules clean install
      shift 1;;
esac
