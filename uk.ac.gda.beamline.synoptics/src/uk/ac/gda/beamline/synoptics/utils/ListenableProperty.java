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

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.util.stream.Collectors;

import gda.configuration.properties.LocalProperties;
import gda.jython.InterfaceProvider;
import gda.jython.JythonServerFacade;
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
        	propertyValue=InterfaceProvider.getPathConstructor().createFromTemplate(propertyValue);
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
        	oldValue=InterfaceProvider.getPathConstructor().createFromTemplate(oldValue);
        }
        if (!newValue.equals(oldValue)) {
        	//client side property change
        	LocalProperties.set(getPropertyName(), newValue);
        	this.pcs.firePropertyChange(getPropertyName(), oldValue, newValue);
        	//server side property change
        	String command = getFileAsString("setProperty.py");
        	command=command+System.getProperty("line.separator")+"setProperty(\""+getPropertyName()+"\",\""+newValue+"\")";
        	JythonServerFacade.getInstance().runCommand(command);
        }
    }

    /**
     * @param fileName for file located within this class' package
     */
	private String getFileAsString(String fileName) {
		try (InputStream is = getClass().getResourceAsStream(fileName);
				InputStreamReader isr = new InputStreamReader(is);
				BufferedReader br = new BufferedReader(isr);) {
			return br.lines().collect(Collectors.joining(System.lineSeparator()));
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}

	public String getPropertyName() {
		return propertyName;
	}

	public void setPropertyName(String propertyName) {
		this.propertyName = propertyName;
	}

}
