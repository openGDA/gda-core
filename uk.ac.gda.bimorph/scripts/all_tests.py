import testhelpers.gda_test_harness
import sys
import unittest
import test.all_tests

def suite():
	
	suite = unittest.TestSuite()
	suite.addTest(test.all_tests.suite())
	return suite

if __name__ == '__main__':
	print sys.path
	testhelpers.gda_test_harness.GdaTestRunner(suite(), "(script tests)", "TEST-scripts.all_tests.xml")