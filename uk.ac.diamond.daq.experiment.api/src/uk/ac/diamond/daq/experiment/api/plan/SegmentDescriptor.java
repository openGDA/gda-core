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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import uk.ac.diamond.daq.experiment.api.remote.Inequality;
import uk.ac.diamond.daq.experiment.api.remote.SegmentRequest;
import uk.ac.diamond.daq.experiment.api.remote.SignalSource;
import uk.ac.diamond.daq.experiment.api.remote.TriggerRequest;
import uk.ac.diamond.daq.experiment.api.ui.EditableWithListWidget;

public class SegmentDescriptor implements EditableWithListWidget, SegmentRequest, PropertyChangeListener {

	public static final String NAME_PROPERTY = "name";
	public static final String SOURCE_PROPERTY = "signalSource";
	public static final String DURATION_PROPERTY = "duration";
	public static final String SEV_PROPERTY = "sampleEnvironmentVariableName";
	public static final String INEQUALITY_PROPERTY = "inequality";
	public static final String INEQUALITY_REFERENCE_PROPERTY = "inequalityArgument";
	public static final String TRIGGERS_PROPERTY = "triggers";

	private static final long serialVersionUID = 4022241468104721756L;
	private String name;
	private SignalSource source;

	private String sevName;
	private Inequality ineq = Inequality.LESS_THAN;
	private double ineqRef;

	private double duration;

	private List<TriggerDescriptor> triggers = new ArrayList<>();

	private final PropertyChangeSupport pcs;

	public SegmentDescriptor() {
		pcs = new PropertyChangeSupport(this);
	}

	@Override
	public String getName() {
		return name;
	}

	public void setName(String name) {
		String oldName = this.name;
		this.name = name;
		pcs.firePropertyChange(NAME_PROPERTY, oldName, this.name);
		pcs.firePropertyChange(REFRESH_PROPERTY, oldName, this.name);
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
	public Inequality getInequality() {
		return ineq;
	}

	public void setInequality(Inequality ineq) {
		Inequality old = this.ineq;
		this.ineq = ineq;
		pcs.firePropertyChange(INEQUALITY_PROPERTY, old, ineq);
	}

	@Override
	public double getInequalityArgument() {
		return ineqRef;
	}

	public void setInequalityArgument(double ineqRef) {
		double old = this.ineqRef;
		this.ineqRef = ineqRef;
		pcs.firePropertyChange(INEQUALITY_REFERENCE_PROPERTY, old, ineqRef);
	}

	@Override
	public double getDuration() {
		return duration;
	}

	public void setDuration(double duration) {
		double old = this.duration;
		this.duration = duration;
		pcs.firePropertyChange(DURATION_PROPERTY, old, duration);
	}

	@Override
	public SignalSource getSignalSource() {
		return source;
	}

	public void setSignalSource(SignalSource source) {
		SignalSource old = this.source;
		this.source = source;
		pcs.firePropertyChange(SEV_PROPERTY, old, source);
	}

	public List<TriggerDescriptor> getTriggers() {
		return triggers;
	}

	@Override
	public List<TriggerRequest> getTriggerRequests() {
		return triggers.stream()
					.map(TriggerRequest.class::cast)
					.collect(Collectors.toList());
	}

	public void setTriggers(List<TriggerDescriptor> triggers) {
		List<TriggerDescriptor> old = this.triggers;
		if (old != null) {
			old.forEach(trigger -> trigger.removePropertyChangeListener(this));
		}
		triggers.forEach(trigger -> trigger.addPropertyChangeListener(this));
		this.triggers = triggers;
		pcs.firePropertyChange(TRIGGERS_PROPERTY, old, triggers);
	}

	@Override
	public String getLabel() {
		return getName();
	}

	@Override
	public EditableWithListWidget createDefault() {
		SegmentDescriptor model = new SegmentDescriptor();
		model.setName("Unnamed segment");
		model.setSignalSource(SignalSource.TIME);
		model.setInequality(Inequality.LESS_THAN);
		return model;
	}

	@Override
	public void addPropertyChangeListener(PropertyChangeListener listener) {
		pcs.addPropertyChangeListener(listener);
	}

	@Override
	public void removePropertyChangeListener(PropertyChangeListener listener) {
		pcs.removePropertyChangeListener(listener);
	}

	@Override
	public void propertyChange(PropertyChangeEvent event) {
		// when triggers change, I want my listeners to know about it
		pcs.firePropertyChange(event);
	}
}
