/**
 */
package org.opengda.detector.electronanalyser.model.regiondefinition.api;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.eclipse.emf.common.util.Enumerator;

/**
 * <!-- begin-user-doc -->
 * A representation of the literals of the enumeration '<em><b>RUN MODES</b></em>',
 * and utility methods for working with them.
 * <!-- end-user-doc -->
 * @see org.opengda.detector.electronanalyser.model.regiondefinition.api.RegiondefinitionPackage#getRUN_MODES()
 * @model
 * @generated
 */
public enum RUN_MODES implements Enumerator {
	/**
	 * The '<em><b>NORMAL</b></em>' literal object.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #NORMAL
	 * @generated
	 * @ordered
	 */
	NORMAL_LITERAL(0, "NORMAL", "NORMAL"), //$NON-NLS-1$ //$NON-NLS-2$

	/**
	 * The '<em><b>ADD DIMENSION</b></em>' literal object.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #ADD_DIMENSION
	 * @generated
	 * @ordered
	 */
	ADD_DIMENSION_LITERAL(1, "ADD_DIMENSION", "ADD_DIMENSION"); //$NON-NLS-1$ //$NON-NLS-2$

	/**
	 * The '<em><b>NORMAL</b></em>' literal value.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of '<em><b>NORMAL</b></em>' literal object isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @see #NORMAL_LITERAL
	 * @model
	 * @generated
	 * @ordered
	 */
	public static final int NORMAL = 0;

	/**
	 * The '<em><b>ADD DIMENSION</b></em>' literal value.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of '<em><b>ADD DIMENSION</b></em>' literal object isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @see #ADD_DIMENSION_LITERAL
	 * @model
	 * @generated
	 * @ordered
	 */
	public static final int ADD_DIMENSION = 1;

	/**
	 * An array of all the '<em><b>RUN MODES</b></em>' enumerators.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private static final RUN_MODES[] VALUES_ARRAY =
		new RUN_MODES[] {
			NORMAL_LITERAL,
			ADD_DIMENSION_LITERAL,
		};

	/**
	 * A public read-only list of all the '<em><b>RUN MODES</b></em>' enumerators.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public static final List<RUN_MODES> VALUES = Collections.unmodifiableList(Arrays.asList(VALUES_ARRAY));

	/**
	 * Returns the '<em><b>RUN MODES</b></em>' literal with the specified literal value.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public static RUN_MODES get(String literal) {
		for (int i = 0; i < VALUES_ARRAY.length; ++i) {
			RUN_MODES result = VALUES_ARRAY[i];
			if (result.toString().equals(literal)) {
				return result;
			}
		}
		return null;
	}

	/**
	 * Returns the '<em><b>RUN MODES</b></em>' literal with the specified name.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public static RUN_MODES getByName(String name) {
		for (int i = 0; i < VALUES_ARRAY.length; ++i) {
			RUN_MODES result = VALUES_ARRAY[i];
			if (result.getName().equals(name)) {
				return result;
			}
		}
		return null;
	}

	/**
	 * Returns the '<em><b>RUN MODES</b></em>' literal with the specified integer value.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public static RUN_MODES get(int value) {
		switch (value) {
			case NORMAL: return NORMAL_LITERAL;
			case ADD_DIMENSION: return ADD_DIMENSION_LITERAL;
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
	private RUN_MODES(int value, String name, String literal) {
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
	
} //RUN_MODES
