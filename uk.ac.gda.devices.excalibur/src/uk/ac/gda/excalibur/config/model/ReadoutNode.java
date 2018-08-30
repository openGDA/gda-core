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

/**
 * @author rsr31645
 * @model
 */
public interface ReadoutNode extends BaseNode {

	/**
	 * @model
	 * @return id of the node
	 */
	int getId();

	/**
	 * Sets the value of the '{@link uk.ac.gda.excalibur.config.model.ReadoutNode#getId <em>Id</em>}' attribute. <!--
	 * begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @param value
	 *            the new value of the '<em>Id</em>' attribute.
	 * @see #getId()
	 * @generated
	 */
	void setId(int value);

	/**
	 * @model type="ReadoutNodeFemModel" containment="true"
	 */
	ReadoutNodeFemModel getReadoutNodeFem();

	/**
	 * Sets the value of the '{@link uk.ac.gda.excalibur.config.model.ReadoutNode#getReadoutNodeFem <em>Readout Node Fem</em>}' containment reference.
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @param value the new value of the '<em>Readout Node Fem</em>' containment reference.
	 * @see #getReadoutNodeFem()
	 * @generated
	 */
	void setReadoutNodeFem(ReadoutNodeFemModel value);

}
