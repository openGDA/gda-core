from gdascripts.analysis.datasetprocessor.oned.MaxPositionAndValue import MaxPositionAndValue
from gdascripts.analysis.datasetprocessor.oned.MinPositionAndValue import MinPositionAndValue
from gdascripts.scan.process.ScanDataProcessor import ScanDataProcessor

scanprocessor = ScanDataProcessor( [MaxPositionAndValue(),MinPositionAndValue()], globals() )
exec("scanp = Scan([scanprocessor])")
alias("scanp") #@UndefinedVariable
alias("go") #@UndefinedVariable