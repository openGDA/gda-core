/**
 */
package org.opengda.lde.model.ldeexperiment.util;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.impl.ResourceFactoryImpl;
import org.eclipse.emf.ecore.xmi.XMLResource;

/**
 * <!-- begin-user-doc -->
 * The <b>Resource Factory</b> associated with the package.
 * <!-- end-user-doc -->
 * @see org.opengda.lde.model.ldeexperiment.util.LDEExperimentsResourceImpl
 * @generated
 */
public class LDEExperimentsResourceFactoryImpl extends ResourceFactoryImpl {
	/**
	 * Creates an instance of the resource factory.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public LDEExperimentsResourceFactoryImpl() {
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
		Resource result = new LDEExperimentsResourceImpl(uri);
		((XMLResource) result).getDefaultSaveOptions().put(XMLResource.OPTION_KEEP_DEFAULT_CONTENT, Boolean.TRUE);
		return result;
	}

} //LDEExperimentsResourceFactoryImpl
