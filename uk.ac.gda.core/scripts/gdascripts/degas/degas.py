#------------------------------------------------------------------------------------------------------------------
# Script to degas a device
#
# Move a device (e.g. single slit blade, diagnostic stick) gradually into the beam,
# by an amount calculated using a PID formula (see en.wikipedia.org/wiki/PID_controller).
# However, to avoid pressure spikes, the speed of moving into the beam
# is limited to <maxForwardMovement>.
#
# If the pressure exceeds <maxPressure>, return the device to its starting position, close
# the front end and terminate the script.
# Ideally, we should set the parameters so that this never happens.
#
# If the ring current goes below <minRingCurrent>, assume the beam is off and wait for it to recover.
#
# The script assumes that the front end is open and that the device has been
# positioned manually at a suitable starting position. When it terminates, it will
# close the front end and return the device to the starting position.
#
# Pressures are in millibars, distances in millimetres
#
# Constructor arguments:
#  mandatory:
#    motor:              the motor that moves the device
#    motorMax:           end position for the motor i.e. when the device is fully in the beam
#    gauge:              the pressure gauge to monitor
#    frontend:           the front end shutter
#    ringCurrent:        monitor for ring current
#
#  optional:
#    minPressure:        the pressure below which we assume no significant outgassing is taking place
#    targetPressure:     optimum gas pressure for degassing
#    pressureDeadband:   if the pressure is only slightly above or below the target pressure, don't move the device
#    maxPressure:        maximum gas pressure we allow before terminating the script and closing the front end
#    maxForwardMovement: limit the distance that the device can move into the beam in one movement
#                        This is designed to prevent sudden pressure as the beam reaches an unconditioned part of the device.
#                        There is no limit on movement out of the beam.
#------------------------------------------------------------------------------------------------------------------

import sys
from exceptions import KeyboardInterrupt
from time import sleep, gmtime, strftime
from gda.jython import ScriptBase

