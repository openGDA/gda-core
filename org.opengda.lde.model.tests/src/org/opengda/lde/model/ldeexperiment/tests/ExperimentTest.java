/**
 * Copyright ©2015 Diamond Light Source Ltd
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
 * 
 * Contributors:
 * 	Fajin Yuan
 */
package org.opengda.lde.model.ldeexperiment.tests;

import junit.framework.TestCase;

import junit.textui.TestRunner;

import org.opengda.lde.model.ldeexperiment.Experiment;
import org.opengda.lde.model.ldeexperiment.LDEExperimentsFactory;

/**
 * <!-- begin-user-doc -->
 * A test case for the model object '<em><b>Experiment</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following operations are tested:
 * <ul>
 *   <li>{@link org.opengda.lde.model.ldeexperiment.Experiment#getStageByID(java.lang.String) <em>Get Stage By ID</em>}</li>
 * </ul>
 * </p>
 * @generated
 */
public class ExperimentTest extends TestCase {

	/**
	 * The fixture for this Experiment test case.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected Experiment fixture = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public static void main(String[] args) {
		TestRunner.run(ExperimentTest.class);
	}

	/**
	 * Constructs a new Experiment test case with the given name.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public ExperimentTest(String name) {
		super(name);
	}

	/**
	 * Sets the fixture for this Experiment test case.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected void setFixture(Experiment fixture) {
		this.fixture = fixture;
	}

	/**
	 * Returns the fixture for this Experiment test case.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected Experiment getFixture() {
		return fixture;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see junit.framework.TestCase#setUp()
	 * @generated
	 */
	@Override
	protected void setUp() throws Exception {
		setFixture(LDEExperimentsFactory.eINSTANCE.createExperiment());
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see junit.framework.TestCase#tearDown()
	 * @generated
	 */
	@Override
	protected void tearDown() throws Exception {
		setFixture(null);
	}

	/**
	 * Tests the '{@link org.opengda.lde.model.ldeexperiment.Experiment#getStageByID(java.lang.String) <em>Get Stage By ID</em>}' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.opengda.lde.model.ldeexperiment.Experiment#getStageByID(java.lang.String)
	 * @generated
	 */
	public void testGetStageByID__String() {
		// TODO: implement this operation test method
		// Ensure that you remove @generated or mark it @generated NOT
		fail();
	}

} //ExperimentTest
