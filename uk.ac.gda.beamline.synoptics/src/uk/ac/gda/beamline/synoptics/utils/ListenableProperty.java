/*-
 * Copyright © 2015 Diamond Light Source Ltd.
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

package uk.ac.gda.beamline.synoptics.utils;

import gda.configuration.properties.LocalProperties;
import gda.data.PathConstructor;
import gda.jython.JythonServerFacade;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.InputStream;

import uk.ac.gda.beamline.synoptics.composites.PropertyValueSelectionCompositeFactory;
/**
 * provide the property change support for a specified property. Change to a property via this object
 * ensure it occurs on on both the Client and Server. 
 * 
 * You can find an example of usage at {@link PropertyValueSelectionCompositeFactory}
 */
public class ListenableProperty {
    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
	private String propertyName;

    public ListenableProperty(String propertyName) {
		this.setPropertyName(propertyName);
	}

	public void addPropertyChangeListener(PropertyChangeListener listener) {
        this.pcs.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        this.pcs.removePropertyChangeListener(listener);
    }

    /**
     * returns current value set for the property. 
     * If property value contains $template$ (like substitution variable) they are interpreted first.
     * For example {@link LocalProperties#GDA_DATAWRITER_DIR}
     * @return
     */
    public String get() {
    	
        String propertyValue = LocalProperties.get(getPropertyName());
        if (getPropertyName().equals(LocalProperties.GDA_DATAWRITER_DIR)) {
        	propertyValue=PathConstructor.createFromTemplate(propertyValue);
        }
		return propertyValue;
    }
    /**
     * set the new value to this property in both server and client application.
     * If the value contains $template$ (like variable substitution) they are interpreted first.
     * Change is only propagated if and only if the value actually changed.
     * 
     * @param newValue
     */
    public void set(String newValue) {
        String oldValue = LocalProperties.get(getPropertyName());
        if (getPropertyName().equals(LocalProperties.GDA_DATAWRITER_DIR)) {
        	oldValue=PathConstructor.createFromTemplate(oldValue);
        }
        if (!newValue.equals(oldValue)) {
        	//client side property change
        	LocalProperties.set(getPropertyName(), newValue);
        	this.pcs.firePropertyChange(getPropertyName(), oldValue, newValue);
        	//server side property change
        	InputStream file=getClass().getResourceAsStream("setProperty.py");
        	String command=JythonServerFacade.slurp(file);
        	command=command+System.getProperty("line.separator")+"setProperty(\""+getPropertyName()+"\",\""+newValue+"\")";
        	JythonServerFacade.getInstance().runCommand(command);
        }
    }

	public String getPropertyName() {
		return propertyName;
	}

	public void setPropertyName(String propertyName) {
		this.propertyName = propertyName;
	}

}
