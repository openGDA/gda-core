print "Performing beamline specific initialisation code"

from gdascripts.pd.dummy_pds import DummyPD
from gdascripts.pd.dummy_pds import MultiInputExtraFieldsDummyPD
#from gdascripts.pd.dummy_pds import ZeroInputExtraFieldsDummyPD

from gdascripts.pd.time_pds import showtimeClass
from gdascripts.pd.time_pds import showincrementaltimeClass
from gdascripts.pd.time_pds import waittimeClass

from gdascripts.scannable.timerelated import t, dt, w, clock #@UnusedImport
from gdascripts.scannable.timerelated import TimeOfDay #@UnusedImport
from gdascripts.scan.installStandardScansWithProcessing import * #@UnusedWildImport
scan_processor.rootNamespaceDict=globals()


print "Creating dummy devices x,y and z"
x=DummyPD("x")
y=DummyPD("y")
z=DummyPD("z")

print "Creating timer devices t, dt, and w"
t = showtimeClass("t") # cannot also be driven.
dt= showincrementaltimeClass("dt")
w = waittimeClass("w")


print "Creating multi input/extra field device, mi, me and mie"
mi=MultiInputExtraFieldsDummyPD('mi',['i1','i2'],[])
me=MultiInputExtraFieldsDummyPD('me',[],['e1','e2'])
mie=MultiInputExtraFieldsDummyPD('mie',['i1'],['e2','e3'])

#print "Createing zero input/extra field device, zie"
#zie=ZeroInputExtraFieldsDummyPD('zie')


# Course specific
from simpleDummyScannable import VerboseDummyScannable
xx = VerboseDummyScannable('xx')
yy = VerboseDummyScannable('yy')
zz = VerboseDummyScannable('zz')

from scannableClasses import ScannableGaussian
sg = ScannableGaussian("sg", 0.0)


# python vrml_animator.py sixc.wrl 4567 alpha delta gamma omega chi phi
#execfile('diffcalc/example/startup/sixcircle_dummy.py')
