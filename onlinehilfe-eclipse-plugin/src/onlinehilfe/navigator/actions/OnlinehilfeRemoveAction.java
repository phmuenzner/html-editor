package onlinehilfe.navigator.actions;

import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.navigator.CommonNavigator;

import onlinehilfe.MessageBoxUtil;
import onlinehilfe.navigation.NavigationMetadataController;
import onlinehilfe.navigator.IOnlinehilfeElement;

public class OnlinehilfeRemoveAction extends AbstractOnlinehilfeSelectionListenerAction {
	static {
		id = "onlinehilfe.OnlinehilfeRemoveAction";
		labelText = "&Löschen";
		tooltipText = "Löscht Content";
	}
		
	public OnlinehilfeRemoveAction(IWorkbenchPage page) {
		super(page);		
	}
			
	public void runForEachSelectedOnlineHilfeElement(IOnlinehilfeElement onlinehilfeElement) {				
		boolean result = MessageBoxUtil.confirmDialog("Content löschen?", "Wollen Sie den Content mit Namen \"%s\" wirklich löschen?", onlinehilfeElement.getElementName());
		
		if (openContentIsClosed(onlinehilfeElement)) {
		
			IOnlinehilfeElement parent = onlinehilfeElement.getParentOnlinehilfeElement();
			if (result) {
				NavigationMetadataController.getInstance().removeElement(onlinehilfeElement);
				
				IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
				IWorkbenchPart view = window.getPartService().getActivePart();
				if (view instanceof CommonNavigator) {
					CommonNavigator nav = (CommonNavigator) view;
					nav.getCommonViewer().refresh(parent);
				}	
			}	
		}
	}
}
