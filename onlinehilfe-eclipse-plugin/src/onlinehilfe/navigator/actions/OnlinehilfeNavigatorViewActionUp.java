package onlinehilfe.navigator.actions;

import java.io.IOException;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.IAction;
import org.eclipse.ui.navigator.CommonNavigator;

import onlinehilfe.navigation.NavigationMetadataController;


public class OnlinehilfeNavigatorViewActionUp extends AbstractOnlinehilfeNavigatorViewAction {
	
	@Override
	public boolean checkActive() {
		return NavigationMetadataController.getInstance().canMoveItemVertical(selectedOnlineHilfeElement, true);
	}
	
	@Override
	public void runAction(IAction action) {
		try {
			boolean hasMoved = NavigationMetadataController.getInstance().moveItemVertical(selectedOnlineHilfeElement, true);
			
			if (hasMoved) {
				refresh(selectedOnlineHilfeElement.getParentOnlinehilfeElement());
			}
			
		} catch (CoreException | IOException e) {
			e.printStackTrace();
		}
	}
}
