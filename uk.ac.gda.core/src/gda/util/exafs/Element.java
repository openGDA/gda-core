/*-
 * Copyright © 2009 Diamond Light Source Ltd.
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

package gda.util.exafs;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.apache.commons.lang.ArrayUtils;
import org.jscience.physics.quantities.Quantity;
import org.jscience.physics.units.NonSI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.jscience.physics.quantities.PhotonEnergy;
import gda.jscience.physics.units.NonSIext;
import uk.ac.gda.util.io.TokenFileParser;

/**
 * To provide information about the chemical elements.
 */
public final class Element {

	private static Logger logger = LoggerFactory.getLogger(Element.class);

	final static Integer NUMBER_OF_ELEMENTS = 103;

	/**
	 * The Core Holes are being read from a tab separated text file as the old values in this class were wrong and
	 * difficult to check and maintain.
	 */
	/**
	 * K edge Core-hole levels in eV
	 */
	private static Double[] kCoreHoles, l1CoreHoles, l2CoreHoles, l3CoreHoles, m1CoreHoles, m2CoreHoles, m3CoreHoles,
			m4CoreHoles, m5CoreHoles;
	static {
		try {
			final TokenFileParser p = new TokenFileParser(Element.class.getResource("Element-CoreHole.txt"));
			p.parse();
			kCoreHoles = setArrayFromFileParser(NUMBER_OF_ELEMENTS, 0., p, 2);
			l1CoreHoles = setArrayFromFileParser(NUMBER_OF_ELEMENTS, 0., p, 3);
			l2CoreHoles = setArrayFromFileParser(NUMBER_OF_ELEMENTS, 0., p, 4);
			l3CoreHoles = setArrayFromFileParser(NUMBER_OF_ELEMENTS, 0., p, 5);

			m1CoreHoles = setArrayFromFileParser(NUMBER_OF_ELEMENTS, 0., p, 6);
			m2CoreHoles = setArrayFromFileParser(NUMBER_OF_ELEMENTS, 0., p, 7);
			m3CoreHoles = setArrayFromFileParser(NUMBER_OF_ELEMENTS, 0., p, 8);
			m4CoreHoles = setArrayFromFileParser(NUMBER_OF_ELEMENTS, 0., p, 9);
			m5CoreHoles = setArrayFromFileParser(NUMBER_OF_ELEMENTS, 0., p, 10);

		} catch (Throwable e) {
			logger.error("Failed to read Element-CoreHole.txt file. All core holes will be null!",e);
		}
	}
	private static Double[][] coreHoles = { kCoreHoles, l1CoreHoles, l2CoreHoles, l3CoreHoles, m1CoreHoles,
			m2CoreHoles, m3CoreHoles, m4CoreHoles, m5CoreHoles };

	static Double[] setArrayFromFileParser(int maxAtomicNumber, Double defaultValue, TokenFileParser p, int column) {
		Double[] arrayToSet = new Double[maxAtomicNumber];
		Arrays.fill(arrayToSet, defaultValue);

		List<String> atomicNumberFromFile = p.getColumn(0);
		List<String> valuesFromFile = p.getColumn(column);
		for (int i = 0; i < atomicNumberFromFile.size(); i++) {
			// ignore first line
			if (i == 0)
				continue;
			int atomicNumber = Integer.parseInt(atomicNumberFromFile.get(i));
			if (atomicNumber <= maxAtomicNumber)
				arrayToSet[atomicNumber - 1] = Double.parseDouble(valuesFromFile.get(i));
		}
		return arrayToSet;
	}

