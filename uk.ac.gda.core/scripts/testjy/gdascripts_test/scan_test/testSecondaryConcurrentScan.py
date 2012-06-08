from testjy.gdascripts_test.scan_test.testConcurrentScanWrapper import createScannables

from gdascripts.scan.SecondaryConcurrentScan import SecondaryConcurrentScan
import unittest

class TestSecondaryConcurrentScan(unittest.TestCase):

	def setUp(self):
		self.a, self.b, self.c, self.mie = createScannables()
		self.scan = SecondaryConcurrentScan([self.a, 1, 3, 1, self.b, 4, self.c])

	def test__init__(self):
		pass

	def testMoveAndReadScannableFields(self):
		move = self.scan.moveAndReadScannableFields
		self.a.moveTo(0)
		self.b.moveTo(1)
		self.c.moveTo(2)
		self.mie.moveTo([10,11])		
		self.assertEquals(move([[self.b],[self.c]]), [1,2])
		self.assertEquals(move([[self.b,3],[self.c]]), [3,2])
		self.assertEquals(move([[self.b ,4],[self.c, 5]]), [4,5])
		self.assertEquals(move([[self.mie]]), [10.0, 11.0, 100.0, 101.0])
		self.assertEquals(move([[self.mie,[20.,21.]]]), [20.0, 21.0, 100.0, 101.0])		
		

	def testScanIntoArrayAndSecondaryPlot(self):	
		r = self.scan.scanIntoArrayAndSecondaryPlot([[self.a, 1, 4, 1], [self.b, 4], [self.c]])
		
	def testScanIntoArrayAndSecondaryPlotWithReadMie(self):		
		r = self.scan.scanIntoArrayAndSecondaryPlot([[self.a, 1, 4, 1], [self.b, 4], [self.mie]])

	def testScanIntoArrayAndSecondaryPlotWithMovedMie(self):		
		#r = self.scan.scanIntoArrayAndSecondaryPlot([[self.mie, (0,0), (4,8), (1,2)], [self.b, 4], [self.mie]])
		pass # frange does not support this. Skip for this hacked solution anyway
def suite():
	return unittest.TestLoader().loadTestsFromTestCase(TestSecondaryConcurrentScan)

if __name__ == '__main__':
	unittest.TextTestRunner(verbosity=2).run(suite())