#@PydevCodeAnalysisIgnore
#
#
# Import various functions and libraries
#
#
import Jama
from java.lang import *
from org.python.modules import math
from java.util import Random
from gda.analysis.numerical.optimization.objectivefunction import AbstractLSQObjectiveFunction
from gda.analysis.datastructure import DataVector
from java.lang import *
from time import sleep
from org.python.modules.jarray import *
from org.python.modules.math import *
from gda.analysis.numerical.optimization.optimizers.leastsquares import minpackOptimizer
from gda.analysis.numerical.optimization.objectivefunction import chisquared
from gda.analysis.numerical.optimization.optimizers.filtering import iffco 
from gda.analysis.numerical.linefunction import Parameter
from gda.analysis.numerical.optimization.optimizers.simplex import NelderMeadOptimizer
import jarray


#
#
# This is a collection of methods used in focussing a KB mirror
# Method works with a bender or bimorph
#
#
class MirrorFocus:
	#
	# Initialize the class
	#
	def __init__(self):
		print 'Init Mirror Focus Class'


	#
	# useful method for creating an empty 2D list filled with zeros
	#
	def initArray2D(self,rows,columns):
		a = []
		for x in range(rows):
			a.append([0.0] * columns)
		return a	

	#		
	# useful method for creating an empty 1D list filled with zeros		
	#
	def initArray1D(self,rows):
		a = []
		for x in range(rows):
			a.append(0.0)
		return a

	#	
	# get weighted mean of a list of values
	#
	def getMean(self,a,weights):	
		# sum of weight(i)*value(i)
		sumwi =0.0
		# sum of the weights
		sumw =0.0
		for i in range(len(a)):
			sumwi = sumwi + a[i]*weights[i]
			sumw  = sumw  + weights[i]
		return sumwi/sumw

	#  
	# Find best voltages
	# Solves the interaction matrix
	#
	# Inputs:
	# noOfM : No of measurements ( no of centroid positions recorded)
	# noOfParams : no of paramters (e.g. electrodes)
	# matrix  : the interaction matrix size [noOfM][noOfParams]
        #           [[dC1/dV1, dC2/dV1............]   changes of centroids with voltage 1
        #           [dC1/dV2, dC2/dV2............]    change of centroids with voltage 2 etc.    
	# weights : A list of length noOfM. For each measurement you can supply a weighting between 0 and 1, 0 will mean the value is completely ignored. 
	#           This is usefuly as the edges of the slit scan so first and last centroids can be poor. On I18 you see more intensity variation
	#	    at the edges. Ideally you would weight with the std_devs of the centroid measurements but our SESO camera doesn't supply this for the 
	#	    gda interface
	# currentPositions: The centroid measurements at the current point 
	# currentVolts    : Optional: The current voltages on the mirror. Code will print out the change in voltage required or if you supply the current voltages
	#		     the absolute voltages needed.
	# mode            : Untested,optional parameter but for focussing you are effectively bringing the centroids to the same points 
	#		    In defocussed setup you want the centroids will actually be spread out, basically along a line
	#		    mode=0 will try and adjust the voltages to reduce spread of centroids..i.e. rms slope error
        #                   mode=1 will try and adjust the voltages to give the adjust the centroids to a linear trend.
        #                   This should remove beam splitting, tailings etc. that can be seen when defocussing
	#
	#
	# Output: Print out the voltages you need to change:
	#         In the event of poor measurements at the edges, and lack of sensitivity to the movement at the edge of the mirror
	#	  You may get some very large voltages (outside mirror range) at the ends. Simply set them to be the same at the next electrode in 
	#         e.g 1500, 656,....712,2150....changes to 656,656.....712,712   or play around with the weights
	#         
	#
	def iterationMatrix(self,noOfM,noOfParams,matrix,weights,currentPositions,currentVolts=None,mode=0):
		#
		# Set up the weighting array
		#
		myweight=self.initArray2D(noOfM,noOfM)
		# fill it with the weight values
		for i in range(noOfM):
			myweight[i][i]=weights[i]
		# Create a jama matrix from the weighting arrays
		matrix_w = Jama.Matrix(myweight)
		#
		# Determine the pseudo slope errors
		#
		slopeerrors=self.getSlopeErrors(currentPositions,weights,mode)
		print 'pseudo slope errors',slopeerrors
		#
		# Solution 1...solve the matrix to reduce the errors
		#
		# construct jama matrices and solve using weighting
		matrix_b = Jama.Matrix(slopeerrors,len(slopeerrors))
		mat_A = Jama.Matrix(matrix)
		mat_Awt=(matrix_w.times(mat_A)).transpose()
		mat_Aw=(matrix_w.times(mat_A))
		mat_wb=(matrix_w.times(matrix_b))
		# x = ((WA)T(WA))-1 (WA)TWb
		mat_result=(((mat_Awt.times(mat_Aw)).inverse()).times(mat_Awt)).times(mat_wb)

		# Voltages required 
		print "============================================"
		for i in range(noOfParams):
			if(currentVolts==None):
				print 'Matrix Inversion: voltage change required:',mat_result.get(i,0)
			else:
				print 'Matrix Inversion: voltages required:',mat_result.get(i,0)+currentVolts[i]
		print "============================================"
		



	#
	# Given the current centroid/beam positions from the slit scan
	# return the error. 
	# Basically the deviation about the centre or mean value.
	#
	# If you are at the focal point you look at the deviation about the mean
	# i.e. in this case all centroid positions should ideally be the same
	# as all sections of the mirror are focussing to the same point
	# so you try minimize the deviations from this. 
	# 
	# 
	# If you are not at the focal point, but want nice defocussing you
	# are looking at the deviation about a line. 
	# Basically off focus the traces coming from the mirror sections 
	# should be regularly spaced roughly along a line so you want to 
	# minimize the deviations from this line
	#
	#
	def getSlopeErrors(self,newcentroids,weights,mode):		
		sloperr=[]
		fv=0
		# focus point
		if(mode==0):
			# weighted mean
			v=self.getMean(newCentroids,weights)
			for cent in newcentroids:
				sloperr.append(v-cent)
				fv=fv+(v-cent)*(v-cent)
			print 'sum_sq_diff',fv,'weighted mean',v
		else:
			# off focus
			# Line fit
			x=range(len(newCentroids))
			v,a=self.fitline(x,newCentroids,weights)
			for cent in newcentroids:
				newy = v*x[i]+a
				# Return ax +b - yi
				# Not this is inversed as solving the matrix to remove this
				sloperr.append(newy-cent)
				fv=fv+(newy-cent)*(newy-cent)
			print 'sum_sq_diff',fv,'line parameters',v,a
	
		return sloperr

	#
	#
	# Weighted least squares line fit
	# 
	#
	def fitline(self,x,y,weights):
		a=self.initArray2D(len(x),2)
		for i in range(len(x)):
			a[i][0]=x[i]
			a[i][1]=1
                # construct jama matrices and solve using weighting
		matrixA=Jama.Matrix(a)
		matrixW=Jama.Matrix(weights)
                matrixB = Jama.Matrix(y,len(y))
                matrixAWT=(matrixW.times(matrixA)).transpose()
                matrixAW=(matrixW.times(matrixA))
                matrixWB=(matrixW.times(matrixB))
                # x = ((WA)T(WA))-1 (WA)TWb
                matrixResult=(((matrixAWT.times(matrixAW)).inverse()).times(matrixAWT)).times(matrixWB)
		return matrixResult.get(0,0),matrixResult.get(1,0)



