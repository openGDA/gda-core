#!/bin/env dls-python

# RasterGenerator class
# A class to generate waveforms and trigger points for a PI E727 controller
# controlling a XY stage with 100 um range in both axes
# This works in conjunction with the PIpiezo support module which provides
# PVs for uploading the commands to the controller
# Giles Knap 14/02/2014

import platform

try:
    import argparse
except ImportError:
    if platform.system() != 'Java':
        raise

from pkg_resources import require, DistributionNotFound
try:
    require("cothread")
    from cothread.catools import caput
    from cothread.catools import DBR_CHAR_STR
except DistributionNotFound:
    if platform.system() != 'Java':
        raise

import math


def enum(**enums):
    return type('Enum', (), enums)

cmdTypes = enum(setup=1, start=2, stop=3)

# global constants. Note that this code generates waveforms for a 2D XY stage
# with 100 micrometer by 100 micrometer range
xsize = 100
ysize = 100
wavepoints = 2000
servocycle = 0.00005


class RasterGenerator():
    def __init__(self, _rate, _xmin, _xmax, _ymin, _ymax, _rows=-1, _cols=-1):
        rate = float(_rate)
        xmin = float(_xmin)
        xmax = float(_xmax)
        ymin = float(_ymin)
        ymax = float(_ymax)
        rows = int(_rows)
        cols = int(_cols)
        # note cols and rows are inclusive of 1st point
        if cols == -1:
            cols = xmax - xmin + 1
        if rows == -1:
            rows = ymax - ymin + 1

        #verify the parameters
        if rate > 100:
            raise(Exception("Error: rate too high. Motor lag approaches .5"
                            " microns over 20Hz"))
        if xmax < xmin or \
            ymax < ymin or xmax > xsize or ymax > 100 or ymin < 0:
            raise(Exception("Error: invalid range parameters"))

        # initialise members
        self.commands = ""
        self.startCmd = ""
        self.stopCmd = ""

        # copy init parms into class variables
        self.__dict__.update(locals())

        # constant values for wave table generation ...
        # number of wave table points to use in one row: it is possible to use
        # a one to one mapping between micrometers and points,
        # HOWEVER I found lower numbers meant that demand position did
        # not reach the requested value even when increasing WTR
        # BUT lower numbers would save memory and allow more complex waveforms
        # so this deserves further investigation
        self.points = wavepoints

        # calculate values for wave table generation
        # step size in motor micrometers (note that there is one less interval
        # than there is columns/rows so hence the - 1
        self.xstepSize = (self.xmax - self.xmin) / ( float(self.cols) - 1)
        self.ystepSize = (self.ymax - self.ymin) / ( float(self.rows) - 1)

        if self.xmin < self.xstepSize / 2.0 or \
            self.xmax > xsize - self.xstepSize / 2.0:
            raise(Exception("range of scan must leave space for 1/2 step each"
                            " side of the scan"))
        else:
            # move the X start and end points out by half a step to accommodate
            # triggers which happen half step before reaching a point ---
            # acquisition is then assumed to continue to the next half
            # step point
            self.xmin -= self.xstepSize / 2.0
            self.xmax += self.xstepSize / 2.0


        # eguTo pts is a conversion factor from microns to steps in the wavetable
        self.eguToPts = self.points / (self.xmax - self.xmin)
        # waveTableRate sets the no. of servocycles per step in the wavetable
        # this controls the rate the wavetable is executed at
        self.waveTableRate = ((self.xmax - self.xmin) / self.rate /
                              servocycle / self.points)

    def add(self, command, cmdtype=cmdTypes.setup):
        if cmdtype is cmdTypes.setup:
            self.commands += command + '\n'
        elif cmdtype == cmdTypes.start:
            self.startCmd += command + '\n'
        elif cmdtype == cmdTypes.stop:
            self.stopCmd += command + '\n'

    def createStartCommands(self):
        # reset offset on axes 2 waveform to 0 (this accumulates with each row)
        self.add("WOS 2 0", cmdTypes.start)
        # return all motors to 0 position
        self.add("MOV 1 0", cmdTypes.start)
        self.add("MOV 2 0", cmdTypes.start)
        self.add("MOV 3 0", cmdTypes.start)
        # start the wave generators 1 and 2. 2 set to start from previous pos
        self.add("WGO 1 1 2 257", cmdTypes.start)

    def createStopCommands(self):
        # put wave generator 1 in stop mode
        self.add("WGO 1 0 2 0", cmdTypes.stop)
        # stop all motors
        self.add("STP", cmdTypes.stop)

    def createCommands(self):
        self.createStartCommands()
        self.createStopCommands()

        # set servo state to closed loop on all 3 axes
        self.add("SVO 1 1")
        self.add("SVO 2 1")
        self.add("SVO 3 1")

        # create waveform for axis 1 (X)
        #    linear ascend from min to max over n points
        #    linear return to min over 0.1 * n points
        flybackPoints = self.points * .1
        flybackTime = flybackPoints * .9
        # command format is as follows (X = create new waveform, & = append)
        # WAV <axis> X <SegLength> <Amplitude> <Offset> <WaveLength>
        #         <StartPoint> <SpeedUpDown>
        # the Offset is the start point and the amplitude the change in
        # relative position from that point over the life of the LIN waveform
        self.add("WAV 1 X LIN %d %f %f %d 0 0" %
                 (self.points, self.xmax - self.xmin, self.xmin, self.points))
        self.add("WAV 1 & LIN %d %f %f %d 0 0" %
                 (flybackPoints, -(self.xmax - self.xmin), self.xmax,
                  flybackTime))

        # create waveform for axis 2 (Y)
        #    linear hold position for n points
        #    linear set one step size over 0.1 * n points
        self.add("WAV 2 X LIN %d 0 %d %d 0 0" %
                 (self.points, self.ymin, self.points))
        self.add("WAV 2 & LIN %d %f 0 %d 0 0" %
                 (flybackPoints, self.ystepSize, flybackTime))

        # set wave table rate - each point in the wave table corresponds to
        # the servo update time of 0.0001 secs when WTR is 1. Increase WTR to
        # slow the scan. Also set record table rate to match
        self.add("WTR 0 %d 1" % self.waveTableRate)
        self.add("RTR %d" % self.waveTableRate)
        # connect the wave tables to wave generators for axes 1 and 3
        self.add("WSL 1 1")
        self.add("WSL 2 2")
        # set the number of cycles the wave generator performs
        self.add("WGC 1 %d" % self.rows)

        # clear all wave table triggers
        self.add("TWC")
        # set triggers to be tied to wave generators
        self.add("CTO 1 3 4")

        # add the points for triggering output 1
        # Note rows and cols here refer to rows and cols of command strings
        # you are only allowed 10 parms per line in the command interface
        # the trigger points are for a single row of X positions
        start = self.xmin
        stop = self.xmax - self.xstepSize / 2.0
        pos = start
        for row in range(int(math.ceil(self.cols / 10.0))):  # @UnusedVariable
            cmdr = "TWS "
            for col in range(10):  # @UnusedVariable
                # note the +1 because the triggers table is indexed from 1
                nextpos = ((pos - start) * self.eguToPts + 1)
                cmdr += "1 %4d 1 " % nextpos
                # DEBUG cmdr += "   %4d  %6.3f" % (nextpos, pos)
                pos += self.xstepSize
                if round(pos, 10) > stop:
                    # DEBUG print "pos %f, rounded %f, stop %f, xstep %f" % (pos, round(pos, 10), stop, self.xstepSize )
                    break
            self.add(cmdr)

        # set up data recorder to capture demand and actual position for 3 axes
        self.add("DRC 1 1 2")
        self.add("DRC 2 2 2")
        self.add("DRC 3 3 2")
        self.add("DRC 4 1 1")
        self.add("DRC 5 2 1")
        self.add("DRC 6 3 1")

    def uploadCommands(self, prefix):
        print "uploading commands to PVs with prefix %s" % prefix
        # potentially we could enable these PVs before setting them and this
        # would update the GUI nicely HOWEVER - when the records process
        # they are sent to the controller via the stream protocol file
        # there is no way to suppress this for a waveform hooked to streamdevice
        # so we leave these disabled

