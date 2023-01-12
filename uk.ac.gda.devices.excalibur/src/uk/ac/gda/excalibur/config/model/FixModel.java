package uk.ac.gda.excalibur.config.model;

import org.eclipse.emf.ecore.EObject;

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


/**
 * @model
 */
public interface FixModel extends EObject {

	/**
	 * @model
	 */
	boolean isStatisticsEnabled() ;

	/**
	 * Sets the value of the '{@link uk.ac.gda.excalibur.config.model.FixModel#isStatisticsEnabled <em>Statistics Enabled</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Statistics Enabled</em>' attribute.
	 * @see #isStatisticsEnabled()
	 * @generated
	 */
	void setStatisticsEnabled(boolean value);

	/**
	 * @model
	 */
	boolean isScaleEdgePixelsEnabled();

	/**
	 * Sets the value of the '{@link uk.ac.gda.excalibur.config.model.FixModel#isScaleEdgePixelsEnabled <em>Scale Edge Pixels Enabled</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Scale Edge Pixels Enabled</em>' attribute.
	 * @see #isScaleEdgePixelsEnabled()
	 * @generated
	 */
	void setScaleEdgePixelsEnabled(boolean value);

}
