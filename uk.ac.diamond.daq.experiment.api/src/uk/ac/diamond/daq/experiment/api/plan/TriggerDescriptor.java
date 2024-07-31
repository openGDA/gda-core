/*-
 * Copyright © 2020 Diamond Light Source Ltd.
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

package uk.ac.diamond.daq.experiment.api.plan;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.UUID;

import uk.ac.diamond.daq.experiment.api.remote.ExecutionPolicy;
import uk.ac.diamond.daq.experiment.api.remote.SignalSource;
import uk.ac.diamond.daq.experiment.api.remote.TriggerRequest;
import uk.ac.diamond.daq.experiment.api.ui.ExperimentUIConstants;

public class TriggerDescriptor implements TriggerRequest {

	private static final long serialVersionUID = 1545993638702697236L;

	public static final String NAME_PROPERTY = "name";
	public static final String SCAN_PROPERTY = "scanId";
	public static final String SOURCE_PROPERTY = "signalSource";
	public static final String EXECUTION_POLICY_PROPERTY = "executionPolicy";
	public static final String SEV_PROPERTY = "sampleEnvironmentVariableName";
	public static final String TARGET_PROPERTY = "target";
	public static final String TOLERANCE_PROPERTY = "tolerance";
	public static final String INTERVAL_PROPERTY = "interval";
	public static final String OFFSET_PROPERTY = "offset";

	private final PropertyChangeSupport pcs;

	private UUID id;
	private UUID parent;

	private String name;
	private UUID scanId;
	private SignalSource source = SignalSource.TIME;
	private ExecutionPolicy policy = ExecutionPolicy.SINGLE;
	private String sevName;
	private double target;
	private double tolerance;
	private double interval;
	private double offset;

	public TriggerDescriptor() {
		pcs = new PropertyChangeSupport(this);
	}

	@Override
	public UUID getComponentId() {
		return id;
	}

	public void setComponentId(UUID id) {
		this.id = id;
	}

	@Override
	public UUID getParentId() {
		return parent;
	}

	public void setParentId(UUID parent) {
		this.parent = parent;
	}

	@Override
	public String getName() {
		return name;
	}

	public void setName(String name) {
		String oldName = this.name;
		this.name = name;
		pcs.firePropertyChange(NAME_PROPERTY, oldName, this.name);
		pcs.firePropertyChange(ExperimentUIConstants.REFRESH_PROPERTY, oldName, name);
	}

	public void setScanId(UUID scanId) {
		var old = this.scanId;
		this.scanId = scanId;
		pcs.firePropertyChange(SCAN_PROPERTY, old, this.scanId);
	}

	@Override
	public UUID getScanId() {
		return scanId;
	}

	@Override
	public String getSampleEnvironmentVariableName() {
		return sevName;
	}

	public void setSampleEnvironmentVariableName(String sevName) {
		String old = this.sevName;
		this.sevName = sevName;
		pcs.firePropertyChange(SEV_PROPERTY, old, sevName);
	}

	@Override
	public double getTarget() {
		return target;
	}

	public void setTarget(double target) {
		double old = this.target;
		this.target = target;
		pcs.firePropertyChange(TARGET_PROPERTY, old, target);
	}

	@Override
	public double getTolerance() {
		return tolerance;
	}

	public void setTolerance(double tolerance) {
		double old = this.tolerance;
		this.tolerance = tolerance;
		pcs.firePropertyChange(TOLERANCE_PROPERTY, old, tolerance);
	}

	@Override
	public double getInterval() {
		return interval;
	}

	public void setInterval(double interval) {
		double old = this.interval;
		this.interval = interval;
		pcs.firePropertyChange(INTERVAL_PROPERTY, old, interval);
	}

	@Override
	public double getOffset() {
		return offset;
	}

	public void setOffset(double offset) {
		double old = this.offset;
		this.offset = offset;
		pcs.firePropertyChange(OFFSET_PROPERTY, old, offset);
	}

	@Override
	public SignalSource getSignalSource() {
		return source;
	}

	public void setSignalSource(SignalSource source) {
		SignalSource old = this.source;
		this.source = source;
		pcs.firePropertyChange(SOURCE_PROPERTY, old, source);
	}

	@Override
	public ExecutionPolicy getExecutionPolicy() {
		return policy;
	}

	public void setExecutionPolicy(ExecutionPolicy policy) {
		ExecutionPolicy old = this.policy;
		this.policy = policy;
		pcs.firePropertyChange(EXECUTION_POLICY_PROPERTY, old, policy);
	}

	public void addPropertyChangeListener(PropertyChangeListener listener) {
		pcs.addPropertyChangeListener(listener);
	}

	public void removePropertyChangeListener(PropertyChangeListener listener) {
		pcs.removePropertyChangeListener(listener);
	}

}
