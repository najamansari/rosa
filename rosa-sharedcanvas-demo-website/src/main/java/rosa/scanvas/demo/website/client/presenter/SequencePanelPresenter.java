package rosa.scanvas.demo.website.client.presenter;

import java.util.Iterator;

import rosa.scanvas.demo.website.client.PanelData;
import rosa.scanvas.demo.website.client.event.PanelDisplayedEvent;
import rosa.scanvas.demo.website.client.widgets.PageTurnerWidget;
import rosa.scanvas.demo.website.client.widgets.ThumbnailWidget;
import rosa.scanvas.demo.website.client.widgets.ThumbnailWidget.ThumbnailImageWidget;
import rosa.scanvas.model.client.Canvas;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.ScrollEvent;
import com.google.gwt.event.dom.client.ScrollHandler;
import com.google.gwt.event.logical.shared.HasSelectionHandlers;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;

public class SequencePanelPresenter implements PanelPresenter {

    public interface Display extends IsWidget {
        PageTurnerWidget getPageTurnerWidget();

        ThumbnailWidget getThumbnailWidget();

        void setSelectedTab(int index);

        HasSelectionHandlers<Integer> getTabPanelSelector();

        void resize(int width, int height);
    }

    private final HandlerManager eventBus;
    private final Display display;
    private final int panel_id;

    private int tab;
    private int visiblePosition;

    private PanelData data;
    private int canvasIndex;
    private Canvas[] canvas = new Canvas[2];

    public SequencePanelPresenter(Display display, HandlerManager eventBus,
            int panel_id) {
        this.eventBus = eventBus;
        this.display = display;
        this.panel_id = panel_id;

        visiblePosition = 0;
    }

    /**
     * Add handlers to listen for DOM events
     */
    private void bind() {
        display.getTabPanelSelector().addSelectionHandler(
                new SelectionHandler<Integer>() {
                    public void onSelection(SelectionEvent<Integer> event) {
                        doSelection(event.getSelectedItem());
                    }
                });

        display.getThumbnailWidget().getTablePanel()
                .addScrollHandler(new ScrollHandler() {
                    public void onScroll(ScrollEvent event) {
                        doScroll();
                    }
                });

        display.getPageTurnerWidget().getPrevButton()
                .addClickHandler(new ClickHandler() {
                    public void onClick(ClickEvent event) {
                        doPrevious();
                    }
                });

        display.getPageTurnerWidget().getNextButton()
                .addClickHandler(new ClickHandler() {
                    public void onClick(ClickEvent event) {
                        doNext();
                    }
                });

        display.getPageTurnerWidget().getJumpButton()
                .addClickHandler(new ClickHandler() {
                    public void onClick(ClickEvent event) {
                        doJump();
                    }
                });
    }

    /**
     * Once the thumbnail data has been set, add handlers to listen for DOM
     * events
     */
    private void bindThumbnails() {
        Iterator<Widget> it = display.getThumbnailWidget().getThumbTable()
                .iterator();
        while (it.hasNext()) {
            ThumbnailImageWidget thumb = (ThumbnailImageWidget) it.next();
            final String[] data = { thumb.getCollectionUri(),
                    thumb.getManifestUri(), thumb.getSequenceUri(),
                    thumb.getCanvasUri() };
            thumb.addClickHandler(new ClickHandler() {
                public void onClick(ClickEvent event) {
                    doThumbnailClick(data);
                }
            });
        }
    }

    private void bindPageTurner(final PanelData data) {
        FlowPanel main = display.getPageTurnerWidget().getCanvasDisplayPanel();

        try {
            FocusPanel panel = (FocusPanel) main.getWidget(0);
            panel.addClickHandler(new ClickHandler() {
                public void onClick(ClickEvent event) {
                    doPageTurnerClick(data, 0);
                }
            });

            panel = (FocusPanel) main.getWidget(1);
            panel.addClickHandler(new ClickHandler() {
                public void onClick(ClickEvent event) {
                    doPageTurnerClick(data, 1);
                }
            });
        } catch (IndexOutOfBoundsException e) {
        }

    }

    /**
     * Action for scroll events in thumbnail browser. Responsible for paging
     */
    private void doScroll() {
        int rowHeight = display.getThumbnailWidget().getRowHeight(0);
        int difference = display.getThumbnailWidget()
                .getVerticalScrollPosition() - visiblePosition;

        if (difference > 0) {
            display.getThumbnailWidget().loadThumbnails(visiblePosition);
            visiblePosition = difference + visiblePosition;
        }
    }

    /**
     * Action for selection events (changing tabs) in CanvasNav view
     */
    private void doSelection(int selection) {
        // TODO change to fire an event on the eventBus, instead of directly
        // accessing the History

        // TODO why should this be in history

        // tab = selection;
        // History.newItem(HistoryInfo.setAttribute(panelId, 2,
        // String.valueOf(selection)));
    }

    /**
     * Set data in Page Turner to the next opening
     */
    private void doNext() {
        canvasIndex += 2;
        canvas[0] = data.getSequence().canvas(canvasIndex - 1);
        canvas[1] = data.getSequence().canvas(canvasIndex);

        display.getPageTurnerWidget().setData(canvas,
                data.getImageAnnotations());
    }

    /**
     * Set data in Page Turner to the previous opening
     */
    private void doPrevious() {

    }

    /**
     * Set data in Page Turner to the opening specified in the text box
     */
    private void doJump() {
        String page = display.getPageTurnerWidget().getPageText();
    }

    /**
     * Action for click events in thumbnail browser
     */
    private void doThumbnailClick(String[] uris) {
        // TODO
        // eventBus.fireEvent(new GetDataEvent(HistoryInfo.newToken(panelId,
        // "canvas", "0", uris[0], uris[1], uris[2], uris[3])));
    }

    private void doPageTurnerClick(final PanelData data, int index) {
        // TODO
        // eventBus.fireEvent(new GetDataEvent(HistoryInfo.newToken(panelId,
        // "canvas", "0", data.tokenForData())));
    }

    // ------------------------------------------------

    public void setSelectedTab(int index) {
        display.setSelectedTab(index);

    }

    /**
     * Returns the index of the canvas of the first page with label 1r, or 001r,
     * etc. If no such canvas exists, returns NULL.
     */
    private int findFirstPage() {
        Iterator<Canvas> it = data.getSequence().iterator();
        int index = 0;

        while (it.hasNext()) {
            Canvas canv = it.next();

            if (canv.label().equals("1r") || canv.label().equals("001r")) {
                return index;
            }
            index++;
        }
        return -1;
    }

    public void setPageTurner() {

        if (canvas[0] != null) {

        } else {
            canvasIndex = findFirstPage();
            if (canvasIndex > 0) {
                canvas[0] = data.getSequence().canvas(canvasIndex - 1);
                canvas[1] = data.getSequence().canvas(canvasIndex);

                display.getPageTurnerWidget().setData(canvas,
                        data.getImageAnnotations());
            }
        }
    }

    @Override
    public Widget asWidget() {
        return display.asWidget();
    }

    @Override
    public void display(PanelData data) {
        this.data = data;

        if (tab == 0) {
            display.getThumbnailWidget().setData(data);
            bindThumbnails();
        } else if (tab == 1) {
            setPageTurner();
            bindPageTurner(data);
        }

        PanelDisplayedEvent event = new PanelDisplayedEvent(panel_id, data);
        eventBus.fireEvent(event);
    }

    @Override
    public void resize(int width, int height) {
        display.resize(width, height);
    }

}