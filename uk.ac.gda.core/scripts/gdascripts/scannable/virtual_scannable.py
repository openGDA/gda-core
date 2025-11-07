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


    def __init__(self, name, initial_value = 0.0, value_format = '%5.5f', valid_values = None):
        '''
        Constructor
        '''
        self.setName(name)
        self.setInputNames([name])
        self.setOutputFormat([value_format])
        self.value = initial_value
        self.valid_values = valid_values

    def getPosition(self):
        return self.value

    def asynchronousMoveTo(self, new_pos):
        if self.valid_values is None:
            self.value = new_pos
        else:
            if new_pos in self.valid_values:
                self.value = new_pos
            else:
                raise ValueError("input value is not valid. valid values are %r" % self.valid_values)

    def isBusy(self):
        return False

    def toFormattedString(self):
        return "%s : %s" % (self.getName(), self.getPosition())