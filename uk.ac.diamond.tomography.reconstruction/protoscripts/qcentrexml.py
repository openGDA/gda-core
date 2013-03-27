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
   print("Create a queued batch for testing image centre values")
   print("Usage: \n\n%s cstart [ [nsteps cstep] [slicestart nslices slicestep] ]\n") % (os.path.basename(sys.argv[0]))
   print("default: nsteps=16 cstep=1 slicestart=500")
   print("inputs: settings.xml file in current folder and sinograms in 'sinograms' folder")
   print("outputs: stored in 'centre_outputs' folder, in time-stamped subfolders -- one for text files and one for images")


   
def main():
   global qenviron
   argc=len(sys.argv)
   if (argc <= 1 ):
      printusage()
      return



   cstart=2000.0
   cstop=2016.0
   cstep=1.0

   cstart=float(sys.argv[1])


   if (argc >= 3 ):
      nsteps = int(sys.argv[2])
   else:
      nsteps  = 16

   if (argc >= 4 ):
      cstep = float(sys.argv[3])
   else:
      cstep = 1.0

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

   holdflag=0
   xmlinfile="settings.xml"
   tstamp=time.strftime("%Y_%m%d_%H%M%S")
   if (argc >= 8 ):
      holdname = (sys.argv[7])
      holdstring = "-hold_jid"
      holdflag=1
      tstamp=holdname
   else:
      holdname=""
      holdstring=""
      holdflag=0

   #set the queue environment
   setenvironment()
   reconprogram="dt64n -v 4"
   obasefolder="centre_output"
   myfolder="c_%s_files" % tstamp
   imfolder="c_%s_images" % tstamp
   mypath="%s/%s" % (obasefolder,myfolder)
   impath="%s/%s" % (obasefolder,imfolder)

   if not(os.access (obasefolder,os.F_OK)):
      os.mkdir(obasefolder)

   if not(os.access ("settings.xml",os.F_OK)):
      print("copying default settings.xml file to the current folder ")
      os.system("cp /dls_sw/i12/software/tomography_scripts/settings.xml .")
   else:
      print("Using  settings.xml file found in the current folder ")


   if not(os.access (mypath,os.F_OK)):
      os.mkdir(mypath)

   if not(os.access (impath,os.F_OK)):
      os.mkdir(impath)



   print("cstart: %f" % cstart)
   print("cstep: %f" % cstep)
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



   #create a separate xml for each centre value
   for step in range(0,nsteps,1):
      sgetask=step+1
      ctr=round(cstart+float(step)*cstep,2)
      print("step: %i task: %i centre: %f" % (step,sgetask,ctr))

      #change the values in the xml tags 
      #create the file prefix for this job
      outputs=mydoc.getElementsByTagName("OutputData")
      prefix=outputs[0].getElementsByTagName("Prefix")
      prefix[0].childNodes[0].data="ctr_%08.2f_" % ctr

      #set the centre as specified
      bptag=mydoc.getElementsByTagName("Backprojection")
      ictr=bptag[0].getElementsByTagName("ImageCentre")
      ictr[0].childNodes[0].data=ctr

      bptag=mydoc.getElementsByTagName("LogFile")
      bptag[0].childNodes[0].data="%s/log_%03i.xml" %(mypath,sgetask)

      #use the task number for file name to simplify queue script

      f=file("%s/ctr_%03d.xml" % (mypath,(sgetask)),"w")
      f.write(mydoc.toxml())
      f.close()



   #create a little bash script to run in the queue
   myscript="%s/centre.qsh" % mypath
   sys.stdout = open(myscript,"w")

   #output the introductory script section
   print("""\

#!/bin/bash 
#centring script
set -x
#add the modules required 
source /dls_sw/i12/modulefiles/modules.sh
module add /dls_sw/i12/modulefiles/local-64
myjob=$JOB_ID
mytask=$SGE_TASK_ID
myxmlfile=`printf "ctr_%03d.xml" $mytask`
""")

   print("""\
mypath=%s
myxmlpath=$mypath/$myxmlfile
""") % mypath

   print("""\
%s $myxmlpath
""") % (reconprogram)



#end of the queue script text
   sys.stdout.flush()
   sys.stdout=sys.__stdout__

   #run the script in the queue
   myqueue="low.q"
   if (holdflag == 1 ):
      jobname="c_%s" % holdname
   else:
      jobname="ctr"

   pyenv_o=open("%s/python_stdout_%s.txt" % (mypath,jobname),"w")
   pyenv_e=open("%s/python_stderr_%s.txt" % (mypath,jobname),"w")
   try:
         #print ("Spawning the centring job ... ")
         #if (holdflag == 1 ):
         #   thispid=os.spawnlpe(os.P_WAIT,"qsub","qsub","-l","gpus=1","-P","i12","-e",mypath, "-o", mypath,holdstring,holdname, "-q",myqueue,"-N",jobname,"-cwd","-t","%i-%i" % (1,nsteps),myscript, qenviron)
         #else:
         #   thispid=os.spawnlpe(os.P_WAIT,"qsub","qsub", "-l","gpus=1","-P","i12","-e",mypath, "-o", mypath, "-q",myqueue,"-N",jobname,"-cwd","-t","%i-%i" % (1,nsteps),myscript, qenviron)
         #print ("return value was %s" % thispid)
         print ("Spawning the centring job ... ")
         if (holdflag == 1 ):
            thispid=os.spawnlpe(os.P_WAIT,"qsub","qsub","-l","gpus=1","-e",mypath, "-o", mypath,holdstring,holdname, "-q",myqueue,"-N",jobname,"-cwd","-t","%i-%i" % (1,nsteps),myscript, qenviron)
         else:
            thispid=os.spawnlpe(os.P_WAIT,"qsub","qsub", "-l","gpus=1","-e",mypath, "-o", mypath, "-q",myqueue,"-N",jobname,"-cwd","-t","%i-%i" % (1,nsteps),myscript, qenviron)
         print ("return value was %s" % thispid)
   except:
         print ("ERROR Spawning the sinogram job didn't work")
         pyerr.close()
         pyout.close()
         pyenv_o.close()
         pyenv_e.close()
         sys.exit(147)
   



if __name__ == "__main__":
      #print "Running main"
      main()
