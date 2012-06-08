from gdascripts.analysis.datasetprocessor.oned.MaxPositionAndValue import MaxPositionAndValue
from gdascripts.analysis.datasetprocessor.oned.MinPositionAndValue import MinPositionAndValue
from gdascripts.analysis.datasetprocessor.oned.CentreOfMass import CentreOfMass
from gdascripts.analysis.datasetprocessor.oned.GaussianPeakAndBackground import GaussianPeakAndBackground
from gdascripts.analysis.datasetprocessor.oned.GaussianEdge import GaussianEdge
from gdascripts.analysis.datasetprocessor.oned.TwoGaussianEdges import TwoGaussianEdges
from gdascripts.analysis.datasetprocessor.oned.scan_stitching import Lcen, Rcen

from testjy.gdascripts_test.analysis_test.datasetprocessor_test.oned_test.files.files import WIRESCANFILE, WIRESCANFILE2, DAT_31473, DAT_31474, DAT_31484
from gda.analysis import DataSet, ScanFileHolder
from gda.analysis.io import SRSLoader


import unittest
import threading

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
	
	def setUp(self, DataSet=DataSet):
		self.x =    DataSet.array([10.,11.,12.,13.,14.,15.,16.,17.,18.,19.])
		self.peak = DataSet.array([0.,1.,2.,3.,4.,5.,4.,3.,2.,1.])
		self.dip = DataSet.array([5.,4.,3.,2.,1.,0.,1.,2.,3.,4.])
		self.p = None
		
	def check__init__(self, name, labelList, keyxlabel, formatString=''):
		self.assertEqual(self.p.name, name)
		self.assertEqual(self.p.labelList, labelList)
		self.assertEqual(self.p.keyxlabel, keyxlabel)
		if formatString:
			self.assertEqual(self.p.formatString, formatString)
		

class TestMin(Test):
	
	def setUp(self):
		Test.setUp(self)
		self.p = MinPositionAndValue()
		
	def test_process(self):
		self.assertEqual(self.p._process(self.x, self.dip), (15., 0.0))
		self.assertEqual(self.p._process(self.x, self.peak), (10., 0.0))
		self.assertEqual(self.p._process(self.x, self.x), (10., 10.))
		
		
class TestMax(Test):
	
	def setUp(self):
		Test.setUp(self)
		self.p = MaxPositionAndValue()
		
	def test_process(self):
		self.assertEqual(self.p._process(self.x, self.peak), (15., 5.0))

class TestLcen(Test):
	
	def setUp(self):
		Test.setUp(self)
		self.p = Lcen()
		
	def test_process(self):
		self.assertEqual(self.p._process(self.x, self.peak), (4.5,))
		
class TestRcen(Test):
	
	def setUp(self):
		Test.setUp(self)
		self.p = Rcen()
		
	def test_process(self):
		self.assertEqual(self.p._process(self.x, self.peak), (24.5,))


class TestCOM(Test):
	
	def setUp(self):
		Test.setUp(self)
		self.p = CentreOfMass()
		
	def test_process(self):
		self.assertEqual(self.p._process(self.x, self.peak), (15.0, 4.0))# <--Not 100% sure of this! RobW
		self.assertEqual(self.p._process(self.x, self.x), (15.068965517241379, 7.92627824019025)) # <--Not 100% sure of this! RobW


class ProcessWithTimeout(threading.Thread):

	def __init__(self, path, processor):
		threading.Thread.__init__(self)
		self.x, self.y = self.loadDataSets(path)
		self.processor = processor

	def loadDataSets(self, path):
		sfh = ScanFileHolder()
		sfh.load(SRSLoader(path))
		return sfh.getDataSet('omega'),	sfh.getDataSet('sum') # x,y

	def run(self):
		self.processor._process(self.x, self.y)
	
	
class TestStandardPrcoessorsForCompletion(Test):
	# some processors never return
		
	def setUp(self):
		self.peak = GaussianPeakAndBackground()
	
	def doGaussianTest(self, path):
		pwt = ProcessWithTimeout(path, GaussianPeakAndBackground())
		pwt.start()
		pwt.join(10)
		self.assertFalse(pwt.isAlive(), "Processing still running after 10 seconds. WARNING: may effect future tests or test machine?")
	
	def test31473(self):
		self.doGaussianTest(DAT_31473)

	def test31484(self):
		self.doGaussianTest(DAT_31484)
		
	def test31474(self):
		self.doGaussianTest(DAT_31474)
		
		
