from gdascripts.analysis.io.dataLoaders import loadImageIntoSFH
from testjy.gdascripts_test.analysis_test.io_test.images import TESTFILE, IPP_XRAY_EYE_FILE
from org.eclipse.dawnsci.analysis.api.io import ScanFileHolderException
from gda.analysis.io import TIFFImageLoader
import os.path
import unittest

class TestImageLoading(unittest.TestCase):
	
	def testLoadIntoSFH(self):
		sfh = loadImageIntoSFH(TESTFILE)
		self.assert_(sfh is not None)
		
	def testLoadIntoSFHWithExplicitLoader(self):
		sfh = loadImageIntoSFH(TESTFILE, TIFFImageLoader)
		self.assert_(sfh is not None)
	
	def testLoadOddImageIntoSFHWithStandardTIFFLoader(self):
		sfh = loadImageIntoSFH(IPP_XRAY_EYE_FILE, TIFFImageLoader)
		self.assert_(sfh is not None)
	
	def testLoadOddImageIntoSFH(self):
		sfh = loadImageIntoSFH(IPP_XRAY_EYE_FILE)
		self.assert_(sfh is not None)
	
	
def suite():
	suite = unittest.TestSuite()
	suite.addTest(unittest.TestLoader().loadTestsFromTestCase(TestImageLoading))

	return suite 

if __name__ == '__main__':
	unittest.TextTestRunner(verbosity=2).run(suite())
	