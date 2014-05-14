========================
 Eclipse GUI Preferences
========================

This document lists the various preferences defined by the GDA Eclipse Client 

The common preferences are defined in the class uk.ac.gda.preferences.PreferenceConstants.

How to Specify Preferences
==========================
The file system location of a properties file containing default settings for plug-in preferences can be 
set in the following ways:

- use -Declipse.pluginCustomization=<path> as a VM argument to the Java VM
    
- set the property, -pluginCustomization, in the config.ini file in the appropriate configuration area
    


    
ScanPlot Preferences
====================

- uk.ac.gda.client/gda.client.plot.colors

  Comma separated list of integers used to construct Color(int rgb). Def = PlotColorUtility.getDefaultColour(nr);
  
  Values converted to Integer using Integer.valueof(s,16)., i.e. using radix 16  e.g. FF0000 = red ::

	e.g. uk.ac.gda.client/gda.client.plot.colors=FF0000,FF00,FF

- uk.ac.gda.client/gda.client.plot.linewidth

  Integer value for line width. Def = PlotColorUtility.getDefaultLineWidth(0)::

	e.g. uk.ac.gda.client/gda.client.plot.linewidth=1

- uk.ac.gda.client/gda.client.plot.linestyles

  Comma separated list of integers for line style. Def = PlotColorUtility.getDefaultStyle(nr)::

	e.g uk.ac.gda.client/gda.client.plot.linestyles=3,3,3,3,4,4,4,4
	


Preference to display text beside icons in GDA view
===================================================

There is a preference to display text next to the relevant icons in the GDA views. To do so

- Click on the menu options  Window -> Preferences

- GDA preferences will be displayed 

- Select the checkbox that says - "Show text along with icons for menus in the toolbar"

- Click "OK" (GDA will prompt to restart itself - this is so that GDA can apply the relevant changes)
