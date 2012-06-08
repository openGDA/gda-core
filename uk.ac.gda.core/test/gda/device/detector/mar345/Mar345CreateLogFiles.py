import time
import shutil

# copies mar.message every given no of secs

copyDelay = 1
copyPeriod = 200
folder = "/home/qjk53739/temp/marFilesDump/scan1oct30/"
i=0

t0 = time.clock()
t1 = t0
while ( (t1 - t0) < copyPeriod ):

	shutil.copyfile("/dls/i15/mar/log/mar.message", folder + "mar" + str(i) + ".message")
	print "copied file: mar" + str(i) + ".message"
	time.sleep(copyDelay)
	t1 = time.clock()
	i = i+1
