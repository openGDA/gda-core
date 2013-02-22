package org.opengda.detector.electronanalyser.client.sequenceeditor;

import java.util.List;

import org.eclipse.emf.common.notify.Adapter;
import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.util.EContentAdapter;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.opengda.detector.electronanalyser.client.RegionDefinitionResourceUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SequenceViewContentProvider implements IStructuredContentProvider {
	private static final Logger logger = LoggerFactory
			.getLogger(SequenceViewContentProvider.class);

	private Viewer viewer;
	private Resource res;
	private RegionDefinitionResourceUtil resUtil;

	public SequenceViewContentProvider(RegionDefinitionResourceUtil resUtil) {
		this.resUtil = resUtil;
		// this.res = res;
		// if (res != null) {
		// res.eAdapters().add(notifyListener);
		// }
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
				return resUtil.getRegions(false).toArray();
			} catch (Exception e) {
				logger.error("Cannot load regions in the sequence.",e);
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
