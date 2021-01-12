package gda.rcp.views;

import java.util.Arrays;
import java.util.HashMap;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

import uk.ac.gda.client.exception.GDAClientException;
import uk.ac.gda.ui.tool.ClientSWTElements;

/**
 * Helps the creation of a tab composite. This tab composite provides also a functionality which allows a tab to acquire
 * in exclusive more the CTabFolder using a {@link ReservableControl} instance.
 *
 * @author Maurizio Nagni
 *
 */
public class TabFolderBuilder {

	private static final Logger logger = LoggerFactory.getLogger(TabFolderBuilder.class);

	/**
	 * Identifies a property in the composite data created by {@link CompositeFactory#createComposite(Composite, int)}.
	 * Calling getData(CTAB_FOLDER) that composite returns the inner {@link TabFolderComposite}. This is property can be
	 * useful when is necessary to apply custom layout on the inner tab composite
	 */
	public static final String CTAB_FOLDER = "CTAB_FOLDER";

	/**
	 * Identifies a property in {@link CTabFolder} containing a {@link ReservableControl} instance. The same property
	 * and instance is set into each {@link CTabItem} control's data. When a composite execute successfully
	 * {@link ReservableControl#reserve(Object)} CTabFolder guarantees that the tab cannot be deselected until the same
	 * composite executes {@link ReservableControl#release(Object)}
	 */
	public static final String LOCK_TAB = "LOCK_TAB";

	private final CompositeFactoriesBuilder<TabCompositeFactory> builder = new CompositeFactoriesBuilder<>();
	private SelectionListener selectionListener;
	private final ReservableControl lockTab = new ReservedControl();

	public TabFolderBuilder addTab(TabCompositeFactory tab) {
		builder.add(tab);
		return this;
	}

	public CompositeFactory build() {
		try {
			return new TabFolderCompositeFactory(builder.build(), selectionListener);
		} catch (GDAClientException e) {
			logger.error("Cannot build the Tab", e);
			return missingCompositeFactory();
		}
	}

	private TabFolderCompositeFactory missingCompositeFactory() {
		return new TabFolderCompositeFactory(new TabCompositeFactory[] {missingTab()}, null) ;
	}

	public class TabFolderCompositeFactory implements CompositeFactory, InitializingBean {
		protected final TabCompositeFactory[] factories;
		private final SelectionListener selectionListener;

		public TabFolderCompositeFactory(TabCompositeFactory[] factories, SelectionListener selectionListener) {
			super();
			this.factories = factories;
			this.selectionListener = selectionListener;
		}

		@Override
		public Composite createComposite(Composite parent, int style) {
			final TabFolderComposite comp = new TabFolderComposite(parent, style, factories, selectionListener);
			comp.createControls();
			comp.setData(TabFolderBuilder.CTAB_FOLDER, comp.getTabFolder());
			return comp;
		}

		@Override
		public void afterPropertiesSet() throws Exception {
			if (factories == null) {
				throw new IllegalArgumentException("availableModes == null");
			}
		}

	}

	class TabFolderComposite extends Composite {

		private CTabFolder tabFolder;
		protected TabCompositeFactory[] availableModes;
		private HashMap<TabCompositeFactory, CTabItem> tabs;
		private final SelectionListener selectionListener;

		public TabFolderComposite(Composite parent, int style, TabCompositeFactory[] availableModes,
				SelectionListener selectionListener) {
			super(parent, style);
			this.availableModes = availableModes;
			this.selectionListener = selectionListener;
		}

		public CTabFolder getTabFolder() {
			return tabFolder;
		}

		void createControls() {
			GridLayoutFactory.fillDefaults().numColumns(1).margins(0, 0).spacing(0, 0).applyTo(this);
			GridDataFactory.fillDefaults().grab(true, false).applyTo(this);
			tabFolder = new CTabFolder(this, SWT.TOP | SWT.BORDER);
			tabFolder.setData(LOCK_TAB, lockTab);
			GridLayoutFactory.fillDefaults().numColumns(1).margins(0, 0).spacing(0, 0).applyTo(tabFolder);
			GridDataFactory.fillDefaults().grab(true, false).applyTo(tabFolder);

			tabs = new HashMap<>();
			for (int i = 0; i < availableModes.length; i++) {
				TabCompositeFactory mode = availableModes[i];
				if (mode.isEnabled() || !mode.isVisible()) {
					CTabItem cTab = new CTabItem(tabFolder, SWT.NONE);
					Image tabImage = mode.getImage();
					if (tabImage != null) {
						cTab.setImage(tabImage);
					}
					cTab.setText(mode.getLabel());
					cTab.setToolTipText(mode.getTooltip());
					Control control = mode.createComposite(tabFolder, SWT.NONE);
					control.setData(LOCK_TAB, lockTab);
					cTab.setControl(control);
					tabs.put(mode, cTab);
				}

			}
			tabFolder.setSelection(0);
			tabFolder.addSelectionListener(getTabSelectionListener());
			setVisible(true);
		}

		/**
		 * Validates a tab selection aborting the opeartion if a CTabItem control already reserved
		 * {@link TabFolderBuilder.LOCK_TAB} property
		 *
		 * @return
		 */
		private SelectionListener getTabSelectionListener() {
			return new SelectionListener() {

				@Override
				public void widgetSelected(SelectionEvent e) {
					if (CTabFolder.class.isInstance(e.getSource())) {
						CTabFolder tabFolderInstance = CTabFolder.class.cast(e.getSource());
						ReservableControl lock = (ReservableControl) tabFolderInstance
								.getData(TabFolderBuilder.LOCK_TAB);
						if (!lock.isReserved() || lock.isOwner(tabFolderInstance.getSelection().getControl())) {
							return;
						}

						Arrays.stream(tabFolderInstance.getItems())
							.filter(ct -> lock.isOwner(ct.getControl()))
							.findFirst()
							.ifPresent(tabFolderInstance::setSelection);
					}
				}

				@Override
				public void widgetDefaultSelected(SelectionEvent e) {
					widgetSelected(e);
				}
			};
		}
	}

	private TabCompositeFactory missingTab() {
		return new TabCompositeFactory() {

			@Override
			public Composite createComposite(Composite parent, int style) {
				Composite container = ClientSWTElements.createComposite(parent, SWT.NONE, 1);
				ClientSWTElements.createClientGridDataFactory().align(SWT.FILL, SWT.FILL).grab(true, false)
						.indent(5, SWT.DEFAULT).applyTo(container);
				ClientSWTElements.createClientLabel(container, SWT.NONE, getLabel());
				return container;
			}

			@Override
			public boolean isEnabled() {
				return false;
			}

			@Override
			public boolean isVisible() {
				return true;
			}

			@Override
			public String getTooltip() {
				return "This tab is incomplete";
			}

			@Override
			public String getLabel() {
				return "Crashed tab";
			}

			@Override
			public Image getImage() {
				return null;
			}
		};
	}
}
