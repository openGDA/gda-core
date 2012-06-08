from gda.jython.commands.GeneralCommands import pause

def myMethod():
	print "your have run myMethod!"

def myLoopingMethod():
	for i in range(1,100000):
		print i 
		pause()