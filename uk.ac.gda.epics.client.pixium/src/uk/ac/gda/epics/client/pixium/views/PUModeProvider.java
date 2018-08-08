/*-
 * Copyright © 2014 Diamond Light Source Ltd.
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

package uk.ac.gda.epics.client.pixium.views;

import java.util.ArrayList;
import java.util.List;

public enum PUModeProvider {
	  INSTANCE;

	  private List<PUMode> modes;

	  private PUModeProvider() {
	    modes = new ArrayList<PUMode>();
	    // Image here some fancy database access to read the PU modes and to
	    // put them into the model
	    modes.add(new PUMode(1, "2880x2881", "80 ms", "4 fps"));
	    modes.add(new PUMode(3, "960x961", "1 ms", "18.5 fps"));
	    modes.add(new PUMode(4, "1440x1441", "15 ms", "12 fps"));
	    modes.add(new PUMode(7, "1024x1025", "6 ms", "16 fps"));
	    modes.add(new PUMode(13, "640x641", "1 ms", "30 fps"));
	    modes.add(new PUMode(14, "768x769", "1 ms", "24 fps"));
	    modes.add(new PUMode(15, "672x673", "1 ms", "30 fps"));
	  }

	  public List<PUMode> getPUModes() {
	    return modes;
	  }

}
