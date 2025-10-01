'''
Created on 9 Sept 2025

@author: fy65
'''
from gda.device.scannable import ScannableMotionBase

class VirtualScannable(ScannableMotionBase):
    '''
    a scannable stores the value it is set to and return this value from getPosition().
    This is different from DummyScannable.java which is coded differently and tied to Double value.
    '''


    def __init__(self, name, initial_value = 0.0, value_format = '%5.5f'):
        '''
        Constructor
        '''
        self.setName(name)
        self.setInputNames([name])
        self.setOutputFormat([value_format])
        self.value = initial_value

    def getPosition(self):
        return self.value

    def asynchronousMoveTo(self, new_pos):
        self.value = new_pos

    def isBusy(self):
        return False
