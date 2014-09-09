# Example data at
#/dls/i12/data/2014/cm4963-1/rawdata/35802.nxs
#/dls/i12/data/2014/cm4963-1/rawdata/35803.nxs

import os
import time
import scisoftpy as dnp
import math

from uk.ac.diamond.scisoft.analysis.dataset import Image as javaImage


print "Loading the  sphere alignment processing functions"

def readTestInputs(filename='height.txt',idealname='ideal.txt',visit_directory='/dls/i12/data/2014/cm4963-2',input_directory='tmp',printres=False):
    infilename="%s/%s/%s"%(visit_directory,input_directory,filename)
    idealfilename="%s/%s/%s"%(visit_directory,input_directory,idealname)
    inlist=[]
    ideallist=[]
    
    
    try:
        infile=open(infilename,'r')
    except:
        print("File %s not opened properly"%infilename)
        return(None)
    n=0
    for line in infile:
        if printres:
            print n,line
            n=n+1
        inlist.append(float(line))
    infile.close()
    try:
        infile=open(idealfilename,'r')
    except:
        print("File %s not opened properly"%idealfilename)
        return(None)
    for line in infile:
        if printres:
            print n,line
            n=n+1
        ideallist.append(float(line))
    infile.close()
    if printres:
        print inlist
        print ideallist
    inarray=dnp.array(inlist)
    idealarray=dnp.array(ideallist)
    print len(inarray)
    angarray=((dnp.linspace(0,dnp.pi*2.0,len(inarray),True)))
    horizarray=2500.0*dnp.cos(angarray)
    ofile=open("%s/%s/testpoints.txt"%(visit_directory,input_directory),'w')
    ofile.write("angle,X,idealY,noisyY\n")
    for i in range(0,len(angarray)):
        ofile.write("%g,%g,%g,%g\n"%(angarray[i],horizarray[i],idealarray[i],inarray[i]))
    ofile.close()
    return(horizarray,inarray,idealarray)
    

def request_DAWN_Input(message):
    return raw_input(message)

def dofourier(xarray,yarray,printres=False):
    nvals=len(yarray)
    avht=yarray.mean()
    angles=dnp.linspace(0,2*dnp.pi,len(yarray))
    #print angles
    ycarray=yarray-avht
    cosines=dnp.cos(angles)
    sines=dnp.sin(angles)
    cosint=cosines*ycarray
    sinint=sines*ycarray
    if printres:
        print "Yarray:",yarray
        print "Ycarray:",ycarray
        print "Sinint:",sinint
        print "Cosint:",cosint
    cosum=cosint[:-1].sum()
    sinsum=sinint[:-1].sum()
    cosfactor=cosum/float(nvals-1)
    sinfactor=sinsum/float(nvals-1)
    if printres:
        print "sinfactor, cosfactor: %.4f %.4f"%(sinfactor,cosfactor)
    ratio=sinfactor/cosfactor
    delta=math.atan(ratio)
    deltadeg=math.degrees(delta)
    if printres:
        print"Delta degrees %06.4f"%deltadeg
    halfmag=math.sqrt(sinfactor**2+cosfactor**2)
    mag=2.0*halfmag
    if printres:
        print"Magnitude: %06.4f"%mag
        print "vertical centre %7.4f"%avht
    
    calcresult=avht+mag*dnp.cos(angles-delta)
    if printres:
        print"Calcresult:",calcresult
        for idx in range(0,len(yarray)):
            print yarray[idx],calcresult[idx]
        
    return(avht,mag,delta,calcresult)

