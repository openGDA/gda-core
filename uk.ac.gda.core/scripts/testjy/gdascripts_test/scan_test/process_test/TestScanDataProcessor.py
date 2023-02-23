from gdascripts.scan.process.ScanDataProcessor import ScanDataProcessor
from testjy.gdascripts_test.scan_test.process_test.TestScanDataProcessorResult import createSimpleScanFileHolderAndScannables, createSimpleScanFileHolderWithOneValueAndScannables,\
	MockScannable
from testjy.gdascripts_test.analysis_test.datasetprocessor_test.oned_test.testXYDataSetProcessor import SimpleXYDataSetProcessorWithError
from gdascripts.analysis.datasetprocessor.oned.MaxPositionAndValue import MaxPositionAndValue
from gdascripts.analysis.datasetprocessor.oned.MinPositionAndValue import MinPositionAndValue
from gdascripts.analysis.datasetprocessor.oned.scan_stitching import Lcen, Rcen
import java.lang.Exception #@UnresolvedImport
import java.lang.Error #@UnresolvedImport

import unittest
from mock import Mock
from gdascripts.scan.process.ScanDataProcessorResult import ScanDataProcessorResult
from gda.scan import ConcurrentScan
import os
import array

def MockConcurrentScan(filename = '1234.dat', scanDims=None):
	w, x, y, z, _ = createSimpleScanFileHolderAndScannables()
	mock = Mock()
	mock.getScanPlotSettings.return_value = Mock()
	mock.getScanPlotSettings.return_value.getYAxesShown.return_value = ['yi','ye']
	mock.getScanPlotSettings.return_value.getXAxisName.return_value = 'x'
	mock.getScanInformation.return_value.getDimensions.return_value = scanDims
	mock.getUserListedScannables.return_value = [x, y, z, w]
	mock.getAllScannables.return_value = [x, y, z, w]
	mock.getDetectors.return_value = []
	mock.getDataWriter.return_value = Mock()
	filepath = os.path.join(os.path.dirname(__file__), filename)
	mock.getDataWriter.return_value.getCurrentFileName.return_value = filepath
	return mock

def createMockConcurrentScanForRealNexusFile():

	filepath = os.path.join(os.path.dirname(__file__), 'i22-166406.nxs')
	a = MockScannable('base_x', ['base_x'], [])
	b = MockScannable('bsdiode', ['bsdiode'], [])

	mock = Mock()
	mock.getScanPlotSettings.return_value = Mock()
	mock.getScanPlotSettings.return_value.getYAxesShown.return_value = ['bsdiode']
	mock.getScanPlotSettings.return_value.getXAxisName.return_value = 'base_x'
	mock.getScanInformation.return_value.getDimensions.return_value = array.array('i', [33])
	mock.getUserListedScannables.return_value = [a, b]
	mock.getAllScannables.return_value = [a, b]
	mock.getDetectors.return_value = []
	mock.getDataWriter.return_value = Mock()
	mock.getDataWriter.return_value.getCurrentFileName.return_value = filepath
	return mock

