class TwodDataSetResult(object):
	def __init__(self, processorName, resultsDict, keyxlabel, keyylabel, report):
		self.processorName = processorName
		self.resultsDict = resultsDict
		self.report = report
		self.keyxlabel = keyxlabel
		self.keyylabel = keyylabel
		
	def __repr__(self):
		return self.processorName + ';' + `self.resultsDict` + ';' + self.report
	
	def __cmp__(self, other):
		return not (
				self.processorName == other.processorName and \
				self.resultsDict == other.resultsDict and \
				self.report == other.report and \
				self.keyxlabel == other.keyxlabel and \
				self.keyylabel == other.keyylabel
				)
