package gda.rcp.views;

/**
 * Helps the creation of a tab composite.
 * 
 * @author Maurizio Nagni
 *
 */
public class TabFolderBuilder {

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
