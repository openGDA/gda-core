package org.opengda.lde.ui.providers;

import java.util.List;

import org.eclipse.emf.common.notify.Adapter;
import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.util.EContentAdapter;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.opengda.lde.model.ldeexperiment.LDEExperimentsPackage;
import org.opengda.lde.ui.utils.LDEResourceUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SampleGroupViewContentProvider implements IStructuredContentProvider {
	private static final Logger logger = LoggerFactory.getLogger(SampleGroupViewContentProvider.class);

	private Viewer viewer;
	private LDEResourceUtil resUtil;

	public SampleGroupViewContentProvider(LDEResourceUtil resUtil) {
		this.resUtil = resUtil;
	}

	/**
	 * resource notify listener to update table selection during model instance modification.
	 */
	private Adapter notifyListener = new EContentAdapter() {
		@Override
		public void notifyChanged(Notification notification) {
			super.notifyChanged(notification);
			if (notification.getNotifier() != null) {
				// do not want to refresh when Region status change only.
				if (notification.getFeature() != null && !notification.getFeature().equals("null") && notification.getNotifier() != null
						&& !notification.getFeature().equals(LDEExperimentsPackage.eINSTANCE.getSample_Status())) {

					viewer.refresh();
					Table table = ((TableViewer) viewer).getTable();
					int itemCount = table.getItemCount();
					if (notification.getEventType() == Notification.ADD) {
						if (itemCount > 0) {
							TableItem item = table.getItem(itemCount - 1);
							viewer.setSelection(new StructuredSelection(item.getData()));
						}
					} else if (notification.getEventType() == Notification.REMOVE) {
						int position = notification.getPosition();
						TableItem item;
						if (itemCount > 0) {
							if (itemCount > position) {
								item = table.getItem(position);
							} else {
								item = table.getItem(itemCount - 1);
							}
							viewer.setSelection(new StructuredSelection(item.getData()));
						} else {
							viewer.setSelection(StructuredSelection.EMPTY);
						}
					}
				}
			}
		}
	};

	@Override
	public void dispose() {
		try {
			if (resUtil.getResource() != null) {
				resUtil.getResource().eAdapters().remove(notifyListener);
			}
		} catch (Exception e) {
			logger.error("Cannot get resource from RegionDefinitionResourceUtil", e);
		}
	}

	/**
	 * set input changed, also remove and add model instance modification notify listener from old input and new input respectively.
	 */
	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		this.viewer = viewer;
		// new input to be resource
		if (newInput != null && newInput instanceof Resource) {
			((Resource) newInput).eAdapters().add(notifyListener);
			if (oldInput instanceof Resource) {
				((Resource) oldInput).eAdapters().remove(notifyListener);
			}
		}
	}

	@Override
	public Object[] getElements(Object inputElement) {
		if (inputElement instanceof Resource) {
			try {
				return resUtil.getSamples().toArray();
			} catch (Exception e) {
				logger.error("Cannot load regions in the sequence.", e);
			}
		} else if (inputElement instanceof List) {
			List regionList = (List) inputElement;
			// for (Object object : regionList) {
			// if (object instanceof Region) {
			// Region region = (Region) object;
			// region.eAdapters().add(notifyListener);
			// }
			// }
			return regionList.toArray();
		}
		return null;
	}
}
