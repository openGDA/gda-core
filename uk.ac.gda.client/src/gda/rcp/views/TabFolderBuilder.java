package gda.rcp.views;

import org.eclipse.swt.widgets.Composite;

/**
 * Helps the creation of a tab composite.
 *
 * @author Maurizio Nagni
 *
 */
public class TabFolderBuilder {

	/**
	 * Identifies a property in the composite data created by {@link CompositeFactory#createComposite(Composite, int)}.
	 * Calling getData(CTAB_FOLDER) that composite returns the inner {@link TabFolderComposite}.
	 * This is property can be useful when is necessary to apply custom layout on the inner tab composite
	 */
	public static String CTAB_FOLDER = "CTAB_FOLDER";

	private final CompositeFactoriesBuilder<TabCompositeFactory> builder = new CompositeFactoriesBuilder<>();

	public TabFolderBuilder addTab(TabCompositeFactory tab) {
		builder.add(tab);
		return this;
	}

	public CompositeFactory build() {
		TabFolderCompositeFactory tabsFactory = new TabFolderCompositeFactory();
		tabsFactory.setFactories(builder.build());
		return tabsFactory;
	}
}
