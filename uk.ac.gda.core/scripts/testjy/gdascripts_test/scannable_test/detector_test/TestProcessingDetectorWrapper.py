from testjy.gdascripts_test.analysis_test.io_test.images import TESTFILE
from gdascripts.scannable.detector.ProcessingDetectorWrapper import ProcessingDetectorWrapper, BasicDataSetProvider
from gdascripts.scannable.detector.DatasetShapeRenderer import LinePainter, RectPainter
from mock import Mock
from gda.analysis import DataSet
from gda.device.scannable import PseudoDevice
from gda.device.detector import PseudoDetector
from gdascripts.analysis.io.dataLoaders import loadImageIntoSFH
import unittest

class MockDetector(PseudoDetector):
	
	def __init__(self, path):
		self.path = path
		self.collection_time = 0
		
	def setCollectionTime(self, t):
		self.collection_time = t
		
	def getCollectionTime(self):
		return self.collection_time	
	
	def collectData(self):
		pass
	
	def getStatus(self):
		return 1000 #i.e. not BUSY
	
	def readout(self):
		return self.path
	
	def createsOwnFiles(self):
		return True


class NonFileCreatingMockDetector(MockDetector):
	
	def createsOwnFiles(self):
		return False

	def readout(self):
		return loadImageIntoSFH(self.path)[0]#, self.iFileLoader)[0]


class MockDetectorDataProcessor(PseudoDevice):
	
	def __init__(self, name, keys, vals):
		self.name = name
		self.keys = keys
		self.vals = vals
	
	def getPosition(self, twodDataSet=None):
		return self.vals
	
	def getInputNames(self):
		return []
	
	def getExtraNames(self):
		return self.keys
	
	def getOutputFormat(self):
		return ['%f'] * len(self.keys)

	
class Test(unittest.TestCase):

	def setUp(self):
		self.det = MockDetector(TESTFILE)
		self.pdw = ProcessingDetectorWrapper('test', self.det, processors=[], panel_name=None, toreplace=None, replacement=None, iFileLoader=None)
	
	def test__init__(self):
		self.assertEquals(list(self.pdw.inputNames), ['t'])
		self.assertEquals(list(self.pdw.getExtraNames()),['path'])
		self.assertEquals(list(self.pdw.outputFormat), ['%f','%s'])
		
	def testAsynchronousMoveTo(self):
		self.pdw.asynchronousMoveTo(1.234)
		self.assertEquals(self.det.collectionTime, 1.234)
	
	def testIsBusy(self):
		self.assertEquals(self.pdw.isBusy(), False)
		
	def testReplacePartOfPath(self):
		self.pdw.toreplace = 'abc'
		self.pdw.replacement = 'ABCD'
		self.assertEquals(self.pdw.replacePartOfPath('abcdefg'),'ABCDdefg')
		
	def testDisplay(self):
		# pass, needs a client
		self.assertRaises(Exception, self.pdw.panel_name)
		
	def testGetDataSet(self):
		self.assert_(self.pdw.datasetProvider is None)
		self.assert_(self.pdw.getDataset() is not None)
		self.assert_(self.pdw.datasetProvider is not None)
		
	def testGetPosition(self):
		self.pdw.display_image = False
		self.pdw.asynchronousMoveTo(1.234)
		self.assertEquals(self.pdw.getPosition(), [1.234, TESTFILE])
	
		
class TestWithProcessors(Test):
	
	def setUp(self):
		self.det = MockDetector(TESTFILE)
		self.p1 = MockDetectorDataProcessor('p1', ['a', 'b'], [1, 2])
		self.p2 = MockDetectorDataProcessor('p2', ['c', 'd'], [3, 4])
		self.pdw = ProcessingDetectorWrapper('test', self.det, [self.p1, self.p2])

	def test__init__(self):
		self.assertEquals(list(self.pdw.inputNames), ['t'])
		self.assertEquals(list(self.pdw.getExtraNames()),['path','a','b','c','d'])#
		self.assertEquals(list(self.pdw.getOutputFormat()), ['%f','%s'] + ['%f']*4)

	def testGetPosition(self):
		self.pdw.display_image = False
		self.pdw.asynchronousMoveTo(1.234)
		self.assertEquals(self.pdw.getPosition(), [1.234, TESTFILE, 1, 2, 3, 4])


