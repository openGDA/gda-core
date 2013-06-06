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
    metashop=Finder.getInstance().find("metashop")
    metashop.add([farg]+list(vargs))
    return metashop.list(False)
    
#    metashop.ll()
def meta_ll():
    metashop=Finder.getInstance().find("metashop")
    return metashop.list(True)

def meta_ls():
    metashop=Finder.getInstance().find("metashop")
    return metashop.list(False)

def meta_rm(farg, *vargs):
    metashop=Finder.getInstance().find("metashop")
    metashop.remove([farg]+list(vargs))
    return metashop.list(False)