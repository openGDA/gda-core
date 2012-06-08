import gda
from gda.util.userOptions import UserOptions

def getUserOptions():
    return UserOptions.getUserOptions()
def getUserOption(key, default):
    options = getUserOptions()
    if options.containsKey(key):
        return options.get(key).value
    else:
        return default
def getAutomaticProcessing():
    return getUserOption("AutomaticProcessing",True)
