#!/dls_sw/prod/tools/RHEL5/bin/python2.6
import sys
import os
import time
import getopt
import subprocess
import platform
from xml.dom.minidom import parse

def setenvironment():
   global qenviron

   #set the queue environment
   qenviron=os.environ
   oldpath=""
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
   
def printusage():
   print(sys.argv[0])
   print("Create a queued batch for testing reconstruction parameter values")
   print("Usage: %s pstart [nsteps pstep]") % (sys.argv[0])
   print("default: nsteps=16 pstep=1")
   print("inputs: settings.xml file in current folder and sinograms in 'sinograms' folder")
   print("inputs: make sure the best centre already found is put into the settings.xml file")
   print("outputs: stored in 'param_outputs' folder, in time-stamped subfolders -- one for text files and one for images")


   
def main():
   global qenviron
   argc=len(sys.argv)
   print (argc)
   if (argc <= 1 ):
      printusage()
      return



   #set the queue environment
   setenvironment()
   reconprogram="dt64n -v 4"
   obasefolder="param_output"
   tstamp=time.strftime("%Y_%m%d_%H%M%S")
   myfolder="par_%s_files" % tstamp
   imfolder="par_%s_images" % tstamp
   mypath="%s/%s" % (obasefolder,myfolder)
   impath="%s/%s" % (obasefolder,imfolder)

   if not(os.access (obasefolder,os.F_OK)):
      os.mkdir(obasefolder)

   if not(os.access (mypath,os.F_OK)):
      os.mkdir(mypath)

   if not(os.access (impath,os.F_OK)):
      os.mkdir(impath)

   pstart=2000.0
   cstop=2016.0
   pstep=1.0

   pstart=float(sys.argv[1])


   if (argc >= 3 ):
      nsteps = int(sys.argv[2])
   else:
      nsteps  = 16

   if (argc >= 4 ):
      pstep = float(sys.argv[3])
   else:
      pstep = 1.0

   if (argc >= 5 ):
      slicestart = int(sys.argv[4])
   else:
      slicestart = 500

   if (argc >= 6 ):
      nslices = int(sys.argv[5])
   else:
      nslices = 1

   if (argc >= 7 ):
      slicestep = int(sys.argv[6])
   else:
      slicestep = 1

   if (argc >= 8 ):
      xmlinfile = (sys.argv[7])
   else:
      xmlinfile = "settings.xml" 


   print("pstart: %f" % pstart)
   print("pstep: %f" % pstep)
   print("nsteps: %i" % nsteps)


   slicestop=slicestart + ((nslices - 1 ) * slicestep)

   print("slicestart: %i" % slicestart)
   print("slicestop: %i" % slicestop)
   print("slicestep: %i" % slicestep)
   print("xmlinfile: %s" % xmlinfile)

   mydoc=parse(xmlinfile)

   #set the slice range and input folder which is constant for all different centre values

   bptag=mydoc.getElementsByTagName("InputData")
   valtag=bptag[0].getElementsByTagName("FileFirst")
   valtag[0].childNodes[0].data=slicestart

   valtag=bptag[0].getElementsByTagName("FileLast")
   valtag[0].childNodes[0].data=slicestop

   valtag=bptag[0].getElementsByTagName("FileStep")
   valtag[0].childNodes[0].data=slicestep

   valtag=bptag[0].getElementsByTagName("Folder")
   valtag[0].childNodes[0].data="sinograms"

   #set the output folder and path for this job
   bptag=mydoc.getElementsByTagName("OutputData")

   valtag=bptag[0].getElementsByTagName("Folder")
   valtag[0].childNodes[0].data=impath
   mycpu=0



   #create a separate xml for each centre value
   for step in range(0,nsteps,1):
      sgetask=step+1
      ctr=round(pstart+float(step)*pstep,5)
      print("step: %i task: %i parameter: %f" % (step,sgetask,ctr))

      #change the values in the xml tags 
      #create the file prefix for this job
      outputs=mydoc.getElementsByTagName("OutputData")
      prefix=outputs[0].getElementsByTagName("Prefix")
      prefix[0].childNodes[0].data="param_%08.5g_" % (ctr)

      #set the centre as specified
      bptag=mydoc.getElementsByTagName("Preprocessing")
      ntag=bptag[0].getElementsByTagName("RingArtefacts")
      ictr=ntag[0].getElementsByTagName("ParameterR")
      ictr[0].childNodes[0].data=ctr

      bptag=mydoc.getElementsByTagName("LogFile")
      bptag[0].childNodes[0].data="%s/log_%03i.xml" %(mypath,sgetask)

      #use the task number for file name to simplify queue script

      f=file("%s/param_%03d.xml" % (mypath,(sgetask)),"w")
      f.write(mydoc.toxml())
      f.close()



   #create a little bash script to run in the queue
   myscript="%s/param.qsh" % mypath
   sys.stdout = open(myscript,"w")

   #output the introductory script section
   print("""\
#!/bin/bash 
#parameter script
set -x
#add the modules required 
source /dls_sw/i12/modulefiles/modules.sh
module add /dls_sw/i12/modulefiles/local-64
myjob=$JOB_ID
mytask=$SGE_TASK_ID
myxmlfile=`printf "param_%03d.xml" $mytask`
""")

   #create the correct path for the input file
   print("""\
mypath=%s
myxmlpath=$mypath/$myxmlfile
""") % mypath

   #section to implement the cuda locking

   print("""\
mycuda=0
cudafound=0
tries=0
mylockdir=/tmp/tomo-i12-$USER/lock

mydate=`date`
mkdir -p $mylockdir
#try to acquire a lock representing one of the cuda cards
while [[ $cudafound -eq 0 ]]
do

   for mycuda in 0 1 
   do
       mylockf=$mylockdir/cuda$mycuda
       exec 8>>$mylockf

       if  flock -w 5 -x 8
       then
          cudafound=1
          echo "host $HOSTNAME date $mydate job $myjob task $tasknum"  >> $mylockf
          echo "Got lock on $mylockf"
          break
       else
          echo "Did not get lock on $mylockf"
       fi
   done
    if [[ $cudafound -eq 0 ]]
    then
       (( tries ++ ))
       if [[ $tries -gt 10 ]]
       then
            echo "Could not acquire the lock on a CUDA device lock-file!"
            echo "Check $mylockdir "
            exit
       fi
     sleep 5
     fi

done
echo "using cuda $mycuda" 
#change the input file to change the device ... aaargh ... 
sed -i "s|^.*GPUDeviceNumber.*$|<GPUDeviceNumber>$mycuda</GPUDeviceNumber>|" $myxmlpath
""")

   #actually run the program at last
   print("""\
%s $myxmlpath
""") % (reconprogram)



#end of the queue script text
   sys.stdout.flush()
   sys.stdout=sys.__stdout__

   #run the script in the queue
   myqueue="medium.q"
   jobname="param"
   pyenv_o=open("%s/python_stdout_%s.txt" % (mypath,jobname),"w")
   pyenv_e=open("%s/python_stderr_%s.txt" % (mypath,jobname),"w")
   try:
         print ("Spawning the  job ... ")
         thispid=os.spawnlpe(os.P_WAIT,"qsub","qsub", "-P","i12","-e",mypath, "-o", mypath, "-q",myqueue,"-N",jobname,"-cwd","-pe","smp","4","-t","%i-%i" % (1,nsteps),myscript, qenviron)
         print ("return value was %s" % thispid)
   except:
         print ("ERROR Spawning the job didn't work")
         pyerr.close()
         pyout.close()
         pyenv_o.close()
         pyenv_e.close()
         sys.exit(147)
   



if __name__ == "__main__":
      #print "Running main"
      main()
