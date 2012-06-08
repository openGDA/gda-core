# Before moving in hkl space, must calculate UB matrix
 # crystal lattice paramters - B matrix
# find two reflections - to determine U matrix

# to setup an orientation matrix with two reflections

helpub

# start new UB calculation
newub 'b16_270608'

# set lattice paramters
setlat 'xtal' 3.8401 3.8401 5.43072 90 90 90

# set sigma and tau
sigtau 0 0

# set energy
pos en 10

# add first reflection
pos sixc [5.000, 22.790, 0.000, 1.552, 22.400, 14.255]
addref 1 0 1.0628 'ref101'

# add second reflection
pos sixc [5.000, 22.790, 0.000,4.575, 24.275, 101.320]
addref 0 1 1.0628 'ref011'

# will calculate UB matrix
showref
ub

####################################################################

# move position
helphkl
hklmode
hklmode 1

# simulate move

sim hkl [1 0 0]

# do real move
pos hkl [1 0 0]
pos hkl
pos sixc
 
# scan h
hklmode 1
pos hkl [1 0 0]
scan h .9 1.1 .02 sixc
scan h .9 1.1 .02 hklverbose sixc
 
# Energy scan at fixed reflection
scan en 9 10 .1 hklverbose [1 0 0] sixc
