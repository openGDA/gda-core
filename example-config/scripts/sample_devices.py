from gdascripts.pd.dummy_pds import DummyPD
from gdascripts.pd.dummy_pds import MultiInputExtraFieldsDummyPD
from gdascripts.pd.dummy_pds import ZeroInputExtraFieldsDummyPD

from gdascripts.pd.time_pds import showtimeClass
from gdascripts.pd.time_pds import showincrementaltimeClass
from gdascripts.pd.time_pds import waittimeClass

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

print "Createing zero input/extra field device, zie"
zie=ZeroInputExtraFieldsDummyPD('zie')
