package onlinehilfe.navigator.actions;

import java.io.IOException;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.IAction;

import onlinehilfe.navigation.NavigationMetadataController;


public class OnlinehilfeNavigatorViewActionDown extends AbstractOnlinehilfeNavigatorViewAction {
	
	@Override
	public boolean checkActive() {
		return NavigationMetadataController.getInstance().canMoveItemVertical(selectedOnlineHilfeElement, false);
	}
	
	@Override
	public void runAction(IAction action) {
		try {
			boolean hasMoved = NavigationMetadataController.getInstance().moveItemVertical(selectedOnlineHilfeElement, false);
		
			if (hasMoved) {
				refresh(selectedOnlineHilfeElement.getParentOnlinehilfeElement());
			}
		} catch (CoreException | IOException e) {
			e.printStackTrace();
		}
	}
}
