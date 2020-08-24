package onlinehilfe.navigator.actions;

import java.io.IOException;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.IAction;
import org.eclipse.ui.navigator.CommonNavigator;

import onlinehilfe.navigation.NavigationMetadataController;
import onlinehilfe.navigator.IOnlinehilfeElement;
import onlinehilfe.navigator.IOnlinehilfeElement.ElementType;


public class OnlinehilfeNavigatorViewActionOut extends AbstractOnlinehilfeNavigatorViewAction {
	
	@Override
	public boolean checkActive() {
		return NavigationMetadataController.getInstance().canMoveItemHorizontal(selectedOnlineHilfeElement, false);
	}
	
	@Override
	public void runAction(IAction action) {
		try {
			boolean hasMoved = NavigationMetadataController.getInstance().moveItemHorizontal(selectedOnlineHilfeElement, false);
			
			if (hasMoved) {
				IOnlinehilfeElement parent = selectedOnlineHilfeElement.getParentOnlinehilfeElement();
				if (parent !=null) {
					IOnlinehilfeElement toRefresh = parent.getParentOnlinehilfeElement();
					if (toRefresh==null || toRefresh.getElementType() == ElementType.NAVROOT) {
						refresh(null);
					} else {
						refresh(toRefresh);
					}
				}
			}
			
		} catch (CoreException | IOException e) {
			e.printStackTrace();
		}
	}
}
