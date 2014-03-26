import scisoftpy as dnp
import math
from gda.jython.commands.Input import requestInput

print "Running camera to slit alignment"

print "Opening the slits"
# open the slits
pos s2.ys 15

print "Collecting flat field images" #provide option for this to be optional
scan ix 0 1 1 pco4000_dio_hdf 0.4
working_dir = wd()
current_file = cfn()
filename = '%s/%d.nxs' % (working_dir, current_file)

print "Loading flat field image to data processing pipeline"
data = dnp.io.load(filename)
ff = data['entry1']['pco4000_dio_hdf']['data'][...].mean(0)

dnp.plot.image(ff)

print "Moving slits into field of view"
pos s2.ys 1


print "Collecting projection"
scan ix 0 1 1 pco4000_dio_hdf 0.4
working_dir = wd()
current_file = cfn()
filename = '%s/%d.nxs' % (working_dir, current_file)


print "Loading projection to data processing pipeline"
data = dnp.io.load(filename)
dd = data['entry1']['pco4000_dio_hdf']['data'][...].mean(0)

res = dd/ff

res = res[10:-1,:]


#res = javaImage.medianFilter(res, [3, 3])
#

dnp.plot.image(res)

res = res > 0.5

dnp.plot.image(res)

print "Calculting coms"

points = []
for i in range(res.shape[1]):
    a = res[:,i:i+1].mean(1)
    points.append(dnp.centroid(a))

coms = dnp.array(points)
coms.squeeze()
dnp.plot.line(coms)

p = dnp.fit.polyfit(dnp.arange(coms.shape[0]), coms, 1, full=True)

p[1].plot()

rotation = math.degrees(math.atan2(p[0][0],1.))

responce = requestInput("Would you like to rotate the camera stage by %f" % (rotation))

if responce == 'y' :
    print "Moving cam1.roll"
    pos cam1.roll
    inc cam1.roll rotation
    pos cam1.roll
    print "In position."
    


