#@PydevCodeAnalysisIgnore
# setup CLASSPATH as for gda 
# run using command :
# jython ODCCDControllerTest.py
import unittest
import re
from gda.device.detector.odccd import ODCCDController;

class Item:
	def __init__(self, name):
		self.name = name
		self.type = None
		self.dimension=None
		self.value = None
	
	def initFromDescription(self, description):
		self.type = description[0:description.find("[")]
		self.dimension=description[description.find("[")+1:description.find("]")]
		self.value = description[description.find("=")+1:]
		
	def __str__(self):
		return self.name + ":" + `self.type` + `self.dimension` + " = " + `self.value`

def MakeItem(name, type, dimension, value):
	i = Item(name)
	i.type=type
	i.dimension=dimension
	i.value=value
	return i		

class Folder:
	def extractFolders(self, ls, controller):
		folders = {}
		lines =  ls.splitlines()
		prefix = "api:[+] ("
		for line in lines:
			start_api = line.find(prefix)
			if start_api >= 0:
				name = line[start_api+len(prefix):line.rfind(")")]
				f = Folder(self.target, name)
				f.initFromController(controller)
				folders[name] = f
		return folders

	def extractItems(self, ls, controller):
		items = {}
		lines =  ls.splitlines()
		prefix = "api:    ("
		for line in lines:
			start_api = line.find(prefix)
			if start_api >= 0:
				colon=line.rfind(":")
				name = line[start_api+len(prefix):line.rfind(")")]
				i = Item(name)
				i.initFromDescription(line[colon+1:])
				items[name] = i
		return items
	
	def initFromController(self,controller):	
		print self.target
		controller.runScript("db ls -v " + self.target + ";");
		controller.readInputUntil("api:(" + self.target + ")");
		ls = controller.readInputUntil("api:End of list.");		
#		print ls
		self.subfolders = self.extractFolders(ls, controller)
		self.items = self.extractItems(ls, controller)
		
	def __init__(self, parent , name):
		self.name = name
		self.target = parent + "/" + name
		self.subfolders = None
		self.items = None

	def dump(self):
		print "Folder name = " + self.name
		print "Target = " + self.target + " contains:"
		for item in self.items:
			print "Item " + str(self.items[item])
		for f in self.subfolders:
			self.subfolders[f].dump()
	

class Run:
	floatValueNames=["domegaindeg","ddetectorindeg","dkappaindeg","dphiindeg",
			    "dscanstartindeg","dscanendindeg","dscanwidthindeg","dscanspeedratio",
			    "dexposuretimeinsec"]	
	intValueNames=["inum","irunscantype","dwnumofframes","dwnumofframesdone"]	
	def __init__(self, runFolder):
		self.values={}
		self.name = runFolder.name
		for name in Run.floatValueNames:
			self.values[name]=float(runFolder.items[name].value)
		for name in Run.intValueNames:
			self.values[name]=int(float(runFolder.items[name].value))

	def getScantype(self):
		if( self.values["irunscantype"] == 0):
			return "Omega"
		if( self.values["irunscantype"] == 4):
			return "Phi"
		raise "Unknown scantype = " + `self.values["irunscantype"]`
	def dump(self):
		print "Run " + self.name
		for value in self.values:
			print value + " = " + `self.values[value]`

	def dumpRun(self):
		print "ScanType = " + self.getScantype()
		print "Scan from " + `self.values["dscanstartindeg"]` 
		print "Step size = " + `self.values["dscanwidthindeg"]`
		print "Exposure time = " + `self.values["dexposuretimeinsec"]`
		print "No. steps = " + `self.values["dwnumofframes"]`
		print "Phi = " + `self.values["dphiindeg"]`
		print "Detector = " + `self.values["ddetectorindeg"]`
		print "Omega = " + `self.values["domegaindeg"]`
		print "Kappa = " + `self.values["dkappaindeg"]`

		
