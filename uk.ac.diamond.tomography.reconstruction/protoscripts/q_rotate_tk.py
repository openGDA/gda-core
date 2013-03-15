#!/dls_sw/dasc/Python-2.4.4/bin/python
# $Id: q_rotate_tk.py 214 2012-02-13 15:46:36Z kny48981 $
import sys
import os
import commands
import shutil
import time
import glob
import platform
import subprocess

from Tkinter import *
mwin = Tk()
vals=[]
labels=[]
entries=[]
medv = IntVar()
rotv = IntVar()
dblv = IntVar()
nrows=8

def runproc():
   global nrows
   global vals
   global medv
   global dblv
   global rotv
   myqueue="medium.q"

   vfolder=vals[0].get()
   snum=vals[1].get()
   obase=vals[2].get()
   ofold=vals[3].get()
   fsuffix=vals[7].get()
   jobname="r_%s" % snum
   try:
      angle=float(vals[4].get())
   except ValueError:
      print "Angle must be a number of degrees"
      return

   try:
      nchunks=int(vals[5].get())
   except ValueError:
      print "Number of chunks must be a positive integer!"
      return

   try:
      nfiles=int(vals[6].get())
   except ValueError:
      print "Number of files must be a positive integer!"
      return

   if ( medv.get() == 0 ) :
      medflag=" "
   else:
      medflag="-median 1"
   if ( dblv.get() == 0 ) :
      dblflag=" "
   else:
      dblflag="-resize 200%x200%"
   if (rotv.get() == 0 ):
      rotflag=" "
   else:
      rotflag="-rotate %f" % angle

   print "dblflag=%s" % dblflag
   print "medflag=%s" % medflag
   print "rotflag=%s" % rotflag
   vpath="%s/%s/" % (vfolder,snum)
   infolder="%s/%s/projections/" %(vfolder,snum)
   ofolder="%s/%s" % (obase,ofold)
   osample="%s/%s/" % (ofolder,snum)
   outfolder="%s/%s/projections/" % (ofolder,snum)
   qfolder="%s/q_out_%s" % (ofolder,snum)
   print "infolder=%s outfolder=%s" %(infolder,outfolder)
   print "ofolder=%s qfolder=%s" %(ofolder,qfolder)

   if not (os.access (ofolder, os.F_OK)):
      print "creating output parent folder %s " % ofolder
      os.mkdir(ofolder)

   if not (os.access (osample, os.F_OK)):
      print "creating output sample folder %s " % osample
      os.mkdir(osample)

   if not (os.access (outfolder, os.F_OK)):
      print "creating output target folder %s " % outfolder
      os.mkdir(outfolder)

   if not (os.access (qfolder, os.F_OK)):
      print "creating quueue output folder %s " % qfolder
      os.mkdir(qfolder)

   chunksize=nfiles/nchunks
   lastchunk=nfiles-nchunks*chunksize
   lasttask=nchunks+1
   print infolder
   print outfolder
   print chunksize,lastchunk,lasttask
   


   #create the script to submit to the queue
   chunkscript="%s/runrotate.qsh" % qfolder

   sys.stdout = open(chunkscript,"w")
   
   print("""\
#!/bin/bash  
#modules don't work for gda user, use hacked module files 
set -x 
      source /dls_sw/i12/modulefiles/modules.sh 
      #add the modules required  
      module add /dls_sw/i12/modulefiles/local-64 
      module add global/cluster 
      tasknum=$SGE_TASK_ID 
      umask -S u=rwx,g=rwx,o=rx
      uname -a 
#variables set from the master python script
      if=%s
      of=%s
      chunksize=%i
      lasttask=%i
      lastchunk=%i
      medflag=\"%s\"
      dblflag=\"%s\"
      rotflag=\"%s\"
      fsfx=\"%s\"
      angle=%f
#calculate the derived values
      (( zidx = $tasknum - 1 )) 
      (( start = $zidx * $chunksize )) 
      (( stop = $start + $chunksize ))  
      echo "start $start stop $stop zidx $zidx"
      if [[ $tasknum -eq $lasttask ]] 
      then 
         (( stop = $start + $lastchunk + 1 )) 
      fi 
 
      for ((i=$start;i<$stop;i++)) 
      do 
         infile=`printf \"${if}/p_%%05d.%%s\" $i $fsfx` 
         outfile=`printf \"${of}/p_%%05d.%%s\" $i $fsfx` 
         echo $infile 
         echo $outfile 
         which convert
         convert $dblflag $medflag $rotflag  +matte $infile $outfile  
      done 
      set +x
   """) % (infolder,outfolder,chunksize,lasttask,lastchunk,medflag,dblflag,rotflag,fsuffix,angle) 

   sys.stdout.flush()
   sys.stdout=sys.__stdout__

   #set the queue environment
   qenviron=os.environ
   oldpath=""
   #check the architecture to get the right executable
   try :
      oldpath = qenviron["PATH"]
   except :
      oldpath = ""
   if ( "64bit" == platform.architecture()[0] ) :
      newpath="/dls_sw/apps/sge/SGE6.2/bin/lx24-amd64:/bin:/usr/bin:%s" % oldpath
   else:
      newpath="/dls_sw/apps/sge/SGE6.2/bin/lx24-x86/:/bin:/usr/bin:%s" % oldpath
   qenviron["SGE_CELL"]="DLS"
   qenviron["SGE_EXECD_PORT"]="60001"
   qenviron["SGE_QMASTER_PORT"]="60000"
   qenviron["SGE_ROOT"]="/dls_sw/apps/sge/SGE6.2"
   qenviron["PATH"]=newpath

   pyerr=open("%s/q_rotate_err.txt"%qfolder , "w")
   pyout=open("%s/q_rotate_out.txt"%qfolder , "w")

   print ("Spawning the rotation job ... ")
   queue_args="qsub -e %s -o %s -P i12 -q %s  -N %s -cwd -t 1-%i %s  " % (qfolder,qfolder,myqueue,jobname,lasttask,chunkscript)
   print ("queue args are",queue_args)

   try:
         subprocess.Popen(queue_args,env=qenviron,shell=True,stdout=pyout,stderr=pyerr)
   except:
         print ("ERROR Spawning the rotation job didn't work")
         pyerr.close()
         pyout.close()
         sys.exit(147)

   pyerr.close()
   pyout.close()
