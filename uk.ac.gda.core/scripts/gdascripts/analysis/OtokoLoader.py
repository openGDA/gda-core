from gda.analysis.io import IFileLoader
from uk.ac.diamond.scisoft.analysis.io import DataHolder
from org.eclipse.january.dataset import DoubleDataset, DatasetFactory
from struct import unpack
import re
import os

class OtokoLoader(IFileLoader):
	'''class to populate a ScanFileHolder with the contents of a set of Otoko files'''
	
	def __init__(self, filename):
		self.frames=0
		self.detectors={}
		self.filename=filename
	
	def loadFile(self):
		self.frames=0
		self.detectors={}
		self.result=DataHolder()

		self.path=os.path.dirname(self.filename)
		fname=os.path.basename(self.filename)
		ftohf=re.compile('[0-9]\.')
		hfname=self.path+"/"+ftohf.sub('0.',fname)
		# read header file
		hfile=open(hfname, 'r')
		lines=hfile.readlines()
		hfile.close()

		# file format
		# my comments: # file contents: ##
		# ignore two header lines
		## Created at DLS-I22 on Wednesday 6/8/08 at 16:57:16
		## AG Beh 3m camera 15keV (title)
		# saxs 1
		## x y frames 7x something
		## saxfile
		#calib 2
		## frames channels 1 7x something
		## calibfile
		# waxs 3
		## x frames y=1 7x something
		## waxsfile
		# timing 4
		## frames 4 1 7x something
		## timingfile
		# comments
		## cal chan 1 src: Timer (10ns)
		## cal chan 2 src: BS PD
		## cal chan 3 src: 
		## cal chan 4 src: 
		## cal chan 5 src: 
		## cal chan 6 src: 
		## cal chan 7 src: 
		## cal chan 8 src: 
		## cal chan 9 src: Diode in beamstop

		i=2
		while (len(lines)>i):
			# stop if we reached the comments (second test should never be true with an intact file)
			if (lines[i][0]=='#'):
				break
			if (lines[i+1][0]=='#'):
				break
			dfilename=lines[i+1].strip()
			info=lines[i]

			# the file section
			s=int(dfilename[5])

			if (s==1):
				saxs=self.parseitem("saxs", dfilename, info)
				saxs["x"]=int(saxs["info"][0])
				saxs["y"]=int(saxs["info"][1])
				saxs["frames"]=int(saxs["info"][2])
				saxs["endian"]=int(saxs["info"][3])
				self.reindet(saxs)
			elif (s==2):
				cali=self.parseitem("cali", dfilename, info)
				cali["x"]=int(cali["info"][1])
				cali["y"]=1
				cali["frames"]=int(cali["info"][0])
				cali["endian"]=int(cali["info"][3])
				self.reincal(cali)
			elif (s==3):
				waxs=self.parseitem("waxs", dfilename, info)
				waxs["x"]=int(waxs["info"][0])
				waxs["y"]=1
				waxs["frames"]=int(waxs["info"][1])
				waxs["endian"]=int(waxs["info"][3])
				self.reindet(waxs)
			elif (s==4):
				time=self.parseitem("time", dfilename, info)
				time["x"]=1
				time["y"]=1
				time["frames"]=int(time["info"][0])
				time["endian"]=int(time["info"][3])
				self.reincal(time)
			else:
				print "unknown detector recorded in "+dfilename
			i=i+2

		return self.result

	def parseitem(self, name, filename, line):
		result = {"name": name}
		result["info"]= line.strip().split()
		result["filename"] = self.path+"/"+filename
		return result
		
	def reindet(self, thing):
		""" read in detector data you want to view per frame """
		try:
			#print "opening x"+thing["filename"]+"x"
			file=open(thing["filename"],'rb')
			size=thing["x"]*thing["y"]
			if (thing["endian"]==0):
				# default big endian/motorola
				endstr=">"
			else:
				# little endian/intel
				endstr="<"
			for i in range(thing["frames"]):
				list=unpack(endstr+size.__str__()+'f', file.read(size*4))
				ds = DatasetFactory.createFromObject(list)
				
				if thing["x"] > 1 and thing["y"] > 1:
					ds.setShape(thing["x"], thing["y"])
				ds.setName(os.path.basename(thing["filename"])+" frame "+i.__str__())
				self.result.addDataSet(thing["name"]+i.__str__(), ds)
			file.close()
			self.detectors[thing["name"]]=thing["frames"]
		except IOError, message:
			print "Warning: Could not read (all of) the "+thing["name"]+" data: "+message.__str__()

		
	def reincal(self, thing):
		""" read in calibration data you want to view per channel for all frames """
		try:
			#print "opening x"+thing["filename"]+"x"
			file=open(thing["filename"],'rb')
			size=thing["x"]*thing["frames"]
			if (thing["endian"]==0):
				# default big endian/motorola
				endstr=">"
			else:
				# little endian/intel
				endstr="<"
			list=unpack(endstr+size.__str__()+'f', file.read(size*4))
			ds = DatasetFactory.createFromObject(list)
			if thing["x"] > 1 and thing["frames"] > 1:
				ds.setShape(thing["x"], thing["frames"])
			ds.setName(os.path.basename(thing["filename"])+" all frames")
			self.result.addDataSet(thing["name"], ds)
			file.close()
			self.detectors[thing["name"]]=thing["frames"]
		except IOError, message:
			print "Warning: Could not read (all of) the "+thing["name"]+" data: "+message.__str__()
	def saveFile(self, sfh, name):
		print "ERROR: saving in Otoko-Format is not supported (by this class)"

	def getDetectors(self):
		return self.detectors.keys()
		
	def getFrames(self):
		return self.detectors.values()[0]
		
# quick and dirty way (not using this class):

	#from struct import *
	#file=open("/dls/i22/data/2008/sm580-1/processing/bsa/C01001.728",mode='rb')
	#list=unpack('>262144f', file.read(262144*4))
	#ds=DataSet(512,512,list)
	#Plotter.plotImage("Data Vector",ds)

# proper way:

	#s=ScanFileHolder()
	## example files /home/zjt21856/workspace/gda-trunk/users/data/B09000.806"
	## 		 /dls/i22/data/2008/sm0/B09000.806
	#s.load(OtokoLoader(), "filename")
	#s.info()
	#Plotter.plotImage("Data Vector", s[0])