class Degas:
    def __init__(self, motor, motorMax, gauge, frontend, ringCurrent, 
                 minPressure = 5e-9, targetPressure = 3e-8, pressureDeadband = 0.2e-8, maxPressure = 5e-8,
                 maxForwardMovement = 0.05):
        self.motor = motor
        self.motorMax = float(motorMax)
        self.gauge = gauge
        self.frontend = frontend
        self.ringCurrent = ringCurrent

        self.minPressure = minPressure
        self.targetPressure = targetPressure
        self.pressureDeadband = pressureDeadband
        self.maxPressure = maxPressure
        self.maxForwardMovement = maxForwardMovement 

        # The following values have been set by experimentation on i21
        # Please be careful if you intend to change them

        # PID factors
        self.Kp = 5e7
        self.Ki = 10000
        self.Kd = 0.3
        self.Derivator = 0
        self.Integrator = 0
        self.Integrator_max = 500
        self.Integrator_min = -500
        
        # Deduce direction of travel from current and end positions
        self.initialPosition = self.motor.getPosition()
        if (self.motorMax > self.initialPosition):
            self.direction = 1
        else:
            self.direction = -1
        
        # Frequency of monitoring pressure (seconds)
        self.monitorFreq = 0.1
        
        # Minimum number of monitoring cycles before moving
        self.minBladeMoveCycles = 100
        
        # Ring current below which we assume there is no beam (mA)
        self.minRingCurrent = 50
        
        # Number of cycles between reporting ring current too low
        self.ringCurrentReportCycles = 100


    # Check whether the device is at its maximum position, taking account
    # of its direction of travel.
    def atBladeMax(self):
        position = self.motor.getPosition()
        if (self.direction > 0):
            if (position >= self.motorMax):
                return True
        else:
            if (position <= self.motorMax):
                return True
        return False
    
    
    # Update position, based on current & target pressures
    def updatePosition(self, pressure):
        error = self.targetPressure - pressure

        self.Derivator = error
        self.Integrator = self.Integrator + error
        self.Integrator = min(self.Integrator, self.Integrator_max)
        self.Integrator = max(self.Integrator, self.Integrator_min)
        
        # Don't move if pressure is within the deadband
        if (abs(error) < self.pressureDeadband):
            self.printMessage("pressure " + str(pressure) + ", not moving device")
            return

        # Calculate PID
        self.P_value = self.Kp * error
        self.D_value = self.Kd * (error - self.Derivator)
        self.I_value = self.Integrator * self.Ki
        PID = self.P_value + self.I_value + self.D_value
        
        # Limit the forward (into beam) movement
        if (PID > 0):
            PID = min(PID, self.maxForwardMovement) 

        currentPosition = self.motor.getPosition()
        newPosition = round(currentPosition + (PID * self.direction), 3)
    
        # Don't try to move the device beyond its maximum (fully in beam) position 
        if (self.direction > 0):
            newPosition = min(newPosition, self.motorMax)
        else:
            newPosition = max(newPosition, self.motorMax)
        
        if (newPosition == currentPosition):
            self.printMessage("pressure " + str(pressure) + ", not moving device (at max position)")
        else:
            self.printMessage("pressure " + str(pressure) + ", moving device to " + str(newPosition))
            self.motor.moveTo(newPosition)
        

    def run(self):
        self.report()
        cyclesBeforeMove = 0
        cyclesBeforeRingCurrentReport = 0
        finished = False
        
        try:
            while (finished == False):
                pressure = self.gauge.getPosition()
                ringCurrent = self.ringCurrent.getPosition()
                
                if (ringCurrent < self.minRingCurrent):
                    if (cyclesBeforeRingCurrentReport > 0):
                        cyclesBeforeRingCurrentReport = cyclesBeforeRingCurrentReport - 1
                    else:
                        self.printMessage("ring current " + str(ringCurrent) + " mA too low: waiting")
                        cyclesBeforeRingCurrentReport = self.ringCurrentReportCycles - 1
                    
                elif (pressure > self.maxPressure):
                    self.printMessage("pressure too high: terminating script")
                    finished = True
                        
                elif (pressure < self.minPressure and self.atBladeMax()):
                    self.printMessage("outgassing finished: terminating script")
                    finished = True
                    
                elif (cyclesBeforeMove > 0):
                    cyclesBeforeMove = cyclesBeforeMove - 1
                    
                else:
                    self.updatePosition(pressure)
                    cyclesBeforeMove = self.minBladeMoveCycles - 1
                    
                sleep(self.monitorFreq)
            
        except KeyboardInterrupt:
            self.printMessage("script terminated by user")
            
        except:
            self.printMessage("script terminated by exception: " + str(sys.exc_info()[0]))

        finally:
            try:
                self.printMessage("moving device back to initial position")
                self.motor.asynchronousMoveTo(self.initialPosition)
            except:
                self.printMessage("exception moving device: " + str(sys.exc_info()[0]))
            finally:
                self.printMessage("closing front end")
                self.frontend.moveTo('Close')
                self.report()


    def printMessage(self, message):
        print strftime("%Y-%m-%d %H:%M:%S", gmtime()), message


    def report(self):
        print ""
        print "--------------- Degas ------------------------"
        print self.motor
        print "initialPosition : ", self.initialPosition
        print "motorMax : ", self.motorMax
        print "maxForwardMovement : ", self.maxForwardMovement
        print "direction : ", self.direction
        print ""
        print self.gauge
        print self.frontend
        print self.ringCurrent
        print ""
        print "minPressure : ", self.minPressure
        print "targetPressure : ", self.targetPressure
        print "pressureDeadband : ", self.pressureDeadband
        print "maxPressure : ", self.maxPressure
        print ""
        print "Kp : ", self.Kp
        print "Ki : ", self.Ki
        print "Kd : ", self.Kd
        print "--------------------------------------------------"
        print ""
