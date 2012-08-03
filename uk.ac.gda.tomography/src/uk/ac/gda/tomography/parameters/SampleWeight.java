/**
 * <copyright>
 * </copyright>
 *
 * $Id$
 */
package uk.ac.gda.tomography.parameters;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.eclipse.emf.common.util.Enumerator;

/**
 * <!-- begin-user-doc -->
 * A representation of the literals of the enumeration '<em><b>Sample Weight</b></em>',
 * and utility methods for working with them.
 * <!-- end-user-doc -->
 * @see uk.ac.gda.tomography.parameters.TomoParametersPackage#getSampleWeight()
 * @model
 * @generated
 */
public enum SampleWeight implements Enumerator {
	/**
	 * The '<em><b>LESS THAN 1</b></em>' literal object.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #LESS_THAN_1_VALUE
	 * @generated
	 * @ordered
	 */
	LESS_THAN_1(0, "LESS_THAN_1", "LESS_THAN_1"),

	/**
	 * The '<em><b>ONE TO TEN</b></em>' literal object.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #ONE_TO_TEN_VALUE
	 * @generated
	 * @ordered
	 */
	ONE_TO_TEN(1, "ONE_TO_TEN", "ONE_TO_TEN"),

	/**
	 * The '<em><b>TEN TO TWENTY</b></em>' literal object.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #TEN_TO_TWENTY_VALUE
	 * @generated
	 * @ordered
	 */
	TEN_TO_TWENTY(2, "TEN_TO_TWENTY", "TEN_TO_TWENTY"),

	/**
	 * The '<em><b>TWENTY TO FIFTY</b></em>' literal object.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #TWENTY_TO_FIFTY_VALUE
	 * @generated
	 * @ordered
	 */
	TWENTY_TO_FIFTY(3, "TWENTY_TO_FIFTY", "TWENTY_TO_FIFTY");

	/**
	 * The '<em><b>LESS THAN 1</b></em>' literal value.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of '<em><b>LESS THAN 1</b></em>' literal object isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @see #LESS_THAN_1
	 * @model
	 * @generated
	 * @ordered
	 */
	public static final int LESS_THAN_1_VALUE = 0;

	/**
	 * The '<em><b>ONE TO TEN</b></em>' literal value.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of '<em><b>ONE TO TEN</b></em>' literal object isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @see #ONE_TO_TEN
	 * @model
	 * @generated
	 * @ordered
	 */
	public static final int ONE_TO_TEN_VALUE = 1;

	/**
	 * The '<em><b>TEN TO TWENTY</b></em>' literal value.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of '<em><b>TEN TO TWENTY</b></em>' literal object isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @see #TEN_TO_TWENTY
	 * @model
	 * @generated
	 * @ordered
	 */
	public static final int TEN_TO_TWENTY_VALUE = 2;

	/**
	 * The '<em><b>TWENTY TO FIFTY</b></em>' literal value.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of '<em><b>TWENTY TO FIFTY</b></em>' literal object isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @see #TWENTY_TO_FIFTY
	 * @model
	 * @generated
	 * @ordered
	 */
	public static final int TWENTY_TO_FIFTY_VALUE = 3;

	/**
	 * An array of all the '<em><b>Sample Weight</b></em>' enumerators.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private static final SampleWeight[] VALUES_ARRAY =
		new SampleWeight[] {
			LESS_THAN_1,
			ONE_TO_TEN,
			TEN_TO_TWENTY,
			TWENTY_TO_FIFTY,
		};

	/**
	 * A public read-only list of all the '<em><b>Sample Weight</b></em>' enumerators.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public static final List<SampleWeight> VALUES = Collections.unmodifiableList(Arrays.asList(VALUES_ARRAY));

	/**
	 * Returns the '<em><b>Sample Weight</b></em>' literal with the specified literal value.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public static SampleWeight get(String literal) {
		for (int i = 0; i < VALUES_ARRAY.length; ++i) {
			SampleWeight result = VALUES_ARRAY[i];
			if (result.toString().equals(literal)) {
				return result;
			}
		}
		return null;
	}

	/**
	 * Returns the '<em><b>Sample Weight</b></em>' literal with the specified name.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public static SampleWeight getByName(String name) {
		for (int i = 0; i < VALUES_ARRAY.length; ++i) {
			SampleWeight result = VALUES_ARRAY[i];
			if (result.getName().equals(name)) {
				return result;
			}
		}
		return null;
	}

	/**
	 * Returns the '<em><b>Sample Weight</b></em>' literal with the specified integer value.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public static SampleWeight get(int value) {
		switch (value) {
			case LESS_THAN_1_VALUE: return LESS_THAN_1;
			case ONE_TO_TEN_VALUE: return ONE_TO_TEN;
			case TEN_TO_TWENTY_VALUE: return TEN_TO_TWENTY;
			case TWENTY_TO_FIFTY_VALUE: return TWENTY_TO_FIFTY;
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
	private SampleWeight(int value, String name, String literal) {
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
	
} //SampleWeight
