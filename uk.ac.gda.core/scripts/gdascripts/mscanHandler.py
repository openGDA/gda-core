###
# Copyright (c) 2018 Diamond Light Source Ltd.
#
# All rights reserved. This program and the accompanying materials
# are made available under the terms of the Eclipse Public License v1.0
# which accompanies this distribution, and is available at
# http://www.eclipse.org/legal/epl-v10.html
#
#
###

"""
A friendly interface to mapping scans.

Basic Syntax is of the form:

mscan [<axes> <RegionShape> <RegionShape Params>]* [<Scanpath> <Scanpath Params>]^ <Detectors/Monitors> <ScanDataConsumers>

* When defining a Static/Acquire scan, these arguments should be excluded
^ When defining a Static/Acquire scan, these arguments are optional (unless requiring multiple exposures)

where:

<axes> can be two Scannables or two element ScannableGroup
<RegionShape> can be rect (rectangle)
             crec (centred_rectangle)
             circ (circle)
             poly (polygon)
             line (line) -- a line in 2D space
             poin (point)
             axis (axial) -- a line along a scannable axis
    N.B. the full name or abbreviation can be used
<RegionShape Params> are the numeric parameters for the specified RegionShape
<Scanpath> can be grid (grid_points)
                      rast (raster/grid_step)
                      spir (spiral)
                      liss (lissajous)
                      step (line_step)
                      nopt (line_points)
                      poin (point)
                      axst (axis_step)
                      axno (axis_points)
                      stat (static/acquire) [a scan in which no motors are moved, only exposures taken]
    N.B. the full name or abbreviation can be used, other aliases are also provided see below)
    
    In order to try to be consistent and standardised and move away from poorly chose pathnames
    link raster (which actually means step along each axis) and grid (which actually means no
    of points along each axis) the line_step and line_points paths can be used for the axis roi
    and for the rectangular, circular and polygonal rois. So the following a valid:
    
    mscan S1 axis 0 10 step 1...             (an axis_step scan along the S1 axis with step size 1)
    mscan S1 axis 0 10 pts 10...             (an axis_points scan along the S1 axis with 10 points)
    mscan S1 S2 rect 0,0 1,1 step 0.1 0.2... (a 'raster' scan in S1 and S2 with step sizes of 0.1 and 0.2 along S1 and S2)
    mscan S1 S2 circ 0,0 1 pts 10 11...      (a 'grid' scan (inside a circle) in S1 and S2 with a max of 10 points along 
                                              S1 and 11 along S2) 
    
<Scanpath Params> are the numeric parameters for the specified Scanpath
<Detectors/Monitors> are a list of Detectors, Monitors or IRunnableDevices
<ScanDataConsumers> are lists of per-scan monitors, nexus templates or processor:config filename pairs 

N.B. At the moment IRunnableDevice detectors are not supported natively and must
have an accompanying Scannable based Detector in the beamline config with a
matching name to be used.

The default RegionShape and Scanpath are rectangle and raster and these will be
selected if none are specified in the command e.g.

mscan sc1 sc2 0,0 5,5 0.5,0.5 d1

is the same as

mscan sc1 sc2 rect 0,0 5,5 rast 0.5,0.5 d1

commas are not required but may be added between axis params to improve 
readability.

If no non-detector Scannables are passed, and the default Scanpath is instead a Static scan with a single exposure, 
e.g.

mscan det <det_params>
mscan static det <det_params>
mscan static 1 det <det params>

are all the same. 

mscan static <size> det <det_params> 

Would take <size> exposures with det, and requires calling the static scanpath explicitly.

ScanDataConsumer parameters are space separated lists enclosed in single or double quotes, e.g.

mscan s1 s...........det1 <det_params> psms "mon1 mon2"

Full details of syntax etc. can be found at https://confluence.diamond.ac.uk/x/wjEoBQ or 
https://alfred.diamond.ac.uk/documentation/manuals/GDA_User_Guide/master/scanning/mapping_scan_command.html

To enable the functionality:
1. Make sure any references to importing "mapping_scan_commands.py" are removed
from your localstation.py or any other scripts
2. Add the line "from gdascripts.mscanHandler import *" to your localstation.py
3. Start the GDA server or run reset_namespace if it's already active.

"""

import gda.mscan.element.Scanpath as Scanpath
import gda.mscan.element.RegionShape as RegionShape
import gda.mscan.element.Mutator as Mutator
import gda.mscan.element.ScanDataConsumer as ScanDataConsumer
import gda.mscan.element.Action as Action