class TestScanDataProcessor(unittest.TestCase):

	def setUp(self):
		self.w, self.x, self.y, self.z, self.sfh = createSimpleScanFileHolderAndScannables()
		self.concurrentScan = MockConcurrentScan(scanDims=array.array('i', [10]))

	def testProcessScanWithNoProcessors(self):
		rootNamespaceDict = {'a':1}
		sdp = ScanDataProcessor([], rootNamespaceDict, raiseProcessorExceptions=True)
		self.assertEquals(sdp.processScan(self.concurrentScan),'<No dataset processors are configured>')
		self.assertEquals(rootNamespaceDict, {'a':1})

	def testProcessScan(self):
		rootNamespaceDict = {}
		sdp = ScanDataProcessor([MaxPositionAndValue(), MinPositionAndValue()], rootNamespaceDict, raiseProcessorExceptions=True)
		report = sdp.processScan(self.concurrentScan)
		print "report:"
		print "***"
		print report
		print "***"
		self.assertEquals(report['minval'].name, 'minval')
		self.assertEquals(report['maxval'].name, 'maxval')
		self.assertEquals(self.x.name, sdp.last_scannable_scanned.name)
		print rootNamespaceDict

	def testProcessScanLcenAndRcen(self):
		rootNamespaceDict = {}
		lcen = Lcen()
		rcen = Rcen()
		lcen.raise_process_exceptions = True
		rcen.raise_process_exceptions = True
		sdp = ScanDataProcessor([lcen, rcen], rootNamespaceDict, raiseProcessorExceptions=True)
		report = sdp.processScan(self.concurrentScan)
		print "report:"
		print "***"
		print report
		print "***"
		self.assertEquals(report['lcen'].name, 'lcen')
		self.assertEquals(report['rcen'].name, 'rcen')
		print rootNamespaceDict

	def testProcessScanWithMultiInputXScannable(self):
		concurrentScan = MockConcurrentScan(scanDims=array.array('i', [10]))
		concurrentScan.getUserListedScannables.return_value = [self.w, self.x, self.y, self.z]
		concurrentScan.getScanPlotSettings.return_value.getXAxisName.return_value = 'wi1'
		sdp = ScanDataProcessor([MaxPositionAndValue(), MinPositionAndValue()], {}, raiseProcessorExceptions=True)
		report = sdp.processScan(concurrentScan)
		print "report:"
		print "***"
		print report
		print "***"

	def testProcessScanNamespaceWritingWorksTwice(self):
		sdp = ScanDataProcessor([MaxPositionAndValue(), MinPositionAndValue()], {}, raiseProcessorExceptions=True)
		sdp.processScan(self.concurrentScan)
		sdp.processScan(self.concurrentScan)

	def testPrepareForScan(self):
		dataSetResult = Mock()
		dataSetResult.resultsDict = {'key':None}
		dataSetResult.keyxlabel = 'key'
		sdpr = ScanDataProcessorResult(dataSetResult, None, None, None, None)
		namespace = {'myobject': 1, 'minval': 'Non SDPR should be left alone at this stage', 'maxval': sdpr}
		sdp = ScanDataProcessor([MaxPositionAndValue(), MinPositionAndValue()], namespace, raiseProcessorExceptions=True)
		sdp.prepareForScan()
		self.assertEquals(namespace, {'myobject': 1, 'minval': 'Non SDPR should be left alone at this stage'})

	def testPrepareForScanWithDuplicatedNames(self):
		dataSetResult = Mock()
		dataSetResult.resultsDict = {'key':None}
		dataSetResult.keyxlabel = 'key'
		sdpr = ScanDataProcessorResult(dataSetResult, None, None, None, None)
		namespace = {'myobject': 1, 'minval': 'Non SDPR should be left alone at this stage', 'maxval': sdpr,'maxpos': sdpr }
		sdp = ScanDataProcessor([MaxPositionAndValue(), MinPositionAndValue()], namespace, raiseProcessorExceptions=True)
		sdp.duplicate_names = {'maxval':'maxpos', 'minval':'minpos'}
		sdp.prepareForScan()
		self.assertEquals(namespace, {'myobject': 1, 'minval': 'Non SDPR should be left alone at this stage'})

	def testPrepareForScanDisablesGo(self):
		sdp = ScanDataProcessor([MaxPositionAndValue(), MinPositionAndValue()], {}, raiseProcessorExceptions=True)
		sdp.last_scannable_scanned = self.x
		sdp.prepareForScan()
		self.assertRaises(Exception, sdp.go, 3)

	def testProcessScanWithJythonException(self):
		concurrentScan = Mock()
		concurrentScan.getUserListedScannables.side_effect = Exception("e")
		sdp = ScanDataProcessor([], {})
		result = sdp.processScan(concurrentScan)
		print "***"
		print result
		print "***"

	def testProcessScanWithJavaException(self):
		concurrentScan = Mock()
		def r():
			raise java.lang.Exception("e")
		concurrentScan.getUserListedScannables = r
		sdp = ScanDataProcessor([], {})
		result = sdp.processScan(concurrentScan)
		print "***"
		print result
		print "***"

	def testProcessScanWithJavaError(self):
		concurrentScan = Mock()
		def r():
			raise java.lang.Error("e")
		concurrentScan.getUserListedScannables = r
		sdp = ScanDataProcessor([], {})
		sdp.processScan(concurrentScan)

	def testGoCalledWithSDPResult(self):
		sdpresult = Mock()
		sdp = ScanDataProcessor([], {})
		sdp.go(sdpresult)
		sdpresult.go.assert_called_with()

	def testGoCalledWithNoScannableFromLastScan(self):
		sdp = ScanDataProcessor([], {})
		self.assertRaises(Exception, sdp.go, 2)

	def testGoWithScannbaleFromLastScan(self):
		scannable = Mock()
		sdp = ScanDataProcessor([], {})
		sdp.last_scannable_scanned = scannable
		sdp.go(1)
		scannable.moveTo.assert_called_with(1)
		sdp.go([1])
		scannable.moveTo.assert_called_with([1])
		sdp.go([1,2])
		scannable.moveTo.assert_called_with([1, 2])

