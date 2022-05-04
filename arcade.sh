#!/bin/bash

case ${SIMULATION_TYPE} in
	AWS)
		# Set input and output file paths
		INPUT_FILE_PATH="${BATCH_WORKING_URL}inputs/"
		OUTPUT_FILE_PATH="${BATCH_WORKING_URL}outputs/"
		INIT_FILE_PATH="${BATCH_WORKING_URL}inits/"

		# Get copy of the input file
		INPUT_FILE_NAME="${FILE_SET_NAME}_${AWS_BATCH_JOB_ARRAY_INDEX:-0}.xml"
		aws s3 cp $INPUT_FILE_PATH$INPUT_FILE_NAME input/input.xml

		# Get copies of .CELL initialization files (if they exist)
		INIT_CELL_FILES=$(aws s3 sync ${INIT_FILE_PATH} init/ --exclude="*" --include="*.CELLS.json" --dryrun)
		if test -n "$INIT_CELL_FILES"; then
			LOAD_CELLS=true
			aws s3 cp $INIT_FILE_PATH init/ --recursive --exclude="*" --include="*.CELLS.json"
		fi

		# Get copies of .LOCATION initialization files (if they exist)
		INIT_LOCATION_FILES=$(aws s3 sync ${INIT_FILE_PATH} init/ --exclude="*" --include="*.LOCATIONS.json" --dryrun)
		if test -n "$INIT_LOCATION_FILES"; then
			LOAD_LOCATIONS=true
			aws s3 cp $INIT_FILE_PATH init/ --recursive --exclude="*" --include="*.LOCATIONS.json"
		fi
	;;
	LOCAL)
		# Set input, output, and init file paths
		INPUT_FILE_PATH="/mnt/inputs/"
		OUTPUT_FILE_PATH="/mnt/outputs/"
		INIT_FILE_PATH="/mnt/inits/"

		# Get copy of the input file
		INPUT_FILE_NAME="${FILE_SET_NAME}_${JOB_ARRAY_INDEX}.xml"
		cp $INPUT_FILE_PATH$INPUT_FILE_NAME input/input.xml

		# Get copies of .CELL initialization files (if they exist)
		INIT_CELL_FILES=$(find ${INIT_FILE_PATH} -name '*.CELLS.json' -print -quit)
		if test -n "$INIT_CELL_FILES"; then
			LOAD_CELLS=true
			cp ${INIT_FILE_PATH}*.CELLS.json init/
		fi

		# Get copies of .LOCATION initialization files (if they exist)
		INIT_LOCATION_FILES=$(find ${INIT_FILE_PATH} -name '*.LOCATIONS.json' -print -quit)
		if test -n "$INIT_LOCATION_FILES"; then
			LOAD_LOCATIONS=true
			cp ${INIT_FILE_PATH}*.LOCATIONS.json init/
		fi
	;;
esac

# Set load command flags.
LOAD_COMMAND=""
if [ "$LOAD_CELLS" = true ] || [ "$LOAD_LOCATIONS" = true ]; then
    LOAD_COMMAND+="--loadpath init/${FILE_SET_NAME}_[#]"
fi
if [ "$LOAD_CELLS" = true ]; then
    LOAD_COMMAND+=" --cells"
fi
if [ "$LOAD_LOCATIONS" = true ]; then
    LOAD_COMMAND+=" --locations"
fi

# Run the jar
java -jar arcade.jar potts input/input.xml output/ $LOAD_COMMAND
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
