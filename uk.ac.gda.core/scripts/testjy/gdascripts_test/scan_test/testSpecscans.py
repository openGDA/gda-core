from gdascripts.scan.specscans import *
from testjy.gdascripts_test.scan_test.testConcurrentScanWrapper import createScannables

import unittest

class TestSpecScan(unittest.TestCase):
	def testCheckArgStructForLoops(self):
		scan = SpecScan(None)
		a,b,c,mie = createScannables()
		scan.checkArgStructForLoops([[a], [b,1], [c], [mie,(1,2)]])
		self.assertRaises(Exception, scan.checkArgStructForLoops, [[a], [b,1], [c], [mie,(1,2),(1,2)]])
		self.assertRaises(Exception, scan.checkArgStructForLoops, [[a], [b,1], [c,1,2,3], [mie,(1,2),(1,2)]])


class TestBase(unittest.TestCase):
	def checkFlags(self, returnToStart, relativeScan):
		self.assertEqual(self.scan.returnToStart, returnToStart)
		self.assertEqual(self.scan.relativeScan, relativeScan)


class TestAscan(TestBase):
	
	def setUp(self):	
		self.scan = Ascan()

	def test__init__(self):
		self.checkFlags(returnToStart = False, relativeScan = False)
		
	def testConvertArgsToConcurrentScanArgs(self):
		convert = self.scan.convertArgStruct
		a,b,c,_ = createScannables()
		# ascan motor start finish intervals [scn [pos]]...
		self.assertEqual(convert([ [a,1,10,9] ]), 			[ [a, 1, 10 ,1] ])
		self.assertEqual(convert([ [a,10,1,9] ]), 			[ [a, 10, 1 ,-1] ])
		self.assertEqual(convert([ [a,1,10,9],[b,1],[c] ]),	[ [a,1,10,1],[b,1],[c] ])	
		self.assertRaises(Exception,convert,[ [a,1,10,9],[b,1,2,3],[c] ] )
		self.assertRaises(Exception,convert,[ [a,1,10,9],[b,1,2],[c] ] )


class TestA2scan(TestBase):
	
	def setUp(self):	
		self.scan = A2scan()

	def test__init__(self):
		self.checkFlags(returnToStart = False, relativeScan = False)
		
	def testConvertArgsToConcurrentScanArgs(self):
		convert = self.scan.convertArgStruct
		a,b,c,_ = createScannables()
		# a2scan m1 s1 f1 m2 s2 f2 intervals [scn [pos]] ...
		self.assertEqual(convert([ [a,1,10],[b,11,20,9] ]), 			[ [a, 1,10,1], [b,11,20,1] ])
		self.assertEqual(convert([ [a,1,10],[b,20,11,9] ]), 			[ [a, 1,10,1], [b,20,11,-1] ])
		self.assertEqual(convert([ [a,1,10],[b,11,20,9],[b,1],[c] ]), 			[ [a, 1,10,1], [b,11,20,1],[b,1],[c] ])	
		self.assertRaises(Exception,convert,[ [a,1,10],[b,11,20,9],[b,1,2],[c] ] )
		self.assertRaises(Exception,convert,[ [a,1,10],[b,11,20,9],[b,1,2,3],[c] ] )


class TestA3scan(TestBase):
	
	def setUp(self):	
		self.scan = A3scan()

	def test__init__(self):
		self.checkFlags(returnToStart = False, relativeScan = False)
		
	def testConvertArgsToConcurrentScanArgs(self):
		convert = self.scan.convertArgStruct
		a,b,c,_ = createScannables()
		# a3scan m1 s1 f1 m2 s2 f2 m3 s3 f3 intervals [scn [pos]] ...
		self.assertEqual(convert([ [a,1,10],[b,11,20],[c,21,30,9] ]), 			[ [a, 1,10,1], [b,11,20,1], [c,21,30,1]])
		self.assertEqual(convert([ [a,10,1],[b,11,20],[c,30,21,9] ]), 			[ [a, 10,1,-1], [b,11,20,1], [c,30,21,-1]])
		self.assertEqual(convert([ [a,10,1],[b,11,20],[c,30,21,9],[b,1],[c] ]), 			[ [a, 10,1,-1], [b,11,20,1], [c,30,21,-1],[b,1],[c]])

		self.assertRaises(Exception,convert,[ [a,10,1],[b,11,20],[c,30,21,9],[b,1,2],[c] ])
		self.assertRaises(Exception,convert,[ [a,10,1],[b,11,20],[c,30,21,9],[b,1,2,3],[c] ])

	
class TestMesh(TestBase):
	
	def setUp(self):	
		self.scan = Mesh()

	def test__init__(self):
		self.checkFlags(returnToStart = False, relativeScan = False)
		
	def testConvertArgsToConcurrentScanArgs(self):
		convert = self.scan.convertArgStruct
		a,b,c,_ = createScannables()
		#mesh m1 s1 f1 intervals1 m2 s2 f2 intervals2 time ...
		self.assertEqual(convert([ [a,1,10,18],[b,11,20,9] ]), 			[ [a, 1,10,.5], [b,11,20,1] ])
		self.assertEqual(convert([ [a,1,10,18],[b,20,11,9] ]), 			[ [a, 1,10,.5], [b,20,11,-1] ])
		self.assertEqual(convert([ [a,1,10,18],[b,11,20,9],[b,1],[c] ]), 			[ [a, 1,10,.5], [b,11,20,1],[b,1],[c] ])	
		self.assertRaises(Exception,convert,[ [a,1,10,18],[b,11,20,9],[b,1,2],[c] ] )
		self.assertRaises(Exception,convert,[ [a,1,10,18],[b,11,20,9],[b,1,2,3],[c] ] )

class TestDscan(TestBase):
	
	def setUp(self):	
		self.scan = Dscan()

	def test__init__(self):
		self.checkFlags(returnToStart = True, relativeScan = True)
		

class TestD2scan(TestBase):
	
	def setUp(self):	
		self.scan = D2scan()

	def test__init__(self):
		self.checkFlags(returnToStart = True, relativeScan = True)


class TestD3scan(TestBase):
	
	def setUp(self):	
		self.scan = D3scan()

	def test__init__(self):
		self.checkFlags(returnToStart = True, relativeScan = True)



#mesh m1 s1 f1 intervals1 m2 s2 f2 intervals2 time
def suite():
	return unittest.TestSuite((
							unittest.TestLoader().loadTestsFromTestCase(TestSpecScan),
							unittest.TestLoader().loadTestsFromTestCase(TestAscan),
							unittest.TestLoader().loadTestsFromTestCase(TestA2scan),
							unittest.TestLoader().loadTestsFromTestCase(TestA3scan),
							unittest.TestLoader().loadTestsFromTestCase(TestMesh),
							unittest.TestLoader().loadTestsFromTestCase(TestDscan),
							unittest.TestLoader().loadTestsFromTestCase(TestD2scan),
							unittest.TestLoader().loadTestsFromTestCase(TestD3scan)
							))
if __name__ == '__main__':
	unittest.TextTestRunner(verbosity=2).run(suite())
