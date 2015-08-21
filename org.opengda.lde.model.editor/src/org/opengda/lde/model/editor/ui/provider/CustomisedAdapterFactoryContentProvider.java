package org.opengda.lde.model.editor.ui.provider;

import org.eclipse.emf.common.notify.AdapterFactory;
import org.eclipse.emf.edit.provider.IItemPropertySource;
import org.eclipse.emf.edit.ui.provider.AdapterFactoryContentProvider;
import org.eclipse.ui.views.properties.IPropertySource;

public class CustomisedAdapterFactoryContentProvider extends AdapterFactoryContentProvider {

	public CustomisedAdapterFactoryContentProvider(AdapterFactory adapterFactory) {
		super(adapterFactory);
	}
	@Override
	protected IPropertySource createPropertySource(Object object, IItemPropertySource itemPropertySource) {
		return new CustomisedPropertySource(object, itemPropertySource);
	}
}