	private static Double[] kEdgeEnergies, l1EdgeEnergies, l2EdgeEnergies, l3EdgeEnergies, m1EdgeEnergies,
			m2EdgeEnergies, m3EdgeEnergies, m4EdgeEnergies, m5EdgeEnergies;
	static {
		try {
			final TokenFileParser p = new TokenFileParser(Element.class.getResource("Element-Edge.txt"));
			p.parse();
			kEdgeEnergies = setArrayFromFileParser(NUMBER_OF_ELEMENTS, 0., p, 2);
			l1EdgeEnergies = setArrayFromFileParser(NUMBER_OF_ELEMENTS, 0., p, 3);
			l2EdgeEnergies = setArrayFromFileParser(NUMBER_OF_ELEMENTS, 0., p, 4);
			l3EdgeEnergies = setArrayFromFileParser(NUMBER_OF_ELEMENTS, 0., p, 5);
			m1EdgeEnergies = setArrayFromFileParser(NUMBER_OF_ELEMENTS, 0., p, 6);
			m2EdgeEnergies = setArrayFromFileParser(NUMBER_OF_ELEMENTS, 0., p, 7);
			m3EdgeEnergies = setArrayFromFileParser(NUMBER_OF_ELEMENTS, 0., p, 8);
			m4EdgeEnergies = setArrayFromFileParser(NUMBER_OF_ELEMENTS, 0., p, 9);
			m5EdgeEnergies = setArrayFromFileParser(NUMBER_OF_ELEMENTS, 0., p, 10);

		} catch (Throwable e) {
			logger.error("Failed to read Element-Edge.txt file. All core holes will be null!",e);
		}
	}
	private static Double[][] edgeEnergies = { kEdgeEnergies, l1EdgeEnergies, l2EdgeEnergies, l3EdgeEnergies,
			m1EdgeEnergies, m2EdgeEnergies, m3EdgeEnergies, m4EdgeEnergies, m5EdgeEnergies };

	/**
	 *
	 */
	public static final int METAL = 0;

	/**
	 *
	 */
	public static final int NONMETAL = 1;

	/**
	 *
	 */
	public static final int METALLOID = 2;

	/**
	 *
	 */
	public static final int NOBLEGAS = 3;

	/**
	 *
	 */
	public static final int LANTHANIDE = 4;

	/**
	 *
	 */
	public static final int ACTINIDE = 5;

	// private static final int KEDGE = 0;
	// private static final int L1EDGE = 1;
	// private static final int L2EDGE = 2;
	// private static final int L3EDGE = 3;

	private static final String[] edgeNames = { "K", "L1", "L2", "L3", "M1", "M2", "M3", "M4", "M5" };

	private static Map<String, Element> ALL_ELEMENTS;

	/* The only instance field of an Element is an integer */
	/* which is used to look up values in the static tables. */
	/* If Elements become widely used it might be more */
	/* efficient to add instance fields and extract the values */
	/* at construction. */
	/* Array positions are always (atomic number - 1) */

	private final int arrayRef;

	/* Static initializer creates an array of instances, one */
	/* for each real element */

	private static void initElements() {
		if (ALL_ELEMENTS != null) {
			return;
		}
		final Map<String, Element> tmp = new LinkedHashMap<String, Element>(NUMBER_OF_ELEMENTS);
		for (int i = 1; i <= NUMBER_OF_ELEMENTS; i++) {
			final Element ele = new Element(i);
			tmp.put(symbols[i - 1], ele);
		}
		ALL_ELEMENTS = Collections.unmodifiableMap(tmp);
	}

	/**
	 * Constructs an Element from its atomic number. The constructor is private because a static array of all the
	 * elements is created in the static initializer.
	 *
	 * @param atomicNumber
	 *            the atomic number in periodic table
	 */
	private Element(int atomicNumber) {
		arrayRef = atomicNumber - 1;
	}

	/**
	 * Returns the Collection<Element> of all elements in order of atomic number.
	 *
	 * @return the Collection<Element> of all elements
	 */
	public static Collection<Element> getAllElements() {
		initElements();
		return new ArrayList<Element>(ALL_ELEMENTS.values());
	}

	/**
	 * @param from
	 * @param to
	 * @return Elements in range
	 */
	public static Collection<Element> getElements(final int from, final int to) {
		initElements();
		final Collection<Element> ret = new HashSet<Element>((to - from) + 1);
		for (int i = from; i <= to; i++) {
			ret.add(ALL_ELEMENTS.get(symbols[i]));
		}
		return ret;
	}

	/**
	 * Gets the edge name "K" etc which corresponds to the given integer value.
	 *
	 * @param edge
	 *            integer edge value
	 * @return the edge name corresponding to the given integer value
	 */
	public static String edgeName(int edge) {
		return (edgeNames[edge]);
	}

	/**
	 * Gets the atomic number of the element
	 *
	 * @return atomic number
	 */
	public int getAtomicNumber() {
		return arrayRef + 1;
	}

	/**
	 * Gets the symbol of the element
	 *
	 * @return element symbol
	 */
	public String getSymbol() {
		return symbols[arrayRef];
	}

