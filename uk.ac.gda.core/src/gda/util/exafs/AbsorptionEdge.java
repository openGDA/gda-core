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

import java.io.Serializable;
import java.util.StringTokenizer;

/**
 * Represents an absorption edge.
 */
public class AbsorptionEdge implements Serializable{
	private String elementSymbol;

	private String edgeType;

	private double energy; //in eV

	/**
	 * Constructor.
	 * <p>
	 * No checking of values here but elementSymbol should be a standard symbol, edgeType should be one of "K", "LI",
	 * "LII" or "LIII" and energy should be in keV.
	 * 
	 * @param elementSymbol
	 *            the element symbol - should be a standard symbol
	 * @param edgeType
	 *            the edge type - should be one of "K", "LI", "LII" or "LIII"
	 * @param energy
	 *            the energy value - should be in eV
	 */
	public AbsorptionEdge(String elementSymbol, String edgeType, double energy) {
		this.elementSymbol = elementSymbol;
		this.edgeType = edgeType;
		this.energy = energy;
	}

	/**
	 * Constructs and AbsorptionEdge from a single string as produced by the toString() method.
	 * 
	 * @param string
	 */

	public AbsorptionEdge(String string) {
		StringTokenizer strtok = new StringTokenizer(string);
		elementSymbol = strtok.nextToken();
		edgeType = strtok.nextToken();
		energy = Double.valueOf(strtok.nextToken());
	}

	/**
	 * Returns the element symbol
	 * 
	 * @return the edge type
	 */
	public String getElementSymbol() {
		return (elementSymbol);
	}

	/**
	 * Returns the type (K, LI etc.)
	 * 
	 * @return the edge type
	 */
	public String getEdgeType() {
		return (edgeType);
	}

	/**
	 * Returns the edge energy
	 * 
	 * @return the edge energy in eV
	 */
	public double getEnergy() {
		return (energy);
	}

	/**
	 * Returns a string representation of the edge.
	 * 
	 * @return string representing the edge
	 */
	@Override
	public String toString() {
		return "" + getElementSymbol() + " " + getEdgeType() + " " + getEnergy();
	}

}
