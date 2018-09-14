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

mscan <axes> <Roi> <Roi Params> <AreaScanpath> <AreaScanpath Params> <Detectors/Monitors>

where:

<axes> can be two Scannables or two element ScannableGroup
<Roi> can be rect (rectangle)
             crec (centred_rectangle)
             circ (circle)
             poly (polygon)
    N.B. the full name or abbreviation can be used
<Roi Params> are the numeric parameters for the specified Roi
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

The default Roi and AreaScanpath are rectangle and raster and these will be
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

import sys
import gda.mscan.element.AreaScanpath as AreaScanpath
import gda.mscan.element.Roi as Roi
import gda.mscan.element.Mutator as Mutator

from gda.jython.commands.GeneralCommands import alias
from gda.mscan import MScanSubmitter
from org.eclipse.scanning.command.Services import *

# Set up lambdas that return the AreaScanpath Enum instances that link to the
# Models and assign them to the corresponding names and standard 4 character
# abbreviations so that they can be protected using alias()

grid = (lambda:AreaScanpath.GRID)()
rast = raster = (lambda:AreaScanpath.RASTER)()
spir = spiral = (lambda:AreaScanpath.SPIRAL)()
liss = lissajous = (lambda:AreaScanpath.LISSAJOUS)()

# Register the commands with the Translator
alias('grid')
alias('rast')
alias('raster')
alias('spir')
alias('spiral')
alias('liss')
alias('lissajous')

# Set up functions that return the Roi Enum instances and assign them to
# the corresponding names and standard 4 character abbreviations so that
# they can be protected using alias()

rect = rectangle = (lambda:Roi.RECTANGLE)()
crec = centred_rectangle = (lambda:Roi.CENTRED_RECTANGLE)()
circ = circle = (lambda:Roi.CIRCLE)()
poly = polygon = (lambda:Roi.POLYGON)()

# Register the commands with the Translator
alias('rect')
alias('rectangle')
alias('crec')
alias('centred_rectangle')
alias('circ')
alias('circle')
alias('poly')
alias('polygon')

# Set up functions that return Mutator Enum instances and assign them to
# the corresponding names and standard 4 character abbreviations so that
# they can be protected using alias()

snak = snake = (lambda:Mutator.SNAKE)()
roff = random_offset = (lambda:Mutator.RANDOM_OFFSET)()

# Register the commands with the Translator
alias('snak')
alias('snake')
alias('roff')
alias('random_offset')

# The Java entry point for MScan commands, creates the scan builder and
# submits the command string via the GeneralTranslator.
def mscan(*args):
    builder = MScanSubmitter(getEventService(), getRunnableDeviceService())
    builder.buildAndSubmitBlockingScanRequest(args)

# Register the mscan command with the Translator
alias('mscan')