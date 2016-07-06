# TestFWHM is implemented using new Dataset - DoubleDataset,not in module TestProcessors which use the deprecated DataSet
import unittest

from gdascripts.analysis.datasetprocessor.oned.FullWidthHalfMaximum import FullWidthHalfMaximum
from org.eclipse.january.dataset import DatasetFactory
def close(l1, l2):
	for v1, v2 in zip(l1, l2):
		if abs(v1-v2) > .01:
			return False
	return True

def closer(l1, l2, tolerance=0.01):
	for v1, v2 in zip(l1, l2):
		if abs(v1-v2) > tolerance:
			return False
	return True


class Test(unittest.TestCase):
	
	def setUp(self):
		self.x =    DatasetFactory.createFromObject([10.,11.,12.,13.,14.,15.,16.,17.,18.,19.,20.,21.,22.,23.,24.,25.,26.])
		self.peak = DatasetFactory.createFromObject([1.,1.1,1.5,2.,3.,5.,7.,9.,11.,9.,7.,5.,3.,2.,1.5,1.1,1.])
		#self.dip = DatasetFactory.createFromObject([5.,4.,3.,2.,1.,0.,1.,2.,3.,4.])
		self.p = None
		
	def check__init__(self, name, labelList, keyxlabel, formatString=''):
		self.assertEqual(self.p.name, name)
		self.assertEqual(self.p.labelList, labelList)
		self.assertEqual(self.p.keyxlabel, keyxlabel)
		if formatString:
			self.assertEqual(self.p.formatString, formatString)
		

class TestFWHM(Test):
	
	def setUp(self):
		Test.setUp(self)
		self.p = FullWidthHalfMaximum()
		
	def test__init__(self):
		self.check__init__('peak', ('peakpos','peakvalue','peakbase','fwhm'), 'peakpos')
		
	def test_process(self):
		peakpos = 18.0
		peakvalue=11.0
		peakbase=1.0
		fwhm = 5.0

		result = self.p._process(self.x, self.peak)
		expected = (peakpos,peakvalue,peakbase,fwhm)
		self.assert_(close(result, expected),"%s\n is not close to expected:\n%s"%(`result`,`expected`))


def suite():
	suite = unittest.TestSuite()
	suite.addTest(unittest.TestLoader().loadTestsFromTestCase(TestFWHM))
	return suite 


if __name__ == '__main__':
	unittest.TextTestRunner(verbosity=2).run(suite())
