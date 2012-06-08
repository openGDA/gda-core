from gda.device.scannable import ScannableMotionBase
from gda.device.scannable import ScannableBase

class ScannableMotionBase74(ScannableMotionBase):
	'''This extended version of ScannableMotionBase contains a completeInstantiation() method
	which adds a dictionary of MotionScannableParts to an instance. Each part allows one of the
	instances fields to be interacted with like it itself is a scannable. Fields are dynamically 
	added to the instance linking to these parts allowing dotted access from Jython.  They may
	also be accessed using Jython container access methods (via the __getitem__() method).
	To acess them from Jave use the getComponent(name) method.
	
	When moving a part (via either a pos or scan command), the part calls the parent to perform
	the actual task.  The parts asynchronousMoveto command will call the parent with a list
	of None values except for the field it represents which will be passed the desired position
	value.
	
	The asynchronousMoveTo method in class that inherats from this base class then must handle
	these Nones.  In some cases the method may actually be able to move the underlying system
	assoiciated with one field individually from others. If this is not possible the best behaviour
	may be to simply not support this beahviour and exception or alternatively to substitute the
	None values with actual current position of parent's scannables associated fields.
	
	ScannableMotionBaseWithMemory() inherats from this calss and provides a solution useful
	for some scenarious: it keeps track of the last position moved to, and replaces the Nones
	in an asynchronousMoveTo request with these values. There are a number of dangers associated
	with this which are addressed in that class's documentation, but it provides a way to move
	one axis within a group of non-orthogonal axis while keeping the others still.
'''
	childrenDict = {}
	numInputFields = None
	numExtraFields = None
	
	def completeInstantiation(self):
		'''This method should be called at the end of all user defined consructors'''
		# self.validate()
		self.numInputFields = len(self.getInputNames())
		self.numExtraFields = len(self.getExtraNames())
		self.addScannableParts()

	def addScannableParts(self):
		'''Creates an array of MotionScannableParts each of which allows acces to the scannable's
		fields. See this class's documentation for more info.'''
		self.childrenDict = {}
		# Add parts to access the input fields
		for index in range(len(self.getInputNames())):
			scannableName = self.getInputNames()[index]
			self.childrenDict[scannableName] = self.MotionScannablePart(scannableName, index, self, isInputField=1)
			#exec "self." + scannableName + "= self.childrenDict['" + scannableName + "']"
		
		# Add parts to access the extra fields
		for index in range(len(self.getExtraNames())):
			scannableName = self.getExtraNames()[index]
			self.childrenDict[scannableName] = self.MotionScannablePart(scannableName, index+len(self.getInputNames()), self, isInputField=0)
			#exec "self." + scannableName + "= self.childrenDict['" + scannableName + "']"
	
	def fillPosition(self, position):
		'''If position contains any null or None values, these are replaced with the
		corresponding fields from the scannables current position and then returned.'''
		# Just return position if it does not need padding
		if None not in position:
			return position
			
		currentPosition = self.getPosition()[:self.numInputFields]
		for i in range(self.numInputFields):
			if position[i] == None:
				position[i] = currentPosition[i]
				
	def __getattr__(self, name):
		return self.childrenDict[name]
	
	def __getitem__(self, key):
		'''Provides container like access from Jython'''
		return self.childrenDict[key]
	
	def getPart(self, name):
		'''Returns the a compnent scannable'''
		return self.childrenDict[name]
	
	class MotionScannablePart(ScannableBase):
		'''A scannable to be placed in the parent's childrenDict that allows access to the
		parent's individual fields.'''
	
		def __init__(self, scannableName, index, parentScannable, isInputField):
			self.setName(scannableName)
			if isInputField:
				self.setInputNames([scannableName])
			else:
				self.setExtraNames([scannableName])
			self.index = index
			self.parentScannable = parentScannable
			
		def isBusy(self):
			return self.parentScannable.isBusy()

		def asynchronousMoveTo(self,new_position):
			if self.parentScannable.isBusy():
				raise Exception, self.parentScannable.getName() + "." + self.getName() + " cannot be moved because " + self.parentScannable + " is already moving"
				
			toMoveTo=[None] * len(self.parentScannable.getInputNames())
			toMoveTo[self.index] = new_position
			self.parentScannable.asynchronousMoveTo(toMoveTo)

		def getPosition(self):
			return self.parentScannable.getPosition()[self.index]
	
		def toString(self):
			# Get the name of this field (assume its an input field first and correct if wrong
			name = self.getInputNames()[0]
			
			if name=='value':
				name = self.getExtraNames()[0]
			return self.parentScannable.getName() + "." + name + " : " + str(self.getPosition())
	

				

class MultiInputDummyPD74(ScannableMotionBase74):
	'''Multi input Dummy PD Class'''
	def __init__(self, name, inputNames, extraNames):
		self.setName(name)
		self.setInputNames(inputNames)
		self.setExtraNames(extraNames)
		self.Units=['Units']*(len(inputNames)+len(extraNames))
		self.setOutputFormat(['%6.4f']*(len(inputNames)+len(extraNames)))
		self.setLevel(3)
		self.currentposition=[0]*len(inputNames)
		self.completeInstantiation()
		
	def isBusy(self):
		return 0

	def asynchronousMoveTo(self,new_position):
		assert len(new_position)==len(self.currentposition), "Wrong new_position size"
		for i in range(len(new_position)):
			if new_position[i] != None:
				self.currentposition[i] = new_position[i]

	def getPosition(self):
		extraValues = range(100,100+len(self.getExtraNames()))
		return self.currentposition + extraValues
	