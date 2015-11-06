from XYDataSetResult import XYDataSetResult
import java.lang #@UnresolvedImport
#from gdascripts.analysis.datasetprocessor.oned import XYDataSetProcessor

class XYDataSetProcessor(object):

	def _process(self, xDataSet, yDataSet):
		"""
		[result1, result2 ...] = _process(self, xDataSet, yDataSet)
		overide this to return the list of results in the same order as labelList"""
		raise RuntimeError("Not implemented")
###
	def __init__(self, name, labelList, keyxlabel, formatString):
		self.name = name
		self.labelList = labelList # must be a valid python variable label or list of these
		self.formatString = formatString # see getWordyResults()
		self.lastResults = None
		self.keyxlabel = keyxlabel
		if keyxlabel not in labelList:
			raise ValueError('key x label not in label list')
		self.raise_process_exceptions = False
		
	def process(self, xDataSet, yDataSet):
		results = {}
		try:
			answer = self._process(xDataSet, yDataSet)
		except (Exception, java.lang.Exception, java.lang.Error), e:
			for label in self.labelList:
				results[label] = None
			if self.raise_process_exceptions:
				raise e
			return XYDataSetResult(self.name, results, self.labelList, self.keyxlabel, "Exception: %s" % `e`)
		if type(answer[0])==type(tuple()):
			xydatasetResults=[]
			for each in answer:
				for (label, result) in zip(self.labelList, each):
					results[label] = result
				xydatasetResults.append(XYDataSetResult(self.name, results, self.labelList, self.keyxlabel, self.__getWordyResults(results)))
			return xydatasetResults
		else:
			for (label, result) in zip(self.labelList, answer):
				results[label] = result
			return XYDataSetResult(self.name, results, self.labelList, self.keyxlabel, self.__getWordyResults(results))
	
	def __getWordyResults(self, resultsDict):
		orderedResults = []
		for label in self.labelList:
			orderedResults.append(resultsDict[label])
		try:
			return self.formatString % tuple(orderedResults)
		except TypeError, e:
			return "In %s XYDataSetProcessor: wrong format (%s) for results (%s)." % (self.name, self.formatString, `tuple(orderedResults)`)

class XYDataSetFunction(XYDataSetProcessor):
	"""For processing DataVectors to floats"""
	def _process(self, xDataSet, yDataSet):
		"""returns a float/double"""
		raise RuntimeError("Not implemented")


class XYDataSetMap(XYDataSetProcessor):
	def _process(self, xDataSet, yDataSet):
		"""returns another DataVector"""
		raise RuntimeError("Not implemented")
