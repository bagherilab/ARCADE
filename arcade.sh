#!/bin/bash

case ${SIMULATION_TYPE} in
	AWS)
		# Set input and output file paths
		INPUT_FILE_PATH="${BATCH_WORKING_URL}inputs/"
		OUTPUT_FILE_PATH="${BATCH_WORKING_URL}outputs/"

		# Get copy of the input file
		INPUT_FILE_NAME="${FILE_SET_NAME}_${AWS_BATCH_JOB_ARRAY_INDEX:-0}.xml"
		aws s3 cp $INPUT_FILE_PATH$INPUT_FILE_NAME input/input.xml

		# Extract bucket name
		BUCKET_NAME="${BATCH_WORKING_URL/'s3://'/""}"
		BUCKET_NAME=(${BUCKET_NAME//'/'/ })
		BUCKET_NAME=${BUCKET_NAME[0]}

		# Get copy of cell file (if it exists)
		INPUT_FILE_CELLS_NAME="${FILE_SET_NAME}_${AWS_BATCH_JOB_ARRAY_INDEX:-0}.CELLS.json"
		INPUT_FILE_CELLS_KEY=$INPUT_FILE_PATH$INPUT_FILE_CELLS_NAME
		INPUT_FILE_CELLS_KEY="${INPUT_FILE_CELLS_KEY/'s3://'${BUCKET_NAME}'/'/""}"
		CELL_FILE_EXISTS=$(aws s3api head-object --bucket $BUCKET_NAME --key $INPUT_FILE_CELLS_KEY > /dev/null 2>&1; echo $?)
		if [ "$CELL_FILE_EXISTS" = 0 ]; then
		    LOAD_CELLS=true]
		    aws s3 cp $INPUT_FILE_PATH$INPUT_FILE_CELLS_NAME input/input.CELLS.json
		fi

		# Get copy of location file (if it exists)
		INPUT_FILE_LOCATIONS_NAME="${FILE_SET_NAME}_${AWS_BATCH_JOB_ARRAY_INDEX:-0}.LOCATIONS.json"
		INPUT_FILE_LOCATIONS_KEY=$INPUT_FILE_PATH$INPUT_FILE_LOCATIONS_NAME
		INPUT_FILE_LOCATIONS_KEY="${INPUT_FILE_LOCATIONS_KEY/'s3://'${BUCKET_NAME}'/'/""}"
		LOCATION_FILE_EXISTS=$(aws s3api head-object --bucket $BUCKET_NAME --key $INPUT_FILE_LOCATIONS_KEY > /dev/null 2>&1; echo $?)
		if [ "$LOCATION_FILE_EXISTS" = 0 ]; then
		    LOAD_LOCATIONS=true]
		    aws s3 cp $INPUT_FILE_PATH$INPUT_FILE_LOCATIONS_NAME input/input.LOCATIONS.json
		fi
	;;
	LOCAL)
		# Set input and output file paths
		INPUT_FILE_PATH="/mnt/inputs/"
		OUTPUT_FILE_PATH="/mnt/outputs/"

		# Get copy of the input file
		INPUT_FILE_NAME="${FILE_SET_NAME}_${JOB_ARRAY_INDEX}.xml"
		cp $INPUT_FILE_PATH$INPUT_FILE_NAME input/input.xml

		# Get copy of cell file (if it exists)
		INPUT_FILE_CELLS_NAME="${FILE_SET_NAME}_${JOB_ARRAY_INDEX}.CELLS.json"
		if [ -f "$INPUT_FILE_PATH$INPUT_FILE_CELLS_NAME" ]; then
			LOAD_CELLS=true
			cp $INPUT_FILE_PATH$INPUT_FILE_CELLS_NAME input/input.CELLS.json
		fi

		# Get copy of location file (if it exists)
		INPUT_FILE_LOCATIONS_NAME="${FILE_SET_NAME}_${JOB_ARRAY_INDEX}.LOCATIONS.json"
		if [ -f "$INPUT_FILE_PATH$INPUT_FILE_LOCATIONS_NAME" ]; then
			LOAD_LOCATIONS=true
			cp $INPUT_FILE_PATH$INPUT_FILE_LOCATIONS_NAME input/input.LOCATIONS.json
		fi
	;;
esac

# Set load command flags.
LOAD_COMMAND=""
if [ "$LOAD_CELLS" = true ] || [ "$LOAD_LOCATIONS" = true ]; then
    LOAD_COMMAND+="--loadpath input/input"
fi
if [ "$LOAD_CELLS" = true ]; then
    LOAD_COMMAND+=" --cells"
fi
if [ "$LOAD_LOCATIONS" = true ]; then
    LOAD_COMMAND+=" --locations"
fi

# Run the jar
java -jar arcade-3.0.jar potts input/input.xml output/ $LOAD_COMMAND
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
