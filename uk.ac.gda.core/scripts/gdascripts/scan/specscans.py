from gdascripts.scan.concurrentScanWrapper import ConcurrentScanWrapper
from copy import copy
#ascan motor start finish intervals time
#a2scan m1 s1 f1 m2 s2 f2 intervals time
#a3scan m1 s1 f1 m2 s2 f2 m3 s3 f3 intervals time
#mesh m1 s1 f1 intervals1 m2 s2 f2 intervals2 time
#lup motor start finish intervals time
#dscan motor start finish intervals time
#d2scan m1 s1 f1 m2 s2 f2 intervals time
#d3scan m1 s1 f1 m2 s2 f2 m3 s3 f3 intervals time
#th2th tth_start_rel tth_finish_rel intervals time

#regular


class SpecScan(ConcurrentScanWrapper):
	"""SpecScan
	"""
	def __init__(self, scanListeners = None):
		ConcurrentScanWrapper.__init__(self, False, False, scanListeners)
	
	def appendRemainingArgs(self, result, argStruct):
		self.checkArgStructForLoops(argStruct)
		if len(argStruct)>0:
			result += argStruct
		return result
	
	def checkArgStructForLoops(self, argStruct):
		for currentList in argStruct:
			if len(currentList) > 2:
				raise Exception(self.__doc__)


class Ascan(SpecScan):
	"""
USAGE:
	
  ascan motor start stop intervals [scn [pos]] ...
"""
	def convertArgStruct(self, argStruct):
		"""ascan motor start stop intervals [scn [pos]] ..."""
		motor, start, stop, intervals = argStruct.pop(0)
		result = [ [motor, start, stop, (float(stop-start)/intervals)] ]
		return self.appendRemainingArgs(result, argStruct)
	
class A2scan(SpecScan):
	"""
USAGE:
	
  a2scan m1 s1 f1 m2 s2 f2 intervals [scn [pos]] ...
"""
	def convertArgStruct(self, argStruct):
		"""ascan motor start stop intervals [scn [pos]] ..."""
		m1, s1, f1 = argStruct.pop(0)
		m2, s2, f2, intervals = argStruct.pop(0)
		result = [ [m1, s1, f1, (float(f1-s1)/intervals)], [m2, s2, f2, (float(f2-s2)/intervals)] ]
		return self.appendRemainingArgs(result, argStruct)
	
class A3scan(SpecScan):
	"""
USAGE:
	
  a3scan m1 s1 f1 m2 s2 f2 m3 s3 f3 intervals [scn [pos]] ...
"""
	def convertArgStruct(self, argStruct):
		"""ascan motor start stop intervals [scn [pos]] ..."""
		m1, s1, f1 = argStruct.pop(0)
		m2, s2, f2 = argStruct.pop(0)		
		m3, s3, f3, intervals = argStruct.pop(0)
		result = [ [m1, s1, f1, (float(f1-s1)/intervals)], [m2, s2, f2, (float(f2-s2)/intervals)], [m3, s3, f3, (float(f3-s3)/intervals)] ]
		return self.appendRemainingArgs(result, argStruct)
	
class Mesh(SpecScan):
	"""
USAGE:
	
  mesh m1 s1 f1 intervals1 m2 s2 f2 intervals2 time [scn [pos]] ...
"""
	def convertArgStruct(self, argStruct):
		#mesh m1 s1 f1 intervals1 m2 s2 f2 intervals2 time ...
		m1, s1, f1, intervals1 = argStruct.pop(0)
		m2, s2, f2, intervals2 = argStruct.pop(0)		
		result = [ [m1, s1, f1, (float(f1-s1)/intervals1)], [m2, s2, f2, (float(f2-s2)/intervals2)] ]
		return self.appendRemainingArgs(result, argStruct)
		

class Dscan(Ascan):
	"""
USAGE:
	
  dscan motor start stop intervals [scn [pos]] ...
"""
	def __init__(self, scanListeners = None):
		ConcurrentScanWrapper.__init__(self, True, True, scanListeners)


class D2scan(A2scan):
	"""
USAGE:
	
  d2scan m1 s1 f1 m2 s2 f2 intervals [scn [pos]] ...
"""
	def __init__(self, scanListeners = None):
		ConcurrentScanWrapper.__init__(self, True, True, scanListeners)


class D3scan(A3scan):
	"""
USAGE:
	
  d3scan m1 s1 f1 m2 s2 f2 m3 s3 f3 intervals [scn [pos]] ...
"""
	def __init__(self, scanListeners = None):
		ConcurrentScanWrapper.__init__(self, True, True, scanListeners)

