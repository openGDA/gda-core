package uk.ac.diamond.tomography.reconstruction.views;

import org.eclipse.core.resources.IResource;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.model.WorkbenchLabelProvider;
import org.eclipse.ui.navigator.ICommonContentExtensionSite;
import org.eclipse.ui.navigator.ICommonLabelProvider;

/**
 * <p>
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as part of a work in progress. There is a
 * guarantee neither that this API will work nor that it will remain the same. Please do not use this API without
 * consulting with the Platform/UI team.
 * </p>
 * 
 * @since 3.2
 */
public class NxsNavigatorLabelProvider extends WorkbenchLabelProvider implements ICommonLabelProvider {

	@Override
	public void init(ICommonContentExtensionSite aConfig) {
		// init
	}
	
	@Override
	protected String decorateText(String input, Object element) {
		if (element instanceof IResource) {
			return String.format("%s (%s)", super.decorateText(input, element), ((IResource) element).getFullPath());
		}
		return input;
	}
	
	@Override
	public String getDescription(Object anElement) {

		if (anElement instanceof IResource) {
			IResource resource = (IResource) anElement;
			return String.format("%s (%s)", resource.getFullPath().makeRelative().toString(), resource.getFullPath());
		}
		return null;
	}

	@Override
	public void restoreState(IMemento aMemento) {

	}

	@Override
	public void saveState(IMemento aMemento) {
	}
}