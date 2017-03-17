'''
>>> from beamlineParameters import parked_z
>>> parked_z # automatically converted to a float
-49.3
'''

import magicmodule

def __getattr__(self, name):
	from gdascripts.parameters.beamline_parameters import Parameters
	value = Parameters()[name]
	if value is None:
		raise AttributeError("'%s' object has no attribute '%s'" % (__name__, name))
	else:
		try:
			value = float(value)
		except:
			pass
	return value

magicmodule.init()
