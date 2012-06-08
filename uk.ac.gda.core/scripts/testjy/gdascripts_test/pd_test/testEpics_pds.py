'''
Created on 13 Apr 2010

@author: fy65
'''
from gdascripts.pd.epics_pds import DisplayEpicsPVClass,SingleEpicsPositionerClass
import unittest

class TestEpics_pds(unittest.TestCase):
    
    def setUp(self):
        self.yplus=DisplayEpicsPVClass('S2YPlus', 'BL11I-AL-SLITS-02:Y:PLUS', 'mm', '%.1e')
        self.yminus=DisplayEpicsPVClass('S2YMinus', 'BL11I-AL-SLITS-02:Y:MINUS', 'mm', '%.1e')

    def test_rawGetPosition(self):
        yplus = self.yplus.rawGetPosition()
        self.assertTrue(yplus==1.0)
        yminus = self.yminus.rawGetPosition()
        self.assertTrue(yminus==1.0)
        
        
    def test_getLoop(self):
        for i in range(1000):
            yplus = self.yplus.rawGetPosition()
            self.assertTrue(yplus==1.0)
    
    def test_getLoop1(self):
        for i in range(1000):
            yminus = self.yminus.rawGetPosition()
            self.assertTrue(yminus==1.0)

def suite():
    unittest.TestLoader().loadTestsFromTestCase(TestEpics_pds)
           
if __name__ == '__main__':
    unittest.TextTestRunner(verbosity=2).run(suite)