	/**
	 * Gets the name of the element
	 *
	 * @return element name
	 */
	public String getName() {
		return names[arrayRef];
	}

	/**
	 * Gets the type of element (METAL etc)
	 *
	 * @return element type
	 */
	public int getType() {
		return types[arrayRef];
	}

	/**
	 * Gets the atomic radius of the element
	 *
	 * @return atomic radius
	 */
	public double getAtomicRadius() {
		return atomicRadii[arrayRef];
	}

	/**
	 * Gets the covalent radius of the element
	 *
	 * @return covalent radius
	 */
	public double getCovalentRadius() {
		return covalentRadii[arrayRef];
	}

	/**
	 * Gets the atomic mass of the element
	 *
	 * @return atomic mass
	 */
	public double getAtomicMass() {
		return masses[arrayRef];
	}

	/**
	 * Gets the boiling point of the element
	 *
	 * @return boiling point
	 */
	public double getBoilingPT() {
		return boilingPTs[arrayRef];
	}

	/**
	 * Gets the melting point of the element
	 *
	 * @return melting point
	 */
	public double getMeltingPT() {
		return meltingPTs[arrayRef];
	}

	/**
	 * Gets the density of the element
	 *
	 * @return density
	 */
	public double getDensity() {
		return densities[arrayRef];
	}

	/**
	 * Gets the atomic volume of the element
	 *
	 * @return atomic volume
	 */
	public double getAtomicVolume() {
		return volumes[arrayRef];
	}

	/**
	 * Returns the iterator of a vector of the names of the edges which are in the given energy range.
	 *
	 * @param minEnergy
	 *            the beginning of the energy range in eV
	 * @param maxEnergy
	 *            the end of the energy range in eV
	 * @return Vector<String of names
	 */
	public Iterator<String> getEdgesInEnergyRange(double minEnergy, double maxEnergy) {
		List<String> edges = getListOfEdgesInEnergyRange(minEnergy, maxEnergy);
		return (edges.isEmpty()) ? null : edges.iterator();
	}

	/**
	 * Returns of the names of the edges which are in the given energy range.
	 *
	 * @param minEnergy the beginning of the energy range in eV
	 * @param maxEnergy the end of the energy range in eV
	 */
	public List<String> getListOfEdgesInEnergyRange(double minEnergy, double maxEnergy) {
		Vector<String> edges = new Vector<String>();

		for (int i = 0; i < edgeNames.length; i++) {
			if (edgeExists(i) && getEdgeEnergy(i) >= minEnergy && getEdgeEnergy(i) <= maxEnergy)
				edges.add(edgeNames[i]);
		}

		return edges;
	}

	/**
	 * Checks whether a particular edge exists or not
	 *
	 * @param edge
	 *            the selected edge
	 * @return true if edge exists, false if it does not
	 */
	private boolean edgeExists(int edge) {
		return (getEdgeEnergy(edge) > 0);
	}

	/**
	 * Checks whether a particular edge exists or not
	 *
	 * @param edgeName
	 *            the selected edge
	 * @return true if edge exists, false if it does not
	 */
	private boolean edgeExists(String edgeName) {
		int index = ArrayUtils.indexOf(edgeNames, edgeName);
		if (index >= 0){
			return edgeExists(index);
		}
		return false;
	}

	/**
	 * Gets the energy of the given edge in eV
	 *
	 * @param edge
	 *            the edge (KEDGE, LONEEDGE etc)
	 * @return the energy
	 */
	private double getEdgeEnergy(int edge) {

		final Double[] energies = edgeEnergies[edge];
		return energies[arrayRef];
	}

	/**
	 * Gets the energy of the given edge in eV
	 *
	 * @param edgeName
	 *            the edge ("K", "L1" etc)
	 * @return energy value
	 */
	public double getEdgeEnergy(String edgeName) {
		int index = ArrayUtils.indexOf(edgeNames, edgeName);
		if (index >= 0){
			return getEdgeEnergy(index);
		}

		return Double.NaN;
	}

	/**
	 * Gets the energy of the given edge in keV
	 *
	 * @param edgeName
	 *            the edge ("K", "L1" etc)
	 * @return energy value
	 */
	public double getEdgeEnergyInkeV(String edgeName) {
		return getEdgeEnergy(edgeName) / 1000d;
	}

