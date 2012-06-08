import unittest

import TestImageReadingDummyDetector
import TestFocusedBeamDataset

def suite():
	suite = unittest.TestSuite()
	suite.addTest(TestImageReadingDummyDetector.suite())
	suite.addTest(TestFocusedBeamDataset.suite())
	return suite

if __name__ == '__main__':
	unittest.TextTestRunner(verbosity=2).run(suite())
