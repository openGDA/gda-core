/**
 */
package org.opengda.lde.model.ldeexperiment.tests;

import junit.framework.TestCase;

import junit.textui.TestRunner;

import org.opengda.lde.model.ldeexperiment.ExperimentDefinition;
import org.opengda.lde.model.ldeexperiment.LDEExperimentsFactory;

/**
 * <!-- begin-user-doc -->
 * A test case for the model object '<em><b>Experiment Definition</b></em>'.
 * <!-- end-user-doc -->
 * @generated
 */
public class ExperimentDefinitionTest extends TestCase {

	/**
	 * The fixture for this Experiment Definition test case.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected ExperimentDefinition fixture = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public static void main(String[] args) {
		TestRunner.run(ExperimentDefinitionTest.class);
	}

	/**
	 * Constructs a new Experiment Definition test case with the given name.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public ExperimentDefinitionTest(String name) {
		super(name);
	}

	/**
	 * Sets the fixture for this Experiment Definition test case.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected void setFixture(ExperimentDefinition fixture) {
		this.fixture = fixture;
	}

	/**
	 * Returns the fixture for this Experiment Definition test case.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected ExperimentDefinition getFixture() {
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
		setFixture(LDEExperimentsFactory.eINSTANCE.createExperimentDefinition());
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

} //ExperimentDefinitionTest
