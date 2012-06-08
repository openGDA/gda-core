import unittest
import gdascripts_test.all_tests 

def suite():
	suite = unittest.TestSuite()
	suite.addTest(gdascripts_test.all_tests.suite())
	return suite

if __name__ == '__main__':
	unittest.TextTestRunner(verbosity=2).run(suite())