def get_image_data(previewmean=False, preview_frames=True,visit_directory='/dls/i12/data/2014/cm4963-2/',flatfile=37384, projectionfile=37385, threshold=0.5, max_tries=15,prompt=False, request_input_command=request_DAWN_Input):

    working_directory="%s/rawdata"%visit_directory
    flatfilename="%s/%i.nxs"%(working_directory,flatfile)
    projectionfilename="%s/%i.nxs"%(working_directory,projectionfile)
    
    #TODO put a unique time stamp on the otuput
    #TODO make a folder for the outputs
    
    
    print("FlatFilename = %s"% flatfilename)
    print("projectionfilename = %s"% projectionfilename)
    
    print "Loading flat field image %s\n\t to data processing pipeline" % flatfilename
    ff = None
    try_number = 0
    while (ff == None and try_number < max_tries):
        try_number += 1
        try:
            print("Trying to open the file %s" % (flatfilename))
            data = dnp.io.load(flatfilename)
            ff = data['entry1']['pco4000_dio_hdf']['data'][...].mean(0)
        except:
            print("ff is not null is it %s" % ff)
            print("ff is not null is of type %s" % type(ff))
            print("Failed to load %i, will try again in 5 seconds" % (try_number))
            time.sleep(5)
    
    ff = dnp.array(javaImage.medianFilter(ff._jdataset(), [3, 3]))
    dnp.plot.image(ff)
    
    time.sleep(5)

    
    print "Loading projections %s into data processing pipeline"% projectionfilename
    data = dnp.io.load(projectionfilename)
    try:
        dd = data['entry1']['pco4000_dio_hdf']['data']
    except:
        print"Couldn not find the data in %s"%projectionfilename
        raise
    
    
    print "Processing data"
    
    # apply any filtering
    # exclude spheres which are not completely in the field of view
    
    if (previewmean):
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
            #print "Applying median filter"
            cor = dnp.array(javaImage.medianFilter(cor._jdataset(), [3, 3]))
            cor = cor[10:-1,:]<threshold
            if (preview_frames):
                dnp.plot.clear()
                time.sleep(0.01)
                dnp.plot.image(cor)
                time.sleep(.2)
            y,x = dnp.centroid(cor)
            xs.append(x)
            ys.append(y)
        

        if (prompt==False):
            thresholdOK=True
        else:
            response = request_input_command("Current threshold is %f, y for ok, otherwise enter a new threshold value " % (threshold))
            if response == 'y' :
                thresholdOK = True
            else :
                try :
                    threshold = float(response)
                except :
                    print "could not interpret %s as a float" % response
                    
    return(xs,ys)

