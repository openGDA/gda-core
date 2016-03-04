from gdascripts.scannable.detector.ProcessingDetectorWrapper import ProcessingDetectorWrapper, BodgedProcessingDetectorWrapper
from gdascripts.scannable.detector.DetectorDataProcessor import DetectorDataProcessor, DetectorDataProcessorWithRoi
from gdascripts.analysis.datasetprocessor.twod.TwodGaussianPeak import TwodGaussianPeak
from gdascripts.analysis.datasetprocessor.twod.SumMaxPositionAndValue import SumMaxPositionAndValue
from gdascripts.scannable.detector.dummy.focused_beam_dataset import CreateImageReadingDummyDetector

f = DummyPD("f")

pildet = CreateImageReadingDummyDetector.create(f)
pil = BodgedProcessingDetectorWrapper('pil', pildet, [], panel_name='Data Vector')

peak2d = DetectorDataProcessorWithRoi('peak2d', pil, [TwodGaussianPeak()])
max2d = DetectorDataProcessorWithRoi('max2d', pil, [SumMaxPositionAndValue()])

pos f 430
