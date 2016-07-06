# TestSinglePeakParameters is implemented using new Dataset - DoubleDataset,not in module TestProcessors which use the deprecated DataSet
import unittest

from gdascripts.analysis.datasetprocessor.oned.extractPeakParameters import ExtractPeakParameters
from org.eclipse.january.dataset import DatasetFactory
from testjy.gdascripts_test.analysis_test.datasetprocessor_test.oned_test.files.files import SILICON_DIFFRACTION
import scisoftpy as dnp
import time

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
		

class TestSinglePeakParameters(Test):
	
	def setUp(self):
		Test.setUp(self)
		self.p = ExtractPeakParameters()
		
	def test__init__(self):
		self.check__init__('peak', ('peakpos','peakvalue','fwhm'), 'peakpos')
		
	def test_process(self):
		peakpos = 18.0
		peakvalue=11.0
		fwhm = 5.0

		result = self.p._process(self.x, self.peak)
		result = [(r[0], r[1], round(r[2], 3)) for r in result]
		expected = [(peakpos,peakvalue,fwhm)]
		self.assert_(result==expected,"%s\n is not close to expected:\n%s"%(`result`,`expected`))


class TestMultiplePeaksParameters(Test):
	ClassIsSetup=False
	def setUp(self):
		if not self.ClassIsSetup:
			self.setupClass()
			self.__class__.ClassIsSetup=True
		self.p=ExtractPeakParameters()
	
	def setupClass(self):
		#starttime=time.time()
		self.__class__.x, self.__class__.y, self.__class__.e=self.loadDatasets(SILICON_DIFFRACTION)
		#print "file load time : ", time.time()-starttime
		
	def loadDatasets(self,path):
		datasets=dnp.io.load(path, format='srs', withmetadata=True)
		return datasets[0],datasets[1],datasets[2]
	
	def test__init__(self):
		self.check__init__('peak', ('peakpos','peakvalue','fwhm'), 'peakpos')
		
	def test_process(self):
		expected=[(15.156, 463725.125, 0.0104), (24.874, 325846.76, 0.0104), (29.255, 193910.516, 0.0107), (35.465, 52433.046, 0.0115), (38.769, 84067.427, 0.0115), (43.806, 102833.843, 0.0124), (46.614, 52911.517, 0.0127), (51.029, 28026.335, 0.0134), (53.549, 44341.915, 0.0140), (57.578, 30681.745, 0.0153), (59.909, 13675.918, 0.0152), (63.681, 6821.767, 0.0160), (65.883, 16287.52, 0.0166), (69.475, 25123.505, 0.0176), (71.59, 16143.899, 0.0182), (80.5, 8575.468, 0.0207), (82.514, 6015.423, 0.0215), (87.851, 5426.258, 0.0230), (101.89, 4819.12, 0.0291)]
		#starttime=time.time()
		#self.p.setDelta(max(self.e))
		#self.p.setSmoothness(25)
		result=self.p._process(self.x, self.y)
		result = [(r[0], r[1], round(r[2], 4)) for r in result]
		#print "analysising time: ", time.time()-starttime
		self.assert_(result==expected,"%s\n is not close to expected:\n%s"%(`result`,`expected`))
		
def suite():
	suite = unittest.TestSuite()
	suite.addTest(unittest.TestLoader().loadTestsFromTestCase(TestSinglePeakParameters))
	suite.addTest(unittest.TestLoader().loadTestsFromTestCase(TestMultiplePeaksParameters))
	return suite 


if __name__ == '__main__':
	unittest.TextTestRunner(verbosity=2).run(suite())
