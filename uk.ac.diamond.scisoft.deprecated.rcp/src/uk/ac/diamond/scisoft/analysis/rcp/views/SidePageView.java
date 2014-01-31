/*-
 * Copyright 2013 Diamond Light Source Ltd.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.ac.diamond.scisoft.analysis.rcp.views;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.part.IPage;
import org.eclipse.ui.part.MessagePage;
import org.eclipse.ui.part.PageBook;
import org.eclipse.ui.part.PageBookView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Deprecated
public class SidePageView extends PageBookView {

	public static final String ID = "uk.ac.diamond.scisoft.diffraction.rcp.DiffractionView";
	private static final Logger logger = LoggerFactory.getLogger(SidePageView.class);
	//private DataSetPlotter mainPlotter;
	
	
	@Override
	protected IPage createDefaultPage(PageBook book) {
		MessagePage messagePage = new MessagePage();
		initPage(messagePage);
		messagePage.setMessage("This is the Diffraction viewer");
		messagePage.createControl(book);
		return messagePage;
	}

	@Override
	protected PageRec doCreatePage(IWorkbenchPart part) {
		
		// TODO diffractionpage -> side.plot.page
		// Also the class must be an ISidePage
		IConfigurationElement[] config = Platform.getExtensionRegistry().getConfigurationElementsFor(
				"uk.ac.diamond.scisoft.analysis.rcp.diffractionpage");

		ISidePage page = null;
		try {
			for (IConfigurationElement e : config) {
				final Object o = e.createExecutableExtension("class");

				if (o instanceof ISidePage) {
					page = (ISidePage) o;
					if (!page.isApplicableFor((ISidePlotPart)part)) 
						continue;
					initPage(page);
					page.createControl(getPageBook());
					return new PageRec(part, page);
				} 
			}
		} catch (CoreException ex) {
			logger.warn("Could not find a page");
		}
		return null;
	}

	@Override
	protected void doDestroyPage(IWorkbenchPart part, PageRec record) {
		record.page.dispose();
	}

	@Override
	protected IWorkbenchPart getBootstrapPart() {
		IWorkbenchPage page = getSite().getPage();
		if (page != null) {
			IWorkbenchPart part = page.getActivePart();
			return isImportant(part) ? part : null;
		}
		return null;
	}

	@Override
	protected boolean isImportant(IWorkbenchPart part) {
		return part instanceof ISidePlotPart;
	}

//	if (part instanceof IMetadataProvider) {
//		IMetaData metadata = ((IMetadataProvider) part).getMetadata();
//		if (page instanceof ISidePageView)
//			((ISidePageView) page).setMetadataObject(metadata);
//		if (part instanceof PlotView) {
//			((PlotView) part).addDataObserver(((IObserver) page));
//			setMainPlotter(((PlotView) part).getMainPlotter());
//		}
//	}
	 
	@Override
	public void partActivated(IWorkbenchPart part) {

		super.partActivated(part);

		final IPage page = getCurrentPage();
		final String title = page instanceof IAdaptable ? (String) ((IAdaptable) page)
				.getAdapter(String.class) : null;
		if (title != null) {
			setPartName(title);
		} else {
			setPartName("Data");
		}

	}
}