	/**
	 * Returns an estimation in eV of the initial energy to start a monochromatic specroscopy scan.
	 *
	 * @param edge
	 * @return double eV
	 */
	public double getInitialEnergy(String edge) {
		return (getEdgeEnergy(edge)) - 200;
	}

	/**
	 * Returns an estimation in eV of the final energy to end a monochromatic spectroscopy scan.
	 *
	 * @param edge
	 * @return double eV
	 */
	public double getFinalEnergy(String edge) {

		if (edge == null)
			throw new RuntimeException("Edge is null.");

		final double edgeEnergy = (getEdgeEnergy(edge));
		if ("K".equals(edge)) { // K
			return edgeEnergy + 850;

		} else if ("L1".equals(edge)) { // L1
			// fix L1 final energy at 15 A-1
			Quantity edgeInEV = Quantity.valueOf(edgeEnergy, NonSI.ELECTRON_VOLT);
			Quantity k = Quantity.valueOf(15, NonSIext.PER_ANGSTROM);
			double finalEnergyInEV = PhotonEnergy.photonEnergyOf(edgeInEV, k).getAmount();
			return finalEnergyInEV;
			// return edgeEnergy + 500;

		} else if ("L2".equals(edge)) { // L2
			return (getEdgeEnergy("L1")) - 10;

		} else if ("L3".equals(edge)) { // L3
			return (getEdgeEnergy("L2")) - 10;

		} else if ("M1".equals(edge)) { // M1
			return edgeEnergy + 500;

		} else if ("M2".equals(edge)) { // M2
			return (getEdgeEnergy("M1")) - 10;

		} else if ("M3".equals(edge)) { // M3
			return (getEdgeEnergy("M2")) - 10;

		} else if ("M4".equals(edge)) { // M4
			return (getEdgeEnergy("M3")) - 10;

		} else if ("M5".equals(edge)) { // M5
			return (getEdgeEnergy("M4")) - 10;
		}
		throw new RuntimeException("Edge '" + edge + "' is not recognised.");
	}

	/**
	 * Returns the allowable edges for an elements
	 *
	 * @return the edges
	 */
	public List<String> getAllowedEdges() {
		final List<String> ret = new ArrayList<String>(4);

		if (getAtomicNumber() >= 15 && getAtomicNumber() <= 54)
			ret.add("K");
		if (getAtomicNumber() >= 37 && getAtomicNumber() <= 93)
			ret.add("L1");
		if (getAtomicNumber() >= 39 && getAtomicNumber() <= 93)
			ret.add("L2");
		if (getAtomicNumber() >= 39 && getAtomicNumber() <= 93)
			ret.add("L3");

		if (getAtomicNumber() >= 66 && getAtomicNumber() <= 93)
			ret.add("M1");
		if (getAtomicNumber() >= 69 && getAtomicNumber() <= 93)
			ret.add("M2");
		if (getAtomicNumber() >= 72 && getAtomicNumber() <= 93)
			ret.add("M3");
		if (getAtomicNumber() >= 77 && getAtomicNumber() <= 93)
			ret.add("M4");
		if (getAtomicNumber() >= 78 && getAtomicNumber() <= 93)
			ret.add("M5");
		return ret;
	}

	/**
	 * @param edgeName
	 * @return core hole level in eV
	 */
	public double getCoreHole(String edgeName) {
		int index = ArrayUtils.indexOf(edgeNames, edgeName);
		if (index >= 0){
			return getCorehole(index);
		}

		return Double.NaN;
	}

	/**
	 * @param edge
	 * @return core hole level in eV
	 */
	public double getCorehole(int edge) {
		return (coreHoles[edge][arrayRef]);
	}

	/**
	 * Creates and returns an AbsorptionEdge
	 *
	 * @param edgeName
	 *            the edge name ("K", "L1", "L2", "L3")
	 * @return an AbsorptionEdge containing the relevant information or null if the edge does not exist
	 */

	public AbsorptionEdge getEdge(String edgeName) {
		AbsorptionEdge rtrn = null;
		double edgeEnergy;

		edgeEnergy = getEdgeEnergy(edgeName);

		if (edgeEnergy != Double.NaN) {
			rtrn = new AbsorptionEdge(getSymbol(), edgeName, edgeEnergy);
		}

		return (rtrn);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + arrayRef;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		Element other = (Element) obj;
		if (arrayRef != other.arrayRef) {
			return false;
		}
		return true;
	}

