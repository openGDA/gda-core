import unittest

import detector_test.all_tests
import testSelectableCollectionOfScannables
import TestScanFileHolderScannable
import test_metadata

def suite():
	suite = unittest.TestSuite()
	suite.addTest(testSelectableCollectionOfScannables.suite())
	suite.addTest(TestScanFileHolderScannable.suite())
	suite.addTest(test_metadata.suite())
	suite.addTest(detector_test.all_tests.suite())
	return suite

if __name__ == '__main__':
	unittest.TextTestRunner(verbosity=2).run(suite())
