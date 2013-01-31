/**
 */
package org.opengda.detector.electronanalyser.model.regiondefinition.tests;

import junit.framework.TestCase;

import junit.textui.TestRunner;

import org.opengda.detector.electronanalyser.model.regiondefinition.api.Detector;
import org.opengda.detector.electronanalyser.model.regiondefinition.api.RegiondefinitionFactory;

/**
 * <!-- begin-user-doc -->
 * A test case for the model object '<em><b>Detector</b></em>'.
 * <!-- end-user-doc -->
 * @generated
 */
public class DetectorTest extends TestCase {

	/**
	 * The fixture for this Detector test case.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected Detector fixture = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public static void main(String[] args) {
		TestRunner.run(DetectorTest.class);
	}

	/**
	 * Constructs a new Detector test case with the given name.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public DetectorTest(String name) {
		super(name);
	}

	/**
	 * Sets the fixture for this Detector test case.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected void setFixture(Detector fixture) {
		this.fixture = fixture;
	}

	/**
	 * Returns the fixture for this Detector test case.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected Detector getFixture() {
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
		setFixture(RegiondefinitionFactory.eINSTANCE.createDetector());
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

} //DetectorTest
