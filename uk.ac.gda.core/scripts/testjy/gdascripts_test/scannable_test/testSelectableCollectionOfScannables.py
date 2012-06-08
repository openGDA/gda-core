from gdascripts.scannable.SelectableCollectionOfScannables import SelectableCollectionOfScannables

import unittest
from testjy.gdascripts_test.scan_test.testConcurrentScanWrapper import createScannables
from gdascripts.pd.dummy_pds import  MultiInputExtraFieldsDummyPD



class TestSelectableCollectionOfScannables(unittest.TestCase):

	def setUp(self):
		self.a, self.b, self.c, _ = createScannables()
		self.mie = MultiInputExtraFieldsDummyPD('mie', ['i1'], ['e1','e2'])
		self.col = SelectableCollectionOfScannables('c', [self.a, self.b, self.mie, self.c])

	def test__init__(self):
		self.assertEquals(list(self.col.inputNames), ['exp'])
	
	def testGetReorderedScannables(self):
		self.assertEquals(self.col.selectedIdx, 3)
		self.assertEquals(self.col.getReorderedScannables(), [self.a, self.b, self.mie, self.c])
		
		self.col.selectScannable(0)
		self.assertEquals(self.col.getReorderedScannables(), [self.b, self.mie, self.c, self.a])

		self.col.selectScannable(2)
		self.assertEquals(self.col.getReorderedScannables(), [self.a, self.b, self.c, self.mie])	
	
	def testAsynchronousMoveTo(self):
		self.col.asynchronousMoveTo(1.2)
		self.assertEquals(self.a.getPosition(), 1.2)
		self.assertEquals(self.b.getPosition(), 1.2)
		self.assertEquals(self.c.getPosition(), 1.2)				
		self.assertEquals(self.mie.getPosition(), [1.2, 100., 101.])			

	def testGetOutputFormat(self):
		self.a.outputFormat=['a']
		self.b.outputFormat=['b']		
		self.c.outputFormat=['c']		
		self.mie.outputFormat=['mie']		
		self.col.selectScannable(2)
		self.assertEquals(self.col.getOutputFormat(), ['%f','a', 'b', 'c', 'mie'])

	def testExtraNames(self):
		self.col.selectScannable(2)
		self.assertEquals(list(self.col.extraNames), ['a', 'b', 'c', 'i1', 'e1', 'e2'])

	def testGetPosition(self):
		self.col.moveTo(.001)	
		self.a.moveTo(1.)
		self.b.moveTo(2.)		
		self.c.moveTo(3.)		
		self.mie.moveTo(4.)					
		self.col.selectScannable(2)
		self.assertEquals(list(self.col.getPosition()), [.001, 1., 2., 3., 4., 100., 101.])



def suite():
	return unittest.TestLoader().loadTestsFromTestCase(TestSelectableCollectionOfScannables)

if __name__ == '__main__':
	unittest.TextTestRunner(verbosity=2).run(suite())