package onlinehilfe.preferences;

import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;

import onlinehilfe.jetty.JettyServerHandler;

/**
 * Constant definitions for plug-in preferences
 */
public class PreferenceConstants {

	private static final Bundle BUNDLE = FrameworkUtil.getBundle(PreferenceConstants.class);
	
	public static final String SCOPED_PREF_INSTANCE = BUNDLE.getSymbolicName();
	
	public static final String JETTY_PORT = "jettyPort";
	public static final String HHC_PATH = "hhcPathPreference";
}
