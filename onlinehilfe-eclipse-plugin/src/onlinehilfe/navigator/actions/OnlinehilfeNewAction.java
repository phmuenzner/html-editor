package onlinehilfe.navigator.actions;

import java.util.Properties;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.util.OpenStrategy;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.navigator.CommonNavigator;

import onlinehilfe.dialogs.NewContentWizard;
import onlinehilfe.dialogs.RenameContentWizard;
import onlinehilfe.navigation.NavigationMetadataController;
import onlinehilfe.navigator.IOnlinehilfeElement;
import onlinehilfe.navigator.IOnlinehilfeElement.ElementType;

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
		NewContentWizard wizard = new NewContentWizard(returnProperties);
		WizardDialog wizardDialog = new WizardDialog(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), wizard);
		
		
		if (wizardDialog.open() == Window.OK) {
			String newName = returnProperties.getProperty(RenameContentWizard.PROPERTIES_KEY_TITLE);
			
			try {
				IOnlinehilfeElement toShow = NavigationMetadataController.getInstance().newElement(onlinehilfeElement, newName);
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
