/**
 */
package org.opengda.detector.electronanalyser.model.regiondefinition.api;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.eclipse.emf.common.util.Enumerator;

/**
 * <!-- begin-user-doc -->
 * A representation of the literals of the enumeration '<em><b>LENS MODE</b></em>',
 * and utility methods for working with them.
 * <!-- end-user-doc -->
 * @see org.opengda.detector.electronanalyser.model.regiondefinition.api.RegiondefinitionPackage#getLENS_MODE()
 * @model
 * @generated
 */
public enum LENS_MODE implements Enumerator {
	/**
	 * The '<em><b>TRANSMISSION</b></em>' literal object.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #TRANSMISSION_VALUE
	 * @generated
	 * @ordered
	 */
	TRANSMISSION(0, "TRANSMISSION", "TRANSMISSION"),

	/**
	 * The '<em><b>Angular45</b></em>' literal object.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #ANGULAR45_VALUE
	 * @generated
	 * @ordered
	 */
	ANGULAR45(1, "Angular45", "Angular45"),

	/**
	 * The '<em><b>Angular60</b></em>' literal object.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #ANGULAR60_VALUE
	 * @generated
	 * @ordered
	 */
	ANGULAR60(2, "Angular60", "Angular60");

	/**
	 * The '<em><b>TRANSMISSION</b></em>' literal value.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of '<em><b>TRANSMISSION</b></em>' literal object isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @see #TRANSMISSION
	 * @model
	 * @generated
	 * @ordered
	 */
	public static final int TRANSMISSION_VALUE = 0;

	/**
	 * The '<em><b>Angular45</b></em>' literal value.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of '<em><b>Angular45</b></em>' literal object isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @see #ANGULAR45
	 * @model name="Angular45"
	 * @generated
	 * @ordered
	 */
	public static final int ANGULAR45_VALUE = 1;

	/**
	 * The '<em><b>Angular60</b></em>' literal value.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of '<em><b>Angular60</b></em>' literal object isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @see #ANGULAR60
	 * @model name="Angular60"
	 * @generated
	 * @ordered
	 */
	public static final int ANGULAR60_VALUE = 2;

	/**
	 * An array of all the '<em><b>LENS MODE</b></em>' enumerators.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private static final LENS_MODE[] VALUES_ARRAY =
		new LENS_MODE[] {
			TRANSMISSION,
			ANGULAR45,
			ANGULAR60,
		};

	/**
	 * A public read-only list of all the '<em><b>LENS MODE</b></em>' enumerators.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public static final List<LENS_MODE> VALUES = Collections.unmodifiableList(Arrays.asList(VALUES_ARRAY));

	/**
	 * Returns the '<em><b>LENS MODE</b></em>' literal with the specified literal value.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public static LENS_MODE get(String literal) {
		for (int i = 0; i < VALUES_ARRAY.length; ++i) {
			LENS_MODE result = VALUES_ARRAY[i];
			if (result.toString().equals(literal)) {
				return result;
			}
		}
		return null;
	}

	/**
	 * Returns the '<em><b>LENS MODE</b></em>' literal with the specified name.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public static LENS_MODE getByName(String name) {
		for (int i = 0; i < VALUES_ARRAY.length; ++i) {
			LENS_MODE result = VALUES_ARRAY[i];
			if (result.getName().equals(name)) {
				return result;
			}
		}
		return null;
	}

	/**
	 * Returns the '<em><b>LENS MODE</b></em>' literal with the specified integer value.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public static LENS_MODE get(int value) {
		switch (value) {
			case TRANSMISSION_VALUE: return TRANSMISSION;
			case ANGULAR45_VALUE: return ANGULAR45;
			case ANGULAR60_VALUE: return ANGULAR60;
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
	private LENS_MODE(int value, String name, String literal) {
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
	
} //LENS_MODE
