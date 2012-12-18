import threading
import datetime
from time import sleep
from gda.data import PathConstructor
from gda.factory import Finder

class ThreadClass(threading.Thread):

    def __init__(self, cirrus, energyScannable, startEnergy, stopEnergy, fileName):
        super(ThreadClass, self).__init__()
        self.cirrus = cirrus
        self._stop = threading.Event()
        self.energyScannable = energyScannable
        self.startEnergy = startEnergy
        self.stopEnergy = stopEnergy
        self.fileName = fileName
        
    def stop(self):
        print "cirrus thread stopped"
        self._stop.set()
        f.close()
        
    def stopped(self):
        return self._stop.isSet()
    
    # add method to run indepenent of qexafs and specify filename
    def run(self):
        print "started cirrus thread"
        dataDir = PathConstructor.createFromProperty("gda.data.scan.datawriter.datadir");
        path = dataDir + self.fileName
        f=open(path,'w')
        
        f.write("pressure=")
        f.write(str(self.cirrus.getPressure())+"\n\n")
        
        f.write("# energy")
        for i in range(len(self.cirrus.getMasses())):
            f.write("\tmass " + str(i+1))
        f.write("\n")    
        
        while self.stopped()==False and (self.energyScannable()<self.startEnergy or self.energyScannable()>self.stopEnergy):
            sleep(1)
            
        while self.stopped()==False and (self.energyScannable()>=self.startEnergy and self.energyScannable()<=self.stopEnergy):
            if self.stopped()==False:
                now = datetime.datetime.now()
                self.cirrus.collectData()
                data = self.cirrus.readout()
                currentEnergy = self.energyScannable()
                f.write(str(currentEnergy))
                for d in data:
                    f.write("\t"+str(d))
                #add time from sys clock
                f.write("\n")
                sleep(1)
        f.close()
        print "finished cirrus thread"

#cirrus.setMasses([2, 28, 32])
#finder = Finder.getInstance()
#if energyScannable==None:
#    energyScannable = finder.find("qexafs_energy")
#t = ThreadClass(qexafs_energy, 10000, 15000, "cirrus_scan.dat")
#t.setName("cirrus")
#t.start()