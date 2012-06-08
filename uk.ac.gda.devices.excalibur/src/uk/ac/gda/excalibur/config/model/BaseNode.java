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
package uk.ac.gda.excalibur.config.model;

import org.eclipse.emf.ecore.EObject;

/**
 * @author rsr31645
 * @model
 */
public interface BaseNode extends EObject {
	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	String copyright = "\nCopyright © 2011 Diamond Light Source Ltd.\n\nThis file is part of GDA.\n\nGDA is free software: you can redistribute it and/or modify it under the\nterms of the GNU General Public License version 3 as published by the Free\nSoftware Foundation.\n\nGDA is distributed in the hope that it will be useful, but WITHOUT ANY\nWARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS\nFOR A PARTICULAR PURPOSE. See the GNU General Public License for more\ndetails.\n\nYou should have received a copy of the GNU General Public License along\nwith GDA. If not, see <http://www.gnu.org/licenses/>.";

	/**
	 * @model type="GapModel" containment="true"
	 */
	GapModel getGap();

	/**
	 * Sets the value of the '{@link uk.ac.gda.excalibur.config.model.BaseNode#getGap <em>Gap</em>}' containment reference.
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @param value the new value of the '<em>Gap</em>' containment reference.
	 * @see #getGap()
	 * @generated
	 */
	void setGap(GapModel value);

	/**
	 * @model type="MasterModel" containment="true"
	 */
	MasterModel getMst();

	/**
	 * Sets the value of the '{@link uk.ac.gda.excalibur.config.model.BaseNode#getMst <em>Mst</em>}' containment reference.
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @param value the new value of the '<em>Mst</em>' containment reference.
	 * @see #getMst()
	 * @generated
	 */
	void setMst(MasterModel value);

	/**
	 * @model type="FixModel" containment="true"
	 */
	FixModel getFix();

	/**
	 * @param value
	 */
	void setFix(FixModel value);
}
