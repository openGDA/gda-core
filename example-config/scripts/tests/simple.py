'''
Created on 12 Apr 2010

@author: tjs15132
'''
import unittest


class Test1(unittest.TestCase):
    def setUp(self):
        pass

    def tearDown(self):
        pass

    def testComment(self):
        
        pass #this is a comment

class Test2(unittest.TestCase):
    def setUp(self):
        pass

    def tearDown(self):
        pass

    def testName(self):
        pass


def suite():
    return unittest.TestSuite((
                            unittest.TestLoader().loadTestsFromTestCase(Test1),
                            unittest.TestLoader().loadTestsFromTestCase(Test2)))


if __name__ == "__main__":
    #import sys;sys.argv = ['', 'Test.testName']
    unittest.main()