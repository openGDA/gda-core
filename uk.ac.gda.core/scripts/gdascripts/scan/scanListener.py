class ScanListener(object):
	
	def prepareForScan(self):
		pass # just a hook
	
	def update(self,scanObject):
		raise RuntimeError("Not implemented")
