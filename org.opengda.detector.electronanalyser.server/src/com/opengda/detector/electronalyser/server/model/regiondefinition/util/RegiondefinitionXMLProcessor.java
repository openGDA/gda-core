/**
 */
package com.opengda.detector.electronalyser.server.model.regiondefinition.util;

import com.opengda.detector.electronalyser.server.model.regiondefinition.RegiondefinitionPackage;

import java.util.Map;

import org.eclipse.emf.ecore.EPackage;

import org.eclipse.emf.ecore.resource.Resource;

import org.eclipse.emf.ecore.xmi.util.XMLProcessor;

/**
 * This class contains helper methods to serialize and deserialize XML documents
 * <!-- begin-user-doc -->
 * <!-- end-user-doc -->
 * @generated
 */
public class RegiondefinitionXMLProcessor extends XMLProcessor {

	/**
	 * Public constructor to instantiate the helper.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public RegiondefinitionXMLProcessor() {
		super((EPackage.Registry.INSTANCE));
		RegiondefinitionPackage.eINSTANCE.eClass();
	}
	
	/**
	 * Register for "*" and "xml" file extensions the RegiondefinitionResourceFactoryImpl factory.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected Map<String, Resource.Factory> getRegistrations() {
		if (registrations == null) {
			super.getRegistrations();
			registrations.put(XML_EXTENSION, new RegiondefinitionResourceFactoryImpl());
			registrations.put(STAR_EXTENSION, new RegiondefinitionResourceFactoryImpl());
		}
		return registrations;
	}

} //RegiondefinitionXMLProcessor
