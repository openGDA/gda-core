from gda.server.collisionAvoidance import CollisionCheckerBase

reload(CollisionCheckerBase)

class StubChecker(CollisionCheckerBase):
	'''Stub Checker'''
	def __init__(self, name):
		self.setName(name)

	def isConfigurationPermitted(self, position):
		print "StubChecker.isPositionPermitted(" + str(position) + ") returning okay"
		return None