def sphere_alignment_processing(previewmean=False,visit_directory='/dls/i12/data/2014/cm4963-2/',flatfile=37384, projectionfile=37385, threshold=0.4, max_tries=15,prompt=False,testdata=False, request_input_command=request_DAWN_Input):
    results_directory="%s/tmp/orbit_test"%visit_directory
    if not os.path.exists(results_directory) :
        os.makedirs(results_directory)
    #The acquisition of the data is separated into another fuction, returning the lists of x and y data
    if not testdata:
        #the image processing step might fail. 
        try:
            xs,ys=get_image_data(visit_directory=visit_directory, flatfile=flatfile ,projectionfile=projectionfile, threshold=threshold,max_tries=max_tries,prompt=prompt,request_input_command=request_input_command)
        except:
            raise
        
        xarray=dnp.array(xs)
        yarray=dnp.array(ys)
    else:
        #Lists of numbers can be read in to compare results
        xarray,yarray,idealarray=readTestInputs(printres=False)
        #is there a less annoying way? 
        
    #return
    
    print "Fitting ellipse to centroid data"
    ellipseFitValues = dnp.fit.ellipsefit(xarray, yarray)
    
    t = dnp.arange(100)*dnp.pi/50.
    fitplot = dnp.fit.makeellipse(ellipseFitValues, t)
    print("fitplot is: %s element is %s "%(type(fitplot), type(fitplot[0])))
    
    dnp.plot.line(fitplot[0], fitplot[1])
    if previewmean:
        time.sleep(1)
    dnp.plot.addline(xarray, yarray)
    
    print "Results of ellipse fitting routine:"
    print "   Major: %f" % (ellipseFitValues[0])
    print "   Minor semi-axis: %f" % (ellipseFitValues[1])
    print "   Major axis angle: %f" % (ellipseFitValues[2])
    print "   Centre co-ord 1: %f" % (ellipseFitValues[3])
    print "   Centre co-ord 2: %f" % (ellipseFitValues[4])
    
    print "Calculating stage tilts"
    xangle = math.fabs( math.atan2(ellipseFitValues[1], ellipseFitValues[0]))
    
    #find out which direction
    #find the extremes in horizontal direction
    npoints=len(xarray)
    xmax=dnp.argmax(xarray)
    xmin=dnp.argmin(xarray)
    
    #index 1/4 of the way around
    firstquarter=(int(math.floor(npoints/4.0))+xmax)%npoints
    lastquarter=(int(math.floor(3.0*npoints/4.0))+xmax)%npoints
    #Show which points are used for finding the front and back:
    dnp.plot.addline(dnp.array([xarray[firstquarter],xarray[lastquarter]]),dnp.array([yarray[firstquarter],yarray[lastquarter]]))
    dnp.plot.addline(dnp.array([xarray[xmin],xarray[xmax]]),dnp.array([yarray[xmin],yarray[xmax]]))
    #check whether the 90 deg or 270 deg point is higher and use the corresponding sign for the angle
    if (yarray[firstquarter] > yarray[lastquarter]):
        xangle=-math.degrees(xangle)
        print("   negative xangle %f"%xangle)
    else:
        xangle=math.degrees(xangle)
        print("   positive xangle %f"%xangle)
    # This needs to be calculated correctly
    zangle = math.degrees(ellipseFitValues[2])
    if zangle > 90.0:
        zangle -= 180.0
    
    print "efitter Xangle (degrees)",xangle
    print "efitter Zangle (degrees)",zangle
    
    print "Now trying direct fourier integral method"
    f_ht,f_mag,f_delta,f_calcres=dofourier(xarray,yarray,printres=False)
    #dnp.plot.addline(xdata,f_calcres)
    dnp.plot.addline(xarray,f_calcres)
    if(testdata):
        dnp.plot.addline(xarray,idealarray)
    #nfitpoints=len(dnp.array(t))
    nfitpoints=t.size
    fitplotarray=dnp.array(fitplot)
    
    resultsfilename="%s/efitter_%s.dat"%(results_directory,projectionfile)
    fout=open(resultsfilename,"w")
    for i in range(0,nfitpoints):
        fout.write("%i,%f,%f\n"%(i,fitplotarray[0][i],fitplotarray[1][i]))
    fout.close()
    
    resultsfilename="%s/input_%s.dat"%(results_directory,projectionfile)
    fout=open(resultsfilename,"w")
    for i in range(0,npoints):
        fout.write("%i,%f,%f\n"%(i,xarray[i],yarray[i]))
    fout.close()
    
    resultsfilename="%s/fcalc_%s.dat"%(results_directory,projectionfile)
    fout=open(resultsfilename,"w")
    for i in range(0,npoints):
        fout.write("%i,%f,%f\n"%(i,xarray[i],f_calcres[i]))
    fout.close()
    
    return (xangle,zangle,xarray,yarray)

def doseveral():
    orbits=[]
    infilename="/dls/i12/data/2014/cm4963-2/tmp/orbitlist.txt"
    try:
        infile=open(infilename,'r')
    except:
        print("File %s not opened properly"%infilename)
        return(None)
    n=0
    for line in infile:
        orbits.append(int(line))
    infile.close()
    oray=dnp.array(orbits)
    fray=oray-1
    print oray
    print fray
    for i in range(0,len(oray)):
        print i,oray[i]
        try:
            sphere_alignment_processing(previewmean=True,flatfile=fray[i],projectionfile=oray[i])
        except:
            print("%i didn't work"%oray[i])
        print("finished %i"%oray[i])
    return

    
