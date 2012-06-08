# To use this module (from LocalStation.py for example):
# >>> from gdascripts.scan.installStandardScansWithProcessing import * #@UnusedWildImport
# >>> scan_processor.rootNamespaceDict=globals()
#
# If you want your scan command to be added to SRS metadata, also add: 
# >>> gdascripts.scan.concurrentScanWrapper.ROOT_NAMESPACE_DICT = globals() 
# 
# To modify the processor called at the end of each scan, modify scan_processor.

from gdascripts.analysis.datasetprocessor.oned.MaxPositionAndValue import MaxPositionAndValue
from gdascripts.analysis.datasetprocessor.oned.MinPositionAndValue import MinPositionAndValue
from gdascripts.analysis.datasetprocessor.oned.CentreOfMass import CentreOfMass
from gdascripts.analysis.datasetprocessor.oned.GaussianPeakAndBackground import  GaussianPeakAndBackground
from gdascripts.scan.process.ScanDataProcessor import ScanDataProcessor
from gdascripts.scan import specscans
from gdascripts.scan import gdascans

from gda.jython.commands.GeneralCommands import alias

print "Setting up scan data processor, scan_processor"
scan_processor = ScanDataProcessor( [MaxPositionAndValue(),MinPositionAndValue(),CentreOfMass(), GaussianPeakAndBackground() ], globals() )
go = scan_processor.go
alias("go")

print "Creating spec-like commands:"
ascan  = specscans.Ascan([scan_processor])
a2scan = specscans.A2scan([scan_processor])
a3scan = specscans.A3scan([scan_processor])
mesh   = specscans.Mesh([scan_processor])
dscan  = specscans.Dscan([scan_processor])
d2scan = specscans.D2scan([scan_processor])
d3scan = specscans.D3scan([scan_processor])
alias('ascan');print ascan.__doc__.split('\n')[3]
alias('a2scan');print a2scan.__doc__.split('\n')[3]
alias('a3scan');print a3scan.__doc__.split('\n')[3]
alias('mesh');print mesh.__doc__.split('\n')[3]
alias('dscan');print dscan.__doc__.split('\n')[3]
alias('d2scan');print d2scan.__doc__.split('\n')[3]
alias('d3scan');print d3scan.__doc__.split('\n')[3]


print "Creating gda scan commands:"
scan=gdascans.Scan([scan_processor])
rscan=gdascans.Rscan([scan_processor])
cscan=gdascans.Cscan([scan_processor])
alias('scan');print scan.__doc__.split('\n')[2]
alias('rscan');print rscan.__doc__.split('\n')[2]
alias('cscan');print cscan.__doc__.split('\n')[2]

for s in ascan, a2scan, a3scan, mesh, dscan, d2scan, d3scan, scan, rscan:
	s.dataVectorPlotNameForSecondaryScans =  "Secondary Plot"
