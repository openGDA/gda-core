/**
 * Diamond Light Source Ltd
 */
package uk.ac.gda.tomography.scan.util;

import java.util.Map;

import org.eclipse.emf.ecore.EPackage;

import org.eclipse.emf.ecore.resource.Resource;

import org.eclipse.emf.ecore.xmi.util.XMLProcessor;

import uk.ac.gda.tomography.scan.ScanPackage;

/**
 * This class contains helper methods to serialize and deserialize XML documents
 * <!-- begin-user-doc -->
 * <!-- end-user-doc -->
 * @generated
 */
public class ScanXMLProcessor extends XMLProcessor {

	/**
	 * Public constructor to instantiate the helper.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public ScanXMLProcessor() {
		super((EPackage.Registry.INSTANCE));
		ScanPackage.eINSTANCE.eClass();
	}

	/**
	 * Register for "*" and "xml" file extensions the ScanResourceFactoryImpl factory.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected Map<String, Resource.Factory> getRegistrations() {
		if (registrations == null) {
			super.getRegistrations();
			registrations.put(XML_EXTENSION, new ScanResourceFactoryImpl());
			registrations.put(STAR_EXTENSION, new ScanResourceFactoryImpl());
		}
		return registrations;
	}

} //ScanXMLProcessor
