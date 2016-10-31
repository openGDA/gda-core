import unittest
import gdascripts_test.all_tests
import gda_completer_test

def suite():
	suite = unittest.TestSuite()
	suite.addTest(gdascripts_test.all_tests.suite())
	suite.addTest(gda_completer_test.suite())
	return suite

if __name__ == '__main__':
	unittest.TextTestRunner(verbosity=2).run(suite())
