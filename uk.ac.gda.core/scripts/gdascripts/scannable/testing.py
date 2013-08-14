import time

import java.util.concurrent.Callable

from gda.device.scannable import ScannableBase
from gda.device.scannable import PositionCallableProvider

VERBOSE = True

class TimeBomb(ScannableBase):
	
	def __init__(self, name, move_time=0, asynch_trigger=None, wait_while_busy_trigger=None):
		self.name = name
		self.inputNames = [name]
		self.outputFormat =['%6.4f']
		
		self.asynch_trigger = asynch_trigger
		self.wait_while_busy_trigger = wait_while_busy_trigger
		self.move_time = move_time

		self._current_position = 0.0
		self._target_position = 0.0

	def rawAsynchronousMoveTo(self, target):
		if VERBOSE:
			print " " * 40 + self.name + ".rawAsynchronousMoveTo(%r)" % target
		if target == self.asynch_trigger:
			raise Exception("Request to move to asynch_trigger: %r" % target)
		self._target_position = float(target)

	def waitWhileBusy(self):
		if VERBOSE:
			print " " * 40 + self.name + ".waitWhileBusy()"
		time.sleep(self.move_time / 2.0)
		if self._target_position == self.wait_while_busy_trigger:
			raise Exception("Request to move to wait_while_busy_trigger: %r" % self._target_position)
		time.sleep(self.move_time / 2.0)
		self._current_position = self._target_position
		if VERBOSE:
			print " " * 40 + self.name + ".waitWhileBusy() exit"

	def isBusy(self):
		return False

	def rawGetPosition(self):
		if VERBOSE:
			print " " * 40 + self.name + ".rawGetPosition()"
		return self._current_position


class DelayedCallableBomb(java.util.concurrent.Callable):
	
	def __init__(self, delay, index, raise_exception):
		self._delay = delay
		self._index = index
		self._raise_exception = raise_exception
		
	def call(self):
		if self._raise_exception:
			raise Exception("Calling point specified by call_index_trigger: %r" % self._index)
		try:
			time.sleep(self._delay)
		except:
			print " " * 40 + "Callable %i interrupted while sleeping (and rethrowing)" % self._index
		return self._index


class SlowPositionCallableProviderBomb(ScannableBase, PositionCallableProvider):
	
	def __init__(self, name, call_index_trigger=None):
		self.name = name
		self.inputNames = [name]
		self.outputFormat =['%6.4f']

		self._current_position = 0.0
		self._last_point = 0
		self.call_index_trigger = call_index_trigger
		
	def atScanStart(self):
		self._last_point = 0

	def rawAsynchronousMoveTo(self, target):
		self._current_position = float(target)

	def waitWhileBusy(self):
		return

	def isBusy(self):
		return False

	def rawGetPosition(self):
		return self._current_position
	
	def getPositionCallable(self):
		self._last_point += 1
		trigger = self._last_point == self.call_index_trigger
		msg = " that will raise exception" if trigger else ""
		if VERBOSE:
			print " " * 40 + self.name + ".getPositionCallable() --> %r" % self._last_point + msg
			
		return DelayedCallableBomb(self._current_position, self._last_point, trigger)