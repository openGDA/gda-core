#
# A collection of pds which can be used in scans to help time the scan steps.
# To create the pds simply type: run time_pds
#

import time
from gda.device.scannable import ScannableMotionBase

from org.slf4j import Logger
from org.slf4j import LoggerFactory

class tictoc:
	"""Class tictoc. Creates new timer object.
	__call__ returns numerical value of elapsed time in seconds since initialization
	__repr__ returns a mesage containing the elapsed time
	reset resets timer
	e.g. t1=tictoc()  -  create a new timer
	t1 -  display elapsed time"""

	def __init__(self):
		self.starttime=time.clock();
	def reset(self):
		self.starttime=time.clock();
		return 'Resetting timer'	
	def __call__(self):
		return time.clock()-self.starttime;
	def __repr__(self):
		return 'Elapsed time: %.4g seconds' % (time.clock()-self.starttime)


class showtimeClass(ScannableMotionBase):
	"""showtimeClass - show time since initialization or atStart. Useful for timing scan points"""
	def __init__(self, name):
		self.setName(name);
		self.setInputNames(['Elapsed']);
		self.Units=['sec']
		self.setOutputFormat(['%6.2f'])
		self.setLevel(7)
		self.timer=tictoc()
		self.waitUntilTime = 0

	def atScanStart(self):
		#print "Reseting timer"
		self.timer.reset()
		self.waitUntilTime = 0

	def rawGetPosition(self):
		#print "Returning time"
		return self.timer()

	def rawAsynchronousMoveTo(self,waitUntilTime):
		#print "Changing waitUntilTimer to: ", waitUntilTime
		self.waitUntilTime=waitUntilTime

	def stop(self):
		self.waitUntilTime = 0

	def rawIsBusy(self):
		#print "Checking rawIsBusy"
		if self.timer()<self.waitUntilTime:
			#print "Is busy"
			return 1
		else:
			#print "Is not busy"
			return 0
	

class showincrementaltimeClass(ScannableMotionBase):
	'''showincrementaltimeClass - show time increment since last call. Useful for timing scan points'''
	def __init__(self, name):
		self.setName(name);
		self.setInputNames([])
		self.setExtraNames(['Elapsed']);
		self.Units=['sec']
		self.setOutputFormat(['%6.2f'])
		self.setLevel(7)
		self.timer=tictoc()

	def rawGetPosition(self):
		t=self.timer()
		self.timer.reset()
		return t

	def rawIsBusy(self):
		return 0
	
	def atScanStart(self):
		self.timer.reset()



class waittimeClass(ScannableMotionBase):
	'''waittimeClass - waits for elapsed time. Use as dummy counter in scans etc'''
	
	def __init__(self, name):
		self.setName(name);
		self.setInputNames(['Time'])
		self.Units=['sec'];
		self.setOutputFormat(['%6.2f'])
		self.setLevel(7)
		self.timer=tictoc()
		self.waitfortime=0
		self.currenttime=0

	def rawAsynchronousMoveTo(self,waittime):
		self.currenttime=self.timer()
		self.waitfortime=self.currenttime+waittime

	def rawGetPosition(self):
		return self.timer()-self.currenttime

	def rawIsBusy(self):
		if self.timer()<self.waitfortime:
			return 1
		else:
			return 0

class waittimeClass2(ScannableMotionBase):
	'''waittimeClass - waits for elapsed time. Use as dummy counter in scans etc'''
	
	def __init__(self, name):
		self.setName(name);
		self.setInputNames(['Time'])
		self.Units=['sec'];
		self.setOutputFormat(['%6.2f'])
		self.setLevel(7)
		self.timer=tictoc()
		self.waitfortime=0
		self.currenttime=self.timer()

	def rawAsynchronousMoveTo(self,waittime):
		self.currenttime=self.timer()
		self.waitfortime=self.currenttime+waittime

	def rawGetPosition(self):
		return 0.

	def rawIsBusy(self):
		if self.timer()<self.waitfortime:
			return 1
		else:
			return 0

class waittimeClass3(ScannableMotionBase):
	'''waittimeClass - waits for elapsed time. Use as dummy counter in scans etc'''
	
	def __init__(self, name):
		self.setName(name);
		self.setInputNames(['PausedTime'])
		self.Units=['sec'];
		self.setOutputFormat(['%6.2f'])
		self.setLevel(7)
		self.timer=tictoc()
		self.waitfortime=0
		self.currenttime=self.timer()
		self.waittime = 0

	def rawAsynchronousMoveTo(self,waittime):
		self.currenttime=self.timer()
		self.waitfortime=self.currenttime+waittime
		self.waittime = waittime

	def rawGetPosition(self):
		return self.waittime

	def rawIsBusy(self):
		if self.timer()<=self.waitfortime:
			return 1
		else:
			return 0


class actualTimeClass(ScannableMotionBase):
	"""Reports the result of time.time()"""
	def __init__(self, name):
		self.setName(name);
		self.setInputNames([]);
		self.setExtraNames([name]);
		self.setOutputFormat(["%f"])
		self.setLevel(7)
		self.waitUntilTime = 0

	def rawGetPosition(self):
		return time.time()

	def rawIsBusy(self):
		return False
	
class LogTimeClass(ScannableMotionBase):
	"""Logs the scan progress to the GDA logging system"""
	def __init__(self, name):
		self.setName(name);
		self.setInputNames([]);
		self.setExtraNames([]);
		self.setOutputFormat([])
		self.setLevel(7)
		self.logger = LoggerFactory.getLogger(name)

	def rawGetPosition(self):
		return None

	def rawIsBusy(self):
		return False
	
	def atScanStart(self):
		self.logger.info("atScanStart")
		
	def atScanEnd(self):
		self.logger.info("atScanEnd")
		
	def atPointStart(self):
		self.logger.info("atPointStart")

	def atPointEnd(self):
		self.logger.info("atPointEnd")

#if __name__ == "__main__":
showtime=showtimeClass('showtime')
inctime=showincrementaltimeClass('inctime')
waittime=waittimeClass2('waittime')
print "finished time_utilities: providing objects: 'showtime','inctime','waittime', created from classes: tictoc, showtimeClass, showincrementaltimeClass, waittimeClass2"
