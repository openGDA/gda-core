'''
module provides a class definition for creating a PseudoDevice from a EPICS PV name with an EPICS Monitor Listener and a monitoring thread.
The thread is used to control the scan process that this object involved in. When this object reaches a specified maximum scan pauses; 
the scan would resume again only if this object reaches a specified minimum value.
 usage:
		temp = EpicsPVWithMonitorListener('temp', 'ME02P-MO-RET-01:ROT:TEMP', 'degree', '%.4.1f')
 
 more examples in pvmonitor.polarimeterTemperatureMonitor.py
		
Created on 15 Dec 2009

@author: fy65
'''

MAXTEMP=100.0
MINTEMP=50.0
from gov.aps.jca.event import MonitorEvent
from gov.aps.jca.event import MonitorListener
from time import sleep
from gda.epics import CAClient
from gda.device.scannable import PseudoDevice
from java import lang
from java.lang import Thread, Runnable
from gda.jython import JythonServerFacade, Jython

class EpicsPVWithMonitorListener(PseudoDevice, MonitorListener, Runnable):
	'''create a scannable that monitors the EPICS PV value changes and update its value passively. 
	This value is used by a running thread to control the scan processing this object participates in.
	'''
	def __init__(self, name, pvstring, unitstring, formatstring):
		self.setName(name)
		self.setInputNames([name])
		self.Units=[unitstring]
		self.setOutputFormat([formatstring])
		self.setLevel(5)
		self.outcli=CAClient(pvstring)
		self.currenttemp=float(self.rawGetPosition())
		self.monitor=None
		self.thread=None
		self.runThread=False

	def atScanStart(self):
		'''prepare to start scan: creating channel, monitor, and start control thread'''
		if not self.outcli.isConfigured():
			self.outcli.configure()
			self.monitor=self.outcli.camonitor(self)
			self.thread=Thread(self,"Thread: "+self.getName())
			self.runThread=True
			self.thread.start()
	def atScanEnd(self):
		'''clean up after scan finished successfully: remove monitor, destroy channel, and stop control thread'''
		if self.outcli.isConfigured():
			self.outcli.removeMonitor(self.monitor)
			self.monitor=None
			self.outcli.clearup()
			self.runThread=False
			self.thread=None
			
	def rawGetPosition(self):
		''' return current position.'''
		output=0.0
		if not self.outcli.isConfigured():
			self.outcli.configure()
			output=float(self.outcli.caget())
			self.outcli.clearup()
		else:
			output=float(self.outcli.caget())
		return float(output)

	def rawAsynchronousMoveTo(self,position):
		'''Not implemented, this is only a monitoring object'''
		print "object " + self.getName()+" cannot be moved."
		return

	def rawIsBusy(self):
		'''monitoring object never busy'''
		return 0

	def monitorChanged(self, mevent):
		self.currenttemp = float(mevent.getDBR().getDoubleValue()[0])

	def run(self):
	#	print "Thread: " + self.getName() + " started"
		while (self.runThread):
			if (JythonServerFacade.getInstance().getScanStatus() == Jython.RUNNING and self.currenttemp >= float(MAXTEMP)):
				JythonServerFacade.getInstance().pauseCurrentScan()	
				print "Scan paused as temperature " + self.getName() +" returns: "+str(self.currenttemp)
			elif (JythonServerFacade.getInstance().getScanStatus() == Jython.PAUSED and self.currenttemp <= float(MINTEMP)):
				print "Scan resumed as temperature " + self.getName() +" returns: "+str(self.currenttemp)
				JythonServerFacade.getInstance().resumeCurrentScan()
			sleep(10)


	def stop(self):
		'''clean up after scan finished successfully: remove monitor, destroy channel, 
		and stop control thread on emergence stop or unexpected crash. If required, can be used to manually clean up the object.'''
		if not self.monitor == None:
			self.outcli.removeMonitor(self.monitor)
			self.monitor=None
		if self.outcli.isConfigured():	
			self.outcli.clearup()
		if not self.thread == None:
			self.runThread=False
			self.thread=None
		


