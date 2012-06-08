#@PydevCodeAnalysisIgnore
##from java.lang import exception???? not needed
global det, CurrentScanObject, SRSWriteAtFileCreation
import time

class ScanWrapperClass:

	def __init__(self):
		global det, CurrentScanObject					#used for data collumn for peak value if this collumn is present in scan
		self.name='scanwrapper'						#name of the scan type

	def go(self, pos):
		print(ScanWrapperClass.FirstPD(pos))

	def __call__(self,*params):
		global CurrentScanObject, SRSWriteAtFileCreation
		CurrentScanObject=self

		print "=== Scan started on: "+time.ctime()
		self.starttime=time.time()					#start timer
		self.clear_scan_stats()						#set maxpos etc to string
		[old_devlist, old_paramlist]=self.get_scan_params(params)	#convert scan parameter list to separate device and parameter lists
#		print params
#		print old_devlist
#		print old_paramlist
		self.pd0=old_devlist[0]						#select first PD in list
		ScanWrapperClass.FirstPD=self.pd0				#save first pd name to class to be accessible to all scan objects
 		self.startpos=self.get_start_positions(self.pd0)		#get position of PD
		[self.devlist, self.paramlist]=self.convert_scan_params(old_devlist,old_paramlist)	#convert parameters for special scan types (e.g. centred scan)
		self.newparams=self.create_scan_params(self.devlist,self.paramlist)	#create new scan parameter list

		SRSWriteAtFileCreation="\ntestdata='letagetabitarockin'\n"
#		print 'old params:',params
#		print 'new params:', self.newparams
		
		try:
			try:
				scan(self.newparams)			#do scan
			except InterruptedException,e:
				print "=== Problem with scan or halt button pressed"
				raise
		finally:
			self.get_scan_stats()				#set up maxpos etc
			self.after_scan_end()				#do after_scan_end stuff such as return to start if coded
			print "=== Scan ended on: "+time.ctime()+". Elapsed time: %.0f sec" % (time.time()-self.starttime)


	def after_scan_end(self):
		print "=== Scan ended"

	def isPD(self,obj):
		# test if object is a Pseudodevice
		return isinstance(obj, PseudoDevice) or isinstance(obj, ScannableBase) 

	def get_scan_params(self,params):
#		print len(params), params
		# create lists of devices and parameters
		devlist=[]; paramlist=[];
		for token in params:
			if self.isPD(token):						#parameter is a Pseudodevice
				if not(devlist==[]):				#if device list not empty...
					paramlist+=[currentlist];		#append list of device parameters to parameter list for previous device
				currentlist=[];					#reset current list
				devlist=devlist+[token]; 			#append new device to device list
			elif isinstance(token, (int, float, list)):		#paramemer is a number
				currentlist+=[token];				#add parameter to current parameter list
			else:
				raise '=== Parameter must be a PseudoDevice, number or list',token
		paramlist+=[currentlist];					#append last one to list	
		return [devlist, paramlist]

	def convert_scan_params(self, devlist,paramlist):
		return	[devlist, paramlist]					#no change to parameters. Change this method as required.

	def create_scan_params(self,devlist,paramlist):
		full_list=[]
		full_name_list=[]
		for n in range(len(devlist)):
#			print 'devlist[n]', [devlist[n]]
			full_list+=[devlist[n]]
			full_name_list+=[devlist[n].getName()]
#			print 'full_name_list', full_name_list
			if paramlist[n]!=[]:					#do not append if empty list
#				print 'hmm...', paramlist[n]
				full_list+=paramlist[n]
#				print 'dumdidum...'
				full_name_list+=paramlist[n]
#				print 'ooo...'
#		print 'full_list', full_list
#		print 'full_name_list', full_name_list
		return full_list

	def get_start_positions(self,pd):

		try:
			pdpos=list(pd[:]);			#list converts to list. remove when bug fixed
			if len(pdpos)==1:			#this line not needed if bug fixed. 
				pdpos=pdpos[0]		#this line not needed if bug fixed
		except:
			pdpos=pd[:]
		return pdpos

	def get_scan_stats(self):
		#get cen value assuming first PD in scan list is x and last collumn of the last PD is y
		global maxval, maxpos
		pdx=self.devlist[0];
		pdy=self.devlist[-1];
		try:
			maxval=FindScanPeak(det);				#collumn from global det
		except:
			maxval=FindScanPeak((pdy.getInputNames()+pdy.getExtraNames())[-1]);	#use last collumn of pdy
