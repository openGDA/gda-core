package org.opengda.detector.electroanalyser.client.regioneditor;

import java.util.Collections;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.part.ViewPart;
import org.opengda.detector.electronanalyser.model.regiondefinition.api.Region;
import org.opengda.detector.electronanalyser.model.regiondefinition.util.RegionDefinitionResourceUtil;

public class RegionView extends ViewPart {

	private RegionDefinitionResourceUtil regionDefinitionResourceUtil;

	@Override
	public void createPartControl(Composite parent) {
		Composite regionViewComposite = new Composite(parent, SWT.None);
		regionViewComposite.setLayout(new GridLayout(2, true));

		Composite leftComposite = new Composite(regionViewComposite, SWT.None);
		leftComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
		leftComposite.setLayout(new GridLayout(2, false));

		Label lblLowEnergy = new Label(leftComposite, SWT.None);
		lblLowEnergy.setText("Low");
		lblLowEnergy.setLayoutData(new GridData(GridData.FILL_BOTH));

		Text txtLowEnergy = new Text(leftComposite, SWT.None);
		txtLowEnergy.setLayoutData(new GridData(GridData.FILL_BOTH));

		Composite rightComposite = new Composite(regionViewComposite, SWT.None);
		rightComposite.setLayoutData(new GridData(GridData.FILL_BOTH));

		rightComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
		rightComposite.setLayout(new GridLayout(2, false));

		Label lblLens = new Label(rightComposite, SWT.None);
		lblLens.setText("Lens");
		lblLens.setLayoutData(new GridData(GridData.FILL_BOTH));

		Text txtLens = new Text(rightComposite, SWT.None);
		txtLens.setLayoutData(new GridData(GridData.FILL_BOTH));

		for (Region region : getRegions()) {
			Label lblRegionName = new Label(regionViewComposite, SWT.None);
			lblRegionName.setText(region.getName());
			GridData layoutData = new GridData();
			layoutData.horizontalSpan = 2;
			lblRegionName.setLayoutData(layoutData);
		}

	}

	private List<Region> getRegions() {
		if (regionDefinitionResourceUtil != null) {
			return regionDefinitionResourceUtil.getRegions(false);
		}
		return Collections.emptyList();
	}

	@Override
	public void setFocus() {

	}

	public void setViewPartName(String viewPartName) {
		setPartName(viewPartName);
	}

	public void setRegionDefinitionResourceUtil(
			RegionDefinitionResourceUtil regionDefinition) {
		this.regionDefinitionResourceUtil = regionDefinition;

	}

}
