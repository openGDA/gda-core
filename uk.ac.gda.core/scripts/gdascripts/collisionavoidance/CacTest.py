from gda.server.collisionAvoidance import CollisionAvoidanceController
from gda.jython.commands.ScannableCommands import pos
reload(CollisionAvoidanceController)
import CacDummyDevices
reload(CacDummyDevices)
import CacCheckers
reload(CacCheckers)

from pprint import pprint

### Setup scannables to play with
# First delete old scannables
try:  
	x=1; y=1; abc=1
except:
	pass
del x; del y; del abc

### Make some dummy checked scannables
x = CacDummyDevices.CheckedSlowDummyClass('x', 1)
y = CacDummyDevices.CheckedSlowDummyClass('y', 1)
abc = CacDummyDevices.Checked3SlowDummyClass('abc',1)
pos

### Make some collisiona avoidance CacCheckers
stubChecker = CacCheckers.StubChecker("stub")

### Make a new CollisionAvoidanceController 
CollisionAvoidanceController.getInstanceFromFinder().clearController()
cac = CollisionAvoidanceController.getInstanceFromFinder()

### Register scannables with CAC and set limits
cac.registerScannable(abc)
cac.registerScannable(x)
cac.registerScannable(y)
cac.setLimits('abc','b',-5,None)
cac.setLimits('abc','c',-15,15)
cac.setLimits('x','x',-10,10)
cac.display()

### Test checkers
print "Testing checkers..."
stubChecker.checkMove([0,1,2],[0.1,.2,.3],[4,5,6],None)

cac.registerChecker(stubChecker,['abc','abc','x'],['a','c','x'])
cac.cacCheckersMap.get('stub').checkMove([1,2,None])
cac.isMoveAllowed('abc',[1,2,3],0)
