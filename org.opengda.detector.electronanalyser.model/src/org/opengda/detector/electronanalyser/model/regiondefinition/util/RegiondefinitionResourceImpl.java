/**
 */
package org.opengda.detector.electronanalyser.model.regiondefinition.util;

import java.util.Map;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.xmi.XMLResource;
import org.eclipse.emf.ecore.xmi.impl.XMLResourceImpl;

/**
 * <!-- begin-user-doc --> The <b>Resource </b> associated with the package.
 * <!-- end-user-doc -->
 * @see org.opengda.detector.electronanalyser.model.regiondefinition.util.RegiondefinitionResourceFactoryImpl
 * @generated
 */
public class RegiondefinitionResourceImpl extends XMLResourceImpl {
	/**
	 * Creates an instance of the resource.
	 * <!-- begin-user-doc --> <!--
	 * end-user-doc -->
	 * @param uri the URI of the new resource.
	 * @generated
	 */
	public RegiondefinitionResourceImpl(URI uri) {
		super(uri);
	}

	@Override
	public Map<Object, Object> getDefaultSaveOptions() {
		Map<Object, Object> defaultSaveOptions = super.getDefaultSaveOptions();
		defaultSaveOptions.put(OPTION_KEEP_DEFAULT_CONTENT, Boolean.TRUE);
		return defaultSaveOptions;
	}
} // RegiondefinitionResourceImpl
