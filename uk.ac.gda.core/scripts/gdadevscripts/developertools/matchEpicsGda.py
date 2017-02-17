#! /usr/bin/python

# usage: python matchEpicsGda.py phase2interface.xml server_epics.xml
# example: python matchEpicsGda.py BL24I-gda-interface.xml server_epics.xml

# v. 1.1: Rob Walton has slightly changed the information output in the CSV file such as including OE names
# v. 2: Jun Aishima has added other devices from the server_epics.xml file to the final printout. This will be useful for eventually making integration tests
import sys
from xml.dom import minidom
import pdb


if (len(sys.argv)<3):
	print "matchEpicsGda.py: prints out the relationship between server_epics.xml items, gda-interface.xml items, and PVs"
	print "required arguments: new style interface file, server.xml file"
	print "example: python dump_xml.py BL24I-gda-interface.xml server_epics.xml"
	sys.exit()

class interfaceItem(object):
#<simplePv desc="...." name="..."> -> simplePv=deviceType, name is for epicsName
# <RECORD desc="current value" pv="..." ro="true/false" type="pv"/> -> pv=pvName, desc and simplePv desc used for description, type-> type, RECORD=fieldType
	deviceType=""	
	epicsName=""
	fieldType=""	
	pvName=""
	description=""
	typename=""
	def __repr__(self):
		return self.deviceType+"\t"+self.epicsName+"\t"+self.fieldType+"\t"+self.pvName+"\t"+self.description+"\t"+self.typename+"\t"
		#return "deviceType="+self.deviceType+"epicsName="+self.epicsName+"fieldType="+self.fieldType+"pvName="+self.pvName+"description="+self.description+"type="+self.type
class gdaItem(object):
	epicsName=""
	gdaName=""
	typename=""
	oename=""
	dofname=""
	scannablename=""
	def __repr__(self):
		return self.epicsName+"\t"+self.gdaName+"\t"+self.oename+"\t"+self.scannablename+"\t"+self.typename
	#	return "epicsName="+self.epicsName+"gdaName="+self.gdaName+"type="+self.type

map={"simpleMotor":"EpicsMotor", "simplePv":"EpicsMonitor","positioner":"EpicsPositioner","pneumatic":"EpicsPneumatic","currAmp":"EpicsCurrentAmplifier", "simpleScaler":"EpicsScaler"}
gdaItems=["EpicsMotor", "EpicsMonitor","EpicsPositioner","EpicsPneumatic","EpicsCurrentAmplifier", "EpicsScaler", "MarCCDDetector", "MXCameraForDummy", "DefaultFileHeader","PXParameters","GDABCM","RTPCamera", "JythonServer","ScriptController"]
DOFTypes=["DOF","SingleAxisLinearDOF","SingleAxisAngularDOF","EpicsAxisLinearDOF","EpicsAxisAngularDOF","SingleAxisAngularFixedSpeedDOF","SingleAxisWavelengthDOF","DoubleAxisGapWidthDOF","DoubleAxisGapPositionDOF","DoubleAxisLinearDOF","DoubleAxisAngularDOF","UndulatorPhaseDOF","MonoDOF","SlaveMonoDOF","MonoWithSettleTimeDOF","CoupledDOF","XafsMonoAndTableDOF","SineDriveDOF","SineDriveAngularDOF","SineDriveCoupledDOF","MirrorAndGratingMonoDOF","DOFRouteChecker","UndulatorHarmonicDOF","UndulatorPolarizationDOF","UndulatorEnergyDOF","LookupDOF"]
positionerTypes=["LinearPositioner","AngularPositioner","AngularPositioner_ReverseOffset"]

#parse gda-interface.xml file
def parseInterface(interface):
	interfaceNodeList=interface.firstChild.childNodes
	newItemList=[]
	for one in interfaceNodeList:
		if one.nodeType!=1:
			continue
		for child in one.childNodes:	
			if child.nodeType!=1:
				continue
			newItem=interfaceItem()
			newItem.deviceType=one.nodeName
			parentdesc=one.getAttribute("desc")
			newItem.epicsName=one.getAttribute("name")
			newItem.fieldType=child.nodeName
			newItem.pvName=child.getAttribute("pv")
			newItem.description=parentdesc+" "+child.getAttribute("desc")
			newItem.typename=child.getAttribute("type")
			newItemList.append(newItem)
	return newItemList

def parseGdaInterface(interface, motorToDofAndOeMap): #(rdw)
	newItemList=[]
	for gdaType in gdaItems:
		itemList=interface.getElementsByTagName(gdaType)
		
		for item in itemList:
			newItem=gdaItem()
			newItem.typename=gdaType
			for child in item.childNodes:
				if child.nodeType!=1:
					continue
				if child.nodeName=="name":
					newItem.gdaName=child.childNodes[0].data
				if child.nodeName=="deviceName":
					newItem.epicsName=child.childNodes[0].data
			# Generate the scannable name
			if gdaType == "EpicsMotor":
				try:
					[newItem.oename, newItem.dofname] = motorToDofAndOeMap[newItem.gdaName]
				except KeyError:
					[newItem.oename, newItem.dofname] = ["",""]
				newItem.scannablename = newItem.dofname
			else:
				newItem.scannablename=newItem.gdaName
			#if newItem.epicsName!="": #only add if we have a deviceName, like an Epics item should
			newItemList.append(newItem)
	return newItemList

#associate an epicsName with the item
def makeMap(parsedInterface):
	newMap={}
	for item in parsedInterface:
		if item.epicsName!="":
			newMap[item.epicsName]=item
		else:
			newMap[item.gdaName]=item
	return newMap

def makeMotorToDofAndOeMap(server):
	motorToDof={}
	oeNodes=server.getElementsByTagName("GenericOE")
	for oe in oeNodes:
		oename = oe.getElementsByTagName("name")[0].firstChild.data
		positionerToMotor = {}
		positioners=[]
		for POS in positionerTypes: 
			positioners += oe.getElementsByTagName(POS)
		for positioner in positioners:
			posname = positioner.getElementsByTagName("name")[0].firstChild.data
			motorname = positioner.getElementsByTagName("motorName")[0].firstChild.data
			positionerToMotor[posname] = motorname
		dofs=[]
		for DOF in DOFTypes:
			dofs += oe.getElementsByTagName(DOF)
		for dof in dofs:
			dofname = dof.getElementsByTagName("name")[0].firstChild.data
			posname = dof.getElementsByTagName("moveableName")[0].firstChild.data
			motorToDof[ positionerToMotor[posname] ] = [oename, dofname]
	return motorToDof


interface=minidom.parse(sys.argv[1])
server=minidom.parse(sys.argv[2])
motorToDofAndOeMap=makeMotorToDofAndOeMap(server)
epicsInterface=parseInterface(interface)
epicsMap={}
epicsMap=makeMap(epicsInterface)
gdaInterface=parseGdaInterface(server, motorToDofAndOeMap)
gdaMap={}
gdaMap=makeMap(gdaInterface)


#this won't account for items unique to GDA...
for item in epicsMap.keys():
	if gdaMap.has_key(item):
		print gdaMap[item],"\t", epicsMap[item]
	else:
		print "\t"*5, epicsMap[item]
for item in gdaMap.keys():
	if not epicsMap.has_key(item):
		print gdaMap[item], "\t"*6
