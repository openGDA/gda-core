#@PydevCodeAnalysisIgnore
# setup CLASSPATH as for gda 
# run using command :
# jython Mar345StatusReaderTest.py
import unittest
import re
from gda.device.detector.mar345 import Mar345StatusReader

class Mar345StatusReaderTest(unittest.TestCase):
	folder = "/dls/i15/software/work/i15_gda_release_8_0_branch/test/gda/device/detector/mar345/Mar345StatusReaderTestFiles/StatusReaderTest/"
	def setUp(self):
		pass

## (real file may not exist if mar not switched on!)
#
#	def testGetDetectorStatusFromRealFile(self):
#		r = Mar345StatusReader("/dls/i15/mar/log/mar.message")
#		status = r.getDetectorStatus();
#		print "Status of real file: " + str(status)
		
	def testGetDetectorStatusTestFileEmpty(self):
		status = Mar345StatusReader(self.folder+"empty").getDetectorStatus();
		print "Status of empty file: " + str(status)
		self.assertEqual(status, 0)
	
	def testGetDetectorStatusTestFileIdle0(self):
		status = Mar345StatusReader(self.folder+"marIdle0.message").getDetectorStatus();
		self.assertEqual(status, 0)
	
	def testGetDetectorStatusTestFileIdle1(self):
		status = Mar345StatusReader(self.folder+"marIdle1.message").getDetectorStatus();
		self.assertEqual(status, 0)
		
	def testGetDetectorStatusTestFilesBusy0(self):
		status = Mar345StatusReader(self.folder+"marBusy0.message").getDetectorStatus();
		self.assertEqual(status, 1)
	
	def testGetDetectorStatusTestFilesBusy1(self):
		status = Mar345StatusReader(self.folder+"marBusy1.message").getDetectorStatus();
		self.assertEqual(status, 1)	
		
	def testGetDetectorStatusTestFilesBusy2(self):
		status = Mar345StatusReader(self.folder+"marBusy2.message").getDetectorStatus();
		self.assertEqual(status, 1)	
		
	def testGetDetectorStatusTestFilesBusy3(self):
		status = Mar345StatusReader(self.folder+"marBusy3.message").getDetectorStatus();
		self.assertEqual(status, 1)	
		
	def testGetDetectorStatusTestFilesBusy4(self):
		status = Mar345StatusReader(self.folder+"marBusy4.message").getDetectorStatus();
		self.assertEqual(status, 1)	

	def tearDown(self):
		pass
		
	
if __name__ == '__main__':
	print "Mar345StatusReaderTest"
	suite = unittest.TestLoader().loadTestsFromTestCase(Mar345StatusReaderTest)
	unittest.TextTestRunner(verbosity=2).run(suite)