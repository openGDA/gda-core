#!/dls_sw/tools/bin/python2.4
#$Id: sino_listener.py 243 2012-08-31 11:05:21Z kny48981 $
import getopt
import sys
import os
import commands
import shutil
import time
import glob
import platform
import subprocess

interval=1
lt=10
wd=4008                   #default
ht=2672                    #default
firstchunk=1              #default
lastchunk=16
nchunks=16                #default
chunk_ht=167              #default
vflag=False
nproj=0
nperseg=0
infmt="p_%05d.tif"


def errprint(message="none"):
   print "errprint"
   print "sino_listener.py: %s" % (message)

def vprint(message):
    global vflag
    if vflag:
        print  message


def usage():
   svnstring="$Id: sino_listener.py 243 2012-08-31 11:05:21Z kny48981 $"
   global interval
   global infmt
   global lt
   global wd
   global ht
   global chunk_ht
   global firstchunk
   global lastchunk
   global nchunks
   global nperseg
   global nproj
   global chunk_ht
   print("%s version %s" %(sys.argv[0],svnstring))
   print("Usage:")
   print(sys.argv[0])
   print ("-i input dir (currently: projections)")
   print ("-o output dir (currently: sinograms)")
   print ("-p number of projections (currently %i )" % nperseg)
   print ("    NOT automatically determined!")
   print ("-w width (currently %i )" % wd)
   print ("-l length (height) of the image(currently %i)" % ht)
   print ("-F number of first chunk (currently %i) " % firstchunk)
   print ("-L number of last chunk (currently same as number of chunks) " )
   print ("-n total number chunks (currently %i) " % nchunks)
   print ("-J job hame ")
   print ("-j suffix of job name ")
   print ("-b bytes per pixel (currently 2)")
   print ("-s number of segments (currently 1) ")
   print ("-Z timeout (currently %i)" % lt)
   print ("-z check interval (currently %i)" % interval )
   print ("-Q queue (currently medium.q) ")
   print ("-U Unique ID (currently use PID) ")
   print ("-I input filename format (C printf style) (currently %s) " % infmt)
   print ("-1 start numbering of input files from 1 instead of 0 ")
   


def main():
   print "Entering main"
   global chunk_ht
   global firstchunk
   global ht
   global infmt
   global interval
   global lastchunk
   global lt
   global nchunks
   global nperseg
   global nproj
   global vflag
   global wd
   uniqueflag=False
   inflag=False
   idxflag=" "
   outflag=False
   lastflag=False
   delflag=False
   tifflag=False
   vflag=False
   pstrings=['p','f','a']
   pnums=[0,0,0]
   pidnums=[]
   mypid=os.getpid()

   #width and height need to come from somewhere too ..
   #some defaults for testing

   indir="projections"
   outdir="sinograms"
   bytes=2                   #default
   firstchunk=1              #default
   nchunks=16                #default
   nsegs=1                   #default
   nperseg=6000              #default
   jobbasename="chunk"       #default
   jobname="chunk_sino"      #default
   existflag=" "             #default
   jobsuffix=""              #default
   myqueue="low.q"        #default
   uniqueid="U"              #default
   cropleft=0
   cropright=0
   hflag=0
   if (len(sys.argv) < 2):
      hflag=1


   Wflag=0
   try:                                
      opts, args = getopt.gnu_getopt(sys.argv[1:],  "1U:O:C:EGI:J:N:R:S:T:Z:b:cF:L:hi:j:l:n:o:p:s:vw:xz:Q:W:")
   except getopt.GetoptError, err:           
      errprint ("Option parsing error")
      errprint ("Command line values: %s" % (sys.argv))
      errprint("Message is %s" % (str(err))) 


      usage()                          
      sys.exit(2)                     
   if not (len(args) == 0 ):
      errprint ("Option parsing error")
      errprint ("Command line values: %s" % (sys.argv))
      errprint("This program should not have non-flagged arguments") 
      print "unrecognized arguments: ", args
      print opts, args
      sys.exit(2)

   for o,a in opts:
      if o == "-W":
          mywait=a
          Wflag=1
      if o == "-Q":
          myqueue=a
      elif o == "-i":
          indir=a
          inflag=True
      elif o == "-1":
          idxflag="-1"
      elif o == "-I":
          infmt=a
          inflag=True
      elif o == "-o":
           outdir=a
           outflag=True
      elif o == "-l":
           ht=int(a)
      elif o == "-w":
           wd=int(a)
      elif o == "-b":
           bytes=int(a)
      elif o == "-J":
           jobbasename="%s" % a
      elif o == "-U":
           uniqueid="%s" % a
           uniqueflag=True
      elif o == "-j":
           jobsuffix="_%s" % a 
      elif o == "-E":
           existflag="-E"
      elif o == "-s":
           nsegs=int(a)
      elif o == "-p":
           nperseg=int(a)
      elif o == "-n":
           nchunks=int(a)
      elif o == "-L":
           lastchunk=int(a)
           lastflag=True
      elif o == "-F":
           firstchunk=int(a)
      elif o == "-R":
           cropright=int(a)
      elif o == "-T":
           cropleft=int(a)
      elif o == "-h":
           hflag=1
      elif o == "-Z":
          lt=int(a)
      elif o == "-z":
          interval=int(a)
      elif o == "-v":
          vflag=True
      else:
           errprint ("Ignored option")
           errprint ("option %s value %s" % (o,a))
           #usage()
           #sys.exit(2)
