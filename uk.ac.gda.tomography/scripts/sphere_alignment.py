# Example data at
#/dls/i12/data/2014/cm4963-1/rawdata/35802.nxs
#/dls/i12/data/2014/cm4963-1/rawdata/35803.nxs

import time
import scisoftpy as dnp
from uk.ac.diamond.scisoft.analysis.fitting import EllipseFitter as _efitter
from uk.ac.diamond.scisoft.analysis.dataset import AbstractDataset as absd

print "Running sphere alignment"
# scan flat field

print "Moving to flatfield position" # this should be an optional step as it may not be possible to do this move without disrupting user equipment

pos ss1.x -200

print "Collecting flat field image"
scan ix 0 1 1 pco4000_dio_hdf 0.4
working_dir = wd()
current_file = cfn()
filename = '%s/%d.nxs' % (working_dir, current_file)

print "Loading flat field image to data processing pipeline"
data = dnp.io.load(filename)
ff = data['entry1']['pco4000_dio_hdf']['data'][...].mean(0)

dnp.plot.image(ff)

print "Moving to sample position
pos ss1.x -1

print "Collecting projections"
scan p2r_rot 0 360 20 pco4000_dio_hdf 0.4
working_dir = wd()
current_file = cfn()
filename = '%s/%d.nxs' % (working_dir, current_file)

time.sleep(5)

print "Loading projections into data processing pipeline"
data = dnp.io.load(filename)
dd = data['entry1']['pco4000_dio_hdf']['data']


print "Processing data"

# apply any filtering
# exclude spheres which are not completely in the field of view

print "Generating centroid data"
xs = []
ys = []
for i in range(dd.shape[0]):
    cor = dd[i,:,:]*1.0/ff
    cor = cor[10:-1,:]<0.4
    dnp.plot.image(cor)
    x,y = dnp.centroid(cor)
    xs.append(x)
    ys.append(y)

xxx = absd.createFromList(ys)
yyy = absd.createFromList(xs)

print "Fitting ellipse to centroid data"
f = _efitter()
f.algebraicFit(xxx,yyy)
f.geometricFit(xxx,yyy,f.parameters)

t = dnp.arange(100)*dnp.pi/50.
t = absd.createFromList(t.tolist())
fitplot = _efitter.generateCoordinates(t, f.parameters)

dnp.plot.line(fitplot[0], fitplot[1])

ellipseFitValues = f.parameters

print "Results of ellipse fitting routine:"
print "Major: %f" % (ellipseFitValues[0])
print "Minor semi-axis: %f" % (ellipseFitValues[1])
print "Major axis angle: %f" % (ellipseFitValues[2])
print "Centre co-ord 1: %f" % (ellipseFitValues[3])
print "Centre co-ord 2: %f" % (ellipseFitValues[4])

print "Calculating stage tilts"
xangle = math.atan2(ellipseFitValues[1], ellipseFitValues[0])
xangle = math.degrees(xangle)

zangle = math.degrees(ellipseFitValues[2]) - 180

print xangle
print zangle

#inc ss1.rz zangle 
#inc ss1.rx xangle 