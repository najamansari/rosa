package rosa.scanvas.demo.website.client.presenter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import rosa.scanvas.demo.website.client.PanelData;
import rosa.scanvas.demo.website.client.PanelState;
import rosa.scanvas.demo.website.client.PanelView;
import rosa.scanvas.demo.website.client.dynimg.IIIFImageServer;
import rosa.scanvas.demo.website.client.dynimg.MasterImage;
import rosa.scanvas.demo.website.client.dynimg.WebImage;
import rosa.scanvas.demo.website.client.event.PanelDisplayedEvent;
import rosa.scanvas.demo.website.client.event.PanelRequestEvent;
import rosa.scanvas.demo.website.client.widgets.Opening;
import rosa.scanvas.demo.website.client.widgets.PageTurner;
import rosa.scanvas.demo.website.client.widgets.Thumbnail;
import rosa.scanvas.demo.website.client.widgets.ThumbnailBrowser;
import rosa.scanvas.model.client.Annotation;
import rosa.scanvas.model.client.AnnotationTarget;
import rosa.scanvas.model.client.Canvas;
import rosa.scanvas.model.client.Sequence;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.HasSelectionHandlers;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.client.Window;
public class SequencePanelPresenter implements PanelPresenter {
    public interface Display extends IsWidget {
        PageTurner getPageTurner();

        ThumbnailBrowser getThumbnailBrowser();

        void setSelectedTab(int index);

        HasSelectionHandlers<Integer> getTabPanelSelector();

        void resize(int width, int height);
        
        void selected(boolean is_selected);
    }

    private final HandlerManager eventBus;
    private final Display display;
    private final int panel_id;
    private int page_width, page_height;
    private int thumb_size;
    private int tab;
    private int frontmatter_pastedown;
    private int endmatter_pastedown;
    private int before_frontmatter;
    private int after_endmatter;
    boolean thumb_browser_setup;
    boolean page_turner_setup;

    private PanelData data;
    List<Opening> openings;

