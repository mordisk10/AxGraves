#!/usr/bin/env bash

if [ -x ./mvnw ]; then
  ./mvnw -s settings.xml -U -DskipTests package
else
  mvn -s settings.xml -U -DskipTests package
fi