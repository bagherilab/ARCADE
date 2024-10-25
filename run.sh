#!/bin/bash
new_jar=$(ls *.jar)

if [ -z "$new_jar" ]; then
    echo "No JAR file found. Build may have failed."
    exit 1
fi

java -jar "$new_jar" patch input/chemo_test.xml output
