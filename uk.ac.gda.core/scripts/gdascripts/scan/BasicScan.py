
import math
import os

from gda.device.scannable import ScannableMotionBase, ScannableBase
from gda.device import Scannable

from gda.scan import ConcurrentScan
from gda.scan import ScanPositionProviderFactory
from gda.jython.commands.GeneralCommands import alias
from gda.jython import InterfaceProvider
from gda.jython import JythonServerFacade;
from gda.jython.commands.ScannableCommands import createScanPlotSettings

from gda.configuration.properties import LocalProperties

import java.lang.InterruptedException #@UnresolvedImport

import __main__ as gdamain;

class BasicScanClass(object):
	def __init__(self):
		self.finalPosition=None;
		self.scan=None;
		self.devices=None
		self.parameters=None;
		self.scanType='scan';
		self.command=None;
		
	def __call__(self, *args):

		self.devices, self.parameters=self.mapArgs(args);
		
#		vars(gdamain)["CustomerisedUserCommand"]=self.getLastCommand();
		vars(gdamain)["CustomerisedUserCommand"]=self.getCommand();
		
		newArgs=self.parseArgs(self.devices, self.parameters);
		
		#To create the scan
		self.scan = ConcurrentScan(newArgs)
		
		self.settings = createScanPlotSettings(self.scan);
		self.scan.setScanPlotSettings(self.settings);

#		if self.yaxis is not None:
#			self.settings.setYAxesShown([self.yaxis])

		#To run the scan
		try:
			self.scan.runScan()
			self.postScanRestoration();
		except java.lang.InterruptedException, e:
			vars(gdamain)["CustomerisedUserCommand"]=None;
			if not self.scan.wasScanExplicitlyHalted():
				raise e
			else:
				# Keep going if the scan was stopped manually
				print ("Scan stopped by user.")

		finally:
			pass;
		

	# To return to start positions, etc
	def postScanRestoration(self):
		if self.finalPosition:
			pass
#			self.returnToInitialPositions(initialPositions)
	
	def parseArgs(self, devices, parameters):
		newArgs=[]
		for k, v in zip(devices, parameters):
			newArgs.append(k);
			newArgs.extend(v);
		
		return newArgs;

	def mapArgs(self, args):
		''' To return two lists with scannables in the first list and their positions in the second '''
		newArgs=[]
		if len(args) == 0:
			raise SyntaxError(self.__doc__)
		# start off with the first arg which must be a scannable
		# the first argument should be a scannable else a syntax error
		if self.isScannable(args[0]) == False:
			raise Exception("First argument to scan command must be a scannable")
		
		if len(args) == False:
			raise Exception("First argument to scan command must be a scannable")
		
		scanDict=[[],[]];
		tokens = [self.scanType] #get the scan name
			
		i=-1	
		for a in (args):
			if self.isScannable(a): #new entry in the scanDict
				newDevice=a;
				tokens.append(newDevice.name);
				scanDict[0].append(newDevice);
				scanDict[1].append([]) #new positions for new device
				i+=1;
			else:
				tokens.append( str(a) );
				scanDict[1][i].append(a);
		
		self.command =' '.join(tokens);
		return scanDict;

	def mapArgsOld(self, args):
		''' To return a dictionary with scannable as key and positions as a list '''
		scanDict=dict();
			
		for a in (args):
			if self.isScannable(a): #new entry in the scanDict
				newDevice=a;
				scanDict[newDevice]=[];
			else:
				scanDict[newDevice].append(a);
		return scanDict;

	"A range function, that does accept float increments..."
	def frange(self, start, end=None, inc=None):
		if end == None:
			end = start + 0.0
			start = 0.0
		if inc == None:
			inc = 1.0
	
		L = []
		while True:
		    next = start + len(L) * inc
		    if inc > 0 and next >= end:
		        break
		    elif inc < 0 and next <= end:
		        break
		    L.append(next)
		
		return L

	def isScannable(self, obj):
		return isinstance(obj, (ScannableMotionBase, ScannableBase, Scannable))
	
	def getLastCommand(self):
		#jsf=InterfaceProvider.getJythonNamespace()
		jsf=JythonServerFacade.getInstance();
		
		historyFilePath = LocalProperties.get("gda.jythonTerminal.commandHistory.path", jsf.getDefaultScriptProjectFolder());
		historyFileName = os.path.join(historyFilePath, ".cmdHistory.txt")
		
		if not os.path.exists(historyFileName):
			print "No history found"
			strCmd=''
		else:
			historyFile=open(historyFileName, 'r');
			strCmd=( historyFile.readlines() )[-1];
			historyFile.close();
			
		#print "The last command is: " + strCmd;
		return strCmd

	def getCommand(self):
		return self.command;
	

#Usage
#from Diamond.Scans.BasicScan import BasicScanClass;
#del bscan
#bscan=BasicScanClass()
#alias('bscan');
#bscan( testMotor1, 0, 10, 1 )
#bscan( *[testMotor1, 0, 10, 1] )

