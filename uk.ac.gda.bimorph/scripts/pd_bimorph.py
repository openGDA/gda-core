from gda.device.scannable import ScannableMotionBase
from gda.factory import Finder
from time import sleep
from gda.epics import CAClient
import time

class Bimorph(ScannableMotionBase):
	"""
	bimorph controller for Epics bimorph controller developed in house
	To create if not already in localStation.py:
		import pd_bimorph
		bm_hfm = pd_bimorph.Bimorph_HFM()
		bm_vfm = pd_bimorph.Bimorph_VFM()
		
	to scan from current to current plus 100 in steps of 10:
		scan bm_hfm bm_hfm() bm_hfm.getPosPlusIncrement(100) bm_hfm.getListOfValues(10)
	to scan slits as well using gonx from -1. to 1 in steps of .1 and measure i_pin  
		scan bm_hfm bm_hfm() bm_hfm.getPosPlusIncrement(10) bm_hfm.getListOfValues(5)  gonx -2. -1.5 .1 i_pin

	to plot i_pin against gonx:
		LocalProperties.set("gda.scan.useScanPlotSettings","True")
	to plot i_pin and only the first channel of the mirror:
		LocalProperties.set("gda.plot.ScanPlotSettings.YFieldIndicesInvisible","0")
		
	sleepdInS - time to wait after sending changes to EPICS for the bimorph to respond fully	
	"""
	def __init__(self, name, startChan,numofChans, pvPrefix, sleepInS):
        # eembimorph = EemBimorph("eembimorph", 0, 8, "EEM_Bimorph:", sleepInS=0)
		self.numOfChans=numofChans
		self.startChan=startChan
		self.channelIndexes = tuple(range(startChan, startChan+numofChans))
		self.setName(name)
		self.pvPrefix=pvPrefix
		self.sleepInS=sleepInS
		inputNames=[]
		extraNames=[]
		self.pos=[]
		for i in range(self.numOfChans):
			inputNames.append("C"+`i`)
			self.pos.append(0.) 
		self.setInputNames(inputNames)
		for i in range(self.numOfChans):
			extraNames.append("A"+`i`)
			self.pos.append(0.) 
		self.setExtraNames(extraNames)
		self.configure()
		self.IAmBusy=False

	def configure(self):
		self.beamline=Finder.getInstance().find("Beamline")

	def rawIsBusy(self):
		return self.IAmBusy

	def rawGetPosition(self):
		for i in range(self.startChan,self.startChan+self.numOfChans):
			self.pos[i-self.startChan]=self.beamline.getValue(None,"Top",self.pvPrefix+`i`+"DR")
		for i in range(self.startChan,self.startChan+self.numOfChans):
			self.pos[i-self.startChan++self.numOfChans]=self.beamline.getValue(None,"Top",self.pvPrefix+`i`+"R")
		return self.pos

	def rawAsynchronousMoveTo(self,new_position):
		self.IAmBusy=True
		try:
			for i in range(self.startChan,self.startChan+self.numOfChans):
				self.beamline.setValue(None,"Top",self.pvPrefix+`i`+"D",new_position[i-self.startChan], 60)
			sleep(self.sleepInS)
		except:
			pass
		self.IAmBusy=False

	def getPosPlusIncrement(self, increment):
		pos = self.rawGetPosition()
		newPos=[]
		for i in range(self.numOfChans):
			newPos.append(pos[i]+increment)
		return newPos

	def getListOfValues(self, value):
		pos = []
		for i in range(self.numOfChans):
			pos.append(value)
		return pos

	def moveOne(self, motor, new_position):
		self.IAmBusy=True
		try:
			self.beamline.setValue("Top",self.pvPrefix+`motor`+"D",new_position)
			sleep(4)
		except:
			pass
		self.IAmBusy=False

	def moveAll(self, new_position):
		newPos=self.getListOfValues(new_position)
		self.rawAsynchronousMoveTo(newPos)

	def incrementOne(self, motor, increment):
		pos=self.beamline.getValue(None,"Top",self.pvPrefix+`motor`+"DR")
		self.moveOne(motor, pos+increment)
	
	def incrementAll(self, increment):
		newPos=self.getPosPlusIncrement(increment)
		self.rawAsynchronousMoveTo(newPos)

class DynamicPvManager(object):
	
	def __init__(self, pvroot):
		self.pvroot = pvroot
		self.clients = {}

	def __getitem__(self, pvname):
		try:
			return self.clients[pvname]
		except KeyError:
			self.add(pvname)
			return self.clients[pvname]
			
	def add(self, pvname):
		self.clients[pvname] = CAClient(self.pvroot + pvname)
		self.clients[pvname].configure()
		
APPLY_TARGET_PROFILE = True

