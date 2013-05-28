import uk.ac.gda.excalibur.config.model.util.ExcaliburConfigModelHelper
import uk.ac.gda.excalibur.config.model.util.NexusTreeHelper
from gda.factory import Finder
'''file extension is excaliburconfig'''
def getExcaliburConfigModelFromDetector():
    c = Finder.getInstance().find("excalibur_config")
    print "excalibur Config read from detector" + `c`
    nodes = c.get("nodes")
    print "nodes present" + `nodes`
    return uk.ac.gda.excalibur.config.model.util.ExcaliburConfigModelHelper.INSTANCE.createExcaliburConfig(nodes)

'''file extension is excaliburconfig'''
def createModelAndSaveToFile(fileName):
    exconf = getExcaliburConfigModelFromDetector()
    print "created persistent exalibur configuration " + `exconf`
    saveToFile(fileName, exconf)

def saveToFile(fileName, exconf):
    print "Saving to file :" + `fileName`
    uk.ac.gda.excalibur.config.model.util.ExcaliburConfigModelHelper.INSTANCE.saveToXML(fileName, exconf)
    print "Import the file or refresh the folder to see the file:" + `fileName`

def getExcaliburConfigFromFile(fileName):
    return uk.ac.gda.excalibur.config.model.util.ExcaliburConfigModelHelper.INSTANCE.getExcaliburConfigFromFile(fileName)

def getShortArray1():
    sarr = []
    for n in range(1, 257):
        for m in range(1, 257):
            sarr.append(m)
    return sarr

def getShortArray2():
    sarr = []
    for n in range(1, 257):
        for m in range(1, 257):
            sarr.append(n)
    return sarr

def getNexusTree(exconf):
    data = uk.ac.gda.excalibur.config.model.util.NexusTreeHelper.INSTANCE.saveToDetectorData("excalibur", exconf)
    return data.getNexusTree()

def getSummaryNodeModel():
    summ = Finder.getInstance().find("excalibur_summary")
    summaryFem = summ.get("fem")
    return uk.ac.gda.excalibur.config.model.util.ExcaliburConfigModelHelper.INSTANCE.createSummaryAdBaseModel(summaryFem)

def createExcaliburConfigFromNexusTree(tree):
    return uk.ac.gda.excalibur.config.model.util.NexusTreeHelper.INSTANCE.createModelFromNexus(tree)

def sendToDetector(exconf):
    c = Finder.getInstance().find("excalibur_config")
    print "excalibur Config read from detector" + `c`
    nodes = c.get("readoutFems")
    print "Sending to detector"
    uk.ac.gda.excalibur.config.model.util.ExcaliburConfigModelHelper.INSTANCE.sendToExcalibur(nodes, exconf)

def sendConfigInFileToDetector(filename):
    excaliburConfig = getExcaliburConfigFromFile(filename)
    if excaliburConfig != None:
        uk.ac.gda.excalibur.config.model.util.ExcaliburConfigModelHelper.INSTANCE.reloadResource(excaliburConfig.eResource())
        sendToDetector(excaliburConfig)

def reloadConfig(exconf):
    res = exconf.eResource()
    if res != None:
        uk.ac.gda.excalibur.config.model.util.ExcaliburConfigModelHelper.INSTANCE.reloadResource(res)
