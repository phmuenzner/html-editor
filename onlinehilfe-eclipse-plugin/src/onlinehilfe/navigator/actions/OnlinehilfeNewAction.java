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
import onlinehilfe.dialogs.NewContentWizard;
import onlinehilfe.dialogs.RenameContentWizard;
import onlinehilfe.navigation.NavigationMetadataController;
import onlinehilfe.navigator.IOnlinehilfeElement;

public class OnlinehilfeNewAction extends AbstractOnlinehilfeSelectionListenerAction {
	static {
		id = "onlinehilfe.OnlinehilfeNewAction";
		labelText = "&Neu";
		tooltipText = "Erstellt Neuen Content";
	}
	
	public OnlinehilfeNewAction(IWorkbenchPage page) {
		super(page);
	}
			
	public void runForEachSelectedOnlineHilfeElement(IOnlinehilfeElement onlinehilfeElement) {
		Properties returnProperties = new Properties();
		
		Properties customFieldConfigurationProperties = new Properties();
		try {
			Properties projectDataProperties = FilesUtil.readProjectProperties(onlinehilfeElement.getProject());
			customFieldConfigurationProperties.putAll(projectDataProperties);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		NewContentWizard wizard = new NewContentWizard(customFieldConfigurationProperties, returnProperties);
		WizardDialog wizardDialog = new WizardDialog(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), wizard);
		
		if (wizardDialog.open() == Window.OK) {
			String newName = returnProperties.getProperty(RenameContentWizard.PROPERTIES_KEY_TITLE);
			
			Map<Object, Object> customFieldEntries = returnProperties.entrySet().stream()
					.filter(f -> ((String)(f.getKey())).startsWith(String.format(NewContentWizard.CUSTOM_FIELD_PREFIX_FORMAT, "")))
					.collect(Collectors.toMap(Entry::getKey, Entry::getValue));
			
			try {
				IOnlinehilfeElement toShow = NavigationMetadataController.getInstance().newElement(onlinehilfeElement, newName, customFieldEntries);
				IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
				IWorkbenchPart view = window.getPartService().getActivePart();
				if (view instanceof CommonNavigator) {
					CommonNavigator nav = (CommonNavigator) view;
					nav.getCommonViewer().refresh(onlinehilfeElement);
				}
			
				if (toShow != null) {
					openContentInEditorPart(toShow);
				}
				
			} catch (Exception e) {
				e.printStackTrace();
			}
		
        }
		
	
	}
}
