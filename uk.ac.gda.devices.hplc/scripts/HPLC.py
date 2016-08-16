from uk.ac.gda.devices.hplc.beans import HplcSessionBean
from gda.commandqueue import JythonScriptProgressProvider
from time import sleep

class HPLC(object):
    def __init__(self, filename):
        self.hplcFile = filename
        self.bean = HplcSessionBean.createFromXML(filename)
    def run(self, processing=True):
        if processing == "true": processing = True
        elif processing == "false": processing = False
        print "working with %s" % self.hplcFile
        print "processing is %s" %processing
        for i, b in enumerate(self.bean.measurements):
            print b.sampleName
            JythonScriptProgressProvider.sendProgress(100*i/float(len(self.bean.measurements)), "Processing %s" %b.sampleName)
            sleep(2)
