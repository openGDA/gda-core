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

package uk.ac.diamond.daq.mapping.ui.xanes;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

import com.github.tschoonj.xraylib.Xraylib;

import gda.factory.Findable;
import gda.factory.FindableBase;

/**
 * Wraps a list of {@link XanesElement} in a {@link Findable} object
 */
public class XanesElementsList extends FindableBase implements InitializingBean {

	private static final Logger logger = LoggerFactory.getLogger(XanesElementsList.class);

	private Map<Integer, String> shells;

	private double minEnergyKev = 2.0;
	private double maxEnergyKev = 21.0;
	private int minAtomicNumber = 15;
	private int maxAtomicNumber = 95;

	private boolean generateList = false;

	public XanesElementsList() {
		shells = new LinkedHashMap<>();
		shells.put(Xraylib.K_SHELL, "K");
		shells.put(Xraylib.L3_SHELL, "L");
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		if (generateList) {
			generateElementList();
		}
	}

	private List<XanesElement> xanesElements;

	public List<XanesElement> getXanesElements() {
		return xanesElements;
	}

	public void setXanesElements(List<XanesElement> xanesElements) {
		this.xanesElements = xanesElements;
	}

	/**
	 * Generate list of XanesElements by looping over all elements and adding new XanesElement to list if the K or L edge
	 * energy lies between {@link #minEnergyKev} and {@link #maxEnergyKev}.
	 */
	private void generateElementList() {
		logger.debug("Generating list of Xanes elements and K, L edges : Z = {} ... {}, energy range = {} ... {} keV",
				minAtomicNumber, maxAtomicNumber, minEnergyKev, maxEnergyKev);

		xanesElements = new ArrayList<>();

		for (int atomicNumber = minAtomicNumber; atomicNumber <= maxAtomicNumber; atomicNumber++) {

			int z = atomicNumber; // atomic number needs to be local to the scope of the stream

			// make list of shells within acceptable energy range
			List<String> shellList = shells.entrySet()
				.stream()
				.filter(entry -> shellInRange(z, entry.getKey()))
				.map(Entry::getValue)
				.toList();

			if (shellList.isEmpty()) {
				logger.debug("No edges found within energy range for {}", Xraylib.AtomicNumberToSymbol(atomicNumber));
			} else {
				XanesElement element = new XanesElement();
				element.setElementName(Xraylib.AtomicNumberToSymbol(atomicNumber));
				element.setEdges(shellList);
				element.setRadioactive(isRadioactive(atomicNumber));
				logger.debug("{}", element);

				xanesElements.add(element);
			}
		}
	}

	private boolean shellInRange(int atomicNumber, int shell) {
		double energy = Xraylib.EdgeEnergy(atomicNumber, shell);
		return energy > minEnergyKev && energy < maxEnergyKev;
	}

	/**
	 * Function to determine if an element is always radioactive :
	 * <li>All elements starting from Po(Z=84) are radioactive,
	 * <li> Tc(Z=43) and Pm(Z=61) are also radioactive.
	 *
	 * @param atomicNumber
	 * @return true if element with given atomicNumber is always radioactive
 	 */
	private boolean isRadioactive(int atomicNumber) {
		if (atomicNumber == 43 || atomicNumber == 61) {
			return true;
		}
		return atomicNumber >= 84;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + Objects.hash(xanesElements);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		XanesElementsList other = (XanesElementsList) obj;
		return Objects.equals(xanesElements, other.xanesElements);
	}

	@Override
	public String toString() {
		return "XanesElementsList [xanesElements=" + xanesElements + "]";
	}

	public double getMinEnergyKev() {
		return minEnergyKev;
	}

	public void setMinEnergyKev(double minEnergyKev) {
		this.minEnergyKev = minEnergyKev;
	}

	public double getMaxEnergyKev() {
		return maxEnergyKev;
	}

	public void setMaxEnergyKev(double maxEnergyKev) {
		this.maxEnergyKev = maxEnergyKev;
	}

	public boolean isGenerateList() {
		return generateList;
	}

	public void setGenerateList(boolean generateList) {
		this.generateList = generateList;
	}
}
