#! /bin/bash

SRC_ZIP=flowable-designer-5.22.0.zip
SRC_PLUGIN_NAME=flowable_designer
SRC_PLUGIN_DIR=/Users/osip.lisitsa/Desktop/FTDDevelopment
DST_ZIP=flowable-designer-5.22.1.zip
CHANGE_DIR=changed
SIZE_FILE=sizeChanges.log

NEW_VERSION=_5.22.0
FOUND=
DIR_NAME=

declare -a arr=("org.flowable.designer.eclipse" "org.flowable.designer.feature" "org.flowable.designer.gui"
 "org.flowable.designer.integration" "org.flowable.designer.libs" "org.flowable.designer.parent"
 "org.flowable.designer.updatesite" "org.flowable.designer.util" "org.flowable.designer.validation.bpmn20")


####### HELPER PARAMS #####################
usage() {
    echo ""
    echo "./package.sh"
    echo "\t-h --help"
    echo "\t-s --source=SRC_ZIP default ${SRC_ZIP}"
    echo "\t-d --destination=DST_ZIP default ${DST_ZIP}"
    echo ""
}

findFolder() {
  ## now loop through the above array
  FOUND=
  for i in "${arr[@]}"
    do
      if [ "$DIR_NAME" = "$i" ]; then
        echo "Found $DIR_NAME"
        FOUND=YES
        break
      fi
    done
}

changeOriginalBranch() {
  if [ -s "${DIR_NAME_STRIPPED}/org" ];  then
      echo "Removing ${DIR_NAME_STRIPPED}/org"
      rm -rf "${DIR_NAME_STRIPPED}/org"
      echo "Coping ${DIR_NAME}/target/classes/org to ${DIR_NAME_STRIPPED}"
      cp  -a "${DIR_NAME}/target/classes/org" "${DIR_NAME_STRIPPED}"
  fi
  if [ -s "${DIR_NAME_STRIPPED}/icons" ];  then
      echo "Removing ${DIR_NAME_STRIPPED}/icons"
      rm -rf "${DIR_NAME_STRIPPED}/icons"
      echo "Coping ${DIR_NAME}/icons to ${DIR_NAME_STRIPPED}"
      cp  -a "${DIR_NAME}/icons" "${DIR_NAME_STRIPPED}"
  fi
  if [ -s "${DIR_NAME_STRIPPED}/plugin.xml" ];  then
      echo "Removing ${DIR_NAME_STRIPPED}/plugin.xml"
      rm -f "${DIR_NAME_STRIPPED}/plugin.xml"
      echo "Coping ${DIR_NAME}/plugin.xml to ${DIR_NAME_STRIPPED}"
      cp "${DIR_NAME}/plugin.xml" "${DIR_NAME_STRIPPED}"
  fi
  rm -rf "${DIR_NAME}"
}

unpackOriginalBranch() {
  mkdir -p "${DIR_NAME_STRIPPED}"
  cd ${DIR_NAME_STRIPPED}
  jar xfv ../${filename}
  cd ..
  FILESIZE=$(stat -f%z ${filename})
  echo "Old Size of ${filename} = $FILESIZE bytes." >> ../../${CHANGE_DIR}/${SIZE_FILE}
  mv ${filename} ../../${CHANGE_DIR}
}

packOriginalBranch() {
  jar cMf ${filename}  ./${DIR_NAME_STRIPPED}/*
  FILESIZE=$(stat -f%z ${filename})
  echo "New Size of ${filename} = $FILESIZE bytes." >> ../../${CHANGE_DIR}/${SIZE_FILE}
  rm -rf ${DIR_NAME_STRIPPED}
}
pwd

moveSrc2Original() {
  DST_COPY="$DIR_NAME$NEW_VERSION"
  mv ${DIR_NAME} ${DST_COPY}
  jar cMf ${filename} ./${DST_COPY}/*
  rm -rf ${DST_COPY}
}

finalStep() {
  cd ../
  mkdir -p artifacts
  cd artifacts
  jar xvf ../artifacts.jar
  cd ..
  rm -rf artifacts.jar
  #rm -rf ${SRC_ZIP}
  #zip -r ${DST_ZIP} ${SRC_ZIP_DIR}/*
  #jar cMf ${DST_ZIP}  ./${SRC_ZIP_DIR}/*
  #rm -rf ${SRC_ZIP_DIR}
  #rm -rf ${NEW_SRC_PLUGIN_PATH}
}

############## SCRIPT STARTED HERE ##################

for i in "$@"
do
    case $i in
        -d=*|--destination=*)
            DST_ZIP="${i#*=}"
            echo "Destination name = ${DST_ZIP}"
            ;;
         -s=*|--source=*)
            SRC_ZIP="${i#*=}"
            echo "Source name = ${SRC_ZIP}"
            ;;
          -h*|--help*)
            usage
            exit
            ;;
        *)
            echo "ERROR: unknown parameter"
            usage
            exit 1
            ;;
    esac
    shift
done

if [ ! -s "${SRC_ZIP}" ];  then
  echo " ${SRC_ZIP} does not exist, or is empty "
  exit 1
fi

echo "Source name :  ${SRC_ZIP}"
echo "Destination name :  ${DST_ZIP}"

SRC_ZIP_DIR="${SRC_ZIP%.*}"
SRC_PLUGIN_PATH=${SRC_PLUGIN_DIR}/${SRC_PLUGIN_NAME}
NEW_SRC_PLUGIN_PATH=$(pwd)/${SRC_PLUGIN_NAME}

rm -rf ${SRC_ZIP_DIR}
rm -rf ${SRC_PLUGIN_NAME}
rm -rf ${CHANGE_DIR}

mkdir -p ${CHANGE_DIR}
cp -a ${SRC_PLUGIN_PATH} .
mkdir -p ${SRC_ZIP_DIR}
cd ${SRC_ZIP_DIR}
jar xfv ../${SRC_ZIP}
cd plugins

for filename in *; do
    echo "$filename"
    DIR_NAME_STRIPPED="${filename%.*}"
    DIR_NAME="${DIR_NAME_STRIPPED%_5.22.0}"
    findFolder
    if [ ! -z "$FOUND" ] ; then
      cp -a ${NEW_SRC_PLUGIN_PATH}/${DIR_NAME} .
      unpackOriginalBranch
      changeOriginalBranch
      packOriginalBranch
      #finalStep
    fi
done

#finalStep



