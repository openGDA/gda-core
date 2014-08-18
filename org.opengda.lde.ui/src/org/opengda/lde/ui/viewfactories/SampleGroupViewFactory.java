/*-
 * Copyright © 2011 Diamond Light Source Ltd.
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

package org.opengda.lde.ui.viewfactories;

import java.util.List;

import javax.jms.IllegalStateException;

import gda.rcp.views.FindableExecutableExtension;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.opengda.lde.ui.utils.LDEResourceUtil;
import org.opengda.lde.ui.views.SampleGroupView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Factory method that create the View object
 */
public class SampleGroupViewFactory implements FindableExecutableExtension {

	private final Logger logger = LoggerFactory.getLogger(SampleGroupViewFactory.class);
	private String viewPartName;
	private String name;
	private LDEResourceUtil resUtil;
	private String dataDriver;
	private String dataFolder;
	private String beamlineID;
	private List<String> cellIDs;
	private List<String> calibrantNames;

	public String getViewPartName() {
		return viewPartName;
	}

	public void setViewPartName(String viewPartName) {
		this.viewPartName = viewPartName;
	}

	@Override
	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String getName() {
		return this.name;
	}

	@Override
	public Object create() throws CoreException {
		logger.info("Creating sequence editor view");
		SampleGroupView sampleGroupView = new SampleGroupView();
		sampleGroupView.setViewPartName(viewPartName);
		sampleGroupView.setResUtil(resUtil);
		if (getDataDriver()!=null) {
			sampleGroupView.setDataDriver(getDataDriver());
		}
		if (getDataFolder() != null) {
			sampleGroupView.setDataFolder(getDataFolder());
		}
		if (getBeamlineID() != null) {
			sampleGroupView.setBeamlineID(beamlineID);;
		}
		sampleGroupView.setCellIDs(cellIDs.toArray(new String[] {}));
		sampleGroupView.setCalibrants(calibrantNames.toArray(new String[] {}));
		return sampleGroupView;
	}

	@Override
	public void setInitializationData(IConfigurationElement config,
			String propertyName, Object data) throws CoreException {
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		if (resUtil==null) {
			throw new IllegalStateException("LDE resource util must NOT be null.");
		}
	}

	public LDEResourceUtil getResUtil() {
		return resUtil;
	}

	public void setResUtil(LDEResourceUtil resUtil) {
		this.resUtil = resUtil;
	}

	public String getDataDriver() {
		return dataDriver;
	}

	public void setDataDriver(String dataDriver) {
		this.dataDriver = dataDriver;
	}

	public String getDataFolder() {
		return dataFolder;
	}

	public void setDataFolder(String dataFolder) {
		this.dataFolder = dataFolder;
	}

	public String getBeamlineID() {
		return beamlineID;
	}

	public void setBeamlineID(String beamlineID) {
		this.beamlineID = beamlineID;
	}

	public List<String> getCellIDs() {
		return cellIDs;
	}

	public void setCellIDs(List<String> cellIDs) {
		this.cellIDs = cellIDs;
	}

	public List<String> getCalibrantNames() {
		return calibrantNames;
	}

	public void setCalibrantNames(List<String> calibrantNames) {
		this.calibrantNames = calibrantNames;
	}


}