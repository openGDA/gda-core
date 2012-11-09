from gda.epics import CAClient
from java import lang
from time import sleep
import math
from gda.device.scannable import PseudoDevice
from gda.device import DeviceException

class Linkam(PseudoDevice):
	'''Create PD to operate Linkam'''
	def __init__(self, name, pvstring):
		self.setName(name)
		self.pvbase = pvstring
		self.temp=CAClient(pvstring+":TEMP")
		self.logreset=CAClient(pvstring+":LOG:RESET")
		self.logread=CAClient(pvstring+":LOG:STARTREAD")
		self.logdsc=CAClient(pvstring+":LOG:DSC:CMP")
		self.logtemp=CAClient(pvstring+":LOG:TEMP:CMP")
		self.lognuse=CAClient(pvstring+":LOG:TEMP:CMP.NUSE")
		self.rampctl=CAClient(pvstring+":RAMP:CTRL:SET")
		self.ramplmt=CAClient(pvstring+":RAMP:LIMIT:SET")
		self.ramprte=CAClient(pvstring+":RAMP:RATE:SET")
		self.status=CAClient(pvstring+":STATUS")
		self.disable=CAClient(pvstring+":DISABLE")
		self.temp.configure()
		self.ramplmt.configure()
		self.rampctl.configure()
		self.ramprte.configure()
		self.status.configure()
		self.logreset.configure()
		self.logdsc.configure()
		self.logtemp.configure()
		self.logread.configure()
		self.lognuse.configure()
		self.disable.configure()
		self.rampctl.caput("Stop")
		self.state=0
		self.nextmove=25
		self.tries=25
		self.enableDSC()
		self.nextLimit = None

	def enableDSC(self):
		self.setInputNames([self.getName()+"temp"])
		self.setExtraNames([self.getName()+"dsc"])
		self.setOutputFormat(["%5.1f", "%5.1f"])
		self.dscenabled = True

	def disableDSC(self):
		self.setInputNames([self.getName()+"temp"])
		self.setExtraNames([])
		self.setOutputFormat(["%5.1f"])
		self.dscenabled = False

	def setRate(self, rate):
		self.ramprte.caput(rate)

	def getStatus(self):
		return self.status.caget()

	def getRate(self):
		return float(self.ramprte.caget())

	def setLimit(self, limit):
		self.ramplmt.caput(limit)

	def getLimit(self):
		return float(self.ramplmt.caget())

	def start(self):
		self.rampctl.caput("Start")
	
	def stop(self):
		self.rampctl.caput("Stop")
		self.state=0

	def hold(self):
		self.rampctl.caput("Hold")
		self.state=0

	def setNextScanLimit(self, limit):
		self.nextLimit = limit

	def atScanStart(self):
		if not self.nextLimit == None:
			self.setLimit(self.nextLimit)
			self.nextLimit = None

	def atScanEnd(self):
		self.hold()

	def getPosition(self):
		if int(self.disable.caget()) == 1:
			raise DeviceException(self.getName()+" disabled in EPICS")
		if self.dscenabled:
			#return float(self.temp.caget())
			# synchronously reset
			self.logreset.caput(50,1)
			# asynchronously start reading
			# as soon as there is data it will be fresh from after the reset
			n=0
			while n<self.tries:
				self.logread.caput(1)
				sleep(0.11)
				if int(float(self.lognuse.caget())) > 0:
					return [float(self.logtemp.cagetArray()[0]), float(self.logdsc.cagetArray()[0])]
				n = n + 1
			self.disableDSC()
			raise IOError, "timeout reading log/dsc values from %s. DSC disabled for now. " % (self.name)
		else:
			return [float(self.temp.caget())]
		
	def asynchronousMoveTo(self,p):
		if int(self.disable.caget()) == 1:
			raise DeviceException(self.getName()+" disabled in EPICS")
		self.nextmove=p
		t=float(self.temp.caget())
		l=float(self.ramplmt.caget())
		if (0.05 >= math.fabs(p-t)):
			self.state=0
			return
		if (p>t):
			self.state=1
			if (l<p):
				self.setLimit(p)
		if (p<t):
			self.state=-1
			if (l>p):
				self.setLimit(p)
		self.start()			
		return

	def isBusy(self):
		if (self.state==0):
			return 0
		if (self.state > 0):
			if (float(self.temp.caget()) + 0.05 >= self.nextmove):
				return 0
			else:
				return 1
		if (self.state < 0):
			if (float(self.temp.caget()) - 0.05 <= self.nextmove):
				return 0
			else:
				return 1
