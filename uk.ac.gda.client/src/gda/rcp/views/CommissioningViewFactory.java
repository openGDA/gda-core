package gda.rcp.views;

import java.util.List;
import java.util.Map;

import org.eclipse.ui.part.ViewPart;

/**
 * View factory for creating a {@link CommissioningView} with specific contents.
 *
 * To use it, add a view to the org.eclipse.ui.views extension points in the client plugin.xml. e.g. :
 * <pre>
 *  {@code
      <view
            class="gda.rcp.views.ViewFactoryFinder:linkamControlView"
            id="gda.rcp.views.CommissioningView.linkamControlView"
            name="Script Chooser and Live Linkam Control"
            category="uk.ac.gda.client.general.category"
            icon="platform:/plugin/uk.ac.gda.common.rcp/icons/table_multiple.png"
            restorable="true">
      </view>
      }
 * </pre>
 * Where 'linkamControlView' should be replaced with the ID of the CommissioningViewFactory Bean in the client side spring
 * configuration that you want the view to use. E.g.:
 * <pre>
 *  {@code
	<bean id="linkamControlView"	class="gda.rcp.views.CommissioningViewFactory">
		<property name="title" value="Linkam" />
		<property name="header" value="Linkam Sample Environment" />
		<property name="sections">
			<util:list>
				<util:map>
					<entry key="title" value="Linkam Furnace" />
					<entry key="description" value="Linkam Furnace controller" />
					<entry key="elements">
						<util:list>
							<util:map>
								<entry key="name" value="Temperature" />
								<entry key="scannable" value-ref="linkam_temperature" />
								<entry key="readonly" value="true" />
							</util:map>
						</util:list>
					</entry>
				</util:map>
			</util:list>
		</property>
	</bean>
      }
 * </pre>
 *
 * The view can be referenced in the usual way from menu items and perspectives etc.
 *
 * This is based on uk.ac.gda.beamline.i19.shared.views.CommissioningViewFactory
 * as of commit 1ee5e754467119ffdef03e848d39913c60b4cbc7
 */

public class CommissioningViewFactory extends FindableViewFactoryBase {

	private List<Map<String, Object>> sections;
	private String header;
	private String title;

	public void setHeader(String header) {
		this.header = header;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public void setSections(List<Map<String, Object>> sections) {
		this.sections = sections;
	}

	@Override
	public ViewPart createView() {
		CommissioningView commview = new CommissioningView();
		ViewUtils.setViewName(commview, title);
		commview.setHeader(header);
		commview.setSections(sections);
		return commview;
	}
}
