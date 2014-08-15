/**
 */
package org.opengda.lde.model.ldeexperiment.tests;

import junit.framework.TestCase;
import junit.textui.TestRunner;
import org.opengda.lde.model.ldeexperiment.LDEExperimentsFactory;
import org.opengda.lde.model.ldeexperiment.Sample;

/**
 * <!-- begin-user-doc -->
 * A test case for the model object '<em><b>Sample</b></em>'.
 * <!-- end-user-doc -->
 * @generated
 */
public class SampleTest extends TestCase {

	/**
	 * The fixture for this Sample test case.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected Sample fixture = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public static void main(String[] args) {
		TestRunner.run(SampleTest.class);
	}

	/**
	 * Constructs a new Sample test case with the given name.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public SampleTest(String name) {
		super(name);
	}

	/**
	 * Sets the fixture for this Sample test case.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected void setFixture(Sample fixture) {
		this.fixture = fixture;
	}

	/**
	 * Returns the fixture for this Sample test case.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected Sample getFixture() {
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
		setFixture(LDEExperimentsFactory.eINSTANCE.createSample());
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

} //SampleTest
