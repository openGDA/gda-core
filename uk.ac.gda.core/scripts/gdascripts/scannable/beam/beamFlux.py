'''
Beam flux at sample position for current.

Created on 19 May 2025

@author: fy65
'''
from gda.device.scannable import ScannableMotionBase

class BeamFlux(ScannableMotionBase):
    '''
    a scannable provides beam flux values at a specified position down the X-ray beam.
    '''

    def __init__(self, name, flux = 0.0):
        '''
        scannable that provides beam flux value at a specific position down the beam line.
        '''
        self.setName(name)
        self.setInputNames([''])
        self.setExtraNames(['flux'])
        self.flux = flux

    def getPosition(self):
        return self.flux

    def asynchronousMoveTo(self, new_pos):
        self.flux = float(new_pos)

    def isBusy(self):
        return False