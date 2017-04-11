'''
A module provides class definitions for Hexapod control. User should create instances of HexapodAxis for each of the available axes in hexapod.
Instance of Hexapod (i.e. the controller) is required to implement the interlock between axes, as you may only move one axis at any one time.
HexaPodAxisStatus is just a internal help class with which axis status can be queried.

Usage:
	Please see polarimeterHexapod.py for an example of usage.

'''

from gda.device.scannable import PseudoDevice
from gda.epics import CAClient
from time import sleep

class HexapodAxisStatus(object):
	'''Hexapod axis status class implementing position-compare algorithm with tolerance input. isBusy() method should be used to query the motion status of this axis.'''
	def __init__(self, name, pvinstring, pvoutstring, tolerance=0.01):
		self.name=name
		self.incli=CAClient(pvinstring)
		self.outcli=CAClient(pvoutstring)
		self.currentpos=0.0
		self.targetpos=0.0
		self._tolerance=tolerance

	def getCurrentPosition(self):
		try:
			if self.outcli.isConfigured():
				self.currentpos=float(self.outcli.caget())
			else:
				self.outcli.configure()
				self.currentpos=float(self.outcli.caget())
				self.outcli.clearup()
			return  self.currentpos
		except Exception, err:
			print "Error returning current position of " + self.name + err
			return 0

	def getTargetPosition(self):
		try:
			if self.incli.isConfigured():
				self.targetpos=float(self.incli.caget())
			else:
				self.incli.configure()
				self.targetpos=float(self.incli.caget())
				self.incli.clearup()
			return  self.targetpos
		except Exception, err:
			print "Error returning target position of " + self.name + err
			return 0

	def resetTargetPosition(self, target):
		try:
			if self.incli.isConfigured():
				self.incli.caput(target)
			else:
				self.incli.configure()
				self.incli.caput(target)
				self.incli.clearup()
		except Exception, err:
			print "Failed to reset target position of " + self.name + err
			return 0


	def isBusy(self):
		try:	
			if abs(self.getCurrentPosition() - self.getTargetPosition()) < self._tolerance:
				return 0
			else:
				return 1
		except Exception, err:
			print "Exception caught while checking if device " + self.name + " isBusy."
			print err.__str__()
			return 0

class Hexapod(PseudoDevice):
	'''hexapod controller class that implements interlock behaviour among the 6 axes. It's updateStatus() method will return 1 if any of the axes is busy, otherwise return 0.'''
	def __init__(self, name, x_inputPV, x_readbackPV, x_tolerance, y_inputPV, y_readbackPV, y_tolerance, z_inputPV, z_readbackPV, z_tolerance, c_inputPV, c_readbackPV, c_tolerance, b_inputPV, b_readbackPV, b_tolerance, a_inputPV, a_readbackPV, a_tolerance):
		self.setName(name);
		self.setInputNames([name])
		self.x=HexapodAxisStatus('x', x_inputPV, x_readbackPV, x_tolerance)
		self.y=HexapodAxisStatus('y', y_inputPV, y_readbackPV, y_tolerance)
		self.z=HexapodAxisStatus('z', z_inputPV, z_readbackPV, z_tolerance)
		self.yaw=HexapodAxisStatus('yaw', c_inputPV, c_readbackPV, c_tolerance)
		self.pitch=HexapodAxisStatus('pitch', b_inputPV, b_readbackPV, b_tolerance)
		self.roll=HexapodAxisStatus('roll', a_inputPV, a_readbackPV, a_tolerance)
		self.first=1
		self.startstatus=1

	def rawGetPosition(self):
		try:
			positions=[self.x.getCurrentPosition(),self.y.getCurrentPosition(),self.z.getCurrentPosition(),self.yaw.getCurrentPosition(),self.pitch.getCurrentPosition(),self.roll.getCurrentPosition()]
			return  positions
		except Exception, err:
			print "Error returning current positions from Hexpod", err
			return 0

	def rawAsynchronousMoveTo(self,new_position):
		print "You can not move more than 2 axises of hexapod at the same time."
		
	def setFirst(self):
		self.first=1
	
	def call(self):
		return self.rawGetPosition()

	def statusUpdate(self):
		try:	
			s=self.x.isBusy() + self.y.isBusy() + self.z.isBusy() + self.yaw.isBusy() + self.pitch.isBusy() + self.roll.isBusy()
			if s == 0:
				return 0
			elif s >= 1: 
				return 1
			else:
				return 0
		except Exception, err:
			print "Exception caught while checking if device " + self.getName() + " isBusy."
			print err.__str__()
			return 0


