import unittest
import testConcurrentScanWrapper
import testGdascans
import testSpecscans
import testSecondaryConcurrentScan
import process_test.all_tests

def suite():
	return unittest.TestSuite((
							testConcurrentScanWrapper.suite(),
							testGdascans.suite(),
							testSpecscans.suite(),
							testSecondaryConcurrentScan.suite(),
							process_test.all_tests.suite()
							))
	return suite

if __name__ == '__main__':
	unittest.TextTestRunner(verbosity=2).run(suite())
