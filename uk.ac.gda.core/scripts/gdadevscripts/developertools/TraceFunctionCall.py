import sys
import re
from org.slf4j import LoggerFactory
	
	
class TraceFunctionCall:
	
	
	def __init__(self,  options, toTerminal=True, toGdaLog=False):
		self.options = options
		self.logToTerminal = toTerminal
		self.logToGdaLog = toGdaLog
		if self.logToGdaLog:
			self.logger = LoggerFactory.getLogger('**Trace**')
	
	def output(self, string):
		# end with comma to ensure linefeed is not added as this is already done by the call to print in logEntry
		if self.logToTerminal:
			print string
		if self.logToGdaLog:
			self.logger.info(string)
			
	def findOption(self, functionName):
		for option in self.options:
			match =  re.match( option[0], functionName)
			if match and (match.group(0)==functionName):
				return option[1]
				
		return None
		
	def isPointToBeTraced( self, optionlist, point):
		if not optionlist == None:
			for option in optionlist:
				if option == point:
					return True
			#return point in optionlist
		return False
		
	def __call__(self, objName, functionName, functionObject, functionArguments, functionKeywords):
		option = self.findOption( functionName)
		if self.isPointToBeTraced( option, "In") == True:
			s = ""
			s+= "< " + objName + "." + functionName
			if not (functionArguments is None):
				if len(functionArguments)>=1:
					s+= ", args = " + str(functionArguments)
			if not (functionKeywords is None):
				if len(functionKeywords)>=1:
					s+= ", keywords = " + str(functionKeywords)
			s+= " >"	
			self.output(s)

		returnCode = functionObject( *functionArguments, **functionKeywords)			

		if self.isPointToBeTraced( option, "Out") == True:
			s = ""
			s+= "</" + objName + "." + functionName
			if not (returnCode is None):
				s+= ", ret = " + str(returnCode) + " >"
			else:
				s+= " >"
			self.output(s)

		if not (returnCode is None):
			return returnCode


	
		
if __name__ == '__main__':
	class Test:
		def Test(self, argument1, argument2):
			print argument1, argument2
		
		def TestReturn(self, argument1):
			return argument1
			
		def getName(self):
			return "Test"
	
	class TestTest(Test):
		def __init__(self, traceFunctionCall, test):
			self.traceFunctionCall = traceFunctionCall
			self.obj = test
			
		def Test(self, *arguments, **keywords):
			function = self.obj.Test
			return self.traceFunctionCall( self.obj.getName(), "Test", function, arguments, keywords)

		def TestReturn(self, *arguments, **keywords):
			function = self.obj.TestReturn
			return self.traceFunctionCall( self.obj.getName(), "TestReturn", function, arguments, keywords)



	traceOptionsIn = [ "In" ]
	traceOptionsAll = [ "In", "Out" ]
	options = [ (r'TestReturn' , traceOptionsIn ), (r'.*' , traceOptionsAll ) ]
	traceFunctionCall = TraceFunctionCall( options )
	test = Test()
	testTest = TestTest( traceFunctionCall, test)
	print testTest.Test("Argument1", "Argument2")
	print testTest.TestReturn("Argument1")
		