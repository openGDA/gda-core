#Use a tab-delimited text file containing all of the devices in the epics-gda interface and server_epics.xml file to generate an interface testing routine
import sys
import shutil

def quickListCheck(minimumParams, typeString, searchList, returnList): #returns isEmpty flag
	isEmpty=True
	for line in searchList:
		splitline=line.split("	") #requires tab-delimited file
		if len(splitline)<=minimumParams:
			continue
		type=splitline[4].strip()
		if type==typeString:
			returnList.append(splitline[1])
			if isEmpty==True:
				isEmpty=False
	return isEmpty

class monitorCheckGenerator:
	monitorList=[]
	isEmpty=True

	def parseCSVFile(self,file):
		text=[]
		fp=open(file)
		line=fp.readline()
		while line:
			text.append(line)
		self.parseCSVList(text)
	def parseCSVList(self,list):
		for line in list:
			splitline=line.split("	")
			if len(splitline)<=4:
				continue
			type=splitline[4].strip()
			if type=="EpicsMonitor":
				self.monitorList.append(splitline[1])
				if self.isEmpty==True:
					self.isEmpty=False
	def write(self,fileout):
		if self.isEmpty==False:
			print >>fileout,'def checkMonitors(command_server):'
			print >>fileout,'	ok=True'
			for monitor in self.monitorList:
				print>>fileout, '	ok=checkMonitor("%s") and ok' % monitor
			print >>fileout,'	return ok'
	def template(self):
		print '''def checkOE(oeName,expectedDOFNames):
	ok = True
	try:
		oe=finder.find(oeName)
		#oe = jythonNameMap.__getattr__(oeName)
		dofNames = oe.getDOFNames()
		if(len(dofNames) != len(expectedDOFNames)):
			print "Error - number of  dofs != numberExpected  for " + oe.getName()
			ok = False
		for dofName in expectedDOFNames:
			try:
				val = oe.getPosition(dofName)
				dof=facade.getFromJythonNamespace(dofName) #dof = jythonNameMap.__getattr__(dofName)
				dofval = dof()
				if( java.lang.Double.isNaN(dofval)):
					print oe.getName()+"."+dofName + " = NaN"
					ok = False
#				print dofName, val, dof()
			except:
				type, exception, traceback = sys.exc_info()
				handle_messages.log(None,"Error checking " + oe.getName()+"."+ dofName,
					type, exception, None, False)
				ok = False
	except:
		type, exception, traceback = sys.exc_info()
		handle_messages.log(None,"Error checking oe  " + oeName, type, exception, None, False)
		ok = False
	
	return ok
'''
	def writeCheckAll(self, fileout): #print out only if we need it
		if self.isEmpty==False:
			print >>fileout,'	ok=checkMonitors(command_server) and ok'
class motorCheckGenerator:
	oeList=[]
	dofList={}
	isEmpty=True
	def parseCSVFile(self,file):
		text=[]
		fp=open(file)
		line=fp.readline()
		while line:
			text.append(line)
		self.parseCSVList(text)
	def parseCSVList(self,list):
		for line in list:
			splitline=line.split("	")
			if len(splitline)<=4:
				continue
			type=splitline[4].strip()
			if type=="EpicsMotor":
				oeName=splitline[2].strip()
				dofName=splitline[3].strip()
				if len(oeName)==0 or len(dofName)==0:
					continue
				if self.oeList.count(oeName)==0:
					self.oeList.append(oeName)
					newList=[]
					newList.append(dofName)
					self.dofList[oeName]=newList
				else:
					tempDofNames=self.dofList[oeName]
					tempDofNames.append(dofName)
					self.dofList[oeName]=tempDofNames
				if self.isEmpty==True:
					self.isEmpty=False
	def write(self,fileout):
		if self.isEmpty==False:
			print>>fileout,'def checkOEs(command_server):'
			print>>fileout,'	ok=True'
			for oe in self.dofList.keys():
				outstring=""
				dofs=self.dofList[oe]
				outstring+='	ok=checkOE("%s",[' % oe
				for i in range(len(dofs)):
					if i>0:
						outstring+= ','
					outstring+= '"%s"'%dofs[i]
				outstring+= ']) and ok'
				print >>fileout,outstring
			print >>fileout,'	return ok'
	def writeCheckAll(self, fileout): #print out only if we need it
		if self.isEmpty==False:
			print >>fileout,'	ok=checkOEs(command_server) and ok'

