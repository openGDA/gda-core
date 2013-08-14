import unittest
import bm_test

def suite():
	return  unittest.TestSuite(bm_test.suite())

if __name__ == '__main__':
	unittest.TextTestRunner(verbosity=2).run(suite())
