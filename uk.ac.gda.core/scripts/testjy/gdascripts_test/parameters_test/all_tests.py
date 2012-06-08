import unittest
import beamline_parameters_test

def suite():
	return unittest.TestSuite(beamline_parameters_test.suite())

if __name__ == '__main__':
	unittest.TextTestRunner(verbosity=2).run(suite())
