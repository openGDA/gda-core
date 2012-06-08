from gda.device.scannable.scannablegroup import ScannableGroup
from gda.device.scannable import ScannableBase
import gda.jython.commands.ScannableCommands.pos
from gda.jython import ScriptBase

allScannablesInNamespace=[]
allSGMembersInNamespace=[]

#loop through the current namespace
try:
    # clear the flag at the start of this method
    ScriptBase.interrupted = False
    
    for ii in dir(): 
        thisevaluatedscannable = eval(ii)
        
        # add all scannables to the array
        if isinstance(thisevaluatedscannable, ScannableBase):
            allScannablesInNamespace.append(ii)
            
            # add all ScannableGroupMembers to another array
            if isinstance(thisevaluatedscannable, ScannableGroup):
                for thisname in thisevaluatedscannable.getGroupMemberNames():
                    allSGMembersInNamespace.append(thisname)
    
    #remove duplicate entries
    for name in allSGMembersInNamespace:
        if allScannablesInNamespace.__contains__(name):
            allScannablesInNamespace.remove(name)
    
    #call pos command on all unqiue entries
    for scannable in allScannablesInNamespace:
        # test that no panic stop has occurred since this method started
        ScriptBase.checkForPauses()
        try:
            print gda.jython.commands.ScannableCommands.pos(eval(scannable))
        except:
            pass

finally:
    # cleanup namespace
    try:
        del(thisevaluatedscannable)
    except:
        pass
    try:
        del(allScannablesInNamespace)
    except:
        pass
    try:
        del(allSGMembersInNamespace)
    except:
        pass
    
