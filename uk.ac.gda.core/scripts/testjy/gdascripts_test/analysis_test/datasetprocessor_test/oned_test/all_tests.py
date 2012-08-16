import unittest

import testXYDataSetProcessor
import TestProcessors
import TestFWHM

def suite():
	suite=  unittest.TestSuite()
	suite.addTest(testXYDataSetProcessor.suite())
	suite.addTest(TestProcessors.suite())
	suite.addTest(TestFWHM.suite())
	return suite

if __name__ == '__main__':
	unittest.TextTestRunner(verbosity=2).run(suite())
