from gdascripts.scan import gdascans
from testjy.gdascripts_test.scan_test.testConcurrentScanWrapper import createScannables
from testjy.gdascripts_test.scan_test.testSpecscans import TestBase

import unittest


class TestScan(unittest.TestCase):

	def setUp(self):	
		self.scan = gdascans.Scan()

	def test__init__(self):
		self.assertEqual(self.scan.returnToStart, False)
		self.assertEqual(self.scan.relativeScan, False)
		
	def testConvertArgsToConcurrentScanArgs(self):
		# Well tested in testConcurrentScanWrapper already!
		a,b,c,_ = createScannables()
		argStruct =[[a, 1, 2, 1], [b, 3, 5, 1], [c]]
		self.assertEqual(self.scan.convertArgStruct(argStruct), argStruct)


class TestRscan(TestBase):
	
	def setUp(self):	
		self.scan = gdascans.Rscan()

	def test__init__(self):
		self.checkFlags(returnToStart = True, relativeScan = True)


class TestCscan(TestBase):
	
	def setUp(self):	
		self.scan = gdascans.Cscan()

	def test__init__(self):
		self.checkFlags(returnToStart = True, relativeScan = True)
		
	def testConvertArgsToConcurrentScanArgs(self):
		convert = self.scan.convertArgStruct
		a,b,c,_ = createScannables()
		# cscan motor halfwidths step [motor halfwidth step] ...
		self.assertEqual(convert([ [a,10,1] ]), [ [a, -10, 10 ,1] ])
		self.assertEqual(convert([ [a,10,1], [b,5,2]] ), [[a,-10,10,1], [b,-5,5,2]])
		self.assertEqual(convert([ [a,10,1], [b, 1]]), [[a,-10,10,1], [b, 1]])
		self.assertEqual(convert([ [a,10,1], [b, 1], [c]]), [[a,-10,10,1], [b, 1], [c]])

	def testConvertArgsToConcurrentScanArgsWithMultipleInputFields(self):
		convert = self.scan.convertArgStruct
		a,b,c,_ = createScannables()
		# cscan motor halfwidths step [motor halfwidth step] ...
		self.assertEqual(convert([ [a,[10, 100],[1, 10]] ]), [ [a, [-10, -100], [10, 100] ,[1, 10]] ])
		self.assertEqual(convert([ [a,[10, 100],[1, 10]], [b,5,2]] ), [[a,[-10, -100],[10, 100],[1, 10]], [b,-5,5,2]])
		self.assertEqual(convert([ [a,[10, 100],[1, 10]], [b, 1]]), [[a,[-10, -100],[10, 100],[1, 10]], [b, 1]])
		self.assertEqual(convert([ [a,[10, 100],[1, 10]], [b, 1], [c]]), [[a,[-10, -100],[10, 100],[1, 10]], [b, 1], [c]])


class TestScancn(TestBase):
	
	def setUp(self):	
		self.scan = gdascans.Scancn()

	def test__init__(self):
		self.checkFlags(returnToStart = True, relativeScan = True)
		
	def testConvertArgsToConcurrentScanArgs(self):
		convert = self.scan.convertArgStruct
		a,b,c,_ = createScannables()
		# cscan motor halfwidths step [motor halfwidth step] ...
		self.assertEqual(convert([ [a,.2,10] ]), [ [a, -.9, .9 , .2] ])
		self.assertEqual(convert([ [a, .2, 9] ]), [ [a, -.8, .8 , .2] ])
		self.assertEqual(convert([ [a, .2, 9], [b,1, 2] ]), [ [a, -.8, .8 , .2], [b,1,2,] ])
		
	def testConvertArgsToConcurrentScanArgsWithMultipleInputFields(self):
		convert = self.scan.convertArgStruct
		a,b,c,_ = createScannables()
		# cscan motor halfwidths step [motor halfwidth step] ...
		self.assertEqual(convert([ [a, [.2, 20],10] ]), [ [a, [-.9, -90], [.9, 90] , [.2, 20]] ])
		self.assertEqual(convert([ [a, [.2, 20], 9] ]), [ [a, [-.8, -80] , [.8, 80] , [.2, 20]] ])
		self.assertEqual(convert([ [a, [.2, 20], 9], [b,1, 2] ]), [ [a, [-.8, -80], [.8, 80] ,  [.2, 20]], [b,1,2,] ])

def suite():
	return unittest.TestSuite((
							unittest.TestLoader().loadTestsFromTestCase(TestScan),
							unittest.TestLoader().loadTestsFromTestCase(TestRscan),
							unittest.TestLoader().loadTestsFromTestCase(TestCscan),
							unittest.TestLoader().loadTestsFromTestCase(TestScancn)
							))	

if __name__ == '__main__':
	unittest.TextTestRunner(verbosity=2).run(suite())
