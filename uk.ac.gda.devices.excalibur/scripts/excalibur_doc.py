"""
Help for using excalibur


To take repeated images:
>repscan 1000 excalibur_summary_cam_normal .5

e.g. normal acquisition
repscan 100 excalibur_config_normal .01

e.g burst acquisition
repscan 100 excalibur_config_burst .1

To do a slow (GDA in the loop) threshold0 scan
e.g. threshold scan using summary image
scan threshold0 0 149 15 excalibur_summary_cam_normal .01


To do a threshold scan
>dacscan threshold0 <start> <stop> <step> <detector_name> <exposure>
where <detector_name> is: excalibur_config_dacscan | excalibur_summary_cam_dacscan

e.g. dacscan using per node writing
dacscan threshold0 0 149 15 excalibur_config_dacscan .01

e.g. dacscan using summary image
dacscan threshold0 0 149 15 excalibur_summary_cam_dacscan .01

Detector names available
excalibur_config_burst - drives config EPICS detector in burst mode using internal trigger
excalibur_config_normal - drives config EPICS detector in normal mode using internal trigger
excalibur_config_dacscan  - drives config EPICS detector in dacscan mode

excalibur_summary_cam_normal             - drives the summary EPICS detector in normal mode using software trigger 
excalibur_summary_cam_dacscan             - drives the summary EPICS detector in dacscan mode 
(normally this will be decimated, as dictated by divisor in mst tab of excalibur master configuration EDM screen)

excalibur_readoutNode1_ad   - drives readout node 1 EPICS detector
... and similar for other individual readout nodes


Scannables available are:

gainMode
mask
test
thresholdA
thresholdB

threshold0
threshold1
thresholdN
dacPixel
tpRef
tpRefA
tpRefB


actualTime
waittime

To globally set a scannable:
> pos <scannable> <value>

To globally scan a scannable:
>scan <scannable> <start> <stop> <step>  [detector] [ exposuretime]
e.g.
>scan thresholdN 0 100 10 excalibur_readoutNode1_det .5


To repeat a scan the above 100 times wraps with a scan of counter ix or iy

scan ix 0 100 1 thresholdN 0 100 10 excalibur_readoutNode1_det .5


NOW OBSOLETE? NEW INSTRUCTIONS FURTHER DOWN IN FILE
To perform an equalisation run the command:
-------------------------------------------
>equalisation.equalisationProcess()


To save the current config to file
----------------------------------
recordConfig.createModelAndSaveToFile("/data/excalibur/config/yourname.excaliburconfig")


To send a config from a file to the detector use:
-------------------------------------------------
recordConfig.sendConfigInFileToDetector("/data/excalibur/config/initial.excaliburconfig")    


To send the optimized thresholdN stored in a file to the chips
--------------------------------------------------------------
excalibur.setThresholdNFromFile(edgeThresholdNResponseFile, readoutFems, chipRows,chipCols ,chipPresentVals, False)
for 1 fem chipRows = 1 and chipCols = 8
chipPresentVals can be None


To send the optimized DAC pixel stored in a file to the chips
-----------------------------------------------
excalibur.setDACPixelFromFile(edgeThresholdNResponseFile, readoutFems, chipRows,chipCols ,chipPresentVals)
for 1 fem chipRows = 1 and chipCols = 8
chipPresentVals can be None


To send the thresholdAjd stored in a file to the chips:
------------------------------------------------------
excalibur.setThresholAdjFromFile(setThresholAdjFromFile, readoutFems, chipRows,chipCols ,chipPresentVals)
for 1 fem chipRows = 1 and chipCols = 8
chipPresentVals can be None

To send a pixel disable mask from file to the chips:
-----------------------------------------------------------
0 = pixel enabled , 1 = pixel disabled
Not yet implementedin GDA - need to go around with eqLoad.py
e.g. python eqLoad.py --mask --maskfile='/data/excalibur/configs/masks/maskRow1Col1.csv' --node=1 --chip=1

For direct access to PV use object excalibur_config
>>>excalibur_config.keySet()
[sync, proc, roi, gap, fix, mst, hdf, nodes, readoutFems]

readoutFems=excalibur_config.get("readoutFems")
readout1 = readoutFems[0]

>readout1.dacSense

All Terminal io is written to /scratch/excalibur/excalibur/data/gdaterminal.log




To scan over different ranges of threshold

 
Scanning

See GDA User Guide - scanning
How do I scan a motor over a sequence of ranges of different step size

This can be done with a combination of passing a scannable a tuple of values to iterate over and using the scisoft numpy arange command to generate the tuple.


>>>import scisiftpy as dnp
>>>scan thresholdA tuple(dnp.arange(700., 705.))+tuple(dnp.arange(705., 706., .5))+tuple(dnp.arange(706., 707.1, .2))

The only thing to note is that arange(start, end, step) uses an exclusive end condition (like pythons range()), which prevents 705.0 and 706.0 being duplicated in the example above, but would also omit 707.0 if the end point were not specified as 707.1.


To Perform a dacscan:
dacscan threshold0 0 500 1 excalibur_summary_cam_hdf .1
 
 
Additional notes
================
To run an equalisation:
-----------------------
epg=equalisation_script.ExcaliburEqualiser(logFileName="/data/excalibur/data/jl_60v3.dat")    - need to manually create the .dat file before doing this
epg.run()      - (Note: this will fail on first run if EPICS hdf array sizes have not previously been initialised)

To analyse the number of pixels with no edge found and the width of the distribution
------------------------------------------------------------------------------------
res=epg.analyse_thresholdfile(filePath="/data/excalibur/data/387_10.nxs", printOutResults=False) 

To send a threshold array to the chips (1 value per chip)
---------------------------------------------------------
epg.sendTailThresholdToChips(res)
 
To apply equalisation results (e.g. from intermediate steps)
------------------------------------------------------------
epg.excaliburEH.setThresholdAdjFromFile(epg.getDACPixelControlBitsFilename(), None, epg.readoutFems, epg.chipRows,epg.chipCols ,epg.chipPresentVals,0)

Notes on using equalisation scripts
-----------------------------------
If changes are made to code for which an object has already been created, you must clear out all objects prior to using the newversion of the code:
>>> reset_namespace

The logfile allows individual steps to be repeated as necessary. To do this, just delete the relevant lines from the .dat file, save it, then re-create the object and exercise the run method as above. Failure to re-rceate the object will result in the deleted lines being restored to the .dat file without any fresh data collection.
  


To check hdf writer chunking
----------------------------
excalibur_summary_cam_hdf.fileWriter.rowChunks
excalibur_summary_cam_hdf.fileWriter.colChunks

To fix hdf writer chunking
--------------------------
excalibur_summary_cam_hdf.fileWriter.colChunks=2069

Example: to collect a threshold0 scan and analyse
dacscan threshold0 0 511 1 excalibur_config_dacscan .01
epg.createThresholdFileFromScanData("<FILENAME>")

To read out any value for any given chip:
-----------------------------------------
e.g readoutFems[0].getIndexedMpxiiiChipReg(0).getAnper().threshold0
can also assign to a variable:
tmpVar=readoutFems[0].getIndexedMpxiiiChipReg(0).getAnper()
tmpVar. <auto complete> for list of all paramaters which can be read

To assign values, add = <value to be assigned> to end.
e.g
readoutFems[0].getIndexedMpxiiiChipReg(0).getAnper().threshold0 = 35

Can script with for loop to apply over all chips, as in sentThreshold0ToTailThreshold method withing equalsiation script.


TO DO (fixes etc)
=================
# GDA:     enable per chip readback of DAC sense so that it can be recoded for each point in a scan
X GDA:     make chip parameters (GND, CAS, FBK, RPZ, IKRUM, Shaper, Preamp, dealy) scannable
X GDA:     summary image data needs to be linked in to wrapper .nxs file - done
X GDA:     .dat file ought to be automatically created during equalisation
X GDA:     why alphabetical list? (can it be chronological)
X GDA:     how do we view the helper functions
X GDA:     set histogram bins to unity 
X GDA:     How to display 2D images from threshold scan
X GDA:     Send pixel configs and anper configs to each node in parallel
X GDA:     population anaylsis is rowN_colM, whereas edgeThresholds are colM_rowN: please make consistent

# EPICS:   equalisation fails on first run if array sizes have not been correctly set in EPICS hdf writer
# EPICS:   parallel hdf writer to be integrated

# self:    Choose more optimal values for thresholdNscans (Check that 40 and 50 are OK: 30 & 50 may be more appropriate)
# self:    Choose more optimal values for DACPixelscans (check that 10 and 40 are OK, or is something else better)


/home/excalibur/gda_versions/gda_8_26/workspace_git/gda-dls-excalibur.git/uk.ac.gda.devices.excalibur/src/uk/ac/gda/devices/excalibur/equalization/ExcaliburEqualizationHelper.java
"""