class TestPeak(Test):
	
	def setUp(self):
		Test.setUp(self)
		self.p = GaussianPeakAndBackground()
		
	def test__init__(self):
		self.check__init__('peak', ('pos','offset','top', 'fwhm'), 'pos')
		
	def test_process(self):
		pos = 15.014649472869898
		offset = 0
		top = 4.939879027681013
		fwhm = 5.224729872484415

		result = self.p._process(self.x, self.peak)
		expected = (pos, offset, top, fwhm)
		self.assert_(close(result, expected),"%s\n is not close to expected:\n%s"%(`result`,`expected`))

		result = self.p._process(self.x, self.peak+123)
		expected = (pos, offset+123, top, fwhm)
		self.assert_(close(result, expected),"%s\n is not close to expected:\n%s"%(`result`,`expected`))

		result = self.p._process(self.x, 123-self.peak)
		expected = (pos, offset+123, -top, fwhm)
		self.assert_(close(result, expected),"%s\n is not close to expected:\n%s"%(`result`,`expected`))

class TestEdge(Test):
		
	def setUp(self):
		Test.setUp(self)
		self.p = GaussianEdge()
		
		sfh = ScanFileHolder()
#		sfh.load(SRSLoader(WIRESCANFILE))
#		self.xDataset = sfh.getDataSet('tbdiagY')
#		self.yDataset = sfh.getDataSet('pips2')		
#		
#		self.xDatasetLeft = self.xDataset[0:len(self.xDataset)]
#		self.yDatasetLeft = self.yDataset[0:len(self.yDataset)]		
		sfh.load(SRSLoader(WIRESCANFILE2))
		self.xDataset = sfh.getDataSet('tbdiagY') # Scannable or field name? -- RobW
		self.yDataset = sfh.getDataSet('ch16')
		
	def test__init__(self):
		self.check__init__('edge', ('pos','slope', 'fwhm'), 'pos')
		
	def test_process(self):
		pos,  top, fwhm = self.p._process(self.xDataset, self.yDataset)
		self.assertAlmostEqual(pos, 6.4703041688, 4)
		self.assertAlmostEqual(abs(top), 10050.0, -2)
		self.assertAlmostEqual(fwhm, 0.0066020719, 4)


class TestTwoEdges(Test):
		
	def setUp(self):
		Test.setUp(self)
		self.p = TwoGaussianEdges()
		
		sfh = ScanFileHolder()
#		sfh.load(SRSLoader(WIRESCANFILE))
#		self.xDataset = sfh.getDataSet('tbdiagY')
#		self.yDataset = sfh.getDataSet('pips2')		
#		
#		self.xDatasetLeft = self.xDataset[0:len(self.xDataset)]
#		self.yDatasetLeft = self.yDataset[0:len(self.yDataset)]		
		sfh.load(SRSLoader(WIRESCANFILE)) ## where tbdiagZcoarse is outer loop, tbdiagY is inner loop and pips2 is data
		self.outer = sfh.getDataSet('tbdiagZcoarse')
		self.inner = sfh.getDataSet('tbdiagY')
		self.det   = sfh.getDataSet('pips2')
	
	def getInnerAndDetDatasetForGivenOuterValue(self, outerValue, DataSet=DataSet):
		mush =  [[o, i, d] for o,i,d in
				 zip(self.outer.doubleArray(), self.inner.doubleArray(), self.det.doubleArray())
				 if o==float(outerValue)]
		x = [a[1] for a in mush]
		y = [a[2] for a in mush]
		print x
		print y
		return DataSet.array(x), DataSet.array(y)
	
