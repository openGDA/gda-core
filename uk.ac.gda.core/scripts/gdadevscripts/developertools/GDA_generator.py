#! /usr/bin/python

# usage: python GDA_generator.py phase2interface.xml output.xml
# example: python GDA_generator.py BL24I-gda-interface.xml server_epics.xml

import sys
from xml.dom import minidom
#from xml.dom.ext import PrettyPrint #note, python 2.4 does not have prettyprint

from optparse import OptionParser
parser = OptionParser()
parser.add_option("-s", "--spring", dest="spring", action="store_true", default=False, help="write out Spring bean definitions")
(options, args) = parser.parse_args()

if not options.spring:
	top_element = "ObjectFactory"
else:
	top_element = "beans"

motorSuffix="Motor"
positionerSuffix="Positioner" #suffix for LinearPositioner

if (len(args) != 2):
	print "GDA_generator.py: automatically generate server_epics.xml file from new type Epics interface file"
	print "required arguments: new style interface file, output file"
	print "example: python GDA_generator.py BL24I-gda-interface.xml server_epics.xml"
	sys.exit()

epicsToGdaMap={"simpleMotor":"EpicsMotor", "simplePv":"EpicsMonitor","positioner":"EpicsPositioner","pneumatic":"EpicsPneumatic","currAmp":"EpicsCurrentAmplifier", "simpleScaler":"EpicsScaler"}

#mapping between the gda-interface.xml file to a GDA device. Actually, simplePv can also be changed to ControlPoint as well, depending on RO. we don't handle that for now
motorItemNames=["simpleMotor"] #motor items that need to be handled separately
simpleItemNames=["simplePv","positioner","pneumatic","currAmp","simpleScaler"] #just take the name, then put it into a converted item of the proper type. very simple, unlike motors
interface=minidom.parse(args[0])
fp=open(args[1],"w")

def create_spring_bean_element(id, clazz):
	"""Creates a Spring <bean> element"""
	bean = xml.createElement("bean")
	if id is not None:
		bean.setAttribute("id", id)
	bean.setAttribute("class", clazz)
	return bean

def create_spring_property_element(name, value):
	"""Creates a Spring <property> element"""
	property = xml.createElement("property")
	property.setAttribute("name", name)
	if value is not None:
		property.setAttribute("value", value)
	return property

def create_element_with_text(name, text):
	element = xml.createElement(name)
	textNode = xml.createTextNode(text)
	element.appendChild(textNode)
	return element

#parse easy stuff. return a map containing the typename:nameList
def parseSimpleItems(interface,simpleItemNames):
	simpleItemReturn={}
	for simpleType in simpleItemNames:
		newItemList=[]
		newPv=interface.getElementsByTagName(simpleType)
		for one in newPv:
			oneName=one.getAttribute("name")
			newItemList.append(oneName)
		simpleItemReturn[simpleType]=newItemList
	return simpleItemReturn
#motors are special cases mostly in that the OE name and the second parts are separated
def motorItems(interface):
	motorItemReturn=[]
	for motorType in motorItemNames:
		newMotors=interface.getElementsByTagName(motorType)
		for one in newMotors:
			oneName=one.getAttribute("name")
			motorItemReturn.append(oneName)
			splitName=oneName.split(".")
			if (splitName[0] not in oeNames):
				oeNames[splitName[0]]=splitName[0]
			if (splitName[0] not in secondNames):
				secondNames[splitName[0]]=oneName[oneName.find(".")+1:] #everything after the first dot makes up the secondName (s1.x.minus -> s1=oename, x.minus = secondname)
			else:
				secondNames[splitName[0]]+=","+oneName[oneName.find(".")+1:]
	return (oeNames,secondNames)
#write stuff out...
def writeSimpleItems(simpleItemMap):
	top=xml.createElement(top_element)
	
	keys=simpleItemMap.keys()
	for itemType in keys: #check whether order is important
		
		creationList=simpleItemMap[itemType]
		
		for item in creationList:
			
			# Determine GDA name
			nameList=item.split(".")
			newName=""
			for i in nameList:
				newName += i.lower().capitalize()
			
			if not options.spring:
				gdaName=epicsToGdaMap[itemType]
				newItem=xml.createElement(gdaName)
				name=xml.createElement("name")
				name.appendChild(xml.createTextNode(newName))
				#print gdaName,newName,
				deviceNode=xml.createElement("deviceName")
				deviceNode.appendChild(xml.createTextNode(item))
				#print item
				newItem.appendChild(name)
				newItem.appendChild(deviceNode)
				top.appendChild(newItem)
			
			else:
				if itemType == "simplePv":
					bean = create_spring_bean_element(id=newName, clazz="gda.spring.EpicsMonitorFactoryBean")
					bean.appendChild(create_spring_property_element(name="deviceName", value=item))
					top.appendChild(bean)
				elif itemType == "positioner":
					bean = create_spring_bean_element(id=newName, clazz="gda.spring.EpicsPositionerFactoryBean")
					bean.appendChild(create_spring_property_element(name="deviceName", value=item))
					top.appendChild(bean)
				else:
					print >>sys.stderr, "Sorry, don't know how to convert '%s' to Spring configuration" % itemType
					sys.exit(1)
			
	return top
