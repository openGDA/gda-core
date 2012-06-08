from gdascripts.scannable.detector.dummy.ImageReadingDummyDetector import ImageReadingDummyDetector

from gdascripts.scannable.dummy import SingleInputDummy
import unittest

class TestImageReadingDummyDetector(unittest.TestCase):

	def setUp(self):
		self.x = SingleInputDummy('x')
		self.d = {1.:'image1.tif', 2.: 'image2.tif'}
		self.det = ImageReadingDummyDetector('det', self.x, '/folder',self.d, 'info')
		
	def testCollectionTime(self):
		self.det.setCollectionTime(12.3)
		self.assertEqual(self.det.getCollectionTime(), 12.3)
		
	def testReadout(self):
		self.x.asynchronousMoveTo(1.)
		self.assertEquals(self.det.readout(), '/folder/image1.tif')
		self.x.asynchronousMoveTo(2)
		self.assertEquals(self.det.readout(), '/folder/image2.tif')
	
	def testReadoutWithBadPosition(self):
		self.x.asynchronousMoveTo(3.)
		self.assertRaises(KeyError, self.det.readout)		
	
	
def suite():
	suite = unittest.TestSuite()
	suite.addTest(unittest.TestLoader().loadTestsFromTestCase(TestImageReadingDummyDetector))
	return suite 

if __name__ == '__main__':
	unittest.TextTestRunner(verbosity=2).run(suite())