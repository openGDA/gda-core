from gdascripts.scan.gdascans import Scan
from gdascripts.scan.trajscans import TrajScan
from mock import Mock
from testjy.gdascripts_test.scan_test.testConcurrentScanWrapper import createScannables
import gda.scan.ConcurrentScan
import gda.scan.TrajectoryScanLine
import gdascripts.scan #@UnusedImport
import unittest

class TestBase(unittest.TestCase):

	def setUp(self):
		self.mockConcurrentScan = Mock() # object
		self.MockConcurrentScan = Mock() # class
		self.MockConcurrentScan.return_value = self.mockConcurrentScan
		
		self.mockTrajectoryScanLine = Mock() # object
		self.mockTrajectoryScanLine.getNumberPoints.return_value = 10
		self.MockTrajectoryScanLine = Mock() # class
		self.MockTrajectoryScanLine.return_value = self.mockTrajectoryScanLine
		
		gdascripts.scan.concurrentScanWrapper.ConcurrentScan = self.MockConcurrentScan
		gdascripts.scan.trajscans.TrajectoryScanLine = self.MockTrajectoryScanLine
		gdascripts.scan.trajscans.ConcurrentScan = self.MockConcurrentScan
		
		gdascripts.scan.trajscans.setDefaultScannables = Mock()

	def tearDown(self):
		gdascripts.scan.concurrentScanWrapper.ConcurrentScan = gda.scan.ConcurrentScan
		gdascripts.scan.trajscans.TrajectoryScanLine = gda.scan.TrajectoryScanLine
		gdascripts.scan.trajscans.ConcurrentScan = gda.scan.ConcurrentScan


class TestScan(TestBase):
	# Just as a baseline test

	def test_createScan(self):
		a, b, c, _ = createScannables()
		scanobject = Scan()._createScan([a, 1, 10, 1, b, 2, c])
		self.assertEqual(scanobject, self.mockConcurrentScan)
		self.MockConcurrentScan.assert_called_with([a, 1, 10, 1, b, 2, c])


class TestTrajScan(TestBase):


	def testDoc(self):
		print TrajScan().__doc__

	def test_createScan(self):
		
		a, b, c, mied = createScannables()
		scanobject = TrajScan()._createScan([a, 1, 10, 1, b, 2, c])
		
		self.MockTrajectoryScanLine.assert_called_with([a, 1, 10, 1, b, 2, c])
		self.assertEqual(scanobject, self.mockTrajectoryScanLine)

	def test_createScanWithTuple(self):
		a, b, c, _ = createScannables()
		scanobject = TrajScan()._createScan([a, (1, 2, 3), b, 2, c])
		
		self.MockTrajectoryScanLine.assert_called_with([a, (1, 2, 3), b, 2, c])
		self.assertEqual(scanobject, self.mockTrajectoryScanLine)
		
	def test_createNestedOnceScan(self):
		a, b, c, mie = createScannables()
		scanobject = TrajScan()._createScan([a, 1, 10, 1, b, 2, 20, 2, c, 1, mie])
		
		self.MockTrajectoryScanLine.assert_called_with([b, 2, 20, 2, c, 1, mie])
		self.MockConcurrentScan.assert_called_with([a, 1, 10, 1, self.mockTrajectoryScanLine])
		self.assertEqual(scanobject, self.mockConcurrentScan)
		
	def test_createNestedOnceScanWithTuple(self):
		a, b, c, mie = createScannables()
		scanobject = TrajScan()._createScan([a, 1, 10, 1, b, (1,2,3), c, 1, mie])
		
		self.MockTrajectoryScanLine.assert_called_with([b, (1,2,3), c, 1, mie])
		self.MockConcurrentScan.assert_called_with([a, 1, 10, 1, self.mockTrajectoryScanLine])
		self.assertEqual(scanobject, self.mockConcurrentScan)
		

def suite():
	return unittest.TestSuite((
							unittest.TestLoader().loadTestsFromTestCase(TestScan),
							unittest.TestLoader().loadTestsFromTestCase(TestTrajScan),
							))	

if __name__ == '__main__':
	unittest.TextTestRunner(verbosity=2).run(suite())
