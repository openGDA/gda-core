from gdascripts.analysis.datasetprocessor.twod.SumMaxPositionAndValue import SumMaxPositionAndValue
from gdascripts.analysis.datasetprocessor.twod.TwodGaussianPeak import TwodGaussianPeak

from gdascripts.analysis.io.dataLoaders import loadImageIntoSFH
from testjy.gdascripts_test.analysis_test.io_test.images import TESTFILE
from gdascripts.analysis.datasetprocessor.twod.TwodDataSetResult import TwodDataSetResult

import unittest


class TestSumMaxPositionAndValue(unittest.TestCase):

	def setUp(self):
		self.ds = loadImageIntoSFH(TESTFILE)[0]
		self.p = SumMaxPositionAndValue()
		
	def test_process(self):
		dsyaxis = dsxaxis = None # STUB
		self.assertEquals(self.p._process(self.ds, dsxaxis, dsyaxis), (664, 223, 254.0, 3733029.0))

	def testProcess(self):
		result=self.p.process(self.ds)
		#expected = TwodDataSetResult('maxval': 254.0, 'sum': 3733029.0, 'maxy': 223, 'maxx': 664};'')
		expected = TwodDataSetResult('max', {'maxval': 254.0, 'sum': 3733029.0, 'maxy': 223, 'maxx': 664}, 'maxx', 'maxy', 'Maximum value found to be at 664.000000,223.000000 (maxx,maxy) was 254.000000 (maxval). Sum was 3733029.000000 (sum)')
		self.assertEqual(result, expected)

def close(l1, l2):
	for v1, v2 in zip(l1, l2):
		# use relative tolerance or 0.01
		tol = max(0.01, 0.01*abs(v2))
#		tol = 0.01
		if abs(v1-v2) > tol:
			return False
	return True

class TestTwodGaussianPeak(unittest.TestCase):

	def setUp(self):
		self.ds = loadImageIntoSFH(TESTFILE)[0]
		self.p = TwodGaussianPeak()
		
	def test_process(self):
		dsyaxis = dsxaxis = 0# None # STUB
		result = self.p._process(self.ds, dsxaxis, dsyaxis)
		expected = [2.2549502730080174E-13,  # background
				 455.429215942832,  # peakx
				 401.3221935111784,  # peaky
				 20457.29,  # topx
				 34128.17,  # topyy
				 188.05936891187554,  # fwhmx
				 110.05648002510901,  # fwhmy
				 16255.505308303551]  # fwhmarea
		self.assert_(close(result, expected),"%s\n is not close to expected:\n%s"%(`result`,`expected`))


	def testProcess(self):
		result=self.p.process(self.ds, 0, 0)
		#expected = TwodDataSetResult('max', {'maxval': 254.0, 'sum': 3733029.0, 'maxy': 223, 'maxx': 664}, 'maxx', 'maxy', 'Total counts were 3733029.000000 and Maximum value at 254.000000,664.000000 (maxx,maxy) was 223.000000 (maxval)')
		#self.assertEqual(result, expected)



def suite():
	suite = unittest.TestSuite()
	suite.addTest(unittest.TestLoader().loadTestsFromTestCase(TestSumMaxPositionAndValue))
	suite.addTest(unittest.TestLoader().loadTestsFromTestCase(TestTwodGaussianPeak))	
	return suite 

if __name__ == '__main__':
	unittest.TextTestRunner(verbosity=2).run(suite())