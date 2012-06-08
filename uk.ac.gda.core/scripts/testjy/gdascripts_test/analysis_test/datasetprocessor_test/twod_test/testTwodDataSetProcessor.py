from gdascripts.analysis.datasetprocessor.twod.TwodDataSetProcessor import TwodDataSetProcessor
from gdascripts.analysis.datasetprocessor.twod.TwodDataSetResult import TwodDataSetResult
import unittest

class SimpleTwodDataSetProcessor(TwodDataSetProcessor):

	def __init__(self, name='simple', labelList=('a','b','c'),keyxlabel='a', keyylabel='b',formatString='a was %f; b was %f; c was %f'):
		TwodDataSetProcessor.__init__(self, name, labelList, keyxlabel, keyylabel, formatString)
	
	def _process(self,dataset, dsxaxis, dsyaxis):
		if dataset != 1: raise Exception # test datasets passed in correctly
		if dsxaxis != 2: raise Exception
		if dsyaxis != 3: raise Exception		
		return ( 3., 4., 5.)
	

class TestTwodDataSetProcessor(unittest.TestCase):

	def setUp(self):
		self.dp =  SimpleTwodDataSetProcessor()

	def test_process(self):
		self.assertEquals(self.dp._process(1, 2, 3), (3.,4., 5.) )
		
	def testProcess(self):
		self.assertEqual(self.dp.process(1, 2, 3),TwodDataSetResult('simple', {'b': 4.0, 'a': 3.0, 'c': 5.0}, 'a', 'b', "a was 3.000000; b was 4.000000; c was 5.000000" ) )
	

def suite():
	return unittest.TestLoader().loadTestsFromTestCase(TestTwodDataSetProcessor)

if __name__ == '__main__':
	unittest.TextTestRunner(verbosity=2).run(suite())

