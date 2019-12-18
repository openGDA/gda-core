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

mscan <axes> <RegionShape> <RegionShape Params> <AreaScanpath> <AreaScanpath Params> <Detectors/Monitors>

where:

<axes> can be two Scannables or two element ScannableGroup
<RegionShape> can be rect (rectangle)
             crec (centred_rectangle)
             circ (circle)
             poly (polygon)
    N.B. the full name or abbreviation can be used
<RegionShape Params> are the numeric parameters for the specified RegionShape
<AreaScanpath> can be grid (grid)
                      rast (raster)
                      spir (spiral)
                      liss (lissajous)
    N.B. the full name or abbreviation can be used
<AreaScanpath Params> are the numeric parameters for the specified AreaScanpath
<Detectors/Monitors> are a list of Detectors, Monitors or IRunnableDevices

N.B. At the moment IRunnableDevice detectors are not supported natively and must
have an accompanying Scannable based Detector in the beamline config with a
matching name to be used.

The default RegionShape and AreaScanpath are rectangle and raster and these will be
selected if none are specified in the command e.g.

mscan sc1 sc2 0,0 5,5 0.5,0.5 d1

is the same as

mscan sc1 sc2 rect 0,0 5,5 rast 0.5,0.5 d1

commas are not required but may be added between axis params to improve 
readability.

To enable the functionality:
1. Make sure any references to importing "mapping_scan_commands.py" are removed
from your localstation.py or any other scripts
2. Add the line "from gdascripts.mscanHandler import *" to your localstation.py
3. Start the GDA server or run reset_namespace if it's already active.

"""

import gda.mscan.element.AreaScanpath as AreaScanpath
import gda.mscan.element.RegionShape as RegionShape
import gda.mscan.element.Mutator as Mutator
import gda.mscan.element.ScanDataConsumer as ScanDataConsumer

from gda.jython.commands.GeneralCommands import alias
from gda.mscan import MScanSubmitter
from org.eclipse.scanning.command.Services import getEventService
from org.eclipse.scanning.command.Services import getRunnableDeviceService

# Set up lambdas that return the AreaScanpath Enum instances that link to the
# Models and assign them to the corresponding names and standard 4 character
# abbreviations so that they can be protected using alias()

grid = AreaScanpath.GRID_POINTS
rast = raster = AreaScanpath.GRID_STEP
spir = spiral = AreaScanpath.SPIRAL
liss = lissajous = AreaScanpath.LISSAJOUS
step = angl = angle = AreaScanpath.LINE_STEP
nopt = pts = noofpoints = points = proj = projections = AreaScanpath.LINE_POINTS
axst = axisstep = AreaScanpath.AXIS_STEP
axno = axispoints = AreaScanpath.AXIS_POINTS

# Register the commands with the Translator
alias('grid')
alias('rast')
alias('raster')
alias('spir')
alias('spiral')
alias('liss')
alias('lissajous')
alias('step')
alias('angl')
alias('angle')
alias('nopt')
alias('pts')
alias('noofpoints')
alias('points')
alias('projections')
alias('proj')
alias('axst')
alias('axisstep')
alias('axno')
alias('axispoints')


# Set up functions that return the RegionShape Enum instances and assign them to
# the corresponding names and standard 4 character abbreviations so that
# they can be protected using alias()

rect = rectangle = RegionShape.RECTANGLE
crec = centred_rectangle = RegionShape.CENTRED_RECTANGLE
circ = circle = RegionShape.CIRCLE
poly = polygon = RegionShape.POLYGON
line = RegionShape.LINE
poin = point = pt = RegionShape.POINT
axis = RegionShape.AXIAL

# Register the commands with the Translator
alias('rect')
alias('rectangle')
alias('crec')
alias('centred_rectangle')
alias('circ')
alias('circle')
alias('poly')
alias('polygon')
alias('line')
alias('poin')
alias('point')
alias('pt')
alias('axis')


# Set up functions that return Mutator Enum instances and assign them to
# the corresponding names and standard 4 character abbreviations so that
# they can be protected using alias()

snak = snake = alte = alternating = Mutator.ALTERNATING
roff = random_offset =  Mutator.RANDOM_OFFSET
cont = continuous =  Mutator.CONTINUOUS

# Register the commands with the Translator
alias('snak')
alias('snake')
alias('alte')
alias('alternating')
alias('roff')
alias('random_offset')
alias('cont')
alias('continuous')

temp = templates = ScanDataConsumer.TEMPLATE
proc = processors = ScanDataConsumer.PROCESSOR

alias('temp')
alias('templates')
alias('proc')
alias('processors')

# The Java entry point for MScan commands, creates the scan builder and
# submits the command string via the GeneralTranslator.
def mscan(*args):
    builder = MScanSubmitter(getEventService(), getRunnableDeviceService())
    builder.buildAndSubmitBlockingScanRequest(args)

# Register the mscan command with the Translator
alias('mscan')