#	def test__init__(self):
#		self.check__init__('edge', ('pos','slope', 'fwhm'), 'pos')
		
	def test_process_positive_pulse(self):
		upos, ufwhm, dpos, dfwhm = -3.945, 0.006538598547287666, -3.897, 0.007012183526397653
		# outer = 148
		x, y = self.getInnerAndDetDatasetForGivenOuterValue(148.0)
		result = self.p._process(x, y)
		expected = (upos, ufwhm, dpos, dfwhm , 1.94, (ufwhm+ dfwhm)/2)
		self.assert_(closer(result, expected, 0.02),"%s\n is not close to expected:\n%s"%(`result`,`expected`))

	def test_process_negative_pulse(self):
		dpos, dfwhm ,upos, ufwhm, area, fwhm = -3.897, 0.007012183526397653, -3.945, 0.006538598547287666, 1.9477794856163586, 0.0067753910368426595
		# outer = 148, y --> -1
		x, y = self.getInnerAndDetDatasetForGivenOuterValue(148.0)
		y = -1 * y
		print y.doubleArray()
		result = self.p._process(x, y)
		expected = (dpos, dfwhm ,upos, ufwhm, area, fwhm)
		self.assert_(closer(result, expected),"%s\n is not close to expected:\n%s"%(`result`,`expected`))
	
	def test_process_positive_edge(self):
		upos, ufwhm, dpos, dfwhm , area, fwhm =	-3.94, 0.006, -3.896, 0.00634, 1.691, 0.0062
		uarea = 1.68
		# outer = 148, left half (first half of data though!)
		x, y = self.getInnerAndDetDatasetForGivenOuterValue(148.0)
		x=x[len(x)/2:]
		y=y[len(y)/2:]
		result = self.p._process(x, y)
		expected = (upos, ufwhm, 0, 0 , uarea, ufwhm)
		# compare with the same tolerance as specified in the NelderMead constructor (_process in TwoGaussianEdges)
		# This has been Dissabled but should be sorted out in the code reviews to be conducted as part of 
		#self.assert_(closer(result, expected, 0.02),"%s\n is not close to expected:\n%s"%(`result`,`expected`))

	def test_process_negative_edge(self):
		dpos, dfwhm =  -3.897, 0.007012183526397653
		darea = 2.0146003271342665
		
		
		# outer = 148, left half (first half of data though!)
		x, y = self.getInnerAndDetDatasetForGivenOuterValue(148.0)
		x=x[:len(x)/2]
		y=y[:len(y)/2]
		result = self.p._process(x, y)
		expected = (0, 0, dpos, dfwhm, darea, dfwhm)
		# compare with the same tolerance as specified in the NelderMead constructor (_process in TwoGaussianEdges)
		self.assert_(closer(result, expected, 0.02),"%s\n is not close to expected:\n%s"%(`result`,`expected`))

	def testWithFailedRealworldCase1(self):
		#	upos, ufwhm, uarea, dpos, dfwhm, darea =	-3.9446817310490303, 0.00606781983747795, 1.683015428325902, -3.8964078360719188, 0.00634377630045584, 1.6912320011757114 
		# outer = 148
		x, y = self.getInnerAndDetDatasetForGivenOuterValue(158.0)
		result = self.p._process(x, y)
		print 123
		#expected = (upos, ufwhm, dpos, dfwhm , 1.6912320011757114, (ufwhm+ dfwhm)/2)
		#self.assert_(closer(result, expected),"%s\n is not close to expected:\n%s"%(`result`,`expected`))

	def testWithFailedRealworldCase2(self):
		#	upos, ufwhm, uarea, dpos, dfwhm, darea =	-3.9446817310490303, 0.00606781983747795, 1.683015428325902, -3.8964078360719188, 0.00634377630045584, 1.6912320011757114 
		# outer = 148
		x, y = self.getInnerAndDetDatasetForGivenOuterValue(185.0)
		result = self.p._process(x, y)
		print 123
		#expected = (upos, ufwhm, dpos, dfwhm , 1.6912320011757114, (ufwhm+ dfwhm)/2)
		#self.assert_(closer(result, expected),"%s\n is not close to expected:\n%s"%(`result`,`expected`))

	

def suite():
	suite = unittest.TestSuite()
	suite.addTest(unittest.TestLoader().loadTestsFromTestCase(TestMin))
	suite.addTest(unittest.TestLoader().loadTestsFromTestCase(TestMax))
	suite.addTest(unittest.TestLoader().loadTestsFromTestCase(TestCOM))
	suite.addTest(unittest.TestLoader().loadTestsFromTestCase(TestPeak))
	suite.addTest(unittest.TestLoader().loadTestsFromTestCase(TestEdge))
	suite.addTest(unittest.TestLoader().loadTestsFromTestCase(TestTwoEdges))			
	suite.addTest(unittest.TestLoader().loadTestsFromTestCase(TestLcen))			
	suite.addTest(unittest.TestLoader().loadTestsFromTestCase(TestRcen))			
	suite.addTest(unittest.TestLoader().loadTestsFromTestCase(TestStandardPrcoessorsForCompletion))			
	return suite 


if __name__ == '__main__':
	unittest.TextTestRunner(verbosity=2).run(suite())
