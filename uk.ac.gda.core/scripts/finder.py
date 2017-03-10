'''
>>> from finder import gonx as x, gony as y
>>> x, y
(gonx : 0.0000mm mot(-1.7977e+308:1.7977e+308), gony : 0.0000 mot(-1.7977e+308:1.7977e+308)
'''

import magicmodule

def __getattr__(self, name):
	from gdascripts.parameters.beamline_parameters import FinderNameMapping
	try:
		value = FinderNameMapping()[name]
	except AttributeError:
		value = None
	# equivalent to:	from gda.factory import Finder; value = Finder.getInstance().findNoWarn(name)
	if value is None:
		raise AttributeError("'%s' object has no attribute '%s'" % (__name__, name))
	return value

magicmodule.init()
