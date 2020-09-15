package onlinehilfe.preferences;

import java.io.File;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.ui.preferences.ScopedPreferenceStore;

import onlinehilfe.jetty.JettyServerHandler;

/**
 * Class used to initialize default preference values.
 */
public class PreferenceInitializer extends AbstractPreferenceInitializer {

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer#
	 * initializeDefaultPreferences()
	 */
	public void initializeDefaultPreferences() {
		ScopedPreferenceStore scopedPreferenceStore = new ScopedPreferenceStore(InstanceScope.INSTANCE, PreferenceConstants.SCOPED_PREF_INSTANCE);
		scopedPreferenceStore.setDefault(PreferenceConstants.JETTY_PORT, "8291");
		scopedPreferenceStore.setDefault(PreferenceConstants.HHC_PATH, new File("C:/Program Files (x86)/HTML Help Workshop/hhc.exe").getAbsolutePath());

		scopedPreferenceStore
				.addPropertyChangeListener(JettyServerHandler.getInstance().createPropertyChangeListener());
	}

}
