from gda.factory import Finder
from scisoftpy.jython.jyhdf5io import SDS
import scisoftpy as dnp
from scisoftpy.jython.jyroi import _roi_wrap
import ast

plotName = "Saxs Plot"

def load():
#     print "\nLoading profiles"
    profileFile = Finder.getInstance().find('detectorInfoPath').getSaxsDetectorInfoPath()
    data = dnp.io.load(profileFile)
    if "entry" in data:
        entry = data.entry
        if "region" in entry:
            loadProfiles(entry.region)
    else:
        print "no 'entry' in file"

def loadProfile(region):
#     print "load profile"
    rois = getRegions(region)
    roiList = dnp.plot.roi_list([(roi.name, roi) for roi in rois])
    if roiList:
        dnp.plot.setrois(roiList, name=plotName)

def getRegions(region):
    rois = []
    for i in region:
        r = region[i]
        try:
            roi = makeRoi(r)
            rois.append(roi)
        except:
            pass
    return rois

def makeRoi(roiData):
    json = roiData.attrs['JSON']
    json = json.replace("true", "True")
    json = json.replace("false", "False")
    rDict = ast.literal_eval(json)
    type = rDict.pop("type","")
    if type == "SectorROI":
        roi = dnp.roi.sector(point=rDict.pop("startPoint",[0,0]), **rDict)
    return roi

if __name__ == "__main__":
    load()