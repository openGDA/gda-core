'''
2 Scannable classes - one for XAS mode,  the other for mapping XAS mode to nexus link path.

Created on 5 Dec 2024

@author: fy65
'''
from gda.device.scannable import ScannableMotionBase


class XASMode(ScannableMotionBase):
    '''
    Scannable that allows to set XAS measurement mode,
    i.e. the default measurement PV or channel for the absorbed beam defined in NXxas.nxdl.xml 
    '''

    def __init__(self, name, xas_modes, mode = 'TEY'):
        '''
        Constructor
        '''
        self.setName(name)
        self.setInputNames([name])
        self.mode = mode
        self.xas_modes = xas_modes

    def getPosition(self):
        return self.mode

    def asynchronousMoveTo(self, m):
        if m not in self.xas_modes:
            raise ValueError("%s is not a supported measurement mode. Supported mode must be one of %r." % (m, self.xas_modes))
        self.mode = m

    def isBusy(self):
        return False


class XASModePathMapper(ScannableMotionBase):
    ''' Scannable that return linked path for a given XAS mode.
    '''
    def __init__(self, name, mode, dic):
        self.setName(name)
        self.setInputNames([name])
        self.dict = dic
        self.mode = mode

    def getPosition(self):
        return self.dict[self.mode.getPosition()]

    def asynchronousMoveTo(self, pos):
        raise RuntimeError(self.getName() + " is a READ-ONLY scannable object")

    def isBusy(self):
        return False



