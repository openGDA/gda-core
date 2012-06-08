# This module must be run rather than imported to work
# >>> lookupLabel['X']
#'x'
#>>> x.getName()
#'X'
from gda.device import Scannable

lookupLabel={}
for label in filter( lambda label : isinstance(eval(label),Scannable) , dir() ):
	lookupLabel[eval(label).getName()]=label


#class LookupLabel:
#	
#	"""An object that can be called like a function, but that contains state.
#	>>> import sys
#	>>> lookupLabel = LookupLabel(sys.__dict__)
#	"""
#	def __init__(self,namespaceDict):
#		from gda.device import Scannable
#		self.xref = {}
#		for label in filter( lambda label : isinstance(eval(label, namespaceDict),Scannable) , dir() ):
#			self.xref[eval(label).getName()]=label
#	
#	def __call__(self,label):
#		return self.xref[label]
#
#import sys
#lookupLabel = LookupLabel(sys.__dict__)

