#@PydevCodeAnalysisIgnore
## USER COMMANDS

# Extended syntax
for i in range(1,12):
    print i,

pos(x)
pos x

# What's available
help
help x
help scan
help []
pos
ls
ls Scannable
run "hello_world"


# Scannable commands
pos
x
pos x
pos x 4
inc x 1
x()
pos y x()*2


# Basic scan construction
scan x 1 5 1
scan x 1 5 1 y
scan x 1 5 1 y 2
scan x 1 5 1 y 10 .1
scan x 1 3 1 y 10 13 1 z
scan x (1 2 4 8)
scan x 1 2 1 y (1 2 4 8) 

#s Other scans
pos x 10
rscan x -3 3 1
pos x
cscan x 1 .1

# Scan Plotting
sg.width
scan sg -5 5 .1

sg.width = 2
scan sg -5 5 .1

sg.width = 4
scan sg -5 5 .1 z

# Demo/exercise 2b
pos xx
pos xx 1.234
scan xx 1 3 1 yy 4 zz
level yy 6
scan xx 1 3 1 yy 4 zz

# Scan Data Processors
scan sg -5 5 .1
go maxval
peak
print peak.result.fwhm




### 3. USER SCRIPTING ###

run "hello_world"

# Exercise 3
for w in (1,2,4,8,16):
    print "Setting width to ", w
    sg.width = w
    scan sg -5 5 .1


# 4. WRITING SCANNABLES

x.asynchronousMoveTo(2)
x.getPosition()
x.isBusy()


# Aside: Extending Classes

# E.g. Dummy Scannables
from simpleDummyScannable import SimpleDummyScannable
ds=SimpleDummyScannable('ds', 10)
ds=SimpleDummyScannable('ds', 10)
del ds
ds=SimpleDummyScannable('ds', 10)

# Exercise 4a
# Fails to bring in change:
from simpleDummyScannable import SimpleDummyScannable
del ds
ds=SimpleDummyScannable('ds', 10)

# reload the *module*
import simpleDummyScannable
reload(simpleDummyScannable)
from simpleDummyScannable import SimpleDummyScannable
ds=SimpleDummyScannable('ds', 10)
del ds
ds=SimpleDummyScannable('ds', 10)

run "simpleDummyScannable" #BAD
del ds
ds=SimpleDummyScannable('ds', 10)
p # BAD
SimpleDummyScannable

# Multinput Scannable
from threeInputScannable import hklScannable
hkl = hklScannable('hkl')
pos hkl [1, 2, 3]
scan hkl [0 0 0] [10 20 0] [1 2 0]

# Extra fields
mie
pos mie 1
scan mie 1 5 1
mie.getInputNames()
mie.getExtraNames()
type(mie)

#TwoCircle.py
from twoCircle import TwoCircle
twoc = TwoCircle('twoc', x, y)
pos twoc 1.23
scan twoc 1 5 1

### 5. MORE SCRIPTING ###
# Script folders
from pprint import pprint
import sys
pprint(sys.path)

#Aliasing
def p(x):
    print x

alias p
p 2
p 'abc'
p [1 2 -3]

dir()

reset_namespace

# Persistance
from gda.util.persistence import LocalParameters
config = LocalParameters.getXMLConfiguration()?
config = LocalParameters.getXMLConfiguration()
config.setProperty("test",10)
config.save()
config.getInt("test")


### ANALYSIS ###

#Dataset usage
import scisoftpy as dnp
help(dnp)
a = dnp.array([0.2, 1, 2, -2.3])
print a
a.shape
a[0]
a[0:2] # slice a dataset
2*a # new dataset
a += 1.2 # same dataset

b = dnp.arange(4) # integer dataset (for float, use 4.)
print b
b.shape = 2,2 # change to 2x2 dataset
b[0,1] # (0,1) is row-0, column-1 item
b[1] # partial index is like a slice, b[1,:]

c = dnp.zeros((3,2)).fill(3) # create 3x2 dataset of floats and fill it with 3
c + dnp.ones((3,2), dtype=dnp.int32) # note: result is of higher  type
dnp.ones((2,3,4), dtype=dnp.int16) # create 2x3x4 dataset of 16-bit integers

# Plotting 1D
import math
xds = dnp.linspace(0,math.pi,100) # 100 values between 0 and pi
xds = dnp.linspace(0,math.pi,100) # 100 values between 0 and pi
help(dnp.plot)
dnp.plot.plot(xds, yds)
x2, y2 = dnp.meshgrid(xds,xds)
im = dnp.exp(-(x2**2 + y2**2))

dnp.plot.plot(xds, im[0])
dnp.plot.image(im, xds, xds)
dnp.plot.surface(im, xds, xds)
dnp.plot.stack(xds, [im[0], im[10], im[20]])

# File loading and fitting
import scisoftpy as dnp
data = dnp.io.load("config/users/data/1415.dat", asdict=True, formats=[“srs”])?
data.keys()
dnp.plot.line(data["finepitch"],data["c3"])?

#Multiple peak fitting
data = dnp.io.load("config/users/data/4815.dat", asdict=True, formats=[“srs”])?
xdata = data["s2ycentre"]
ydata = data["cc3"]
result = dnp.fit.fit([dnp.fit.function.gaussian, dnp.fit.function.gaussian, dnp.fit.function.offset], xdata, ydata, [0, 1, 4, 0, 1, 4, 2], [(-1, 1), (0,2), (0,8), (-1, 1), (0,2), (0,8), (0.0, 4)])
result.plot()

