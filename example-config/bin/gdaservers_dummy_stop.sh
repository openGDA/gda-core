CMD="python2.6 $1/bin/gda --config=$2 --smart --trace servers -v --mode=$3 --stop $4 $5 $6 $7"
echo $CMD
$CMD
