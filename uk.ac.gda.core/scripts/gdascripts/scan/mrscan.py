from gda.scan import ScanPositionProviderFactory
from gdascripts.scan.concurrentScanWrapper import ConcurrentScanWrapper

class MultiRegionalScanClass(ConcurrentScanWrapper):
	'''
	USAGE:
	mrscan motor (R1, R2, ... Rx) ... for multiple-region scan

	Where the region Rx is defined by a [start, stop, step] list and all regions are grouped in a tuple.
	Allows to scan over while changing step sizes for the scannable.

	e.g.: mrscan x ([0, 5, 1], [6, 10, 0.1], [10, 15, 1]) y 0.1
	'''

	def __init__(self, scan_listeners = None):
		ConcurrentScanWrapper.__init__(self, False, False, scan_listeners)

	def __call__(self, *args):
		ConcurrentScanWrapper.__call__(self, *args);

	#Try to convert tuples containing lists of numbers to points
	#e.g
	#[[x ([0, 5, 1], [6,7,0.1])]
	#to
	#[[x, (0, 1.0, 2.0, 3.0, 4.0, 5.0, 6, 6.1, 6.2, 6.3, 6.4, 6.5, 6.6, 6.7, 6.8, 6.9, 7)]]
	def convertArgStruct(self, arg_struct):
		new_args = []
		for i in arg_struct:
			arg = []
			for j in i:
				if self.isRegionTuple(j):
					scan_region = ScanPositionProviderFactory.createFromRegion(self.regionTupleToList(j))
					arg.append(scan_region)
				else:
					arg.append(j)

			new_args.append(arg)

		return new_args

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
				raise ValueError(str(stp) + " must contain numbers or a list")
		return result;

	#True if rp is (X, [Y,Y,Y], ... [Z,Z,Z])
	def isRegionTuple(self, rp):
		result = True;
		if not isinstance(rp, tuple):
			return False
		for stp in rp:
			if isinstance(stp, int):
				continue;
			if isinstance(stp, float):
				continue;
			if isinstance(stp, list) and len(stp) != 3:
				result=False;
				break;

		return result;
