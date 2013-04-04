package rosa.scanvas.viewer.client.view;

import rosa.scanvas.viewer.client.presenter.SidebarFullPresenter;
import rosa.scanvas.viewer.client.widgets.AnnotationListWidget;
import rosa.scanvas.viewer.client.widgets.ManifestListWidget;

import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.TabLayoutPanel;
import com.google.gwt.user.client.ui.Widget;

public class SidebarFullView extends Composite implements SidebarFullPresenter.Display {

	private TabLayoutPanel tabPanel = new TabLayoutPanel(4, Style.Unit.EM);
	
	private FlowPanel mainPanel = new FlowPanel();
	private FlowPanel panelChangePanel = new FlowPanel();
	private FlowPanel linkPanel = new FlowPanel();
	
	private ListBox panelList = new ListBox();
	private Button addPanelButton = new Button();
	private Button removePanelButton = new Button();
	
	private Label panelListLabel = new Label("Current Panel: ");
	
	private AnnotationListWidget annoListWidget = new AnnotationListWidget();
	private ManifestListWidget metaListWidget = new ManifestListWidget();
	
	public HasClickHandlers getAddPanelButton() { return addPanelButton; }
	public HasClickHandlers getRemovePanelButton() { return removePanelButton; }
	public ListBox getPanelList() { return panelList; }
	public AnnotationListWidget getAnnoListWidget() { return annoListWidget; }
	public ManifestListWidget getMetaListWidget() { return metaListWidget; }
	public Widget asWidget() { return this; }
	
	
	public SidebarFullView() {
		initWidget(mainPanel);
		ScrollPanel metaScrollPanel = new ScrollPanel();
		ScrollPanel annoScrollPanel = new ScrollPanel();
		
		mainPanel.add(panelListLabel);
		mainPanel.add(panelChangePanel);
		mainPanel.add(tabPanel);
		mainPanel.add(linkPanel);
		
		tabPanel.add(metaScrollPanel, "Metadata");
		tabPanel.add(annoScrollPanel, "Annotations");
		
		metaScrollPanel.add(metaListWidget);
		annoScrollPanel.add(annoListWidget);
		
		panelChangePanel.add(panelList);
		panelChangePanel.add(addPanelButton);
		panelChangePanel.add(removePanelButton);
		panelList.setWidth("125px");
		addPanelButton.setText("Add");
		removePanelButton.setText("Remove");
		
//		panelChangePanel.setStyleName("horizontalFlowPanel");
		
		linkPanel.add(new Label("this will be a link"));
		
		mainPanel.setWidth("250px");
		mainPanel.setHeight("100%");
		
		panelChangePanel.setHeight("5%");
		tabPanel.setHeight("90%");
		tabPanel.setWidth("100%");
		linkPanel.setHeight("100%");
	}

}