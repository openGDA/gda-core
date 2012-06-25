package uk.ac.gda.client.tomo.configuration.view.xviewer;

import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.nebula.widgets.xviewer.XViewer;
import org.eclipse.nebula.widgets.xviewer.XViewerColumn;
import org.eclipse.nebula.widgets.xviewer.XViewerLabelProvider;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;

public class TomoConfigurationLabelProvider extends XViewerLabelProvider {
	Font font = null;
	private final XViewer xViewer;

	public TomoConfigurationLabelProvider(XViewer xViewerTest) {
		super(xViewerTest);
		this.xViewer = xViewerTest;
	}

	@Override
	public String getColumnText(Object element, XViewerColumn xCol, int columnIndex) {
		if (element instanceof TomoConfigContent) {
			TomoConfigContent configContent = (TomoConfigContent) element;

			String colId = xCol.getId();
			if (TomoConfigXViewerFactory.SAMPLE_DESC_COL_ID.equals(colId)) {
				return configContent.getSampleDescription();
			} else if (TomoConfigXViewerFactory.FLAT_EXPOSURE_TIME_COL_ID.equals(colId)) {
				return Double.toString(configContent.getFlatExposureTime());
			} else if (TomoConfigXViewerFactory.SAMPLE_EXPOSURE_TIME_COL_ID.equals(colId)) {
				return Double.toString(configContent.getSampleExposureTime());
			}
		}

		if (element instanceof String) {
			if (columnIndex == 1)
				return (String) element;
		}

		return "unhandled column";
	}

	@Override
	public void dispose() {
		if (font != null)
			font.dispose();
		font = null;
	}

	@Override
	public boolean isLabelProperty(Object element, String property) {
		return false;
	}

	@Override
	public void addListener(ILabelProviderListener listener) {
	}

	@Override
	public void removeListener(ILabelProviderListener listener) {
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.osee.framework.ui.skynet.widgets.xviewer.XViewerLabelProvider#getColumnImage(java.lang.Object,
	 * org.eclipse.osee.framework.ui.skynet.widgets.xviewer.XViewerColumn)
	 */
	@Override
	public Image getColumnImage(Object element, XViewerColumn xCol, int columnIndex) {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ITableColorProvider#getBackground(java.lang.Object, int)
	 */
	@Override
	public Color getBackground(Object element, int columnIndex) {
		return super.getBackground(element, columnIndex);
	}

}
