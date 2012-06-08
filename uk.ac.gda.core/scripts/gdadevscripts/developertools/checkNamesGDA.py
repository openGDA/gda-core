#! /usr/bin/python

# usage: python checkNamesGDA.py server.xml phase2interface.xml mapping.xml
# example: python checkNamesGDA.py server_epics.xml BL24I-gda-interface.xml mapping.xml

import sys
from xml.dom import minidom

if (len(sys.argv)<2):
	print "checkNamesGDA.py: check names of device names, record names, and motor names"
	print "	to reduce the number of null pointer errors due to missing names"
	print "	arguments: your_epics_server.xml phase2interface.xml"
	print "example: python checkNamesGDA.py server_epics.xml BL24I-gda-interface.xml"
	sys.exit()
#do more handling here so that different schemas can be accomodated
doc=minidom.parse(sys.argv[1])
interface=minidom.parse(sys.argv[2]) #don't do this yet
#mapping=minidom.parse(sys.argv[3])

#would be nice if we could read mapping.xml and find all of the following types automatically
#many of these only apply to old-style Epics interface...

#parse the server.xml file
deviceTypes=["deviceName"]
motorTypes=["EpicsMotor","DummyMotor","DummyParkerMotor","SRControlMotor","DummyDisplayMotor","DisplayMotor","DummySlaveMotor","SlaveMotor","McLennanStepperMotor","McLennanServoMotor","McLennan600Motor","Parker6kMotor","PIMotor","NewportMotor","AerotechMotor","TriaxMotor","DummyQueensgate","Queensgate","SRS122Motor","NewportXPSMotor"]
monitorTypes=["EpicsMonitor"]
enumTypes=["EpicsPositioner","EpicsPneumatic"]
recordTypes=["EpicsRecord"] #anything else that should go here? controllers for motors?
positionerTypes=["LinearPositioner","AngularPositioner","AngularPositioner_ReverseOffset"]
DOFTypes=["DOF","SingleAxisLinearDOF","SingleAxisAngularDOF","EpicsAxisLinearDOF","EpicsAxisAngularDOF","SingleAxisAngularFixedSpeedDOF","SingleAxisWavelengthDOF","DoubleAxisGapWidthDOF","DoubleAxisGapPositionDOF","DoubleAxisLinearDOF","DoubleAxisAngularDOF","UndulatorPhaseDOF","MonoDOF","SlaveMonoDOF","MonoWithSettleTimeDOF","CoupledDOF","XafsMonoAndTableDOF","SineDriveDOF","SineDriveAngularDOF","SineDriveCoupledDOF","MirrorAndGratingMonoDOF","DOFRouteChecker","UndulatorHarmonicDOF","UndulatorPolarizationDOF","UndulatorEnergyDOF","LookupDOF"]

devices=[]
monitors=[]
motors=[]
records=[]
positioners=[]
DOFs=[]

for device in deviceTypes:
	devices+=doc.getElementsByTagName(device)
for record in recordTypes:
	records+=doc.getElementsByTagName(record)
for type in motorTypes:
	motors+=doc.getElementsByTagName(type)
for type in monitorTypes:
	monitors+=doc.getElementsByTagName(type)
for positioner in positionerTypes:
	positioners+=doc.getElementsByTagName(positioner)
for DOF in DOFTypes:
	DOFs+=doc.getElementsByTagName(DOF)
devicesInRecords=[]
recordNames=[]
recordsInMotors=[]
devicesInMotors=[]
monitorNames=[]
recordsInMonitors=[]
devicesInMonitors=[]
motorNames=[]
motorsInPositioners=[]
positionerNames=[]
positionersInDOFs=[]
DOFNames=[]

#parse the interface file so that the validity of the deviceNames can be determined
pvType=[] #names of all simplePvs
motorType=[] #names of all simpleMotors

simplePvs=["simplePv"]
simplePvNames=[]
pvTopNames={}
pvNextNames={}
simpleMotors=["simpleMotor"]
simpleMotorNames=[]
for pv in simplePvs:
	newPv=interface.getElementsByTagName(pv)
	pvType+=newPv
	for one in newPv:
		oneName=one.getAttribute("name")
		simplePvNames.append(oneName)

for motor in simpleMotors:
	newMotor=interface.getElementsByTagName(motor)
	motorType+=newMotor
	for one in newMotor:
		oneName=one.getAttribute("name")
		simpleMotorNames.append(oneName)
		#now take the names and parse it into period-delimited names
		splitName=oneName.split(".")
		pvTopNames[splitName[0]]=splitName[0]
		#print pvNextNames
		if (splitName[0] not in pvNextNames):
			pvNextNames[splitName[0]]=oneName[oneName.find(".")+1:] #want to add the new suffix...
		else:
			pvNextNames[splitName[0]]+=","+oneName[oneName.find(".")+1:]
