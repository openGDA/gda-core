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
public interface SummaryNode extends EObject {
	/**
	 * @model type="SummaryAdbaseModel" containment="true"
	 */
	SummaryAdbaseModel getSummaryFem();

	/**
	 * Sets the value of the '{@link uk.ac.gda.excalibur.config.model.SummaryNode#getSummaryFem <em>Summary Fem</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Summary Fem</em>' containment reference.
	 * @see #getSummaryFem()
	 * @generated
	 */
	void setSummaryFem(SummaryAdbaseModel value);
}
