'''
Usage:
    1. run this script from jython editor, you will have an object called 'pathscan'
    2. start your scan using
    >>>pathscan startpoint stoppoint "/path/to/your/input/scan_path_data_file" detectorToUse exposureTime
    
    Your scan path data file must be in the format of tab or space delimited columns. 
    It should contains just scannable poisitions that define the path the scan to follow.

#SScanSS project file format:
First line:    1
Last line:     10
ScannableNames    ss2.x    ss2.z    ss2.y    ss2.rx    ss2.theta
ScannableUnits    mm    mm    mm    deg    deg    s
1    3.25714    74.5914    0    0    2.50032    1.1
2    1.62857    37.2957    -0.00011139    0    2.50032
3    0    0    0    0    2.50032
4    -1.62857    -37.2957    0    0    2.50032
5    -3.25714    -74.5914    0    0    2.50032
6    -39.5623    1.72753    0.0199849    0    2.50032
7    -19.891    0.868553    0.0199584    0    2.50032
8    -0.219791    0.0095771    0.0201187    0    2.50032
9    19.4515    -0.849398    0.0197406    0    2.50032
10    39.1227    -1.70837    0.0200012    0    2.50032

Created on 7 Oct 2011

@author: fy65
'''
# load in scan path data
import string
from gda.device.scannable.scannablegroup import ScannableGroup
from gda.jython.commands.ScannableCommands import scan
from gda.jython.commands.GeneralCommands import alias
from init_scan_commands_and_processing import scanp

class PathScan():
    def __init__(self, name, detectorToUse=edxd):#@UndefinedVariable
        self.name=name
        self.sg = ScannableGroup()
        self.pointid=[]
        self.path=[]
        self.startline=1
        self.lastline=10
        self.scannablelist=[]
        self.scannableunitlist=[]
        self.detector=detectorToUse
        self.detectorunit="s"
        self.exposuretime=[]
        
    def read_scan_path(self,filename):
        f = open(filename, "r")
        lines = f.readlines()
        f.close()
        lines = map(string.split, map(string.strip, lines))
        self.pointid=[]
        self.path=[]
        self.scannablelist=[]
        self.scannableunitlist=[]
        self.exposuretime=[]
        # parsing the input data
        for line in lines:
            print line
            if line[0].startswith("#"):     #ignore comment
                continue
            elif line[0].startswith("First"):
                self.startline=line[-1]
            elif line[0].startswith("Last"):
                self.lastline=line[-1]
            elif line[0].startswith("ScannableNames"):
                self.scannablelist=[globals()[x] for x in line[1:]] # get all motors
                self.sg.setName("pathscangroup")
                self.sg.setGroupMembers(self.scannablelist)
                self.sg.configure()
            elif line[0].startswith("ScannableUnits"):
                self.scannableunitlist=[x for x in line[1:]]
            else: #real data go here
                if int(line[0])>= self.startline or int(line[0]) <= self.lastline:
                    self.pointid.append(int(line[0]))
                    self.path.append([float(x) for x in line[1:]])
                    
    def setStartPoint(self, start):
        self.startline=start
    
    def getStartPoint(self):
        return self.startline
    
    def setStopPoint(self, stop):
        self.lastline=stop
        
    def getStopPoint(self):
        return self.lastline
    
    def setDetector(self, det):
        self.detector=det
        
    def getDetector(self):
        return self.detector
    
    def getPath(self):
        return self.path
    
    def setPath(self, path):
        self.path=path
        
    def getPointIDs(self):
        return self.pointid
    
    def setPointIDs(self, points):
        self.pointid=points

    def start(self,filename, exposureTime):
        ''' kept for backward compatibility'''
        self.read_scan_path(filename)
        print self.pointid
        print self.path
        print self.exposuretime
        pathPositions=tuple(self.path)
        print pathPositions
        scan self.sg pathPositions self.detector exposureTime
    
    def startScan(self,filename, exposureTime):
        print self.pointid
        print self.exposuretime
        pathPositions=tuple(self.path)
        print pathPositions
        scan self.sg pathPositions self.detector exposureTime
            
    def setName(self, name):
        self.name=name
        
    def getName(self):
        return self.name

scanPath=PathScan("pathscan", detectorToUse=edxd) #@UndefinedVariable
    
def pathscan(startpoint, stoppoint, pathfile, detector, exposureTime):
    ''' Scan following a defined path in a specified file from start point to end point, collect data at each point for the specified time.'''
    scanPath.read_scan_path(pathfile)
    if startpoint<scanPath.getStartPoint() or startpoint > scanPath.getStopPoint():
        raise ValueError("start point ID is out of the range specified in the path file: " + pathfile)
    scanPath.setStartPoint(startpoint)
    if stoppoint>scanPath.getStopPoint() or startpoint < scanPath.getStartPoint():
        raise ValueError("Stop point ID is out of the range specified in the path file: " + pathfile)
    scanPath.setStopPoint(stoppoint)
    scanPath.setDetector(detector)
    pathSelected=scanPath.getPath()[startpoint:stoppoint+1]
    scanPath.setPath(pathSelected)
    pointsSelected=scanPath.getPointIDs()[startpoint:stoppoint+1]
    scanPath.setPointIDs(pointsSelected)
    scanPath.startScan(pathfile, exposureTime)
    
alias("pathscan")
