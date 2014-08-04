from mpl_toolkits.mplot3d import Axes3D
import numpy as np
from matplotlib import cm
import matplotlib
matplotlib.use('Agg')
import matplotlib.pyplot as plt

#set up plot
fig = plt.figure()
ax=fig.gca(projection='3d')

#set up data
import h5py
f=h5py.File("/dls/b21/data/2014/cm4976-3/b21-12434.nxs.22967.reduction/output/background_b21-12433_detector_260614_132314.nxs",'r')
#f=h5py.File("/dls/b21/data/2014/cm4976-1/processing/Mark/results_b21-7591_detector_050314_135807.nxs")
reducedDataArray = f["/entry1/detector_processing/Normalisation/data"]
qArray=f["/entry1/detector_result/q"]
x=qArray
y=np.arange(0,len(reducedDataArray[0]))
x,y = np.meshgrid(x,y)
z=np.log(reducedDataArray[0])
surf=ax.plot_surface(x,y,z,cmap=cm.coolwarm,linewidth=0,antialiased=True)
fig.colorbar(surf, shrink=0.5, aspect=5)
plt.show()
fig.savefig("surface12434.png")
fig.clf()