class EemBimorph(Bimorph):
	"""
	bimorph controller for 'EEM_Bimorph-SY900S Bimorph PSU'
	pvPrefix is e.g. EEM_Bimorph:
	"""
	def configure(self):
		self.pvs = DynamicPvManager(self.pvPrefix)
	
	def rawGetPosition(self):
		target = []
		for i in self.channelIndexes:
			target.append(self._getChannelTarget(i))
		output = []
		for i in self.channelIndexes:
			output.append(self._readOutput(i))
		return target + output
	
	def rawAsynchronousMoveTo(self, targetArray):
		"""Blocks until move is under way
		"""
		if self.isBusy():
			raise Exception("Cannot move %s's to %s as it is busy" % (self.getName(), `targetArray`))
		self.__checkTargetSafe(targetArray)
		for (i, target) in zip (self.channelIndexes, targetArray):
			time.sleep(1)
			self._setChannelTarget(i, target)
		self.__waitForTargetSet(targetArray);
		time.sleep(5)
		self._applyTargetProfile()
		time.sleep(15)
		
	def rawIsBusy(self):
		for i in self.channelIndexes:
			if self._isTargetStatusBusy(i): return True
		return False
	
	def moveOne(self, motor, new_position):
		raise RuntimeError("Not supported")

	def incrementOne(self, motor, increment):
		raise RuntimeError("Not supported")
###

	def __checkTargetSafe(self, targetArray):
		# Every element must be set
		if len(targetArray) != len (self.channelIndexes):
			raise Exception("Cannot move %s to %s as %i elements are required" %(self.getName(), `targetArray`, len(self.channelIndexes)) )

		# No delta between elements to exceed 500v
		for i in range(len(targetArray)-1):
			delta = float(targetArray[i+1] - targetArray[i])
			if abs(delta) > 500:
				raise Exception("Cannot move %s to %s as a voltage delta (%s) was found to be greater than 500v" %(self.getName(), `targetArray`, `delta`) )

	def __waitForTargetSet(self, targetArray):
		starttime = time.time()
		while (time.time()-starttime) < 10:
			if self.__isTargetSet(targetArray):	return
			sleep(.2)
		raise Exception("Timed out setting %s's target to %s after 10 seconds" % (self.getName(), `targetArray`))

	def __isTargetSet(self, targetArray):
		for (i, target) in zip(self.channelIndexes, targetArray):	
			if float(target) != self._getChannelTarget(i):
				return False
		return True
	
	def _readOutput(self, i):
		return float(self.pvs['GET-VOUT%02i'%i].caget())
	
	def _setChannelTarget(self, i, target):
###		print "Setting first batch", i, target
		self.pvs['SET-VTRGT%02i' % i].caput(target)
		
	def _getChannelTarget(self, i):
		return float(self.pvs['GET-VTRGT%02i'%i].caget())
	
	def _applyTargetProfile(self):
		if APPLY_TARGET_PROFILE:
			self.pvs['SET-ALLTRGT'].caput(1)
		else:
			print "*** Not applying target profile. change with"
			print ">>> pd_bimorph.APPLY_TARGET_PROFILE = True"
		
	def _isTargetStatusBusy(self, i):
		return int(self.pvs['GET-STATUS%02i'%i].caget())!=1


class SixteenChannelEemBimorph(EemBimorph):
	""" MADNESS!:
$ caget EEM_Bimorph:GET-VTRGT07
EEM_Bimorph:GET-VTRGT07        -55
$ caget EEM_Bimorph:GET-VTRGT08
Channel connect timed out: 'EEM_Bimorph:GET-VTRGT08' not found.
$ caget Spare_8:GET-VTRGT00
Spare_8:GET-VTRGT00            0

	"""

	def __init__(self, name, startChan,numofChans, pvPrefix, pvPrefix2, sleepInS):
		assert startChan == 0
		assert numofChans == 16
		
		self.pvPrefix2 = pvPrefix2
		EemBimorph.__init__(self, name, startChan, numofChans, pvPrefix, sleepInS)
		
	def configure(self):
		EemBimorph.configure(self)
   		self.pvs2 = DynamicPvManager(self.pvPrefix2)
   		
	def _applyTargetProfile(self):
		print "bimorph moving first block of 8"
		EemBimorph._applyTargetProfile(self)
		sleep(15)

		if APPLY_TARGET_PROFILE:
			self.waitWhileBusy()
			print "bimorph moving second block of 8"
			sleep(5)
			self.pvs2['SET-ALLTRGT'].caput(1)
		

   	def _readOutput(self, i):
   		if i >= 8:
   			return float(self.pvs2['GET-VOUT%02i' % (i - 8)].caget())
   		else:
   			return EemBimorph._readOutput(self, i)
	
	def _setChannelTarget(self, i, target):
		if i >= 8:
###			print "Setting second batch", i, target
			self.pvs2['SET-VTRGT%02i' % (i - 8)].caput(target)
		else:
			EemBimorph._setChannelTarget(self, i, target)
		
	def _getChannelTarget(self, i):
		if i >= 8:
			return float(self.pvs2['GET-VTRGT%02i' % (i - 8)].caget())
		else:
			return EemBimorph._getChannelTarget(self, i)
	
	def _isTargetStatusBusy(self, i):
		if i >= 8:
			return int(self.pvs2['GET-STATUS%02i' % (i - 8)].caget()) != 1
		else:
			return EemBimorph._isTargetStatusBusy(self, i)
	

class Bimorph_HFM(Bimorph):
	def __init__(self):
		Bimorph.__init__(self,"hfm_bimorph",0,14,"-MO-PSU-01:BM:V",0)

class Bimorph_VFM(Bimorph):
	def __init__(self):
		Bimorph.__init__(self,"vfm_bimorph",14,8,"-MO-PSU-01:BM:V",0)
