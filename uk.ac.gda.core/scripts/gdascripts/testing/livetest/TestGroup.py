#from javaos import name
from gda.device import Scannable
from time import clock, sleep
import sys

class Test:		
	def test(self):
		'''(okayflag, report, exc_info) = test()
		performs test and returns a boolean okayflag, a viewable report and an exception if appropriate
		report. 
		'''
		#OVERIDE THIS
		pass


class TestGroup:

	def __init__(self):
		# OVERIDE THIS
		self.headerString = "What this will do"
		self.nTests = None
			
	def test(self):
		'''(nTests, nFailed, failedTests) = test()'''
		print ""
		print ""
		print "##########"*8
		print self.headerString
		print "##########"*8
		print ""
		
		failedTests = self.mytest()
		if len(failedTests)>0:
			print ""
			print "###### Failure Summary #####"
			keys = failedTests.keys()
			keys.sort()
			for keyname in keys:
				print failedTests[keyname][0]
			return(self.nTests, len(failedTests), failedTests)
		
		print ""
		print ""
	
	def mytest(self):
		# OVERIDE this
		pass