#@PydevCodeAnalysisIgnore
#
# Test script for use during GDA development to ensure the bacis Jython syntax works
#

#commenting and printing
#	print "hello" 
# print "hello"
print "hello"	# end of line comment
print "you should see hello only once" # another comment with a 	tab in it
print "this     line     should    contain       	lots 	of spaces"

#list comprehension
vec = [1,2,3,4,5,6,7]
vec = [3*i for i in vec if i < 2]
print vec
# should get [3]

#for loops
for i in range(5):
	print i

#alias
def myTemporaryFunction():
   print "aliased function ran successfully"
alias("myTemporaryFunction")
myTemporaryFunction
myTemporaryFunction()

#these tests rely PDs from the base localStation.py
x
pos x
x()
scan x 10   12 1
scan x 10       12 1 y
scan x 10 12 	1 y 5
scan x 10 12 1 y 5 1
scan x 10 12 1 y 5 7 1
scan x 10 12 1 y 5 7 1 z 3

#test \r
for i in range(1001):
	print "\r"+	`i`
print "\n"
print "should only see 1000 printed out now."
print "hello\rworld" #should get: world
print "\n"

#commas
a = [  [1 2 3] [ 4 5 6]  ] 
print a
a=[(1,1 + 2 +1,1,"asldkfjh,sdklajgh    ,   +  ")]
print a # should get: [(1, 4, 1, 'asldkfjh,sdklajgh    ,+  ')]


#exception output
raise "test script has finished and should finish by throwing this exception"
