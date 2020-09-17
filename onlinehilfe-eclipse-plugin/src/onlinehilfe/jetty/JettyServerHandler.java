package onlinehilfe.jetty;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URL;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.ObjectUtils;
import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.MultiPartFormDataCompliance;
import org.eclipse.jetty.server.ResourceService;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.util.resource.Resource;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.ui.preferences.ScopedPreferenceStore;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;

import onlinehilfe.contentbuilder.FilesUtil;
import onlinehilfe.preferences.PreferenceConstants;

public class JettyServerHandler {

	private static final Bundle BUNDLE = FrameworkUtil.getBundle(JettyServerHandler.class);
	private static final ILog LOGGER = Platform.getLog(JettyServerHandler.class);

	private IPreferenceStore preferenceStore = new ScopedPreferenceStore(InstanceScope.INSTANCE,
			PreferenceConstants.SCOPED_PREF_INSTANCE);

	private static JettyServerHandler uniqueInstance = null;

	private Server server = null;
	private File staticWebFolder = null;

	private JettyServerHandler() {
		// TODO Auto-generated constructor stub
	}

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
		LOGGER.info("JettyServerHandler start");

		new Thread(() -> {
			LOGGER.info("JettyServerHandler starting ...");
			server = new Server(
					new InetSocketAddress("localhost", preferenceStore.getInt(PreferenceConstants.JETTY_PORT)));
			try {
				HttpConnectionFactory connectionFactory = server.getConnectors()[0]
						.getConnectionFactory(HttpConnectionFactory.class);
				// notwendig ab Jetty 9
				connectionFactory.getHttpConfiguration()
						.setMultiPartFormDataCompliance(MultiPartFormDataCompliance.RFC7578);
				HandlerList handlers = new HandlerList();

				ClassLoader cl = Thread.currentThread().getContextClassLoader();
				URL f = cl.getResource("jodit/server.js");
				if (f == null) {
					throw new RuntimeException("Unable to find plugin jar");
				}
				URI webRootUri = f.toURI().resolve("../").normalize();
				Resource baseResource = Resource.newResource(webRootUri);

				ResourceHandler resourceHandler = new ResourceHandler();
				resourceHandler.setDirectoriesListed(true);
				resourceHandler.setBaseResource(baseResource);

				ContextHandler resourceContextHandler = new ContextHandler();
				resourceContextHandler.setContextPath("/static");
				resourceContextHandler.setHandler(resourceHandler);

				ContentHttpRequestHandler contentHandler = new ContentHttpRequestHandler();

				handlers.setHandlers(new Handler[] { resourceContextHandler, contentHandler });
				server.setHandler(handlers);

				server.start();
				LOGGER.info("JettyServerHandler started!");

				// server.join();

			} catch (Exception e) {
				e.printStackTrace();
			}
		}).start();
	}

	public void stop() throws Exception {
		LOGGER.info("JettyServerHandler stop");
		server.stop();
		server = null;
	}

	public IPropertyChangeListener createPropertyChangeListener() {
		return new PropertyChangeListener();
	}

	private class PropertyChangeListener implements IPropertyChangeListener {

		@Override
		public void propertyChange(PropertyChangeEvent arg0) {
			if (PreferenceConstants.JETTY_PORT.equals(arg0.getProperty())) {
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
