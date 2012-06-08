from gda.epics import CAClient
from java import lang
from gda.device.scannable import ScannableMotionBase
from time import sleep

#The Class for creating a Scaler channel monitor directly from EPICS PV
#For 8512 Scaler Card. This scaler card is not supported by EPICS scaler record
class ScalerChannelEpicsPVClass(ScannableMotionBase):
	def __init__(self, name, strChTP, strChCNT, strChSn):
		self.setName(name);
		self.setInputNames([]);
		self.setExtraNames([name]);
#		self.Units=[strUnit];
		#self.setLevel(5);
		self.setOutputFormat(["%20.12f"]);
		self.chTP=CAClient(strChTP);
		self.chCNT=CAClient(strChCNT);
		self.chSn=CAClient(strChSn);
		self.tp = -1;

#		self.setTimePreset(time)

	def atStart(self):
		if not self.chTP.isConfigured():
			self.chTP.configure()
		if not self.chCNT.isConfigured():
			self.chCNT.configure()
		if not self.chSn.isConfigured():
			self.chSn.configure()

	#Scannable Implementations
	def rawGetPosition(self):
		return self.getCount();
	
	def rawAsynchronousMoveTo(self,newPos):
		self.setCollectionTime(newPos);
		self.collectData();

	def rawIsBusy(self):
		return self.getStatus()

	def atEnd(self):
		if self.chTP.isConfigured():
			self.chTP.clearup()
		if self.chCNT.isConfigured():
			self.chCNT.clearup()
		if self.chSn.isConfigured():
			self.chSn.clearup()


	#Scaler 8512 implementations		
	def getTimePreset(self):
		if self.chTP.isConfigured():
			newtp = self.chTP.caget()
		else:
			self.chTP.configure()
			newtp = float(self.chTP.caget())
			self.chTP.clearup()
		self.tp = newtp
		return self.tp

	#Set the Time Preset and start counting automatically
	def setTimePreset(self, newTime):
		self.tp = newTime
		newtp = newTime;
		if self.chTP.isConfigured():
			tp = self.chTP.caput(newtp)
		else:
			self.chTP.configure()
			tp = self.chTP.caput(newtp)
			self.chTP.clearup()
#		Thread.sleep(1000)	

	def getCount(self):
		if self.chSn.isConfigured():
			output = self.chSn.caget()
		else:
			self.chSn.configure()
			output = self.chSn.caget()
			self.chSn.clearup()
		return float(output)


	#Detector implementations
	
	#Tells the detector to begin to collect a set of data, then returns immediately.
	#public void collectData() throws DeviceException;
	#Set the Time Preset and start counting automatically
	def collectData(self):
		#self.setTimePreset(self.tp)
		if self.chCNT.isConfigured():
			tp = self.chCNT.caput(1)
		else:
			self.chCNT.configure()
			tp = self.chCNT.caput(1)
			self.chCNT.clearup()
#		Thread.sleep(1000)	

	#Tells the detector how long to collect for during a call of the collectData() method.
	#public void setCollectionTime(double time) throws DeviceException;
	def setCollectionTime(self, newTime):
		self.setTimePreset(newTime)
		
	#Returns the latest data collected.
	#public Object readout() throws DeviceException;
	def getCollectionTime(self):
		nc=self.getTimePreset()
		return nc

	#Returns the current collecting state of the device.
	# return ACTIVE (1) if the detector has not finished the requested operation(s), 
	#        IDLE(0) if in an completely idle state and 
	#        STANDBY(2) if temporarily suspended.
	#public int getStatus() throws DeviceException;
	def getStatus(self):
		if self.chCNT.isConfigured():
			self.stauts = self.chCNT.caget()
		else:
			self.chCNT.configure()
			self.stauts = self.chCNT.caget()
			self.chCNT.clearup()	
		if self.stauts == '0': #still counting, Busy
			return 0
		else:
			return 1


