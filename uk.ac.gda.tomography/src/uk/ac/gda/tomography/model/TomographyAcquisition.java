/*-
 * Copyright © 2019 Diamond Light Source Ltd.
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

package uk.ac.gda.tomography.model;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Describes how a tomography acquisition should be performed
 *
 * @author Maurizio Nagni
 */
public class TomographyAcquisition implements Acquisition<TomographyConfiguration>, MementoOriginator<TomographyAcquisition> {

	private String name;
	private URL script;
	private TomographyConfiguration configuration;
	private TomographyReconstruction reconstruction;
	private List<ActionLog> logs;

	public TomographyAcquisition() {
		super();
	}

	public TomographyAcquisition(TomographyAcquisition acquisition) {
		super();
		setName(acquisition.getName());
		try {
			setScript(null);
			if (Objects.nonNull(acquisition.getScript())) {
				setScript(new URL(acquisition.getScript().getProtocol(), acquisition.getScript().getHost(), acquisition.getScript().getFile()));
			}
		} catch (MalformedURLException e1) {
			// e1.printStackTrace();
		}

		setConfiguration(new TomographyConfiguration(acquisition.getConfiguration()));
		setLogs(new ArrayList<>());
		getLogs().stream().forEach(l -> {
			this.logs.add(new ActionLog(l));
		});
	}

	@Override
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public URL getScript() {
		return script;
	}

	public void setScript(URL script) {
		this.script = script;
	}

	@Override
	public TomographyConfiguration getConfiguration() {
		return configuration;
	}

	public void setConfiguration(TomographyConfiguration configuration) {
		this.configuration = configuration;
	}

	public TomographyReconstruction getReconstruction() {
		return reconstruction;
	}

	public void setReconstruction(TomographyReconstruction reconstruction) {
		this.reconstruction = reconstruction;
	}

	@Override
	public List<ActionLog> getLogs() {
		return logs;
	}

	public void setLogs(List<ActionLog> logs) {
		this.logs = logs;
	}

	@Override
	public TomographyMemento<TomographyAcquisition> save() {
		return new TomographyMementoImpl<>(new TomographyAcquisition(this));
	}

	@Override
	public void undo(TomographyMemento<TomographyAcquisition> obj) {
		this.setName(obj.getMemento().getName());
		this.setConfiguration(obj.getMemento().getConfiguration());
		this.setScript(obj.getMemento().getScript());
		this.setReconstruction(obj.getMemento().getReconstruction());
		this.setLogs(obj.getMemento().getLogs());
	}
}
