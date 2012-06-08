from gda.device.scannable import ScannableBase
from gda.factory import Finder
from ScannableMotionBase74 import ScannableMotionBase74

class ScannableMotionGroupAssembler:
	'''Looks trhough all the scannables in the finder and builds scannables groups, analagous
	to OEs for any named with the format abc__xyz with abc becoming the group name and xyz the
	a component scannable name within that group.'''
	
	def Assemble(self):
		nameList=Finder.getInstance().listAllNames('Scannable')
		nameList=['ab__a', 'ab__b', 'xyz__x', 'xyz__y', 'xyz__z', 'blarghh']
		
		# Filter out those containing __
		groupComponentList=[]
		for name in nameList:
			if name.__contains__('__'):
				groupComponentList.append(name)
		
		# Create a dictionary with one key for each group, each key pointing to a list of component strings"
		groupDict={}
		for name in groupComponentList:
			group, component = name.split('__')
			if not(groupDict.has_key(group)):
				groupDict[group]=[]
			groupDict[group].append(component)
		
		# Create the group scannables and pass back as list (STUB)
		toReturn = []
		groupNameList = groupDict.keys()
		for groupName in groupNameList:
			tmp=groupName
			for componentName in groupDict[groupName]:
				tmp += "." + componentName
			toReturn += [tmp]
			
		return toReturn
		
		
class ScannableMotionGroup(ScannableMotionBase74):
	'''Bundles up a group of externally defined scannables. A potential replacement for OEs.
	The group has a set of input and extra fields, one for each input and extra field of each
	component scannable. It also stores the compnent scannables so they can be accesed directly:
	as fields ( >>>group.comp ) or using container access ( >>>group['comp'] ) from jython, or
	using group.getComponent(comp) from java.
	
	   >>> groupScannable = ScannableMotionGroup(name, componentScannables, newComponentNames, 
	                                      resultingGroupsInputNames, resultingGroupsExtraNames)
	
	Creates a scannable called name that bundles up a number of component scannables given by
	componentScannables. The fields from all the components are flattened into the group and 
	labeled according to the names passed in resultingGroupsInputNames and resultingGroupsExtraNames.
	
	For example the three scannables (with input and extra field names indicated in brackets and
	begining with i ane respectively) 
	 
	    a (i: i1, i2)
	    b (e: e1)
	    c (i: i1, i2; e: e3)
	    
	might be grouped using the command
	
	    >>> abc = ScannableMotionGroup('abc', [a, b, c], ['i1a','i2a','i1c','i2c'], ['e1b','e3c'] )
	    
	resulting in the scannable (with input/extra field names)
	
	    abc (i: i1a, i2a, i1c, i2c; e: e1b, e3c)
	    
	and fields abc.i1a, abc.i1a, abc.i2a, abc.i1c, abc.i2c, abc.e1b and abc.e3c each themselves
	scannables.
	    
	Notice that the input and extra fields from a component scannable may not end up next to each
	other!
   
	'''
	
	componentList = [] # Holds wrappers contaiing component scannables
	componentDict = {} # Points to componets by name
	numInputFields = 0
	numExtraFields = 0
	def __init__(self, groupName, componentScannables, inputFieldNames, extraFieldNames):
		'''Creates a group scannable with parameters as described in this class's documentation'''

		# Check input

		# Basic PD setup
		self.setName(groupName)
		
		# Add the component scannables to an ordered list and add links to them via a dictionary
		# Keep track of number of input and extra names for checking input
		inputFormatsToSet = []
		extraFormatsToSet = []	
		for scannable in componentScannables:
			self.numInputFields += len(scannable.getInputNames())
			self.numExtraFields += len(scannable.getExtraNames())		
			self.componentList.append(scannable)
			self.componentDict[scannable.getName()] = self.componentList[len(self.componentList)-1]
			inputFormatsToSet += scannable.getOutputFormat()[:self.numInputFields]
			extraFormatsToSet += scannable.getOutputFormat()[self.numInputFields:]
		
		# Check input
		if (len(inputFieldNames)!=self.numInputFields) or (len(extraFieldNames)!=self.numExtraFields):
			raise Exception, self.numInputFields+" input field names, and "+self.numExtraFields+" extra field names must be given"
		
		# Set this group scannable's input and extra names
		self.setInputNames(inputFieldNames)
		self.setExtraNames(extraFieldNames)
		
		# set this group's format fields
		self.setOutputFormat(inputFormatsToSet + extraFormatsToSet)

		# As standard, call completeInstantiation()
		self.completeInstantiation()

	def isBusy(self):
		'''Returns true if any component scannable's are busy.'''
		toReturn = 0
		for scannable in self.componentList:
			if scannable.isBusy():
				toReturn = 1
		return toReturn
					
	def asynchronousMoveTo(self, groupPosition):
		'''Moves the component scannables. groupPosition must have a value for each input field
		of all the componet scannables. If all of the input fields corresponding to a given
		component scannable are None (or null) this scannable will not be moved.'''
		
		# Check input length
		assert len(groupPosition)==self.numInputFields, "Wrong number of values given"
		
		# For each component scannable, pop off the correct number of input values from begining
		# and move the scannable if any are not None/null
		for component in self.componentList:
			componentPos = groupPosition[0:len(component.getInputNames())]
			groupPosition = groupPosition[len(component.getInputNames()):]
			
			# if groupPosition has at least one non null value then move this scannable
			moveIt = 0
			for value in componentPos:
				if value != None:
					moveIt = 1
			if moveIt:
				component.asynchronousMoveTo(componentPos)

	def getPosition(self):
		'''Returns the position of all the component's input and extra fields in the order described
		in this class's documentation.
		'''
		inputFieldValues = []
		extraFieldValues = []
		
		
		# Visit each component and add its input and extra values to the lists to return
		for component in self.componentList:
			values = component.getPosition()
			if (type(values)==type(1.0)) or (type(values)==type(1)): #FLAKEY
				values = [values]
			inputFieldValues += values[0:len(component.getInputNames())]
			extraFieldValues += values[len(component.getInputNames()):]

		# Return the input and output fields concatenated
		return inputFieldValues + extraFieldValues
	
	def getComponent(self, name):
		'''Returns the a component scannable'''
		return self.componentDict[name]
		
