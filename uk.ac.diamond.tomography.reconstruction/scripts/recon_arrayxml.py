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

def folderExistsWithTimeOut(dirToCheck, waitIntervalSec, sleepInterSec, outStream):
	"""
	Returns true if dirToCheck is found to exist within timeToWaitInS. Time interval between checks is  sleepInterValInS
	Else False
	Each check a . is written to outStream
	"""
	wtime=0
	found=0
	#wait for the directory to appear
	while ((wtime<=waitIntervalSec) and (found==0)):
		if not (os.access (dirToCheck, os.F_OK)):
			wtime+=sleepInterSec
			time.sleep(sleepInterSec)
			outStream.write (".")
		else:
			found=1
	#exit if it times out
	return os.access (dirToCheck, os.F_OK)


def main(argv, out=sys.stdout, err=sys.stderr):

	rcn=ReconArrayXML()
	rcn.parseOptions(argv, out, err)
	success, imfolder=rcn.run()
	return success, imfolder

class ReconArrayXML():

	def __init__(self, testing=False):
		#self.argv = argv
		#self.out = out
		#self.err = err


		#set the defaults
		self.obasefolder="reconstruction"
		self.holdflag=0
		self.holdstring=" "
		self.indir="."
		self.sinofolder="sinograms"
		self.wd=4008                       #default
		self.centre=0.0
		self.imagelength=2672              #default
		self.firstslice=0
		self.slicestep=1
		self.bytes=2                   #default
		self.nchunks=16                #default
		self.nsegs=1                   #default
		self.nperseg=6000              #default
		self.jobbasename="chunk"       #default
		self.jobname="chunk_sino"      #default
		self.existflag=" "             #default
		self.jobsuffix=""              #default
		self.myqueue="medium.q"          #default
		self.uniqueid=time.strftime("%Y_%m%d_%H%M%S")           #default
		self.xmlinfile="/dls_sw/i12/software/tomography_scripts/settings.xml"  #default
		self.vflag=False
		self.lastsliceflag=False
		self.centreflag=False

		self.firstchunk=1 #number of first chunk
		self.hflag=0 # show help and exit
		self.testing=testing
		self.interval=1 #time interval when checking for a resource in seconds - controlled by z 
		self.lt=10 # timeout when checking for a resource in seconds - - controlled by Z

		self.qsub_project="i13" # project name given to qsub		
		self.jobID=[]
		self.copyright=\
		"""
		The tomographic reconstruction and artifact reduction algorithms have been developed in collaboration with 
		the University of Manchester. Novel mathematics and algorithms have been used. When reporting results, 
		the papers presenting these developments should be cited as follows:
		
		1. Valeriy Titarenko, Robert Bradley, Christopher Martin, Philip J. Withers and Sofya Titarenko,
		\t"Regularization methods for inverse problems in x-ray tomography",
		\tProc. SPIE 7804, 78040Z (2010); DOI:10.1117/12.860260.

		2. Kyrieleis A., Titarenko V., Ibison M., et al.,
		\t"Region-of-interest tomography using filtered back-projection: assessing the practical limits",
		\tJournal of Microscopy 241:1 pp. 69-82 (2011); DOI: 10.1111/j.1365-2818.2010.03408.

		3. Kyrieleis A., Ibison M., Titarenko V., et al.,
		\t"Image stitching strategies for tomographic imaging of large objects at high resolution at synchrotron sources",
		\tNuclear Instruments and Methods in Physics: Research Section A-Accelerators, Spectrometers, Detectors, and Associated Equipment 607: 3 pp. 677-684 (2009); DOI: 10.1016/j.nima.2009.06. 
		"""
	
	def usage(self):
		svnstring="$Id: recon_arrayxml.py 214 2012-02-13 15:46:36Z kny48981 $"
		self.out.write("Version %s\n"%(svnstring))
		self.out.write("Usage: ")
		self.out.write(self.argv[0]+"\n")
		self.out.write("-I input xml (default: /dls_sw/i12/software/tomography_scripts/settings.xml)\n")
		self.out.write("-C centre of rotation (REQUIRED)\n")
		self.out.write("-i input path (default: ./ )\n")
		self.out.write("-d data (sinogram)  folder name (default: sinograms ) to be added to input path\n")
		self.out.write("-o output folder name (default: reconstruction)\n")
		self.out.write("-w width (default 4008)\n")
		self.out.write("-l length (height) of image(default 2672)\n")
		self.out.write("-F number of first slice (default 0)\n")
		self.out.write("-L number of last slice (default = length)\n")
		self.out.write("-S slices to skip (default = 1)\n")
		self.out.write("-n number of chunks (default 16)\n")
		self.out.write("-J job name\n")
		self.out.write("-j suffix of job name\n")
		self.out.write("-Z timeout (default %i)\n"%self.lt)
		self.out.write("-z check interval (default %i)\n"%self.interval)
		self.out.write("-Q queue (default medium.q)\n")
		self.out.write("-U Unique ID (default use PID)\n")
		self.out.write("-v Verbose messages\n")
		self.out.write ("-t do nothing. Just create the bash script file\n")
	
	
	def parseOptions(self , argv, out, err):
		
		self.argv=argv
		self.out=out
		self.err=err
		if (len(self.argv)<2):
			self.hflag=1
		
		try:
			opts, args=getopt.gnu_getopt(self.argv[1:], "H:C:U:O:I:J:Z:d:b:dS:F:hi:j:l:L:n:o:p:s:vw:xz:Q:t", "qsub_project=")
		except getopt.GetoptError, err:
			self.errprint ("Option parsing error")
			self.errprint ("Command line values: %s"%(self.argv[1:]))
			self.errprint("Message is %s"%(str(err)))
			self.usage()
			raise Exception("Invalid usage")
		
		for o, a in opts:
			#set the queue
			if o=="-Q":
				self.myqueue=a
			#set the input folder
			elif o=="-i":
				self.indir=a
				self.inflag=True
			elif o=="-d":
				self.sinofolder=a
			#set the input settings file
			elif o=="-I":
				self.xmlinfile=a
			#set the output folder base
			elif o=="-o":
				self.obasefolder=a
			#set the length of a chunk
			elif o=="-l":
				self.imagelength=int(a)
			#set the image width
			elif o=="-w":
				self.wd=int(a)
			#set the number of bytes per pixel
			elif o=="-b":
				self.bytes=int(a)
			#set the job prefix
			elif o=="-J":
				self.jobbasename="%s"%a
			#get the job to hold for
			elif o=="-H":
				self.holdname="%s"%a
				self.holdflag=1
				self.holdstring="-hold_jid" 
			#set a unique id to help stream the queue job
			elif o=="-U":
					self.uniqueid="%s"%a
					self.uniqueflag=True
			#set a job suffix
			elif o=="-j":
				self.jobsuffix="_%s"%a 
				#set a number of scan segments 
			elif o=="-s":
				self.nsegs=int(a)
				#set number of images per scan segment
			elif o=="-p":
				self.nperseg=int(a)
			elif o=="-n":
				self.nchunks=int(a)
				#set number of last chunk to process
			elif o=="-L":
				self.lastslice=int(a)
				self.lastsliceflag=True
				#set the centre found by the user
			elif o=="-C":
				self.centre=float(a)
				self.centreflag=True
				#set number of first chunk to proces
			elif o=="-F":
				self.firstslice=int(a)
			elif o=="-S":
				self.slicestep=int(a)
				#usage message
			elif o=="-h":
				self.hflag=1
			elif o=="-d":
				self.delflag=True
			
			#set time-out options
			elif o=="-Z":
				self.lt=int(a)
			elif o=="-z":
				self.interval=int(a)
			
			#set verbosity
			elif o=="-v":
				self.vflag=True
			elif o=="-t":
				self.testing=True
			elif o=="--qsub_project":
				self.qsub_project=a
			
			else:
				self.errprint ("Ignored option")
				self.errprint ("option %s value %s"%(o, a))

		if (len(self.argv)==2 and self.testing):
			self.hflag=1

	def errprint(self, message="none"):
		self.out.write ("Error: %s\n"%(message))

	def vprint(self, message):
		if self.vflag:
			self.out.write (message)

	def getArch(self):
		if  self.testing or (platform.architecture()[0]=="64bit")  :
			return "amd64"
		return "x86"
	
	def PopenWait(self, args, env):
		for e in args:
			self.out.write("arg "+`e`+"\n")
		#for k, v in env.iteritems():
		#	self.out.write("env "+`k`+"="+`v`+"\n")
		executable=args[0]
		#args = args[1:]
		if self.testing:
			executable="test_program.py"
		self.out.write("Using exe = %s\n"%executable)
		p=subprocess.Popen(args, executable=executable, env=env, stdout=subprocess.PIPE, stderr=subprocess.PIPE)
		p.wait()
		(out, err)=p.communicate()
		self.out.write ("return value was %s\n"%p.returncode)
		id=out.split()[2].strip()
		#id=id.split('.')[0]
		#print id
		self.jobID.append(id.split('.')[0])
		
		if len(out)>0:
			self.out.write (out+"\n")
		if len(err)>0:
			self.err.write (err+"\n")
	
	
	def createReconScript(self):
		reconprogram="dt64n"
		if self.testing:
			reconprogram="test_program.py"
		self.myscript="%s/reconstruct.qsh"%self.mypath
		
		self.vprint("\nCreating the queue script: %s\n"%self.myscript)
		self.vprint("Using: %s\n"%reconprogram)
		
		
		myscriptOut=open(self.myscript, "w")
		
		try:
			#output the introductory script section
			myscriptOut.write("""\
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
			myscriptOut.write("""\
mypath=%s
myxmlpath=$mypath/$myxmlfile
"""		%self.mypath)
			
			#section to implement the cuda locking
			myscriptOut.write("""\
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
          echo "host $HOSTNAME date $mydate job $myjob task $tasknum">>$mylockf
          echo "Got lock on $mylockf"
          break
       else
          echo "Did not get lock on $mylockf"
       fi
   done
    if [[ $cudafound -eq 0 ]]
    then
       ((tries++))
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
			
			#assemble the command line that actually does the task
			myscriptOut.write("""\
%s $myxmlpath
"""		%reconprogram)
		
		finally:
			#end of the queue script text
			myscriptOut.flush()
			myscriptOut.close()




	def submitReconScript(self):
		#set the queue environment
		if self.testing:
			qenviron={}
		else:
			qenviron=os.environ
			
		qenviron["SGE_CELL"]="DLS"
		qenviron["SGE_EXECD_PORT"]="60001"
		qenviron["SGE_QMASTER_PORT"]="60000"
		qenviron["SGE_ROOT"]="/dls_sw/apps/sge/SGE6.2"
		oldpath=""
		try :
			oldpath=qenviron["PATH"]
		except :
			oldpath=""
		qenviron["PATH"]="/dls_sw/apps/sge/SGE6.2/bin/lx24-"+self.getArch()+":/bin:/usr/bin:"+oldpath

		self.out.write ("\nSpawning the reconstruction job ...\n")
		
		args=["qsub"]
		if self.qsub_project!=None:
			#add qsub project
			args+=[ "-P", self.qsub_project]
		if self.mypath!=None:
			#add error stream
			#args+=["-e", self.mypath+os.sep+'sge_err.txt']
			args+=["-e", self.mypath]
			#add output stream
			#args+=["-o", self.mypath+os.sep+'sge_out.txt']
			args+=["-o", self.mypath]
		if self.myqueue!=None:
			#add queue
			args+=["-q", self.myqueue]
		if self.jobname!=None:
			#add jobname
			args+=["-N", self.jobname]

		if (self.holdflag==1):
			#wait for previous job
			#args += ["-hold_jid", self.holdname]
			args+=[self.holdstring, self.holdname]

		#execute job in current working directory
		args+=["-cwd"]
		if self.qsub_project == "i12":
			#args+=[ "-pe", "smp", "4"]
			args += [ "-l", "gpus=1"]
		else:
			#i13
			args+=[ "-l", "tesla64", "-pe", "smp", "6"]
		args+=[ "-t", "%i-%i"%(self.firstchunk, self.nchunks)]

		#script
		args+=[self.myscript ]

		
		try:
			self.PopenWait(args, env=qenviron)
		except Exception, ex:
			raise Exception ("ERROR Spawning the reconstruction job didn't work "+str(ex))
	
	
	def run(self):
		
		if self.hflag:
			self.usage()
			return
		
		if not self.testing:
			self.out.write(self.copyright)
		
		if self.testing:
			self.uniqueid="testing_uid"
		
		#check input folder for the project
		if self.indir!=".":
			self.out.write ("Checking for input directory %s \nTimeout: %i seconds\n"%(self.indir, self.lt))
			if not folderExistsWithTimeOut(self.indir, self.lt, self.interval, self.out):
				msg="Input directory %s is not available after %i seconds!"%(self.indir, self.lt)
				self.errprint (msg)
				raise Exception(msg)
			
			self.out.write ("Input directory: %s found\n"%self.indir)
		else:
			indir=os.getcwd()
			self.out.write ("Input directory is cwd: %s \n"%indir)
		self.out.write ("Input directory: %s found\n"%self.indir)
		
		#set up the paths and create if necessary
		self.myfolder="r_%s_files"%self.uniqueid
		self.imfolder="r_%s_images"%self.uniqueid
		
		self.mypath="%s/%s"%(self.obasefolder, self.myfolder)
		self.impath="%s/%s"%(self.obasefolder, self.imfolder)
		self.jobname="%s%s"%(self.jobbasename, self.jobsuffix)
		
		self.sinopath="%s/%s"%(self.indir, self.sinofolder)
		self.darkpath="%s/dark"%(self.indir)
		self.flatpath="%s/flat"%(self.indir)
		
		if not self.lastsliceflag:
			self.lastslice=self.imagelength
		
		if not self.centreflag:
			msg="User  (or Controlling script) must specify the centre of reconstruction"
			self.errprint (msg)
			raise Exception(msg)
		
		if self.vflag:
			self.dumpvalues()
		
		
		#locate or create the folders
		if not(os.access (self.obasefolder, os.F_OK)):
			self.out.write ("creating %s\n"%self.obasefolder)
			os.mkdir(self.obasefolder)
		if not(os.access (self.mypath, os.F_OK)):
			self.out.write ("creating %s\n"%self.mypath)
			os.mkdir(self.mypath)
		if not(os.access (self.impath, os.F_OK)):
			self.out.write ("creating %s\n"%self.impath)
			os.mkdir(self.impath)
		
		
		self.out.write("\nUsing sinogram data in: %s\n"%self.sinopath)
		self.out.write("Using flat field data in: %s\n"%self.flatpath)
		self.out.write("Using dark field data in: %s\n\n"%self.darkpath)
		
		self.out.write("Output parameter and log files will be created in: %s\n"%self.mypath)
		self.out.write("Output images will be created in: %s\n"%self.impath)
		
		nslices=self.lastslice-self.firstslice
		chunksize=nslices/self.nchunks
		
		if self.vflag:
			self.out.write("\nnslices = %i\n"%nslices)
			self.out.write("chunksize = %i\n"%chunksize)

		# parse XML file containg reconstruction settings
		mydoc=parse(self.xmlinfile)
		
		#set the input folder and path for this job
		bptag=mydoc.getElementsByTagName("InputData")
		
		valtag=bptag[0].getElementsByTagName("Folder")
		valtag[0].childNodes[0].data=self.sinopath
		
		valtag=bptag[0].getElementsByTagName("Prefix")
		valtag[0].childNodes[0].data="sino_"
		
		#set the output folder and path for this job
		bptag=mydoc.getElementsByTagName("OutputData")
		
		valtag=bptag[0].getElementsByTagName("Folder")
		valtag[0].childNodes[0].data=self.impath
		
		valtag=bptag[0].getElementsByTagName("Prefix")
		valtag[0].childNodes[0].data="image_"
		
		bptag=mydoc.getElementsByTagName("FlatDarkFields")
		midtag=bptag[0].getElementsByTagName("FlatField")
		valtag=midtag[0].getElementsByTagName("FileBefore")
		valtag[0].childNodes[0].data="%s/flat.tif"%self.flatpath
		
		midtag=bptag[0].getElementsByTagName("DarkField")
		valtag=midtag[0].getElementsByTagName("FileBefore")
		valtag[0].childNodes[0].data="%s/dark.tif"%self.darkpath
		
		bptag=mydoc.getElementsByTagName("Backprojection")
		
		valtag=bptag[0].getElementsByTagName("ImageCentre")
		valtag[0].childNodes[0].data=self.centre
		
		mycpu=0
		
		
		settingsUsedXMLFile=open("settings_used.xml", "w")
		try:
			settingsUsedXMLFile.write(mydoc.toxml())
		finally:
			settingsUsedXMLFile.flush()
			settingsUsedXMLFile.close()
		
		
		
		clistFile=open("%s/chunklist.txt"%self.mypath, "w")
		
		try:
			for chunk in range(0, self.nchunks):
				
				sgetask=chunk+1
				chunkstart=self.firstslice+(chunk*chunksize)
				chunkend=(chunkstart+chunksize-1) 
				if (chunk==self.nchunks-1):
					chunkend=self.lastslice-1
				if (self.vflag):
					#print("chunk %i sge-task %i start %i end %i num %i")%(chunk,sgetask,chunkstart,chunkend,(1+chunkend-chunkstart))
					self.out.write("\nchunk %i sge-task %i start %i end %i num %i"%(chunk, sgetask, chunkstart, chunkend, (1+chunkend-chunkstart)))
				clistFile.write("chunk %i sge-task %i start %i end %i num %i"%(chunk, sgetask, chunkstart, chunkend, (1+chunkend-chunkstart)))
				
				bptag=mydoc.getElementsByTagName("InputData")
				
				valtag=bptag[0].getElementsByTagName("FileFirst")
				valtag[0].childNodes[0].data=chunkstart
				valtag=bptag[0].getElementsByTagName("FileLast")
				valtag[0].childNodes[0].data=chunkend
				valtag=bptag[0].getElementsByTagName("FileStep")
				valtag[0].childNodes[0].data=self.slicestep
				
				bptag=mydoc.getElementsByTagName("LogFile")
				bptag[0].childNodes[0].data="%s/log_%03i.xml"%(self.mypath, sgetask)
				
				#use the task number for file name to simplify queue script
				
				chunkXMLFile=open("%s/chunk_%03d.xml"%(self.mypath, sgetask), "w")
				try:
					chunkXMLFile.write(mydoc.toxml())
				finally:
					chunkXMLFile.flush()
					chunkXMLFile.close()
		
		finally:
			clistFile.flush()
			clistFile.close()
		
		
		
		
		self.createReconScript()
		self.submitReconScript()
		
		recon_success=self.monitorQueueJobs()
		if recon_success:
			print 'All reconstruction jobs appear to have completed successfully.'
		else:
			print 'This monitoring of reconstruction jobs has timed out: please continue monitoring the status of these jobs using qstat.'
		return recon_success, self.imfolder
	
	
	def dumpvalues(self):
		
		self.out.write("******************* Values used are ********************** \n")
		self.out.write("obasefolder: %s\n"%self.obasefolder)
		self.out.write("indir: %s\n"%self.indir)
		self.out.write("xmlinfile: %s\n\n"%self.xmlinfile)
		
		self.out.write("width wd: %i\n"%self.wd)
		self.out.write("imagelength: %i\n"%self.imagelength)
		self.out.write("bytes: %i\n"%self.bytes)
		self.out.write("nchunks: %i\n"%self.nchunks)
		self.out.write("nsegs: %i\n"%self.nsegs)
		self.out.write("nperseg: %s\n"%self.nperseg)
		self.out.write("jobbasename: %s\n"%self.jobbasename)
		self.out.write("jobname: %s\n"%self.jobname)
		self.out.write("existflag: %s\n"%self.existflag)
		self.out.write("jobsuffix: %s\n"%self.jobsuffix)
		self.out.write("myqueue: %s\n"%self.myqueue)
		self.out.write("uniqueid: %s\n\n"%self.uniqueid)
		
		self.out.write("centre: %i\n\n"%self.centre)
		
		self.out.write("myfolder: %s\n"%self.myfolder)
		self.out.write("imfolder: %s\n"%self.imfolder)
		self.out.write("mypath: %s\n"%self.mypath)
		self.out.write("impath: %s\n"%self.impath)

	def monitorQueueJobs(self, nSec=5, totWait=60*45):
		jobStateIdx=4
		waiting=True
		
		print '\nMonitoring recon_arrayxml-jobs with qstat:'
		print "Time (sec)\t"+"\t".join(self.jobID)
		time_passed=0
		bTimeOut=(time_passed>totWait)
		while waiting and (not bTimeOut):
			waiting=False
			#cmd0='module load global/cluster >/dev/null 2>&1'
			cmd="qstat"
			if self.testing:
				cmd="module load global/cluster >/dev/null 2>&1; qstat"
			
			proc=subprocess.Popen([cmd], shell=True, stdout=subprocess.PIPE)
			proc.wait()
			stat=proc.communicate()[0]
			values=stat.split("\n")
			#print type(values)
			#print len(values)
			#for i in range(0, len(values)):
			#	print i, values[i]
				
			status={}
			for id in self.jobID:
				status[id]='unknown'
			#print 'status dct at top...'
			#print status
			for line in values[2:]:
				#print 'sioux'
				#print line
				split=line.split()
				#print 'split'
				#print split
				#print len(split)
				if(len(split)>jobStateIdx):
					lineJobID=split[0].strip()
					#print 'lineJobID:'
					#print lineJobID
					lineJobState=split[jobStateIdx].strip()
					#print 'lineJobState:'
					#print lineJobState
					lineTaskID=split[len(split)-1].strip()
					#print 'lineTaskID:'
					#print lineTaskID
					for id in self.jobID:
						if id==lineJobID:
							if status[id]=='unknown':
								status[id]=''
							status[id]+=lineTaskID+':'+lineJobState+' '
		
			#print 'status dct at bottom:'
			#print status
			
			stats=[]
			for id in self.jobID:
				if status[id]=='unknown':
					stats.append('completed')
				else:
					stats.append(status[id])
					waiting=True
			
			#print out whole line of stats
			print ("%i\t"%(time_passed))+"\t".join(stats)
		
			#advance time
			#print 'adding n sleeping '
			time_passed+=nSec
			time.sleep(nSec)
			bTimeOut=(time_passed>totWait)
			
		#outside while-loop
		if bTimeOut:
			print "Timed-out after approx %s sec"%totWait
		
		return (not waiting)
if __name__=="__main__":
	main(sys.argv)



import math
import unittest
import os
import shutil
class Test1(unittest.TestCase):
	def setUp(self):
		self.obj_to_test="recon_arrayxml"
		# dirname_expected_output needs to be manually created and populated 
		# with files with expected content prior to running tests
		self.dirname_expected_output="utest_expected_output_from_"+self.obj_to_test 
		dirname=self.dirname_expected_output
		if not os.path.exists(dirname):
			os.mkdir(dirname)
		
		emptyFilename="%s/empty.txt"%self.dirname_expected_output 
		if not os.path.exists(emptyFilename):
			open(emptyFilename, "w").close()
		
		usageFilename="%s/expected_usage.txt"%self.dirname_expected_output 
		if not os.path.exists(usageFilename):
			open(usageFilename, "w").close()
		
		self.dirname_actual_output="utest_actual_output_from_"+self.obj_to_test 
		dirname=self.dirname_actual_output
		if not os.path.exists(dirname):
			os.mkdir(dirname)
	
	def tearDown(self):
		pass
	
	def test_option_null_arg_null(self):
		(out, err, errFileName, outputFileName)=self.outAndErr("test_option_null_arg_null")
		main(["program"], out=out , err=err)
		out.close()
		err.close()
		self.checkFilesMatch("expected_usage.txt", outputFileName)
		self.checkFilesMatch("empty.txt" , errFileName)
	
	def test_option_t(self):
		#test testing mode 
		(out, err, errFileName, outputFileName)=self.outAndErr("test_option_t")
		main(["program", "-t"], out=out , err=err)
		out.close()
		err.close()
		self.checkFilesMatch("expected_usage.txt", outputFileName)
		self.checkFilesMatch("empty.txt", errFileName)
	
	def test_option_h(self):
		(out, err, errFileName, outputFileName)=self.outAndErr("test_option_h")
		main(["program", "-h"], out=out , err=err)
		out.close()
		err.close()
		self.checkFilesMatch("expected_usage.txt", outputFileName)
		self.checkFilesMatch("empty.txt", errFileName)

	def test_option_i_arg_missing(self):
		(out, err, errFileName, outputFileName)=self.outAndErr("test_option_i_arg_missing")
		try:
			main(["program", "-i"], out=out , err=err)
		except Exception, ex:
			self.assertEquals('Invalid usage', str(ex))
		out.close()
		err.close()
		self.checkFilesMatch(outputFileName, outputFileName)
		self.checkFilesMatch("empty.txt", errFileName)

	def test_option_i_arg_nonexistentdir(self):
		(out, err, errFileName, outputFileName)=self.outAndErr("test_option_i_arg_nonexistentdir")
		try:
			main(["program", "-i", "nonexistentdir", "-C", "0.0", "-Z", "2", "-t", "-v"], out=out, err=err)
		except Exception, ex:
			self.assertEquals('Input directory nonexistentdir is not available after 2 seconds!', str(ex))
		finally:
			out.close()
			err.close()
	
	def test_option_i_arg_cwd(self):
		defaultdir='reconstruction'
		outdir_actual=self.dirname_actual_output+'/'+defaultdir
		if os.path.exists(outdir_actual):
			savedpath=os.getcwd()
			os.chdir(self.dirname_actual_output)
			shutil.rmtree(defaultdir)
			# go back
			os.chdir(savedpath)
			
		os.makedirs(outdir_actual)
		
		(out, err, errFileName, outputFileName)=self.outAndErr("test_option_i_arg_cwd")
		main(\
			["program"
			, "-i", "."
			, "-o", outdir_actual
			, "-C", "0.0"
			, "-t"
			, "-v"
			, "--qsub_project", "i13"\
			]
			, out=out , err=err)
		out.close()
		err.close()
		self.checkFilesMatch(outputFileName, outputFileName)
		self.checkFilesMatch("empty.txt", errFileName)
		
		reconscriptFilename="reconstruct.qsh"
		reconscriptDir_actual=outdir_actual+"/r_testing_uid_files"
		reconscriptFile_actual=reconscriptDir_actual+'/'+reconscriptFilename
		
		reconscriptDir_expected=self.dirname_expected_output+'/'+defaultdir+'/'+"r_testing_uid_files"
		reconscriptFile_expected=reconscriptDir_expected+'/'+reconscriptFilename
		
		if not os.path.exists(reconscriptDir_expected):
			os.makedirs(reconscriptDir_expected)
		
		# it's handy to create an empty expected-output file if it doesn't exist (and then manually fill in with some desired content, 
		# matching the conetnt of the actual-output  file 
		if not os.path.exists(reconscriptFile_expected):
			open(reconscriptFile_expected, "w").close()
		
		self.checkFilesMatchGivenPaths(reconscriptFile_expected, reconscriptFile_actual)
	
	
	
	def outAndErr(self, testName):
		outputFileName=testName+"_output.txt"
		errFileName=testName+"_err.txt"
		out=open(self.dirname_actual_output+'/'+outputFileName, "w")
		err=open(self.dirname_actual_output+'/'+errFileName, "w")
		return (out, err, errFileName, outputFileName)

	def checkFilesMatch(self, expectedFilePath, actualFilePath):
		expectedFilePath=self.dirname_expected_output+'/'+expectedFilePath
		actualFilePath=self.dirname_actual_output+'/'+actualFilePath
		self.checkFilesMatchGivenPaths(expectedFilePath, actualFilePath)
	
	def checkFilesMatchGivenPaths(self, expectedFilePath, actualFilePath):
		f=open(actualFilePath)
		linesActual=f.readlines()
		f.close()
		f=open(expectedFilePath)
		linesExpected=f.readlines()
		f.close()
		lastIndexToCompare=min(len(linesActual), len(linesExpected))
		for i in range(lastIndexToCompare):
			if linesActual[i]!=linesExpected[i]:
				raise Exception("Line %i in file %s does not match the corresponding line in file %s "%(i, expectedFilePath, actualFilePath))
		if len(linesExpected)==len(linesActual):
			return
		raise Exception("A line in file %s does not match the corresponding line in file %s "%(expectedFilePath, actualFilePath))
	
	
	
class writer_newline:
	def __init__(self, pipe):
		self.pipe=pipe
	def write(self, msg):
		self.pipe.write(str(msg))
		self.pipe.write("\n")
		self.pipe.flush()
	
def suite():
	return unittest.TestSuite((unittest.TestLoader().loadTestsFromTestCase(Test1)))
