/*-
 * Copyright © 2009 Diamond Light Source Ltd., Science and Technology
 * Facilities Council Daresbury Laboratory
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

package gda.beamline.beamline;

import gda.beamline.BeamlineInfo;
import gda.configuration.properties.LocalProperties;
import gda.data.NumTracker;
import gda.data.PathConstructor;
import gda.factory.Configurable;
import gda.factory.FactoryException;
import gda.factory.Localizable;
import gda.observable.IObserver;
import gda.observable.ObservableComponent;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class provides access methods for users to query or change beamline parameters used in data collection, such as
 * current data directory, file prefix, etc.
 *
 */
@SuppressWarnings("serial")
public class Beamline implements BeamlineInfo, Configurable, Localizable {
	private static final Logger logger = LoggerFactory.getLogger(Beamline.class);
	private final String FILE_PREFIX = "gda.data.file.prefix";
	private final String FILE_SUFFIX = "gda.data.file.suffix";
	private final String FILE_EXTENSION = "gda.data.file.extension";
	private final String PROJECT = "gda.data.project";
	private final String EXPERIMENT = "gda.data.experiment";

	private String name = "beamline";
	private String filePrefix;
	private String fileSuffix;
	private String fileExtension;
	private String header = null;
	private String subHeader = null;
	private NumTracker runs;
	private String project;
	private String experiment;
	private boolean configured = false;
	private boolean local = false;
	private ObservableComponent observableComponent = new ObservableComponent();
	private boolean configureAtStartup = false;

	/**
	 * constructor
	 */
	public Beamline() {

	}

	@Override
	public void configure() throws FactoryException {
		if (!configured) {
			filePrefix = LocalProperties.get(FILE_PREFIX, "");
			fileSuffix = LocalProperties.get(FILE_SUFFIX, "");
			fileExtension = LocalProperties.get(FILE_EXTENSION, "dat");
			project = LocalProperties.get(PROJECT, "HRPD");
			experiment = LocalProperties.get(EXPERIMENT, "MAC");
			try {
				runs = new NumTracker("tmp");
			} catch (IOException e) {
				logger.error("Cannot instantiate NumTracker.", e);
				throw new FactoryException("Could not create NumTracker for Beamline", e);
			}
			configured = true;
		}

	}

	@Override
	public String getName() {
		return this.name;
	}

	@Override
	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String getDataDir() {
		return PathConstructor.createFromDefaultProperty();
	}

	@Override
	public void setDataDir(String dataDir) {
		// change the GDA system properties for data directory
		LocalProperties.set(PathConstructor.getDefaultPropertyName(), dataDir);
	}

	@Override
	public String getFilePrefix() {
		return filePrefix;
	}

	@Override
	public void setFilePrefix(String filePrefix) {
		this.filePrefix = filePrefix;
		// change the GDA system properties for data file name prefix
		LocalProperties.set(FILE_PREFIX, filePrefix);
	}

	@Override
	public String getFileSuffix() {
		return fileSuffix;
	}

	@Override
	public void setFileSuffix(String fileSuffix) {
		this.fileSuffix = fileSuffix;
		// change the GDA system properties for data file name suffix
		LocalProperties.set(FILE_SUFFIX, fileSuffix);
	}

	@Override
	public String getFileExtension() {
		return fileExtension;
	}

	@Override
	public void setFileExtension(String fileExtension) {
		this.fileExtension = fileExtension;
		// change the GDA system properties for data file extension
		LocalProperties.set(FILE_EXTENSION, fileExtension);
	}

	@Override
	public int getFileNumber() {
		return runs.getCurrentFileNumber();
	}

	@Override
	public int getNextFileNumber() {
		return runs.incrementNumber();
	}

	@Override
	public String getProjectName() {
		return this.project;
	}

	@Override
	public void setProjectName(String project) {
		this.project = project;
		// change the GDA system properties for data file extension
		LocalProperties.set(PROJECT, project);
	}

	@Override
	public String getExperimentName() {
		return this.experiment;
	}

	@Override
	public void setExperimentName(String experiment) {
		this.experiment = experiment;
		// change the GDA system properties for data file extension
		LocalProperties.set(EXPERIMENT, project);
	}

	@Override
	public String getHeader() {
		return header;
	}

	@Override
	public void setHeader(String header) {
		this.header = header;
	}

	@Override
	public String getSubHeader() {
		return subHeader;
	}

	@Override
	public void setSubHeader(String subHeader) {
		this.subHeader = subHeader;
	}

	/**
	 * Checks to see if the created object should be local to the server or whether a corba impl should be instantiated
	 * and placed on the name server.
	 *
	 * @return true for local only objects
	 */
	@Override
	public boolean isLocal() {
		return local;
	}

	/**
	 * Sets a flag to inform the server that the created object should be local to itself or whether a corba impl should
	 * be instantiated and placed on the name server.
	 *
	 * @param local
	 *            true if a local only implementation.
	 */
	@Override
	public void setLocal(boolean local) {
		this.local = local;
	}

	/**
	 * Check whether the configure method should be called when the server is instantiated.
	 *
	 * @return true if configuration is required at startup.
	 */
	public boolean isConfigureAtStartup() {
		return configureAtStartup;
	}

	/**
	 * Set a flag to inform the server whether the configure method should be called at startup.
	 *
	 * @param configureAtStartup
	 *            true to configure at startup.
	 */
	public void setConfigureAtStartup(boolean configureAtStartup) {
		this.configureAtStartup = configureAtStartup;
	}

	@Override
	public void addIObserver(IObserver anIObserver) {
		observableComponent.addIObserver(anIObserver);
	}

	@Override
	public void deleteIObserver(IObserver anIObserver) {
		observableComponent.deleteIObserver(anIObserver);
	}

	@Override
	public void deleteIObservers() {
		observableComponent.deleteIObservers();
	}

	/**
	 * Notify all observers on the list of the requested change.
	 *
	 * @param theObserved
	 *            the observed component
	 * @param theArgument
	 *            the data to be sent to the observer.
	 */
	public void notifyIObservers(Object theObserved, Object theArgument) {
		observableComponent.notifyIObservers(theObserved, theArgument);
	}
}
