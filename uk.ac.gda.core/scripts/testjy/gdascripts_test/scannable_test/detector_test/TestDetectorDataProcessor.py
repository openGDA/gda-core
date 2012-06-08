from gdascripts.analysis.datasetprocessor.twod.TwodDataSetProcessor import TwodDataSetProcessor
from gdascripts.scannable.detector.DetectorDataProcessor import DetectorDataProcessor,\
	DetectorDataProcessorWithRoi

import unittest
import mock


class SimpleTwodDataSetProcessor1(TwodDataSetProcessor):
	
	def __init__(self, name='test', labelList=('a','b','c','d'), keyxlabel='a', keyylabel='b', 
				 formatString='a:%f, b:%f, c:%f, d:%f'):
		TwodDataSetProcessor.__init__(self, name, labelList, keyxlabel, keyylabel, formatString)
	
	def _process(self, ds, dsxaxis, dsyaxis):
		assert(ds=='mock_dataset')
		return 1., 2., 3., 4.


class SimpleTwodDataSetProcessor2(TwodDataSetProcessor):
	
	def __init__(self, name='test', labelList=('A','B','C'), keyxlabel='A', keyylabel='B', 
				 formatString='A:%f, B:%f, C:%f'):
		TwodDataSetProcessor.__init__(self, name, labelList, keyxlabel, keyylabel, formatString)
	
	def _process(self, ds, dsxaxis, dsyaxis):
		return 10., 20., 30.

class MockProcessingDetector(object):
	def getDataset(self):
		raise Exception("MockProcessingDetector")
		

class TestDetectorDataProcessor(unittest.TestCase):

	def setUp(self):
		self.det = MockProcessingDetector()
		self.p1 = SimpleTwodDataSetProcessor1()
		self.p2 = SimpleTwodDataSetProcessor2()
		self.ddp = DetectorDataProcessor('ddp', self.det, [self.p1, self.p2])
	
	def testGetPositionWithDataSetGiven(self):
		self.assertEqual(list(self.ddp.getPosition('mock_dataset')),[1., 2., 3., 4., 10., 20., 30.])
	
	def testGetPosition(self):
		self.assertRaises(Exception, self.ddp.getPosition)	
	
	def testGetInputNames(self):
		self.assertEqual(list(self.ddp.getInputNames()), [])
	
	def testGetExtraNames(self):
		self.assertEqual(list(self.ddp.getExtraNames()),['ddp_a', 'ddp_b', 'ddp_c', 'ddp_d', 'ddp_A', 'ddp_B', 'ddp_C'])
	
	def testGetOutputFormat(self):
		return ['%f'] * 7
	
	def testGetExtraNames_PrefixOff(self):
		ddp = DetectorDataProcessor('ddp', self.det, [self.p1, self.p2], False)
		self.assertEqual(list(ddp.getExtraNames()),['a', 'b', 'c', 'd', 'A', 'B', 'C'])


class TestDetectorDataProcessorWithRoi(unittest.TestCase):

	def setUp(self):
		self.det = mock.Mock()
		self.p1 = SimpleTwodDataSetProcessor1()
		self.p2 = SimpleTwodDataSetProcessor2()
		self.ddproi1 = DetectorDataProcessorWithRoi('ddp', self.det, [self.p1, self.p2])
		self.ddproi2 = DetectorDataProcessorWithRoi('ddp', self.det, [self.p1, self.p2])
	
	def testRoisAreCorrectlySet(self):
		self.ddproi1.setRoi(1, 2, 3, 4)
		self.ddproi2.setRoi(11, 12, 13, 14)
		self.assertEqual(self.ddproi1.getPositionCallable().roi,(1,2,3,4))
		self.assertEqual(self.ddproi2.getPositionCallable().roi,(11,12,13,14))


def suite():
	suite = unittest.TestSuite()
	suite.addTest(unittest.TestLoader().loadTestsFromTestCase(TestDetectorDataProcessor))
	suite.addTest(unittest.TestLoader().loadTestsFromTestCase(TestDetectorDataProcessorWithRoi))
	return suite 

if __name__ == '__main__':
	unittest.TextTestRunner(verbosity=2).run(suite())

