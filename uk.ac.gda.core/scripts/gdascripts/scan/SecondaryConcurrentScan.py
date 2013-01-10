"""	TEMPORARY SOLUTION ONLY: Will be replaced with Java code when time permits.
	ConcurrentScanWrappers uses one of these if a scan contains a scannable.scan.process.ScannableScan
	which will trigger a new scan.
	
	Little effort has been made to make this code maintainable!
"""



from gdascripts.scannable.preloadedArray import PreloadedArray
from gda.scan import ConcurrentScan
from gda.scan import ScanPlotSettings

from gda.device.scannable import PseudoDevice, ScannableBase
from copy import copy
import math
from java.util import Vector
from gda.jython.commands.GeneralCommands import pause
from java.lang import InterruptedException
import time
from gda.device import Scannable, Detector
from gdascripts.scannable.dummy import SingleInputDummy

def frange(limit1, limit2, increment):
	"""Range function that accepts floats (and integers).
	"""
	increment = float(increment)
	count = int(math.ceil((limit2 - limit1) / increment))
	result = []
	for n in range(count):
		result.append(limit1 + n * increment)
	return result

def forceToList(ob):
	try:
		return list(ob)
	except TypeError :
		return [ob]

def getAllScannableFieldNames(scn):
	return list(scn.getInputNames()) + list(scn.getExtraNames())



class SecondaryConcurrentScan(object):# sort of extends ConcurrentScan
	"""
	For a simple subset of ConcurrentScan's scan types, manually performs a scan.
	After each point it will print not only the current point, but also the prior ones,
	plot the point to data vector window if a name of one is specified in dataVectorPlot. 
	
	NOTE: This class provides only the subset of the ConcurrentScan interface methods required
	to work with ConcurrentScanWrapper.
	
	Supported arguments:
	
		scannableToMove, start, stop, step, [scannable [time]]...
		
	Levels are ignored.
		
	With these arguments 'scannableToMove' will be scanned over ant each step the position
	of scannableToRead will be read 
	"""	
# Used directly by ConcurrentScanWrapepr
	
	def __init__(self, args):
		try:
			self.argStruct = parseArgsIntoArgStruct(args)
		except SyntaxError:
			raise SyntaxError(self.__doc__)
		self.realScanRunAtEnd = None
		self.yaxis = None
		self.dataVectorPlot = None

	def runScan(self):
		arrayScannable = self.scanIntoArrayAndSecondaryPlot(self.argStruct)
			
		# Now perform a real scan over arrayScannable
		self.realScanRunAtEnd = ConcurrentScan((arrayScannable, 0, arrayScannable.getLength()-1, 1))
		sps = ScanPlotSettings()
		sps.setXAxisName(getAllScannableFieldNames(self.argStruct[0][0])[0])
		fieldNames = []
		for group in self.argStruct[1:]:
			fieldNames.extend(getAllScannableFieldNames(group[0]))
		sps.setYAxesNotShown(fieldNames)
		sps.setYAxesShown([getAllScannableFieldNames(self.argStruct[-1][0])[-1]])
		self.realScanRunAtEnd.setScanPlotSettings(sps)
		self.realScanRunAtEnd.runScan()

		# Fudge the real scannables back in as arrayScannable was a conglomeration of these
		
		self.realScanRunAtEnd.setScannables(Vector([SingleInputDummy("idx")] + self.getUserListedScannables()))

	def scanIntoArrayAndSecondaryPlot(self, argStruct):

		allFieldNames = []
		allFormats = []
		for group in argStruct:
			allFieldNames.extend(getAllScannableFieldNames(group[0]))
			allFormats.extend(group[0].getOutputFormat())
		
		result = PreloadedArray('array_scannable', allFieldNames, allFormats)
		scannableToMove, start, stop, step = argStruct[0]							
		try:
			for pos in frange(start, stop, step):
				pause() # may raise InteruptedException
				scannableToMove.moveTo(pos)
				print "Moved %s to %f, and reading.." % (scannableToMove.getName(), pos)
				row = forceToList(scannableToMove.getPosition())
				pause()				
				row += self.moveAndReadScannableFields(argStruct[1:])
				pause()
				result.append(row)
				result.plotAxisToDataVectorPlot(self.dataVectorPlot,
													getAllScannableFieldNames(scannableToMove)[0],
													getAllScannableFieldNames(argStruct[-1][0])[-1]
													)
				result.printTable()
		except InterruptedException:
			print "The secondary-scan was interupted. Data so far will be processed as normal"
		return result	
		
	def moveAndReadScannableFields(self, argStruct):
		#argStruct of format [scannable [,pos]]...
		# Trigger all with positions
		for group in argStruct:
			if len(group) > 2:
				raise SyntaxError
			if len(group) == 2:
				group[0].asynchronousMoveTo(group[1])
		# Wait
		def anyBusy(argStruct):
			for group in argStruct:
				if group[0].isBusy():
					return True
			return False
		
		while anyBusy(argStruct):
			pause()
			time.sleep(.1)		
		# Read
		result = []
		for group in argStruct:
			result.extend(forceToList(group[0].getPosition()))
		
		return result

	def getDataWriter(self):
		if self.realScanRunAtEnd is None:
			raise Exception("dataWriter not available yet")
		return self.realScanRunAtEnd.getDataWriter()

	def getUserListedScannables(self):
		result = []
		for group in self.argStruct:
			result.append(group[0])
		return result
	
	def getScanPlotSettings(self):
		if self.realScanRunAtEnd is None:
			raise Exception("ScanPlotSettings not available yet")
		return self.realScanRunAtEnd.getScanPlotSettings()
	
	def getAllScannables(self):
		return self.realScanRunAtEnd.getAllScannables()

	def getDetectors(self):
		return self.realScanRunAtEnd.getDetectors()
	
################################################################################
# CODE COPIED from ConcurrentScanWrapper:
def parseArgsIntoArgStruct(args):
	"""[[x, 1, 2, 1], [y, 3, 5, 1], [z]] = parseArgsIntoArgStruct(x, 1, 2, 1, y, 3, 5, 1, z)
	"""
	if len(args) == 0:
		raise SyntaxError()
	# start off with the first arg which must be a scannable
	if isObjectScannable(args[0]) == False:
		raise Exception("First argument to scan command must be a scannable")
	currentList = [args[0]]
	
	# start off the list of lists
	listOfLists=[currentList] #NOTE: currentList now point into this list
	
	# loop through args starting new lists for each scannable found
	for arg in args[1:]:
		if isObjectScannable(arg): # start a new list
			currentList = [arg]
			listOfLists.append(currentList)
		elif isinstance(arg, (int, float, list, tuple)):
			# add to the current list
			currentList.append(arg)
		else:
			raise Exception("Arguments to scan must be a PseudoDevice/Scannable, number, or token. Problem with: ",arg)
	return listOfLists

# CODE COPIED from ConcurrentScanWrapper:
def flattenArgStructToArgs(argStruct):
	result = []
	for currentList in argStruct:
		for arg in currentList:
			result.append(arg)
	return result



# CODE COPIED from ConcurrentScanWrapper:
def isObjectScannable(obj):
	return isinstance(obj, (Scannable, PseudoDevice, ScannableBase))

def isDetector(obj):
	return isinstance(obj, Detector)
	
