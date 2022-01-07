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

package uk.ac.diamond.daq.sample.plate.management.ui.service;

import java.io.IOException;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.configuration.properties.LocalProperties;
import uk.ac.diamond.ispyb.api.IspybFactoryService;
import uk.ac.diamond.ispyb.api.IspybSpyCatApi;
import uk.ac.diamond.ispyb.api.Schema;

public class SpyCatApiConnector {
	public static final String ID = "uk.ac.diamond.daq.sample.plate.management.ui.service.SpyCatApiConnector";

	private static final Logger logger = LoggerFactory.getLogger(SpyCatApiConnector.class);

	private static IspybFactoryService<IspybSpyCatApi> spyCatService;
	private static IspybSpyCatApi spyCatApi;

	private static final String URL_PROP = LocalProperties.get(LocalProperties.GDA_BEAMLINE_NAME) + ".server.ispyb.connector.url";
	private static final String USER_PROP = LocalProperties.get(LocalProperties.GDA_BEAMLINE_NAME) + ".server.ispyb.connector.user";
	private static final String PASSWORD_PROP = LocalProperties.get(LocalProperties.GDA_BEAMLINE_NAME) + ".server.ispyb.connector.password";
	private static final String DATABASE_PROP = LocalProperties.get(LocalProperties.GDA_BEAMLINE_NAME) + ".server.ispyb.connector.database";

	public static String getConnectionUrlString() {
		StringBuilder url = new StringBuilder();
		try {
			String[] parts = LocalProperties.getStringArray(URL_PROP);
			boolean isFirstPart = true;
			for (String part : parts) {
				if (isFirstPart) {
					isFirstPart = false;
				} else {
					url.append(",");
				}
				url.append(part);
			}
		} catch (Exception cause) {
			String message = String.format("Connector URL not set. Check the the %s property is set.", URL_PROP);
			logger.error(message,cause);
		}
		return url.toString();
	}

	public static IspybSpyCatApi getIspybSpyCatApi() {
		try {
			if (null==spyCatApi) {
				String url = getConnectionUrlString();
				Optional<String> username = Optional.of(getMandatoryProperty(USER_PROP, "User Name"));
				Optional<String> password = Optional.of(getMandatoryProperty(PASSWORD_PROP, "Password"));
				// Check property string is a valid schema id
				Optional<String> schema = Optional.of(
					Schema.convert(
						getMandatoryProperty(DATABASE_PROP, "Database Name")
					).toString()
				);
				Object instance = spyCatService.buildIspybApi(url,username,password,schema);
				// A guard against a previously misconfigured factory service
				if (instance instanceof IspybSpyCatApi) {
					spyCatApi = (IspybSpyCatApi) instance;
				} else {
					String detail = null==instance ? "" : "(" + instance.getClass().getName() + ")";
					String message = "Invalid ISPyB Factory service " + detail;
					logger.error(message);
				}
			}
		}  catch (Exception cause) {
			String message = "Error on access to ISPyB API service";
			logger.error(message,cause);
		}
		return spyCatApi;
	}

	private static String getMandatoryProperty(String propertyName, String name) {
		final String value = LocalProperties.get(propertyName);
		if (value == null) {
			String message = String.format("Connector %s not set. Check the the %s property is set.", name, propertyName);
			logger.error(message);
		}
		return value;
	}

	public static IspybSpyCatApi resetIspybPlateServiceConnection(){
		closeIspybSpyCatServiceConnection();
		return getIspybSpyCatApi();
	}

	public static void closeIspybSpyCatServiceConnection() {
		if (spyCatApi == null)
			return;

		try {
			spyCatApi.close();
		} catch (IOException e) {
			logger.error("Unable to close ISpyB SpyCat API connection", e);
		}
		spyCatApi = null;
	}

	public void setSpyCatService(IspybFactoryService<IspybSpyCatApi> service) {
		logger.info("Accessing ISPyB Plate Factory Service from VMXi LIMS Connector");
		SpyCatApiConnector.spyCatService = service;
		logger.info("Accessing ISPyB Factory Service. Success = " + (null!=service));
	}

	@SuppressWarnings("unused")
	public void unsetSpyCatService(Object serviceRef) {
		logger.info("Release ISPyB SyCat Factory Service from LIMS Connector");
		spyCatService = null;
	}
}
