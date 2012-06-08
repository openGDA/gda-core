import sys
from gda.scan import ConcurrentScan

from gdascripts.messages import handle_messages
from gdascripts.messages.handle_messages import simpleLog

def scan(*args):
	"""
	Moves several scannable objects simultaneously,	the same number of steps. 
	After each movement data is collected from items in the allDetectors vector.
	Expect arguments in the following format:
	scannable1 start stop step scannable2 [start] [[stop] step] scannable3 [start] [[stop] step]
	The number of steps is calculated from scannable1.
	For subsequent scannables: if only start then they are moved only to that position. If no start value given then
	the current position will be used (so this scannable will not be moved, but will be included in any output from the
	scan.)
	If a step value given then the scannable will be moved each time
	If a stop value is also given then this is treated as a nested scan containing one scannable. 
	This scan will be run in full at each node of the main scan. 
	If there are multiple nested scans then they are nested inside each other to
	create a multidimensional scan space (rasta scan).	
	"""
	print args
	try:
		ConcurrentScan(args[0]).runScan()
	except:
		type, exception, traceback = sys.exc_info()
		handle_messages.log(None, "scan failed", type, exception, traceback, True)
