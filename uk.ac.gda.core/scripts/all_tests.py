import testhelpers.gda_test_harness
import sys
import unittest
import testjy.all_tests

def suite():
	
	suite = unittest.TestSuite()
	suite.addTest(testjy.all_tests.suite())
	suite = UkAcGdaCoreTestSuite.wrap(suite)
	return suite

class UkAcGdaCoreTestSuite(unittest.TestSuite):
	
	@staticmethod
	def wrap(suite):
		new_suite = UkAcGdaCoreTestSuite()
		new_suite.addTest(suite)
		return new_suite

if __name__ == '__main__':
	print sys.path
	testhelpers.gda_test_harness.GdaTestRunner(suite(), "(script tests)", "TEST-scripts.all_tests.xml")

