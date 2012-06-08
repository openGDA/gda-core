from gdascripts.analysis.io.dataLoaders import loadImageIntoSFH
from testjy.gdascripts_test.analysis_test.io_test.images import TESTFILE, IPP_XRAY_EYE_FILE
from gda.analysis.io import ScanFileHolderException, TIFFImageLoader
try:
	from gda.analysis.io import ConvertedTIFFImageLoader
except ImportError:
	print "ConvertedTIFFImageLoader can't be imported from PyDev tests without adding a reference to the swingclient plugin"
	ConvertedTIFFImageLoader = None
import os.path
import unittest

class TestImageLoading(unittest.TestCase):
	
	def testLoadIntoSFH(self):
		sfh = loadImageIntoSFH(TESTFILE)
		self.assert_(sfh is not None)
		
	def testLoadIntoSFHWithExplicitLoader(self):
		sfh = loadImageIntoSFH(TESTFILE, TIFFImageLoader)
		self.assert_(sfh is not None)
	
	def testLoadIntoSFHWithBodgedLoader(self):
		# @Ignore if not running on a Diamond Linux box
		if not os.path.exists("/dls_sw/apps/adsc2tiff/1.0/bin/tiff2tiff"):
			return
		sfh = loadImageIntoSFH(IPP_XRAY_EYE_FILE, ConvertedTIFFImageLoader)
		self.assert_(sfh is not None)
	
	def testLoadIntoSFHWhereTheBodgerLoaderIsUsedAsAFallback(self):
		# @Ignore if not running on a Diamond Linux box
		if not os.path.exists("/dls_sw/apps/adsc2tiff/1.0/bin/tiff2tiff"):
			return
		sfh = loadImageIntoSFH(IPP_XRAY_EYE_FILE)
		self.assert_(sfh is not None)
	
	def testLoadIntoSFHWithBodgedLoaderFails(self):
		# @Ignore if not running on a Diamond Linux box
		if not os.path.exists("/dls_sw/apps/adsc2tiff/1.0/bin/tiff2tiff"):
			return
		if ConvertedTIFFImageLoader is None:
			return 
		self.assertRaises(ScanFileHolderException, loadImageIntoSFH, IPP_XRAY_EYE_FILE+'not here!', ConvertedTIFFImageLoader)

	
def suite():
	suite = unittest.TestSuite()
	suite.addTest(unittest.TestLoader().loadTestsFromTestCase(TestImageLoading))

	return suite 

if __name__ == '__main__':
	unittest.TextTestRunner(verbosity=2).run(suite())
	