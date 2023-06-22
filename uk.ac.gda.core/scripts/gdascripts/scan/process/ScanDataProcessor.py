from UserDict import IterableUserDict
import java.lang.Throwable
import traceback

from gdascripts.scan.process.ScanDataProcessorResult import *  # @UnusedWildImport
from gdascripts.scan.scanListener import ScanListener

import logging

logger = logging.getLogger(__name__)


def loadScanFile(scanOb):
	'''
	From a scan object load back the data file
	returns file reference
	see also ScanDataProcessorResult.getDatasetFromLoadedFile(loadedFile, fieldName)
	'''
	# Get the file path of the last scan from the scan object
	filepath = scanOb.getDataWriter().getCurrentFileName()
	logger.debug('Loading scan file: %s', filepath)

	lastScanFile = dnp.io.load(filepath, formats=('nx','srs'))

	return lastScanFile

class ScanDataProcessorResults(IterableUserDict):

	def __init__(self, resultDict, report):
		IterableUserDict.__init__(self, resultDict)
		self.report = report
		self.__dict__.update(resultDict)

	def __str__(self):
		return self.report

	def __repr__(self):
		return ""
		# Blank so that when concurrentScanWrapper returns this after the
		# scan after having printed the report explicitly, Jython does not show the report again

class ScanDataProcessor(ScanListener):
	log = logger.getChild('ScanDataProcessor')

	def __init__(self, datasetProcessorList=[], rootNamespaceDict={}, raiseProcessorExceptions=False, scanDataPointCache=None):
		self.processors = datasetProcessorList
		self.rootNamespaceDict = rootNamespaceDict
		self.raiseProcessorExceptions = raiseProcessorExceptions
		self.scanDataPointCache = scanDataPointCache
		self.results = {}
		self.report = None
		self.last_scannable_scanned = None
		self.duplicate_names = {}

	def __repr__(self):
		labelList=[]
		for processor in self.processors:
			labelList += processor.labelList
		return str(labelList)

	def processScan(self, concurrentScan):
		self.log.debug('Starting scan processing for scan: %s', concurrentScan.getScanNumber())
		# loop through processors building up sdpresults dictionary and adding to report
		if len(self.processors)==0:
			self.log.debug('No dataset processors are configured')
			return "<No dataset processors are configured>"
		try: # By default catch any exception so that an error processing does not stop scripts from continuing
			scanDimensions = concurrentScan.getScanInformation().getDimensions()
			if len(scanDimensions) > 1:
				self.log.debug('Not processing multidimensional scan')
				return "Cannot process multidimensional scans"

			report = "Data written to file:" + concurrentScan.getDataWriter().getCurrentFileName() + "\n"
			self.results = {}
			allscannables = concurrentScan.getUserListedScannables()
			self.log.debug('Scannables used in scan: %s', allscannables)
			# load scan
			xfieldname, yfieldname = self.__determineKeyFieldNames(concurrentScan)
			xscannable = determineScannableContainingField(xfieldname, allscannables)
			xfieldname = xscannable.name+"."+xfieldname
			yscannable = determineScannableContainingField(yfieldname, allscannables)
			yfieldname = yscannable.name+"."+yfieldname
			self.log.log(5, 'Using field names: x=%s, y=%s', xfieldname, yfieldname)
			self.last_scannable_scanned = determineScannableContainingField(xfieldname, allscannables)
			self.log.log(5, 'last scannable scanned = %s', self.last_scannable_scanned)

			# If we have the cache so don't bother loading the file	
			lastScanFile = None if self.scanDataPointCache else loadScanFile(concurrentScan)

			try:
				xDataset = getDatasetFromLoadedFile(lastScanFile, xfieldname, self.scanDataPointCache)
				yDataset = getDatasetFromLoadedFile(lastScanFile, yfieldname, self.scanDataPointCache)
			except KeyError, e:
				self.log.error('Error loading datasets', exc_info=e)
				if self.raiseProcessorExceptions:
					raise e
				return "<" + e.message + ">"

			report += "   (Processing %s v's %s)\n"%(yfieldname,xfieldname)

			# Check the datasets are processable
			if xDataset.shape[0] in (0,1):
				self.log.debug('Scan too short to process (shape=%s)', xDataset.shape[0])
				return "Scan too short to process sensibly"
			if xDataset.shape[0] != yDataset.shape[0]:
				self.log.error('Scan dimensions do not match (%s != %s)', xDataset.shape[0], yDataset.shape[0])
				return "Scan dimensions mismatch! (length(x)=%d != length(y)=%d)" % (xDataset.shape[0], yDataset.shape[0])

			lines = []
			for processor in self.processors:
				self.log.debug('Processing with %s', processor)
				sdpr = processor.process(xDataset, yDataset)
				self.log.debug('Result: %s', sdpr)
				if type(sdpr) == type(list()):
					for each in sdpr:
						sdpr = ScanDataProcessorResult(each, allscannables, xfieldname, yfieldname, self.scanDataPointCache)
						self.results[each.name] = each
						lines.append('   ' + (each.name+':').ljust(8) + each.report)
				else:
					sdpr = ScanDataProcessorResult(sdpr, lastScanFile, allscannables, xfieldname, yfieldname, self.scanDataPointCache)
					self.results[sdpr.name] = sdpr
					lines.append('   ' + (sdpr.name+':').ljust(8) + sdpr.report)
			report = '\n'.join(lines)
			# add results to root namespace dictionary
			d = self.rootNamespaceDict
			for name, result in self.results.items():
				if d.has_key(name):
					if not isinstance(d[name], ScanDataProcessorResult):
						raise Exception("Could not add ScanDataProcessorResult " + name.toString() + " to root Jython namespace as object by that name already exists")
				with overwriting:
					d[name] = result
					if self.duplicate_names.has_key(name):
						d[self.duplicate_names[name]] = result
			return ScanDataProcessorResults(self.results, report)

		except java.lang.Throwable, e: # Catch both Error and Exception
			self.log.error('Error processing at end of scan', exc_info=True)
			if self.raiseProcessorExceptions:
				raise e
			return "Error processing scan file: " + traceback.format_exc() + "\n<< No exception raised >>>"

		except Exception, e:
			self.log.error('Error processing at end of scan', exc_info=True)
			if self.raiseProcessorExceptions:
				raise e
			return "Error processing scan file: " + traceback.format_exc() + "\n<< No exception raised >>>"

	def prepareForScan(self):
		self.log.debug('Clearing processing data at start of scan')
		# Remove all pre-existing ScanDataProcessorResults
		self.last_scannable_scanned = None
		if self.processors:
			d = self.rootNamespaceDict
			for processor in self.processors:
				name = processor.name
				if d.has_key(name) and isinstance(d[name], ScanDataProcessorResult):
					with overwriting:
						del d[name]
				if self.duplicate_names.has_key(name):
					dupname = self.duplicate_names[name]
					if d.has_key(dupname) and isinstance(d[dupname], ScanDataProcessorResult):
						with overwriting:
							del d[dupname]

	def go(self, position_or_scanDataProcessorResult):
		try:
			# assume its a ScanDataProcessorResult
			position_or_scanDataProcessorResult.go()
		except AttributeError:
			# now assume its a position
			if not self.last_scannable_scanned:
				raise Exception("No scan has successfully completed, so 'go' does not what to move to '" + `position_or_scanDataProcessorResult` +"'")
			print "Moving %s to %s" % (self.last_scannable_scanned.getName(), `position_or_scanDataProcessorResult`)
			self.last_scannable_scanned.moveTo(position_or_scanDataProcessorResult)

	def __getitem__(self, key):
		return self.results[key]

	def __determineKeyFieldNames(self, concurrentScan):
		sps = concurrentScan.getScanPlotSettings()
		if sps is None:
			raise RuntimeError("ScanDataProcessor expects the scan being processed to have had ScanPlotSettings set")
		return sps.getXAxisName(), sps.getYAxesShown()[-1]

### ScanListener
	def update(self, concurrentScan):
		return self.processScan(concurrentScan)

if __name__ == '__main__':
	r = ScanDataProcessorResults({'a':1,'b':2}, 'abc')
	print r
	print r['a']