from java.util import HashMap, List
from gda.jython.commands.GeneralCommands import alias
from gda.mscan import MScanSubmitter
from org.eclipse.dawnsci.analysis.api.roi import IROI
from org.eclipse.scanning.api.points.models import IScanPointGeneratorModel
from org.eclipse.scanning.api.scan.models import ScanMetadata
import org.eclipse.scanning.api.scan.models.ScanMetadata.MetadataType as MetadataType
from org.eclipse.scanning.command.Services import getEventService
from org.eclipse.scanning.command.Services import getRunnableDeviceService
from org.eclipse.scanning.command.Services import getScannableDeviceService
from org.eclipse.scanning.sequencer import ScanRequestBuilder


submitter = MScanSubmitter(getEventService(), getRunnableDeviceService())

def initialise_global_variables_for(mscanEnumTypes):
    """
    Function to set up variables that return the instances of the passed in array of
    Enum types and assign them to the corresponding names and standard 4 character
    abbreviations so that they can be protected using alias()
    """
    for enumType in mscanEnumTypes:
        for (key, value) in enumType.termsMap().items():
            globals()[key] = value
            alias(key)

def mscan(*args):
    """
    The Java entry point for MScan commands, creates the scan builder and
    submits the command string via the GeneralTranslator.
    """
    builder = MScanSubmitter(getEventService(), getRunnableDeviceService())
    builder.buildAndSubmitBlockingScanRequest(args)
    
def get_scannable(name):
    """
    Retrieve a Scannable by name
    """
    return getScannableDeviceService().getScannable(name)
    
def submit(scan_request, block=True, name=None):
    """
    Submit an existing ScanRequest to the GDA server. use block to control
    whether a Blocking Submit is used or not
    """
    submitter.submit(scan_request, block, name)
    
def scan_request(path=None, region=None, monitors_per_point_names=[], monitors_per_scan_names=[], detectors=[], metadata=[], file_path=None, allow_preprocess=False, proc=None):
    """
    Create a ScanRequest object with the given configuration.
    Accepts a single path/region combination
    """
    try:
        assert path is not None
        assert isinstance(path, IScanPointGeneratorModel)
    except AssertionError:
        raise ValueError('Scan request must have a scan path.')
    
    try:
        assert region is not None
        assert isinstance(region, IROI)
    except AssertionError:
        raise ValueError('Scan request must have a region.')
    
    if type(detectors) is not list:
        detectors = [detectors]
        
    detector_map = HashMap()    
    for (name, model) in detectors:
        detector_map[name] = model

    return ScanRequestBuilder()\
        .withPathAndRegion(path, region)\
        .withDetectors(detector_map)\
        .withScanMetadata(List.of(metadata))\
        .withMonitorNamesPerPoint(List.of(monitors_per_point_names))\
        .withMonitorNamesPerScan(List.of(monitors_per_scan_names))\
        .withFilePath(file_path).ignorePreprocess(not allow_preprocess)\
        .withProcessingRequest(proc)\
        .build()

def detector(name, exposure, **kwargs):
    """
    The detector method returns a dictionary of detector name to the detector model.
    You must specific the detector name and exposure time
    You may optionally add keyword arguments which if set will
    call the appropriate setter methods on the detector model. For instance
    if you want to set 'enableNoise' in the model you would have a keyword argument
    enableNoise=True so the detector function would be detector("mandelbrot", 0.1, enableNoise=True)
    """

    detector = getRunnableDeviceService().getRunnableDevice(name)

    assert detector is not None, "Detector '"+name+"' not found."

    model = detector.getModel()
    assert model is not None, "The model of detector '"+name+"' appears to be None."

    if (exposure > 0):
        model.setExposureTime(exposure)

    for key, value in kwargs.iteritems():
        setattr(model, key, value)

    return (name, model)

def sample(**kwargs):
    """
    Include key, value pairs to be written in to the sample region of the file.
    
    For example:
    >>> sample(name="Fe",id=12345)
    will return a ScanMetadata object with the expected content
    """
    md = ScanMetadata(MetadataType.SAMPLE)
    for key, value in kwargs.iteritems():
        md.addField(key,value)
    return md

# Register the mscan command with the Translator
alias('mscan')

initialise_global_variables_for([Scanpath, RegionShape, Mutator, ScanDataConsumer, Action])
