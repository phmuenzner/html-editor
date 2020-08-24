package onlinehilfe.jetty;

import java.net.InetSocketAddress;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.MultiPartFormDataCompliance;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.util.resource.Resource;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.ui.preferences.ScopedPreferenceStore;

import onlinehilfe.preferences.PreferenceConstants;

public class JettyServerHandler {

	private IPreferenceStore preferenceStore = new ScopedPreferenceStore(InstanceScope.INSTANCE, "onlinehilfe");

	private static JettyServerHandler uniqueInstance = null;

	private Server server = null;

	public static JettyServerHandler getInstance() {
		if (uniqueInstance == null) {
			generateInstance();
		}
		return uniqueInstance;
	}

	private static synchronized void generateInstance() {
		if (uniqueInstance == null) {
			uniqueInstance = new JettyServerHandler();
		}
	}

	public void start() throws Exception {
		System.out.println("JettyServerHandler start");

		Runnable runnable = new Runnable() {
			@Override
			public void run() {
				System.out.println("JettyServerHandler starting ...");
				server = new Server(
						new InetSocketAddress("localhost", preferenceStore.getInt(PreferenceConstants.JETTY_PORT)));
				try {
					HttpConnectionFactory connectionFactory = server.getConnectors()[0].getConnectionFactory(HttpConnectionFactory.class);
					// notwendig ab Jetty 9
					connectionFactory.getHttpConfiguration().setMultiPartFormDataCompliance(MultiPartFormDataCompliance.RFC7578);
					HandlerList handlers = new HandlerList();

					ResourceHandler resourceHandler = new ResourceHandler();
					resourceHandler.setDirectoriesListed(true);
					resourceHandler.setResourceBase(preferenceStore.getString(PreferenceConstants.JODIT_PATH));
					//resourceHandler.setBaseResource(Resource.newClassPathResource("/web"));
					// "C:/Projects/onlinehilfe/onlinehilfe/webpart"
					

					ContextHandler resourceContextHandler = new ContextHandler();
					resourceContextHandler.setContextPath("/static");
					resourceContextHandler.setHandler(resourceHandler);

					ContentHttpRequestHandler contentHandler = new ContentHttpRequestHandler();

					handlers.setHandlers(new Handler[] { resourceContextHandler, contentHandler });
					server.setHandler(handlers);

					server.start();
					System.out.println("JettyServerHandler started!");

					// server.join();

				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		};
		new Thread(runnable).start();
	}

	public void stop() throws Exception {
		System.out.println("JettyServerHandler stop");
		server.stop();
		server = null;
	}

	public IPropertyChangeListener createPropertyChangeListener() {
		return new PropertyChangeListener();
	}

	private class PropertyChangeListener implements IPropertyChangeListener {

		@Override
		public void propertyChange(PropertyChangeEvent arg0) {
			if (PreferenceConstants.JODIT_PATH.equals(arg0.getProperty())
					|| PreferenceConstants.JETTY_PORT.equals(arg0.getProperty())) {
				try {
					stop();
					start();
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}
		}
	}
}
