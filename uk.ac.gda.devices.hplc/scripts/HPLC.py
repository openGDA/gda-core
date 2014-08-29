class HPLC(object):
    def __init__(self, filename):
        self.hplcFile = filename
    
    def run(self):
        print "working with %s" % self.hplcFile