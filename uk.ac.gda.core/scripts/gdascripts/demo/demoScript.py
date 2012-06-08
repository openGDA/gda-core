#@PydevCodeAnalysisIgnore
#this script demonstrates scripting functionality
#syntax errors are found immediately
#namespace errors are found during the script (Jython limitation)

#basic movement
print "pos horizgapwidth01 25"
pos horizgapwidth01 25 
print "horizgapwidth01 position now: " + horizgapwidth01.pos()

#move using units
print "pos bragg '25250mdeg'"
pos bragg '25250mdeg'
print "bragg position now: "+bragg.pos()

#simultaneous movement
print "pos horizgapwidth01 15 vertgapwidth01 17"
pos horizgapwidth01 15 vertgapwidth01 17
print "horizgapwidth01 position now: "+horizgapwidth01.pos()
print "vertgapwidth01 now: "+vertgapwidth01.pos()

#This function allows the user to intervene and pause and resume the script at this point
ScriptBase.checkForPauses()

#list all oe's
print "finder.listAllNames(\"OE\")"
names = finder.listAllNames("OE")
print names

#list all detectors
print "finder.listAllNames(\"Detector\")"
names = finder.listAllNames("Detector")
print names

#list all detectors another way
print "DetectorBase.getAllDetectors()"
names = DetectorBase.getAllDetectors()
print names

#list all active detectors (active detectors will be used during a scan automatically)
print "DetectorBase.getActiveDetectors()"
detectors = DetectorBase.getActiveDetectors()
print detectors

#deactivate the detector
print "countertimer01.setActive(0)"
countertimer01.setActive(0)
print "DetectorBase.getActiveDetectors()"
detectors = DetectorBase.getActiveDetectors()
print detectors

#reactivate the detector
print "countertimer01.setActive(1)"
countertimer01.setActive(1)
print "DetectorBase.getActiveDetectors()"
detectors = DetectorBase.getActiveDetectors()
print detectors

#type of scans
#concurrent scan
print "scan horizgapwidth01 10 12 1 "
scan horizgapwidth01 10 12 1

#concurrent with two scannable objects
print "scan  horizgapwidth01 10 12 1 vertgapwidth01 10 12 1"
scan  horizgapwidth01 10 12 1 vertgapwidth01 10 12 1

#grid scan
print "gscan  horizgapwidth01 10 12 1 vertgapwidth01 10 12 1"
gscan  horizgapwidth01 10 12 1 vertgapwidth01 10 12 1

#centroid scan
print "cscan  horizgapwidth01 11 1 1"
cscan  horizgapwidth01 11 1 1

#explicitly include the detector
countertimer01.setActive(0)
print "scan horizgapwidth01 10 12 1 countertimer01"
scan horizgapwidth01 10 12 1 countertimer01

#other features:
#  error handling (syntax and namespace)
#  pause \ stop scans
#  queue scripting






