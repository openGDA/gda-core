class XYDataSetResult(object):
	def __init__(self, processorName, resultsDict, labelList, keyxlabel, report):
		self.processorName = processorName
		self.resultsDict = resultsDict
		self.labelList = labelList
		self.report = report
		self.keyxlabel = keyxlabel
		
	def __repr__(self):
		return self.processorName + ';' + `self.resultsDict` + ';' + self.report
	
	def __cmp__(self, other):
		return not (
				self.processorName == other.processorName and \
				self.resultsDict == other.resultsDict and \
				self.labelList == other.labelList and \
				self.report == other.report and \
				self.keyxlabel == other.keyxlabel
				)
