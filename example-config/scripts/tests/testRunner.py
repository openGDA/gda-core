'''
Created on 12 Apr 2010

@author: tjs15132
'''
import sys
import unittest
import tests.simple
import tests.complex


def suite():
    return unittest.TestSuite((
                            unittest.TestLoader().loadTestsFromTestCase(tests.simple.Test1),
                            unittest.TestLoader().loadTestsFromModule(tests.complex)))
def run_tests():
#    loader = unittest.TestLoader()
#    all_tests=loader.loadTestsFromModule(tests.simple)
    runner = unittest.TextTestRunner(stream=sys.stdout, descriptions=1, verbosity=1)
    runner.run(unittest.TestSuite(suite()))
    print "End of tests"
    