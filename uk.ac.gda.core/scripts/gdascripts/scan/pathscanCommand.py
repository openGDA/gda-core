'''
Usage:
    1. run this script from jython editor, you will have an object called 'pathscan'
    2. start your scan using
    >>>pathscan (x,y.z) ([1,1,1],[2,2,2],[3,4,5]) edxd 5.0
    
Created on 24 July 2012

@author: fy65
'''
from gdascripts.scannable.detector.dummy.dummy_detector import DummyDetector
try:
    if dummydetector == None: #@UndefinedVariable
        dummydetector=DummyDetector("dummydetector")
except:
    dummydetector=DummyDetector("dummydetector")
        
from gda.device.scannable.scannablegroup import ScannableGroup
from gda.jython.commands.ScannableCommands import scan
from gda.jython.commands.GeneralCommands import alias
def pathscan(scannables, path, detector, exposure=None, *args): #@UndefinedVariable
    ''' Scan a group of scannables following the specified path and collect data at each point from specified detector and time'''
    sg=ScannableGroup()
    for each in scannables:
        sg.addGroupMember(each)
    sg.setName("pathgroup")
    if exposure is None:
        scan([sg, path, detector]+list(args))
    else:
        scan([sg, path, detector, exposure]+list(args))

alias("pathscan")