#   if ( not (inflag and outflag)):
#      errprint("Input and Output directories must be specified")
#      usage()
#      sys.exit(3)

   # How to handle the last chunk?
   #for now we assume all chunks are the same height

   if (hflag):
      usage()
      sys.exit(0)


   if (uniqueflag):
      mypid=uniqueid
   else:
      mypid=os.getpid()

   if (lastflag):
      print("Selecting last chunk to process: %i " % lastchunk )
   else:
      lastchunk=nchunks

   print ("Using first chunk %i and last chunk %i") %(firstchunk,lastchunk)

   tstamp=time.strftime("%Y_%m%d_%H%M%S")


   jobname="%s_sn_%s_%s" %(jobbasename,jobsuffix,mypid)

   settingsbase="sino_output"
   settingsfolder="%s/sino_%s_files" % (settingsbase,tstamp)
   print "Settings and log files folder will be : %s " % settingsfolder
   nproj=nperseg*nsegs

   chunk_ht = ht/nchunks

   projnum=0
   size=wd*ht
   size_bytes=wd*ht*bytes
   chunk_size=wd*chunk_ht
   chunk_size_bytes = chunk_size * bytes 
   linebytes=wd*bytes
   chunkrange=range(1,nchunks+1)

   projrange=range(0,nproj)
   sinorange=range(0,chunk_ht)
   vprint ("ht %i chunk_ht: %i " % (ht, chunk_ht))
   vprint (sinorange)
   finishname="f_%s" % jobname
   print("JOB NAME IS %s\n") % finishname
   print("using input file format %s\n") % infmt

#check folder for the project 
   print "Checking for input directory %s \nTimeout: %i seconds" % (indir,lt)
   wtime=0
   found=0
   #wait for the directory to appear
   while ( (wtime <= lt) and (found == 0) ):
      if not ( os.access (indir, os.F_OK)):
         wtime += interval
         time.sleep(interval)
         print "." 
      else:
         found=1
   #exit if it times out
   if not (os.access (indir, os.F_OK)):
         errprint ("Input directory %s is not available after %i seconds!" % (indir,wtime))
         sys.exit(5)
   else:
      print "Input directory %s found...." % indir

#locate or create the output folders
   if not(os.access (settingsbase,os.F_OK)):
      os.mkdir(settingsbase)

   if not(os.access (settingsfolder,os.F_OK)):
      print "creating %s" % settingsfolder
      os.mkdir(settingsfolder)
      #maybe return on an error here?


   #create the queue bash script for the queue task array

   thisdir=os.getcwd()
   #chunkscript="sinochunk.qsh" % os.getpid()
   chunkscript="%s/sinochunk.qsh" % settingsfolder
   finishscript="%s/finishchunk.qsh" % settingsfolder
   chunkprogram="sino_chunk_tiff_new.q"
   chunkflags="-i %s -o %s -w %i -l %i -z %i  -Z %i  -s %i -p %i -b %i %s -S %s -I %s -T %i -R %i %s " % (indir,outdir,wd,chunk_ht,interval,lt,nsegs,nperseg,bytes,existflag,settingsfolder,infmt,cropleft,cropright,idxflag) 
   vprint("Creating the queue script: %s" % chunkscript)
   vprint("Using : %s" % chunkprogram)

   sys.stdout = open(chunkscript,"w")

   #output the introductory script section
   print("""\
#!/bin/bash 
set -x

#add the modules required 
source /dls_sw/i12/modulefiles/modules.sh
module add /dls_sw/i12/modulefiles/local-64
myjob=$JOB_ID
mytask=$SGE_TASK_ID
""") 

   #set the unique identifier value
   print("""\
mypid=%s
""") % mypid

   #set the output folder
   print("""\
odir=%s
""") % outdir

   #check folder existance
   print("""\
if [[ ! -e $odir ]]
then
mkdir -p $odir
fi
mynum=`printf "%%03d" $mytask`

echo PATH is $PATH

#ulimit -c unlimited
# UNCOMMENT  some of these lines to get more diagnostic information
# env > task${mytask}.env
#tracename=trace${myjob}_t${mytask}.trace
#trace execution all
""") 

   #assemble the command line that actually does the task
   print("""\
%s %s -m $mytask  -v -J %s${myjob}_t${mytask}
""") % (chunkprogram,chunkflags,jobname)

   #check for error in return value
   print("""\
retval=$?
if [[ retval -ne 0 ]]
then
  echo -e "job $myjob task $mytask return-value $retval\\n" >> %s/error_$mypid.txt  
fi
""") % settingsfolder

