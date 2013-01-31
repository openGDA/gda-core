/**
 */
package org.opengda.detector.electronanalyser.model.regiondefinition.api;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.eclipse.emf.common.util.Enumerator;

/**
 * <!-- begin-user-doc -->
 * A representation of the literals of the enumeration '<em><b>DETECTOR MODE</b></em>',
 * and utility methods for working with them.
 * <!-- end-user-doc -->
 * @see org.opengda.detector.electronanalyser.model.regiondefinition.api.RegiondefinitionPackage#getDETECTOR_MODE()
 * @model
 * @generated
 */
public enum DETECTOR_MODE implements Enumerator {
	/**
	 * The '<em><b>ADC</b></em>' literal object.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #ADC_VALUE
	 * @generated
	 * @ordered
	 */
	ADC(0, "ADC", "ADC"),

	/**
	 * The '<em><b>PULSE COUNTING</b></em>' literal object.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #PULSE_COUNTING_VALUE
	 * @generated
	 * @ordered
	 */
	PULSE_COUNTING(1, "PULSE_COUNTING", "PULSE_COUNTING");

	/**
	 * The '<em><b>ADC</b></em>' literal value.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of '<em><b>ADC</b></em>' literal object isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @see #ADC
	 * @model
	 * @generated
	 * @ordered
	 */
	public static final int ADC_VALUE = 0;

	/**
	 * The '<em><b>PULSE COUNTING</b></em>' literal value.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of '<em><b>PULSE COUNTING</b></em>' literal object isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @see #PULSE_COUNTING
	 * @model
	 * @generated
	 * @ordered
	 */
	public static final int PULSE_COUNTING_VALUE = 1;

	/**
	 * An array of all the '<em><b>DETECTOR MODE</b></em>' enumerators.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private static final DETECTOR_MODE[] VALUES_ARRAY =
		new DETECTOR_MODE[] {
			ADC,
			PULSE_COUNTING,
		};

	/**
	 * A public read-only list of all the '<em><b>DETECTOR MODE</b></em>' enumerators.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public static final List<DETECTOR_MODE> VALUES = Collections.unmodifiableList(Arrays.asList(VALUES_ARRAY));

	/**
	 * Returns the '<em><b>DETECTOR MODE</b></em>' literal with the specified literal value.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public static DETECTOR_MODE get(String literal) {
		for (int i = 0; i < VALUES_ARRAY.length; ++i) {
			DETECTOR_MODE result = VALUES_ARRAY[i];
			if (result.toString().equals(literal)) {
				return result;
			}
		}
		return null;
	}

	/**
	 * Returns the '<em><b>DETECTOR MODE</b></em>' literal with the specified name.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public static DETECTOR_MODE getByName(String name) {
		for (int i = 0; i < VALUES_ARRAY.length; ++i) {
			DETECTOR_MODE result = VALUES_ARRAY[i];
			if (result.getName().equals(name)) {
				return result;
			}
		}
		return null;
	}

	/**
	 * Returns the '<em><b>DETECTOR MODE</b></em>' literal with the specified integer value.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public static DETECTOR_MODE get(int value) {
		switch (value) {
			case ADC_VALUE: return ADC;
			case PULSE_COUNTING_VALUE: return PULSE_COUNTING;
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
	private DETECTOR_MODE(int value, String name, String literal) {
		this.value = value;
		this.name = name;
		this.literal = literal;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public int getValue() {
	  return value;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public String getName() {
	  return name;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
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
	
} //DETECTOR_MODE
