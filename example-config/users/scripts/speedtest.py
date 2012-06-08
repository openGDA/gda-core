'''
create scan to get pipeline
pass to ScanDataPointProvider to send through lots of scan data points
'''
from gda.configuration.properties import LocalProperties
from gda.scan import ConcurrentScan, ScanDataPointProvider, ScanDataPointPublisher
from gda.data.scan.datawriter import DefaultDataWriterFactory
myscan = ConcurrentScan([scannableGaussian0, -2.0, 2.0, 0.1,])
currentFormat = LocalProperties.get("gda.data.scan.datawriter.dataFormat")
LocalProperties.set("gda.data.scan.datawriter.dataFormat","NexusDataWriter")
datawriter=DefaultDataWriterFactory.createDataWriterFromFactory()
publisher = ScanDataPointPublisher(datawriter, myscan)
pipeline=BasicScanDataPointPipeline(publisher)
sdp = ScanDataPointProvider()
sdp.preparePoints(5000)
#sdp.publishPoints(pipeline)