	/**
	 * Atomic radiuses (in ?)
	 */
	private static final double[] atomicRadii = { 0.79, 0.49, 2.05, 1.4, 1.17, 0.91, 0.75, 0.65, 0.57, 0.51, 2.23,
			1.72, 1.82, 1.46, 1.23, 1.09, 0.97, 0.88, 2.77, 2.23, 2.09, 2.0, 1.92, 1.85, 1.79, 1.72, 1.67, 1.62, 1.57,
			1.53, 1.81, 1.52, 1.33, 1.22, 1.12, 1.03, 2.98, 2.45, 2.27, 2.16, 2.09, 2.01, 1.95, 1.89, 1.83, 1.79, 1.75,
			1.71, 2.0, 1.72, 1.53, 1.42, 1.32, 1.24, 3.34, 2.78, 2.74, 2.7, 2.67, 2.64, 2.62, 2.59, 2.56, 2.54, 2.51,
			2.49, 2.47, 2.45, 2.42, 2.4, 2.25, 2.16, 2.09, 2.02, 1.97, 1.92, 1.87, 1.83, 1.79, 1.76, 2.08, 1.81, 1.63,
			1.53, 1.43, 1.34, 3.5, 3.0, 3.2, 3.16, 3.14, 3.11, 3.08, 3.05, 3.02, 2.99, 2.97, 2.95, 2.92, 0.0, 0.0, 0.0,
			0.0 };

	/**
	 * Covalent radiuses (in ?)
	 */
	private static final double[] covalentRadii = { 0.32, 0.93, 1.23, 0.9, 0.82, 0.77, 0.75, 0.73, 0.72, 0.71, 1.54,
			1.36, 1.18, 1.11, 1.06, 1.02, 0.99, 0.98, 2.03, 1.91, 1.62, 1.45, 1.34, 1.18, 1.17, 1.17, 1.16, 1.15, 1.17,
			1.25, 1.26, 1.22, 1.2, 1.16, 1.14, 1.12, 2.16, 1.91, 1.62, 1.45, 1.34, 1.3, 1.27, 1.25, 1.25, 1.28, 1.34,
			1.48, 1.44, 1.41, 1.4, 1.36, 1.33, 1.31, 2.35, 1.98, 1.69, 1.65, 1.65, 1.64, 1.63, 1.62, 1.85, 1.61, 1.59,
			1.59, 1.58, 1.57, 1.56, 1.74, 1.56, 1.44, 1.34, 1.3, 1.28, 1.26, 1.27, 1.3, 1.34, 1.49, 1.48, 1.47, 1.46,
			1.46, 1.45, 1.43, 2.5, 2.4, 2.2, 1.65, 0.0, 1.42, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0 };

	/**
	 * Masses (in ?)
	 */
	private static final double[] masses = { 1.00794, 4.0026, 6.941, 9.01218, 10.811, 12.011, 14.00674, 15.9994,
			18.9984, 20.1797, 22.98977, 24.305, 26.98154, 28.0855, 30.97362, 32.066, 35.4527, 39.948, 39.0983, 40.078,
			44.95591, 47.88, 50.9415, 51.9961, 54.93085, 55.847, 58.9332, 58.69, 63.546, 65.39, 69.723, 72.61,
			74.92159, 78.96, 79.904, 83.8, 85.4678, 87.62, 88.90585, 91.224, 92.90638, 95.94, 98.0, 101.07, 102.9055,
			106.42, 107.8682, 112.411, 114.82, 118.71, 121.75, 127.6, 126.90447, 131.29, 132.90543, 137.327, 138.9055,
			140.115, 140.90765, 144.24, 145.0, 150.36, 151.965, 157.25, 158.92534, 162.5, 164.93032, 167.26, 168.93421,
			173.04, 174.967, 178.49, 180.9479, 183.85, 186.207, 190.2, 192.22, 195.08, 196.96654, 200.59, 204.3833,
			207.2, 208.98037, 209.0, 210.0, 222.0, 223.0, 226.025, 227.028, 232.0381, 231.03588, 238.0289, 237.048,
			244.0, 243.0, 247.0, 247.0, 251.0, 252.0, 0.0, 0.0, 0.0, 0.0 };

