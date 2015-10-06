"""
Defines Scannables that deal with time. Instantiates four standard Scannables:
   t = TimeSinceScanStart('t')
   dt = TimeSinceLastGetPosition("dt")
   w = Wait("w") # time since last asked to move
   clock = TimeOfDay('clock')
   
Silly module name required as time is taken.
"""

__all__ = ["t", "dt", "w", "clock", "epoch"]


from gda.device.scannable import PseudoDevice
import time # as this module is names time

class _Timer(object):

	def __init__(self):
		self.lastStartClock = 0
		self.start()
		
	def start(self):
		self.lastStartClock = time.time();
		
	def elapsed(self):
		return time.time() - self.lastStartClock
	
	def hasElapsed(self, deltaTime):
		return self.elapsed() >= deltaTime


class TimeSinceInstantiation(PseudoDevice):
	"""
	Returns the time since instantiation. When driven to a time will not return until this time has elapsed.
	"""
	def __init__(self, name):
		self.name = name
		self.inputNames = [name]
		self.outputFormat = ['%6.2f']
		self.level = 7
		
		self.timer = _Timer()
		self.timer.start()
		self.deltaTime = 0

	def getPosition(self):
		return self.timer.elapsed()

	def asynchronousMoveTo(self, deltaTime):
		self.deltaTime = deltaTime

	def stop(self):
		self.deltaTime = 0

	def isBusy(self):
		return not self.timer.hasElapsed(self.deltaTime)

	
class TimeSinceScanStart(TimeSinceInstantiation):
	"""
	Returns the time since the last scan started. When driven to a time will remain busy until this time has elapsed.
	"""
	def atScanStart(self):
		self.deltaTime = 0
		self.timer.start()


class TimeSinceLastGetPosition(TimeSinceScanStart):
	"""
	Returns the time since the last call to getPosition. When driven to a time will remain busy until this time has elapsed.
	"""
	def getPosition(self):
		deltaTime = self.timer.elapsed()
		self.timer.start()
		return deltaTime
	

class Wait(TimeSinceInstantiation):
	"""
	Returns the time since last asked to move. When driven to a time will remain busy until this time has elapsed.
	"""
	def asynchronousMoveTo(self, deltaTime):
		self.deltaTime = deltaTime
		self.timer.start()


class _Clock(object):
	
	def __init__(self):
		self.lastClockedTimeTuple = None
		
	def currentTimeAsTuple(self):
		t = time.time()
		timestruct = time.localtime(t)
		h, m, s = timestruct[3:6]
		fractional_s = t - time.mktime(timestruct)
		return h, m, s + fractional_s
	
	def hasPast(self, timeTuple):
		for current, target in zip(self.currentTimeAsTuple(), timeTuple):
			if current > target:
				return True
		return False
	
	def lastClock(self):
		return self.lastClockedTimeTuple or self.currentTimeAsTuple()

	def clock(self):
		self.lastClockedTimeTuple = self.currentTimeAsTuple()
	
	def clear(self):
		self.lastClockedTimeTuple = None
		
		

class TimeOfDay(PseudoDevice):
	"""
	Returns the time of day in hours minutes and seconds. More specifically the returned time is that clocked when the
	scannables at the same level where moved. When asked to move to an (h, m, s) tuple will remain busy until this time
	of day is reached (it does not know about midnight yet!).
	"""

	def __init__(self, name):
		self.name = name
		self.inputNames = ['h', 'm', 's']
		self.outputFormat = ['%i' , '%i', '%.2f']
		self.level = 9
		
		self.clock = _Clock()
		self.targetTimeTuple = (0, 0, 0)
		
	def atLevelMoveStart(self):
		self.clock.clock()

	def asynchronousMoveTo(self, targetTimeTuple):
		self.targetTimeTuple = targetTimeTuple
	
	def isBusy(self):
		self.clock.clock()
		return not self.clock.hasPast(self.targetTimeTuple)
	
	def getPosition(self):
		position = self.clock.lastClock()
		self.clock.clear()
		return position
		
	def atPointEnd(self):
		self.clock.clear()

	def atCommandFailure(self):
		self.clock.clear()

class TimeSinceEpoch(PseudoDevice):
    """
  
    """

    def __init__(self, name):
        self.name = name
        self.inputNames = ['epoch']
        self.extraNames = []
        self.outputFormat = ['%.3f']
        self.level = 9
        self.target_time = None

    def asynchronousMoveTo(self, time):
        self.target_time = time
        
    def isBusy(self):
        if self.target_time is None:
            return False
        return time.time() <= self.target_time
    
    def getPosition(self):
        return time.time()
       

t = TimeSinceScanStart('t')
dt = TimeSinceLastGetPosition("dt")
w = Wait("w")
clock = TimeOfDay('clock')
epoch = TimeSinceEpoch('epoch')
