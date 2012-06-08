import unittest
import TestDataLoaders
import TestDatasetProvider


def suite():
	suite = unittest.TestSuite()
	suite.addTest(TestDataLoaders.suite())
	suite.addTest(TestDatasetProvider.suite())
	return suite

if __name__ == '__main__':
	unittest.TextTestRunner(verbosity=2).run(suite())
