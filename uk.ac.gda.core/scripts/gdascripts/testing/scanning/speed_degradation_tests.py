"""
Test of the degradation of performance with multiple scans
Runs a scan with many points and records the time for each
"""
import java 
from time import time, strftime
import sys
import gda
from gda.analysis.io import SRSLoader
from gda.scan import ConcurrentScan

from gdascripts.analysis import plotData

from gdascripts.messages import handle_messages
from gdascripts.messages.handle_messages import simpleLog

def time_concurrent_scan(numberOfLoops, *args):
	return time_scan(numberOfLoops,ConcurrentScan, None, args[0])

def time_concurrent_scan_with_axes(numberOfLoops, scanPlotSettings, *args):
	return time_scan(numberOfLoops,ConcurrentScan, scanPlotSettings, args[0])

def time_scan(numberOfLoops, scan_constructor, scanPlotSettings, args):
	print "time_scan : ", `scan_constructor`,`args`
	times=[]
	timebase = time()
	filename = strftime("%Y-%m-%d_%H-%M-%S_time_scan.dat")
	file = open(filename,"w")
	try:
		loop=0;
		loop_count=0
		while(True):
			scan = scan_constructor(args)
			if scanPlotSettings != None:
				scan.setScanPlotSettings(scanPlotSettings)
			scan.runScan()
			loop += 1
			timenow = time()
			t = timenow - timebase
			timebase = timenow
			times.append(t)
			file.write( `loop` + "," + `t`)
			if numberOfLoops > 0:
				loop_count += 1
				if loop_count >= numberOfLoops:
					break
	except :
		type, exception, traceback = sys.exc_info()	
		toRaise = not isinstance(exception, java.lang.InterruptedException)
		handle_messages.log(None,"time_scan exception", type, exception, traceback, toRaise)
	file.close
	filename = strftime("%Y-%m-%d_%H-%M-%S_time_scan.dat_srs")
	if len(times)>0:
		xName="loop"
		yName="time"
		sfh = plotData.getScanFileHolderY(xName,yName,times)
		sfh.save( SRSLoader(filename))
		sfh.plot(xName, yName)
	return filename

def time_command(numberOfLoops, cmd):
	print "time_scan : ", `cmd`
	timebase = time()
	filename = strftime("%Y-%m-%d_%H-%M-%S_time_command.dat")
	file = open(filename,"w")
	try:
		loop=0;
		loop_count=0
		while(True):
			exec(cmd)
			loop += 1
			timenow = time()
			t = timenow - timebase
			timebase = timenow
			file.write( `loop` + "," + `t`)
			if numberOfLoops > 0:
				loop_count += 1
				if loop_count >= numberOfLoops:
					break
	except :
		type, exception, traceback = sys.exc_info()	
		toRaise = not isinstance(exception, java.lang.InterruptedException)
		if toRaise:
			file.close
		handle_messages.log(None,"time_command exception", type, exception, traceback, toRaise)
	file.close
#	filename = strftime("%Y-%m-%d_%H-%M-%S_time_command.dat_srs")
#	if len(times)>0:
#		xName="loop"
#		yName="time"
#		sfh = plotData.getScanFileHolderY(xName,yName,times)
#		sfh.save( SRSLoader(filename))
#		sfh.plot(xName, yName)
	return filename