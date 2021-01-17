package onlinehilfe.navigator.actions;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.stream.Collectors;

import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.navigator.CommonNavigator;

import onlinehilfe.contentbuilder.FilesUtil;
import onlinehilfe.dialogs.MessageBoxUtil;
import onlinehilfe.dialogs.NewContentWizard;
import onlinehilfe.dialogs.RenameContentWizard;
import onlinehilfe.navigation.NavigationMetadataController;
import onlinehilfe.navigator.IOnlinehilfeElement;

public class OnlinehilfeRenameAction extends AbstractOnlinehilfeSelectionListenerAction {
	static {
		id = "onlinehilfe.OnlinehilfeRenameAction";
		labelText = "&Umbenennen/Konfigurieren";
		tooltipText = "Umbenennen des Contents und Anpassung der Konfiguration";
	}
		
	public OnlinehilfeRenameAction(IWorkbenchPage page) {
		super(page);
	}
	
	public void runForEachSelectedOnlineHilfeElement(IOnlinehilfeElement onlinehilfeElement) {
		if (openContentIsSaved(onlinehilfeElement)) {
			Properties returnProperties = new Properties();
			returnProperties.setProperty(RenameContentWizard.PROPERTIES_KEY_TITLE, onlinehilfeElement.getElementName());
			
			try {
				Properties metaDataProperties = FilesUtil.readMetaProperties(onlinehilfeElement.getIFolder());
				Map<Object, Object> customFieldEntries = metaDataProperties.entrySet().stream()
						.filter(f -> ((String)(f.getKey())).startsWith(String.format(NewContentWizard.CUSTOM_FIELD_PREFIX_FORMAT, "")))
						.collect(Collectors.toMap(Entry::getKey, Entry::getValue));
				returnProperties.putAll(customFieldEntries);
			} catch (Exception e) {
				MessageBoxUtil.displayError("Customfields konnten nicht eingelesen werden", e);
			}
			
			Properties customFieldConfigurationProperties = new Properties();
			try {
				Properties projectDataProperties = FilesUtil.readProjectProperties(onlinehilfeElement.getProject());
				customFieldConfigurationProperties.putAll(projectDataProperties);
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			RenameContentWizard wizard = new RenameContentWizard(customFieldConfigurationProperties, returnProperties);
			WizardDialog wizardDialog = new WizardDialog(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), wizard);
			
			if (wizardDialog.open() == Window.OK) {
				boolean wasOpen = contentIsOpendInEditor(onlinehilfeElement);
				if (openContentIsClosed(onlinehilfeElement)) {
					
					String newName = returnProperties.getProperty(RenameContentWizard.PROPERTIES_KEY_TITLE);
					
					Map<Object, Object> customFieldEntries = returnProperties.entrySet().stream()
							.filter(f -> ((String)(f.getKey())).startsWith(String.format(NewContentWizard.CUSTOM_FIELD_PREFIX_FORMAT, "")))
							.collect(Collectors.toMap(Entry::getKey, Entry::getValue));
					
					IOnlinehilfeElement newOnlinehilfeElement = NavigationMetadataController.getInstance().renameElement(onlinehilfeElement, newName, customFieldEntries);
					IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
					IWorkbenchPart view = window.getPartService().getActivePart();
					if (view instanceof CommonNavigator) {
						CommonNavigator nav = (CommonNavigator) view;
						nav.getCommonViewer().refresh(newOnlinehilfeElement.getParentOnlinehilfeElement());
					}
					
					if (wasOpen) {
						openContentInEditorPart(newOnlinehilfeElement);
					}
				}
				
			}
		}
	}
}
