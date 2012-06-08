from gdascripts.scan.concurrentScanWrapper import ConcurrentScanWrapper
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


def makelist(number_or_list):
	try:
		return list(number_or_list)
	except TypeError:
		return [number_or_list]

def scale(number_or_list, scaler):
	ml = [scaler * e for e in makelist(number_or_list)]
	return ml if len(ml) > 1 else ml[0]


# The standard gda scan
class Scan(ConcurrentScanWrapper):
	"""USAGE:
	
  scan scn1 start stop step [scnN [start [stop [step]]]] ...

  e.g.: scan x 1 2 1 z                  --> loop x, for each step read value/position of z 
        scan x 1 2 1 z 1                --> loop x, for each step expose/move z for/to 1 
        scan x 1 2 1 y 10 11 1 z 1      --> loop x, for each x loop y, for each (x,y) step expose/move z for/to 1
        scan x 1 2 1 y 10 11 z 1        --> loop x and y together (no ystop required), for each step expose/move z for/to 1
        scan x 1 2 1 y 10 11 1 z 1 a b  --> combine any of the above!
        
        Use scan.yaxis = 'axis_name' to determine which axis will be analysed and plotted by default.
        
  See also level."""
	def __init__(self, scanListeners = None):
		ConcurrentScanWrapper.__init__(self, False, False, scanListeners)
		
	def convertArgStruct(self, argStruct):
		return argStruct
	
	
class Rscan(ConcurrentScanWrapper):
	"""USAGE:
	
  rscan scn1 relstart relstop step [scnN relstart relstop step]... [scnN relstart step] [scnN [absolute_pos]]...

  As scan except positions of scannables to be scanned (but not simply moved) are relative. The former will be
  reurned to their initial positions.
  
  e.g.: rscan x -1 2 1 z                    --> loop x, for each step read value/position of z 
        rscan x -1 2 1 z 1000               --> loop x, for each step expose/move z for/to 1 
        rscan x -1 2 1 y -10 11 1 z 1000    --> loop x, for each x loop y, for each (x,y) step expose/move z for/to 1
        rscan x -1 2 1 y -1 1 z 1000        --> loop x and y together (no ystop required), for each step expose/move z for/to 1
        rscan x -1 2 1 y -2 2 1 z 1000 a b  --> combine any of the above!
        
  See also scan."""
	def __init__(self, scanListeners = None):
		ConcurrentScanWrapper.__init__(self, True, True, scanListeners)
		
	def convertArgStruct(self, argStruct):
		return argStruct

	
class Cscan(ConcurrentScanWrapper):
	"""USAGE:
	
  cscan scn1 halfwidth step [scnN halfwidth step]... [scnN [absolute_pos]]...

  Performs a centroid scan from current-halfwidth to current+halfwidth, and returns to original position.
  
  e.g.: cscan x 5 1 z                       --> if starting at 10, loops x from -5 to 15 and reads z for each step  
        cscan x 5 1 y 2 1 z                 --> if starting at 10, loops x from -5 to 15, at each step similarly scans y reading z for each step  
        
  See also scan."""
	def __init__(self, scanListeners = None):
		ConcurrentScanWrapper.__init__(self, True, True, scanListeners)
		
	def convertArgStruct(self, argStruct):
		result = []
		while len(argStruct) > 0:
			group = argStruct.pop(0)
			if len(group) != 3:
				result.append(group)
				break
			scn, halfwidth, step = group
			neg_halfwidth = scale(halfwidth, -1)

			result.append([scn, neg_halfwidth, halfwidth, step])
		
		while len(argStruct) > 0:
			result.append(argStruct.pop(0))
		
		return result

#NOTE: This is an i16 scan and is subject to change
class Scancn(ConcurrentScanWrapper):
	"""
USAGE:
	
  scancn scn stepsize numpoints [scnN [start [stop [step]]]] ...
  
  Performs a scan with specified stepsize and numpoints centred on the current scn position, and returns to original position.

  Use scan.yaxis = 'axis_name' to determine which axis will be analysed and plotted by default.

  See also scan.
"""
	def __init__(self, scanListeners = None):
		ConcurrentScanWrapper.__init__(self, True, True, scanListeners)
		
	def convertArgStruct(self, argStruct): 
		motor, stepsize, numpoints = argStruct.pop(0)

		intervals = float(numpoints - 1)
		halfwidth = scale(stepsize, intervals/2.)
		neg_halfwidth = scale(stepsize, -intervals/2.)
		
		result = [ [motor, neg_halfwidth, halfwidth, stepsize] ]
		while len(argStruct) > 0:
			result.append(argStruct.pop(0))
		return result