#		print 'max=',maxval
#		print pdx.getInputNames()
		maxpos=[] 							#assume vector (input) device
		for in_name in pdx.getInputNames():
			maxpos+=[maxval[in_name]]
		if len(maxpos)==1:
			maxpos=maxpos[0]				#return scalar if single element list
		print 'maxpos=', maxpos

	def clear_scan_stats(self):
		global maxval, maxpos
		maxval='Error: Invalid maxval value'		
		maxpos='Error: Invalid maxpos value'

	def getDet(self):
		global det
		try:
			return det
		except:
			print "No scan column specified for detector - it will be closen automatically"
			return None

	def setDet(self,det_col_name):
		global det	
		det=det_col_name
		print "Scan column for detector: ", det	

	def add(self,*items):
		#add  list objects containing lists of lists and numbers. arrays must be same size or scalar
		allscalars=1; somescalars=0; listsize=1; newitems=[];
		for item in items:
			newitems+=[item]				#add item to newitems list
			if isinstance(item,(int, float)):		#numerical objects
				somescalars=1;
			else:
				if len(item)==1:
					newitems[-1]=item[0];		#convert single element list to component object and replace last element in newitems list
					somescalars=1;
				else:
					allscalars=0;			#not all scalars
					if listsize==1:
						listsize=len(item);
					if listsize!=len(item):
						print "=== Inconsistent shape"
						raise
		if allscalars==1:					#all scalars so add
			tot=0;
			for item in newitems:
				tot=tot+item				#add as scalars
			return tot
		if somescalars==1:
			for i in range(len(newitems)):
				try:
					len(newitems[i]);		#will fail if not item with length
				except:
					newitems[i]=[newitems[i]]*listsize;	#pad out scalars to fit lists e.g. 1=>[1,1,1]
		result=(newitems[0])[:];					#remove bracket after bug fix? use [:] to avoid copy by reference
		for i in range(len(result)):
			for item in newitems[1:]:
				result[i]=self.add(result[i], item[i]);		#call add recursively if still not scalars
		return result

	def mult(self,*items):
		#multiply list objects containing lists of lists and numbers. arrays must be same size or scalar
		#same as add but tot=1 and multiply scalars
		allscalars=1; somescalars=0; listsize=1; newitems=[];
		for item in items:
			newitems+=[item]				#add item to newitems list
			if isinstance(item,(int, float)):		#numerical objects
				somescalars=1;
			else:
				if len(item)==1:
					newitems[-1]=item[0];		#convert single element list to component object and replace last element in newitems list
					somescalars=1;
				else:
					allscalars=0;			#not all scalars
					if listsize==1:
						listsize=len(item);
					if listsize!=len(item):
						print "=== Inconsistent shape"
						raise
		if allscalars==1:					#all scalars so add
			tot=1;
			for item in newitems:
				tot=tot*item				#add as scalars
			return tot
		if somescalars==1:
			for i in range(len(newitems)):
				try:
					len(newitems[i]);		#will fail if not item with length
				except:
					newitems[i]=[newitems[i]]*listsize;	#pad out scalars to fit lists e.g. 1=>[1,1,1]
		result=(newitems[0])[:];					#remove bracket after bug fix? use [:] to avoid copy by reference
		for i in range(len(result)):
			for item in newitems[1:]:
				result[i]=self.mult(result[i], item[i]);		#call add recursively if still not scalars
		return result


class ScanWrapperReturnToStartClass(ScanWrapperClass):
	'''
	This scan type returns to start position at the end
	go command moves first PD from scan to value, e.g. 'go maxpos'
	'''

	def after_scan_end(self):
		print '=== Moving ', self.pd0.getName(),' back to start'
		print self.pd0(self.startpos);					#move back to start	
		#print '=== Done'

