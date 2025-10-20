'''
Define Beam extent or size.

Created on 19 May 2025

@author: fy65
'''
from gda.device.scannable import ScannableMotionBase

class BeamExtent(ScannableMotionBase):
    '''
    scannable defines the beam extend or size.
    '''

    def __init__(self, name, horizontal_size=6.0, vertical_size=10.0):
        '''
        Constructor
        '''
        self.setName(name)
        self.setInputNames(['horizontal', 'vertical'])
        self.setExtraNames([''])
        self.h_size = horizontal_size
        self.v_size = vertical_size

    def getPosition(self):
        return [self.h_size, self.v_size]

    def asynchronousMoveTo(self, new_pos):
        if not isinstance(new_pos, list) or len(new_pos) != 2:
            raise ValueError("Input is not a list containing 2 values: [horizontal_size, vertical_size]")
        self.h_size = new_pos[0]
        self.v_size = new_pos[1]

    def isBusy(self):
        return False