#how to use: add map name as parameter
#zip map v2
#! /bin/bash
if [ $# -eq 0 ]
then
	echo "no map name passed as argument. You must run this script from the maps directory."
else
	"Compressing the map:" $1
	zip -r map.zip $1 -x "*.DS_Store"
fi