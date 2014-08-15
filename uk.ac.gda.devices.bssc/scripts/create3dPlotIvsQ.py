from mpl_toolkits.mplot3d import Axes3D
import numpy as np
from matplotlib import cm
import sys

def createPlot(filename, datapath, qpath, output, interactive):
	#set up plot
	if not interactive:
		import matplotlib
		matplotlib.use('Agg')
	import matplotlib.pyplot as plt

	fig = plt.figure()
	ax=fig.gca(projection='3d')
	
	#set up data
	import h5py
	f=h5py.File(filename, 'r')
	reducedDataArray = f[datapath]
	qArray=f[qpath]
	x=qArray
	y=np.arange(0,len(reducedDataArray[0]))
	x,y = np.meshgrid(x,y)
	z=np.log(reducedDataArray[0])
	surf=ax.plot_surface(x,y,z,cmap=cm.coolwarm,linewidth=0,antialiased=True)
	fig.colorbar(surf, shrink=0.5, aspect=5)
	if interactive:
		plt.show()
		
	import os
	outputPath = output[0:output.rfind(os.path.sep)]
	if not os.path.exists(outputPath):
		os.makedirs(outputPath)
	thumbnailOutput = output[0:output.rfind(".")]+"t"+output[output.rfind(".")]
	fig.savefig(output)
	fig.set_figheight(1.79)
	fig.set_figwidth(1.79)
	fig.savefig(thumbnailOutput)
	fig.clf()

if __name__ == '__main__':

	import argparse
	parser = argparse.ArgumentParser()
	parser.add_argument("--filename", type=str, help="filename of input Nexus file after data reduction")
	parser.add_argument("--output", type=str, help="output file name")
	parser.add_argument("--datapath", type=str, help="path for reduced data in Nexus file", default="/entry1/detector_processing/Normalisation/data")
	parser.add_argument("--qpath", type=str, help="Q path name in Nexus file", default = "/entry1/detector_result/q")
	parser.add_argument("--interactive", type=str, help="show interactive plot. Output plot will be produced regardless of interactivity", default=False)
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

	if args.interactive:
		interactive = True
	else:
		interactive = False

	createPlot(filename, datapath, qpath, output, interactive=False)