#end of the queue script text
   sys.stdout.flush()
   sys.stdout=sys.__stdout__


   vprint("Creating the finishing script: %s" % finishscript)

   sys.stdout = open(finishscript,"w")
   print("""\
#!/bin/bash 
set -x

#add the modules required 
source /dls_sw/i12/modulefiles/modules.sh
#add environment required by epics channel access (ezca) 

myjob=$JOB_ID
mytask=$SGE_TASK_ID
mypid=%s
jjname="%s"
""") % (mypid,jobbasename)

   print("""\
ffolder="${jjname}_files"
errfile=%s/error_$mypid.txt
""") % settingsfolder

   print("""\
if [[ -e $errfile ]]
then
errtxt=`cat $errfile`

		#send a mail message when completed
		/usr/sbin/sendmail -t $USER <<-ENDM
			Subject: [QUEUE] I12 job error from PID ${mypid}

                         The I12 data acquisition job has encountered a problem.
                         The error information is:

                         $errtxt

                         This  e-mail was automatically generated by
                         the tomographic reconstruction batch processing system

		ENDM

fi

#mkdir -p ${ffolder}/job${myjob}
#mv ${jjname}_sino* ${ffolder}/job${myjob}

mv *.trace %s
""") % settingsfolder

#end of the finish script text
   sys.stdout.flush()
   sys.stdout=sys.__stdout__



   #set the queue environment
   qenviron=os.environ
   vprint (len(qenviron))
   vprint (qenviron.items())
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
   vprint (len(qenviron))
   vprint (qenviron.items())
   pyerr=open("%s/sino_listener_stderr_%s.txt" % (settingsfolder,jobname) ,"w")
   pyout=open("%s/sino_listener_stdout_%s.txt" % (settingsfolder,jobname) ,"w")
   pyenv_o=open("%s/python_stdout_%s.txt" % (settingsfolder,jobname),"w")
   pyenv_e=open("%s/python_stderr_%s.txt" % (settingsfolder,jobname),"w")

   try:
         print ("Spawning the sinogram job ... ")
         if (vflag):
            subprocess.Popen("env",env=qenviron,shell=False,stdout=pyenv_o,stderr=pyenv_e)
            print qenviron
         if (Wflag):
            thispid=os.spawnlpe(os.P_WAIT,"qsub","qsub", "-P","i12","-e",settingsfolder, "-o", settingsfolder, "-q",myqueue,"-N",jobname,"-hold_jid",mywait,"-cwd","-pe","smp","4","-t","%i-%i" % (firstchunk,lastchunk),chunkscript, qenviron)
         else:
            thispid=os.spawnlpe(os.P_WAIT,"qsub","qsub", "-P","i12","-e",settingsfolder, "-o", settingsfolder, "-q",myqueue,"-N",jobname,"-cwd","-pe","smp","4","-t","%i-%i" % (firstchunk,lastchunk),chunkscript, qenviron)
         #thispid=os.spawnlpe(os.P_WAIT,"qsub","qsub", "-e",settingsfolder, "-o", settingsfolder, "-q",myqueue,"-N",jobname,"-cwd","-t","%i-%i" % (firstchunk,lastchunk),chunkscript, qenviron)
         print ("return value was %s" % thispid)
   except:
         print ("ERROR Spawning the sinogram job didn't work")
         pyerr.close()
         pyout.close()
         pyenv_o.close()
         pyenv_e.close()
         sys.exit(147)

   try:
         print ("Spawning the sinogram finishing job ... ")
         thispid=os.spawnlpe(os.P_WAIT,"qsub","qsub","-P","i12","-e",settingsfolder,"-o",settingsfolder,"-q","high.q","-hold_jid",jobname,"-N",finishname,finishscript,qenviron)
         print ("return value was %s" % thispid)

   except:
         print ("Spawning the sinogram finishing job didn't work")
         pyerr.close()
         pyout.close()
         sys.exit(148)

   pyerr.close()
   pyout.close()

   sys.exit(0)
   

if __name__ == "__main__":
      print "Running main"
      main()
