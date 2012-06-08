from gdascripts.analysis.datasetprocessor.oned.XYDataSetProcessor import XYDataSetProcessor
from gdascripts.analysis.datasetprocessor.oned.XYDataSetResult import XYDataSetResult
import java.lang.Float #@UnresolvedImport
import unittest

class SimpleXYDataSetProcessor(XYDataSetProcessor):

	def __init__(self, name='simple', labelList=('a','b'),keyxlabel='a',formatString='a was %f; b was %f'):
		XYDataSetProcessor.__init__(self, name, labelList, keyxlabel, formatString)
	
	def _process(self,xDataSet, yDataSet):
		if xDataSet != 1: raise Exception # test datasets passed in correctly
		if yDataSet != 2: raise Exception
		return ( 3., 4.)

	
class SimpleXYDataSetProcessorWithError(SimpleXYDataSetProcessor):
	
	def _process(self,xDataSet, yDataSet):
		1/0

class SimpleXYDataSetProcessorWithJavaError(SimpleXYDataSetProcessor):
	
	def _process(self,xDataSet, yDataSet):
		java.lang.Float('not a number')


class TestXYDataSetProcessor(unittest.TestCase):

	def setUp(self):
		self.dp =  SimpleXYDataSetProcessor()

	def test_process(self):
		self.assertEquals(self.dp._process(1,2), (3.,4.) )
		
	def testProcess(self):
		self.assertEqual(self.dp.process(1, 2),XYDataSetResult('simple', {'b': 4.0, 'a': 3.0}, ('a','b'), 'a', "a was 3.000000; b was 4.000000" ) )
	
		

class TestXYDataSetProcessorWithError(unittest.TestCase):

	def setUp(self):
		self.dp =  SimpleXYDataSetProcessorWithError()

	def test_process(self):
		self.assertRaises(ZeroDivisionError, self.dp._process, 1,2)
		
	def testProcess(self):
		result = self.dp.process(1, 2)
		self.assertEquals(result.resultsDict, {'b': None, 'a': None})

	
class TestXYDataSetProcessorWithJavaError(unittest.TestCase):

	def setUp(self):
		self.dp =  SimpleXYDataSetProcessorWithJavaError()

	def test_process(self):
		self.assertRaises(java.lang.Exception, self.dp._process, 1,2)
		
	def testProcess(self):
		result = self.dp.process(1, 2)
		self.assertEquals(result.resultsDict, {'b': None, 'a': None})

def suite():
	suite = unittest.TestSuite()
	suite.addTest(unittest.TestLoader().loadTestsFromTestCase(TestXYDataSetProcessor))
	suite.addTest(unittest.TestLoader().loadTestsFromTestCase(TestXYDataSetProcessorWithError))	
	suite.addTest(unittest.TestLoader().loadTestsFromTestCase(TestXYDataSetProcessorWithJavaError))	
	return suite 

if __name__ == '__main__':
	unittest.TextTestRunner(verbosity=2).run(suite())

