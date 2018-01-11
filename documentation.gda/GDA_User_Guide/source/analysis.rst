===============
 Data Analysis
===============

Plotting
========

The Plotter object provides a different set of methods to plot one or
two-dimensional data either in one, two or three dimensions. This
chapter will give an example for each of these different plotting
types.



1D plots
--------


2D image plots
--------------


Viewing Images
--------------

The ability to view images from many of the detectors at diamond has
been incorporated into the GDA. Currently the images from the
following detectors can be loaded into the GDA:


+ ADSC Detectors on the MX beamlines *ADSCLoader*
+ MAR Detectors *MARLoader*
+ Pilatus Tiffs *PilatusTiffLoader*
+ CBF files *CBFLoader*
+ JPEG, TIFF and PNG images *JPEGLoader,TIFFLoader and PNGLoader*


The above can be used in the following way within the Jython terminal::

   >>> from gda.analysis.io import *
   >>> sfh = ScanFileHolder()
   >>> sfh.load(FileReader("FileName"))
   >>> Plotter.plotImage("Data Vector", sfh[0])
    					
Images can be saved from a ScanFileHolder in either PNG or JPEG format
(PNGSaver and JPEGSaver) with the possibility of scaling the image
such that it will be able to be saved in the requested format. The
upper limit of JPEG is 255 and PNG is 65535 for any intensity of pixel
value. If the pixel intensity is greater than this value the image can
be scaled to fit the maximum depth of the image format. The images can
be scaled using PNGScaledSaver and JPEGScaledSaver::

   >>> from gda.analysis.io.import
   >>> # Assuming that a scan file holder has been created
   >>> # containing the data and is called 'sfh'
   >>> sfh.save(FileSave("FileName"))
    					

If the ending is not specified the proper ending will be added and if
there are many images they will be called 'Image00001.xxx,
Image00002.xxx' etc.


3D surface plots
----------------

This chapter will provide a step by step example on how to generate a
3D surface plot of a two dimensional data or image file


+  Generate a ScanFileHolder object::
      
      >>> data = ScanFileHolder()
    						
+  Load the data file::

      >>> data.load(OtokoLoader("/s/Science/DASC/LinuxFiles/B09000.806"))

+  Display the data as a 3D surface plot::
 
      >>> Plotter.plot3D("Data Vector",data[0])

For the third line to work you have to make sure that you have the
Data Vector panel in your actual configuration. This is the simplest
way to surface plot a 2D dataset. When no third parameter is provided
and the data contained in the ScanFileHolder is too large to display
at once either because there is not enough memory available or the
display refresh rate would be too low and therefore not interactive it
will automatically subsample. If you prefer not to subsample but
rather would like to display a subset of the data as a window on the whole
data, change the last command to this::

    >>> Plotter.plot3D("Data Vector",data[0],True)

If the dataset has more than two dimensions. The plotter will
automatically choose the first two dimensions for generating the
surface plot. If the dataset has only one dimension it still will work
but the plotter will automatically switch to a different usage mode
that allows ploting a series of one dimensional datasets (see the
section called 1D plots).
