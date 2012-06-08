from gda.device.scannable import ScannableMotionBase
from gdascripts.scannable.SelectableCollectionOfScannables import forceToList

def close(a, b):
	return abs(a-b)<.0002 # HACKED for current application

class SFHInterpolator:
	
	def __init__(self, sfh):
		# Build pythony representation of dataset for now (Later code should use sfh directly)
		self.columnDict = {}
		for key in sfh.getHeadings():
			self.columnDict[key] = sfh.getDataSet(key).doubleArray()
		self.length = len(self.columnDict.values()[0])
	
	def getRowAsDict(self, idx):
		result = {}
		for key, column in self.columnDict.items():
			result[key] = column[idx] 
		return result
	
	def getRowByValuesAsDict(self, nameValueDict):
		for idx in range(self.length):
			row = self.getRowAsDict(idx)
			matches = True
			for key, value in nameValueDict.items():
				if not close(row[key], value):
					matches = False
					break
			if matches:
				return row
		raise KeyError("Row matching %s not found" % `nameValueDict`)

def quantise(val, delta):
	return round(val/delta) * delta

class SFHInterpolatorWithHashAccess(SFHInterpolator):
	
	def __init__(self, sfh, quantizeSizeDict, inputNames):
		# Build pythony representation of dataset for now (Later code should use sfh directly)
		self.indexNames = inputNames
		self.columnDict = {}
		self.quantizeSizeDict = quantizeSizeDict
		for key in sfh.getHeadings():
			self.columnDict[key] = sfh.getDataSet(key).doubleArray()
		self.length = len(self.columnDict.values()[0])
		
		# TODO: Quantize here!


		# For the example Columndict:
		# y  x  det
		# 1  1  .1
		# 1  2  .2
		# 2  1  .1
		# 2  2  .2
		
		# We want the result lookupDict = {1:{1:0, 2:1}, 2:{1:2, 2:3}
		#such that for example:
		#	lookupDict[1][1]:0
		#	lookupDict[1][2]:1
		#	lookupDict[2][1]:2
		#	lookupDict[2][2]:3
		
		# To do this easily create an array of columns :y ,x, index:
		#s = [[1,1,2,2], [1,2,1,2], [0,1,2,3]]	
		s=[]
		for name in inputNames:
			col = self.columnDict[name]
			if quantizeSizeDict.has_key(name):
				col = [quantise(x, quantizeSizeDict[name]) for x in col]
			s.append(col)
		s.append(range(self.length))
		
		self.lookupDict = self.buildLookupDict(s)
	
	
	def buildLookupDict(self, s):
		# end of recursion

		if len(s) == 1:
			if len(s[0])!=1:
				print "Warning, %i lines of data mapped into same dictionary location. Using the first found." % len(s[0])
			return s[0][0]
		
		result = {}
		for idx, outerval in enumerate(s[0]):
			if result.has_key(outerval):
				pass # This row will already have been added to the result
			else:
				# Add this row and rows with matching outer values to a new entry
				filtered = self.filterSrows(s, outerval)
				result[outerval] = self.buildLookupDict(filtered[1:])
		return result
						
	def filterSrows(self, s, desiredVal):
		result = [[] for i in range(len(s))]

		for rowidx, outerval in enumerate(s[0]):
			if outerval == desiredVal:
				for colidx in range(len(s)):
					col = s[colidx]
					v = col[rowidx]
					result[colidx].append(v)
		return result

	def getRowByValuesAsDict(self, nameValueDict):
		result = self.lookupDict
		try:
			for name in self.indexNames:
				targetVal = float(nameValueDict[name])
				if self.quantizeSizeDict.has_key(name):
					targetVal = quantise(targetVal, self.quantizeSizeDict[name] )
				result = result[targetVal]
			return self.getRowAsDict(result)
		except KeyError:
			raise KeyError("Row matching %s not found" % `nameValueDict`)
		

	
class ScanFileHolderScannable(ScannableMotionBase):
	
	def __init__(self, name, sfh, inputColumnNames, extraColumnNames, quantizeSizeDict={}):
		"""inputColumnNames and extraColumnNames must corespond to columns in the sfh
		"""
		self.name = name
		self.setInputNames(inputColumnNames)
		self.setExtraNames(extraColumnNames)
		self.outputFormat = ['%f'] * len(self.getNames())
		self.data = SFHInterpolator(sfh )
		self.data = SFHInterpolatorWithHashAccess(sfh, quantizeSizeDict, self.inputNames)
		
		# Prepare the current position
		self.pos = {}
		row = self.data.getRowAsDict(0)
		for name in self.inputNames:
			self.pos[name] = row[name]

	def asynchronousMoveTo(self, pos):
		pos = forceToList(pos)
		pos = map(float, pos)
		assert len(pos)==len(self.inputNames)
		self.updatePosition( dict(zip(self.inputNames, pos)) )
	
	def updatePosition(self, posDict):
		for key in posDict.keys():
			if key not in posDict.keys():
				raise KeyError
		self.pos.update(posDict)
	
	def getPositionAsDict(self):
		return self.data.getRowByValuesAsDict(self.pos)
	
	def getPosition(self):
		row = self.data.getRowByValuesAsDict(self.pos)
		result = []
		for name in self.getNames():
			result.append(row[name])
		return result
	
	def isBusy(self):
		return False		
	
	def getNames(self):
		return list(self.inputNames) + list(self.extraNames)
	
	def scannableFactory(self, name, inputFieldNames, extraFieldNames=[]):
		return SFHSubsetScannable(name, self, forceToList(inputFieldNames),forceToList(extraFieldNames))


class SFHSubsetScannable(ScannableMotionBase):
	
	def __init__(self, name, sfhs, inputFieldNames, extraFieldNames=[]):
		self.sfhs = sfhs	
		self.name = name
		inputFieldNames = forceToList(inputFieldNames)
		extraFieldNames = forceToList(extraFieldNames)		

		for name in inputFieldNames + extraFieldNames:
			if name not in list(self.sfhs.inputNames) + list(self.sfhs.extraNames):
				raise ValueError('%s not a valid field from %s. Try: %s' %(name, self.sfhs.name, `self.sfhs.getNames()`))

		self.inputNames = inputFieldNames
		self.extraNames = extraFieldNames
		
	def isBusy(self):
		return self.sfhs.isBusy()
	
	def asynchronousMoveTo(self, pos):
		pos= forceToList(pos)
		assert len(pos)==len(self.inputNames)
		posDict = dict(zip(self.inputNames, pos))
		for key in posDict.keys():
			if key not in list(self.sfhs.inputNames):
				del posDict[key]
		self.sfhs.updatePosition( posDict )
		
	def getPosition(self):
		rowDict = self.sfhs.getPositionAsDict()
		result = []
		for name in self.getNames():
			result.append(rowDict[name])
		if len(result)==1:
			return result[0]
		else:
			return result
		
	def getNames(self):
		return list(self.inputNames) + list(self.extraNames)
	
		