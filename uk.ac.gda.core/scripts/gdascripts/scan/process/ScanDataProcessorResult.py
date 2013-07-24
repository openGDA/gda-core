def determineScannableContainingField(targetFieldname, scannables):
	for scn in scannables:
		fieldnames = list(scn.getInputNames()) + list(scn.getExtraNames())
		if targetFieldname in fieldnames:
			return scn	
	abbrevtarget = ".".join(targetFieldname.split(".")[1:])
	for scn in scannables:
		fieldnames = list(scn.getInputNames()) + list(scn.getExtraNames())
		if abbrevtarget in fieldnames:
			return scn
	raise KeyError("targetFieldname %s not found in scannables: %s" % (targetFieldname, [scn.getName() for scn in scannables]))


class ScanDataProcessorResult(object):
	"""When viewed as a string this returns nice wordy results, otherwise it
	is a structure of the from:
	processor.abscissa = 'x'
	processor.ordinate = 'e1'
	processor.scn.x = 1
	processor.scn.mie: i1, e1 = 3, 4
	processor.field.x = 1
	processor.field.i1 = 3
	processor.field.e1 = 4
	processor.result.fwhm = 2
	processor.result.peak = 1
	
	"""	
	def __init__(self, dataSetResult, scanFileHolder, allscannables, xfieldname, yfieldname):
		self.name = dataSetResult.processorName
		self.labelValuePairs = dataSetResult.resultsDict
		self.datasetProcessorReport = dataSetResult.report
		xvalue = self.labelValuePairs[dataSetResult.keyxlabel]
		
		class Struct(object):
			
			def __init__(self):
				self.attrnames = []
				
			def addAttribute(self, attrname, value):
				self.__dict__[attrname]=value
				self.attrnames.append(attrname)
	
			def __repr__(self):
				result = ''
				for attrname in self.attrnames:
					result += attrname + ' = ' + `self.__dict__[attrname]` + '\n'
				return result
			
			def __str__(self):
				return self.__repr__()
			
			def __getitem__(self, key):
				return self.__dict__[key]
			
		self.scn = Struct()
		self.field = Struct()
		self.result = Struct()
		
		
		# Give up here if there was a problem with the processor
		for val in self.labelValuePairs.values():
			if val is None:
				self.scannableValues = None
				self.report = self.datasetProcessorReport
				self.str = self.datasetProcessorReport
				return
		
		# generate short report and determine scannable values at feature
		self.scannableValues = self.determineScannableValuesAtFeature(allscannables, scanFileHolder, xfieldname, xvalue)
		self.str = '' # for __str__ and __repr__
		
		# abscissa and ordinate fields:
		self.abscissa = xfieldname
		self.ordinate = yfieldname
		self.str += '   ' + self.name + ".abscissa = '%s'\n" %  self.abscissa
		self.str += '   ' + self.name + ".ordinate = '%s'\n" %  self.ordinate		
		
		# abscissa and ordinate scannables:		
		self.abscissa_scannable = determineScannableContainingField(xfieldname, allscannables)
		self.ordinate_scannable = determineScannableContainingField(yfieldname, allscannables)
		
		# feature location by scannable:

		for scn, value in self.scannableValues.items():
			self.scn.addAttribute(scn.getName(), value)
		for scn in allscannables:
			try:
				self.str += '   ' + self.name + ".scn." + self.reportScannablePositionAtFeature(scn) + '\n'
			except:
				pass
		
		# feature location by field
		for scn, value in self.scannableValues.items():
			fieldnames = list(scn.getInputNames()) + list(scn.getExtraNames())
			
			scnpos = self.getScannableValueAtFeature(scn)
			try:
				scnpos = list(scnpos)
			except TypeError: # not a list
				scnpos = [scnpos]

			for fieldname, pos, format in zip(fieldnames, scnpos, scn.getOutputFormat()):
				self.field.addAttribute(fieldname, pos)
				formattedpos = 'None' if pos == None else format%pos
				self.str += '   ' + self.name + ".field." + fieldname + " = " + formattedpos + '\n'
		
		# results
		for label in dataSetResult.labelList:
			val = dataSetResult.resultsDict[label]
			self.result.addAttribute(label, val)
			self.str += '   ' + self.name + ".result." + label + " = " + str(val) + '\n'
		
		self.report = self.generateShortReport(dataSetResult)
		
	def generateShortReport(self, dataSetResult):
		# input names only on purpose!
		scn = self.abscissa_scannable
		if len(scn.getInputNames())==1:
			scnlabel = scn.getName()
			pos = self.scannableValues[scn]
		
		elif len(scn.getInputNames())>1:
			scnlabel = scn.getName() + "("
			pos="("
			for fieldname, fieldval in zip(scn.getInputNames(), self.scannableValues[scn]):
				scnlabel += fieldname + ","
				pos += str(fieldval) + ","
			scnlabel = scnlabel[:-1] + ")"
			pos = pos[:-1] + ")"
		valuesString = ""
		for label, value in self.labelValuePairs.items():
			if label != dataSetResult.keyxlabel:
				valuesString += label + " = " + str(value) + ", "
		valuesString = valuesString[:-2]
		return "At %s = %s (%s), %s %s. " % (scnlabel, pos, dataSetResult.keyxlabel, self.ordinate, valuesString) 
	
	def reportScannablePositionAtFeature(self, scn):
		result = scn.name
		fieldnames = list(scn.getInputNames()) + list(scn.getExtraNames())

		# Get scannable position as a list
		scnpos = self.getScannableValueAtFeature(scn)
		try:
			scnpos = list(scnpos)
		except TypeError: # not a list
			scnpos = [scnpos]
		
		# if single fieldname matches scannable name, format is "processor.scn = value"
		if len(fieldnames)==1:
			if fieldnames[0]==scn.name:
				return result + ' = ' + scn.getOutputFormat()[0] % scnpos[0]
		
		# Otherwise format is "processor.scn: f1, f2 = v1, v2"
		result += ': '		
		for name in fieldnames:
			result += name + ', '
		result = result[:-2]
		result += ' = '
		for format, pos in zip(scn.getOutputFormat(),scnpos):
			result += 'None' if pos==None else format%pos + ', '
		result = result[:-2]
		return result
		
	def go(self):
		scn = self.abscissa_scannable
		val = self.getScannableValueAtFeature(scn)
		try:
			len(val)
		except TypeError:
			val = [val]
		val = val[:len(scn.getInputNames())]
		if len(val)==0:
			raise Exception("The scannable %s has no input fields"%scn.getName())
		if len(val)==1:
			val = val[0]
		print "Moving %s to %s" % (scn.getName(), `val`)
		scn.moveTo(val)

	def __findValueInDataSet(self, value, dataset):
		return list(dataset.doubleArray()).index(value)
	
	def determineScannableValuesAtFeature(self, scannables, scanFileHolder, xname, xvalue):
		dsx = scanFileHolder.getDataSet(xname)
		feature_inside_scan_data = dsx.min() <= xvalue <= dsx.max()
			
		result = {}
		for scn in scannables:
			try:
				pos = []
				fieldnames = list(scn.getInputNames()) + list(scn.getExtraNames())
				for fieldname, format in zip(fieldnames, scn.getOutputFormat()):
					if format == '%s':
						# Cannot get filenames from SRS files!
						value = float('nan')
					else:
						dsfield = scanFileHolder.getDataSet(fieldname)
						if feature_inside_scan_data:
							interp = scanFileHolder.getInterpolatedX(dsfield, dsx, xvalue)
							# Hack to get around GDA-2269
							if len(list(interp))==0:
								value = dsx[len(dsx)-1]
							else:
								value = interp[0]
						else: # feature not inside scan
							if fieldname == xname:
								value = xvalue
							else:
								# Trick case. Return start or end value for field
								if xvalue <= dsx.min():
									value = scanFileHolder.getDataSet(fieldname)[0]
								else: # xvalue >= dsx.min()
									value = scanFileHolder.getDataSet(fieldname)[-1]
								
					pos.append(value)
				if len(pos)==1:
					result[scn] = pos[0]
				else:
					result[scn] = pos
			except:
				pass
		return result

	def getScannableValueAtFeature(self, scn):
		return self.scannableValues[scn]
	


	def __repr__(self):
		return self.str
	
	def __str__(self):
		return self.__repr__()
