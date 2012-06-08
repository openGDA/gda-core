import unittest
import TestScanDataProcessor
import TestScanDataProcessorResult

def suite():
	suite = unittest.TestSuite()
	suite.addTest(TestScanDataProcessor.suite())
	suite.addTest(TestScanDataProcessorResult.suite())
	return suite

if __name__ == '__main__':
	unittest.TextTestRunner(verbosity=2).run(suite())
