package uk.ac.diamond.daq.mapping.ui.sampletransfer;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The SampleTransferControlPanel class provides the user interface for controlling the
 * sample transfer process. It allows activating and deactivating the sample transfer,
 * setting up sample holder positions, and moving samples between the spigot and holders.
 */
public class SampleSetupPanel extends Composite {
    private static final Logger logger = LoggerFactory.getLogger(SampleSetupPanel.class);

    private HolderSelectionComposite selectionComposite;
    private HolderTransferComposite transferComposite;

    public SampleSetupPanel(Composite parent) {
        super(parent, SWT.NONE);
        setLayout(new GridLayout(1, false));

        CTabFolder tabFolder = new CTabFolder(this, SWT.BORDER);
        tabFolder.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        CTabItem selectionTab = new CTabItem(tabFolder, SWT.NONE);
        selectionTab.setText("Sample Selection");
        selectionComposite = new HolderSelectionComposite(tabFolder, SWT.NONE);
        selectionTab.setControl(selectionComposite);

        CTabItem transferTab = new CTabItem(tabFolder, SWT.NONE);
        transferTab.setText("Sample Transfer");
        transferComposite = new HolderTransferComposite(tabFolder, SWT.NONE);
        transferTab.setControl(transferComposite);

        tabFolder.setSelection(selectionTab);
    }
}
