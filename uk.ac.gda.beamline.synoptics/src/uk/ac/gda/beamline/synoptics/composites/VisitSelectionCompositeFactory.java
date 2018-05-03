/*-
 * Copyright © 2011 Diamond Light Source Ltd.
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

package uk.ac.gda.beamline.synoptics.composites;

import org.eclipse.swt.widgets.Composite;

import gda.rcp.views.CompositeFactory;

public class VisitSelectionCompositeFactory implements CompositeFactory {

	private String label = "Visit";
	private String entryName = "visit";

	public void setEntry(String metadata) {
		entryName = metadata;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	@Override
	public Composite createComposite(Composite parent, int style) {
		VisitSelectionComposite visitSelectionComposite = new VisitSelectionComposite(parent, style, label, entryName);
		return visitSelectionComposite;
	}

}