#print "Top",pvTopNames.keys(),"next",pvNextNames
#for OE in pvTopNames.keys():
#	print "Top name",OE #,"next",pvNextNames[OE],
#	print "Item name",#
#	for itemName in pvNextNames[OE].split(","):
#		newNameList= itemName.split(".")
#		newName=""
#		for i in newNameList:
#			newName +=i.lower().capitalize()
#		newName+="Pos" #suffix for positioner... do the same for DOF
#		print newName
#print "simplePvs",
#for pv in simplePvNames:
#'	print pv,
#print
#print "simpleMotors",
#for motor in simpleMotorNames:
#	print motor,

#search the server.xml file for records referring to higher-up items, or deviceName (EpicsRecord)
for record in records:
	a=record.getElementsByTagName("name")
	b=a[0].firstChild
	recordNames.append(b.data)
	c=record.getElementsByTagName("deviceName")
	for device in c:
		d=device.firstChild
		devicesInRecords.append(d.data)
		#print "adding record",b.data,"adding device",d.data
print "Done adding devices from records"

for monitor in monitors:
	a=monitor.getElementsByTagName("name")
	b=a[0].firstChild
	monitorNames.append(b.data)
	c=monitor.getElementsByTagName("epicsRecordName")
	newC=monitor.getElementsByTagName("deviceName")
	#for new interface, need to check deviceName
	for record in c:
		d=record.firstChild
		recordsInMonitors.append(d.data)
	for record in newC:
		d=record.firstChild
		devicesInMonitors.append(d.data)
print "Done adding monitors"

for motor in motors:
	a=motor.getElementsByTagName("name")
	b=a[0].firstChild
	motorNames.append(b.data)
	c=motor.getElementsByTagName("epicsRecordName")
	newC=motor.getElementsByTagName("deviceName")
	#for new interface, need to check deviceName
	for record in c:
		d=record.firstChild
		recordsInMotors.append(d.data)
	for record in newC: #new style
		d=record.firstChild
		devicesInMotors.append(d.data)
print "Done adding records and device names from motors"

for positioner in positioners:
	a=positioner.getElementsByTagName("name")
	b=a[0].firstChild
	positionerNames.append(b.data)
	c=positioner.getElementsByTagName("motorName")
	for motor in c:
		d=motor.firstChild
		motorsInPositioners.append(d.data)
		#print "adding positioner", b.data,"adding motor",d.data
print "Done adding motors from positioners"

for dof in DOFs:
	a=dof.getElementsByTagName("name")
	b=a[0].firstChild
	DOFNames.append(b.data)
	c=dof.getElementsByTagName("moveableName")
	for moveable in c:
		d=moveable.firstChild
		positionersInDOFs.append(d.data)
		#print "adding DOF",b.data,"adding positioner",d.data
print "Done adding positioners from DOFs"
print

for positioner in positionersInDOFs:
	if (positionerNames.count(positioner)<1):
		print "positioner",positioner,"exists in a DOF but not may not be defined properly"
for positioner in positionerNames:
	if (positionersInDOFs.count(positioner)<1):
		print "positioner",positioner,"is defined but not used in a DOF"
print "All positioners checked against DOFs"

for motor in motorsInPositioners:
	if (motorNames.count(motor)<1):
		print "motor",motor,"exists in a positioner but may not be defined properly"
for motor in motorNames:
	if (motorsInPositioners.count(motor)<1):
		print "motor",motor,"is defined but not used in a positioner"
print "All motors checked against positioners"

for record in recordsInMotors:
	if (recordNames.count(record)<1):
		print "record",record,"exists in a motor but may not be defined properly"
for record in recordNames:
	if (recordsInMotors.count(record)<1):
		print "record",record,"is defined but not used in a motor"
print "All records checked against motors"

#check monitors for proper device names
#these will tell us which simplePvs aren't used in monitors and simpleMotors that aren't used in EpicsMotors
for deviceName in devicesInMonitors:
	if (simplePvNames.count(deviceName)<1):
		print "Monitor",deviceName,"does not have a simplePv"
for monitor in simplePvNames:
	if (devicesInMonitors.count(monitor)<1):
		print "simplePv",monitor,"is defined but not used in a monitor"
print "All monitors checked against simplePvs"

for motorName in devicesInMotors:
	if (simpleMotorNames.count(motorName)<1):
	    print "Motor",motorName,"does not have a simpleMotor"
for motor in simpleMotorNames:
	if (devicesInMotors.count(motor)<1):
		print "simpleMotor",motor,"is defined but not used in a EpicsMotor"
print "All motors checked against simpleMotors"