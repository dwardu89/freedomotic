package it.freedomotic.gwtclient.client;

import java.util.Date;

import org.restlet.client.Client;
import org.restlet.client.Context;
import org.restlet.client.Request;
import org.restlet.client.Response;
import org.restlet.client.Uniform;
import org.restlet.client.representation.StringRepresentation;
import org.restlet.client.resource.ClientResource;
import org.restlet.client.resource.Result;
import org.restlet.data.Protocol;
import org.vectomatic.dom.svg.OMSVGRect;
import org.vectomatic.dom.svg.OMSVGSVGElement;
import org.vectomatic.dom.svg.ui.ExternalSVGResource;
import org.vectomatic.dom.svg.ui.SVGImage;
import org.vectomatic.dom.svg.ui.SVGResource;

import it.freedomotic.gwtclient.client.api.EnvironmentController;
import it.freedomotic.gwtclient.client.api.proxies.FreedomoticUrlResourceProxy;
import it.freedomotic.gwtclient.client.widgets.ConfigurationDialog;
import it.freedomotic.gwtclient.client.widgets.FloorPlanWidget;
import it.freedomotic.gwtclient.client.widgets.OkCancelDialogCallback;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.resources.client.ResourceCallback;
import com.google.gwt.resources.client.ResourceException;
import com.google.gwt.resources.client.ImageResource.ImageOptions;
import com.google.gwt.resources.client.ImageResource.RepeatStyle;
import com.google.gwt.user.client.Cookies;
import com.google.gwt.user.client.ui.DecoratedPopupPanel;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootLayoutPanel;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.SimplePanel;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class Freedomotic implements EntryPoint {
	/**
	 * The message displayed to the user when the server cannot be reached or
	 * returns an error.
	 */
	private static final String SERVER_ERROR = "An error occurred while "
			+ "attempting to contact the server. Please check your network "
			+ "connection and try again.";

	interface Images extends ClientBundle {		
		@Source("resources/background.png")
		@ImageOptions(repeatStyle = RepeatStyle.Both)
		public ImageResource logoBackground();
		
		 @Source("resources/logo-ivan-vector_trazo.svg")		 
	     SVGResource logo_svg();

	}

	interface MyCssResource extends CssResource {
		String headerPanel();
	}

	private String brokerIp;

	
	OMSVGSVGElement svg;
	
	/**
	 * This is the entry point method.
	 */
	public void onModuleLoad() {
		Images images = GWT.create(Images.class);
		Cookies.removeCookie("broker_ip");
		brokerIp = Cookies.getCookie("broker_ip");
		
		DockLayoutPanel myDockLayoutPanel = new DockLayoutPanel(Unit.EM);
		// draw the environment
		FloorPlanWidget floorPlan = new FloorPlanWidget(myDockLayoutPanel);
		RootLayoutPanel rootPanel = RootLayoutPanel.get();

		SimplePanel greenLateralPanel = new SimplePanel();
		greenLateralPanel.setStyleName("lateral_panel");
		SimplePanel borderpanel = new SimplePanel();
		borderpanel.setStyleName("header_panel");
		myDockLayoutPanel.addWest(greenLateralPanel, 10);
		myDockLayoutPanel.addWest(borderpanel, 2);

		final OMSVGSVGElement svg = images.logo_svg().getSvg();
		SVGImage myImage = new SVGImage(svg)
		{
			protected void onAttach() {
	    		OMSVGRect viewBox = svg.getViewBox().getBaseVal();
				if (viewBox.getWidth() == 0 || viewBox.getHeight() == 0) {
					OMSVGRect bbox = svg.getBBox();
					bbox.assignTo(viewBox);
				}				
				svg.getStyle().setWidth(300, Unit.PX);
				svg.getStyle().setHeight(130, Unit.PX);
				super.onAttach();
	    	}
		};	
												
		myImage.setStyleName("logo");
		SimplePanel logoPanel = new SimplePanel();
		logoPanel.add(myImage);
		logoPanel.setStyleName("header_panel");			
		myDockLayoutPanel.addNorth(logoPanel, 10.7);		
		
		SimplePanel footerPanel = new SimplePanel();
		footerPanel.setStyleName("header_panel");
		myDockLayoutPanel.addSouth(footerPanel, 4);
		rootPanel.add(myDockLayoutPanel);
		myDockLayoutPanel.add(floorPlan.getCanvas());
						
	//Temporal hack to the configuration until we decide about it	
		brokerIp = com.google.gwt.user.client.Window.Location.getHostName();
		init();
//		String brokerIp = Cookies.getCookie("broker_ip");
//		if (brokerIp==null)
//		{
//			showConfigurationDialog();
//		}


	}
	
	private void showConfigurationDialog() {
		final ConfigurationDialog dialog = new ConfigurationDialog(
				new OkCancelDialogCallback() {

					@Override
					public void okButtonClick(String text) {
						final long DURATION = 1000 * 60 * 60 * 24 * 14;
						// duration remembering login. 2 weeks in this example.
						Date expires = new Date(System.currentTimeMillis()
								+ DURATION);
						Cookies.setCookie("broker_ip", text, expires, null,
								"/", false);						
						GWT.log("on ok ip:" + text);
						brokerIp = Cookies.getCookie("broker_ip");
						init();

					}

					@Override
					public void cancelButtonClick() {
						// TODO Auto-generated method stub
					}
				});

		dialog.setPopupPosition(400, 400);
		dialog.show();		
	}

	public void init() {							
		//TODO: check the format of the brokerip
		EnvironmentController.getInstance().setBrokerIp(brokerIp);
		// retrieve data from the restapi
		EnvironmentController.getInstance().prepareRestResource();
		EnvironmentController.getInstance().retrieve();
		EnvironmentController.getInstance().initStomp();

	}
}
