from uk.ac.gda.devices.hplc.beans import HplcSessionBean

class HPLC(object):
    def __init__(self, filename):
        self.hplcFile = filename
        self.bean = HplcSessionBean.createFromXML(beanFile)
    def run(self, processing=True):
        if processing == "true": processing = True
        elif processing == "false": processing = False
        print "working with %s" % self.hplcFile
        print "processing is %s" %processing