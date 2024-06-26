/*-
 * Copyright © 2022 Diamond Light Source Ltd.
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

package uk.ac.diamond.daq.autoprocessing.ui;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.Arrays;
import java.util.Map;

import org.eclipse.dawnsci.analysis.api.persistence.IMarshallerService;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.autoprocessing.AutoProcessingBean;

/**
 * User interface to get processing configuration information from GDA-Zocalo-Connector REST-API and use this
 * information to build UI to aid user configuration
 * <p>
 *
 * GET requests currently happen in UI thread - could be extracted out if there are issues
 *
 */
public class AutoProcessingConfigComposite extends Composite {

	private static final Logger logger = LoggerFactory.getLogger(AutoProcessingConfigComposite.class);

	private IMarshallerService service;
	private URI server;
	private String[] apps;
	private AutoProcessingConfig config = null;

	private String processorsEndpoint = "/processors";
	private String processorEndpoint = "/processor/";

	private AutoProcessingConfigurationViewer viewer;

	final Label infoLabel;
	final Combo combo;

	public AutoProcessingConfigComposite(Composite parent, IMarshallerService service, URI server) {
		super(parent, SWT.NONE);
		this.service = service;
		this.server = server;

		this.apps = getApps(service, server);
		this.setLayout(new GridLayout(2, false));

		// build UI
		combo = new Combo(this, SWT.READ_ONLY);
		infoLabel = new Label(this, SWT.NONE);
		viewer = new AutoProcessingConfigurationViewer(this);
		viewer.setLayoutData(GridDataFactory.swtDefaults().align(SWT.FILL, SWT.FILL).grab(true, true)
				.span(new Point(2, 1)).create());

		if (apps == null) {
			infoLabel.setText("Could not read processing information from server");
			return;
		}

		this.config = getConfig(apps[0]);

		if (config == null) {
			infoLabel.setText("Could not read config information from server");
			return;
		}

		combo.setItems(apps);
		combo.select(0);

		infoLabel.setText(config.getDescription());
		viewer.setInput(config.getFields());

		combo.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				int i = combo.getSelectionIndex();
				config = getConfig(apps[i]);

				if (config == null) {
					viewer.setInput(null);
					infoLabel.setText("Could not read config from server");
				} else {
					viewer.setInput(config.getFields());
					infoLabel.setText(config.getDescription());
				}
				infoLabel.getParent().layout();
				viewer.redraw();

			}

		});
	}


	/**
	 * Setup the GUI from AutoProcessingBeanObject :
	 * <li> Retrieve the AutoProcessingConfig object from server matching the appName in the processingBean
	 * <li> Modify each of the AutoProcessingField objects in the AutoProcessingConfig so that the values
	 * match the values from the config object in processingBean
	 *
	 * @param processingBean
	 */
	public void setupFromConfig(AutoProcessingBean processingBean) {
		// Get full configuration object from the server
		AutoProcessingConfig newConfig = getConfig(processingBean.getAppName());

		if (newConfig == null || newConfig.getFields() == null) {
			logger.warn("Problem getting configuration details using name {}", processingBean.getAppName());;
			return;
		}

		// Try to extract configuration map from Object or Json String
		Object configObject = processingBean.getConfig();
		Map<?, ?> map = null;
		if (configObject instanceof String str) {
			try {
				map = service.unmarshal(str, Map.class);
			} catch (Exception e) {
				logger.error("Problem parsing {} to json string", str, e);
			}
		} else 	if (configObject instanceof Map<?,?> m ) {
			map = m;
		}

		// update the AutoProcessingConfig config fields from the AutoProcessingBean values
		if (map != null) {

			logger.info("Setting autoprocessing GUI for {} config", processingBean.getAppName());

			// Make list of AutoProcessingFields for the configuration so we can search it easily.
			var fieldarray = Arrays.asList(newConfig.getFields());

			for (var entry : map.entrySet()) {
				// search AutoProcessingFields to find one for matching the entry in AutomProcessingBean config
				fieldarray.stream()
					.filter(f -> f.getName().equals(entry.getKey().toString()))
					.findFirst()
					.ifPresent(matchingField -> {
						logger.debug("Setting parameter : {} = {}", entry.getKey(), entry.getValue());
						matchingField.setValueFromObject(entry.getValue());
						}
					);
			}
		}
		config = newConfig;

		// Select the appropriate entry in the combo box
		Arrays.asList(apps).indexOf(config.getName());
		int index = Arrays.asList(apps).indexOf(config.getName());
		combo.select(index);

		// Update the rest of the fields
		infoLabel.setText(config.getDescription());
		viewer.setInput(config.getFields());
		viewer.redraw();
	}

	private String[] getApps(IMarshallerService service, URI server) {

		URI endpoint = server.resolve(processorsEndpoint);

		String json = getJson(endpoint);

		if (json == null) {
			return null;
		}

		try {
			Object[] obj = service.unmarshal(json, Object[].class);

			String[] apps = new String[obj.length];

			for (int i = 0; i < apps.length; i++) {
				apps[i] = obj[i].toString();
			}

			return apps;

		} catch (Exception e) {
			logger.error("Could not parse apps", e);
		}

		return null;

	}

	private String getJson(URI endpoint) {

		URL url;
		try {
			url = endpoint.toURL();
		} catch (MalformedURLException e1) {
			logger.error("Could not create url", e1);
			return null;
		}

		try (BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()))) {

			return in.readLine();

		} catch (Exception e) {
			logger.error("Could not read from server", e);
		}

		return null;

	}

	private AutoProcessingConfig getConfig(String name) {
		URI endpoint = server.resolve(processorEndpoint + name);

		String json = getJson(endpoint);

		try {
			Map<?, ?> conf = service.unmarshal(json, Map.class);

			return AutoProcessingConfig.parseMap(conf);
		} catch (Exception e) {
			logger.error("Could not parse conf", e);
		}

		return null;
	}

	public AutoProcessingBean getConfiguration(boolean forceString) {

		Map<String, Object> m = config.fieldsToMap();

		String json;
		try {
			json = service.marshal(m);
		} catch (Exception ex) {
			logger.error("Could not serialise config", ex);
			json = "WARNING! COULD NOT MARSHALL CONFIG";

			AutoProcessingBean bean = new AutoProcessingBean(config.getName(), m);
			bean.setDisplayName(json);

			return bean;
		}

		AutoProcessingBean bean = new AutoProcessingBean(config.getName(), forceString ? json : m);
		bean.setDisplayName(json);

		return bean;
	}

}
