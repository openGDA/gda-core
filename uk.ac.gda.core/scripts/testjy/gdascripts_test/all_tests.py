import unittest
import scan_test.all_tests
import bimorph_test.all_tests 
import analysis_test.all_tests
import scannable_test.all_tests
import parameters_test.all_tests

def suite():
	suite = unittest.TestSuite()
	suite.addTest(scan_test.all_tests.suite())
	suite.addTest(bimorph_test.all_tests.suite())
	suite.addTest(analysis_test.all_tests.suite())	
	suite.addTest(scannable_test.all_tests.suite())		
	suite.addTest(parameters_test.all_tests.suite())
	return suite

if __name__ == '__main__':
	unittest.TextTestRunner(verbosity=2).run(suite())
