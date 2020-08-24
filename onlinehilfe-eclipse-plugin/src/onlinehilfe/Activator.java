package onlinehilfe;

import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import onlinehilfe.jetty.JettyServerHandler;
import onlinehilfe.navigation.NavigationMetadataController;

public class Activator extends AbstractUIPlugin implements BundleActivator {

	public void start(BundleContext context) throws Exception {
		super.start(context);
		System.out.println("Activator start");

		JettyServerHandler.getInstance().start();
		
		CurrentPropertiesStore.getInstance().addPropertiesEventListener(NavigationMetadataController.getInstance());
		ResourcesPlugin.getWorkspace().addResourceChangeListener(NavigationMetadataController.getInstance(), IResourceChangeEvent.POST_CHANGE);
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		super.stop(context);
		System.out.println("Activator stop");

		CurrentPropertiesStore.getInstance().removePropertiesEventListener(NavigationMetadataController.getInstance());
		
		NavigationMetadataController.getInstance().terminate();
		
		JettyServerHandler.getInstance().stop();
	}
	
}
