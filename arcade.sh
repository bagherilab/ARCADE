#!/bin/bash

case ${SIMULATION_TYPE} in
	AWS)
		# Set input and output file paths
		INPUT_FILE_PATH="${BATCH_WORKING_URL}inputs/"
		OUTPUT_FILE_PATH="${BATCH_WORKING_URL}outputs/"

		# Get copy of the input file
		INPUT_FILE_NAME="${FILE_SET_NAME}_${AWS_BATCH_JOB_ARRAY_INDEX:-0}.xml"
		aws s3 cp $INPUT_FILE_PATH$INPUT_FILE_NAME input/input.xml
	;;
	LOCAL)
		# Set input and output file paths
		INPUT_FILE_PATH="/mnt/inputs/"
		OUTPUT_FILE_PATH="/mnt/outputs/"

		# Get copy of the input file
		INPUT_FILE_NAME="${FILE_SET_NAME}_${JOB_ARRAY_INDEX}.xml"
		cp $INPUT_FILE_PATH$INPUT_FILE_NAME input/input.xml
	;;
esac

# Run the jar
java -jar arcade.jar potts input/input.xml output/
EXIT_CODE=$?

# Save output files
if [ $EXIT_CODE -eq 0 ]
then
	cd output/

	FILE_REGEX='([A-Za-z0-9\_]+_[0-9]{4})_([0-9]{6})\.([A-Za-z]+)\.json'

	PREFIXES=()
	SUFFIXES=()

	for f in *.json; do
		if [[ $f =~ $FILE_REGEX ]]; then
			PREFIXES+=(${BASH_REMATCH[1]})
			SUFFIXES+=(${BASH_REMATCH[3]})
		fi
	done

	UNIQUE_PREFIXES=($(printf "%s\n" "${PREFIXES[@]}" | sort -u | tr '\n' ' '))
	UNIQUE_SUFFIXES=($(printf "%s\n" "${SUFFIXES[@]}" | sort -u | tr '\n' ' '))

	for prefix in "${UNIQUE_PREFIXES[@]}"; do
		for suffix in "${UNIQUE_SUFFIXES[@]}"; do
			echo $prefix $suffix
			mkdir $prefix.$suffix
			mv $prefix*.$suffix.json $prefix.$suffix
			cd $prefix.$suffix
			COPYFILE_DISABLE=1 tar cJf ../$prefix.$suffix.tar.xz *.json
			cd ..
		done
	done

	case ${SIMULATION_TYPE} in
		AWS)
			aws s3 cp . $OUTPUT_FILE_PATH --recursive --exclude "*" --include "*.tar.xz"
			aws s3 cp . $OUTPUT_FILE_PATH --recursive --exclude "*" --include "*.json" --exclude "*/*"
		;;
		LOCAL)
			cp *.tar.xz $OUTPUT_FILE_PATH
			cp *.json $OUTPUT_FILE_PATH
		;;
	esac
fi
