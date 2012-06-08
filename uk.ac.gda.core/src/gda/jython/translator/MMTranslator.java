/*-
 * Copyright © 2009 Diamond Light Source Ltd., Science and Technology
 * Facilities Council Daresbury Laboratory
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

package gda.jython.translator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An extension to the GeneralTranslator class for the Diamond I16 Materials and Magnetism beamline.
 */
public class MMTranslator extends GeneralTranslator implements Translator {
	
	private static final Logger logger = LoggerFactory.getLogger(MMTranslator.class);

	@Override
	public String translateGroup(String thisGroup) {
		String originalGroup = thisGroup;
		String prefix = "";
		try {

			// ignore comments
			if (thisGroup.startsWith("#")) {
				return thisGroup;
			}

			// remove part of line after comments
			thisGroup = removeComments(thisGroup);

			// tidy up []'s
			thisGroup = GeneralTranslator.tidyBrackets(thisGroup);

			// remove initial tabs into separate prefix string
			while (thisGroup.startsWith(" ") || thisGroup.startsWith("\t")) {
				prefix += thisGroup.substring(0, 1);
				thisGroup = thisGroup.substring(1);
			}

			// split rest of line by space
			String[] args = thisGroup.split(" ");

			// assume first element in 'args' is the method, second is the
			// name of
			// the
			// object and rest are the arguments of the method

			// add a relection to the relection file
			if (args[0].compareTo("refladd") == 0) {
				if (args.length == 3) {
					thisGroup = "rr.Addreflections('" + args[1] + "'," + args[2] + ")";
				} else if (args.length == 2) {
					thisGroup = "rr.Addreflections('" + args[1] + "')";
				} else if (args.length == 1) {
					thisGroup = "rr.Addreflections()";
				} else {
					thisGroup = "print \"Wrong number of arguments for the refladd command\"";
				}

			}
			// lattice accessors
			else if (args[0].compareTo("lattice") == 0) {
				if (args.length == 7) {
					thisGroup = "cr.setLattice([" + args[1] + " " + args[2] + " " + args[3] + " " + args[4] + " "
							+ args[5] + " " + args[6] + "]);";
					thisGroup += "cr.setBMatrix()";
				} else {
					thisGroup = "print cr.getLattice()";
				}
			}
			// lattice file acessors
			else if (args[0].compareTo("reflfile") == 0) {
				if (args.length > 1) {
					thisGroup = "cr.setCrystal('" + args[1] + "');";
					thisGroup += "rr.setReflectionsFileName('" + args[1] + "')";
				} else {
					thisGroup = "print cr.getCrystal()";
				}
			}
			// define which reflections to use from the relections file
			else if (args[0].compareTo("reflset") == 0) {
				thisGroup = "ub.setOrient('" + args[1] + "','" + args[2] + "');";
				thisGroup += "ub.setUB();";
			}

			else if (args[0].compareTo("reflist") == 0 || args[0].compareTo("refllist") == 0
					|| args[0].compareTo("reflList") == 0
					|| (args[0].compareTo("refl") == 0 && args[1].compareTo("list") == 0)) {
				thisGroup = "rr.getAllSavedReflections()";
			}

			// set the mode which the diffractometer uses
			else if (args[0].compareTo("mode") == 0) {
				if (args.length < 2 || args.length > 3) {
					thisGroup = "print \"Wrong number of arguments.\\nType: mode euler, mode e2k or mode sp\"";
				} else if (args.length == 2) {
					if (args[1].compareTo("euler") == 0) {
						thisGroup = "EDi.getMode();EDi.printMode()";
					} else if (args[1].compareTo("e2k") == 0) {
						thisGroup = "EKCM.getEuleriantoKmode();EKCM.printEuleriantoKmodes();";
					} else if (args[1].compareTo("sp") == 0) {
						thisGroup = "print \"Scattering plane = \" + BLobjects.getScatteringPlane();BLobjects.getTth();";
					}
				} else if (args.length == 3) {
					if (args[1].compareTo("euler") == 0) {
						thisGroup = "EDi.setMode(" + args[2] + ")";
					} else if (args[1].compareTo("e2k") == 0) {
						thisGroup = "EKCM.setEuleriantoKmode(" + args[2] + ")";
					} else if (args[1].compareTo("sp") == 0) {
						thisGroup = "BLobjects.setScatteringPlane(\"" + args[2] + "\"); tth = BLobjects.getTth()";
					}
				}
			}
			// set which sector the diffractometer uses
			else if (args[0].compareTo("sector") == 0) {
				if (args.length > 1) {
					thisGroup = "EDi.setSector(" + args[1] + ")";
				} else {
					thisGroup = "EDi.getSector()";
				}
			}
			// set the cuts which the diffractometer uses
			else if (args[0].compareTo("cut") == 0) {
				if (args.length > 1) {
					thisGroup = "hkl.ub.setCuts(" + args[1] + "," + args[2] + "," + args[3] + "," + args[4] + ","
							+ args[5] + ")";
				} else {
					thisGroup = "hkl.ub.getCuts()";
				}
			}
			// set the azimuthal reference
			else if (args[0].compareTo("aziref") == 0) {
				if (args.length == 2) {
					thisGroup = "az.setAzimuthalReference(" + args[1] + ")";
				} else {
					thisGroup = "az.getAzimuthalReference()";
				}
			}
		} catch (Exception e) {
			// if an error, return the original string
			// and let jython interpreter handle any syntax errors
			logger.debug("Error in MMTranslator: " + originalGroup);
			return originalGroup;
		}
		// when finished make sure to pass the group back to the super class
		// as we are extending, not replacing, the syntax
		thisGroup = super.translateGroup(thisGroup);
		return prefix + thisGroup;
	}

	@Override
	public String getHelpMessage() {
		String helpString = super.getHelpMessage();

		helpString += "reflfile filename\\t-\\tsets the file in which reflections are stored.  This msut be set before using the lattice or refl commands\\n";
		helpString += "lattice [ h k l a b g ]\\t-\\tif values are given then sets the lattice matrix to use, else returns the current matrix\\n";
		helpString += "reflist\\t-\\t lists the reflections stored in the file set by the latticefile command\\n";
		helpString += "refladd [ vector ]\\t-\\tadds the current position of the diffractometer as the current [or supplied] reciprocal-space vector to the file of reflections\\n";
		helpString += "reflset index_1 index_2\\t-\\tsets the diffractometer to use the two reflections to in the file of reflections\\n";
		helpString += "mode euler [ value ]\\t-\\tReturns or sets to Eulerian diffractometer mode\\n";
		helpString += "mode e2k [ value ]\\t-\\tReturns or sets to Eulerian to Kappa conversion mode\\n";
		helpString += "mode sp [ plane ]\\t-\\tReturns or sets the scattering plane (h or v)\\n";
		helpString += "sector [ value ]\\t-\\tif value is defined then sets the sector the diffractometer is working in, else returns the current sector\\n";
		helpString += "aziref [ value ]\\t-\\tif value is defined then sets the azimuthal reference, else returns the current one being used\\n";
		helpString += "cut [ ttheta thate chi phi psi ]\\t-\\tif values are supplied then sets the cut off values for the axes, else returns the current cut off values\\n";
		return helpString;
	}
}