#
# Example of use
#
# You need to supply a matrix: Below is example.
# Obviously depends on how you prefere to create and store the matrix data
#
#
#
# Method to read in a line of centroids positions from a file, (read in a particular centroid value marked by index)
#
def read_centroids(centroidFile,index):
	f=open(centroidFile)
	AA=f.read()
	f.close()
	# Now split up the data into a float array
	AA=AA.split('\n')
	a = AA[index].split(" ")
	for i in range(len(a)):
		a[i]=float(a[i])
	return a


mfocus=MirrorFocus()

noOfM=22
noOfParams=8
#
# Measured centroid positions
#
newCentroids=[542.556, 532.119 ,532.159 ,532.949, 531.074 ,529.293, 529.425, 529.738, 530.392 ,529.48, 528.345 ,526.914, 531.331 ,535.464 ,531.071, 525.732, 529.005 ,529.607 ,531.365, 531.6 ,530.407 ,528.987]
#
#
#
#
# Read in the centroid values and build the interaction matrix
#
interactionM=mfocus.initArray2D(noOfM,noOfParams)
voltageStep=100
centroidFile='/dls/i18/tmp/matrix/VFM_18_2_09a_matrix.txt_centroid.dat'
for i in range(noOfParams):
       	test1=read_centroids(centroidFile,i)
	test2=read_centroids(centroidFile,i+1)
	drdb=[]
	# Note this depends on the direction you stepped in...
	for j in range(len(test1)):
		# This is the derivative dC/dV ...be careful of the direction
		interactionM[j][i]=((test1[j]-test2[j])/voltageStep)

weights=[]
for i in range(noOfM):
	weights.append(1.0)

mfocus.iterationMatrix(noOfM,noOfParams,interactionM,weights,newCentroids)


#
# Example with weighting first and last values to 0
#
print '\n\nExample of weighting first and last value as 0.0\n\n'
weights[0]=0.0
weights[noOfM-1]=0.0

mfocus.iterationMatrix(noOfM,noOfParams,interactionM,weights,newCentroids)

#
# Just to show the difference with current volts
#
print '\n\nExample with current voltages set\n\n'
volts=[]
for i in range(noOfParams):
	volts.append(200)

mfocus.iterationMatrix(noOfM,noOfParams,interactionM,weights,newCentroids,currentVolts=volts)





