package onlinehilfe.navigator.actions;

import java.util.Properties;

import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.navigator.CommonNavigator;

import onlinehilfe.dialogs.RenameContentWizard;
import onlinehilfe.navigation.NavigationMetadataController;
import onlinehilfe.navigator.IOnlinehilfeElement;
import onlinehilfe.navigator.IOnlinehilfeElement.ElementType;

public class OnlinehilfeRenameAction extends AbstractOnlinehilfeSelectionListenerAction {
	static {
		id = "onlinehilfe.OnlinehilfeRenameAction";
		labelText = "&Umbenennen";
		tooltipText = "Umbenennen des Contents";
	}
		
	public OnlinehilfeRenameAction(IWorkbenchPage page) {
		super(page);
	}
	
	public void runForEachSelectedOnlineHilfeElement(IOnlinehilfeElement onlinehilfeElement) {
		
		if (openContentIsSaved(onlinehilfeElement)) {
		
			Properties returnProperties = new Properties();
			returnProperties.setProperty(RenameContentWizard.PROPERTIES_KEY_TITLE, onlinehilfeElement.getElementName());
			RenameContentWizard wizard = new RenameContentWizard(returnProperties);
			WizardDialog wizardDialog = new WizardDialog(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), wizard);
			
			if (wizardDialog.open() == Window.OK) {
				boolean wasOpen = contentIsOpendInEditor(onlinehilfeElement);
				if (openContentIsClosed(onlinehilfeElement)) {
					
					IOnlinehilfeElement newOnlinehilfeElement = NavigationMetadataController.getInstance().renameElement(onlinehilfeElement, returnProperties.getProperty(RenameContentWizard.PROPERTIES_KEY_TITLE));
					
					IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
					IWorkbenchPart view = window.getPartService().getActivePart();
					if (view instanceof CommonNavigator) {
						CommonNavigator nav = (CommonNavigator) view;
						
						//nav.getCommonViewer().add(newOnlinehilfeElement.getParentOnlinehilfeElement(), newOnlinehilfeElement);
						//nav.getCommonViewer().remove(onlinehilfeElement);
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
