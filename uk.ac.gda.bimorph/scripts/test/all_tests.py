import unittest
import bm_test
from test import bm_optimising_test

def suite():
	loader = unittest.TestLoader()
	return  unittest.TestSuite([
		loader.loadTestsFromModule(bm_test),
		loader.loadTestsFromModule(bm_optimising_test)])

if __name__ == '__main__':
	unittest.TextTestRunner(verbosity=2).run(suite())
