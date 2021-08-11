#!/usr/bin/env bash

case "$1" in
    spark)
      $mvn_home -P sql,sparkbench,websearch,micro,streaming,spark3.1.1.0,ml,structuredStreaming,graph,scala2.12,!defaultScalaVersion,!allModules clean install
      shift 1;;
esac
