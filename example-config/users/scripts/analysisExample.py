#create the data holder object
data = ScanFileHolder()

#load a file from the data directory
data.loadSRS("1415.dat")

#try the following:
data.plot("finepitch","c3")
c3_data=data.getDataSet("c3")
c3_data.min()
new_c3_data = c3_data - c3_data.min()
data.plot("finepitch",new_c3_data)
data.autoFit("finepitch","c3",[Gaussian(0,1,100,50)])
