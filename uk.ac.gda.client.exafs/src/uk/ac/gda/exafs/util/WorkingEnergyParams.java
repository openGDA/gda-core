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

package uk.ac.gda.exafs.util;

import java.io.File;
import java.nio.file.Paths;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.util.exafs.Element;
import uk.ac.gda.beans.exafs.IScanParameters;
import uk.ac.gda.beans.exafs.QEXAFSParameters;
import uk.ac.gda.beans.exafs.XanesScanParameters;
import uk.ac.gda.beans.exafs.XasScanParameters;
import uk.ac.gda.beans.exafs.XesScanParameters;
import uk.ac.gda.beans.microfocus.MicroFocusScanParameters;
import uk.ac.gda.util.beans.xml.XMLHelpers;
import uk.ac.gda.util.beans.xml.XMLRichBean;

/**
 * Class to store values related to the parameters of an energy scan.
 * <li> min and max energy of the scan
 * <li> 'Working energy' - typically the central energy of the scan.
 *</li>
 *
 * These values can be set by passing a scan bean to {@link #getWorkingEnergyParams(IScanParameters, String)}.
 */
public class WorkingEnergyParams {
	private static final Logger logger = LoggerFactory.getLogger(WorkingEnergyParams.class);

	private Double min;
	private Double max;
	private double value;

	public WorkingEnergyParams(double value) {
		setValue(value);
	}

	public WorkingEnergyParams(double min, double max, double value) {
		this.min = min;
		this.max = max;
		this.value = value;
	}

	public void setFromParams(WorkingEnergyParams params) {
		this.min = params.min;
		this.max = params.max;
		this.value = params.value;
	}

	public WorkingEnergyParams() {
	}

	public Double getMin() {
		return min;
	}

	public void setMin(double min) {
		this.min = min;
	}

	public Double getMax() {
		return max;
	}

	public void setMax(double max) {
		this.max = max;
	}

	public double getValue() {
		return value;
	}

	public void setValue(double value) {
		this.value = value;
	}

	/**
	 * Return midpoint value of range [getMin(), getMax()]
	 *
	 * @return midpoint value, or null if either range limit is null.
	 */
	public Double getMidPoint() {
		if (min != null && max != null) {
			return min + 0.5*(max - min);
		}
		return null;
	}

	@Override
	public String toString() {
		return "WorkingEnergyParams [min=" + min + ", max=" + max + ", value=" + value + "]";
	}

	/**
	 * Setup the start, end and 'working energy' from the given scan parameters.
	 * 'Working energy' is :
	 * <li> central energy of the scan (XES scan with mono, QExafs)
	 * <li> Edge energy (XAS, XANES scan, XES scans based on XAS, XANES parameters)
	 * <li> Incident photon energy (Microfocus scans)
	 *
	 * @param params (i.e. a {@link XanesScanParameters}, {@link XasScanParameters}, {@link XesScanParameters},
	 * {@link MicroFocusScanParameters} or {@link QEXAFSParameters} object).
	 *
	 * @param folderPath folder containing the scan files (required for loading XAS, XANES params for XES scans).
	 * @return WorkingEnergyParams
	 *
	 * @throws Exception if params object is not of expected type or if some other error occurred.
	 */
	public WorkingEnergyParams getWorkingEnergyParams(IScanParameters params, String folderPath) throws Exception {
		if (params instanceof XanesScanParameters) {
			return new WorkingEnergyXanes((XanesScanParameters)params);
		} else if (params instanceof XasScanParameters) {
			return new WorkingEnergyXas((XasScanParameters) params);
		} else if (params instanceof XesScanParameters) {
			return new WorkingEnergyXes(folderPath, (XesScanParameters) params);
		} else if (params instanceof MicroFocusScanParameters) {
			MicroFocusScanParameters mfParams = (MicroFocusScanParameters) params;
			return new WorkingEnergyParams(mfParams.getEnergy());
		} else if (params instanceof QEXAFSParameters) {
			return new WorkingEnergyQexafs((QEXAFSParameters) params);
		}
		throw new IllegalArgumentException("IScanParameter object should be an instance of Xas, Xanes, Xes, MicroFocus or Qexafs");
	}

	private class WorkingEnergyXas extends WorkingEnergyParams {
		public WorkingEnergyXas(XasScanParameters params) {
			super();
			logger.debug("Getting working energy parameters from Xas scan");
			String element = params.getElement();
			String edge = params.getEdge();
			final Element ele = Element.getElement(element);
			setMin(ele.getInitialEnergy(edge));
			setMax(ele.getFinalEnergy(edge));
			setValue(ele.getEdgeEnergy(edge));
		}
	}

	public class WorkingEnergyXanes extends WorkingEnergyParams {
		public WorkingEnergyXanes(XanesScanParameters params) {
			super();
			logger.debug("Creating working energy parameters from Xanes scan");
			String element = params.getElement();
			String edge = params.getEdge();
			final Element ele = Element.getElement(element);
			setMin(params.getRegions().get(0).getEnergy());
			setMax(params.getFinalEnergy());
			setValue(ele.getEdgeEnergy(edge));
		}
	}

	private class WorkingEnergyXes extends WorkingEnergyParams {
		public WorkingEnergyXes(String folderPath, final XesScanParameters params) throws Exception {
			super();
			logger.debug("Getting working energy parameters from Xes scan");
			XesScanParameters xesparams = params;
			if (xesparams.getScanType() == XesScanParameters.SCAN_XES_FIXED_MONO) {
				setValue(xesparams.getMonoEnergy());
			} else if (xesparams.getScanType() == XesScanParameters.SCAN_XES_SCAN_MONO) {
				setMin(xesparams.getMonoInitialEnergy());
				setMax(xesparams.getMonoFinalEnergy());
				setValue(getMidPoint());
			} else {
				// 1d scan using the Xas/Xanes/Region bean for mono/spectrometer energies
				// --> read the bean from the XML file and generate the parameters from that
				String subscanFileName = xesparams.getScanFileName();
				File subscanFile = Paths.get(folderPath).resolve(subscanFileName).toFile();
				XMLRichBean bean = XMLHelpers.getBean(subscanFile);

				if (bean instanceof XasScanParameters || bean instanceof XanesScanParameters) {
					var p = getWorkingEnergyParams((IScanParameters)bean, "");
					setFromParams(p);
				}
			}
		}
	}

	private class WorkingEnergyQexafs extends WorkingEnergyParams {
		public WorkingEnergyQexafs(QEXAFSParameters params) {
			super();
			logger.debug("Getting working energy parameters from Qexafs scan");
			setMin(params.getInitialEnergy());
			setMax(params.getFinalEnergy());
			setValue(getMidPoint());
		}
	}
}
