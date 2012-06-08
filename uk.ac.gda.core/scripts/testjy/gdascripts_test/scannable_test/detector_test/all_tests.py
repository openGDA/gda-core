import dummy_test.all_tests
### import TestDetectorDataProcessor
import TestProcessingDetectorWrapper
import TestDetectorProcessingIntegration
import TestDatasetShapeRenderer

import unittest

def suite():
	suite = unittest.TestSuite()
	suite.addTest(dummy_test.all_tests.suite())
###	suite.addTest(TestDetectorDataProcessor.suite())
	suite.addTest(TestProcessingDetectorWrapper.suite())
	suite.addTest(TestDetectorProcessingIntegration.suite())
	suite.addTest(TestDatasetShapeRenderer.suite())
	return suite

if __name__ == '__main__':
	unittest.TextTestRunner(verbosity=2).run(suite())