import unittest
import oned_test.all_tests
import twod_test.all_tests


def suite():
	suite = unittest.TestSuite()
	suite.addTest(oned_test.all_tests.suite())
	suite.addTest(twod_test.all_tests.suite())	
	return suite

if __name__ == '__main__':
	unittest.TextTestRunner(verbosity=2).run(suite())
