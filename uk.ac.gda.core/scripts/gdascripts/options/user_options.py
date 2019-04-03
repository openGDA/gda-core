from gda.factory import Finder
from gda.util.userOptions import UserOptionsManager

def getUserOptionsManager():
	manager = Finder.getInstance().findSingleton(UserOptionsManager)
	if not manager:
		manager = UserOptionsManager()
		manager.configure()
	
	return manager


def getUserOptions():
	return getUserOptionsManager().createOptionsMapFromTemplate()


def getUserOption(key, default):
	options = getUserOptions()
	if options.containsKey(key):
		return options.get(key).value
	else:
		return default


def getAutomaticProcessing():
	return getUserOption("AutomaticProcessing", True)

