import unittest
import datasetprocessor_test.all_tests
import io_test.all_tests

def suite():
	suite = unittest.TestSuite()
	suite.addTest(datasetprocessor_test.all_tests.suite())
	suite.addTest(io_test.all_tests.suite())
	return suite

if __name__ == '__main__':
	unittest.TextTestRunner(verbosity=2).run(suite())