#easy stuff, check order
#motors
def writeMotors(oeNames,secondNames):
	top=xml.createElement(top_element)
	for OE in oeNames.keys():
		for itemName in secondNames[OE].split(","):
			
			# Determine GDA name for motor
			nameList=itemName.split(".")
			newName=""
			#only do the following if we want to
			for i in nameList:
				newName+=i.lower().capitalize()
			
			name = OE+newName+motorSuffix
			deviceName = OE+"."+itemName
			
			if not options.spring:
				# Create <name> node
				nameNodeText=xml.createTextNode(name)
				nameNode=xml.createElement("name")
				nameNode.appendChild(nameNodeText)
				
				# Create <deviceName> node
				deviceNameNodeText=xml.createTextNode(deviceName)
				deviceNameNode=xml.createElement("deviceName")
				deviceNameNode.appendChild(deviceNameNodeText)
				
				# Create <motor> node
				motorNode=xml.createElement(epicsToGdaMap["simpleMotor"])
				motorNode.appendChild(nameNode)
				motorNode.appendChild(deviceNameNode)
				
				top.appendChild(motorNode)
			
			else:
				bean = create_spring_bean_element(id=name, clazz="gda.spring.EpicsMotorFactoryBean")
				bean.appendChild(create_spring_property_element(name="deviceName", value=deviceName))
				top.appendChild(bean)
			
	return top
#positioners/dofs
def writeDofs(oeNames,secondNames):
	top=xml.createElement(top_element)
	for OE in oeNames.keys():
		if not options.spring:
			genericOeElement=xml.createElement("GenericOE")
			genericOeElement.appendChild(create_element_with_text("name", OE+"OE"))
			
			#how can we keep adding name, etc?
			#within OEs, create positioners
			for itemName in secondNames[OE].split(","):
				
				# determine prefix
				posList=itemName.split(".")
				newName=OE
				for i in posList:
					newName+=i.lower().capitalize()
				
				newPositioner=xml.createElement("LinearPositioner")
				newPositioner.appendChild(create_element_with_text("name", newName+positionerSuffix))
				newPositioner.appendChild(create_element_with_text("motorName", newName+motorSuffix))
				newPositioner.appendChild(create_element_with_text("stepsPerUnit", "1"))
				newPositioner.appendChild(create_element_with_text("softLimitLow", "-100"))
				newPositioner.appendChild(create_element_with_text("softLimitHigh", "200"))
				newPositioner.appendChild(create_element_with_text("poll", "false"))
				genericOeElement.appendChild(newPositioner)
			#DOFs will only have the standard types linked to positioners
			for itemName in secondNames[OE].split(","):
				
				# determine prefix
				posList=itemName.split(".")
				newName=OE
				for i in posList:
					newName +=i.lower().capitalize()
				
				newDOF=xml.createElement("SingleAxisLinearDOF")
				newDOF.appendChild(create_element_with_text("name", newName))
				newDOF.appendChild(create_element_with_text("protectionLevel", "0"))
				newDOF.appendChild(create_element_with_text("moveableName", newName+positionerSuffix))
				genericOeElement.appendChild(newDOF)
			
			top.appendChild(genericOeElement)#then DOFs

	return top
#create a new file
xml=minidom.Document()
top=xml.createElement(top_element)

if options.spring:
	top.setAttribute("xmlns", "http://www.springframework.org/schema/beans")
	top.setAttribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance")
	top.setAttribute("xsi:schemaLocation", "http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans-2.5.xsd")

simpleItemLists=parseSimpleItems(interface,simpleItemNames)
oeNames={}
secondNames={}
(oeNames,secondNames)=motorItems(interface)

testList=writeSimpleItems(simpleItemLists).childNodes
#print len(testList),testList[1].firstChild.firstChild,testList
#there seems to be a problem generating the range for childNodes, half the items are missing if I do a "for...in..."
#aha, it looks like appendChild actually pops off the child from the existing list.
counter=0
while len(testList)>0:
	top.appendChild(testList[0])
	counter+=1
testList=writeMotors(oeNames,secondNames).childNodes
while len(testList)>0:
	top.appendChild(testList[0])
testList=writeDofs(oeNames,secondNames).childNodes
while len(testList)>0:
	top.appendChild(testList[0])
#fp.write(top.toprettyxml("  ",'\n')) #for python2.4
#PrettyPrint(top,fp)
