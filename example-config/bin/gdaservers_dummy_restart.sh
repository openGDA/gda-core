export JAVA_OPTS="-Xms128m -Xmx1024m -XX:MaxPermSize=128m"

CMD="python2.6 $1/bin/gda --config=$2 --smart --trace servers -v --mode=$3 --stop $4 $5 $6 $7"
echo $CMD
$CMD

CMD="python2.6 $1/bin/gda --config=$2 --smart --trace nameserver -v --mode=$3 --start"
echo $CMD
$CMD

sleep 5

CMD="python2.6 $1/bin/gda --config=$2 --smart --trace eventserver -v --mode=$3 --start"
echo $CMD
$CMD

sleep 5

CMD="python2.6 $1/bin/gda --config=$2 --smart --trace logserver -v --mode=$3 --start"
echo $CMD
$CMD

sleep 5

CMD="python2.6 $1/bin/gda --config=$2 --smart --trace objectserver -v --mode=$3 --start -f $4 $5 $6 $7"
echo $CMD
$CMD
