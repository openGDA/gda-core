import testhelpers.gda_test_harness
import sys
import unittest
import test.all_tests

def suite():
	
	suite = unittest.TestSuite()
	suite.addTest(test.all_tests.suite())
	suite = UkAcGdaBimorphTestSuite.wrap(suite)
	return suite

class UkAcGdaBimorphTestSuite(unittest.TestSuite):
	
	@staticmethod
	def wrap(suite):
		new_suite = UkAcGdaBimorphTestSuite()
		new_suite.addTest(suite)
		return new_suite

if __name__ == '__main__':
	print sys.path
	testhelpers.gda_test_harness.GdaTestRunner(suite(), "(script tests)", "TEST-scripts.all_tests.xml")