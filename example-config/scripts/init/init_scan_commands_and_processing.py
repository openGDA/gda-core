from gdascripts.analysis.datasetprocessor.oned.MaxPositionAndValue import MaxPositionAndValue
from gdascripts.analysis.datasetprocessor.oned.MinPositionAndValue import MinPositionAndValue
from gdascripts.analysis.datasetprocessor.oned.CentreOfMass import CentreOfMass
from gdascripts.analysis.datasetprocessor.oned.GaussianPeakAndBackground import  GaussianPeakAndBackground
from gdascripts.scan.process.ScanDataProcessor import ScanDataProcessor
from gdascripts.scan.process.ScanDataProcessorResult import go
from gdascripts.scan import specscans
from gdascripts.scan import gdascans

from gda.jython.commands.GeneralCommands import alias

print "Setting up scan data processor, scan_processor"
scan_processor = ScanDataProcessor( [MaxPositionAndValue(),MinPositionAndValue(),CentreOfMass(), GaussianPeakAndBackground() ], globals() )
alias("go")

print "Creating scan commands:"
ascan  = specscans.Ascan([scan_processor])
a2scan = specscans.A2scan([scan_processor])
a3scan = specscans.A3scan([scan_processor])
mesh   = specscans.Mesh([scan_processor])
dscan  = specscans.Dscan([scan_processor])
d2scan = specscans.D2scan([scan_processor])
d3scan = specscans.D3scan([scan_processor])
scan=gdascans.Scan([scan_processor])
rscan=gdascans.Rscan([scan_processor])
alias('ascan');    print ascan.__doc__.split('\nUSAGE:\n\t\n  ')[1]
alias('a2scan');print a2scan.__doc__.split('\nUSAGE:\n\t\n  ')[1]
alias('a3scan');print a3scan.__doc__.split('\nUSAGE:\n\t\n  ')[1]
alias('mesh');print mesh.__doc__.split('\nUSAGE:\n\t\n  ')[1]
alias('dscan');print dscan.__doc__.split('\nUSAGE:\n\t\n  ')[1]
alias('d2scan');print d2scan.__doc__.split('\nUSAGE:\n\t\n  ')[1]
alias('d3scan');print d3scan.__doc__.split('\nUSAGE:\n\t\n  ')[1]
alias('scan');#print scan.__doc__.split('\nUSAGE:\n\t\n  ')[1]
alias('rscan')
