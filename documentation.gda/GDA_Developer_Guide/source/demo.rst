==========
 GDA Demo
==========

Basic commands
==============

To get help::

   help


scannable = software abstraction of angles, slits, energy, temperature probe, detector...



pos – show current positions of all scannables

e.g. pos x, pos y, pos z


shows extended syntax – no brackets


Move: pos x 10


ls – look at objects of certain types

e.g. ls Motor


Easy to write dummy scannables, e.g. x/y/z, for testing


Other scannables
================

t
   shows time since initialisation

dt
   shows time since last data point captured

w
   waits for specified time. e.g. to wait 2 seconds::
      
      >>> pos w 2


all single-value position so far

multi-input – can move to multi-value position – e.g. pos mi [2, 3]

multi-extra – read-only output values – e.g. pos me

can combine – mie – one input, two (read-only) outputs – pos mie 4


Default detectors
=================

list_defaults

add_default pil

remove_default pil


Beam focusing
=============

fwhm = full width half maximum

minimise fwhmarea = area of spot on detector in pixels


Scan to show the images being plotted::

   >>> scan f 430 600 20 pil 20

To display the images: images plotted on "Data Vector" panel


to focus on region of interest::

   peak2d.setRoi(50, 50, 150, 150)




wide scan
~~~~~~~~~

::
   
   >>> scan f 430 600 20 pil 20 peak2d

(finds 490 as the minimum)

data plotted as it's collected


finer scan
~~~~~~~~~~

::
   
   >>> go minval
   >>> rscan f -20 20 2.5 pil 20 peak2d

(finds 482.5 as the minimum)


get feature details
~~~~~~~~~~~~~~~~~~~

::
   
   >>> minval



