from gdascripts.scannable.ScanFileHolderScannable import ScanFileHolderScannable,\
	SFHInterpolatorWithHashAccess, quantise
from gdascripts.scannable.ScanFileHolderScannable import SFHInterpolator
from gda.analysis import ScanFileHolder
from org.eclipse.january.dataset import DatasetFactory


import unittest


def createSFH():

	result = ScanFileHolder()
	result.addDataSet('i1', DatasetFactory.createFromObject([0,1,2,3,4,5,6,7,8,9]))
	result.addDataSet('i2', DatasetFactory.createFromObject([0,10,20,30,40,50,60,70,80,90]))
	result.addDataSet('e1', DatasetFactory.createFromObject([0,.1,.2,.3,.4,.5,.3,.2,.1,0]))
# 	result.addDataSet('e2', DatasetFactory.createFromObject([0,.1,.2,.3,.4,.5,.3,.2,.1,0])+100)	
	result.addDataSet('e2', DatasetFactory.createFromObject([100.,100.1,100.2,100.3,100.4,100.5,100.3,100.2,100.1,100.]))	
	return result



class TestSFHInterpolator(unittest.TestCase):
	
	def setUp(self):
		self.sfhi = SFHInterpolator(createSFH())
		
	def test__init__(self):
		self.assertEqual(self.sfhi.length, 10)
		
	def testGetRowAsDict(self):
		self.assertEqual(self.sfhi.getRowAsDict(0), {'i1':0., 'i2':0., 'e1':0., 'e2':100.})
		self.assertEqual(self.sfhi.getRowAsDict(1), {'i1':1., 'i2':10., 'e1':.1, 'e2':100.1})
	
	def testGetRowByValuesAsDict(self):
		g = self.sfhi.getRowByValuesAsDict
		self.assertEqual(g({'i1':0., 'i2':0.}), {'i1':0., 'i2':0., 'e1':0., 'e2':100.})
		self.assertEqual(g({'i1':1., 'i2':10.}), {'i1':1., 'i2':10., 'e1':.1, 'e2':100.1})

	def testGetRowByValuesAsDictWithMissingValue(self):
		g = self.sfhi.getRowByValuesAsDict
		self.assertRaises(KeyError, g, {'i1':0., 'i2':100})


class TestSFHInterpolatorWithHashAccess(TestSFHInterpolator):
	def setUp(self):
		self.sfhi = SFHInterpolatorWithHashAccess(createSFH(), {'i1':1, 'i2':1}, ('i1','i2'))
		

class TestSFHInterpolatorWithHashAccessProperly(unittest.TestCase):

	def setUp(self):
		sfh = ScanFileHolder()
		sfh.addDataSet('i1', DatasetFactory.createFromObject([10,10,10, 20,20,20, 30,30,30]))
		sfh.addDataSet('i2', DatasetFactory.createFromObject([1,2,3, 1,2,3, 1,2,3]))
		sfh.addDataSet('e1', DatasetFactory.createFromObject([0,.1,.2,.3,.4,.5,.6, .7, .8, .9]))
		self.sfhi = SFHInterpolatorWithHashAccess(sfh, {'i1':1, 'i2':1}, ('i1','i2'))

	def test__init__(self):
		self.assertEquals(self.sfhi.lookupDict, {10.0: {3.0: 2, 2.0: 1, 1.0: 0}, 20.0: {3.0: 5, 2.0: 4, 1.0: 3}, 30.0: {3.0: 8, 2.0: 7, 1.0: 6}})

	def testQuantise(self):
		self.assertEquals(quantise(1.0001, 1) ,1.)
		self.assertEquals(quantise(.6, 1) ,1.)
		self.assertEquals(quantise(1.49, 1) ,1.)
		self.assertEquals(quantise(1.5, 1) ,2.)
		self.assertEquals(quantise(1.1234, .1) ,1.1)
		
	def testWithWoblyData(self):
		sfh = ScanFileHolder()
		sfh.addDataSet('i1', DatasetFactory.createFromObject([10.1,10.4,9.6, 20.1,20.2,19.9, 30,30,30]))
		sfh.addDataSet('i2', DatasetFactory.createFromObject([1.09,1.99,3, 1.01,2.099,3, 1,2,3]))
		sfh.addDataSet('e1', DatasetFactory.createFromObject([0,.1,.2,.3,.4,.5,.6, .7, .8, .9]))
		self.sfhi = SFHInterpolatorWithHashAccess(sfh, {'i1':10, 'i2':0.2}, ('i1','i2'))
		self.assertEquals(self.sfhi.lookupDict, {10.0: {3.0: 2, 2.0: 1, 1.0: 0}, 20.0: {3.0: 5, 2.0: 4, 1.0: 3}, 30.0: {3.0: 8, 2.0: 7, 1.0: 6}})


