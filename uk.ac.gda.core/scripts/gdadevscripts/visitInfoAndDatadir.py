"""
Currently bespoke to I16
"""
from gda.configuration.properties import LocalProperties


import os

def whereisscan(number):
	"""
	Returns the path to a numbered data 
	"""
	filename = str(number) + ".dat"
	
	# Try regular data directories	
	f = os.popen('ls /dls/i16/data/2009/*/%s'%filename)
	path = f.readline()
	f.close()
	
	# If no luck try commisioning folders
	if path =='':
		f = os.popen('ls /dls/i16/data/2009/mt0/*/%s'%filename)
		path = f.readline()
		f.close()
	
	if path == '':
		print "File %s not found in /dls/i16/data/2009/" % filename
	return path.rstrip('\n')

def datadir(newpath=None):
	"""
	Reads or sets the data directory. May also need to configure some detectors.
	"""
	if newpath==None:
		return LocalProperties.getPath("gda.data.scan.datawriter.datadir",None)
	else:
		if not os.path.exists(newpath):
			raise ValueError("The directory %s does NOT exist!"%newpath)

		print "Setting gda.data.scan.datawriter.datadir preference..."
		LocalProperties.set("gda.data.scan.datawriter.datadir", newpath)
#		Save to localShelf
		return LocalProperties.getPath("gda.data.scan.datawriter.datadir",None)


### def setDatadirToSavedValue
###		load from jythonShelf
###		datadir(...)

def visit(visitname=None):
	"""
	Reads or sets the data directory based only on the visit name or run number
	"""
	ROOT = "/dls/i16/data/2009/"
	if visitname == None:
		try:
			s = datadir().split(ROOT)[1]
		except:
			raise Exception("The current datadir '%s' does not match the format of either a visit or commisioning run folder"%datadir())
		if s[:4]=='mt0/':
			s=s[4:]
		return s
	else:
		if visitname[:3] == 'run':
			visitname = "mt0/" + visitname
			
		print "Setting datadir to '%s':" % (ROOT+visitname,)
		datadir(ROOT+visitname)

def autosetdatadir():
	ROOT = "/dls/i16/data/2009/"
	# Check for visit number from iKitten
	f = os.popen('/dls_sw/dasc/bin/iKittenScripts/getCurrentVisit')
	visitid = f.readline()
	f.close()
	if visitid[:2]=='mt':
		visitid = visitid.split('\n')[0]
		print "Setting datadir to current visit: %s (based only on date and time)"%visitid
		datadir(ROOT+visitid)
	else:
		print "No visit in progress (based only on date and time)"
		print "* use 'visitinfo' for some clues!"


def visitinfo(visitid=None):
	ROOT = "/dls/i16/data/2009/"	
	# If no visitid specified, check with iKitten
	visitidSpecified = visitid
	if visitid == None:
		# Check for visit numbert from iKitten
		f = os.popen('/dls_sw/dasc/bin/iKittenScripts/getCurrentVisit')
		visitid = f.readline()
		f.close()
		if visitid[:2]=='mt':
			visitid = visitid.split('\n')[0]
			print "\nCurrent visit: %s (based only on date and time)"%visitid
			print "* Use 'autosetdatadir' to switch to this visit, or 'datadir folder' to choose manually\n"
		else:
			print "\nNo visit in progress (based only on date and time)"
			print "* use 'datadir folder' to switch to a commisioning folder or visit folder listed below.\n"
			visitid = None
		
	# if visitid was specified or found in iKitten display info about the visit
	if visitid:
		f = os.popen('/dls_sw/dasc/bin/iKittenScripts/getDetailsOfVisit ' + visitid.upper())
		line = f.readline()
		while line != '':
			if (line != '\n') and (line.find('rows selected')==-1):
				print line,
			line = f.readline()
		f.close()
		
	if not visitidSpecified:
		# Also display generic info
		print "\nVisit folders in %s:" %ROOT
		ls = os.listdir(ROOT)
		ls.sort()
		print "   " + str(ls)
		print "\nCommisioning folders in %s:" %( ROOT+"mt0",)
		ls = os.listdir(ROOT+"mt0")
		ls.sort()
		print "   " + str(ls)

