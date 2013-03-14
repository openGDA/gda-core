from gda.analysis.io import SRSLoader, NexusLoader
from gda.configuration.properties import LocalProperties
from gda.data import PathConstructor, NumTracker
from gda.data.metadata import GDAMetadataProvider
from uk.ac.diamond.scisoft.analysis.io import LoaderFactory
import os.path
class ScanFileLoader:



	def __init__(self, filespec=None, dir=None):
		
		self.format = LocalProperties.get("gda.data.scan.datawriter.dataFormat")
		if dir!=None and dir != "None":
			self.dir=dir
		else:
			self.dir = PathConstructor.createFromDefaultProperty()
		self.beamline = GDAMetadataProvider.getInstance().getMetadataValue("instrument", "gda.instrument", "tmp")
		if (self.format == "NexusDataWriter"):
			#really should use scanFileName prefix rather than forcing to beamline-
#			self.prefix = self.beamline+"-"
#			if self.prefix == "tmp-":
			self.prefix=""
			self.ext = "nxs"
			self.loader = NexusLoader
			if filespec==None:
				filespec = "%d" % NumTracker(self.beamline).currentFileNumber
			
		else:
			self.prefix = ""
			self.ext = "dat"
			self.loader = SRSLoader
			if filespec==None:
				filespec = "%d" % NumTracker().currentFileNumber
		
		filespec = filespec.__str__()
		
		self.filename = self.tryFiles([ filespec, self.dir+"/"+filespec, self.prefix+filespec+"."+self.ext, self.dir+"/"+self.prefix+filespec+"."+self.ext ])
		
		if (self.filename == None):
			raise Exception("no file found for %s" % filespec)

	def tryFiles(self, filespeclist):
		for filespec in filespeclist:
			print "Looking for " +filespec
			if os.path.isfile(filespec):	
				print "Found file :" + filespec
				return filespec
		return None
			
	def getSFH(self):
		return LoaderFactory.getData(self.filename, None);
	
	def setScanDir(self, dir):
		self.dir = dir
