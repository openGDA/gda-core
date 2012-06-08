from gdascripts.testing.livetest.TestGroup import TestGroup, Test


from gda.device.scannable import PseudoDevice, ScannableBase
from gda.jython.commands.GeneralCommands import pause
from time import clock, sleep
import sys


COLUMNHEADER = ['label', 'name', 'exception', 'problem', 'level', 'isBusy()', 'getPosition()', '__repr__()', '__str__()', 'inputNames', 'extraNames', 'outputFormat', 'class', 'module', 'file']

class ReadOnlyScannableTest(Test):
	def __init__(self, name, scannable):
		self.name = name
		self.scannable = scannable
	
	def test(self):
		"""(okayflag, report, exc_info) = test()"""

		self.okay = True
		self.report = ("%s:" % self.name).ljust(20)
		self.row = {}
		self.__completeTable()
		# isBusy
		self.__timedMethodTest("isBusy()", self.__testIsBusy)
		if self.exc_info:
			self.__completeReport()
			return (self.okay, self.report, self.exc_info, self.row)
		
		# getPosition
		self.__timedMethodTest("getPosition()", self.__testGetPosition)
		if self.exc_info:
			self.__completeReport()
			return (self.okay, self.report, self.exc_info, self.row)		

		# __repr__
		self.__timedMethodTest("__repr__()", self.__testRepr)
		if self.exc_info:
			self.__completeReport()
			return (self.okay, self.report, self.exc_info, self.row)

		# __str__
		self.__timedMethodTest("__str__()", self.__testStr)
		if self.exc_info:
			self.__completeReport()
			return (self.okay, self.report, self.exc_info, self.row)
		
		# There were no exceptions
		self.__completeReport()
		return (self.okay, self.report, None, self.row)
	
	def __completeReport(self):
		self.row['label'] = self.name
		self.row['problem'] = False
		self.row['exception'] = False
		# indicate name mismatch
		if self.scannable.getName()!=self.name:
			self.report += " internalname = %s"% self.scannable.getName()
			self.report = "-" + self.report
			self.row['name'] = self.scannable.getName()
			self.row['problem'] = True
		else:
			self.report = " " + self.report
			self.row['name'] = ""
			self.row['problem'] = False
			
		# indicate problem (includes exceptions)
		if not self.okay:
			self.report = "*" + self.report
			self.row['problem'] = True
		else:
			self.report = " " + self.report

		# indicate exception
		if self.exc_info:
			self.report = "*" + self.report
			self.row['exception'] = True
		else:
			self.report = " " + self.report
			
	def __completeTable(self):
		# class
		self.row['class'] = self.scannable.__class__
		
		#module
		try:
			self.row['module'] = str(self.scannable.__class__.__module__)
		except AttributeError:
			self.row['module'] = ""
		
		#file
		if self.row['module'] != "":
			modulename = str(self.scannable.__class__.__module__)
			try:
				module = sys.modules[modulename]
				self.row['file']=module.__file__
			except KeyError:
				self.row['file']=""
			except AttributeError:
				self.row['file']=""
		else:
			self.row['file'] = ""
		
		#inputNames etc.
		self.row['inputNames'] = str(list(self.scannable.getInputNames()))
		self.row['extraNames'] = str(list(self.scannable.getExtraNames()))
		self.row['outputFormat'] = str(list(self.scannable.getOutputFormat()))
		self.row['level'] = self.scannable.getLevel()
		
	def __timedMethodTest(self, name, testMethod):
		self.exc_info = None
		try:
			t = clock()
			problem = testMethod()
			t = clock() - t
		except:
			self.okay = False
			self.exc_info = sys.exc_info()
			try:
				reason =  self.exc_info[1].args[0]
			except:
				# probably a java exception
				reason = self.exc_info[1].getMessage()
			self.report += "[%s(): **%s**] " % (name, reason)
			self.row[name] = reason
			return
		
		if problem:
			self.okay = False	
			self.report += "[%s(): *%s*] " % (name, problem)
			self.row[name] = problem
		else: #okay
			timestring = "%f"%(t*1000.0)
			self.row[name] = timestring
			timestring = timestring.rjust(10)
			self.report += "[%s() %sms] " % (name, timestring)


	def __testIsBusy(self):
		"""problem = __testIsBusy()"""
		if self.scannable.isBusy():
			return 'was unexpectadly busy'
		return None
		
	def __testGetPosition(self):
		"""problem = __testGetPosition()"""	
		pos = self.scannable.getPosition()
		if pos==None:
			return "returned None"
		return None
	
	def __testRepr(self):
		"""problem = __testRepr()"""
		s = self.scannable.__repr__()
		if (s==None) :
			return "returned None"
		if (s==""):
			return "returned empty string"
		return None
	
	def __testStr(self):
		"""problem = __testStr()"""
		s = self.scannable.__str__()
		if (s==None) :
			return "returned None"
		if (s==""):
			return "returned empty string"
		return None


class AllScannablesTestGroup(TestGroup):

	def __init__(self, rootNamespaceDict):
		self.allObjects = rootNamespaceDict
		self.scannables = self.__filterScannables(rootNamespaceDict)
		self.nTests = len(self.scannables)
		self.headerString = "Checking scannables for connectivity and implementation (read-only):"
		self.namesToSkip = []

	def addNameToSkip(self, name):
		self.namesToSkip.append(name)

	def mytest(self):
		self.failedTests={}
		keys = self.scannables.keys()
		keys.sort()
		self.rows=[]
		for keyname in keys:
			pause()
			if keyname not in self.namesToSkip:
				print "   " + keyname
				rost = ReadOnlyScannableTest(keyname, self.scannables[keyname])
				(okay, report, exc_info, row) = rost.test()
				self.rows.append(row)
			else:
				report = ("%s:" % self.scannables[keyname].name).ljust(20) + "Skipped"
				report = "   " + report
				okay = True
			print report
			if not okay:
				self.failedTests[keyname] = (report, exc_info)
		return self.failedTests
		
	def __filterScannables(self, dictionary):
		scannables = {}
		print dictionary.keys()
		for name in dictionary.keys():
			if isinstance(dictionary[name], PseudoDevice) or isinstance(dictionary[name], ScannableBase):
				scannables[name] = dictionary[name]
		return scannables
	
	def getCSVString(self):
		'''tabs not commas!'''
		s = ''
		for header in COLUMNHEADER:
			s += header + '\t'
		s = s[:-1] + '\n'
		
		for row in self.rows:
			for header in COLUMNHEADER:
				try:
					s += str(row[header]) + '\t'
				except KeyError:
					s += '\t'
			s = s[:-1] + '\n'
		return s

	def writeCSV(self, path):
		f=open(path, "w")
		f.write(self.getCSVString())
		f.close()