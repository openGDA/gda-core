# Example data at
#/dls/i12/data/2014/cm4963-1/rawdata/35802.nxs
#/dls/i12/data/2014/cm4963-1/rawdata/35803.nxs

import time
import scisoftpy as dnp
import math
from uk.ac.diamond.scisoft.analysis.fitting import EllipseFitter as _efitter
from uk.ac.diamond.scisoft.analysis.dataset import AbstractDataset as absd

from sphere_alignment_processing import sphere_alignment_processing


print "run sphere alignment script"


def sphere_alignment(threshold=0.55, x_stage=ss1_x, flat_field_position=-50, projection_position=2.0, theta_stage=ss1_theta, exposure_time=0.06, max_tries=15, z_tilt_stage=ss1_rz, x_tilt_stage=ss1_rx,doscan=True, working_directory='/dls/i12/data/2014/cm4963-2/rawdata', flatfield_file=36466, projection_file=36467):

    print "Running sphere alignment"
    # scan flat field
    # Run the scan, then call sphere_alignmet_processing(flatfilename, projectionfilename)
    
    if (doscan == True):
        print " Moving to flatfield position" # this should be an optional step as it may not be possible to do this move without disrupting user equipment
        #pos ss1.x -200
        pos(x_stage,flat_field_position)
        
        print "Collecting flat field image"
        scan(ix,1,1,1,pco4000_dio_hdf,exposure_time)
        time.sleep(5)
        working_dir = wd()
        current_file = cfn()
        filename = '%s/%d.nxs' % (working_dir, current_file)
        
        print "Loading flat field image to data processing pipeline"
        ff = None
        try_number = 0
        while (ff == None and try_number < max_tries):
            try_number += 1
            try:
                data = dnp.io.load(filename)
                ff = data['entry1']['pco4000_dio_hdf']['data'][...].mean(0)
            except:
                print("Failed to load %i, will try again in 5 seconds" % (try_number))
                time.sleep(5)
        
        dnp.plot.image(ff)
        
        print "Moving to sample position"
        pos(x_stage,projection_position)
        
        print "Collecting projections"
        scan(theta_stage,0,360,20,pco4000_dio_hdf,exposure_time)
        working_dir = wd()
        current_file = cfn()
        filename = '%s/%d.nxs' % (working_dir, current_file)
        
        time.sleep(5)
        
        print "Loading projections to data processing pipeline"
        #data = dnp.io.load(filename)
        dd = None
        try_number = 0
        while (dd == None and try_number < max_tries):
            try_number += 1
            try:
                data = dnp.io.load(filename)
                dd = data['entry1']['pco4000_dio_hdf']['data']
            except:
                print("Failed to load %i, will try again in 5 seconds" % (try_number))
                time.sleep(5)
        
                       
    else:
        print "Not Moving to flatfield position" # this should be an optional step as it may not be possible to do this move without disrupting user equipment
        
        print "Not Collecting flat field image"
        time.sleep(5)
        working_dir = working_directory
        current_file=flatfield_file
        filename = '%s/%d.nxs' % (working_dir, current_file)
        
        print "Loading flat field image to data processing pipeline"
        ff = None
        try_number = 0
        while (ff == None and try_number < max_tries):
            try_number += 1
            try:
                data = dnp.io.load(filename)
                ff = data['entry1']['pco4000_dio_hdf']['data'][...].mean(0)
            except:
                print("Failed to load %i, will try again in 2 seconds" % (try_number))
                time.sleep(2)
        
        dnp.plot.image(ff)
        
        print "Not Moving to sample position"
            
        print "Not Collecting projections"
        working_dir = working_directory
        current_file=projection_file
        filename = '%s/%d.nxs' % (working_dir, current_file)
        
        time.sleep(5)
        
        print "Loading projections into data processing pipeline"
        data = dnp.io.load(filename)
        dd = data['entry1']['pco4000_dio_hdf']['data']
    
    
    
    print "Processing data"
    
    # apply any filtering
    # exclude spheres which are not completely in the field of view
    
    print "slow look at all the data"
    preview = dd[:,:,:].mean(0)*1.0/ff
    dnp.plot.image(preview[10:-1,:])
    time.sleep(2)
    print "Generating centroid data"
    
    thresholdOK = False
    xs = []
    ys = []
    while not thresholdOK:
        xs = []
        ys = []
        for i in range(dd.shape[0]):
            cor = dd[i,:,:]*1.0/ff
            cor = cor[10:-1,:]<threshold
            dnp.plot.clear()
            #time.sleep(0.1)
            dnp.plot.image(cor)
            #time.sleep(.1)
            x,y = dnp.centroid(cor)
            xs.append(x)
            ys.append(y)
        
        xxx = absd.createFromList(ys)
        yyy = absd.createFromList(xs)
        
        responce = requestInput("Current threshold is %f, y for ok, otherwise enter a new threshold value " % (threshold))
        if responce == 'y' :
            thresholdOK = True
        else :
            try :
                threshold = float(responce)
            except :
                print "could not interpret %s as a float" % responce
    
    print "Fitting ellipse to centroid data"
    f = _efitter()
    f.algebraicFit(xxx,yyy)
    f.geometricFit(xxx,yyy,f.parameters)
    
    t = dnp.arange(100)*dnp.pi/50.
    t = absd.createFromList(t.tolist())
    fitplot = _efitter.generateCoordinates(t, f.parameters)
    
    dnp.plot.line(fitplot[0], fitplot[1])
    time.sleep(1)
    dnp.plot.addline(xxx,yyy)
    #print("XXX is:")
    #print (type(xxx))
    #print(xxx)
    print ("ys is:")
    type (xs)
    print(xs)
    
    
    ellipseFitValues = f.parameters
    
    print "Results of ellipse fitting routine:"
    print "   Major: %f" % (ellipseFitValues[0])
    print "   Minor semi-axis: %f" % (ellipseFitValues[1])
    print "   Major axis angle: %f" % (ellipseFitValues[2])
    print "   Centre co-ord 1: %f" % (ellipseFitValues[3])
    print "   Centre co-ord 2: %f" % (ellipseFitValues[4])
    
    print "Calculating stage tilts"
    xangle = math.fabs( math.atan2(ellipseFitValues[1], ellipseFitValues[0]))
    #find out which direction
    npoints=len(xs)
    firstquarter=int(math.floor(npoints/4.0))
    lastquarter=int(math.floor(3.0*npoints/4.0))
    print("   firstquarter %i %f lastquarter %i %f"%(firstquarter,xs[firstquarter],lastquarter,xs[lastquarter]) )
    if (xs[firstquarter] > xs[lastquarter]):
        xangle=-math.degrees(xangle)
        print("   negative xangle %f"%xangle)
    else:
        xangle=math.degrees(xangle)
        print("   positive xangle %f"%xangle)

    #xangle = math.degrees(xangle) * -1.0
    
    # This needs to be calculated correctly
    zangle = math.degrees(ellipseFitValues[2])
    if zangle > 90.0:
        zangle -= 180.0
    
    print xangle
    print zangle
    
    
    responce = requestInput("Would you like to rotate the X tilt stage by %f" % (xangle))
    if responce == 'y' :
        print "Moving x tilt stage"
        pos(x_tilt_stage)
        inc(x_tilt_stage,xangle)
        pos(x_tilt_stage)
        print "In position."


    responce = requestInput("Would you like to rotate the Z tilt stage by %f" % (zangle))
    if responce == 'y' :
        print "Moving z tilt stage"
        pos(z_tilt_stage)
        inc(z_tilt_stage,zangle)
        pos(z_tilt_stage)
        print "In position."
        
    print "sphere_alignment finished"
