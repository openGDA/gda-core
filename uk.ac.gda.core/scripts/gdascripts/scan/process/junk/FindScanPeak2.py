import math
from gda.configuration.properties import LocalProperties

def FindScanPeak2(ordinateName, printNameList=[], abscissaNameList=[], scanID=0, peakRatio=-1, ToPrint=1, LocalProperties=LocalProperties):
	""" Values= FindScanPeak(ordinateName, printNameList=[], scanID='null', ToPrint=1)
	Finds the peak value within a scan (currently just the maximum value). Returns all
	values from this line as dictionary. If enabled prints some values from this line.
	
	Will return only a peak that extends at least peakRatio*noise_rate above the Poisson noise floor. If no
	peak large enougth exists then Values returned is empty.
  
	Keyword arguements:
	ordinateName -- the header name of the value whose peak is to be found
	printNameList -- a list of header names to print (if enabled),
                  abscissaNameList --  NOT USED. may later be used for more advanced peak finding algorithms
	scanID -- leave empty to read most recent scan. Else a scan number or an absolute
		file path. If negative (-n), will find the peak of the scan n scans old.
	peakRatio -- If non-negative, this is the factor that a peak must extend above the Poisson noise floor to count.
		i.e. (peakValue-noise) >= peakRatio*noise, where noise is the avergae poisson noise floor.
		Be warned that the measured poisson noise floor is calculated without accounting 
		for any real signal that may be present, so will be an overestimate.
	ToPrint -- Set to 0 to disable printing

	Returns:
	The values of all elements from the selected line in a dictionary with header
	names as keys (or an empty dictionary if peakRatio is ser, and no peak was found)

	Example:
	run FindScanPeak  (as import is dodgy!)
	peakValues = FindScanPeak("y", ["x","y"], scanID = 15) #loads scan number 15, finds the maximum y,
		prints x and y, and returns all values.
	peakValues = FindScanPeak("y", ["x","y"], peakRatio=5) #loads the most recent scan and finds the maximum
		y if it large enougth, prints x and y, and returns all values.
	
	"""

 	# ----- Generate path -----
    
	# Use scanID as path if it is a string
	if type(scanID) == type('gda rocks'):
    		filepath = scanID 
	else:  # Assemble a path from the scan number, using current scan if not given
		# If no scanID given, use current scan number
		if scanID <= 0:
			import gda.data.NumTracker
			numtracker = gda.data.NumTracker('tmp')
			scanID = numtracker.getCurrentFileNumber() + scanID
		
		# Implicit else: scanID must be a positive number
        
		# scanID now contains the number of a scan
		filepath = LocalProperties.get("gda.data.scan.datawriter.datadir")
		filepath = filepath + '/' + str(scanID) + '.dat'  
                    
	if ToPrint == 1:    
		print "In file: %s" % filepath


	# ----- Read the file -----
	(headerList, columnList) = readSRSDataFile(filepath)

     
	# ----- Turn these into dictionary objects with header names as keys -----
	dataDict = {}

	for i in range(0, len(headerList)):
		dataDict[headerList[i]]=columnList[i]


	# ----- Find the index of the point -----
	iPeak = findMaxPointNonInterp(dataDict[ordinateName])
	# Get the value of the peak
	tempList = dataDict[ordinateName]
 	peakValue = tempList[iPeak] 
    
	# ----- Calculate statistics -----


	y_array = dataDict[ordinateName]
	calc_noPoints = len(y_array)
	# Normal distribution statistics
	calc_mean = average(y_array)
	calc_stdDev = standardDeviation(y_array)
	# Poisson distribution statistics
	calc_lambda = calc_mean
	calc_poisson_stddev = math.sqrt(calc_lambda)
	# Threshold based on Poisson statistics
	calc_threshold =  peakRatio*calc_poisson_stddev + calc_lambda


	# ----- Print some stuff unless ToPrint passed in as 0 -----
	if ToPrint == 1:   
		print "Max %s occurs at point %i (starting from 0) where:" % (ordinateName, iPeak)
		for i in range(0, len(printNameList)):
			tempList = dataDict[printNameList[i]]                    
			print "%s  =  %f" % (printNameList[i],tempList[iPeak])
		print ""
		if (peakRatio >=0):	# Show statistics as a peak threshold exists
			print "Number points: %i" % calc_noPoints
			print "Normal:\tmean: %f \t stddev: %f" % ( calc_mean, calc_stdDev)
			print "Poisson:\tlambda: %f \t stddev(noise): %f" % ( calc_lambda, calc_poisson_stddev)
	    		print "Threshold (%f*%f+%f) is:%f" % (peakRatio, calc_poisson_stddev, calc_lambda, calc_threshold)
			if peakValue>=calc_threshold:
				print "*Peak found*: The peak value of %f is greater than the threshold" % peakValue	
			else:
				print "*No peak found* The peak value of %f less than the threshold" % peakValue
	
	# ----- Return a dict object of values line where max occured -----
	# If threshold is exceeded...
	if ((peakValue>=calc_threshold) or peakRatio<0):
		maxPeak = {}
		for i in range(0, len(headerList)):
			tempList = dataDict[headerList[i]]
			maxPeak[headerList[i]] = tempList[iPeak]
		return maxPeak
	else: # Return an empty dictionary
		return {}

