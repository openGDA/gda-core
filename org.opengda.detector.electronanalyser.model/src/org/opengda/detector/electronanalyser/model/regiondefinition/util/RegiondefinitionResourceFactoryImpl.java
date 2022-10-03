/**
 */
package org.opengda.detector.electronanalyser.model.regiondefinition.util;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.impl.ResourceFactoryImpl;
import org.eclipse.emf.ecore.xmi.XMLResource;

/**
 * <!-- begin-user-doc -->
 * The <b>Resource Factory</b> associated with the package.
 * <!-- end-user-doc -->
 * @see org.opengda.detector.electronanalyser.model.regiondefinition.util.RegiondefinitionResourceImpl
 * @generated
 */
public class RegiondefinitionResourceFactoryImpl extends ResourceFactoryImpl {
	/**
	 * Creates an instance of the resource factory.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public RegiondefinitionResourceFactoryImpl() {
		super();
	}

	/**
	 * Creates an instance of the resource.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated NOT
	 */
	@Override
	public Resource createResource(URI uri) {
		RegiondefinitionResourceImpl result = new RegiondefinitionResourceImpl(uri);
		result.getDefaultSaveOptions().put(XMLResource.OPTION_KEEP_DEFAULT_CONTENT, Boolean.TRUE);
		return result;
	}

} //RegiondefinitionResourceFactoryImpl