	/**
	 * Standard symbols
	 */
	private static final String[] symbols = { "H", "He", "Li", "Be", "B", "C", "N", "O", "F", "Ne", "Na", "Mg", "Al",
			"Si", "P", "S", "Cl", "Ar", "K", "Ca", "Sc", "Ti", "V", "Cr", "Mn", "Fe", "Co", "Ni", "Cu", "Zn", "Ga",
			"Ge", "As", "Se", "Br", "Kr", "Rb", "Sr", "Y", "Zr", "Nb", "Mo", "Tc", "Ru", "Rh", "Pd", "Ag", "Cd", "In",
			"Sn", "Sb", "Te", "I", "Xe", "Cs", "Ba", "La", "Ce", "Pr", "Nd", "Pm", "Sm", "Eu", "Gd", "Tb", "Dy", "Ho",
			"Er", "Tm", "Yb", "Lu", "Hf", "Ta", "W", "Re", "Os", "Ir", "Pt", "Au", "Hg", "Tl", "Pb", "Bi", "Po", "At",
			"Rn", "Fr", "Ra", "Ac", "Th", "Pa", "U", "Np", "Pu", "Am", "Cm", "Bk", "Cf", "Es", "Fm", "Md", "No", "Lr" };

	/**
	 * Boiling points in Kelvin
	 */
	private static final double[] boilingPTs = { 20.268, 4.215, 1615.0, 2745.0, 4275.0, 4470.0, 77.35, 90.18, 84.95,
			27.096, 1156.0, 1363.0, 2793.0, 3540.0, 550.0, 717.75, 239.1, 87.3, 1032.0, 1757.0, 3104.0, 3562.0, 3682.0,
			2945.0, 2335.0, 3135.0, 3201.0, 3187.0, 2836.0, 1180.0, 2478.0, 3107.0, 876.0, 958.0, 332.25, 119.8, 961.0,
			1650.0, 3611.0, 4682.0, 5017.0, 4912.0, 4538.0, 4423.0, 3970.0, 3237.0, 2436.0, 1040.0, 2346.0, 2876.0,
			1860.0, 1261.0, 458.4, 165.03, 944.0, 2171.0, 3730.0, 3699.0, 3785.0, 3341.0, 3785.0, 2064.0, 1870.0,
			3539.0, 3496.0, 2835.0, 2968.0, 3136.0, 2220.0, 1467.0, 3668.0, 4876.0, 5731.0, 5828.0, 5869.0, 5285.0,
			4701.0, 4100.0, 3130.0, 630.0, 1746.0, 2023.0, 1837.0, 1235.0, 610.0, 211.0, 950.0, 1809.0, 3473.0, 5061.0,
			0.0, 4407.0, 0.0, 3503.0, 2880.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0 };

	/**
	 * Melting points in Kelvin
	 */
	private static final double[] meltingPTs = { 14.025, 0.95, 453.7, 1560.0, 2300.0, 4100.0, 63.14, 50.35, 53.48,
			24.553, 371.0, 922.0, 933.25, 1685.0, 317.3, 388.36, 172.16, 83.81, 336.35, 1112.0, 1812.0, 1943.0, 2175.0,
			2130.0, 1517.0, 1809.0, 1768.0, 1726.0, 1357.6, 692.73, 302.9, 1210.4, 1081.0, 494.0, 265.9, 115.78,
			312.64, 1041.0, 1799.0, 2125.0, 2740.0, 2890.0, 2473.0, 2523.0, 2236.0, 1825.0, 1234.0, 594.18, 429.76,
			505.06, 904.0, 722.65, 386.7, 161.36, 301.55, 1002.0, 1193.0, 1071.0, 1204.0, 1289.0, 1204.0, 1345.0,
			1090.0, 1585.0, 1630.0, 1682.0, 1743.0, 1795.0, 1818.0, 1097.0, 1936.0, 2500.0, 3287.0, 3680.0, 3453.0,
			3300.0, 2716.0, 2045.0, 1337.58, 234.28, 577.0, 600.6, 544.52, 527.0, 575.0, 202.0, 300.0, 973.0, 1323.0,
			2028.0, 0.0, 1405.0, 910.0, 913.0, 1268.0, 1340.0, 0.0, 900.0, 0.0, 0.0, 0.0, 0.0, 0.0 };

