# Example data at
#/dls/i12/data/2014/cm4963-1/rawdata/35802.nxs
#/dls/i12/data/2014/cm4963-1/rawdata/35803.nxs

import time
import scisoftpy as dnp
import math

from sphere_alignment_processing import sphere_alignment_processing


print "run sphere alignment script"


def sphere_alignment(threshold=0.55, x_stage=ss1_x, flat_field_position=-50, projection_position=2.0, theta_stage=ss1_theta, exposure_time=0.06, max_tries=15, z_tilt_stage=ss1_rz, x_tilt_stage=ss1_rx,doscan=True):
    ''' 
    Key things to change are
     flat_field_position - is the location of the x_stage when you want to take a flat field
     projection_position - is the location of the x stage when you want to take a projection
     exposure_time - is the exposure time for the camera to get a good image of the sphere.
     
     threshold - and if you have already run the script and found a good threshold, you can add it to this as well
     
    '''
    
    print "Running sphere alignment"
    # scan flat field
    # Run the scan, then call sphere_alignmet_processing(flatfilename, projectionfilename)
    
    print " Moving to flatfield position" # this should be an optional step as it may not be possible to do this move without disrupting user equipment
    #pos ss1.x -200
    pos(x_stage,flat_field_position)
    
    print "Collecting flat field image"
    scan(ix,1,1,1,pco4000_dio_hdf,exposure_time)
    flat_current_file = cfn()
    
    print "Moving to sample position"
    pos(x_stage,projection_position)
    
    print "Collecting projections"
    scan(theta_stage,0,360,20,pco4000_dio_hdf,exposure_time)
    data_current_file = cfn()
    
    time.sleep(10)
    
    visit_directory = os.sep.join(['']+[a for a in wd().split(os.sep) if a != ''][:-1])
    
    # try to process the data
    (xangle,zangle,xarray,yarray) = sphere_alignment_processing(threshold=threshold,visit_directory=visit_directory,flatfile=flat_current_file, projectionfile=data_current_file, request_input_command=requestInput,prompt=True)
    
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
