/*-
 * Copyright © 2021 Diamond Light Source Ltd.
 *
 * This file is part of GDA.
 *
 * GDA is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License version 3 as published by the Free
 * Software Foundation.
 *
 * GDA is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along
 * with GDA. If not, see <http://www.gnu.org/licenses/>.
 */

package gda.device.detector.addetector.collectionstrategy;

import java.util.HashMap;
import java.util.Map;

import gda.epics.connection.EpicsController;
import gda.scan.ScanInformation;
import gov.aps.jca.CAException;
import gov.aps.jca.TimeoutException;

/**
 * Decorator to customise the plugin chain of an AreaDetector.
 * Configured with a map of each plugin port name to value to produce the desired chain.
 * <p>
 * Example to configure a XSP3 -> DTC -> ARR chain for area detector at BL18I-EA-XSP3-01:
 * <pre>
 * {@code
 * <bean id="custom_chain" class="gda.device.detector.addetector.collectionstrategy.CustomPluginChainDecorator">
 *  <constructor-arg value="BL18I-EA-XSP3-01" />
 *	<constructor-arg>
 *		<map>
 *			<entry key="DTC" value="XSP3" />
 *			<entry key="ARR" value="DTC" />
 *		</map>
 *	</constructor-arg>
 *
 *	<property name="decoratee" ref="softwarestartstop"
 * </bean>}</pre>
 */
public class CustomPluginChainDecorator extends AbstractADCollectionStrategyDecorator {

	private final String basePv;
	private final Map<String, String> customPluginChain;

	private Map<String, String> savedPluginChain;

	/**
	 * @param basePv
	 * 			The base PV for this area detector
	 * @param customPluginChain
	 * 			The name of every required plugin's port name
	 * 			mapped to its required value (which will be another plugin's port name)
	 */
	public CustomPluginChainDecorator(String basePv, Map<String, String> customPluginChain) {
		this.basePv = basePv;
		this.customPluginChain = customPluginChain;
	}

	@Override
	protected void rawPrepareForCollection(double collectionTime, int numberImagesPerCollection, ScanInformation scanInfo) throws Exception {
		configurePluginChain(customPluginChain);
		getDecoratee().prepareForCollection(collectionTime, numberImagesPerCollection, scanInfo);
	}

	private void configurePluginChain(Map<String, String> pluginChainMap) throws CAException, TimeoutException, InterruptedException {
		var epicsController = EpicsController.getInstance();

		for (var plugin : pluginChainMap.entrySet()) {
			var channel = epicsController.createChannel(getPortPv(plugin.getKey()));
			epicsController.caputWait(channel, plugin.getValue());
			epicsController.destroy(channel);
			Thread.sleep(100); // prevent connection timeouts if map is very large
		}
	}

	@Override
	public void saveState() throws Exception {
		getDecoratee().saveState();
		savedPluginChain = new HashMap<>();
		for (var plugin : customPluginChain.keySet()) {
			savedPluginChain.put(plugin, getCurrentPort(plugin));
			Thread.sleep(100); // prevent connection timeouts if map is very large
		}
	}

	private String getCurrentPort(String pluginName) throws CAException, TimeoutException, InterruptedException {
		var epicsController = EpicsController.getInstance();
		var channel = epicsController.createChannel(getPortReadbackPv(pluginName));
		var value = epicsController.cagetString(channel);
		epicsController.destroy(channel);
		return value;
	}

	@Override
	public void restoreState() throws Exception {
		configurePluginChain(savedPluginChain);
		getDecoratee().restoreState();
	}

	private String getPortPv(String pluginName) {
		return String.format("%s:%s:NDArrayPort", basePv, pluginName);
	}

	private String getPortReadbackPv(String plugin) {
		return String.format("%s_RBV", getPortPv(plugin));
	}
}
