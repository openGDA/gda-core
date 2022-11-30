'''
Created on 18 Dec 2009

@author: fy65
'''

from hexapod import Hexapod,HexapodAxis

# Input data must be replaced by PV names of your beamline hexapod device

# the root PV name for the device, must be set to beamline value"
deviceName="ME02P-MO-BASE-01"

x_inputPV=deviceName+":UCS_X"
x_readbackPV=deviceName+":UCSXR"
x_tolerance=0.01
y_inputPV=deviceName+":UCS_Y"
y_readbackPV=deviceName+":UCSYR"
y_tolerance=0.01
z_inputPV=deviceName+":UCS_Z"
z_readbackPV=deviceName+":UCSZR"
z_tolerance=0.01
c_inputPV=deviceName+":UCS_C"
c_readbackPV=deviceName+":UCSCR"
c_tolerance=5.00
b_inputPV=deviceName+":UCS_B"
b_readbackPV=deviceName+":UCSBR"
b_tolerance=5.00
a_inputPV=deviceName+":UCS_A"
a_readbackPV=deviceName+":UCSAR"
a_tolerance=5.00
startPV=deviceName+":START.PROC"

#Hexapod controller internal use only
hpcontroller=Hexapod("hpcontroller", x_inputPV, x_readbackPV, x_tolerance, y_inputPV, y_readbackPV, y_tolerance, z_inputPV, z_readbackPV, z_tolerance, c_inputPV, c_readbackPV, c_tolerance, b_inputPV, b_readbackPV, b_tolerance, a_inputPV, a_readbackPV, a_tolerance)

hpx=HexapodAxis('hpx', x_inputPV, x_readbackPV, startPV, x_tolerance, 'mm', '%9.4f', hpcontroller)
hpy=HexapodAxis('hpy', y_inputPV, y_readbackPV, startPV, y_tolerance, 'mm', '%9.4f', hpcontroller)
hpz=HexapodAxis('hpz', z_inputPV, z_readbackPV, startPV, z_tolerance, 'mm', '%9.4f', hpcontroller)
hpc=HexapodAxis('hpc', c_inputPV, c_readbackPV, startPV, c_tolerance, 'mm', '%9.4f', hpcontroller)
hpb=HexapodAxis('hpb', b_inputPV, b_readbackPV, startPV, b_tolerance, 'mm', '%9.4f', hpcontroller)
hpa=HexapodAxis('hpa', a_inputPV, a_readbackPV, startPV, a_tolerance, 'mm', '%9.4f', hpcontroller)

#just a handy instance to return all axis' values
hexapod=[hpx, hpy, hpz, hpc, hpb, hpa]

