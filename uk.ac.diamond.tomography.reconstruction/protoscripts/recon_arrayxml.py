#!/dls_sw/prod/tools/RHEL5/bin/python2.6
import getopt
import sys
import os
import commands
import shutil
import time
import glob
import platform
import subprocess
from xml.dom.minidom import parse

interval=1
lt=10
vflag=False

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
   
def dumpvalues():
   global obasefolder
   global indir
   global wd
   global imagelength
   global bytes
   global nchunks
   global nsegs
   global centre
   global nperseg
   global jobbasename
   global jobname
   global existflag
   global jobsuffix
   global myqueue
   global uniqueid
   global xmlinfile
   global myfolder
   global imfolder
   global mypath
   global impath

   print "******************* Values used are ********************** " 
   print "obasefolder: " , (obasefolder)
   print "indir: "  , (indir)
   print "xmlinfile: "  , (xmlinfile)

   print " "
   print "width wd: "  , (wd)
   print "imagelength: "  , (imagelength)
   print "bytes: "  , (bytes)
   print "nchunks: "  , (nchunks)
   print "nsegs: "  , (nsegs)
   print "nperseg: "  , (nperseg)
   print "jobbasename: "  , (jobbasename)
   print "jobname: "  , (jobname)
   print "existflag: "  , (existflag)
   print "jobsuffix: "  , (jobsuffix)
   print "myqueue: "  , (myqueue)
   print "uniqueid: "  , (uniqueid)

   print " "
   print "centre: ",centre
   print " "
   print "myfolder: " , myfolder
   print "imfolder: " , imfolder
   print "mypath: " , mypath
   print "impath: " , impath


def errprint(message="none"):
   print "recon_arrayxml.py: %s" % (message)

def vprint(message):
    global vflag
    if vflag:
        print  message

def usage():
   svnstring="$Id: recon_arrayxml.py 262 2013-01-29 10:38:43Z kny48981 $"
   global interval
   global lt
   print("%s version %s" %(sys.argv[0],svnstring))
   print("Usage:")
   print(sys.argv[0])
   print ("-I input xml (default: /dls_sw/i12/software/tomography_scripts/settings.xml)")
   print ("-C centre of rotation (REQUIRED) ")
   print ("-i input path (default: ./ )")
   print ("-d data (sinogram)  folder name (default: sinograms ) to be added to input path")
   print ("-o output folder name (default: reconstruction)")
   print ("-w width (default 4008)")
   print ("-l length (height) of image(default 2672)")
   print ("-F number of first slice (default 0) ")
   print ("-L number of last slice (default = length) ")
   print ("-S slices to skip (default = 1) ")
   print ("-n number of chunks (default 16) ")
   print ("-J job hame ")
   print ("-j suffix of job name ")
   print ("-Z timeout (default %i)" % lt)
   print ("-z check interval (default %i)" % interval )
   print ("-Q queue (default medium.q) ")
   print ("-U Unique ID (default use PID) ")
   print ("-v Verbose messages")
   
