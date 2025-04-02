/*-
 * Copyright © 2024 Diamond Light Source Ltd.
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

package gda.util;

import gda.device.DeviceException;
import gda.exafs.xes.XesUtils;
import gda.util.CrystalParameters.CrystalMaterial;
import gda.util.CrystalParameters.CrystalSpacing;
import uk.ac.gda.beans.exafs.QEXAFSParameters;

public class GenerateScanTemplate {

	private String fileNameFormat = "%s_%s_%s_%s_QEXAFS_Parameters.xml"; // element_edge_finalEnergy_crystal_

	// default value from create_templates.py - save_qexafs_xml() function.
	private double relInit = -200.;
	private double relEnd = 1000.;
	private double numPoints = 4000;
	private double time = 180.;

	// generate new file via element, edge and crystal.
	public QEXAFSParameters generateQexafs(String element, String edge, String crystal) throws Exception {
		double edgeEnergy = XrayLibHelper.getEdgeEnergy(element, edge);
		double initEnergy = edgeEnergy + getRelInit();
		double finalEnergy = edgeEnergy + getRelEnd();
		double step = Math.ceil((finalEnergy - initEnergy) / getNumPoints() * 100.) / 100;
		double rounding = Math.pow(10, 3);
		double speed = Math.ceil((convertEnergyToAngle(initEnergy, crystal) - convertEnergyToAngle(finalEnergy, crystal)) / getTime() * 1000. * rounding)
				/ rounding;
		double scantime = Math.ceil((convertEnergyToAngle(initEnergy, crystal) - convertEnergyToAngle(finalEnergy, crystal)) / speed * 1000. * 10) / 10;

		QEXAFSParameters qxfPara = new QEXAFSParameters();
		qxfPara.setInitialEnergy(initEnergy);
		qxfPara.setFinalEnergy(finalEnergy);
		qxfPara.setSpeed(speed);
		qxfPara.setStepSize(step);
		qxfPara.setTime(scantime);
		qxfPara.setElement(element);
		qxfPara.setEdge(edge);

		return qxfPara;
	}

	public String updateFileName(QEXAFSParameters qexafsParams, String crystal) {
		return fileNameFormat.formatted(qexafsParams.getElement(), qexafsParams.getEdge(), (int)qexafsParams.getFinalEnergy(), crystal);
	}

	private double convertEnergyToAngle(double energyEv, String crystal) throws DeviceException {
		return XesUtils.getBragg(energyEv, getMaterialType(crystal), getCrystalCut(crystal));
	}

	private CrystalMaterial getMaterialType(String crystal) throws DeviceException {
		if (crystal.contains("Si"))
			return CrystalMaterial.SILICON;
		else if (crystal.contains("Ge"))
			return CrystalMaterial.GERMANIUM;
		throw new DeviceException("Material type could not be determined");
	}

	private int[] getCrystalCut(String crystal) {
		String cut = null;
		if (crystal.contains("111"))
			cut = CrystalSpacing.Si_111.getLabel();
		else if (crystal.contains("311"))
			cut = CrystalSpacing.Si_311.getLabel();

		return cut.chars().map(Character::getNumericValue).toArray();
	}

	public double getRelInit() {
		return relInit;
	}

	public void setRelInit(double relInit) {
		this.relInit = relInit;
	}

	public double getRelEnd() {
		return relEnd;
	}

	public void setRelEnd(double relEnd) {
		this.relEnd = relEnd;
	}

	public double getNumPoints() {
		return numPoints;
	}

	public void setNumPoints(double numPoints) {
		this.numPoints = numPoints;
	}

	public double getTime() {
		return time;
	}

	public void setTime(double time) {
		this.time = time;
	}
}
