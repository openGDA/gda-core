'''
Adapted from mprop.py at https://github.com/josiahcarlson/mprop
'''

import sys, types

debug = False
#debug = True
if debug: from pprint import pprint

def init():
	# Pull the module context in which the init function was called.
	glbls = sys._getframe(1).f_globals
	if debug: pprint(glbls)
	name = glbls['__name__']
	module = sys.modules[name]
	if isinstance(sys.modules[name], types.ModuleType):
		class MagicModule(types.ModuleType):
			def __init__(self):
				types.ModuleType.__init__(self, name)
				self.__path__ = []
				# Remove this modules name.
				del glbls[__name__]
				self.__dict__.update(glbls)
			def __getitem__(self, key):
#				if name in ("*", "__all__"):
#					raise AttributeError("Cannot import * from %s" % __name__)
				try:
					return getattr(self, key)
				except AttributeError:
					raise KeyError(key)
		
		if debug: pprint(module.__dict__)
		
#		# Delete the original module?
#		del sys.modules[name]
		# Create our new module.
		module = MagicModule()
		# Replace the original module with this one.
		sys.modules[name] = module
	else:
		MagicModule = type(module)
	
	# Handle property assignment and global namespace cleanup
	''' Directly assign names to the Module class so that they behave
	like attributes, and remove them from the globals so that they don't
	alias themselves.
	'''
	items = glbls.items()
	for k, v in items:
		del glbls[k]
		setattr(MagicModule, k, v)
	
	return module
