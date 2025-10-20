'''
A wrapper class that simplifies temperature controller to enable access each
temperature channel separately using different scannable object.

This is developed to support standard metadata collection of sample temperature
across beamlines, irrespective what lakeshore hardware the beamline uses and setup.

Created on 16 Oct 2025

@author: fy65
'''
from gda.device.scannable import ScannableMotionBase
from gda.device import DeviceException #@UnresolvedImport

class SampleTemperature(ScannableMotionBase):
		'''
		a wrapper scannable class exposes a single lakeshore (340,336) temperature channel,
		output from eurotherm device Eurotherm2K (no channels), or dummy monitor
		'''

		def __init__(self, name, temperature_scannable, channel_number = None):
			self.setName(name)
			self.setScannable(temperature_scannable, channel_number)

		def setScannable(self, temperature_scannable, channel_number = None):
			if temperature_scannable is None:
				self.ts = None
				self.setInputNames([""])
				print("Temperature scannable not set for nxmetadata tsample")
				return
			self.ts = temperature_scannable
			self.ch_no = channel_number
			ts_input_names = self.ts.getInputNames()
			ts_extra_names = self.ts.getExtraNames()
			ts_all_names = ts_input_names + ts_extra_names
			# often input name is same as scannable name, but for monitor input name is null
			_field_name = temperature_scannable.getName()
			# specialt case of lakeshore controller with N channels
			if (ts_extra_names is not None) and (self.ch_no is not None):
				_field_name += "."+ts_all_names[self.ch_no]
			self.setInputNames([_field_name])

		def getPosition(self):
			if self.ts is None:
				self.setOutputFormat(["%s"])
				return "temperature scannable not set"
			if self.ch_no is not None:
				return self.ts.getPosition()[self.ch_no]
			else:
				return self.ts.getPosition()

		def asynchronousMoveTo(self, v):
			raise DeviceException("Move to a position is not implemented in wrapper, please use true {} scannable".format(self.ts.getName()))

		def isBusy(self):
			if self.ts is None:
				return False
			return self.ts.isBusy()