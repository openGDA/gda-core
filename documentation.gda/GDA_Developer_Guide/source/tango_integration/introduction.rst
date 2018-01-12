=========================================
 Integrating TANGO in GDA
=========================================



TANGO `TANGO <http://www.tango-controls.org/>`_


Integration of TANGO within GDA covers the creation of Devices and Scannables that talk to TANGO devices.

Connecting GDA to TANGO is done by use of class gda.device.TangoDeviceProxy in the plugin
org.gda.tango. This plugin contains the TANGO jar TangORG-7.0.3.jar.

To create a bean to connect to the tg_test device supplied with the main Tango release registered in 
the Tango database at localhost:10000 add the following to the Spring config::

	<bean id="tg_test_dev_proxy" class="gda.device.TangoDeviceProxy">
		<constructor-arg value="tango://localhost:10000/sys/tg_test/1" />
	</bean>

This device proxy is then used as a property of device specific classes. An example of this is gda.device.scannable.TangoScannable.
This scannable's position is associated with an attribute of the device via the attributeName property. In the example below the position of the scannable
tg_test is associated with the double_scalar attribute of the device defined above::

	<bean id="tg_test" class="gda.device.scannable.TangoScannable">
	<property name="tangoDeviceProxy" ref="tg_test_dev_proxy"/>
	<property name="attributeName" value="double_scalar"/>
	</bean>
	
	

..
   Local Variables:
   mode: indented-text
   indent-tabs-mode: nil
   sentence-end-double-space: t
   fill-column: 70
   End:

