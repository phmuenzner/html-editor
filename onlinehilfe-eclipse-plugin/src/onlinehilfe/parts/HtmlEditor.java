package onlinehilfe.parts;

import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Base64;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.io.FileUtils;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.ui.di.Focus;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.browser.ProgressAdapter;
import org.eclipse.swt.browser.ProgressEvent;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IPathEditorInput;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.EditorPart;
import org.eclipse.ui.preferences.ScopedPreferenceStore;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;

import onlinehilfe.CurrentPropertiesStore;
import onlinehilfe.MessageBoxUtil;
import onlinehilfe.contentbuilder.FilesUtil;
import onlinehilfe.navigation.NavigationMetadata;
import onlinehilfe.navigation.NavigationMetadataController;
import onlinehilfe.navigator.OnlinehilfeNavigatorContentProvider;
import onlinehilfe.preferences.PreferenceConstants;

public class HtmlEditor extends EditorPart {
	
	private static final Bundle BUNDLE = FrameworkUtil.getBundle(HtmlEditor.class);
	private static final ILog LOGGER = Platform.getLog(BUNDLE);
	
	private Browser browser;
	private String currentPath;
	private IPreferenceStore preferenceStore = new ScopedPreferenceStore(InstanceScope.INSTANCE, PreferenceConstants.SCOPED_PREF_INSTANCE);
	private boolean changedContentEditorStateIsDirty = false;

	private IFileEditorInput fileInput;

	@Override
	public void init(IEditorSite site, IEditorInput input) throws PartInitException {
		LOGGER.info("init: " + site + ", " + input);

		setSite(site);
		setInput(input);

		IPathEditorInput pInput = (IPathEditorInput) input;
		fileInput = (IFileEditorInput) input;
		
		currentPath = pInput.getPath().toPortableString();
		setPartName(fileInput.getFile().getParent().getName());
		

		CurrentPropertiesStore.getInstance().setProject(fileInput.getFile().getProject());
		CurrentPropertiesStore.getInstance().setParent(fileInput.getFile().getParent());

		LOGGER.info(input + " -- " + pInput.getPath().toPortableString());
	}

