#@PydevCodeAnalysisIgnore
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
#		if len(new_position) != self.numOfChans :
#			raise "len(new_position) != "+`self.numOfChans`
		self.IAmBusy=True
		try:
			for i in range(self.startChan,self.startChan+self.numOfChans):
				self.beamline.setValue(None,"Top",self.pvPrefix+`i`+"D",new_position[i-self.startChan], 20)
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

#	def generatePosition(self, current_position, new_position, max_diff):
#		position=[]
#		##calculate new position which is on way to new_position
#		return position
		
#	def generatePositions(self, current_position, new_position, max_diff):
#		positions = []
#		position = current_position
#		while True:
#			position = self.generatePosition(position, new_position, max_diff)
#			positions.append(position)
#			if position.equals(new_position):
#				break
#		return positions
	
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
			target.append(self.__getChannelTarget(i))
		output = []
		for i in self.channelIndexes:
			output.append(self.__readOutput(i))
		return target + output
	
	def rawAsynchronousMoveTo(self, targetArray):
		"""Blocks until move is under way
		"""
		if self.isBusy():
			raise Exception("Cannot move %s's to %s as it is busy" % (self.getName(), `targetArray`))
		self.__checkTargetSafe(targetArray)
		for (i, target) in zip (self.channelIndexes, targetArray):
			time.sleep(1)
			self.__setChannelTarget(i, target)
		self.__waitForTargetSet(targetArray);
		time.sleep(5)
		self.__applyTargetProfile()
		time.sleep(15)
		
	def rawIsBusy(self):
		for i in self.channelIndexes:
			if self.__isTargetStatusBusy(i): return True
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
			if float(target) != self.__getChannelTarget(i):
				return False
		return True
	
	def __readOutput(self, i):
		return float(self.pvs['GET-VOUT%02i'%i].caget())
	
	def __setChannelTarget(self, i, target):
		self.pvs['SET-VTRGT%02i' % i].caput(target)
		
	def __getChannelTarget(self, i):
		return float(self.pvs['GET-VTRGT%02i'%i].caget())
	
	def __applyTargetProfile(self):
		self.pvs['SET-ALLTRGT'].caput(1)
		
	def __isTargetStatusBusy(self, i):
		return int(self.pvs['GET-STATUS%02i'%i].caget())!=1


class Bimorph_HFM(Bimorph):
	def __init__(self):
		Bimorph.__init__(self,"hfm_bimorph",0,14,"-MO-PSU-01:BM:V",0)


class Bimorph_VFM(Bimorph):
	def __init__(self):
		Bimorph.__init__(self,"vfm_bimorph",14,8,"-MO-PSU-01:BM:V",0)
	
