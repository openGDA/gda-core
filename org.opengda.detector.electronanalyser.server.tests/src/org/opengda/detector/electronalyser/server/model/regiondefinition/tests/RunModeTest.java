/**
 */
package org.opengda.detector.electronalyser.server.model.regiondefinition.tests;

import junit.framework.TestCase;

import junit.textui.TestRunner;

import org.opengda.detector.electronalyser.server.model.regiondefinition.RegiondefinitionFactory;
import org.opengda.detector.electronalyser.server.model.regiondefinition.RunMode;

/**
 * <!-- begin-user-doc -->
 * A test case for the model object '<em><b>Run Mode</b></em>'.
 * <!-- end-user-doc -->
 * @generated
 */
public class RunModeTest extends TestCase {

	/**
	 * The fixture for this Run Mode test case.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected RunMode fixture = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public static void main(String[] args) {
		TestRunner.run(RunModeTest.class);
	}

	/**
	 * Constructs a new Run Mode test case with the given name.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public RunModeTest(String name) {
		super(name);
	}

	/**
	 * Sets the fixture for this Run Mode test case.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected void setFixture(RunMode fixture) {
		this.fixture = fixture;
	}

	/**
	 * Returns the fixture for this Run Mode test case.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected RunMode getFixture() {
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
		setFixture(RegiondefinitionFactory.eINSTANCE.createRunMode());
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

} //RunModeTest