class scannableCheckGenerator:
	def parseCSVList(self, list):
		pass
	def write(self, fileout):
		pass
class sampleChangerCheckGenerator:
	sampleChangerList=[]
	isEmpty=True
	def parseCSVList(self, list):
		returnList=[]
		isEmpty=quickListCheck(4,"JActorSampleChanger",list,returnList)
		self.isEmpty=isEmpty
		self.sampleChangerList=returnList
	def write(self, fileout):
		if self.isEmpty==False:
			print >>fileout,'def checkSampleChangers(command_server):'
			print >>fileout,'	ok=True'
			for changer in self.sampleChangerList:
				print >>fileout,'	ok=checkSampleChanger("%s") and ok' % changer
			print >>fileout,'	return ok'
	def writeCheckAll(self,fileout):
		if self.isEmpty==False:
			print >>fileout,'	ok=checkSampleChangers(command_server) and ok'
class mxDetectorCheckGenerator:
	detectorList=[]
	isEmpty=True
	def parseCSVList(self, list):
		returnList=[]
		returnList2=[]
		isEmpty1=quickListCheck(4,"MarCCDDetector",list,returnList)
		isEmpty2=quickListCheck(4,"ADSCDetector",list,returnList2)
		self.isEmpty=isEmpty1 & isEmpty2
		self.detectorList=returnList+returnList2
	def write(self, fileout):
		if self.isEmpty==False:
			print >>fileout,'def checkMXDetectors(command_server):'
			print >>fileout,'	ok=True'
			for detector in self.detectorList:
				print >>fileout,'	ok=checkMXDetector("%s") and ok' % detector
			print >>fileout,'	return ok'
	def writeCheckAll(self, fileout):
		if self.isEmpty==False:
			print >>fileout,'	ok=checkMXDetectors(command_server) and ok'
class bcmCheckGenerator:
	bcmList=[]
	isEmpty=True
	def parseCSVList(self, list):
		returnList=[]
		isEmpty=quickListCheck(4,"GDABCM",list,returnList)
		self.isEmpty=isEmpty
		self.bcmList=returnList
	def write(self, fileout):
		if self.isEmpty==False:
			print >>fileout,'def checkBcms(command_server):'
			print >>fileout,'	ok=True'
			for bcm in self.bcmList:
				print >>fileout,'	ok=checkBcm("%s") and ok' %bcm
			print >>fileout,'	return ok'
	def writeCheckAll(self,fileout):
		if self.isEmpty==False:
			print >>fileout,'	ok=checkBcms(command_server) and ok'
class mxCameraCheckGenerator:
	mxCameraList=[]
	isEmpty=True
	def parseCSVList(self, list):
		returnList=[]
		isEmpty=quickListCheck(4,"MXCameraForDummy",list,returnList)
		self.isEmpty=isEmpty
		self.mxCameraList=returnList
	def write(self, fileout):
		if self.isEmpty==False:
			print >>fileout,'def checkCameras(command_server):'
			print >>fileout,'	ok=True'
			for mxCamera in self.mxCameraList:
				print >>fileout,'	ok=checkCamera("%s") and ok' %mxCamera
			print >>fileout,'	return ok'
	def writeCheckAll(self,fileout):
		if self.isEmpty==False:
			print >>fileout,'	ok=checkCameras(command_server) and ok'
			