class TestScanFileHolderScannable(unittest.TestCase):
	
	def setUp(self):
		sfh = ScanFileHolder()
		sfh.addDataSet('i1', DatasetFactory.createFromObject([10.1,10.4,9.6, 20.1,20.2,19.9, 30,30,30]))
		sfh.addDataSet('i2', DatasetFactory.createFromObject([1.09,1.99,3, 1.01,2.099,3, 1,2,3]))
		sfh.addDataSet('e1', DatasetFactory.createFromObject([0,.1,.2,.3,.4,.5,.6, .7, .8, .9]))
		self.sfhi = SFHInterpolatorWithHashAccess(sfh, {'i1':10, 'i2':0.2}, ('i1','i2'))

		self.sfhs = ScanFileHolderScannable('sfhs', sfh, ('i1','i2'), ('e1',), {'i1':10, 'i2':0.2})
		
	def test__init__(self):
		pass
	
	def testAsynchronousMoveTo(self):
		self.sfhs.asynchronousMoveTo([4,40])
		self.assertEquals(self.sfhs.pos ,{'i1':4., 'i2':40.})
			
	def testGetPosition(self):
		self.sfhs.asynchronousMoveTo([20,3])
		self.assertEquals(self.sfhs.getPosition(),[19.9, 3.0, 0.5])


class TestSFHSubsetScannable(unittest.TestCase):
	
	def setUp(self):
		self.sfhs = ScanFileHolderScannable('sfhs', createSFH(), ('i1','i2'), ('e1','e2'))
		self.i1 = self.sfhs.scannableFactory('scn', 'i1',[])
		self.i2 = self.sfhs.scannableFactory('scn', ('i2',), [])		
		self.i1i2 = self.sfhs.scannableFactory('scn', ('i1', 'i2'), [])
		self.i1e1 = self.sfhs.scannableFactory('scn', 'i1', 'e1')
		self.e1e2 = self.sfhs.scannableFactory('scn', [], ('e1', 'e2'))
		
	def testInit(self):
		self.assertEqual(list(self.i1.inputNames), ['i1'])
		self.assertEqual(list(self.i1.extraNames), [])	
		self.assertEqual(list(self.i1e1.inputNames), ['i1'])
		self.assertEqual(list(self.i1e1.extraNames), ['e1'])
		
	def testMoving(self):
		self.i1.asynchronousMoveTo(2.)
		self.i2.asynchronousMoveTo(20)
		self.assertEqual(self.i1.getPosition(), 2.)
		
		self.i1i2.asynchronousMoveTo((3., 30))
		self.assertEqual(self.i1i2.getPosition(), [3., 30])
		
		self.i2.asynchronousMoveTo(40)
		self.i1e1.asynchronousMoveTo(4.)
		self.assertEqual(self.i1e1.getPosition(), [4., .4])		
			
				
	
	
	
	
def suite():
	return unittest.TestSuite((
							unittest.TestLoader().loadTestsFromTestCase(TestScanFileHolderScannable),
							unittest.TestLoader().loadTestsFromTestCase(TestSFHInterpolator),
							unittest.TestLoader().loadTestsFromTestCase(TestSFHSubsetScannable),
							unittest.TestLoader().loadTestsFromTestCase(TestSFHInterpolatorWithHashAccess),
							unittest.TestLoader().loadTestsFromTestCase(TestSFHInterpolatorWithHashAccessProperly)))
	
if __name__ == '__main__':
	unittest.TextTestRunner(verbosity=2).run(suite())