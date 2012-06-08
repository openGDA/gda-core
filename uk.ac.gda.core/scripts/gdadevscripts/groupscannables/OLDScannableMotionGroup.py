from gda.device.scannable import ScannableBase
from gda.factory import Finder

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
		
		
class ScannableMotionGroup(ScannableBase):
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

	def __init__(self, groupName, componentScannables, newComponentNames, resultingGroupsInputNames, resultingGroupsExtraNames):
		'''Creates a group scannable with parameters as described in this class's documentation'''

		# Check input

		# Basic PD setup
		self.setName(groupName)
		
		# Add the component scannables
		
		
		for scannable, newname in zip(componentScannables, newComponentNames):
			# Create a wrapper for the componet scannable and add to component list
			self.componentList.append(self.ScannableMotionGroupPart(groupName, scannable, newname))
		
			# Create a link to the component based on newname
			self.componentDict[newname] = self.componentList[len(self.componentList)-1]		
			
			# Dynamically add a field to the instantiated object so that the component scannables
			# can be accesed from jython using dotted notation.
			exec "self." + newname + "= self.componentDict['" + newname + "']"		
		
		# Set this group scannable's input and extra names
		self.setInputNames(resultingGroupsInputNames)
		self.setExtraNames(resultingGroupsExtraNames)


	def isBusy(self):
		'''Returns true if any component scannable's are busy.'''
		toReturn = 0
		for scannable in self.componentList:
			if scannable.isBusy():
				toReturn = 1
		return toReturn
					
	def asynchronousMoveTo(self, groupPosition):
		'''Moves the component scannables. newPosition must have a value for each input field
		of all the componet scannables. If all of the input fields corresponding to a given
		component scannable are None (or null) this scannable will not be moved '''
		
		# Check input length
		assert len(groupPosition)==len(self.getInputNames()), "Wrong number of values given"
		
		# For each component scannable, pop off the correct number of input values from begining
		# and move the scannable if any are not None/null
		for component in self.componentList:
			pos = groupPosition[0:component.numInputFields]
			groupPosition = groupPosition[component.numInputFields:]
			if None not in pos:
				print component.getName(), " to ", pos
				component.asynchronousMoveTo(pos)

	def getPosition(self):
		'''Returns the position of all the component's input and extra fields in the order described
		in this class's documentation.
		'''
		inputFieldValues = []
		extraFieldValues = []
		
		# Visit each component and add its input and extra values to the lists to return
		for component in self.componentList:
			values = component.getPosition()
			if (type(values)==type(1.0)) or (type(values)==type(1)):
				values = [values]
			inputFieldValues += values[0:len(component.getInputNames())]
			extraFieldValues += values[len(component.getInputNames()):]

		# Return the input and output fields concatenated
		return inputFieldValues + extraFieldValues

	def toString(self):
		'''Returns a string showing the position of all groups component scannables'''
		
		toReturn = ""
		
		# Visit each component and add its values to string
		for component in self.componentList:
			toReturn += component.toString() + '\n'

		return toReturn
	
	def getComponent(self, name):
		'''Returns the a compnent scannable'''
		return self.componentDict[name]
		
	class ScannableMotionGroupPart(ScannableBase):
		'''A wrapper around each of the group's component scannables. Used onlt to rewrite
		the toString method to make it group aware'''

		original = None
		groupName = None
		numInputFields = None
		numExtraFields = None
	   
		def __init__(self, groupName, scannable, newname):
			self.original = scannable
			self.groupName = groupName			
			self.setName(newname)
			self.setInputNames(scannable.getInputNames())
			self.setExtraNames(scannable.getExtraNames())			
			self.numInputFields=len(scannable.getInputNames())
			self.numExtraFields=len(scannable.getExtraNames())	   
			
		def isBusy(self):
			return self.original.isBusy()

		def asynchronousMoveTo(self, new_position):
			self.original.asynchronousMoveTo(new_position)

		def getPosition(self):
			return self.original.getPosition()
	
		def toString(self):
			pos = self.getPosition()
			# make sure pos is an array
			if (type(pos)==type(1.0)) or (type(pos)==type(1)):
				pos = [pos]
			# Add each value for this component 
			toReturn = ""
			print self.getInputNames() + self.getExtraNames()
			for value, name in zip(pos, self.getInputNames() + self.getExtraNames()):
				toReturn += self.groupName + "." + self.getName() + "." + name + " : " + str(value) + "\n"
			
			return toReturn
		
		
