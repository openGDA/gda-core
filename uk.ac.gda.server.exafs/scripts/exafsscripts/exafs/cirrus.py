from threading import Thread
from gda.data import PathConstructor;
import time

class CollectCirrusData(Thread):
        
    def __init__(self, itterations, interval, filename, cirrus, sample_temperature, sample_temperature2, energy_scannable, initial_energy, final_energy, mList):
        Thread.__init__(self)
        self.itterations = itterations
        self.interval = interval
        from gda.data import NumTracker;
        numTracker = NumTracker("tmp")
        thisFileNumber = numTracker.getCurrentFileNumber() + 1;
        print filename + "/" + str(thisFileNumber) + "_cirrus.txt"
        self.filename = filename + str(thisFileNumber) + "_cirrus.txt"
        self.cirrus = cirrus
        self.sample_temperature = sample_temperature
        self.sample_temperature2 = sample_temperature2
        self.initial_energy = initial_energy
        self.final_energy = final_energy
        self.energy_scannable = energy_scannable
        self.mList = mList
        
    def run(self):
        timeout = 20
        t = 0
        while int(self.energy_scannable()) not in range(self.initial_energy - 10, self.initial_energy + 10):
            t += 1
            if t == timeout:
                break
        
        timeCounter = 0
        
        f = open(self.filename, 'w')
        f.write("time    ")
        for m in self.mList:
            f.write(str(m) + "    ")
        f.write("temperature")
        f.write("\n")
        
        for itt in range(self.itterations):
            print "writing cirrus data"
            self.cirrus.collectData()
            data = self.cirrus.readout().toString().split('\t')
            f.write(str(timeCounter) + "    ")
            for d in data:
                f.write(str(d) + "    ")
            f.write(str(self.sample_temperature()) + "\n")
            f.write(str(self.sample_temperature2()) + "\n")
            timeCounter += self.interval
            time.sleep(self.interval)
            
        f.close()
        
def _control_cirrus_detector(self, bean, initial_energy, final_energy, scan_time):
    masses = []
    masses.append(bean.isMass2())
    masses.append(bean.isMass4())
    masses.append(bean.isMass12())
    masses.append(bean.isMass14())
    masses.append(bean.isMass15())
    masses.append(bean.isMass16())
    masses.append(bean.isMass17())
    masses.append(bean.isMass18())
    masses.append(bean.isMass28())
    masses.append(bean.isMass32())
    masses.append(bean.isMass36())
    masses.append(bean.isMass40())
    masses.append(bean.isMass44())
    masses.append(bean.isMass64())
    masses.append(bean.isMass69())
    self.initial_energy = initial_energy
    self.final_energy = final_energy
    self.scan_time = scan_time
    i = 0
    massList = []
    mList = [2, 4, 12, 14, 15, 16, 17, 18, 28, 32, 36, 40, 44, 64, 69]
    for mass in masses:
        if mass is True:
            massList.append(mList[i])
        i += 1
    if len(massList) > 0:
        print str(massList)
        self.cirrus.setMasses(massList)
    else:
        massList = self.cirrus.getMasses()
    print "Number of cirrus reads=" + str(int(self.scan_time / bean.getInterval()))
    mythread = CollectCirrusData(int(self.scan_time / bean.getInterval()), bean.getInterval(), PathConstructor.createFromProperty("gda.device.cirrus.datadir"), self.cirrus, self.sample_temperature, self.sample_temperature2, self.energy_scannable, self.initial_energy, self.final_energy, massList)
    mythread.start()
    
#self.cirrus = cirrus
#self.sample_temperature = sample_temperature
#self.sample_temperature2 = sample_temperature2