class Info:
	intValueNames=["dwtotalnumofframes","wreferenceframefrequency","wversioninfo","inumofruns","wisreferenceframes",
			    "inumofreferenceruns",]	
	strValueNames=["cexperimentname","cexperimentdir"]	
	def __init__(self, infoFolder):
		self.values={}
		self.name = infoFolder.name
		for name in Info.strValueNames:
			self.values[name]=infoFolder.items[name].value
		for name in Info.intValueNames:
			self.values[name]=int(float(infoFolder.items[name].value))
			
	def dump(self):
		print "Info " + self.name
		for value in self.values:
			print value + " = " + `self.values[value]`
	
	def getExperimentName(self):
		return self.values["cexperimentname"]

class RunList:
	def __init__(self,folder):
		self.runs=[]
		for f in folder.subfolders:
			sf = folder.subfolders[f]
			if sf.name == "runs":
				for irun in range(0,len(sf.subfolders)):
					self.runs.append(Run(sf.subfolders["run"+`(irun+1)`]))
			elif sf.name == "info":
				self.info = Info(sf)

	def dump(self):
		self.info.dump()
		for irun in range(0,len(self.runs)):
			print "Run " + `(irun+1)`
			self.runs[irun].dump()

	def dumpRuns(self):
		print self.info.getExperimentName()
		print "Number of runs = " + `len( self.runs)`
		for irun in range(0,len(self.runs)):
			print "Run " + `(irun+1)`
			self.runs[irun].dumpRun()
			
				
class TestRuns: #(unittest.TestCase):	
	def setUp(self):
		pass
	def tearDown(self):
		pass
	
	def testRuns(self):
		folders={}
		run1 = Folder("runs","1")
		run1.items = {}
		valueNames=["inum","irunscantype","domegaindeg","ddetectorindeg","dkappaindeg","dphiindeg",
				    "dscanstartindeg","dscanendindeg","dscanwidthindeg","dscanspeedratio",
				    "dwnumofframes","dwnumofframesdone","dexposuretimeinsec"]
		for name in valueNames:
			run1.items[name] = MakeItem(name, "1","1,1",1)
		runsFolder = Folder("top","runs")
		runsFolder.subfolders = {}
		runsFolder.subfolders["run1"]=run1


		infoFolder = Folder("top","info")
		infoFolder.items={}
		valueNames=["cexperimentname","dwtotalnumofframes","wreferenceframefrequency","wversioninfo","inumofruns","wisreferenceframes",
			    "inumofreferenceruns","cexperimentdir"]	
		for name in Info.valueNames:
			infoFolder.items[name]=MakeItem(name,"1","test",2)

		topFolder = Folder("top","top")
		topFolder.subfolders = {}
		topFolder.subfolders["runs"]=runsFolder
	
		topFolder.subfolders["info"]=infoFolder

		runList = RunList(topFolder)
		runList.dump()
		
class RunListLoader:
	def getRunList(self, controller, runFile):
		root = "/root"
		folderName = "gda_run"
		target = root + "/" + folderName
		controller.runScript("call importRunList " + "\"" + runFile + "\" \"" + target + "\"");
		controller.readInputUntil("importRunList completed.");
		folder = Folder(root,folderName)
		folder.initFromController(controller)
#		folder.dump()
		runList = RunList(folder)
		return runList
		
				
class TestPosition(unittest.TestCase):
		
	def setUp(self):
		self.controller = ODCCDController()
		self.controller.connect("i15-control")

	def testRunListLoader(self):
		runList = RunListLoader().getRunList(self.controller,"C:/Data/Mark/garnet/garnet.run")
#		runList.dump()
		runList.dumpRuns()
		

	def tearDown(self):
		self.controller.disconnect()
	
if __name__ == '__main__':
	print "ODCCDCOntrollerTest"
#	suite = unittest.TestLoader().loadTestsFromTestCase(TestPosition)
	suite = unittest.TestLoader().loadTestsFromTestCase(TestRuns)
	unittest.TextTestRunner(verbosity=2).run(suite)