	/**
	 * Densities in ?
	 */
	private static final double[] densities = { 0.0899, 0.1787, 0.53, 1.85, 2.34, 2.62, 1.251, 1.429, 1.696, 0.901,
			0.97, 1.74, 2.7, 2.33, 1.82, 2.07, 3.17, 1.784, 0.86, 1.55, 3.0, 4.5, 5.8, 7.19, 7.43, 7.86, 8.9, 8.9,
			8.96, 7.14, 5.91, 5.32, 5.72, 4.8, 3.12, 3.74, 1.53, 2.6, 4.5, 6.49, 8.55, 10.2, 11.5, 12.2, 12.4, 12.0,
			10.5, 8.65, 7.31, 7.3, 6.68, 6.24, 4.92, 5.89, 1.87, 3.5, 6.7, 6.78, 6.77, 7.0, 6.475, 7.54, 5.26, 7.89,
			8.27, 8.54, 8.8, 9.05, 9.33, 6.98, 9.84, 13.1, 16.6, 19.3, 21.0, 22.4, 22.5, 21.4, 19.3, 13.53, 11.85,
			11.4, 9.8, 9.4, 0.0, 9.91, 0.0, 5.0, 10.07, 11.7, 15.4, 18.9, 20.4, 19.8, 13.6, 13.511, 0.0, 0.0, 0.0, 0.0,
			0.0, 0.0, 0.0 };

	/**
	 * (Atomic ?) volumes in ?
	 */
	private static final double[] volumes = { 14.4, 0.0, 13.1, 5.0, 4.6, 4.58, 17.3, 14.0, 17.1, 16.7, 23.7, 13.97,
			10.0, 12.1, 17.0, 15.5, 22.7, 28.5, 45.46, 29.9, 15.0, 10.64, 8.78, 7.23, 1.39, 7.1, 6.7, 6.59, 7.1, 9.2,
			11.8, 13.6, 13.1, 16.45, 23.5, 38.9, 55.9, 33.7, 19.8, 14.1, 10.87, 9.4, 8.5, 8.3, 8.3, 8.9, 10.3, 13.1,
			15.7, 16.3, 18.23, 20.5, 25.74, 37.3, 71.07, 39.24, 20.73, 20.67, 20.8, 20.6, 22.39, 19.95, 28.9, 19.9,
			19.2, 19.0, 18.7, 18.4, 18.1, 24.79, 17.78, 13.6, 10.9, 9.53, 8.85, 8.49, 8.54, 9.1, 10.2, 14.82, 17.2,
			18.17, 21.3, 22.23, 0.0, 50.5, 0.0, 45.2, 22.54, 19.9, 15.0, 12.59, 11.62, 12.32, 17.86, 18.28, 0.0, 0.0,
			0.0, 0.0, 0.0, 0.0, 0.0 };

	/**
	 * IUPAC Names
	 */
	private static final String[] names = { "Hydrogen", "Helium", "Lithium", "Beryllium", "Boron", "Carbon",
			"Nitrogen", "Oxygen", "Fluorine", "Neon", "Sodium", "Magnesium", "Aluminium", "Silicon", "Phosphorus",
			"Sulfur", "Chlorine", "Argon", "Potassium", "Calcium", "Scandium", "Titanium", "Vanadium", "Chromium",
			"Manganese", "Iron", "Cobalt", "Nickel", "Copper", "Zinc", "Gallium", "Germanium", "Arsenic", "Selenium",
			"Bromine", "Krypton", "Rubidium", "Strontium", "Yttrium", "Zirconium", "Niobium", "Molybdenum",
			"Technetium", "Ruthenium", "Rhodium", "Palladium", "Silver", "Cadmium", "Indium", "Tin", "Antimony",
			"Tellurium", "Iodine", "Xenon", "Cesium", "Barium", "Lanthanum", "Cerium", "Praseodymium", "Neodymium",
			"Promethium", "Samarium", "Europium", "Gadolinium", "Terbium", "Dysprosium", "Holmium", "Erbium",
			"Thulium", "Ytterbum", "Lutetium", "Hafnium", "Tantalum", "Tungsten", "Rhenium", "Osmium", "Iridium",
			"Platinum", "Gold", "Mercury", "Thallium", "Lead", "Bismuth", "Polonium", "Astatine", "Radon", "Francium",
			"Radium", "Actinium", "Thorium", "Protactinium", "Uranium", "Neptunium", "Plutonium", "Americium",
			"Curium", "Berkelium", "Californium", "Einsteinium", "Fermium", "Mendelevium", "Nobelium", "Lawrencium" };

