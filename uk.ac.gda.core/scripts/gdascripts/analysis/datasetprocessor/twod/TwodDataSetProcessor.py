from TwodDataSetResult import TwodDataSetResult
from gdascripts.analysis.datasetprocessor.twod.TwodDataSetResult import TwodDataSetResult

# XXX This is extended by Eric's FlippingDevice from i06 and/or i07
class TwodDataSetProcessor(object):

	def _process(self, dataset,dsxaxis, dsyaxis):
		"""
		[result1, result2 ...] = _process(self, dataset,dsxaxis, dsyaxis)
		overide this to return the list of results in the same order as labelList"""
		raise RuntimeError("Not implemented")
###
	def __init__(self, name, labelList, keyxlabel, keyylabel, formatString):
		self.name = name
		self.labelList = labelList # must be a valid python variable label or list of these
		self.keyxlabel = keyxlabel
		self.keyylabel = keyylabel
		self.formatString = formatString # see getWordyResults()
		
	def process(self,dataset, xoffset=0, yoffset=0): #
		results = {}
		for (label, result) in zip(self.labelList, self._process(dataset, xoffset, yoffset)):
			results[label] = result
		return TwodDataSetResult(self.name, results, self.keyxlabel, self.keyylabel, self.__getWordyResults(results))
	
	def __getWordyResults(self,resultsDict ):
		orderedResults=[]
		for label in self.labelList:
			orderedResults.append(resultsDict[label])
		return self.formatString % tuple(orderedResults)