class TestScanDataProcessorWithOnlyOnePoint(TestScanDataProcessor):

	def setUp(self):
		self.w, self.x, self.y, self.z, self.sfh = createSimpleScanFileHolderWithOneValueAndScannables()
		self.concurrentScan = MockConcurrentScan(filename='1234-singlepoint.dat', scanDims=array.array('i', [1]))

	def testProcessScan(self):
		rootNamespaceDict = {}
		sdp = ScanDataProcessor([MaxPositionAndValue(), MinPositionAndValue()], rootNamespaceDict, raiseProcessorExceptions=True)
		report = sdp.processScan(self.concurrentScan)
		self.assertEquals(report, 'Scan too short to process sensibly')

	def testProcessScanLcenAndRcen(self):
		rootNamespaceDict = {}
		lcen = Lcen()
		rcen = Rcen()
		lcen.raise_process_exceptions = True
		rcen.raise_process_exceptions = True
		sdp = ScanDataProcessor([lcen, rcen], rootNamespaceDict, raiseProcessorExceptions=True)
		report = sdp.processScan(self.concurrentScan)
		self.assertEquals(report, 'Scan too short to process sensibly')
		print rootNamespaceDict

class TestScanDataProcessorIntegrationWithRealNexusFile(unittest.TestCase):

	def testProcessScanFile1(self):
		self.concurrentScan = createMockConcurrentScanForRealNexusFile()
		rootNamespaceDict = {}
		min_processor = MinPositionAndValue()
		max_processor = MaxPositionAndValue()
		min_processor.raise_process_exceptions = True
		max_processor.raise_process_exceptions = True

		sdp = ScanDataProcessor([min_processor, max_processor], rootNamespaceDict, raiseProcessorExceptions=True)
		sdp.raiseProcessorExceptions = True
		result = sdp.processScan(self.concurrentScan)
		self.assertAlmostEqual(result['maxval'].result.maxpos, -50)

class TestScanDataProcessorWithEceptionRaisingProcessor(TestScanDataProcessor):

	def testProcessScan(self):
		rootNamespaceDict = {}
		sdp = ScanDataProcessor([MaxPositionAndValue(), MinPositionAndValue(), SimpleXYDataSetProcessorWithError()], rootNamespaceDict, raiseProcessorExceptions=True)
		report = sdp.processScan(self.concurrentScan)
		print report
		print rootNamespaceDict

	def testProcessScanWithMultiInputXScannable(self):
		concurrentScan = MockConcurrentScan()

		sdp = ScanDataProcessor([MaxPositionAndValue(), MinPositionAndValue()], {})
		report = sdp.processScan(concurrentScan)
		print "report:"
		print "***"
		print report
		print "***"


