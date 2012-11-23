from gdascripts.scan.process.ScanDataProcessor import ScanDataProcessor
class Tuner(object):
	
	def __init__(self, name, datasetProcessor, scanClass, *args):
		self.name = name
		self.processorName = datasetProcessor.name
		self.scan = scanClass([ScanDataProcessor([datasetProcessor])])
		self.args = args
		self.use_backlash_correction = False
		self.fraction_of_range_for_backlash = .5
		
		
		
	def tune(self):
		r = self.scan(*self.args)
		print r
		if self.use_backlash_correction:
			full_range = abs(self.args[2] - self.args[1])
			scn = self.args[0]
			peakpos = r.__dict__[self.processorName].scn[scn.name]
			print "Moving %s to %f to allow for backlash" % (scn.name, peakpos - (full_range * self.fraction_of_range_for_backlash))
			scn.moveTo(peakpos - (full_range * self.fraction_of_range_for_backlash))
		r.__dict__[self.processorName].go()

