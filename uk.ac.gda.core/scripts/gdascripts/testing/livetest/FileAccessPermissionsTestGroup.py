from gdascripts.testing.livetest.TestGroup import TestGroup, Test

import popen2


def exe_cmdl(cmd):
	# n.b. strip!!!
	fin, _ = popen2.popen2( cmd )
	l = []
	for s in fin.readlines():
		l.append(s.strip())
	return l


class FileFaclEntryTest(Test):
	
	def __init__(self, path, expectedEntry):
		self.path = path
		self.expectedEntry = expectedEntry
	
	def test(self):
		'''(okayflag, report, exc_info) = test()
		performs test and returns a boolean okayflag, a viewable report and an exception if appropriate
		report. 
		'''
		faclEntries = exe_cmdl("getfacl " + self.path)
		if len(faclEntries)==0:
			return (False, "** " + self.path + " does NOT exist", None)
		
		if self.expectedEntry in faclEntries:
			report = "   " + self.path + " contains facl '" + self.expectedEntry +"'"
			return(True, report, None)
		else:
			report = "** " + self.path + " is MISSING the facl '" + self.expectedEntry +"'"
			return(False, report, None)


class FileAccessPermissionsTestGroup(TestGroup):

	def __init__(self):
		# OVERIDE THIS
		self.headerString = "Checking file access permissions"
		self.nTests = 0
		self.paths = []
		self.entries = []
		
	def addFaclEntry(self, path, entry):
		self.paths.append(path)
		self.entries.append(entry)
		self.nTests += 1
	
	def mytest(self):
		failedTests={}
		failedTestIdx = 0
		for path, entry in zip(self.paths, self.entries):
			test = FileFaclEntryTest(path, entry)
			(okay, report, exc_info) = test.test()
			print report
			if not okay:
				failedTests[failedTestIdx] = (report, exc_info)
				failedTestIdx += 1
		return failedTests