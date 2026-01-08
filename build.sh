#!/usr/bin/env bash

if [ -x ./mvnw ]; then
  ./mvnw -B package -s settings.xml -U -DskipTests package
else
  mvn -B package -s settings.xml -U -DskipTests package
fi