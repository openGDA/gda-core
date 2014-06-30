/**
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
		suite.addTestSuite(SampleListTest.class);
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
