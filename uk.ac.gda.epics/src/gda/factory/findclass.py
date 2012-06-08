
from xml.dom import minidom


def getClassNames(node):
    classNames=[]
    for item in node.childNodes:
        if item.nodeType == item.ELEMENT_NODE:
            if item.tagName=="class":
                classNames.append(item.getAttribute("name"))
    return classNames

def foundInList(list, item):
    allowed=False    
    try:
        list.index(item)
        allowed = True
    except:
        pass
    return allowed

allowedClassNames=[]
f=file("allowedClasses")
lines = f.readlines()
for line in lines:
    allowedClassNames.append(line.strip())
f.close()

dom1 = minidom.parse('/scratch/gda/trunk/plugins/uk.ac.gda.core/src/gda/factory/mapping.xml')
classNames=[]
for subnode in dom1.childNodes:
    if subnode.tagName=="mapping":
        for item in subnode.childNodes:
            if item.nodeType == item.ELEMENT_NODE:
                if item.tagName=="class":
                    className = item.getAttribute("name")
                    if foundInList(allowedClassNames, className):
                        subnode.removeChild(item)

f = open('/scratch/gda/trunk/plugins/uk.ac.gda.epics/src/gda/factory/mapping.xml','w')
dom1.writexml(f)
f.close()

allowedClassNames.append("gda.factory.ObjectFactory")
allowedClassNames.append("gda.device.DeviceBase")
allowedClassNames.append("gda.gui.AcquisitionPanel")
allowedClassNames.append("gda.device.detector.countertimer.CounterTimerBase")
allowedClassNames.append("gda.device.temperature.TemperatureBase")
allowedClassNames.append("gda.device.motor.MotorBase")
allowedClassNames.append("gda.device.detector.analyser.AnalyserBase")
allowedClassNames.append("gda.device.detector.DetectorBase")


dom1 = minidom.parse('/scratch/gda/trunk/plugins/uk.ac.gda.core/src/gda/factory/mapping.xml')
classNames=[]
for subnode in dom1.childNodes:
    if subnode.tagName=="mapping":
        for item in subnode.childNodes:
            if item.nodeType == item.ELEMENT_NODE:
                if item.tagName=="class":
                    className = item.getAttribute("name")
                    if not foundInList(allowedClassNames, className):
                        subnode.removeChild(item)

f = open('/scratch/gda/trunk/plugins/uk.ac.gda.epics/src/gda/factory/mapping_epics.xml','w')
dom1.writexml(f)
f.close()

