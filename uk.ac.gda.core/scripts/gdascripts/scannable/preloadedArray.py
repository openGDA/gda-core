from gda.device.scannable import PseudoDevice
from org.eclipse.january.dataset import DatasetFactory


try:
	from gda.analysis import Plotter
except ImportError:
	Plotter = None

class PreloadedArray(PseudoDevice):
	"""Scannanle to hold a table of data, one column per extraField. The single
	input-field acts as an index to this table. The scannable's position will be
	the currently indexed row.
	"""
	def __init__(self, name, columnNameList, columnFormatList, returnFirstColumnInInputField=False):
		self.name = name
		if returnFirstColumnInInputField:
			self.inputNames = columnNameList[0:1]
			self.extraNames = columnNameList[1:]
			self.outputFormat = columnFormatList
		else:
			self.inputNames = ['idx']
			self.extraNames = columnNameList
			self.outputFormat = ['%i'] + list(columnFormatList)
		self.data = None
		self.index = 0
		self.columnNameList = columnNameList
		self.initialiseData()
		self.colwidth=15 # for printTable
		self.returnFirstColumnInInputField = returnFirstColumnInInputField
	
	def getAllNames(self):
		return list(self.getInputNames()) + list(self.getExtraNames())
	
	def initialiseData(self):
		self.data = {}
		for column in self.columnNameList:
			self.data[column] = []
	
	def setColumn(self, name, column):
		self.data[name]=column

	def getColumn(self, name):
		if name=='idx':
			return range(self.getLength())
		return self.data[name]

	def appendToColumn(self,name, value):
		self.data[name].append(value)
		
	def append(self, row):
		for columnName, val in zip(self.columnNameList, row):
			self.data[columnName].append(val)

	def getLength(self):
		return len(self.data[self.columnNameList[0]])

	def isBusy(self):
		return False

	def asynchronousMoveTo(self,index):
		self.index = int(index)

	def getPosition(self):
		if self.returnFirstColumnInInputField:
			result = []
		else:
			result = [self.index]
		for column in self.columnNameList:
			result.append(self.getColumn(column)[self.index])
		return result
	
	def plotAxisToDataVectorPlot(self, plotName, xColumnName, yColumnName):
		print "plotAxisToDataVectorPlot", plotName, xColumnName, yColumnName
		xdataset = DatasetFactory.createFromObject(self.getColumn(xColumnName))
		xdataset.setName(xColumnName)
		ydataset = DatasetFactory.createFromObject(self.getColumn(yColumnName))
		ydataset.setName(yColumnName)
		if plotName is not None:	
			Plotter.plot(plotName ,xdataset, [ydataset])
		
	def printTable(self):
		header = ""
		for name in self.getAllNames():
			header += name.ljust(self.colwidth)
		toPrint = header + '\n'
		toPrint +="-"*len(header) + '\n'	
		for i in range(self.getLength()):
			row = ""
			for fmt, name in zip(self.getOutputFormat(), self.getAllNames()):
				row += (fmt%self.getColumn(name)[i]).ljust(self.colwidth)
			toPrint += row + '\n'
		toPrint+= "-"*(len(header))	
		print toPrint