from gdascripts.scannable.detector.dummy.ImageReadingDummyDetector import ImageReadingDummyDetector
from gdascripts.scannable.detector.dummy.focused_beam_dataset import CreateImageReadingDummyDetector
from gdascripts.scannable.dummy import SingleInputDummy
import unittest

class TestFocusedBeamDataset(unittest.TestCase):

	def setUp(self):
		self.x = SingleInputDummy('x')
		self.det = CreateImageReadingDummyDetector.create(self.x)
		
	def testReadout(self):
		self.x.asynchronousMoveTo(490.)
		print self.det.readout()
	
	
def suite():
	suite = unittest.TestSuite()
	suite.addTest(unittest.TestLoader().loadTestsFromTestCase(TestFocusedBeamDataset))
	return suite 

if __name__ == '__main__':
	unittest.TextTestRunner(verbosity=2).run(suite())