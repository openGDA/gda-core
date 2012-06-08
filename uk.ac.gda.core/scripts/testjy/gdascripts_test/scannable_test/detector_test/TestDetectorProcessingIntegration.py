from testjy.gdascripts_test.analysis_test.io_test.images import TESTFILE
from testjy.gdascripts_test.scannable_test.detector_test.TestProcessingDetectorWrapper import MockDetector

from gdascripts.scannable.detector.ProcessingDetectorWrapper import ProcessingDetectorWrapper
from gdascripts.scannable.detector.DetectorDataProcessor import DetectorDataProcessorWithRoi
from gdascripts.analysis.datasetprocessor.twod.TwodGaussianPeak import TwodGaussianPeak
import unittest
from gdascripts.scannable.detector.DatasetShapeRenderer import RectPainter


def close(l1, l2):
	for v1, v2 in zip(l1, l2):
		if abs(v1-v2) > .001:
			return False
		return True

class TestDetectorProcessingIntegration(unittest.TestCase):

	def setUp(self):
		self.det = MockDetector(TESTFILE)
		self.peak = DetectorDataProcessorWithRoi('peak', None, [TwodGaussianPeak()])
		self.pdw = ProcessingDetectorWrapper('test', self.det, [self.peak])
		self.peak.det = self.pdw
		self.pdw.display_image = False
		
	def test__init__(self):
		self.assertEquals(list(self.pdw.inputNames), ['t'])
		self.assertEquals(list(self.pdw.getExtraNames()),['path', 'peak_background', 'peak_peakx', 'peak_peaky', 'peak_topx', 'peak_topyy', 'peak_fwhmx', 'peak_fwhmy', 'peak_fwhmarea'])#
		self.assertEquals(list(self.pdw.getOutputFormat()), ['%f','%s'] + ['%f']*8)

	def testGetPosition(self):
		self.pdw.asynchronousMoveTo(9.876)
		result = self.pdw.getPosition()
		expected = [
				 9.876, # time
				 TESTFILE, # file
				 3.287347913927987E-13,
				 455.4292155496275,
				 401.32219254549636,
				 236.83346729561012,
				 230.9785313517741,
				 188.05937419836897,
				 110.0564812351171,
				 16255.505943978298]


		self.assert_(close(result, expected))

	def testSettingRoi(self):
		self.peak.setRoi(1,1, 3,4)
		shape = self.pdw.renderer.shapesToPaint[self.peak]['roi']
		self.assertEqual(type(shape), RectPainter)

	def testUnSettingRoi(self):
		self.testSettingRoi()
		self.peak.setRoi(None)
		self.assertEqual(self.pdw.renderer.shapesToPaint, {})

def suite():
	suite = unittest.TestSuite()
	suite.addTest(unittest.TestLoader().loadTestsFromTestCase(TestDetectorProcessingIntegration))
	return suite 

if __name__ == '__main__':
	unittest.TextTestRunner(verbosity=2).run(suite())
	
	