class opticalCameraCheckGenerator:
	optCamList=[]
	isEmpty=True
	def parseCSVList(self, list):
		returnList=[]
		isEmpty=quickListCheck(4,"RTPCamera",list,returnList)
		self.isEmpty=isEmpty
		self.optCamList=returnList
	def write(self, fileout):
		if self.isEmpty==False:
			print >>fileout,'def checkOpticalCameras(command_server):'
			print >>fileout,'	ok=True'
			for cam in self.optCamList:
				print >>fileout,'	ok=checkOpticalCamera("%s") and ok' %cam
			print >>fileout,'	return ok'
	def writeCheckAll(self,fileout):
		if self.isEmpty==False:
			print >>fileout,'	ok=checkOpticalCameras(command_server) and ok'
class fileHeaderCheckGenerator:
	fileHeaderList=[]
	isEmpty=True
	def parseCSVList(self, list):
		returnList=[]
		isEmpty=quickListCheck(4,"DefaultFileHeader",list,returnList)
		self.isEmpty=isEmpty
		self.fileHeaderList=returnList
	def write(self, fileout):
		if self.isEmpty==False:
			print >>fileout,'def checkFileHeaders(command_server):'
			print >>fileout,'	ok=True'
			for fileheader in self.fileHeaderList:
				print >>fileout,'	ok=checkFileHeader("%s") and ok' %fileheader
			print >>fileout,'	return ok'
	def writeCheckAll(self,fileout):
		if self.isEmpty==False:
			print >>fileout,'	ok=checkFileHeaders(command_server) and ok'
class adcCheckGenerator:
	adcList=[]
	isEmpty=True
	def parseCSVList(self, list):
		returnList=[]
		isEmpty=quickListCheck(4,"EpicsAdc",list,returnList)
		self.isEmpty=isEmpty
		self.adcList=returnList
	def write(self, fileout):
		if self.isEmpty==False:
			print >>fileout,'def checkAdcs(command_server):'
			print >>fileout,'	ok=True'
			for adc in self.adcList:
				print >>fileout,'	ok=checkAdc("%s") and ok' %adc
			print >>fileout,'	return ok'
	def writeCheckAll(self,fileout):
		if self.isEmpty==False:
			print >>fileout,'	ok=checkAdcs(command_server) and ok'

if (len(sys.argv)<3):
        print "generateTester.py: generate a testing jython script from input CSV file from matchEpicsGda.py"
        print "required arguments: output.csv, testerFile.py"
        print "example: python generateTester.py output.csv tester.py"
        sys.exit()
#do more handling here so that different schemas can be accomodated
templateFilename="/home/rbv51579/python/integrationTesterTemplate.py"
shutil.copy(templateFilename,sys.argv[2])
outputFile=open(sys.argv[2],"a") #make a copy of the template file first
tailHeaderFilename="/home/rbv51579/python/integrationTesterCheckAllHeader.py"
tailFilename="/home/rbv51579/python/integrationTesterTail.py" #this should be appended later
fp=open(sys.argv[1],"r")
file=[]
line=fp.readline()
while line:
	file.append(line)
	line=fp.readline()
mot=motorCheckGenerator()
mon=monitorCheckGenerator()
sam=sampleChangerCheckGenerator()
mxdet=mxDetectorCheckGenerator()
bcm=bcmCheckGenerator()
mxcam=mxCameraCheckGenerator()
optcam=opticalCameraCheckGenerator()
fileheader=fileHeaderCheckGenerator()
adc=adcCheckGenerator()
handleList=[mot,mon,sam,mxdet,bcm,mxcam,optcam,fileheader,adc]
#mon.template()
for item in handleList:
	item.parseCSVList(file)
	item.write(outputFile)

#MX-specific stuff - checkAdc, checkSampleChanger, checkDetector, checkBcm, checkMxCamera, checkOpticalCamera, checkFileHeader
#check scan
#Phase I also use checkScannable - wouldn't this be coverred by the others?
#checkall template
tailfp=open(tailHeaderFilename)
line=tailfp.readline()
while line:
	outputFile.write(line)
	line=tailfp.readline()
for item in handleList:
	item.writeCheckAll(outputFile)
tailFile=open(tailFilename)
line=tailFile.readline()
while line:
	outputFile.write( line)
	line=tailFile.readline()
outputFile.close()
tailFile.close()
tailfp.close()
fp.close()
