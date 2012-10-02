
'''
Created on 15 Sep 2009

@author: fy65
'''
from gda.configuration.properties import LocalProperties

class LocalProperties(LocalProperties):
    '''
    classdocs
    '''


    def __init__(self):
        '''
        Constructor
        '''
    @staticmethod
    def get(self,*args):
        if (len(args)>=1):
            apply(self.get, (self,)+args)
        else:
            print "No property name provided."
            
            