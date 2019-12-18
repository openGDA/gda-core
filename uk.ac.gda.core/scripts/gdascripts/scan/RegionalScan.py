from gda.scan import ScanPositionProviderFactory
from gdascripts.scan.BasicScan import BasicScanClass;

class RegionalScanClass(BasicScanClass):
	'''Use mrscan motor (R1, R2, ... R3) for multiple-region scan
	where the region Rx is defined by a [start, stop, step] list and all regions are grouped in a tuple
	For example:
		mrscan testMotor1 ([0, 5, 1], [6,10,0.1], [10,15,1]) dummyCounter1 0.1'''

	def __init__(self):
		BasicScanClass.__init__(self);
		self.scanType='mrscan';
		
	def __call__(self, *args):
		BasicScanClass.__call__(self, *args);
	

#	def parseArgs(self, args):
	def parseArgs(self, devices, parameters):
		""" To change the sections with range and steps into a single individual position list
		eg: [[x, (1, 2, 3, 3.5, 4, 4.5, 5), z] = parseArgs([x, (R1, R2, Ri, ...), z])
		where Ri=[start, stop, step]
		"""
		newArgs=[]
		for k, v in zip(devices, parameters):
			newArgs.append(k);
						
			for r in v:
				if self.isRegionTuple(r):
#					newScanRegion = ScanPositionProviderFactory.createFromRegion(list(r))
					newScanRegion = ScanPositionProviderFactory.createFromRegion( self.regionTupleToList(r) )
					newArgs.append(newScanRegion)
				else:
					newArgs.append(r);
		
		return newArgs;

	#Try to convert a tuple with the single number inside to a full list:
	# (x, [y0,y1,y2], ... [Z,Z,Z]) to [ [x,x,1], [y0,y1,y2], ... [Z,Z,Z] ]
	def regionTupleToList(self, rp):
		result = [];

		for stp in rp:
			if isinstance(stp, int) or isinstance(stp, float):
				result.append([stp, stp, 1]);
			elif isinstance(stp, list):
				result.append(stp);
			else:
				raise "Error on the regional tuple"

		return result;
		
		
	#True if rp is (X, [Y,Y,Y], ... [Z,Z,Z])
	def isRegionTuple(self, rp):
		result = True;

		if not isinstance(rp, tuple):
			result = False;
		else:
			for stp in rp:
				if isinstance(stp, int):
					continue;
				if isinstance(stp, float):
					continue;
				if isinstance(stp, list):
					if len(stp) != 3:
						result=False;
						break;
					
		return result;



#Usage
#rscan=RegionalScanClass()
#alias('rscan');

#Usage:
# For one region, not the comma at the end of tuple:
#rscan testMotor1 ([0, 5, 1],) dummyCounter1 0.1
#For multiple regions:
#rscan testMotor1 ([0, 5, 1], [6,10,0.1], [10,15,1]) dummyCounter1 0.1
