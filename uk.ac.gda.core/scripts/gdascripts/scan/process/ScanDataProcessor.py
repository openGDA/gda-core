from gdascripts.scan.scanListener import ScanListener
from ScanDataProcessorResult import ScanDataProcessorResult
from gdascripts.scan.process.ScanDataProcessorResult import determineScannableContainingField

from gda.analysis.io import SRSLoader
from uk.ac.diamond.scisoft.analysis.io import NexusLoader
from gda.analysis import ScanFileHolder
import java.lang.Throwable
from UserDict import IterableUserDict
import traceback

def loadScanFile(ob, columnNames=None, scannables=[]):
	sfh = ScanFileHolder()
	# ConcurrentScan
	#if isinstance(ob, ConcurrentScan) or isinstance(ob, SecondaryConcurrentScan):
	filepath = ob.getDataWriter().getCurrentFileName()
	if filepath[-3:] == 'nxs':
		sfh.load(NexusLoader(filepath, columnNames))
	elif filepath[-3:] == 'dat':
		srsloader = SRSLoader(filepath)
		srsloader.setUseImageLoaderForStrings(False)
		sfh.load(srsloader)
		for name in sfh.getHeadings():
			scannable = determineScannableContainingField(name, scannables)
			if (len(scannable.getInputNames())+len(scannable.getExtraNames())) > 1:
				newname = scannable.name + "." + name
				sfh.addDataSet(newname, sfh.getAxis(name))
	#else:
	#	raise Exception("Failed to load scan file: was expecting a ConcurrentScan object")
	return sfh

class ScanDataProcessorResults(IterableUserDict):

	def __init__(self, resultDict, report):
		IterableUserDict.__init__(self, resultDict)
		self.report = report
		self.__dict__.update(resultDict)

	def __str__(self):
		return self.report

	def __repr__(self):
		return self.__str__()


class ScanDataProcessor(ScanListener):
	
	def __init__(self, datasetProcessorList=[], rootNamespaceDict={}, raiseProcessorExceptions=False):
		self.raiseProcessorExceptions = raiseProcessorExceptions
		self.processors = datasetProcessorList
		self.rootNamespaceDict = rootNamespaceDict
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
		# loop through processors building up sdpresults dictionary and adding to report
		if len(self.processors)==0:
			return "<No dataset processors are configured>"
		try: # By default catch any exception so that an error processing does not stop scripts from continuing
			report = "Data written to file:" + concurrentScan.getDataWriter().getCurrentFileName() + "\n"
			self.results = {}
			allscannables = concurrentScan.getUserListedScannables()
			# load scan
			xfieldname, yfieldname = self.__determineKeyFieldNames(concurrentScan)
			xscannable = determineScannableContainingField(xfieldname, allscannables)
			yscannable = determineScannableContainingField(yfieldname, allscannables)
			if (len(yscannable.getInputNames())+len(yscannable.getExtraNames())) > 1:
				yfieldname = yscannable.name+"."+yfieldname
			if (len(xscannable.getInputNames())+len(xscannable.getExtraNames())) > 1:
				xfieldname = xscannable.name+"."+xfieldname
			self.last_scannable_scanned = determineScannableContainingField(xfieldname, allscannables)
			all_detectors_and_scannables = list(concurrentScan.getAllScannables()) + list(concurrentScan.getDetectors())
			self.sfh = loadScanFile(concurrentScan, [xfieldname, yfieldname], all_detectors_and_scannables)
			
			# determine from the scan object which fields are to be processed as x and y
			report += "   (Processing %s v's %s)\n"%(yfieldname,xfieldname)
			try:
				xDataSet = self.sfh.getAxis(xfieldname)
			except:
				return "<cannot analyse axis %s in scan file - unable to retrieve>" % yfieldname
			
			if xDataSet.dimensions[0] in (0,1):
				return "<Scan too short to process sensibly>"

			try:
				yDataSet = self.sfh.getAxis(yfieldname)
			except:
				return "<cannot analyse axis %s in scan file - unable to retrieve>" % yfieldname
			
			for processor in self.processors:
				sdpr = processor.process(xDataSet, yDataSet)
				sdpr = ScanDataProcessorResult(sdpr, self.sfh, allscannables, xfieldname, yfieldname)
				self.results[sdpr.name] = sdpr
				report += '   ' + (sdpr.name+':').ljust(8) + sdpr.report + "\n"
	
			# add results to root namespace dictionary
			d = self.rootNamespaceDict
			for name, result in self.results.items():
				if d.has_key(name):
					if not isinstance(d[name], ScanDataProcessorResult):
						raise Exception("Could not add ScanDataProcessorResult %s to root Jython namespace as object by that name already exists"%name)
				d[name] = result
				if self.duplicate_names.has_key(name):
					d[self.duplicate_names[name]] = result
			return ScanDataProcessorResults(self.results, report)

		except java.lang.Throwable, e: # Catch both Error and Exception
			if self.raiseProcessorExceptions:
				raise e
			return "Error processing scan file: " + traceback.format_exc() + "\n<< No exception raised >>>"
		
		except Exception, e:
			if self.raiseProcessorExceptions:
				raise e
			return "Error processing scan file: " + traceback.format_exc() + "\n<< No exception raised >>>"
	
	def prepareForScan(self):
		# Remove all pre-existing ScanDataProcessorResults
		self.last_scannable_scanned = None
		if self.processors:
			d = self.rootNamespaceDict
			for processor in self.processors:
				name = processor.name
				if d.has_key(name) and isinstance(d[name], ScanDataProcessorResult):
					del d[name]
				if self.duplicate_names.has_key(name):
					dupname = self.duplicate_names[name]
					if d.has_key(dupname) and isinstance(d[dupname], ScanDataProcessorResult):
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