	@PostConstruct
	public void createPartControl(Composite parent) {
		LOGGER.info("createPartControl: " + parent.toString());
		
		parent.setLayout(new BorderLayout());
		
		Label label = new Label(parent, SWT.NONE);
		label.setLayoutData(new BorderLayout.BorderData(BorderLayout.NORTH));
		
		try {
			NavigationMetadata navigationMetadata =  NavigationMetadataController.getInstance().getNavigationMetadataByIFolder((IFolder)(fileInput.getFile().getParent()));
			if (navigationMetadata!=null) {
				label.setText(navigationMetadata.getTitle());
				setPartName(navigationMetadata.getTitle());
			}
			
		} catch (Exception e) {
			LOGGER.error("Fehler in Jodit-Browser-Editor", e); //TODO raus oder Logger
			label.setText(fileInput.getFile().getParent().getName());
		}
		
	    label.setFont(new Font( label.getDisplay(), new FontData("Arial", 14, SWT.NONE)));
	    label.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));
	    browser = new Browser(parent, SWT.NONE);
		browser.setLayoutData(new BorderLayout.BorderData(BorderLayout.CENTER));

		try {
			URI uri = new URI("http://localhost:" + preferenceStore.getString(PreferenceConstants.JETTY_PORT)
					+ "/static/jodit/htmleditor/editor.html");
			
			//URI uri = new URI("https://javascript.info/mouse-drag-and-drop");
			
			
			// URI uriWithLoadParam = new URI(uri.getScheme(),
			// uri.getAuthority(), uri.getPath(), "path=" + currentPath,
			// uri.getFragment());
			// String uriString = uriWithLoadParam.toString();
			browser.setUrl(uri.toString());
		} catch (URISyntaxException e) {
			LOGGER.error("Fehler in Jodit-Browser-Editor", e);
		}

		// content ins Script senden

		try {
			char[] buffer = new char[1024];
			int size = -1;
			StringBuilder sb = new StringBuilder();
			try (Reader reader = new InputStreamReader(fileInput.getFile().getContents(true), FilesUtil.CHARSET)) {
				while ((size = reader.read(buffer)) != -1) {
					sb.append(buffer, 0, size);
				}
			}
			String b64ontent = Base64.getEncoder().encodeToString(sb.toString().getBytes(FilesUtil.CHARSET));

			browser.addProgressListener(new ProgressAdapter() {
				@Override
				public void completed(ProgressEvent event) {
					super.completed(event);
					browser.evaluate("evaluateContentOnLoad(b64DecodeUnicode('" + b64ontent + "'));");
				}
			});

		} catch (Exception e) {
			MessageBoxUtil.displayError("Beim Laden des Contents für \"%s\" ist ein Feher aufgetreten. Das tritt meistens auf, wenn der zu ladende Content nicht bzw. nicht mehr existiert.", e, fileInput.getFile().getParent().getName());
			LOGGER.error("Fehler in Jodit-Browser-Editor", e);
		}

		BrowserEventListener browserEventListener = new BrowserEventListener();
		browser.addMouseListener(browserEventListener);
		browser.addKeyListener(browserEventListener);
		
		
		browser.getDisplay().addListener(SWT.MouseDown, new Listener() {
			@Override
			public void handleEvent(Event event) {
				LOGGER.info(">> MouseDown " + event); 
			}
		});
		
		
	}

	@Focus
	public void setFocus() {
		browser.setFocus();
	}

	/**
	 * This method is kept for E3 compatiblity. You can remove it if you do not
	 * mix E3 and E4 code. <br/>
	 * With E4 code you will set directly the selection in ESelectionService and
	 * you do not receive a ISelection
	 * 
	 * @param s
	 *            the selection received from JFace (E3 mode)
	 */
	@Inject
	@Optional
	public void setSelection(@Named(IServiceConstants.ACTIVE_SELECTION) ISelection s) {
		if (s == null || s.isEmpty())
			return;

		if (s instanceof IStructuredSelection) {
			IStructuredSelection iss = (IStructuredSelection) s;
			if (iss.size() == 1)
				setSelection(iss.getFirstElement());
			else
				setSelection(iss.toArray());
		}
	}

	/**
	 * This method manages the selection of your current object. In this example
	 * we listen to a single Object (even the ISelection already captured in E3
	 * mode). <br/>
	 * You should change the parameter type of your received Object to manage
	 * your specific selection
	 * 
	 * @param o
	 *            : the current object received
	 */
	@Inject
	@Optional
	public void setSelection(@Named(IServiceConstants.ACTIVE_SELECTION) Object o) {

		// Remove the 2 following lines in pure E4 mode, keep them in mixed mode
		if (o instanceof ISelection) // Already captured
			return;
	}

	/**
	 * This method manages the multiple selection of your current objects. <br/>
	 * You should change the parameter type of your array of Objects to manage
	 * your specific selection
	 * 
	 * @param o
	 *            : the current array of objects received in case of multiple
	 *            selection
	 */
	@Inject
	@Optional
	public void setSelection(@Named(IServiceConstants.ACTIVE_SELECTION) Object[] selectedObjects) {
		LOGGER.info("setSelection: " + Arrays.toString(selectedObjects));

	}

	@Override
	public void doSave(IProgressMonitor arg0) {
		LOGGER.info("doSave: " + arg0);

		String content = String.valueOf(browser.evaluate("return evaluateContentToSave();"));

		try {

			fileInput.getFile().setContents(new ByteArrayInputStream(content.getBytes(FilesUtil.CHARSET)),
					IResource.FORCE, new NullProgressMonitor());

			fileInput.getFile().refreshLocal(IResource.DEPTH_INFINITE, new NullProgressMonitor());

		} catch (Exception e) {
			LOGGER.error("Fehler in Jodit-Browser-Editor", e);
		}

		checkChanged();

	}

	@Override
	public boolean isDirty() {
		//LOGGER.info("isDirty() --> " + changedContentEditorStateIsDirty);
		return changedContentEditorStateIsDirty;
	}

	@Override
	public void doSaveAs() {
		LOGGER.info("doSaveAs");
	}

	@Override
	public boolean isSaveAsAllowed() {
		return false;
	}

	private void checkChanged() {
		boolean changedContentInJS = Boolean.TRUE.equals(browser.evaluate("return hasChangedContent();"));
		if (changedContentInJS != changedContentEditorStateIsDirty) {
			changedContentEditorStateIsDirty = changedContentInJS;
			firePropertyChange(IEditorPart.PROP_DIRTY);
		}
	}

	class BrowserEventListener implements MouseListener, KeyListener {

		@Override
		public void keyPressed(KeyEvent arg0) {
			// checkChanged();
		}

		@Override
		public void keyReleased(KeyEvent arg0) {
			checkChanged();
		}

		@Override
		public void mouseDoubleClick(MouseEvent arg0) {
			// checkChanged();
		}

		@Override
		public void mouseDown(MouseEvent arg0) {
			// checkChanged();
		}

		@Override
		public void mouseUp(MouseEvent arg0) {
			checkChanged();
		}

	}

}
