from gdascripts.testing.livetest.TestGroup import Test, TestGroup

from gda.jython import JythonServerFacade
from gda.jython.commands.InputCommands import requestInput as raw_input

from time import sleep
import pickle
import sys
import java


class ExtendedSyntaxCommandTest(Test):
	
	class FileLikeObject:
		def __init__(self):
			self.lines =[]
			
		def write(self, line):
			self.lines.append(line)
	
	def __init__(self, command):
		self.command = command
		
	def test(self):
		'''(okayflag, report, exc_info) = test()'''
		(executionTime, e) = self.runWrappedExtendedSyntaxCommandAndReturnException(self.command)
		report = "command '%s'" % self.command
		report = report.ljust(40)
		if not e:
			return (True, report + " returned in %fms"%(executionTime*1000), None)
		else:
			try:
				reason =  e.args[0]
			except:
				# probably a java exception
				reason = e.getMessage()
			return (False, report + " threw EXCEPTION: **%s**" %reason, (None, e, None))


	def runWrappedExtendedSyntaxCommandAndReturnException(self, command):
		'''(time, e) = runWrappedExtendedSyntaxCommandAndCollectAnyException(command)
		
		NOTE: pickle won't pickle-up exc_info() output from in here for some reason.
		Returns the exception instead.'''
		wrappedCommand = """
import pickle
import sys
from time import clock
def trySomething():
	try:
		unlikelyName8466_t = clock()
		%s
		unlikelyName8466_t = clock() - unlikelyName8466_t
	except (Exception, java.lang.Exception), unlikelyName8466_exception:
		print '* returning: ', unlikelyName8466_exception, '*'
		return pickle.dumps((None, unlikelyName8466_exception))
	return pickle.dumps((unlikelyName8466_t, None))
""" % command

		JythonServerFacade.getInstance().runCommand(wrappedCommand)
		sleep(.1)
		mystdout = ExtendedSyntaxCommandTest.FileLikeObject()
		mystderr = ExtendedSyntaxCommandTest.FileLikeObject()
		saveout = sys.stdout
		saveerr = sys.stderr
		sys.stdout = mystdout
		sys.stderr = mystderr
		try:
			pickledTuple = JythonServerFacade.getInstance().evaluateCommand("trySomething()") #just an exception for now
		except (Exception, java.lang.Exception), e:
			sys.stdout = saveout
			sys.stderr = saveerr
			raise e
		sys.stdout = saveout
		sys.stderr = saveerr
		result = pickle.loads(pickledTuple)
		return result

		
class ExtendedSyntaxCommandTestGroup(TestGroup):

	def __init__(self):
		# OVERIDE THIS
		self.headerString = "Checking basic gda commands (mainly with dummy scannables)"
		self.nTests = 0
		self.commands = []
		
	def addCommand(self, command):
		self.commands.append(command)
		self.nTests += 1

	def addPrompt(self, prompt):
		self.addCommand("prompt:" + prompt)
	
	def mytest(self):
		print "in mytest"
		failedTests={}
		failedTestIdx = 0
		for command in self.commands:
			if command[0:7]=="prompt:":
				raw_input(command[7:])
			else:
				test = ExtendedSyntaxCommandTest(command)
				(okay, report, exc_info) = test.test()
				if okay:
					report = "   " + report
				else:
					report = "** " + report
				print report
				if not okay:
					failedTests[failedTestIdx] = (report, exc_info)
					failedTestIdx += 1
		return failedTests
