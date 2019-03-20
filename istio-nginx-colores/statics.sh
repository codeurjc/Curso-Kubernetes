#!/bin/bash

TMP_FILE=$(mktemp)

echo "Launching test..."

for i in $(seq 0 99);
do 
  curl -L --silent http://$INGRESS_HOST:$INGRESS_PORT/app | grep background-color >> $TMP_FILE
done

GREEN=$(grep green $TMP_FILE | wc -l)
RED=$(grep red $TMP_FILE | wc -l)

echo "Test results: "
echo "GREEN     $GREEN/100"
echo "RED       $RED/100"

rm $TMP_FILE