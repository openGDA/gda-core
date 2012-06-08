from gda.server.collisionAvoidance import CollisionCheckerBase
from gda.server.collisionAvoidance import CacException

reload(CollisionCheckerBase)
	
class PyRuleChecker(CollisionCheckerBase):
	'''PyRuleChecker()
	Rules that evaluate to true indicate problems.
	'''
	# Define the parameters names you wish to assign the passed in configuration to
	
	# A list of strings that will be formed into variables and filled with the
	# numbers describing the configuration to be tested. These are the variable
	# names that will be valid in any PythonRules that are set.
	
	
	def __init__(self, name, paramNamesForRules=None):
		# Required
		self.setName(name)
		
		# Custom
		if paramNamesForRules!=None:
			self.setParamNames(paramNamesForRules)	# setting directly doesn't seem to work??
		
		self.ruleNames = []
		self.rules = []
		self.ruleErrorStrings = []
		
	def toString(self):
		# Required
		toReturn = ""
		toReturn += "<" + self.name + ">\n"
		toReturn += "\tparameters:\t"  + str(self.paramNames) + "\n"
		for i in range(0, len(self.rules)):
			toReturn +=  "\t" + self.ruleNames[i] + ":\t" + self.rules[i] + "\t(" + self.ruleErrorStrings[i] + ")"
		return toReturn
			
	def __str__(self):
		return self.toString()			
				
	def addRule(self, ruleName, ruleString, errorString=None):
		'''PyRuleChecker(
		Rules must evaluate to true for save configurations.
		'''
		if self.ruleNames.count(ruleName)>0:
			toThrow = "CAC rule not added . A rule labeled " + ruleName + " already exists in PyRuleChecker "+ self.name+"."
			raise CacException, toThrow
		
		self.ruleNames.append(ruleName)
		self.rules.append(ruleString)
		self.ruleErrorStrings.append(errorString)
		
	def updateRule(self, ruleName, ruleString,  errorString=None):
		try:
			i = self.ruleNames.index(ruleName)
		except ValueError:
			toThrow = "CAC rule not updated. No rule labeled " + ruleName + " found in PyRuleChecker "+ self.name+"."
			raise CacException, toThrow
			
		self.rules[i] = ruleString
		self.ruleErrorStrings.append(errorString)
		
	def removeRule(self, ruleName):
		try:
			i = self.ruleNames.index(ruleName)
		except ValueError:
			toThrow = "CAC rule not removed. No rule labeled " + ruleName + " found in PyRuleChecker "+ self.name+"."
			raise CacException, toThrow
		
		self.ruleNames.pop(i)
		self.rules.pop(i)
	
	#def getParamNames(self):
	#	return self.paramNames
		

	def isConfigurationPermitted(self, configuration):
		
		#Check configuration array matches length of paramNames
		if len(configuration)!=len(self.paramNames):
			toThrow = "CAC error. PyRuleChecker "+ self.name+".isConfigurationPermitted() called with wrong number of parameters."
			raise CacException, toThrow
		
		from math import *
		
		# load the configuration parameetrs into variables named by paramNames
		# these exist only within the scope of this method call
		for i in range(0, len(self.paramNames)):
			# ( needs to be done steps as the gda parser fluffs "[" + "]"
			s = str(self.paramNames[i]) + " = configuration"
			s = s + "["
			s = s + str(i)
			s = s + "]"
			exec(s)
		
		# This value to return will be None if all rule checks are okay, or will be a list
		# user readable strings describing which rules failed.	
		toReturn = list();
		
		# evaluate each rule. If it returns true then move is okay, else tack a descriptive failure string onto to Return.
		try:
			for i in range(0, len(self.rules)):
				s = eval(self.rules[i])
				if s == 1:
					configJython = []
					for tmp in range(0, len(self.paramNames)):
						configJython.append(configuration[tmp])		
					toReturn.append(self.ruleErrorStrings[i] + "\n  (rule " + self.ruleNames[i]
										+": " + self.rules[i] +" was true in checker " + self.name+ " at point " + str(configJython) +")")
		except NameError, e:
			raise NameError, "NameError evaluating a python collision avoidance rule set in the PyRuleChecker "\
		 		+ self.name +".\n( rule " + self.ruleNames[i] +": " + self.rules[i]\
		 		+ " ).\nThe jython names used by rules in this checker are: "\
		 		+ str(self.paramNames) + ".\nOrignial Error: " + e
		 		
		except:
			print "*Error evaluating a python collision avoidance rule set in the PyRuleChecker "\
		 		+ self.name +".\n( rule " + self.ruleNames[i] +": " + self.rules[i]\
		 		+ " ).\nThe jython names used by rules in this checker are: "\
		 		+ str(self.paramNames) + ".\nOrignial Error: "
			raise		#re-raises last exception (would be better to set the above print as data to the re-raised error
			
			
		return toReturn

#class PyFuncChecker
#'''PyFuncChecker()
#	Functions that evaluate to true indicate problems.
#	'''