package rosa.scanvas.viewer.client.presenter;

import rosa.scanvas.viewer.client.HistoryInfo;
import rosa.scanvas.viewer.client.PanelProperties;
import rosa.scanvas.viewer.client.event.GetDataEvent;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.dom.client.HasKeyUpHandlers;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.Hyperlink;
import com.google.gwt.user.client.ui.Widget;

public class HomePresenter implements Presenter {

	public interface Display {
		Hyperlink getRoseDataLink();
		Hyperlink getTestDataLink();
		HasClickHandlers getGoButton();
		HasValue<String> getUserUrlText();
		HasKeyUpHandlers getUserUrlKeyUpHandlers();
		Widget asWidget();
	}
	
	private final int row;
	private final int col;
	private final Display display;
	private final HandlerManager eventBus;
	
	private final PanelProperties props;
	
	public HomePresenter(Display display, HandlerManager eventBus, PanelProperties props) {
		row = props.getRow();
		col = props.getCol();
		
		this.props = props;
		this.display = display;
		this.eventBus = eventBus;
	}
	
	public void go(HasWidgets container) {
		bind();
		if (container instanceof FlexTable) {
			((FlexTable) container).setWidget(
					row, col, display.asWidget());
		}
		
		
	}
	
	private void bind() {
		bindLinks();
		
		display.getGoButton().addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				doGoClick();
			}
		});
		
		display.getUserUrlKeyUpHandlers().addKeyUpHandler(new KeyUpHandler() {
			public void onKeyUp(KeyUpEvent event) {
				if (event.getNativeKeyCode() == KeyCodes.KEY_ENTER) {
					doGoClick();
				}
			}
		});
	}
	
/*	private void gotoRoseDataLink() {
		String url = "http://rosetest.library.jhu.edu/sc";
		
		eventBus.fireEvent(new GetDataEvent(HistoryInfo.newToken(props.getId(), "collection")));
	}
	
	private void gotoTestDataLink() {
		eventBus.fireEvent(new GetDataEvent(HistoryInfo.newToken(props.getId(), "manifest")));
	}*/
	
	private void bindLinks() {
		display.getRoseDataLink().setText("Rose Data");
		display.getTestDataLink().setText("Test Data");
		
		// TODO rebind history token for multiple panels
		
		display.getRoseDataLink().setTargetHistoryToken(
				HistoryInfo.newToken(props.getId(), "collection", "0", "RoseData"));
		// http://rosetest.library.jhu.edu/sc
		
		display.getTestDataLink().setTargetHistoryToken(
				HistoryInfo.newToken(props.getId(), "manifest", "0", "X", "TestLudwigXV7"));
	}
	
	private void doGoClick() {
		// TODO the case of real data: when someone puts in a real URL
		// data will be read from URL, then it must be determined if it is a collection of manifests
		// or a single manifest
		String url = display.getUserUrlText().getValue();
		
		if (url != null && !url.equals("")) {
			eventBus.fireEvent(new GetDataEvent(HistoryInfo.newToken(props.getId(), url, "0")));
		}
	}
	
}
