/**
 */
package org.opengda.detector.electronanalyser.model.regiondefinition.api;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.eclipse.emf.common.util.Enumerator;

/**
 * <!-- begin-user-doc -->
 * A representation of the literals of the enumeration '<em><b>ELEMENT SET</b></em>',
 * and utility methods for working with them.
 * <!-- end-user-doc -->
 * @see org.opengda.detector.electronanalyser.model.regiondefinition.api.RegiondefinitionPackage#getELEMENT_SET()
 * @model
 * @generated
 */
public enum ELEMENT_SET implements Enumerator {
	/**
	 * The '<em><b>LOW</b></em>' literal object.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #LOW_VALUE
	 * @generated
	 * @ordered
	 */
	LOW(0, "LOW", "Low"), //$NON-NLS-1$ //$NON-NLS-2$

	/**
	 * The '<em><b>HIGH</b></em>' literal object.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #HIGH_VALUE
	 * @generated
	 * @ordered
	 */
	HIGH(1, "HIGH", "High"); //$NON-NLS-1$ //$NON-NLS-2$

	/**
	 * The '<em><b>LOW</b></em>' literal value.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of '<em><b>LOW</b></em>' literal object isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @see #LOW
	 * @model literal="Low"
	 * @generated
	 * @ordered
	 */
	public static final int LOW_VALUE = 0;

	/**
	 * The '<em><b>HIGH</b></em>' literal value.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of '<em><b>HIGH</b></em>' literal object isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @see #HIGH
	 * @model literal="High"
	 * @generated
	 * @ordered
	 */
	public static final int HIGH_VALUE = 1;

	/**
	 * An array of all the '<em><b>ELEMENT SET</b></em>' enumerators.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private static final ELEMENT_SET[] VALUES_ARRAY =
		new ELEMENT_SET[] {
			LOW,
			HIGH,
		};

	/**
	 * A public read-only list of all the '<em><b>ELEMENT SET</b></em>' enumerators.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public static final List<ELEMENT_SET> VALUES = Collections.unmodifiableList(Arrays.asList(VALUES_ARRAY));

	/**
	 * Returns the '<em><b>ELEMENT SET</b></em>' literal with the specified literal value.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public static ELEMENT_SET get(String literal) {
		for (int i = 0; i < VALUES_ARRAY.length; ++i) {
			ELEMENT_SET result = VALUES_ARRAY[i];
			if (result.toString().equals(literal)) {
				return result;
			}
		}
		return null;
	}

	/**
	 * Returns the '<em><b>ELEMENT SET</b></em>' literal with the specified name.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public static ELEMENT_SET getByName(String name) {
		for (int i = 0; i < VALUES_ARRAY.length; ++i) {
			ELEMENT_SET result = VALUES_ARRAY[i];
			if (result.getName().equals(name)) {
				return result;
			}
		}
		return null;
	}

	/**
	 * Returns the '<em><b>ELEMENT SET</b></em>' literal with the specified integer value.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public static ELEMENT_SET get(int value) {
		switch (value) {
			case LOW_VALUE: return LOW;
			case HIGH_VALUE: return HIGH;
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
	private ELEMENT_SET(int value, String name, String literal) {
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

} //ELEMENT_SET
