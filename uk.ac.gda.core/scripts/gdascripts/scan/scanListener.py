class ScanListener(object):

	def getContext(self):
		return self.scan_command

	def setContext(self, scan_command):
		self.scan_command = scan_command

	def prepareForScan(self):
		pass # just a hook

	def update(self,scanObject):
		raise RuntimeError("Not implemented")
