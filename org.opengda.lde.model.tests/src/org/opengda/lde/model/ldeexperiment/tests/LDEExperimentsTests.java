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

import junit.framework.Test;
import junit.framework.TestSuite;

import junit.textui.TestRunner;

/**
 * <!-- begin-user-doc -->
 * A test suite for the '<em><b>ldeexperiment</b></em>' package.
 * <!-- end-user-doc -->
 * @generated
 */
public class LDEExperimentsTests extends TestSuite {

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public static void main(String[] args) {
		TestRunner.run(suite());
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public static Test suite() {
		TestSuite suite = new LDEExperimentsTests("ldeexperiment Tests");
		suite.addTestSuite(ExperimentDefinitionTest.class);
		suite.addTestSuite(ExperimentTest.class);
		suite.addTestSuite(StageTest.class);
		suite.addTestSuite(CellTest.class);
		suite.addTestSuite(SampleTest.class);
		return suite;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public LDEExperimentsTests(String name) {
		super(name);
	}

} //LDEExperimentsTests