#         caput("%s:WFSETUP:WR.DISA" % prefix, 0, timeout=1)
#         caput("%s:WFSTART:WR.DISA" % prefix, 0, timeout=1)
#         caput("%s:WFSTOP:WR.DISA" % prefix, 0, timeout=1)
        caput("%s:WFSETUP:WR" % prefix,
              self.commands, timeout=1, datatype=DBR_CHAR_STR)
        caput("%s:WFSTART:WR" % prefix,
              self.startCmd, timeout=1, datatype=DBR_CHAR_STR)
        caput("%s:WFSTOP:WR" % prefix,
              self.stopCmd, timeout=1, datatype=DBR_CHAR_STR)

    def printCommands(self):
        print ("parms: x1=%(xmin)f x2=%(xmax)f y1=%(ymin)f y2=%(ymax)f "
               "rows=%(rows)d cols=%(cols)d xstep=%(xstepSize)f "
               "ystep=%(ystepSize)f eguToPts=%(eguToPts)f"
               % self.__dict__)
        print
        print 'Setup Commands ...'
        print self.commands
        print
        print 'Start Commands ...'
        print self.startCmd
        print
        print 'Stop Commands ...'
        print self.stopCmd
        print


def generateOptionParser():
    usage = """
%(prog)s [options]

%(prog)s generates the commands for a PI E725 controller to perform a 2D raster
scan using waveforms and also to output triggers at points within this scan.

NOTE with default tuning the performance is approximately as follows:-

100Hz scan rate = position lags demand by less than 2 microns
20Hz scan rate = position lags demand by less than .4 microns
10Hz scan rate = position lags demand by less than .2 microns
5Hz scan rate = position lags demand by less than .1 microns

It then uploads these commands to the controller's driver.
"""
    parser = argparse.ArgumentParser(usage=usage)
    parser.add_argument("-o", "--output", action="store_true",
                        help="prints the text of the commands to stdout instead of uploading to IOC")
    parser.add_argument("-p", "--prefix",
                        help="PREFIX for PVs of the IOC e.g. BL16I-EA-PIEZO-01:C1",
                        default="BL16I-EA-PIEZO-01:C1")
    parser.add_argument("-r", "--rows", type=int, default="-1",
                        help="number of rows in scan, defaults to 1um steps if not specified")
    parser.add_argument("-c", "--cols", type=int, default="-1",
                        help="number of columns in scan, defaults to 1um steps if not specified")
    parser.add_argument("-hz", "--rate", type=int, default="10",
                        help="Rate in triggers / second")
    parser.add_argument("-x1", "--xstart", type=float, default="1",
                        help="Start position of horizontal scan")
    parser.add_argument("-x2", "--xstop", type=float, default="99",
                        help="Stop position of horizontal scan")
    parser.add_argument("-y1", "--ystart", type=float, default="0",
                        help="Start position of vertical scan")
    parser.add_argument("-y2", "--ystop", type=float, default="100",
                        help="Stop position of vertical scan")
    return parser


def parseOptions():
    parser = generateOptionParser()
    options = parser.parse_args()
    return options


def main():
    options = parseOptions()
    gen = RasterGenerator(options.rate, options.xstart, options.xstop,
                          options.ystart, options.ystop, options.rows,
                          options.cols,)
    gen.createCommands()

    if options.output:
        gen.printCommands()
    else:
        gen.uploadCommands(options.prefix)


if __name__ == '__main__':
    main()

