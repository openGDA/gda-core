/**
 * <copyright>
 * </copyright>
 *
 * $Id$
 */
package uk.ac.gda.edxd.calibration.edxdcalibration;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.eclipse.emf.common.util.Enumerator;

/**
 * <!-- begin-user-doc -->
 * A representation of the literals of the enumeration '<em><b>COLLIMATOR</b></em>',
 * and utility methods for working with them.
 * <!-- end-user-doc -->
 * @see uk.ac.gda.edxd.calibration.edxdcalibration.EdxdcalibrationPackage#getCOLLIMATOR()
 * @model
 * @generated
 */
public enum COLLIMATOR implements Enumerator {
	/**
	 * The '<em><b>COLLIMATOR1</b></em>' literal object.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #COLLIMATOR1_VALUE
	 * @generated
	 * @ordered
	 */
	COLLIMATOR1(0, "COLLIMATOR1", "HUTCH1"),

	/**
	 * The '<em><b>COLLIMATOR2</b></em>' literal object.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #COLLIMATOR2_VALUE
	 * @generated
	 * @ordered
	 */
	COLLIMATOR2(1, "COLLIMATOR2", "COLLIMATOR2"),

	/**
	 * The '<em><b>COLLIMATOR3</b></em>' literal object.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #COLLIMATOR3_VALUE
	 * @generated
	 * @ordered
	 */
	COLLIMATOR3(3, "COLLIMATOR3", "COLLIMATOR3"),

	/**
	 * The '<em><b>COLLIMATOR4</b></em>' literal object.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #COLLIMATOR4_VALUE
	 * @generated
	 * @ordered
	 */
	COLLIMATOR4(4, "COLLIMATOR4", "COLLIMATOR4");

	/**
	 * The '<em><b>COLLIMATOR1</b></em>' literal value.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of '<em><b>COLLIMATOR1</b></em>' literal object isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @see #COLLIMATOR1
	 * @model literal="HUTCH1"
	 * @generated
	 * @ordered
	 */
	public static final int COLLIMATOR1_VALUE = 0;

	/**
	 * The '<em><b>COLLIMATOR2</b></em>' literal value.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of '<em><b>COLLIMATOR2</b></em>' literal object isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @see #COLLIMATOR2
	 * @model
	 * @generated
	 * @ordered
	 */
	public static final int COLLIMATOR2_VALUE = 1;

	/**
	 * The '<em><b>COLLIMATOR3</b></em>' literal value.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of '<em><b>COLLIMATOR3</b></em>' literal object isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @see #COLLIMATOR3
	 * @model
	 * @generated
	 * @ordered
	 */
	public static final int COLLIMATOR3_VALUE = 3;

	/**
	 * The '<em><b>COLLIMATOR4</b></em>' literal value.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of '<em><b>COLLIMATOR4</b></em>' literal object isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @see #COLLIMATOR4
	 * @model
	 * @generated
	 * @ordered
	 */
	public static final int COLLIMATOR4_VALUE = 4;

	/**
	 * An array of all the '<em><b>COLLIMATOR</b></em>' enumerators.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private static final COLLIMATOR[] VALUES_ARRAY =
		new COLLIMATOR[] {
			COLLIMATOR1,
			COLLIMATOR2,
			COLLIMATOR3,
			COLLIMATOR4,
		};

	/**
	 * A public read-only list of all the '<em><b>COLLIMATOR</b></em>' enumerators.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public static final List<COLLIMATOR> VALUES = Collections.unmodifiableList(Arrays.asList(VALUES_ARRAY));

	/**
	 * Returns the '<em><b>COLLIMATOR</b></em>' literal with the specified literal value.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public static COLLIMATOR get(String literal) {
		for (int i = 0; i < VALUES_ARRAY.length; ++i) {
			COLLIMATOR result = VALUES_ARRAY[i];
			if (result.toString().equals(literal)) {
				return result;
			}
		}
		return null;
	}

	/**
	 * Returns the '<em><b>COLLIMATOR</b></em>' literal with the specified name.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public static COLLIMATOR getByName(String name) {
		for (int i = 0; i < VALUES_ARRAY.length; ++i) {
			COLLIMATOR result = VALUES_ARRAY[i];
			if (result.getName().equals(name)) {
				return result;
			}
		}
		return null;
	}

	/**
	 * Returns the '<em><b>COLLIMATOR</b></em>' literal with the specified integer value.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public static COLLIMATOR get(int value) {
		switch (value) {
			case COLLIMATOR1_VALUE: return COLLIMATOR1;
			case COLLIMATOR2_VALUE: return COLLIMATOR2;
			case COLLIMATOR3_VALUE: return COLLIMATOR3;
			case COLLIMATOR4_VALUE: return COLLIMATOR4;
		}
		return null;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private final int value;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private final String name;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private final String literal;

	/**
	 * Only this class can construct instances.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private COLLIMATOR(int value, String name, String literal) {
		this.value = value;
		this.name = name;
		this.literal = literal;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public int getValue() {
	  return value;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String getName() {
	  return name;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String getLiteral() {
	  return literal;
	}

	/**
	 * Returns the literal value of the enumerator, which is its string representation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String toString() {
		return literal;
	}
	
} //COLLIMATOR
