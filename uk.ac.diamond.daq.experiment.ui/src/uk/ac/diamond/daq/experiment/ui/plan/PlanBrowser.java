package uk.ac.diamond.daq.experiment.ui.plan;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.DelegatingStyledCellLabelProvider.IStyledLabelProvider;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StyledString;

import gda.observable.IObservable;
import gda.observable.IObserver;
import gda.observable.ObservableComponent;
import gda.rcp.views.Browser;
import gda.rcp.views.TreeViewerBuilder;
import uk.ac.diamond.daq.experiment.api.ExperimentService;
import uk.ac.diamond.daq.experiment.api.Services;
import uk.ac.diamond.daq.experiment.api.plan.ExperimentPlanBean;
import uk.ac.gda.api.acquisition.resource.AcquisitionConfigurationResource;
import uk.ac.gda.api.acquisition.resource.AcquisitionConfigurationResourceType;
import uk.ac.gda.client.composites.AcquisitionsBrowserCompositeFactory;


/**
 * A browser listing all available {@link ExperimentPlanBean}s.
 * The widget can be created through {@link AcquisitionsBrowserCompositeFactory}.
 */
public class PlanBrowser extends Browser<ExperimentPlanBean> implements IObservable {

	private Consumer<ExperimentPlanBean> doubleClickProcessor;
	private Map<String, Consumer<ExperimentPlanBean>> processors;
	private ObservableComponent observableComponent = new ObservableComponent();

	/**
	 * @param doubleClickProcessor
	 * 					gets passed the selected item on double-click
	 *
	 * @param additionalProcessors
	 * 					each entry becomes a context menu item where the key is the name
	 * 					and the selected item gets passed to the entry's consumer
	 */
	public PlanBrowser(Optional<Consumer<ExperimentPlanBean>> doubleClickProcessor,
					   Optional<Map<String, Consumer<ExperimentPlanBean>>> additionalProcessors) {
		super(AcquisitionConfigurationResourceType.PLAN);
		this.doubleClickProcessor = doubleClickProcessor.orElse(ignored -> {});
		this.processors = additionalProcessors.orElse(Collections.emptyMap());
	}

	@Override
	public ITreeContentProvider getContentProvider() {
		return new FlatTreeContentProvider();
	}

	@Override
	public void addColumns(TreeViewerBuilder<AcquisitionConfigurationResource<ExperimentPlanBean>> builder) {
		builder.addColumn("Plan name", 520, new PlanNameLabelProvider());
	}

	@Override
	public TreeViewerBuilder<AcquisitionConfigurationResource<ExperimentPlanBean>> getTreeViewBuilder() {
		return new MapTree();
	}

	@SuppressWarnings("unchecked")
	@Override
	public ISelectionChangedListener getISelectionChangedListener(MenuManager contextMenu) {
		if (contextMenu != null) {
			contextMenu.addMenuListener(manager ->
				processors.entrySet().stream()
					.map(this::toContextMenuAction)
					.forEach(manager::add));
		}
		return event -> {
			setSelected((AcquisitionConfigurationResource<ExperimentPlanBean>) event.getStructuredSelection().getFirstElement());
			observableComponent.notifyIObservers(this, getSelected());
		};
	}

	private IAction toContextMenuAction(Map.Entry<String, Consumer<ExperimentPlanBean>> processorEntry) {
		return new Action(processorEntry.getKey()) {
			@Override
			public void run() {
				processorEntry.getValue().accept(getSelectedPlan());
			}
		};
	}

	public ExperimentPlanBean getSelectedPlan() {
		var selection = getSelected(); // FIXME optional
		return selection == null ? null : selection.getResource();
	}

	@Override
	public IDoubleClickListener getDoubleClickListener() {
		return event -> doubleClickProcessor.accept(getSelectedPlan());
	}

	class MapTree extends TreeViewerBuilder<AcquisitionConfigurationResource<ExperimentPlanBean>> {

		@SuppressWarnings("unchecked")
		@Override
		public AcquisitionConfigurationResource<ExperimentPlanBean>[] getInputElements(boolean reload) {

			ExperimentService service = Services.getExperimentService();

			return service.getExperimentPlanNames().stream()
				.map(service::getExperimentPlan)
				.map(plan -> new AcquisitionConfigurationResource<>(null, plan))
				.collect(Collectors.toList()).toArray(new AcquisitionConfigurationResource[0]);
		}

	}

	/**
	 * We are only pretending to be a tree so that we can filter.
	 * Delegates to {@link ArrayContentProvider}.
	 */
	class FlatTreeContentProvider implements ITreeContentProvider {

		private final IStructuredContentProvider provider = ArrayContentProvider.getInstance();

		@Override
		public Object[] getElements(Object inputElement) {
			return provider.getElements(inputElement);
		}

		@Override
		public Object[] getChildren(Object parentElement) {
			return new Object[0];
		}

		@Override
		public Object getParent(Object element) {
			return null;
		}

		@Override
		public boolean hasChildren(Object element) {
			return false;
		}

	}


	class PlanNameLabelProvider extends LabelProvider implements IStyledLabelProvider {

		@Override
		public StyledString getStyledText(Object element) {
			@SuppressWarnings("unchecked")
			AcquisitionConfigurationResource<ExperimentPlanBean> resource = (AcquisitionConfigurationResource<ExperimentPlanBean>) element;
			return new StyledString(resource.getResource().getPlanName());
		}
	}


	@Override
	public void addIObserver(IObserver observer) {
		observableComponent.addIObserver(observer);
	}

	@Override
	public void deleteIObserver(IObserver observer) {
		observableComponent.deleteIObserver(observer);
	}

	@Override
	public void deleteIObservers() {
		observableComponent.deleteIObservers();
	}

}
