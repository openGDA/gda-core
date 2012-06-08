from gda.device import ScannableMotion
#from gdadevscripts.developertools import TraceFunctionCall


class TraceObjectsMethodCallsProxy:
	"""Prints and/or logs an object method calls using the TraceFunctionCall module"""
	
	def __init__(self,  traceMethodCall, scannableObjectToEmbed):
		self.obj = scannableObjectToEmbed
		self.traceMethodCall = traceMethodCall
	
	def __getattr__(self, attrib):
		"""
		Returns a method created on the fly which wraps the target method in a way
		that it takes the same arguments. Note that the wrapped method cannot
		be directly on this class (self), as the method and its name must be stored
		until the wrapped method is called later, and storing them in this object
		might not be thread safe
		"""
		wrapper = TraceObjectsMethodCallsProxy.TargetMethodWrapper( self.getObjectTag(), getattr(self.obj, attrib), attrib, self.traceMethodCall)
		return wrapper.wrapped
	
	def getObjectTag(self):
		return str(self.obj.__class__)
	
	class TargetMethodWrapper:
		"""Contains the method to wrap any target and the state information for a
		particular target"""
		
		def __init__(self, tag, method, methodName, traceMethodCall):
			self.tag = tag
			self.method = method
			self.methodName = methodName
			self.traceMethodCall = traceMethodCall
			
		def wrapped(self,*arguments, **keywords):
			return self.traceMethodCall( self.tag, self.methodName, self.method, arguments, keywords)
	
	
class TraceScannablesMethodCallsProxy(ScannableMotion, TraceObjectsMethodCallsProxy):
	"""Prints and/or logs a scannables method calls using the TraceFunctionCall module"""
	def getObjectTag(self):
		return self.obj.getName()


def createTracedScannable(scannable, traceFunctionCall=None):
	"""A conveniance method to trace only calls to a scannable to do with moving it"""
	from gdadevscripts.developertools.TraceFunctionCall import TraceFunctionCall
#	from gdadevscripts.developertools.TraceObjectsMethodCallsProxy import TraceScannablesMethodCallsProxy
	if traceFunctionCall==None:
		inandout = [ "In", "Out" ]
		inonly = [ "In", ]
		hide = [()]
		options = [ (r'.*Names', hide), 
				(r'getName', hide),
				(r'.*Format', hide),
				(r'at.*', inonly),
				(r'.*', inandout ) ]
		traceFunctionCall = TraceFunctionCall(options, toGdaLog=True)
	
	return TraceScannablesMethodCallsProxy(traceFunctionCall, scannable)

if __name__=='__main__':
	from gdascripts.pd.dummy_pds import DummyPD
	from gdadevscripts.developertools import TraceObjectsMethodCallsProxy
	xval = DummyPD("xval")

	#create the scannable wrapper
	xval_log = TraceObjectsMethodCallsProxy.createTracedScannable(xval)

		

		

	
	