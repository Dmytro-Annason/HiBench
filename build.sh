#!/usr/bin/env bash

case "$1" in
    spark)
      mvn -P sql,sparkbench,websearch,micro,streaming,spark2.4.4,ml,structuredStreaming,graph,scala2.11,!defaultScalaVersion,!allModules clean install
      shift 1;;
esac
