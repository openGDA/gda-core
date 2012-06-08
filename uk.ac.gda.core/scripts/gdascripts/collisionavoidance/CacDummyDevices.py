from gda.device.scannable import ScannableBase
from gda.device.scannable import CheckedScannableMotionBase #@UnresolvedImport
#reload(gda.jython.scannable)
 
reload(CheckedScannableMotionBase)
#reload(gda.jython.scannable.CheckedScannableBase)

from time import clock	
	
class CheckedSlowDummyClass(CheckedScannableMotionBase):
	'''Slow Checked Dummy PD Class'''
	def __init__(self, name, inputName, startpos, speed):
		self.setName(name)
		self.setInputNames(inputName)
		self.Units=['Units']
		self.setOutputFormat(['%6.4f'])
		self.setLevel(3)
		self.currentposition1=startpos
				
		self.speed = speed
		self.timeToMove1 = 0
		self.distanceToMove1=0
		
		self.moveStartedTime = 0
		#self.timer=tictoc()
	
	def uncheckedIsBusy(self):
		if ((clock()-self.moveStartedTime) <= self.timeToMove1):
			return 1
		else:
			return 0

	def uncheckedAsynchronousMoveTo(self, new_position):
		# convert single position if aray input

		if (type(new_position)!=type(1.0)) and (type(new_position)!=type(1)):
			new_position=new_position[0]
	
		self.distanceToMove1 = new_position - self.currentposition1
	
		print self.distanceToMove1
		self.timeToMove1 = abs(self.distanceToMove1 / self.speed)
		
		
		self.moveStartedTime = clock()
		self.currentposition1 = new_position
		
	def getPosition(self):
		# a
		if (clock()-self.moveStartedTime) <= self.timeToMove1:
			try:
				fractionToGo = (self.timeToMove1 - (clock()-self.moveStartedTime))/self.timeToMove1
			except:
				fractionToGo = 0
			aPos = self.currentposition1 - fractionToGo*self.distanceToMove1
		else:
			aPos = self.currentposition1 			
		return [aPos]

	def atStart(self):
		print "In " + self.getName() + "'s atStart()"
		
	def atEnd(self):
		print "In " + self.getName() + "'s atEnd()"
		
	def squareIt(self, iny):
		return iny*iny;
	
	
	
class Checked2SlowDummyClass(CheckedScannableMotionBase):
	'''Slow Dummy PD Class'''
	def __init__(self, name, inputNames, startpos, speed):
		self.setName(name)
		self.setInputNames([inputNames[0], inputNames[1]])
		self.Units=['Units']
		self.setOutputFormat(['%6.4f', '%6.4f', '%6.4f'])
		self.setLevel(3)
		
		self.currentposition1= startpos[0]
		self.currentposition2= startpos[1]
		self.speed = speed
		self.timeToMove1 = 0
		self.timeToMove2 = 0
	
		self.moveStartedTime = 0
		#self.timer=tictoc()
	
	def uncheckedIsBusy(self):
		isMoving1=((clock()-self.moveStartedTime) <= self.timeToMove1) & ((clock()-self.moveStartedTime) <= self.timeToMove1) & ((clock()-self.moveStartedTime) <= self.timeToMove1)
		isMoving2=((clock()-self.moveStartedTime) <= self.timeToMove2) & ((clock()-self.moveStartedTime) <= self.timeToMove2) & ((clock()-self.moveStartedTime) <= self.timeToMove2)
		if (isMoving1 | isMoving2):
			return 1
		else:
			return 0

	def uncheckedAsynchronousMoveTo(self, new_position):
		self.distanceToMove1 = new_position[0] - self.currentposition1
		self.distanceToMove2 = new_position[1] - self.currentposition2
	
		self.timeToMove1 = abs(self.distanceToMove1 / self.speed)
		self.timeToMove2 = abs(self.distanceToMove2 / self.speed)
	
		
		self.moveStartedTime = clock()
		self.currentposition1 = new_position[0]
		self.currentposition2 = new_position[1]
	

	def getPosition(self):
		# a
		if (clock()-self.moveStartedTime) <= self.timeToMove1:
			fractionToGo = (self.timeToMove1 - (clock()-self.moveStartedTime))/self.timeToMove1
			aPos = self.currentposition1 - fractionToGo*self.distanceToMove1
		else:
			aPos = self.currentposition1 

		#b
		if (clock()-self.moveStartedTime) <= self.timeToMove2:
			fractionToGo = (self.timeToMove2 - (clock()-self.moveStartedTime))/self.timeToMove2
			bPos = self.currentposition2 - fractionToGo*self.distanceToMove2
		else:
			bPos = self.currentposition2 
		
		#print [aPos, bPos]
		
		return [aPos, bPos]

	def atStart(self):
		print "In " + self.getName() + "'s atStart()"
		
	def atEnd(self):
		print "In " + self.getName() + "'s atEnd()"

	
