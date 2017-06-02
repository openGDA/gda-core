/*-
 * Copyright © 2017 Diamond Light Source Ltd.
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

package uk.ac.gda.server.ncd.meta;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.XMLConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.eventbus.Subscribe;

import gda.data.metadata.IMetadataEntry;
import gda.device.detector.NXDetectorData;
import gda.factory.FactoryException;
import gda.observable.IObserver;
import gda.util.persistence.LocalParameters;
import uk.ac.diamond.daq.msgbus.MsgBus;
import uk.ac.gda.server.ncd.msg.NcdMetaType;
import uk.ac.gda.server.ncd.msg.NcdMetadataMsg;
import uk.ac.gda.server.ncd.msg.NcdMsg;
import uk.ac.gda.server.ncd.msg.NcdMsgFactory;

public abstract class PerVisitExternalNcdMetadata extends NcdMetaBaseProvider implements IObserver, ExternalMetadataFile {
	private static final Logger logger = LoggerFactory.getLogger(PerVisitExternalNcdMetadata.class);
	private static final String FILE = "file";
	private static final String INTERNAL = "internal";

	protected XMLConfiguration config;
	private String configFileName;
	private IMetadataEntry visit;

	private String detectorType;
	private NcdMetaType metaType;

	private String filepath;
	private String internalPath;
	private NcdMsgFactory msg;

	@Override
	public void configure() throws FactoryException {
		msg = new NcdMsgFactory(detectorType, metaType);
		try {
			config = LocalParameters.getXMLConfiguration(configFileName);
			config.setAutoSave(true);
			restore();
		} catch (ConfigurationException | IOException e) {
			logger.error("Could not access XML config file", e);
		}
		visit.addIObserver(this);
		MsgBus.subscribe(this);
	}

	private boolean isSet() {
		return filepath != null;
	}

	@Override
	public void setExternalFile(String filepath) {
		setExternalFile(filepath, null);
	}

	@Override
	public void setExternalFile(String filepath, String internal) {
		setExternalFile(filepath, internal, true);
	}

	/**
	 * Internal method to set the values of the file and internals paths
	 * Optionally update the modification time stored in the parameters file
	 *
	 * @param filepath the path of the file on disk
	 * @param internal the internal path - can be null if not required
	 * @param modification whether this should be counted as a modification - if so update timestamp
	 */
	private void setExternalFile(String filepath, String internal, boolean modification) {
		if (filepath != null) {
			checkFile(filepath, internal);
		}
		this.filepath = filepath;
		this.internalPath = filepath == null ? null : internal;
		config.setProperty(getPathKey(FILE), filepath);
		config.setProperty(getPathKey(INTERNAL), internalPath);
		if (modification) {
			config.setProperty(getPathKey("modified"), Calendar.getInstance().getTime());
		}
		publishStatus();
	}

	@Override
	public void clear() {
		setExternalFile(null, null);
	}

	public void restore() {
		String file = config.getString(getPathKey(FILE), null);
		String inner = config.getString(getPathKey(INTERNAL), null);
		setExternalFile(file, inner, false);
	}

	private String getPathKey(String key) {
		// needs 'visit-' as prefix as tags can't start with numbers and GDA defaults to 0-0
		// [@att] adds references the attribute of the tag
		// stores as <visit-ab12345 file="x/y/z" internal="/a/b/e" />
		return String.format("visit-%s.%s[@%s]", visit.getMetadataValue(), getName(), key);
	}

	@Subscribe
	public void refresh(NcdMsg.Refresh ref) {
		if (acceptMsg(ref)) {
			publishStatus();
		}
	}

	@Subscribe
	public void requestChange(NcdMsg.ChangeRequest req) {
		if (acceptMsg(req)) {
			setExternalFile(req.getFilepath(), req.getInternalPath());
		}
	}

	private void publishStatus() {
		MsgBus.publish(msg.update(filepath, internalPath));
	}

	private boolean acceptMsg(NcdMetadataMsg msg) {
		boolean rightDetector = msg.getDetectorType() != null && msg.getDetectorType().equals(detectorType);
		boolean rightType = msg.getMetaType() == metaType;
		return rightDetector && rightType;
	}

	@Override
	public void update(Object source, Object arg) {
		restore();
	}

	public void setConfigFileName(String configFileName) {
		this.configFileName = configFileName;
	}

	public void setDetectorType(String detectorType) {
		this.detectorType = detectorType;
	}

	public void setMetaType(NcdMetaType metaType) {
		this.metaType = metaType;
	}

	public void setVisitMeta(IMetadataEntry visit) {
		this.visit = visit;
	}

	public String getFilepath() {
		return filepath;
	}

	public String getInternalPath() {
		return internalPath;
	}

	@Override
	public void writeout(NXDetectorData nxdata, String treeName) {
		if (isSet()) {
			write(nxdata, treeName);
		}
	}

	@Override
	public String toString() {
		if (isSet()) {
			return String.format("%s: %s#%s", getName(), filepath, internalPath);
		}
		return String.format("%s: n/a", getName());
	}

	/**
	 * Check file is valid before accepting it. Base implementation just checks file exists.
	 * Subclasses can perform further checks including of internal path. Should throw exception
	 * if not valid.
	 *
	 * @param file
	 * @param internal
	 *
	 * @throws IllegalArgumentException if file is not valid
	 */
	protected void checkFile(String file, @SuppressWarnings("unused") String internal) {
		if (!new File(file).exists()) {
			throw new IllegalArgumentException("File " + file + " does not exist");
		}
	}

	public void __call__(String ext, String inner) {
		setExternalFile(ext, inner);
	}
	public void __call__(String ext) {
		setExternalFile(ext);
	}

	protected abstract void write(NXDetectorData nxdata, String treeName);
}
