'''
This class uses position compare to determine motor status and set up Monitor on readback PV to notify IObsesrvers.
It does not use low level Motor Status directly.
Only 3 PVs - set, readback, and stop - are required. No other motor status are handled by this class.
The motor's positional tolerance must be set, that is the same as the retry deadband value of the Motor Record.

Created on 27 Oct 2010
Updated on 1 Oct 2026

@author: fy65,eir17846
'''

from gda.epics import CAClient
from gda.device.scannable import ScannableMotionBase
from gov.aps.jca.event import MonitorListener # @UnresolvedImport

class PositionCompareMotorReadbackUpdateClass(ScannableMotionBase, MonitorListener):
	'''Create a scannable for a single motor'''
	def __init__(self, name, pvinstring, pvoutstring, pvstopstring, tolerance, unitstring, formatstring):
		self.setName(name);
		self.setInputNames([name])
		self.Units=[unitstring]
		self.setOutputFormat([formatstring])
		self.setLevel(3)
		self.incli=CAClient(pvinstring)
		self.outcli=CAClient(pvoutstring)
		self.stopcli=CAClient(pvstopstring)
		self._tolerance=tolerance
		self.monitor = None #monitor on readback to update observers

	def configure(self):
		if (self.isConfigured()):
			print "PositionCompareMotorReadbackUpdateClass is already configured"
			return
		# Detangle initialisation with connect. Connect after object is created so it can still be imported
		# into localstaion and connected later.
		if not self.incli.isConfigured():
			self.incli.configure()
		if not self.outcli.isConfigured():
			self.outcli.configure()
		if not self.stopcli.isConfigured():
			self.stopcli.configure()
		self.outcli.caget() # make sure channel connected
		self.monitor = self.outcli.camonitor(self)
		super(PositionCompareMotorReadbackUpdateClass, self).configure()

	def monitorChanged(self, mevent):
		self.new_value = float(mevent.getDBR().getDoubleValue()[0])
		self.notifyIObservers(self, self.new_value)

	def setTolerance(self, tolerance):
		self._tolerance=tolerance

	def getTolerance(self):
		return self._tolerance

	def rawGetPosition(self):
		try:
			if not self.outcli.isConfigured():
				self.outcli.configure()
			output=float(self.outcli.caget())
			return output
		except:
			print "Error returning current position"
			return 0

	def getTargetPosition(self):
		try:
			if not self.incli.isConfigured():
				self.incli.configure()
			target=float(self.incli.caget())
			return target
		except:
			print "Error returning target position"
			return 0

	def rawAsynchronousMoveTo(self,new_position):
		try:
			if not self.incli.isConfigured():
				self.incli.configure()
			self.incli.caput(new_position)
		except:
			print "error moving to position"

	def isBusy(self):
		return (abs(self.rawGetPosition() - self.getTargetPosition()) >= self._tolerance)

	def stop(self):
		if not self.stopcli.isConfigured():
			self.stopcli.configure()
		self.stopcli.caput(1)

	def dispose(self):
		self.incli.clearup()
		if (self.monitor!=None):
			self.outcli.removeMonitor(self.monitor)
		self.outcli.clearup()
		self.stopcli.clearup()

	def toString(self):
		return self.name + " : " + str(self.getPosition())