def main():
   global obasefolder
   global indir
   global wd
   global imagelength
   global firstslice
   global lastslice
   global bytes
   global nchunks
   global nsegs
   global nperseg
   global jobbasename
   global jobname
   global existflag
   global jobsuffix
   global myqueue
   global uniqueid
   global xmlinfile
   global myfolder
   global imfolder
   global mypath
   global impath
   global centre

   setenvironment()
   #set some fixed values

   reconprogram="dt64n"
   tstamp=time.strftime("%Y_%m%d_%H%M%S")

   #set the defaults
   obasefolder="reconstruction"
   holdflag=0
   holdstring=" "
   indir="."
   sinofolder="sinograms"
   wd=4008                       #default
   centre=0.0
   imagelength=2672              #default
   firstslice=0
   slicestep=1
   bytes=2                   #default
   nchunks=16                #default
   nsegs=1                   #default
   nperseg=6000              #default
   jobbasename="chunk"       #default
   jobname="chunk_sino"      #default
   existflag=" "             #default
   jobsuffix=""              #default
   myqueue="medium.q"          #default
   uniqueid=tstamp           #default
   xmlinfile="/dls_sw/i12/software/tomography_scripts/settings.xml"  #default
   vflag=False
   lastsliceflag=False
   centreflag=False

   if (len(sys.argv) <= 2):
      usage()
      sys.exit(0)
   print "Parsing the Options"

   #Parse the commandline options

   try:                                
      opts, args = getopt.gnu_getopt(sys.argv[1:],  "H:C:U:O:I:J:Z:d:b:dS:F:hi:j:l:L:n:o:p:s:vw:xz:Q:")
   except getopt.GetoptError, err:           
      errprint ("Option parsing error")
      errprint ("Command line values: %s" % (sys.argv))
      errprint("Message is %s" % (str(err))) 
      usage()                          
      sys.exit(2)                     
   for o,a in opts:
      #set the queue
      if o == "-Q":
          myqueue=a
      #set the input folder
      elif o == "-i":
          indir=a
          inflag=True
      elif o == "-d":
          sinofolder=a
      #set the input settings file
      elif o == "-I":
          xmlinfile=a
      #set teh output folder base
      elif o == "-o":
           obasefolder=a
      #set the length of a chunk
      elif o == "-l":
           imagelength=int(a)
      #set the image width
      elif o == "-w":
           wd=int(a)
      #set the number of bytes per pixel
      elif o == "-b":
           bytes=int(a)
      #set the job prefix
      elif o == "-J":
           jobbasename="%s" % a
      #get the job to hold for
      elif o == "-H":
           holdname="%s" % a
           holdflag=1
           holdstring="-hold_jid" 
      #set a unique id to help stream the queue job
      elif o == "-U":
           uniqueid="%s" % a
           uniqueflag=True
      #set a job suffix
      elif o == "-j":
           jobsuffix="_%s" % a 
      #set a number of scan segments 
      elif o == "-s":
           nsegs=int(a)
      #set number of images per scan segment
      elif o == "-p":
           nperseg=int(a)
      elif o == "-n":
           nchunks=int(a)
      #set number of last chunk to process
      elif o == "-L":
           lastslice=int(a)
           lastsliceflag=True
      #set the centre found by the user
      elif o == "-C":
           centre=float(a)
           centreflag=True
      #set number of first chunk to proces
      elif o == "-F":
           firstslice=int(a)
      elif o == "-S":
           slicestep=int(a)
      #usage message
      elif o == "-h":
           usage()
           sys.exit(0)
      elif o == "-d":
           delflag=True

      #set time-out options
      elif o == "-Z":
          lt=int(a)
      elif o == "-z":
          interval=int(a)

      #set verbosity
      elif o == "-v":
          vflag=True
      else:
           errprint ("Ignored option")
           errprint ("Ignored: option %s value %s" % (o,a))
           usage()
           sys.exit(2)



   #set up the paths and create if necessary
   myfolder="r_%s_files" % uniqueid
   imfolder="r_%s_images" % uniqueid

   mypath="%s/%s" % (obasefolder,myfolder)
   impath="%s/%s" % (obasefolder,imfolder)
   jobname="%s%s" % (jobbasename,jobsuffix)

   sinopath="%s/%s" %(indir,sinofolder)
   darkpath="%s/dark/" %(indir)
   flatpath="%s/flat/" %(indir)

   if(not (lastsliceflag)):
      lastslice=imagelength

   if not(centreflag):
      errprint("User  (or Controlling script) must specify the centre of reconstruction")
      sys.exit(0)

   if (vflag):
      dumpvalues()

   if not(os.access (obasefolder,os.F_OK)):
      os.mkdir(obasefolder)

   if not (os.access (obasefolder,os.F_OK)):
      print "Could not create folder %s" % obasefolder
      sys.exit(0)

   if not(os.access (mypath,os.F_OK)):
      os.mkdir(mypath)
   if not(os.access (impath,os.F_OK)):
      os.mkdir(impath)



   print ("Using sinogram data in %s") % sinopath
   flatflag=0

   if (os.access ("%s/flat.tif"%flatpath,os.F_OK)):
      print ("Using flat field data in %s/flat.tif") % flatpath
      flatflag=1
   else:
      print ("Using constant flat field data, path %s not found " % flatpath) 
      flatflag=0

   if (os.access ("%s/dark.tif"%darkpath,os.F_OK)):
      print ("Using dark field data in %s/dark.tif") % darkpath
      darkflag=1
   else:
      print ("Using constant dark field data, path %s not found " % darkpath) 
      darkflag=0


   print ("Output parameter and log files will be created in %s") % mypath
   print ("Output images will be created in %s") % impath

   nslices=lastslice-firstslice
   chunksize=(nslices / nchunks)
   if (vflag):
      print ("nslices = "),nslices
      print ("chunksize = "),chunksize

   mydoc=parse(xmlinfile)

   #set the input folder and path for this job
   bptag=mydoc.getElementsByTagName("InputData")

   valtag=bptag[0].getElementsByTagName("Folder")
   valtag[0].childNodes[0].data=sinopath

   valtag=bptag[0].getElementsByTagName("Prefix")
   valtag[0].childNodes[0].data="sino_"

   #set the output folder and path for this job
   bptag=mydoc.getElementsByTagName("OutputData")

   valtag=bptag[0].getElementsByTagName("Folder")
   valtag[0].childNodes[0].data=impath

   valtag=bptag[0].getElementsByTagName("Prefix")
   valtag[0].childNodes[0].data="image_"

   bptag=mydoc.getElementsByTagName("FlatDarkFields")
   midtag=bptag[0].getElementsByTagName("FlatField")
   valtag=midtag[0].getElementsByTagName("FileBefore")
   valtag[0].childNodes[0].data="%s/flat.tif" % flatpath

   valtag=midtag[0].getElementsByTagName("Type")

   if (flatflag == 1 ):
      valtag[0].childNodes[0].data="Row"
   else:
      valtag[0].childNodes[0].data="User" 

   midtag=bptag[0].getElementsByTagName("DarkField")
   valtag=midtag[0].getElementsByTagName("FileBefore")
   valtag[0].childNodes[0].data="%s/dark.tif" % darkpath
   valtag=midtag[0].getElementsByTagName("Type")

   if (darkflag == 1 ):
      valtag[0].childNodes[0].data="Row"
   else:
      valtag[0].childNodes[0].data="User"

   bptag=mydoc.getElementsByTagName("Backprojection")

   valtag=bptag[0].getElementsByTagName("ImageCentre")
   valtag[0].childNodes[0].data=centre

   mycpu=0

   f=file("settings_used.xml","w")
   f.write(mydoc.toxml())
   f.close()



   clist=file("%s/chunklist.txt" % mypath,"w");

   for chunk in range(0,nchunks):

      sgetask=chunk+1
      chunkstart = firstslice + (chunk * chunksize)
      chunkend = (chunkstart + chunksize - 1) 
      if (chunk == nchunks-1 ):
         chunkend=lastslice-1
      if (vflag):
         print("chunk %i sge-task %i start %i end %i num %i")%(chunk,sgetask,chunkstart,chunkend,(1+chunkend-chunkstart))

      clist.write( ("chunk %i sge-task %i start %i end %i num %i") % (chunk,sgetask,chunkstart,chunkend,(1+chunkend-chunkstart)))
      
      bptag=mydoc.getElementsByTagName("InputData")

      valtag=bptag[0].getElementsByTagName("FileFirst")
      valtag[0].childNodes[0].data=chunkstart
      valtag=bptag[0].getElementsByTagName("FileLast")
      valtag[0].childNodes[0].data=chunkend
      valtag=bptag[0].getElementsByTagName("FileStep")
      valtag[0].childNodes[0].data=slicestep

      bptag=mydoc.getElementsByTagName("LogFile")
      bptag[0].childNodes[0].data="%s/log_%03i.xml" %(mypath,sgetask)

      #use the task number for file name to simplify queue script

      f=file("%s/chunk_%03d.xml" % (mypath,(sgetask)),"w")
      f.write(mydoc.toxml())
      f.close()

   clist.close()
   #create a little bash script to run in the queue
   myscript="%s/reconstruct.qsh" % mypath
   sys.stdout = open(myscript,"w")

   #output the introductory script section
   print("""\
#!/bin/bash 
#reconstruction script
set -x
#add the modules required 
source /dls_sw/i12/modulefiles/modules.sh
module add /dls_sw/i12/modulefiles/local-64
myjob=$JOB_ID
mytask=$SGE_TASK_ID
myxmlfile=`printf "chunk_%03d.xml" $mytask`
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
   pyenv_o=open("%s/python_stdout_%s.txt" % (mypath,jobname),"w")
   pyenv_e=open("%s/python_stderr_%s.txt" % (mypath,jobname),"w")

   if (holdflag==1):
      try:
            print ("Spawning the  job ... ")
            thispid=os.spawnlpe(os.P_WAIT,"qsub","qsub", "-P","i12","-e",mypath, "-o", mypath, holdstring,holdname , "-q",myqueue,"-N",jobname,"-cwd","-pe","smp","4","-t","%i-%i" % (1,nchunks),myscript, qenviron)
            print ("return value was %s" % thispid)
      except:
            print ("ERROR Spawning the job didn't work")
            pyenv_o.close()
            pyenv_e.close()
            sys.exit(147)
   else:
      try:
            print ("Spawning the  job ... ")
            thispid=os.spawnlpe(os.P_WAIT,"qsub","qsub", "-P","i12","-e",mypath, "-o", mypath, "-q",myqueue,"-N",jobname,"-cwd","-pe","smp","4","-t","%i-%i" % (1,nchunks),myscript, qenviron)
            print ("return value was %s" % thispid)
      except:
            print ("ERROR Spawning the job didn't work")
            pyenv_o.close()
            pyenv_e.close()
            sys.exit(147)
   






if __name__ == "__main__":
      print "Running main"
      main()