class HexapodAxis(PseudoDevice):
	'''scannable or pseudo device for an individual, single Hexapod axis, it takes 8 inputs in the following order:
		1. the name string of this object
		2. the PV string for input target value
		3. the PV string for read-back value
		4. the PV string that control or start the motion
		5. the positional tolerance within which the motor is treated as in-position
		6. the unit string used for the measurement, keyworded as 'unitstring'
		7. the format string for the return data, keyworded as 'formatstring'
		8. the hexapod controller instance
		
		for example,
			hpx=HexapodAxis('hpx', 'ME02P-MO-BASE-01:UCS_X','ME02P-MO-BASE-01:UCSXR', 'ME02P-MO-BASE-01:START.PROC', 0.01, 'mm', '%9.4f', hexapodController)
			
	'''
	def __init__(self, name, pvinstring, pvoutstring, pvctrlstring, tolerance=0.01, unitstring='mm', formatstring='%9.4f', controller=None):
		self.setName(name);
		self.setInputNames([name])
		self.Units=[unitstring]
		self.setOutputFormat([formatstring])
		self.setLevel(3)
		self.incli=CAClient(pvinstring)
		self.outcli=CAClient(pvoutstring)
		self.movecli=CAClient(pvctrlstring)
		self.lastpos=0.0
		self.currentpos=0.0
		self.targetpos=0.0
		self._tolerance=tolerance
		self.controller=controller

	def atScanStart(self):
		if not self.incli.isConfigured():
			self.incli.configure()
		if not self.outcli.isConfigured():
			self.outcli.configure()
		if not self.movecli.isConfigured():
			self.movecli.configure()

	def atScanEnd(self):
		if self.incli.isConfigured():
			self.incli.clearup()
		if self.outcli.isConfigured():
			self.outcli.clearup()
		if self.movecli.isConfigured():
			self.movecli.clearup()
			
	def rawGetPosition(self):
		try:
			if self.outcli.isConfigured():
				self.currentpos=float(self.outcli.caget())
			else:
				self.outcli.configure()
				self.currentpos=float(self.outcli.caget())
				self.outcli.clearup()
			return  self.currentpos
		except Exception, err:
			print "Error returning current position" + err
			return 0

	def rawAsynchronousMoveTo(self,new_position):
		self.controller.setFirst()
		while self.controller.statusUpdate():
			print "Hexapod is busy. Move " + self.getName() + " is waiting."+ "\nYou can only move one axis at any one time for this device "
			sleep(5)
		else:
			print "Move " + self.getName() + " to " + str(new_position)
		try:
			self.lastpos=float(self.rawGetPosition())
			if self.incli.isConfigured():
				self.incli.caput(new_position) 
			else:
				self.incli.configure()
				self.incli.caput(new_position)
				self.incli.clearup()
		except:
			print "error set " + self.getName() +" to the target position" + str(new_position)
		try:
			if self.movecli.isConfigured():
				self.movecli.caput(1) 
			else:
				self.movecli.configure()
				self.movecli.caput(1)
				self.movecli.clearup()
		except:
			print "error on start " + self.getName() +" to move" 

	def getTargetPosition(self):
		try:
			if self.incli.isConfigured():
				self.targetpos=float(self.incli.caget())
			else:
				self.incli.configure()
				self.targetpos=float(self.incli.caget())
				self.incli.clearup()
			return  self.targetpos
		except Exception, err:
			print "Error returning current position" + err
			return 0

	def rawIsBusy(self):
		try:	
			sleep(1) #EPICS update every second on these PVs
			self.controller.statusUpdate()
			self.currentpos=float(self.rawGetPosition())
			self.targetpos=float(self.getTargetPosition())
			if abs(self.targetpos - self.currentpos) < self._tolerance:
				return 0
			else:
				return 1
		except Exception, err:
			print "Exception caught while checking if device " + self.getName() + " isBusy."
			print err.__str__()
			return 0

