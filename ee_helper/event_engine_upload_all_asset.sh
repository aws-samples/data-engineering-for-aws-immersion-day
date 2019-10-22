#!/bin/bash

# This script uploads all files in the ./assets/ directory to the Event Engine
# S3 bucket and path for the module ID and version specified below.

# DOUBLE-CHECK THE MODULE ID AND VERSION, BELOW:
MODULE_ID=$1
MODULE_VERSION=1

###### No need to edit below this line
EE_BUCKET=ee-assets-prod-${AWS_DEFAULT_REGION}
ASSETS_DIR=./assets

echo "---------------------"
echo "Module Configuration:"
echo "---------------------"
echo "  - MODULE_ID: $1"
echo "  - MODULE_VERSION: $MODULE_VERSION"
echo ""
## To minimize risk that we overwrite assets in wrong version/module, we need
## a positive confirmation from the user that they have double-checked the values above.
  echo "----------------------------------------------------------------------------"
if [ "$2" != "confirm" ]; then
  echo 'Note - this is a dry-run! Double-check the module and version ID and,'
  echo 'if you are ready to upload, then add the "confirm" parameter, shown below:'
  echo ""
  echo "./upload.sh <your module_id> confirm"
  echo "----------------------------------------------------------------------------"
  exit
fi

## If this errors out, double-check that that you assumed the proper credentials using the CREDENTIALS
## info from the Assets page in Event Engine for your module. 
SYNC_TARGET=s3://$EE_BUCKET/modules/$MODULE_ID/v$MODULE_VERSION/

echo "Syncing local ./assets directory to $SYNC_TARGET:"
echo "-------------------------------------------------------------------------"
aws s3 sync $ASSETS_DIR $SYNC_TARGET
echo ""
echo "All done!"
echo ""
