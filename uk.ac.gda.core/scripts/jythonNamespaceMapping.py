'''
>>> from jythonNamespaceMapping import BeamLineEnergy_eV
>>> BeamLineEnergy_eV
BeamLineEnergy_Bragg_Gap_eV : 25205.20eV (5000.00:25000.00)
'''

import magicmodule

def __getattr__(self, name):
	from gdascripts.parameters.beamline_parameters import JythonNameSpaceMapping
	value = JythonNameSpaceMapping()[name]
	# equivalent to:	import __main__; value = getattr(__main__, name)
	if value is None:
		raise AttributeError("'%s' object has no attribute '%s'" % (__name__, name))
	return value

magicmodule.init()
