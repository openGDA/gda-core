from gda.factory import Finder
from gda.data.scan.datawriter import NexusDataWriter
#import gda.data.scan.datawriter.NexusDataWriter 
from gda.data.scan.datawriter.NexusDataWriter import getLocationmap
#from sets import Set
from java.util import HashSet  

def setTitle(title):
    """
    Command to set the title that is recorded in the scan file
    """
    Finder.getInstance().find("GDAMetadata").setMetadataValue("title", title)

def getTitle():
    """
    Command to get the title that is recorded in the scan file
    """
    return Finder.getInstance().find("GDAMetadata").getMetadataValue("title")
    
    
def meta_add( farg, *vargs):
    """
    Command to add a scannable to the items to be put into the scan metadata
    """
    metashop=Finder.getInstance().find("metashop")
    metashop.add([farg]+list(vargs))
    return metashop.list(False)
    
#    metashop.ll()
def meta_ll():
    """
    Command to list the items to be put into the scan metadata. The value of the items will also be listed
    """
    metashop=Finder.getInstance().find("metashop")
    return metashop.list(True)

def meta_ls():
    """
    Command to list the items to be put into the scan metadata. 
    """
    metashop=Finder.getInstance().find("metashop")
    return metashop.list(False)

def meta_rm(farg, *vargs):
    """
    Command to remove items to be put into the scan metadata. 
    """
    metashop=Finder.getInstance().find("metashop")
    metashop.remove([farg]+list(vargs))
    return metashop.list(False)

def meta_clear_alldynamical():
    metashop = Finder.getInstance().find("metashop")
    # clear scannables
    #metashop.getMetaScannables().clear()    
    allMetaScannableList = metashop.getMetaScannables()
    for s in allMetaScannableList:
        metashop.remove(s)
    # clear non-scannables
    metashop.clear()
    staticLocationMap = NexusDataWriter.getLocationmap()
    staticMetaScannableList = []
    for k in staticLocationMap.keySet():
        staticMetaScannableList.append(k)
        #print "key = " , k
    NexusDataWriter.setMetadatascannables(HashSet(staticMetaScannableList))
    return metashop.list(False)
    


