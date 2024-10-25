#!/bin/bash

./gradlew :spotlessApply
rm -f *.jar
./gradlew build -x test
