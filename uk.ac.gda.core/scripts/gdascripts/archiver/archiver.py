from gda.factory import Finder

archiver = Finder.getInstance().find("archiver")

def archive(pvName, dateString):
	try:
		value = archiver.getValueForPv(pvName, dateString)
		print "Value of", pvName, "at", dateString, "was", value;
		return value
	except:
		print "PV could not be found in the archive (or archiver is not available)"