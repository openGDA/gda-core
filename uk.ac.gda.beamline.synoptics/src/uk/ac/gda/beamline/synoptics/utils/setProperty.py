from gda.configuration.properties import LocalProperties
from gda.factory import Finder
def setProperty(propertyName, propertyValue):
    LocalProperties.set(propertyName, propertyValue)
    eventAdmin=Finder.getInstance().find("eventadmin")
    if eventAdmin is None:
        print "Cannot find 'eventAdmin' on the GDA server. Please create a Spring bean using 'Scriptcontroller' Java class"
    else:
        eventAdmin.update(propertyName, propertyValue)