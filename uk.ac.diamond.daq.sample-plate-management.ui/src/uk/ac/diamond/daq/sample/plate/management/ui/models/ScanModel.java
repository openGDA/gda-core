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

package uk.ac.diamond.daq.sample.plate.management.ui.models;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.Arrays;

import org.dawnsci.plotting.system.LineTraceImpl;
import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.widgets.Composite;

import uk.ac.diamond.daq.sample.plate.management.ui.factory.SetParamBuilder;
import uk.ac.diamond.daq.sample.plate.management.ui.widgets.ParamComposite;
import uk.ac.diamond.daq.sample.plate.management.ui.widgets.ShapeComposite;

public class ScanModel {

	private int id;

	private String name = "Untitled";

	private String analyser;

	private CTabItem shapeTabItem;

	private CTabItem paramTabItem;

	private PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);

	public ScanModel(int id, CTabItem shapeTabItem, String analyser, CTabItem paramTabItem) {
		this.id = id;
		this.shapeTabItem = shapeTabItem;
		this.analyser = analyser;
		this.paramTabItem = paramTabItem;
	}

	public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
		propertyChangeSupport.addPropertyChangeListener(propertyName, listener);
	}

	public void removePropertyChangeListener(PropertyChangeListener listener) {
		propertyChangeSupport.removePropertyChangeListener(listener);
	}

	public CTabItem getShapeTabItem() {
		return shapeTabItem;
	}

	public String getAnalyser() {
		return analyser;
	}

	public CTabItem getParamTabItem() {
		return paramTabItem;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		propertyChangeSupport.firePropertyChange("id", this.id, this.id = id);
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		propertyChangeSupport.firePropertyChange("scanName", this.name, this.name = name);
	}

	public String getMotorCoordsFormatted(IPlottingSystem<Composite> plot, double[] xCalibratedAxis, double[] yCalibratedAxis) {
		ArrayList<Double[]> motorCoords = getMotorCoords(plot, xCalibratedAxis, yCalibratedAxis);
		String str = "";
		str += "pos_motors (";
		for (int i = 0; i < motorCoords.size(); i++) {
			if (i != 0) {
				str += ", ";
			}
			Double[] coords = motorCoords.get(i);
			str += Arrays.toString(coords);
		}
		str += ") ";
		return str;
	}

	public ArrayList<Double[]> getMotorCoords(IPlottingSystem<Composite> plot, double[] xCalibratedAxis, double[] yCalibratedAxis) {
		ArrayList<Double[]> motorCoords = new ArrayList<>();
		String regionName = shapeTabItem.getText();
		IDataset datasetX = ((LineTraceImpl) plot.getTrace(regionName)).getXData();
		IDataset datasetY = ((LineTraceImpl) plot.getTrace(regionName)).getYData();
		for (int i = 0; i < datasetX.getSize(); i++) {
			Double[] state = new Double[3];
			state[0] = xCalibratedAxis[datasetX.getInt(i)];
			state[1] = yCalibratedAxis[datasetY.getInt(i)];
			state[2] = ((ShapeComposite) shapeTabItem.getControl()).getThickness();
			motorCoords.add(state);
		}
		return motorCoords;
	}

	public String posPgmEnergyStart() {
		for (AbstractParam setParam: ((ParamComposite) paramTabItem.getControl()).getParams(new SetParamBuilder())) {
			if (setParam.getName().equals("pgm_energy")) {
				return "pos " + setParam.getName() + " " + ((SetParam) setParam).getFirstValue() + "\n";
			}
		}
		return null;
	}

	public String openFastShutter() {
		return "pos fast_shutter 'Open'\n";
	}

	public String closeFastShutter() {
		return "pos fast_shutter 'Close'\n";
	}

	public static String initScript(String[] motors) {
		String str = "from gda.device.scannable.scannablegroup import ScannableGroup\n\n";
		str += "pos_motors = ScannableGroup()\n"
				+ "for member in " + Arrays.toString(motors) + ":\n"
				+ "    pos_motors.addGroupMember(member)\n"
				+ "pos_motors.setName(\"pos_motors\")\n\n";
		return str;
	}

	public boolean isLike(final Object obj) {
		if (obj == null) return false;
		if (!(obj instanceof ScanModel)) return false;
		if (this == obj) return true;
		ScanModel scanModel = (ScanModel) obj;
		if (!scanModel.analyser.equals(analyser)) return false;
		if (!scanModel.shapeTabItem.equals(shapeTabItem)) return false;
		if (!scanModel.paramTabItem.equals(paramTabItem)) return false;
		return true;
	}
}