class Checked3SlowDummyClass(CheckedScannableMotionBase):
	'''Slow Dummy PD Class'''
	def __init__(self, name, inputNames, startpos, speed):
		self.setName(name)
		self.setInputNames([inputNames[0], inputNames[1], inputNames[2]])
		self.Units=['Units']
		self.setOutputFormat(['%6.4f', '%6.4f', '%6.4f'])
		self.setLevel(3)
		self.currentposition1= startpos[0]
		self.currentposition2= startpos[1]
		self.currentposition3= startpos[2]
		self.speed = speed
		self.timeToMove1 = 0
		self.timeToMove2 = 0
		self.timeToMove3 = 0
		self.moveStartedTime = 0
		#self.timer=tictoc()
	
	def uncheckedIsBusy(self):
		isMoving1= ( ((clock()-self.moveStartedTime) <= self.timeToMove1) &
		 ((clock()-self.moveStartedTime) <= self.timeToMove1) &
		  ((clock()-self.moveStartedTime) <= self.timeToMove1) )
		isMoving2=( ((clock()-self.moveStartedTime) <= self.timeToMove2) &
		 ((clock()-self.moveStartedTime) <= self.timeToMove2) &
		  ((clock()-self.moveStartedTime) <= self.timeToMove2) )
		isMoving3=( ((clock()-self.moveStartedTime) <= self.timeToMove3) &
		 ((clock()-self.moveStartedTime) <= self.timeToMove3) &
		  ((clock()-self.moveStartedTime) <= self.timeToMove3) )
		if (isMoving1|isMoving2|isMoving3):
			return 1
		else:
			return 0

	def uncheckedAsynchronousMoveTo(self, new_position):
		self.distanceToMove1 = new_position[0] - self.currentposition1
		self.distanceToMove2 = new_position[1] - self.currentposition2
		self.distanceToMove3 = new_position[2] - self.currentposition3
		self.timeToMove1 = abs(self.distanceToMove1 / self.speed)
		self.timeToMove2 = abs(self.distanceToMove2 / self.speed)
		self.timeToMove3 = abs(self.distanceToMove3 / self.speed)
		
		self.moveStartedTime = clock()
		self.currentposition1 = new_position[0]
		self.currentposition2 = new_position[1]
		self.currentposition3 = new_position[2]

	def getPosition(self):
		# a
		if (clock()-self.moveStartedTime) <= self.timeToMove1:
			fractionToGo = (self.timeToMove1 - (clock()-self.moveStartedTime))/self.timeToMove1
			aPos = self.currentposition1 - fractionToGo*self.distanceToMove1
		else:
			aPos = self.currentposition1 

		#b
		if (clock()-self.moveStartedTime) <= self.timeToMove2:
			fractionToGo = (self.timeToMove2 - (clock()-self.moveStartedTime))/self.timeToMove2
			bPos = self.currentposition2 - fractionToGo*self.distanceToMove2
		else:
			bPos = self.currentposition2 
		#c
		if (clock()-self.moveStartedTime) <= self.timeToMove3:
			fractionToGo = (self.timeToMove3 - (clock()-self.moveStartedTime))/self.timeToMove3
			cPos = self.currentposition3 - fractionToGo*self.distanceToMove3
		else:
			cPos = self.currentposition3 
			
		#print [aPos, bPos, cPos]
		
		return [aPos, bPos, cPos]

	def atStart(self):
		print "In " + self.getName() + "'s atStart()"
		
	def atEnd(self):
		print "In " + self.getName() + "'s atEnd()"