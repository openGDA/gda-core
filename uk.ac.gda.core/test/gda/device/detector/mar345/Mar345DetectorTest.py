#@PydevCodeAnalysisIgnore
# setup CLASSPATH as for gda 
# run using command :
from java.lang import Thread, Runnable
import unittest
import re
from gda.device.detector.mar345 import Mar345Detector
import shutil
import time
import os
import glob

path = "/dls/i15/software/work/i15_gda_release_8_0_branch/test/gda/device/detector/mar345/Mar345StatusReaderTestFiles/"
testFolder = path + "StatusReaderTest/"
sourceFolder = path + "DetectorTest/"
targetFolder = path + "mar/"

stopThread = False
    
class Mar345DetectorTest(unittest.TestCase):
    def setUp(self):
        pass

    def testErase(self):
        scripts = MarScripts()
        mar = Mar345Detector(targetFolder)
        
        scripts.marErase(mar, 1)
        
#    def testScan(self):            # same structure as marErase()
#        scripts = MarScripts()
#        mar = Mar345Detector(targetFolder)
#        
#        scripts.marScan(mar, ...)
        
    def tearDown(self):
            pass

class MarScripts:
    def marErase(self, mar, N):
        
        #shutil.copyfile(testFolder + 'marIdle0.message', targetFolder + "mar.message")
        
        self.startChangingFiles(55, 1)          ## start from ...mar55.message
        print "Start copying files..."
        
        for i in range(0, N, 1):
            print "=============="
            print "Erasing " + str(i+1) + " of " + str(N)
            
            idle = self.waitForMarStatus(mar, 300, 0)
            if (not idle):
                print"Timed out waiting for mar to be ready, so scan not performed"
                return
                       
            mar.sendKeywords("ERASE command")
            
            busy = self.waitForMarStatus(mar, 300, 1)
            if (not busy):
                print"Timed out waiting for mar to start, so scan not performed"
                return
    
            idle = self.waitForMarStatus(mar, 300, 0)
            if (not idle):
                print"Timed out waiting for mar to stop scanning, so scan not performed"
                return            
    
            print"Erasures complete"
            stopThread = True
    	
    def waitForMarStatus(self, mar, timeout, status):
         
        t0 = time.clock()
        t1 = t0
        t2 = t0
        i = 0
        while ( (t1 - t0) < timeout ): 
            st = mar.getStatus()
            if ( (t1 - t2) > 1):
                t2 = time.clock()
                print"status = " + str(st)
                
            if (st == status):
                print"Mar status of " + str(status) + " reached in time %.2f" % (t1 - t0) + "s"
                return True
            t1 = time.clock()
 
#         
#        t0 = time.clock()
#        t1 = t0
#        while ( (t1 - t0) < timeout ): 
#			if (mar.getStatus() == status):
#				print"reached status of " + str(status) + " in time %.2f" % (t1 - t0) + "s"
#				return True
#			t1 = time.clock()
            
        print"Timed out waiting for mar status of " + str(status) + " (waited " + str(timeout) + "s)"
        return False
        
    def startChangingFiles(self, startMarFileNo, delay):
        stopThread = False
        newThread = ChangeMarFile(startMarFileNo, delay)
        t = Thread(newThread)
        t.start()
		
class ChangeMarFile(Runnable):
    def __init__(self, startMarFileNo, delay):
        self.marFileNo = startMarFileNo
        self.delay = delay
    
    def run(self):
        print "Run through " + sourceFolder + "marN.message from " + str(self.marFileNo) + " with delay of " + str(self.delay)
        filesAvailable = True  
        while (filesAvailable and not stopThread):
            time.sleep(self.delay)
            file = sourceFolder + "scan3oct29mar" + str(self.marFileNo) + ".message"
            pathList = glob.glob(file)
            if (len(pathList) > 0):
            #if (os.path.isdir(file)):
                shutil.copyfile(file, targetFolder + "mar.message")
                print "->copied " + file 
                self.marFileNo = self.marFileNo + 1
            else:
                filesAvailable = False
	
if __name__ == '__main__':
	print "Mar345DetectorTest"
	suite = unittest.TestLoader().loadTestsFromTestCase(Mar345DetectorTest)
	unittest.TextTestRunner(verbosity=2).run(suite)