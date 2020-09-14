package onlinehilfe.preferences;

import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.preference.DirectoryFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.FileFieldEditor;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.preferences.ScopedPreferenceStore;

/**
 * This class represents a preference page that is contributed to the
 * Preferences dialog. By subclassing <samp>FieldEditorPreferencePage</samp>, we
 * can use the field support built into JFace that allows us to create a page
 * that is small and knows how to save, restore and apply itself.
 * <p>
 * This page is used to modify preferences only. They are stored in the
 * preference store that belongs to the main plug-in class. That way,
 * preferences can be accessed directly via the preference store.
 */

public class HtmlEditorPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

	public HtmlEditorPreferencePage() {
		super(GRID);
		setDescription("Einstellungen für Onlinehilfe");
	}

	/**
	 * Creates the field editors. Field editors are abstractions of the common
	 * GUI blocks needed to manipulate various types of preferences. Each field
	 * editor knows how to save and restore itself.
	 */
	public void createFieldEditors() {
		addField(new DirectoryFieldEditor(PreferenceConstants.JODIT_PATH, "Jodit Path:", getFieldEditorParent()));
		addField(new IntegerFieldEditor(PreferenceConstants.JETTY_PORT, "Local Jetty Port:", getFieldEditorParent()));
		addField(new FileFieldEditor(PreferenceConstants.HHC_PATH, "HHC Path:", getFieldEditorParent()));
	}

	@Override
	public void init(IWorkbench workbench) {
		// second parameter is typically the plug-in id
		setPreferenceStore(new ScopedPreferenceStore(InstanceScope.INSTANCE, PreferenceConstants.SCOPED_PREF_INSTANCE));
	}

}