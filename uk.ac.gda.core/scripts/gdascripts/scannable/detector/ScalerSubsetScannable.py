from gda.device.scannable import PseudoDevice
from gda.epics import CAClient


class ScalerSubsetScannable(PseudoDevice):
	"""The class holds a scaler wrapping a scalar card. It will return 
	the counts from a number of channels given in channelList.
	"""
	def __init__(self, name, scaler, channelList, nameList=None):
		self.name = name
		self.scaler=scaler
		self.channelList = channelList
		if nameList==None:
			nameList=[]
			for i in range(len(channelList)):
				nameList += ["ch" + str(channelList[i])]
		self.setInputNames([nameList[0]])
		if len(nameList)>1:
			self.setExtraNames(nameList[1:])
		self.setOutputFormat(['%.0f']*len(channelList))
		self.setLevel(9)

	def isBusy(self):
		return self.scaler.isBusy()

	def getPosition(self):
		allChannels = self.scaler.getPosition()
		# If one channel return just a single number...
		if len(self.channelList)==1:
			return self.scaler.getPosition()[self.channelList[0]-1]
		# ... otherwise return a list
		else:
			toReturn=[] 
			# Add the desired channels
			for channel in self.channelList:
				toReturn += [ float(allChannels[channel-1]) ]     
			return toReturn

	def asynchronousMoveTo(self,countTime):
		if self.scaler.isBusy():
			raise Exception, "Scaler already counting: << When reading multiple scaler channels, provide a count-time for only one. >>"
		else:
			self.scaler.asynchronousMoveTo(float(countTime))
			


#struckRootPv = "BL07I-EA-DET-01:"
def assignStruckChannel(channelNo, nameList, namespace):
	allNames = ''
	for name in nameList:
		namespace[name] = ScalerSubsetScannable(name,globals()['struck1'],[channelNo])
		allNames += name + '/'
	allNames = allNames[:-1]

	print "ch%i: %s" % (channelNo, allNames)
	cac = CAClient(ScalerSubsetScannable.struckRootPv+'SCALER.NM%i' % channelNo)
	cac.configure()
	cac.caput(allNames)
	cac.clearup()

	
ScalerSubsetScannable.struckRootPv = "<PVROOT:>"
#print "ch   gda-name"
#assignStruckChannel(1, ['ct1','cttime'], globals())
#assignStruckChannel(2, ['ct2', 'cyber'], globals())
#assignStruckChannel(3, ['ct3'], globals())
#assignStruckChannel(4, ['ct4'], globals())
#assignStruckChannel(5, ['ct5'], globals())
#assignStruckChannel(6, ['ct6'], globals())
#assignStruckChannel(7, ['ct7'], globals())
#assignStruckChannel(8, ['ct8'], globals())