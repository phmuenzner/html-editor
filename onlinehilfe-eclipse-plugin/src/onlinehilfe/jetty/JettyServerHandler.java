package onlinehilfe.jetty;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.commons.io.FileUtils;
import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.Platform;
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
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;

import onlinehilfe.contentbuilder.FilesUtil;
import onlinehilfe.preferences.PreferenceConstants;

public class JettyServerHandler {
	
	private static final Bundle BUNDLE = FrameworkUtil.getBundle(JettyServerHandler.class);
	private static final ILog LOGGER = Platform.getLog(JettyServerHandler.class);
	
	private IPreferenceStore preferenceStore = new ScopedPreferenceStore(InstanceScope.INSTANCE, PreferenceConstants.SCOPED_PREF_INSTANCE);

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

		Runnable runnable = new Runnable() {
			@Override
			public void run() {
				LOGGER.info("JettyServerHandler starting ...");
				server = new Server(
						new InetSocketAddress("localhost", preferenceStore.getInt(PreferenceConstants.JETTY_PORT)));
				try {
					HttpConnectionFactory connectionFactory = server.getConnectors()[0].getConnectionFactory(HttpConnectionFactory.class);
					// notwendig ab Jetty 9
					connectionFactory.getHttpConfiguration().setMultiPartFormDataCompliance(MultiPartFormDataCompliance.RFC7578);
					HandlerList handlers = new HandlerList();

					staticWebFolder = uniqueInstance.createStaticJoditWebFolder();
					
					ResourceHandler resourceHandler = new ResourceHandler();
					resourceHandler.setDirectoriesListed(true);
					resourceHandler.setResourceBase(staticWebFolder.getAbsolutePath());

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
			}
		};
		new Thread(runnable).start();
	}

	public void stop() throws Exception {
		LOGGER.info("JettyServerHandler stop");
		server.stop();
		server = null;
		
		FilesUtil.deleteDirectory(staticWebFolder);
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
		
	public File createStaticJoditWebFolder() throws IOException {
		File tempFolder = new File(System.getProperty("java.io.tmpdir"));
		if (!tempFolder.exists()) {
			tempFolder.mkdirs();	
		}
		
		File tempWebFolder = new File(tempFolder, BUNDLE.getSymbolicName()+"-static-web-" + System.currentTimeMillis());
		tempWebFolder.mkdirs();
		
		LOGGER.info("create static web folder in Temp: " + tempWebFolder.getAbsolutePath());
		
		try (ZipInputStream zis = new ZipInputStream(this.getClass().getResourceAsStream("/web.zip"))) {
			byte[]buffer = new byte[1024];
			ZipEntry zipEntry = zis.getNextEntry();
			while (zipEntry != null) {
	            File newFile = newFileForStaticWebFolder(tempWebFolder, zipEntry);
	            if (zipEntry.isDirectory()) {
	            	newFile.mkdirs();
	            } else {
	            	if (!newFile.getParentFile().exists()) {
	            		newFile.getParentFile().mkdirs();
	            	}
	            	FileOutputStream fos = new FileOutputStream(newFile);
		            int len;
		            while ((len = zis.read(buffer)) > 0) {
		                fos.write(buffer, 0, len);
		            }
		            fos.close();	
	            }
	            
	            zipEntry = zis.getNextEntry();
	        }
	        zis.closeEntry();
		}
		
		
		return tempWebFolder;
		
	}
	
	public static File newFileForStaticWebFolder(File destinationDir, ZipEntry zipEntry) throws IOException {
        File destFile = new File(destinationDir, zipEntry.getName());

        String destDirPath = destinationDir.getCanonicalPath();
        String destFilePath = destFile.getCanonicalPath();

        if (!destFilePath.startsWith(destDirPath + File.separator)) {
            throw new IOException("Entry is outside of the target dir: " + zipEntry.getName());
        }

        return destFile;
    }
}
