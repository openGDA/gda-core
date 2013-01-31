/**
 */
package org.opengda.detector.electronanalyser.server.model.regiondefinition.tests;

import junit.framework.TestCase;

import junit.textui.TestRunner;

import org.opengda.detector.electronanalyser.server.model.regiondefinition.RegiondefinitionFactory;
import org.opengda.detector.electronanalyser.server.model.regiondefinition.Step;

/**
 * <!-- begin-user-doc -->
 * A test case for the model object '<em><b>Step</b></em>'.
 * <!-- end-user-doc -->
 * @generated
 */
public class StepTest extends TestCase {

	/**
	 * The fixture for this Step test case.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected Step fixture = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public static void main(String[] args) {
		TestRunner.run(StepTest.class);
	}

	/**
	 * Constructs a new Step test case with the given name.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public StepTest(String name) {
		super(name);
	}

	/**
	 * Sets the fixture for this Step test case.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected void setFixture(Step fixture) {
		this.fixture = fixture;
	}

	/**
	 * Returns the fixture for this Step test case.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected Step getFixture() {
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
		setFixture(RegiondefinitionFactory.eINSTANCE.createStep());
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

} //StepTest