	/**
	 * (Possibly controversial) types
	 */
	private static final int[] types = { NONMETAL, NOBLEGAS, METAL, METAL, NONMETAL, NONMETAL, NONMETAL, NONMETAL,
			NONMETAL, NOBLEGAS, METAL, METAL, METAL, METALLOID, NONMETAL, NONMETAL, NONMETAL, NOBLEGAS, METAL, METAL,
			METAL, METAL, METAL, METAL, METAL, METAL, METAL, METAL, METAL, METAL, METAL, METALLOID, METALLOID,
			METALLOID, NONMETAL, NOBLEGAS, METAL, METAL, METAL, METAL, METAL, METAL, METAL, METAL, METAL, METAL, METAL,
			METAL, METAL, METAL, METALLOID, METALLOID, NONMETAL, NOBLEGAS, METAL, METAL, METAL, LANTHANIDE, LANTHANIDE,
			LANTHANIDE, LANTHANIDE, LANTHANIDE, LANTHANIDE, LANTHANIDE, LANTHANIDE, LANTHANIDE, LANTHANIDE, LANTHANIDE,
			LANTHANIDE, LANTHANIDE, LANTHANIDE, METAL, METAL, METAL, METAL, METAL, METAL, METAL, METAL, METAL, METAL,
			METAL, METAL, METALLOID, METALLOID, NOBLEGAS, METAL, METAL, METAL, ACTINIDE, ACTINIDE, ACTINIDE, ACTINIDE,
			ACTINIDE, ACTINIDE, ACTINIDE, ACTINIDE, ACTINIDE, ACTINIDE, ACTINIDE, ACTINIDE, ACTINIDE, ACTINIDE };

	private static String[] SORTED_SYMBOLS;

	/**
	 * Returns the symbols sorted alphabetically.
	 *
	 * @return String[]
	 */
	@SuppressWarnings("cast")
	public static String[] getSortedSymbols() {
		if (SORTED_SYMBOLS == null) {
			// NOTE: If tmp = Arrays.asList(symbols) used, symbols
			// string [] can get sorted. Defect found 19 March 2009.
			final List<String> tmp = new ArrayList<String>(symbols.length);
			tmp.addAll(Arrays.asList(symbols));
			Collections.sort(tmp);
			SORTED_SYMBOLS = (String[]) tmp.toArray(new String[tmp.size()]);
		}
		return SORTED_SYMBOLS;
	}

	/**
	 * Return elements sorted in atomic number range.
	 *
	 * @param fromSymbol
	 *            (atomic symbol)
	 * @param toSymbol
	 *            (atomic symbol)
	 * @return String[]
	 */
	@SuppressWarnings("cast")
	public static String[] getSortedEdgeSymbols(final String fromSymbol, final String toSymbol) {

		final List<String> tmp = new ArrayList<String>(symbols.length);
		tmp.addAll(Arrays.asList(symbols));
		Collections.sort(tmp);
		final Iterator<String> it = tmp.iterator();

		final int from = Element.getElement(fromSymbol).getAtomicNumber();
		final int to = Element.getElement(toSymbol).getAtomicNumber();

		ELEMENT_LOOP: while (it.hasNext()) {
			final String symbol = it.next();
			final Element ele = Element.getElement(symbol);
			if (ele.getAtomicNumber() < from) {
				it.remove();
				continue ELEMENT_LOOP;
			}
			if (ele.getAtomicNumber() > to) {
				it.remove();
				continue ELEMENT_LOOP;
			}
			for (int i = 0; i < edgeNames.length; i++) {
				if (ele.edgeExists(edgeNames[i])) {
					continue ELEMENT_LOOP;
				}
			}
			it.remove();
		}
		return (String[]) tmp.toArray(new String[tmp.size()]);

	}

	@Override
	public String toString() {
		return getSymbol();
	}

	/**
	 * @param symbol
	 * @return Element
	 */
	public static Element getElement(final String symbol) {
		initElements();
		return ALL_ELEMENTS.get(symbol);
	}

	/**
	 * @param symbol
	 * @return boolean
	 */
	public static boolean isElement(final String symbol) {
		initElements();
		return ALL_ELEMENTS.containsKey(symbol);
	}

}