#end of runproc()

#setting up the window
top = Frame(mwin,borderwidth=10)
for i in range(0,nrows):
        vals.append(StringVar())
        labels.append (Label(top,text="label %i" %i,padx=0))
        entries.append(Entry(top,width=30,relief="sunken",textvariable=vals[i]))
        labels[i].grid(row=i,column=0)
        entries[i].grid(row=i,column=1,sticky=N+E+S+W)

btrow=Frame(top,borderwidth=10);
rotbt=Checkbutton(btrow,text="Rotate",variable=rotv)
rotbt.pack(side=LEFT)
medbt=Checkbutton(btrow,text="Median-1",variable=medv)
medbt.pack(side=LEFT)
dblbt=Checkbutton(btrow,text="Double size",variable=dblv)
dblbt.pack(side=LEFT)
runbt=Button(btrow,text="Run",command=runproc)
runbt.pack(side=LEFT)
exitbt=Button(btrow,text="Exit",command="exit")
exitbt.pack(side=LEFT)

btrow.grid(row=i+1,column=0,columnspan=2)

top.pack(fill=X,expand=1)
top.grid_columnconfigure(1,weight=1)

#set some defaults
entries[0].insert(0,"/dls/i12/data/2012/ee7697-1/processing/rawdata/")
entries[1].insert(0,"spider")
entries[2].insert(0,"/dls/i12/data/2012/ee7697-1/processing/")
entries[3].insert(0,"rotated")
entries[4].insert(0,"-0.65")
entries[5].insert(0,"64")
entries[6].insert(0,"6000")
entries[7].insert(0,"tif")
labels[0].config(text="Input Base folder")
labels[1].config(text="Input folder")
labels[2].config(text="Output Base Folder")
labels[3].config(text="Output Target Folder")
labels[4].config(text="Rotation Angle [bad - good] ")
labels[5].config(text="Number of tasks")
labels[6].config(text="Number of projections")
labels[7].config(text="file suffix")

mainloop()

