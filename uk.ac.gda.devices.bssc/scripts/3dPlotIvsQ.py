from mpl_toolkits.mplot3d import Axes3D
import numpy as np
from matplotlib import cm
import matplotlib
matplotlib.use('Agg')
import matplotlib.pyplot as plt
import sys

if __name__ == '__main__':

	import argparse
	parser = argparse.ArgumentParser()
	parser.add_argument("--filename", type=str, help="filename of input Nexus file after data reduction")
	parser.add_argument("--output", type=str, help="output file name")
	parser.add_argument("--datapath", type=str, help="path for reduced data in Nexus file", default="/entry1/detector_processing/Normalisation/data")
	parser.add_argument("--qpath", type=str, help="Q path name in Nexus file", default = "/entry1/detector_result/q")
	args = parser.parse_args()

	if args.filename:
		filename = args.filename
	else:
		print "filename must be defined"
		sys.exit(1)

	if args.output:
		output = args.output
	else:
		print "output filename must be defined"
		sys.exit(1)

	if args.datapath:
		datapath = args.datapath

	if args.qpath:
		qpath = args.qpath

	#set up plot
	fig = plt.figure()
	ax=fig.gca(projection='3d')
	
	#set up data
	import h5py
	f=h5py.File(filename)
	reducedDataArray = f[datapath]
	qArray=f[qpath]
	x=qArray
	y=np.arange(0,len(reducedDataArray[0]))
	x,y = np.meshgrid(x,y)
	z=np.log(reducedDataArray[0])
	surf=ax.plot_surface(x,y,z,cmap=cm.coolwarm,linewidth=0,antialiased=True)
	fig.colorbar(surf, shrink=0.5, aspect=5)
	plt.show()
	fig.savefig(output)
	fig.clf()
