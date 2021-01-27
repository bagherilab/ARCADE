#!/bin/bash

# Set input and output file paths
INPUT_FILE_PATH="${BATCH_WORKING_URL}inputs/"
OUTPUT_FILE_PATH="${BATCH_WORKING_URL}output/"

# Get copy of the input file
INPUT_FILE_NAME="input_${BATCH_FILE_SET_NAME}_${AWS_BATCH_JOB_ARRAY_INDEX:-0}.xml"
aws s3 cp $INPUT_FILE_PATH$INPUT_FILE_NAME input/input.xml

# Run the jar
java -jar arcade.jar potts input/input.xml output/
EXIT_CODE=$?

# Save output files
if [ $EXIT_CODE -eq 0 ]
then
	cd output/
	aws s3 cp . $OUTPUT_FILE_PATH --recursive --exclude "*" --include "*.json"
fi

exit $EXIT_CODE
