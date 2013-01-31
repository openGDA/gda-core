/**
 */
package org.opengda.detector.electronanalyser.model.regiondefinition.tests;

import junit.framework.TestCase;

import junit.textui.TestRunner;

import org.opengda.detector.electronanalyser.model.regiondefinition.api.Energy;
import org.opengda.detector.electronanalyser.model.regiondefinition.api.RegiondefinitionFactory;

/**
 * <!-- begin-user-doc -->
 * A test case for the model object '<em><b>Energy</b></em>'.
 * <!-- end-user-doc -->
 * @generated
 */
public class EnergyTest extends TestCase {

	/**
	 * The fixture for this Energy test case.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected Energy fixture = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public static void main(String[] args) {
		TestRunner.run(EnergyTest.class);
	}

	/**
	 * Constructs a new Energy test case with the given name.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EnergyTest(String name) {
		super(name);
	}

	/**
	 * Sets the fixture for this Energy test case.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected void setFixture(Energy fixture) {
		this.fixture = fixture;
	}

	/**
	 * Returns the fixture for this Energy test case.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected Energy getFixture() {
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
		setFixture(RegiondefinitionFactory.eINSTANCE.createEnergy());
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

} //EnergyTest
