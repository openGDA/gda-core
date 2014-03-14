from gda.factory import Finder
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

def meta_clear():
    """
    Command to remove items to be put into the scan metadata. 
    """
    metashop=Finder.getInstance().find("metashop")
    #metashop.getMetaScannables().clear()    
    metadataList = metashop.getMetaScannables()
    for metadata in metadataList:
        metashop.remove(metadata)
    metashop.clear()
    return metashop.list(False)

def meta_enable():
    """
    Command to enable reading out and writing out (to scan file) of meta data at scan start. 
    """
    metashop=Finder.getInstance().find("metashop")
    metashop.setEnabled(True)
    
def meta_disable():
    """
    Command to disable reading out and writing out (to scan file) of meta data at scan start. 
    """
    metashop=Finder.getInstance().find("metashop")
    metashop.setEnabled(False)
    
def meta_enabled():
    """
    Command to find out if reading out and writing out (to scan file) of meta data at scan start is enabled or not. 
    """
    metashop=Finder.getInstance().find("metashop")
    return metashop.isEnabled()
    
    

