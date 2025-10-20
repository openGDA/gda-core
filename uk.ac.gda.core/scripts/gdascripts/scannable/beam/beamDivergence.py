'''
Beam Divergence at the sample position.

Created on 19 May 2025

@author: fy65
'''
from gda.device.scannable import ScannableMotionBase


class BeamDivergence(ScannableMotionBase):
    '''
    a scannable provides beam divergence values at a specified position down the X-ray beam.
    '''

    def __init__(self, name, horizontal = 3.3, vertical = 4.9):
        '''
        Constructor
        '''
        self.setName(name)
        self.setInputNames(['horizontal', 'vertical'])
        self.setExtraNames([])
        self.horiz = horizontal
        self.vertical = vertical

    def getPosition(self):
        return [self.horiz, self.vertical]

    def asynchronousMoveTo(self, new_pos):
        if not isinstance(new_pos, list) or len(new_pos) != 2:
            raise ValueError("Input is not a list containing 2 values: [horizontal, vertical]")
        self.horiz = new_pos[0]
        self.vertical = new_pos[1]

    def isBusy(self):
        return False