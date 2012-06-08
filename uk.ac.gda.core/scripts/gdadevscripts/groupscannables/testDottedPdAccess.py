#@PydevCodeAnalysisIgnore


'''



Access fields of standard scannable using dotted notation
=========================================================
 
(In the following scannables are described by their name followed by their field names in brackets.
inputs are given after 'i:' and extra outputs after an 'e:'.)
   
Take for example a polarisation analyser with one angle set/read and one detector values to readback
   pol(i:theta; e:rate)
   
This can be accessed as follows:

   >>> pos pol
   pol.theta : 0
   pol.rate : 100
   
   >>> pos pol.theta 45 # Note that pol.theta is full-on scannable itself
   pol.theta : 45
   
   >>> pos pol.rate
   pol.rate: 70.7
   
   >>> pol['rate']()
   (70.7)
   
For a second example take a scannable with more than one input field:
	plotter(i:x, y)
	
The values can be read using 'pos plotter', 'pos plotter.x' and 'pos plotter.y',  and the pair
can be moved as expected using 'pos plotter (3,4)'. Moving just the x or the y is more complex.
The default behaviour is that when the method plotter.x.asynchronousMoveto(5) is run this
calls asynchronousMoveTo([5, None]) on the 'real' scannable plotter. This essentially tells
plotter to move its first field (x) to 5 and to do what it feels appropriate to y.  If it does
not make sense for a scannable to respond sanely to such partial input it may choose to throw
an exception. One alternative is to use the provided private method fillPosition() at the beginning
of the scannables asynchrnonousMoveTo() command which replaces the Nones in the list with the
scannable's corresponding current field values.  The class ScannableMotionBaseWithMemory will
attempt to fill these missing values the last requested move to positions.

Pos and scan commands
---------------------
In the future the pos and scan commands may be made aware of scannable parts. For a scannable
such as p(i: x, y, z) the pos command might be smart enougth to recognise that in the command
	
	pos p.x 3 p.z 5
	
the scannables p.x and p.y are both scannable parts and combine the two moves into a single
request

	p.aynchronousMoveTo([3, None, 5]) .
	
The same behavior might be extended to the scan command. For example, where s is a scannable
with two input fields x and y, the command

	scan s.x 1 3 1 s.y 5 1

would result in the following calls
	
	s(1,5)
	s(2,6)
	s(3,7) .
	
The command

   scan s.x 1 2 1 s.y 3 4 1
   
would resulting in the following calls

	s(1,3)
	s(1,4)
	s(2,3)
	s(2,4) .


Build up a group scannable from components
==========================================
Take for example two separate slit motors:

   sample__x(i:sx)
   sample__y(i:sy) .
   
These can be combined using

   sample = ScannableMotionGroup('sample', [sample__x, sample__y], ['x','y'], [])
   
to get a new scannable

	sample(i:x, y) .
	
This can can be used as follows

	>>> pos sample
	sample.x : 1
	sample.y : 2
	
	>>> pos sample.x 10
	sample.x : 10
	
	>>> sample.y()
	(2)
	
	>>> sample()
	(10,2)
	
	>>> sample['x']
	sample.x : 10


Group more complex things
========================= 
Combine both of these into a polarisation anlayser with sample x and sample y offsets:

	both = ScannableMotionGroup('both', [pol, sample], ['theta','sx','sy'], ['rate'])
	
would result in the scannable

   both (i:theta, x, y; e: rate)
   
which can be accessed as follows:

	>>> pos both
	both.theta : 45
	both.sx : 10
	both.sy : 2
	both.rate : 70.7
	
	>>> both()
	(45, 10, 2, 70.7)
	
	>>> pos both.theta 90
	both.theta : 90
	
	>>> both['rate']
	both.rate : 0
	
	
	= Possible Replacement for OEs and dotted access to scannables fields =
== OE replacement ==

I've been working on a prototype scannable called ScannableMotionGroup that groups together a number of component scannables makeing them look like a single scannable.  I've also protyped a new version of ScannableMotionBase called ScannableMotionBase74 that adds 'dotted' access from Jython to scannables fields.   These two changes are discussed separetely and in detail below.  The rest of this section describes how taken together these might provide a good way to replace the current OE/dof hierarchy with. 

===User interaction===
As an example, the following single-motor scannables 
{{{
slit1xplus
slit1xminus
slit1yplus
slit1yminus
}}}
could be combined into a single scannable called sl with four input fields. Basic behaviour would work as before:
{{{
>>> s1()
(1,2,3,4)

>>> pos s1 [10, None, 30, none]
s1.xplus : 10
s1.xminus : 2
s1.yplus : 30
s1.yminus : 4
}}}
Notice that to leave some components still the list input to the group cane have Nones in it.

The scannable s1 will also contain four jython fields that alow each component to be accessed as separate scannables (this works for scannables that arn't built up for compnents too). For example:
{{{
>>> pos sl.xplus 3
s1.xplus : 3

>>> scan s1.xplus 1 3 .1

>>> s1['xplus']()

}}}
Notice that collection type access can alsoe be used.  To access these fields from java use getPart(name). The raw component scannables themselves can always be accessed with getComponent(name).
===Automatic startup===
There is a really easy way to set up these scannable groups that is compatable with the new epics integration procedure and requires no xml to be written.  If the control's guys create the 'gda tags' for axis that will become part of a group in the form {{{groupname__axisname}}} it is very easy, after all these scannables have been created, to automatically
	
	

'''


# Dotted access to scannables
import ScannableMotionBase74
reload(ScannableMotionBase74)

iie = ScannableMotionBase74.MultiInputDummyPD74('iie',['i1','i2'],['e1'])
pos iie (1,2)
print iie
print iie.i1
print iie.i2
print iie.e1

pos iie.i1 3
pos iie.i2 4

print iie
print iie['e1']


print "Testing groups"

# Test Group Scannable with one element inputs
from gdascripts.pd.dummy_pds import DummyPD
ab__a=DummyPD('ab__a')
ab__b=DummyPD('ab__b')

import ScannableMotionGroup
reload(ScannableMotionGroup)
ab = ScannableMotionGroup.ScannableMotionGroup('ab',[ab__a,ab__b], ['a','b'],[])