    public SequencePanelPresenter(Display display, HandlerManager eventBus,
            int panel_id) {
        this.eventBus = eventBus;
        this.display = display;
        this.panel_id = panel_id;
        
        this.thumb_size = 128;
        this.page_width = 200;
        this.page_height = 300;
        
        this.thumb_browser_setup = false;
        this.page_turner_setup = false;
        
        this.frontmatter_pastedown = 0;
        this.endmatter_pastedown = 0;
        this.before_frontmatter = 0;
        this.after_endmatter = 0;

        display.getTabPanelSelector().addSelectionHandler(
                new SelectionHandler<Integer>() {
                    @Override
                    public void onSelection(SelectionEvent<Integer> sel) {
                        tab = sel.getSelectedItem();

                        if (tab == 0) {
                            if (!page_turner_setup) {
                                setup_page_turner();
                            }
                        } else if (tab == 1) {
                            if (!thumb_browser_setup) {
                                setup_thumb_browser();
                            }
                        }
                    }
                });
        
        final PageTurner turner = display.getPageTurner();
        
        turner.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                /*int canvas_index = turner.getPosition() * 2;
                
                if (!turner.clickedVerso()) {
                    canvas_index++;
                }
                
                if (canvas_index > frontmatter_pastedown) {
                	canvas_index -= before_frontmatter;
                }
                
                if (canvas_index > endmatter_pastedown) {
                	canvas_index -= after_endmatter;
                }*/
                
            	int canvas_index = turner.getClickedIndex();
            	
                gotoCanvasView(canvas_index);
            }
        });
    }

    /**
     * Once the thumbnail data has been set, add handlers to listen for DOM
     * events
     */
    private void bindThumbnails(List<Thumbnail> thumbs) {
        for (final Thumbnail thumb : thumbs) {
            thumb.addClickHandler(new ClickHandler() {
                public void onClick(ClickEvent event) {
                    /*gotoCanvasView(display.getThumbnailBrowser()
                            .getThumbnailIndex(thumb));*/
                	gotoCanvasView(thumb.canvasIndex());
                }
            });
        }
    }

    private void gotoCanvasView(int canvas_index) {
        PanelState state = new PanelState(PanelView.CANVAS, data.getSequence()
                .uri(), data.getManifest().uri(), canvas_index);
        PanelRequestEvent event = new PanelRequestEvent(
                PanelRequestEvent.PanelAction.CHANGE, panel_id, state);
        eventBus.fireEvent(event);
    }

    @Override
    public Widget asWidget() {
        return display.asWidget();
    }

    private Map<String, Annotation> map_targets(List<Annotation> annotations) {
        // target uri -> annotation
        // TODO maybe panel data should contain various uri mappings...
        Map<String, Annotation> targets = new HashMap<String, Annotation>();

        for (Annotation a : annotations) {
            // TODO favor image annotations of the whole canvas

            for (AnnotationTarget at : a.targets()) {
                if (!at.isSpecificResource()) {
                    targets.put(at.uri(), a);
                }
            }
        }

        return targets;
    }

    private List<Thumbnail> construct_thumbs_from_openings() {
    	if (openings == null) {
    		openings = build_openings(data.getSequence(), 
    				data.getImageAnnotations());
    	}
    	
    	List<Thumbnail> result = new ArrayList<Thumbnail>();
    	IIIFImageServer iiif_server = IIIFImageServer.instance();
    	
    	for (Opening opening : openings) {
    		if (opening == null) {
    			continue;
    		}
    		
    		if (opening.getVerso() != null) {
    			WebImage image_v = iiif_server.renderToSquare(opening.getVerso(),
    				thumb_size);
    			Thumbnail thumb_v = new Thumbnail(image_v, opening.getVersoLabel(),
    				opening.getVersoIndex());
    			
    			result.add(thumb_v);
    		}
    		
    		if (opening.getRecto() != null) {
    			WebImage image_r = iiif_server.renderToSquare(opening.getRecto(),
    				thumb_size);
    			Thumbnail thumb_r = new Thumbnail(image_r, opening.getRectoLabel(),
    				opening.getRectoIndex());
    			
    			result.add(thumb_r);
    		}
    	}
    	
    	return result;
    }
    
    private List<Thumbnail> construct_thumbs(Sequence sequence,
            List<Annotation> annotations) {
        List<Thumbnail> result = new ArrayList<Thumbnail>();

        IIIFImageServer iiif_server = IIIFImageServer.instance();
        Map<String, Annotation> targets = map_targets(annotations);
        
        for (int i = 0; i < sequence.size(); i++ ) {
        	Canvas c = sequence.canvas(i);
        	Annotation a = targets.get(c.uri());
        	
        	if (a != null) {
        		MasterImage master = as_master_image(a, c);
        		WebImage image = iiif_server.renderToSquare(master, thumb_size);
        		Thumbnail thumb = new Thumbnail(image, a.label(), i);
        		result.add(thumb);
        	}
        }
        
        return result;
    }
    
    private List<Opening> build_openings(Sequence sequence, 
    		List<Annotation> annotations) {
    	List<Opening> openings = new ArrayList<Opening>();
    	Map<String, Annotation> targets = map_targets(annotations);
    	int seq_size = sequence.size();
    	
    	frontmatter_pastedown = 0;
    	endmatter_pastedown = seq_size;
    	
    	// Find pastedown positions in the sequence
    	for (int i = 0; i < seq_size; i++) {
    		Canvas canvas = sequence.canvas(i);
    		Annotation ann = targets.get(canvas.uri());
    		
    		if (ann != null) {
    			if (ann.label().toLowerCase().equals("frontmatter.pastedown")) {
    				frontmatter_pastedown = i;
    			}
    			
    			if (ann.label().toLowerCase().equals("endmatter.pastedown")) {
    				endmatter_pastedown = i;
    			}
    		}
    	}
    	
    	// Grab any single images before frontmatter pastedown
    	for (int i = 0; i < frontmatter_pastedown; i++) {
    		Canvas canvas = sequence.canvas(i);
    		Annotation ann = targets.get(canvas.uri());
    		
    		MasterImage verso = null;
    		String verso_label = null;
    		
    		if (ann != null) {
    			verso = as_master_image(ann, canvas);
    			verso_label = ann.label();
    			
    			before_frontmatter++;
    			openings.add(new Opening(verso, verso_label, i, null, null, 0));
    		}
    	}
    	
    	// Grab openings between front/end - matter pastedowns
    	for (int i = frontmatter_pastedown; i < endmatter_pastedown;) {
            Canvas c1 = sequence.canvas(i++);
            Canvas c2 = i + 1 < seq_size ? sequence.canvas(i++) : null;

            Annotation a1 = targets.get(c1.uri());
            Annotation a2 = c2 == null ? null : targets.get(c2.uri());

            MasterImage verso = null;
            String verso_label = null;
            int verso_index = 0;
            MasterImage recto = null;
            String recto_label = null;
            int recto_index = 0;

            if (a1 != null) {
                verso = as_master_image(a1, c1);
                verso_label = a1.label();
                verso_index = i - 1;
            }

            if (a2 != null) {
                recto = as_master_image(a2, c2);
                recto_label = a2.label();
                recto_index = i - 1;
                
                verso_index -= 1;
            }
            
            openings.add(new Opening(verso, verso_label, verso_index, 
            		recto, recto_label, recto_index));
        }
    	
    	// Grab any single images after endmatter pastedown
    	for (int i = endmatter_pastedown + 1; i < seq_size; i++) {
    		Canvas canvas = sequence.canvas(i);
    		Annotation ann = targets.get(canvas.uri());
    		
    		MasterImage verso = null;
    		String verso_label = null;
    		
    		if (ann != null) {
    			verso = as_master_image(ann, canvas);
    			verso_label = ann.label();
    			
    			after_endmatter++;
    			openings.add(new Opening(verso, verso_label, i, null, null, 0));
    		}
    	}
    	
    	return openings;
    	
    }
    
    /*private List<Opening> construct_openings(Sequence sequence,
            List<Annotation> annotations) {
        List<Opening> openings = new ArrayList<Opening>();

        Map<String, Annotation> targets = map_targets(annotations);

        int seq_size = sequence.size();

        for (int i = 0; i < seq_size;) {
            Canvas c1 = sequence.canvas(i++);
            Canvas c2 = i + 1 < seq_size ? sequence.canvas(i++) : null;

            Annotation a1 = targets.get(c1.uri());
            Annotation a2 = c2 == null ? null : targets.get(c2.uri());

            MasterImage verso = null;
            String verso_label = null;
            MasterImage recto = null;
            String recto_label = null;

            if (a1 != null) {
                verso = as_master_image(a1, c1);
                verso_label = a1.label();
            }

            if (a2 != null) {
                recto = as_master_image(a2, c2);
                recto_label = a2.label();
            }
            
            openings.add(new Opening(verso, verso_label, recto, recto_label));
        }

        return openings;
    }*/

    // Assume annotation is image covering whole canvas using iiif
    private MasterImage as_master_image(Annotation a, Canvas canvas) {
        String id = IIIFImageServer.parseIdentifier(a.body().uri());
        return new MasterImage(id, canvas.width(), canvas.height());
    }

    private void setup_thumb_browser() {
        /*List<Thumbnail> thumbs = construct_thumbs(data.getSequence(),
                data.getImageAnnotations());*/
    	List<Thumbnail> thumbs = construct_thumbs_from_openings();
        display.getThumbnailBrowser().setThumbnails(thumbs);
        bindThumbnails(thumbs);
        thumb_browser_setup = true;
    }

    private void setup_page_turner() {
        /*List<Opening> openings = construct_openings(data.getSequence(),
                data.getImageAnnotations());*/
    	/*List<Opening> */openings = build_openings(data.getSequence(),
                data.getImageAnnotations());

        display.getPageTurner().setOpenings(openings, page_width, page_height);

        page_turner_setup = true;
    }

    @Override
    public void display(PanelData data) {
        this.data = data;

        thumb_browser_setup = false;
        page_turner_setup = false;

        if (tab == 0) {
            setup_page_turner();
        } else if (tab == 1) {
            setup_thumb_browser();
        }
        
        PanelDisplayedEvent event = new PanelDisplayedEvent(panel_id, data);
        eventBus.fireEvent(event);
    }

    @Override
    public void resize(int width, int height) {
        page_width = (width / 2) - 20;
        // TODO
        page_height = height - 120;

        display.getPageTurner().resize(page_width, page_height);

        // TODO scale thumb size

        display.resize(width, height);
    }
    
    @Override
    public void selected(boolean is_selected) {
    	display.selected(is_selected);
    }
}
