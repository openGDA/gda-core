'''This module to bring in scannables from pd package
'''
from gdascripts.pd.dummy_pds import DummyPD, MultiInputExtraFieldsDummyPD, ZeroInputExtraFieldsDummyPD
from gda.device.scannable import ScannableBase

class SingleInputDummy(DummyPD):
	pass
	
class MultiInputExtraFieldsDummy(MultiInputExtraFieldsDummyPD):
	pass

class ZeroInputExtraFieldsDummy(ZeroInputExtraFieldsDummyPD):
	pass

class SingleInputStringDummy(ScannableBase):
	'''Dummy PD Class'''
	def __init__(self, name):
		self.name = name
		self.inputNames = [name]
		self.outputFormat = ['%s']
		self.level = 3
		self.currentposition = 'initialised'
		
	def isBusy(self):
		return 0

	def rawAsynchronousMoveTo(self,new_position):
		self.currentposition = str(new_position)

	def rawGetPosition(self):
		return self.currentposition