class MockScanDataPointCache():

	def __init__(self, sfh):
		self.sfh = sfh

	def getPositionsFor(self, scannableName):
		return self.sfh[scannableName]


class TestScanDataProcessorWithSDPC(unittest.TestCase):

	def setUp(self):
		self.w, self.x, self.y, self.z, self.sfh = createSimpleScanFileHolderAndScannables()
		self.concurrentScan = MockConcurrentScan(scanDims=array.array('i', [10]))
		self.sdpc = MockScanDataPointCache(self.sfh)

	def testProcessScanWithNoProcessors(self):
		rootNamespaceDict = {'a':1}
		sdp = ScanDataProcessor([], rootNamespaceDict, raiseProcessorExceptions=True, scanDataPointCache=self.sdpc)
		self.assertEquals(sdp.processScan(self.concurrentScan),'<No dataset processors are configured>')
		self.assertEquals(rootNamespaceDict, {'a':1})

	def testProcessScan(self):
		rootNamespaceDict = {}
		sdp = ScanDataProcessor([MaxPositionAndValue(), MinPositionAndValue()], rootNamespaceDict, raiseProcessorExceptions=True, scanDataPointCache=self.sdpc)
		report = sdp.processScan(self.concurrentScan)
		print "report:"
		print "***"
		print report
		print "***"
		self.assertEquals(report['minval'].name, 'minval')
		self.assertEquals(report['maxval'].name, 'maxval')
		self.assertEquals(self.x.name, sdp.last_scannable_scanned.name)
		print rootNamespaceDict

	def testProcessScanLcenAndRcen(self):
		rootNamespaceDict = {}
		lcen = Lcen()
		rcen = Rcen()
		lcen.raise_process_exceptions = True
		rcen.raise_process_exceptions = True
		sdp = ScanDataProcessor([lcen, rcen], rootNamespaceDict, raiseProcessorExceptions=True, scanDataPointCache=self.sdpc)
		report = sdp.processScan(self.concurrentScan)
		print "report:"
		print "***"
		print report
		print "***"
		self.assertEquals(report['lcen'].name, 'lcen')
		self.assertEquals(report['rcen'].name, 'rcen')
		print rootNamespaceDict

	def testProcessScanWithMultiInputXScannable(self):
		concurrentScan = MockConcurrentScan(scanDims=array.array('i', [10]))
		concurrentScan.getUserListedScannables.return_value = [self.w, self.x, self.y, self.z]
		concurrentScan.getScanPlotSettings.return_value.getXAxisName.return_value = 'wi1'
		sdp = ScanDataProcessor([MaxPositionAndValue(), MinPositionAndValue()], {}, raiseProcessorExceptions=True, scanDataPointCache=self.sdpc)
		report = sdp.processScan(concurrentScan)
		print "report:"
		print "***"
		print report
		print "***"

	def testProcessScanNamespaceWritingWorksTwice(self):
		sdp = ScanDataProcessor([MaxPositionAndValue(), MinPositionAndValue()], {}, raiseProcessorExceptions=True, scanDataPointCache=self.sdpc)
		sdp.processScan(self.concurrentScan)
		sdp.processScan(self.concurrentScan)

	def testPrepareForScan(self):
		dataSetResult = Mock()
		dataSetResult.resultsDict = {'key':None}
		dataSetResult.keyxlabel = 'key'
		sdpr = ScanDataProcessorResult(dataSetResult, None, None, None, None, scanDataPointCache=self.sdpc)
		namespace = {'myobject': 1, 'minval': 'Non SDPR should be left alone at this stage', 'maxval': sdpr}
		sdp = ScanDataProcessor([MaxPositionAndValue(), MinPositionAndValue()], namespace, raiseProcessorExceptions=True, scanDataPointCache=self.sdpc)
		sdp.prepareForScan()
		self.assertEquals(namespace, {'myobject': 1, 'minval': 'Non SDPR should be left alone at this stage'})

	def testPrepareForScanWithDuplicatedNames(self):
		dataSetResult = Mock()
		dataSetResult.resultsDict = {'key':None}
		dataSetResult.keyxlabel = 'key'
		sdpr = ScanDataProcessorResult(dataSetResult, None, None, None, None, scanDataPointCache=self.sdpc)
		namespace = {'myobject': 1, 'minval': 'Non SDPR should be left alone at this stage', 'maxval': sdpr,'maxpos': sdpr }
		sdp = ScanDataProcessor([MaxPositionAndValue(), MinPositionAndValue()], namespace, raiseProcessorExceptions=True, scanDataPointCache=self.sdpc)
		sdp.duplicate_names = {'maxval':'maxpos', 'minval':'minpos'}
		sdp.prepareForScan()
		self.assertEquals(namespace, {'myobject': 1, 'minval': 'Non SDPR should be left alone at this stage'})

	def testPrepareForScanDisablesGo(self):
		sdp = ScanDataProcessor([MaxPositionAndValue(), MinPositionAndValue()], {}, raiseProcessorExceptions=True, scanDataPointCache=self.sdpc)
		sdp.last_scannable_scanned = self.x
		sdp.prepareForScan()
		self.assertRaises(Exception, sdp.go, 3)

	def testProcessScanWithJythonException(self):
		concurrentScan = Mock()
		concurrentScan.getUserListedScannables.side_effect = Exception("e")
		sdp = ScanDataProcessor([], {}, scanDataPointCache=self.sdpc)
		result = sdp.processScan(concurrentScan)
		print "***"
		print result
		print "***"

	def testProcessScanWithJavaException(self):
		concurrentScan = Mock()
		def r():
			raise java.lang.Exception("e")
		concurrentScan.getUserListedScannables = r
		sdp = ScanDataProcessor([], {}, scanDataPointCache=self.sdpc)
		result = sdp.processScan(concurrentScan)
		print "***"
		print result
		print "***"

	def testProcessScanWithJavaError(self):
		concurrentScan = Mock()
		def r():
			raise java.lang.Error("e")
		concurrentScan.getUserListedScannables = r
		sdp = ScanDataProcessor([], {}, scanDataPointCache=self.sdpc)
		sdp.processScan(concurrentScan)

	def testGoCalledWithSDPResult(self):
		sdpresult = Mock()
		sdp = ScanDataProcessor([], {}, scanDataPointCache=self.sdpc)
		sdp.go(sdpresult)
		sdpresult.go.assert_called_with()

	def testGoCalledWithNoScannableFromLastScan(self):
		sdp = ScanDataProcessor([], {}, scanDataPointCache=self.sdpc)
		self.assertRaises(Exception, sdp.go, 2)

	def testGoWithScannbaleFromLastScan(self):
		scannable = Mock()
		sdp = ScanDataProcessor([], {}, scanDataPointCache=self.sdpc)
		sdp.last_scannable_scanned = scannable
		sdp.go(1)
		scannable.moveTo.assert_called_with(1)
		sdp.go([1])
		scannable.moveTo.assert_called_with([1])
		sdp.go([1,2])
		scannable.moveTo.assert_called_with([1, 2])


def suite():
	return unittest.TestSuite((
		unittest.TestLoader().loadTestsFromTestCase(TestScanDataProcessor),
		unittest.TestLoader().loadTestsFromTestCase(TestScanDataProcessorWithOnlyOnePoint),
		unittest.TestLoader().loadTestsFromTestCase(TestScanDataProcessorWithEceptionRaisingProcessor),
		unittest.TestLoader().loadTestsFromTestCase(TestScanDataProcessorIntegrationWithRealNexusFile),
		unittest.TestLoader().loadTestsFromTestCase(TestScanDataProcessorWithSDPC)
	))

if __name__ == '__main__':
	unittest.TextTestRunner(verbosity=2).run(suite())
