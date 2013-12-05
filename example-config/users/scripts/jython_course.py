import scisoftpy as dnp
sg = scannableGaussian0
sg1 = scannableGaussian1

scan sg -1.0 1.0 0.1
data = dnp.io.load("<File name from the beginning of the scan>")
data  # you should see the contents of the file shown here
data.keys()  # this shows the items which can be picked to display from the file
x = data['entry1']['default']['x'][...]    # gets the x data from the structure. 
y = data['entry1']['default']['y'][...]   # The […] extracts the lazy loaded data.
dnp.plot.line(x,y)                

scan sg -1.0 1.0 0.1
data2 = dnp.io.load("<File name from the beginning of the scan>")
y2=data2['entry1']['default']['y'][...]
dnp.plot.line(x,[y,y2])

ds = y - y2
ds.max()
ds.min()
dnp.plot.line(x,ds)

dnp.plot.line(x,[y-y2])

help(dnp)
help(dnp.plot)

scan sg -1.0 1.0 .1 sg1 -1.0 1.0 0.1

data3 = dnp.io.load("<File name from the beginning of the scan>")
data3
image = data3['entry1']['default']['y'][...]
image
image.shape=[21,21]
image

dnp.plot.image(image)

dnp.plot.line(image[0,:])
dnp.plot.line(image[:,0])
dnp.plot.line(data[‘y1’][0:21],[image[:,0],image[:,1]])

dnp.plot.surface(image)
help(dnp.plot)

dss = scannableSine
scan sg -5.0 5.0 0.1
scan dss -5.0 5.0 0.1

d1 = dnp.io.load(“<File name from the beginning of the scan>", formats=["srs"])
d1y = d1['entry1']['default']['y'][...]
d2 = dnp.io.load(“<File name from the beginning of the scan>", formats=["srs"])
d2y = d2['entry1']['default']['y'][...]

r1 = dnp.random.rand(101)
r2 = dnp.random.rand(101)

a = d1y+d2y+r1*0.1
b = d2y+r2*0.1
x = d1['entry1']['default']['x'][...]
dnp.plot.plot(x,[a,b])
dnp.plot.plot(x,[a,b,a-b])


coords = d1['entry1']['default']['x'][...]
fr = dnp.fit.fit([dnp.fit.function.gaussian, dnp.fit.function.offset], coords, a-b, [0.5,0.1,0.1,0.1], [(-5,5), (0,10), (0.,10.), (-10,10)],optimizer='global')
fr.plot()
fr.parameters
fr[0]
fr.residual

d3x = data3['entry1']['default']['x'][...]
d3y = data3['entry1']['default']['y'][...]
ds = dnp.fft.fft(d3y)
dnp.plot.line(d3x,[ds.real,ds.imag])
dnp.plot.line(d3x,[dnp.fft.fftshift(ds.real),dnp.fft.fftshift(ds.imag)])

scan sg -1 1 0.001
data4 = dnp.io.load("<File name from the beginning of the scan>”)
d4y = data4['entry1']['default']['y'][...]
d4x = data4['entry1']['default']['x'][...]
ds = dnp.fft.fft(d4y)
dnp.plot.line(d4x,[d4y,dnp.fft.ifft(ds).real])
ds[500:1500] = 0
dnp.plot.line(d4x,[d4y,dnp.fft.ifft(ds).real])
ds[250:1750] = 0
dnp.plot.line(d4x,[d4y,dnp.fft.ifft(ds).real])
ds[50:1950] = 0


filter = dnp.arange(1,-1,(-2./2001.))
dnp.plot.line(d4x,filter)
filter = dnp.abs(filter)
dnp.plot.line(d4x,filter)

dnp.plot.line(d4x,[d4y,dnp.fft.ifft(ds*dnp.power(filter,1)).real])
dnp.plot.line(d4x,[d4y,dnp.fft.ifft(ds*dnp.power(filter,2)).real])