def average(values):
	"""Computes the arithmetic mean of a list of numbers.
	>>> print average([20, 30, 70])
	40.0
	"""
	return sum(values, 0.0) / len(values)

def standardDeviation(x):
	"""Computes the standard deviation of a list of numbers.
	"""
	E=average				# Expected function
	x_squared = map(pow, x, [2]*len(x));	# each value of x squared

	#return math.sqrt( E(x_squared) - ( E(x) )**2 )
	return math.sqrt(10)



 
def findMaxPointNonInterp(ordinateList):
	"""index = rindMaxPointNonInterp(ordinateList): Returns index of maximum value in list"""
	runningMaxVal = ordinateList[0]
	runningMaxI = 0
	for i in range (0, len(ordinateList)):
		if ordinateList[i] > runningMaxVal:
			runningMaxI = i
			runningMaxVal = ordinateList[i]
            
	return runningMaxI


def readSRSDataFile(filepath):
	"""(headerList, data) = readSRSDataFile(filepath): Reads an SRS data file.

	Keyword arguments:
	filepath -- the absolute path of file to open.
    
	Returns:
	headerList -- Column headers as an array of strings
	data -- An list of columns data. Each column a list of doubles.

	"""
    
	# Read the entire file into an array of strings called lines
	try:
		f = open(filepath, "r")
		lines = f.readlines()
		f.close()
	except Exception, e:
		raise IOError, 'Failed to read SRS data file: Could not open file'
	
	fileLength = len(lines)
    
	# Move lineIndex to first line after the line with END (The header line)
	lineIndex = 0;
	while lineIndex < fileLength:
		if lines[lineIndex].find('END')!=-1:
			break      
		if (lineIndex == (fileLength - 1)):
			raise IOError, 'Failed to read SRS data file: No END found'
		lineIndex = lineIndex + 1
	lineIndex = lineIndex + 1    
    
	# Read headers and leave lineIndex on first line of data
	lines[lineIndex] = lines[lineIndex].strip("\n")
	headerList = lines[lineIndex].split("\t")
	lineIndex = lineIndex + 1
	noColumns = len(headerList)
    
	# Make an array to hold data for each column
	data=[]
	for i in range(0, noColumns):
		data.append([])
    
	# Fill in each line of data
	while lineIndex < fileLength:
		rowData = lines[lineIndex].split("\t")
		for i in range(0, noColumns):
			data[i].append(float(rowData[i]))
		lineIndex = lineIndex + 1
        
	# Return the header list, and the array of column lists
	

	return (headerList, data)



