import unittest
import testEpics_pds

def suite():
	return unittest.TestSuite((
							testEpics_pds.suite(),
								))
	return suite

if __name__ == '__main__':
	unittest.TextTestRunner(verbosity=2).run(suite())
