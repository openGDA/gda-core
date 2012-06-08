#@PydevCodeAnalysisIgnore
from Jama import Matrix
from java.io import PrintWriter
from java.io import OutputStream
from java.lang import Double
from dl.factory import Finder
from org.python.core import PyTuple
from org.python.core import PySlice
from org.python.core import PyInteger

class M:

	def __init__(self,*twod_array):
		if isinstance(twod_array[0],Matrix):
			self.theMatrix = twod_array[0]
		elif isinstance(twod_array[0],M):
			self.theMatrix = twod_array[0].theMatrix
		else:
			self.theMatrix =Matrix(twod_array)

	def __add__(self,other):
		if self.isaMatrix(other):
			return M(self.theMatrix.plus(self.extractMatrix(other)))
		elif isinstance(other,PyInteger):
			out = M(self.theMatrix.copy())
			for i in range(len(out)):
				out[i] = self[i] + other
			return out			
		#assume a sequence or tuple
		else:
			out = M(self.theMatrix.copy())
			for i in range(len(other)):
				out[i] = self[i] + other[i]
			return out

	def __radd__(self,other):
		if self.isaMatrix(other):
			return M(self.theMatrix.plus(self.extractMatrix(other)))
		elif isinstance(other,PyInteger):
			out = M(self.theMatrix.copy())
			for i in range(len(out)):
				out[i] = self[i] + other
			return out			
		#assume a sequence or tuple
		else:
			out = M(self.theMatrix.copy())
			for i in range(len(other)):
				out[i] = self[i] + other[i]
			return out

	def __iadd__(self,other):
		temp = self.extractMatrix(self.__add__(other))
		self.theMatrix = temp
		return self

	def __sub__(self,other):
		if self.isaMatrix(other):
			return M(self.theMatrix.minus(self.extractMatrix(other)))
		elif isinstance(other,PyInteger):
			out = M(self.theMatrix.copy())
			for i in range(len(out)):
				out[i] = self[i] - other
			return out			
		#assume a sequence or tuple
		else:
			out = M(self.theMatrix.copy())
			for i in range(len(other)):
				out[i] = self[i] - other[i]
			return out

	def __rsub__(self,other):
		if self.isaMatrix(other):
			return M(self.theMatrix.minus(self.extractMatrix(other)))
		elif isinstance(other,PyInteger):
			out = M(self.theMatrix.copy())
			for i in range(len(out)):
				out[i] = other - self[i]
			return out			
		#assume a sequence or tuple
		else:
			out = M(self.theMatrix.copy())
			for i in range(len(other)):
				out[i] = other[i] - self[i]
			return out

	def __isub__(self,other):
		temp = self.extractMatrix(self.__sub__(other))
		self.theMatrix = temp
		return self

	def __mul__(self,other):
		if isinstance(other,M):
			return M(self.theMatrix.arrayTimes(self.extractMatrix(other)))
		elif isinstance(other,Matrix):
			return M(self.theMatrix.arrayTimes(self.extractMatrix(other)))
		elif type(other) == type([]):
			out = M(self.theMatrix.copy())
			for i in range(len(other)):
				out.__setitem__(i,self.__getitem__(i) * other[i])
			return out
		else:
			return M(self.theMatrix.times(other))

	def __rmul__(self,other):
		if isinstance(other,M):
			return M(self.theMatrix.arrayTimes(self.extractMatrix(other)))
		elif isinstance(other,Matrix):
			return M(self.theMatrix.arrayTimes(self.extractMatrix(other)))
		elif type(other) == type([]):
			out = M(self.theMatrix.copy())
			for i in range(len(other)):
				out.__setitem__(i,self.__getitem__(i) * other[i])
			return out
		else:
			return M(self.theMatrix.times(other))

	def __imul__(self,other):
		temp = self.extractMatrix(self.__mul__(other))
		self.theMatrix = temp
		return self

	def cross(self,other):
		return self.__mul__(other)

	def dot(self,other):
		return M(self.theMatrix.arrayTimes(self.extractMatrix(other)))

	def __getitem__(self,key):
		out = []
		rows = self.theMatrix.getColumnDimension()
		if isinstance(key,PySlice):
			start = key.start
			step = key.step
			stop = key.stop
			if start == None:
				start = 0
			if stop == None:
				stop = len(self)
			for i in range(start,stop,step):
				a,b = divmod(i,rows)
				out.append(self.theMatrix.get(a,b))
		if isinstance(key,PyTuple):
			if len(key) == 1:
				for i in range(key[0]):
					a,b = divmod(i,rows)
					out.append(self.theMatrix.get(a,b))
			if len(key) == 2:
				for i in range(key[0],key[1]):
					a,b = divmod(i,rows)
					out.append(self.theMatrix.get(a,b))
			elif len(key) == 3:
				for i in range(key[0],key[1],key[2]):
					a,b = divmod(i,rows)
					out.append(self.theMatrix.get(a,b))
		if isinstance(key,PyInteger):
			a,b = divmod(key,rows)
			out = self.theMatrix.get(a,b)
		return out

	def __setitem__(self,key,value):
		cols = self.theMatrix.getColumnDimension()
		a,b = divmod(key,cols)
		return self.theMatrix.set(a,b,value)

	def __len__(self):
 		return self.theMatrix.getColumnDimension()*self.theMatrix.getRowDimension()

	def __str__(self):
		output =  ""
		for i in range(self.theMatrix.getRowDimension()):
			if i == 0:
				output = output +"["
			for j in range(self.theMatrix.getColumnDimension()):
				if j == 0:
					output = output +"["
				output = output + Double.toString(self.theMatrix.get(i,j))
				if (j == (self.theMatrix.getColumnDimension() -1)):
					output = output +"]"
				else:
					output = output + ", "
			if (i == (self.theMatrix.getRowDimension() -1)):
				output = output +"]"
			else:
				output = output + ", "
		return output

	def __repr__(self):
		return self.__str__()

	def __call__(self):
		return self.__str__()

	#also will want every method supplied by Jama.Matrix class

	def extractMatrix(self,other):
		if isinstance(other, Matrix):
			return other
		elif isinstance(other, M):
			return other.theMatrix

	def isaMatrix(self,other):
		if isinstance(other, Matrix):
			return 1
		elif isinstance(other, M):
			return 1
		else:
			return 0