class TestWithProcessorsWithNonFileCreatingDetector(TestWithProcessors):
	
	def setUp(self):
		self.det = NonFileCreatingMockDetector(TESTFILE)
		self.p1 = MockDetectorDataProcessor('p1', ['a', 'b'], [1, 2])
		self.p2 = MockDetectorDataProcessor('p2', ['c', 'd'], [3, 4])
		self.pdw = ProcessingDetectorWrapper('test', self.det, [self.p1, self.p2])
	
	def testGetPosition(self): # COPIED 
		self.pdw.display_image = False
		self.pdw.asynchronousMoveTo(1.234)
		result = self.pdw.getPosition()
		result[1] = None# returns the time so don't test!
		self.assertEquals(result, [1.234, None, 1, 2, 3, 4])


class TestWithProcessorsAndDisplayDisabled(Test):
	
	def setUp(self):
		self.det = MockDetector(TESTFILE)
		self.p1 = MockDetectorDataProcessor('p1', ['a', 'b'], [1, 2])
		self.p2 = MockDetectorDataProcessor('p2', ['c', 'd'], [3, 4])
		self.pdw = ProcessingDetectorWrapper('test', self.det, [self.p1, self.p2])
		self.pdw.process_image = False
		self.pdw.display_image = False

	def test__init__(self):
		self.assertEquals(list(self.pdw.inputNames), ['t'])
		self.assertEquals(list(self.pdw.getExtraNames()),['path'])#
		self.assertEquals(list(self.pdw.getOutputFormat()), ['%f','%s'])

	def testGetPosition(self):
		self.pdw.display_image = False
		self.pdw.asynchronousMoveTo(1.234)
		self.assertEquals(self.pdw.getPosition(), [1.234, TESTFILE])


class TestWithPainters(Test):
	
	def setUp(self):
		Test.setUp(self)
		self.p1 = MockDetectorDataProcessor('p1', ['a'], [1.])
		self.p2 = MockDetectorDataProcessor('p2', ['a'], [1.])
		self.line = LinePainter(0,0 ,0,1)
		self.rect = RectPainter(1,1, 3,4)
	
	def testAddShape(self):
		self.pdw.display_image = False
		self.pdw.addShape(self.p1, 'p1line', self.line)
		self.assertEqual(self.pdw.renderer.shapesToPaint, {self.p1:{'p1line':self.line}})
		self.pdw.addShape(self.p1, 'p1line', self.line)
		self.assertEqual(self.pdw.renderer.shapesToPaint, {self.p1:{'p1line':self.line}})		
		self.pdw.addShape(self.p1, 'p1rect', self.rect)
		self.assertEqual(self.pdw.renderer.shapesToPaint, {self.p1:{'p1line':self.line, 'p1rect':self.rect}})
		self.pdw.addShape(self.p2, 'p2line', self.line)
		self.assertEqual(self.pdw.renderer.shapesToPaint, {self.p1:{'p1line':self.line, 'p1rect':self.rect}, self.p2:{'p2line':self.line}})
	
	def testRemoveShape(self):
		self.testAddShape()
		self.pdw.removeShape(self.p1, 'p1line')
		self.assertEqual(self.pdw.renderer.shapesToPaint, {self.p1:{'p1rect':self.rect}, self.p2:{'p2line':self.line}})
		self.pdw.removeShape(self.p1, 'p1rect')
		self.pdw.removeShape(self.p2, 'p2line')
		self.assertEqual(self.pdw.renderer.shapesToPaint, {})

def ds2lofl(ds):
	return [list(l) for l in list(ds.doubleMatrix())]


def suite():
	suite = unittest.TestSuite()
	suite.addTest(unittest.TestLoader().loadTestsFromTestCase(Test))
	suite.addTest(unittest.TestLoader().loadTestsFromTestCase(TestWithProcessors))	
	suite.addTest(unittest.TestLoader().loadTestsFromTestCase(TestWithPainters))
	suite.addTest(unittest.TestLoader().loadTestsFromTestCase(TestWithProcessorsAndDisplayDisabled))
	suite.addTest(unittest.TestLoader().loadTestsFromTestCase(TestWithProcessorsWithNonFileCreatingDetector))
	return suite 

if __name__ == '__main__':
	unittest.TextTestRunner(verbosity=2).run(suite())
	
	