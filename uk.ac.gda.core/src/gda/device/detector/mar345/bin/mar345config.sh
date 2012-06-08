#!/bin/bash -f

DIR=/usr/local/diamond/apps/i15/mar345

PATH=$PATH:$DIR/bin:$DIR/bin/linux/default

MARHOME=$DIR
#should be changed to a directory local on 
MARLOGDIR=$HOME/mar345/log
if [ !-d $MARLOGDIR ]; then
  mkdir -p $MARLOGDIR
fi
MARTABLEDIR=$DIR/tables


export PATH MARTABLEDIR
