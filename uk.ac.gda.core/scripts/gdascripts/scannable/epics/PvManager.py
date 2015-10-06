from gda.epics import CAClient

class PvManager(object):

	def __init__(self, pvnames=[], pvroot = ""):
		self.pvnames_at_init = pvnames
		self.pvroot = pvroot
		self.clients = {}
		# dict
		if type(pvnames) is dict:
			for key, pvname in pvnames.iteritems():
				self.clients[key] = self._newCAClient(pvname)
		# list
		else:
			try:
				pvnames = tuple(pvnames)
			except TypeError:
				pvnames = (pvnames,)
			
			for pvname in pvnames:
				self.clients[pvname] = self._newCAClient(pvname)

	def __repr__(self):
		return "%s(pvnames=%r, pvroot=%r)" % (self.__class__.__name__, self.pvnames_at_init, self.pvroot)

	def __getitem__(self, key):
		try:
			return self.clients[key]
		except KeyError:
			pvname = key
			self.clients[pvname] = self._newCAClient(pvname)
			self.clients[pvname].configure()
			return self.clients[pvname]
	
	def _newCAClient(self, pvname):
		return CAClient(self.pvroot + pvname)

	def configure(self):
		for client in self.clients.values(): client.configure()
	
	def unconfigure(self):
		for client in self.clients.values(): client.unconfigure()
	
	def caput(self, *args):
		"""If only one pv registered caput to this, other wise raise Exception"""
		if len(self.clients)==1:
			self.clients.values()[0].caput(*args)
		else:
			raise Exception("caput can only be used if only one pv has been configured. Use: pvs['key'].caput(val) instead")
	
	def caget(self):
		"""If only one pv registered caget fromt this, other wise raise Exception"""
		if len(self.clients)==1:
			return self.clients.values()[0].caget()
		else:
			raise Exception("caget can only be used if only one pv has been configured. Use: pvs['key'].caget(val) instead")

from mock import Mock

class PvManagerWithMockCAClients(PvManager):
	def _newCAClient(self, pvname):
		mock = Mock()
		mock.pvName = self.pvroot + pvname
		return mock


#class ManagedCAClient(CAClient):
#	"""
#	Extends CAClient so that if caput or caget fails, owner.configure() is called and the command
#	is tried once more.
#	"""
#	def __init__(self, key, pvname, owner):
#		del key
#		self.owner = owner
#		CAClient.__init__(self, pvname)
#	
#	def caput(self, *args):
#		try:
#			CAClient.caput(self, *args)
#		except NotConnected:
#			self.owner.configure()
#			CAClient.caput(self, *args)
#
#		def caget(self, *args):
#			try:
#				CAClient.caget(self)
#			except NotConnected:
#				self.owner.configure()
#				return CAClient.caget(self)
