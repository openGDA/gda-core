package org.opengda.detector.electronanalyser.client.sequenceeditor;

import java.util.List;

import org.eclipse.emf.common.notify.Adapter;
import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.notify.Notifier;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.util.EContentAdapter;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.opengda.detector.electronanalyser.model.regiondefinition.api.Region;

public class SequenceViewContentProvider implements IStructuredContentProvider {

	private Viewer viewer;
	private Resource res;

	public SequenceViewContentProvider(Resource res) {
		this.res = res;
		if (res != null) {
			res.eAdapters().add(notifyListener);
		}
	}

	private Adapter notifyListener = new EContentAdapter() {

		@Override
		public void notifyChanged(Notification notification) {
			super.notifyChanged(notification);
			if (notification.getNotifier() != null) {
				viewer.refresh();
			}
		}

	};

	@Override
	public void dispose() {
		if (res != null) {
			res.eAdapters().remove(notifyListener);
		}
	}

	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		this.viewer = viewer;
	}

	@Override
	public Object[] getElements(Object inputElement) {
		if (inputElement instanceof List) {
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
