from gdascripts.scan.process.ScanDataProcessor import ScanDataProcessor
class Tuner(object):
	
	def __init__(self, name, datasetProcessor, scanClass, *args):
		self.name = name
		self.processorName = datasetProcessor.name
		self.scan = scanClass([ScanDataProcessor([datasetProcessor])])
		self.args = args
		self.use_backlash_correction = False
		
	def tune(self):
		r = self.scan(*self.args)
		print r
		if self.use_backlash_correction:
			range = abs(self.args[2] - self.args[1])
			scn = self.args[0]
			peakpos = r.__dict__[self.processorName].scn[scn.name]
			print "Moving %s to %f to allow for backlash" % (scn.name, peakpos-range/2.)
			scn.moveTo(peakpos-range/2.)
		r.__dict__[self.processorName].go()