class ScanWrapperAscanClass(ScanWrapperClass):	
	'''
	This is an absolute scan based on the SPEC acan syntax
	ascan dev1 start end intervals dev2 ...
	Note that only the first device follows the ascan syntax with subsequent devices following the normal scan syntax	
	go command moves first PD from scan to value, e.g. 'go maxpos'
	'''

	def __init__(self):
		global det, CurrentScanObject						#used for data collumn for peak value if this collumn is present in scan
		self.name='ascan'							#name of the scan type

	def convert_scan_params(self, devlist,paramlist):				#scan parameters for spec dscan #######################################
		if len(paramlist[0])!=3:
			print "== First device needs three parameters for a dscan"
			raise
		start=paramlist[0][0]
		end=paramlist[0][1]
		intervals=paramlist[0][2]
		step=self.mult(self.add(end,self.mult(start,-1.0)),(1.0/intervals))		#calculate step size
#		print step
		paramlist[0][2]=step						#replace internals with step
		return	[devlist, paramlist]	

class ScanWrapperDscanReturnToStartClass(ScanWrapperReturnToStartClass):	
	'''
	This is a centred scan based on the SPEC dcan syntax
	dscan dev1 start end intervals dev2 ...
	Note that only the first device follows the dscan syntax with subsequent devices following the normal scan syntax
	go command moves first PD from scan to value, e.g. 'go maxpos'
	'''

	def __init__(self):
		global det, CurrentScanObject						#used for data collumn for peak value if this collumn is present in scan
		self.name='dscan'							#name of the scan type

	def convert_scan_params(self, devlist,paramlist):				#scan parameters for spec dscan #######################################
		if len(paramlist[0])!=3:
			print "== First device needs three parameters for a dscan"
			raise
		start=self.add(paramlist[0][0],self.startpos)				#add start position
		end=self.add(paramlist[0][1],self.startpos)
		intervals=paramlist[0][2]
		step=self.mult(self.add(end,self.mult(start,-1.0)),(1.0/intervals))		#calculate step size
		paramlist[0][0]=start
		paramlist[0][1]=end
		paramlist[0][2]=step						#replace internals with step
		return	[devlist, paramlist]	

class ScanWrapperCscanReturnToStartClass(ScanWrapperReturnToStartClass):	
	'''
	This is a centred scan based on the Pincer scancn syntax
	cscan dev1 step1 n1 step2 n2 ... dev2 ...
	Multidimensional scans can be built up with several step/n pairs up to the (input) dimensionality of the device
	Note that only the first device follows the dscan syntax with subsequent devices following the normal scan syntax
	go command moves first PD from scan to value, e.g. 'go maxpos'
	'''

	def __init__(self):
		global det, CurrentScanObject						#used for data collumn for peak value if this collumn is present in scan
		self.name='scancn'							#name of the scan type

	def convert_scan_params(self, devlist,paramlist):				#scan parameters for spec dscan 
		pddim=len(devlist[0].getInputNames())					#dimension of first pd
		scandim=len(paramlist[0])/2
		print '=== Doing a %.0f dimensional centred scan' % scandim	
		if scandim*2!=len(paramlist[0]):
			print "== First device needs an even number of parameters for a cscan"
			raise
		if scandim>pddim or scandim==0:
			print "== Wrong vector size for first device"
			raise
		newparams=(scandim+2)*[0]						#list of zeros of correct length
		steps=[]
		span=self.mult(0,paramlist[0][0])						#scalar or list of zeros of same size as original parameter 
		for i in range(0,2*(scandim-1)+1,2):
#			print 'plist0', paramlist[0][i]
			steps+=[paramlist[0][i]]						#add step vector to list		
			span=self.add(span,self.mult(paramlist[0][i],paramlist[0][i+1]-1))		#add step*n to span of scan

		start=self.add(self.startpos,self.mult(span,-0.5))
		end=self.add(self.startpos,self.mult(span,0.5))
		newparams=2*[0]								#new list with two zeros, to be replaced with...
		newparams[0]=start
		newparams[1]=end
		newparams+=steps
#		print 'steps', steps
#		print 'nerparams', newparams
		paramlist[0]=newparams
		return [devlist, paramlist]

from gda.jython.commands.GeneralCommands import alias
lup=dscan=ScanWrapperDscanReturnToStartClass()
go=dscan.go
alias("go")
ascan=ScanWrapperAscanClass()
scancn=ScanWrapperCscanReturnToStartClass()
alias("dscan")
alias("lup")
alias("ascan")
alias("scancn")
