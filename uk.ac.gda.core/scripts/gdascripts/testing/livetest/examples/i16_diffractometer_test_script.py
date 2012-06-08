#@PydevCodeAnalysisIgnore
#livetest/scripts/diffractometer_test.py
from gda.jython.commands.ScannableCommands import pos, scan

# DANGER: make sure it is safe to run this script. Everything must be at 0 to start
for p in sixc():
	if abs(p) > 1:
		raise Exception("All sixc angles must be 0 to start reasonably safely")



print "Creating reffile and setting crystal"
reffile('dummy_test_with_cubic')
mode euler 1 
latt([1,1,1,90,90,90])

print "Moving Energy..."
#pos energy  12.398 #moves bragg, undulator, mirros, diffractometer, optics table
print "...complete"
print "Going to first 001"
c2th([0,0,1])

pos delta c2th([0,0,1]) eta c2th([0,0,1])/2
pos() mu 0 gam 0
pos phi 0

saveref('001',[0,0,1])

ubm('001',[1,0,0]) # assumes [1,0,0] is in the vertical plane, and determines azimuth normallly found from or2
#array('d',[-0.9999723587733832, 0.007435165380914164, 2.2260001343412905E-6, -0.00743516540177009, -0.9999723587304612, -9.51241620544971E-6, 2.1552122170511666E-6, -9.528703949842119E-6, 0.9999999999522796]) 
hkl_calc([0,1,1])

print "Going to first 011"
pos hkl [0 1 1]

# Normaly scan phi to find reflection
pos phi
saveref('011',[0,1,1])
ubm('001','011')
#array('d',[-0.9999726435469858, 0.007396766367641229, 2.22600013434129E-6, -0.007396766388498801, -0.999972643504065, -9.51241620544971E-6, 2.155578118353377E-6, -9.528621182518454E-6, 0.9999999999522796]) 

showref()

print "Scanning"
scan hkl [0 0 1] [0 1 1] [0 .1 0] euler inctime
#===Injection mode pausing is enabled: TimeToInjection must exceed 5
#Warning::Vector and Azimuthal reference //, azimuthal reference not used
#Writing data to file:96716.dat
#h	k	l	phi	chi	eta	mu	delta	gamma	time_increment
#-0.0000	0.0000	1.0000	 -76.82897	  89.99923	  30.00113	  -0.00000	  60.00218	   0.00001	 18.56
#0.0000	0.1000	1.0000	 -89.57942	  84.28967	  30.16514	   0.00001	  60.33248	   0.00000	  4.98
#0.0000	0.2000	1.0000	 -89.57534	  78.68951	  30.65834	   0.00001	  61.31688	   0.00001	  5.40
#0.0000	0.3000	1.0000	 -89.57501	  73.30078	  31.46812	   0.00001	  62.93740	   0.00001	  4.27
#-0.0000	0.4000	1.0000	 -89.57581	  68.19738	  32.58428	  -0.00001	  65.16770	   0.00000	  3.80
#0.0000	0.5000	1.0000	 -89.57570	  63.43471	  33.98882	   0.00000	  67.97812	  -0.00001	  4.14
#-0.0000	0.6000	1.0000	 -89.57395	  59.03423	  35.67067	   0.00002	  71.33964	   0.00001	  3.93
#0.0000	0.7000	1.0000	 -89.57655	  55.00874	  37.61381	  -0.00000	  75.22925	  -0.00000	  4.15
#-0.0000	0.8000	1.0000	 -89.57550	  51.33904	  39.81685	   0.00001	  79.63341	  -0.00002	 10.49
#-0.0000	0.9000	1.0000	 -89.57448	  48.01146	  42.27629	  -0.00001	  84.55201	  -0.00000	  4.90
#0.0000	1.0000	1.0000	 -89.57644	  44.99981	  45.00151	   0.00002	  90.00381	  -0.00002	  4.53
#Scan complete.