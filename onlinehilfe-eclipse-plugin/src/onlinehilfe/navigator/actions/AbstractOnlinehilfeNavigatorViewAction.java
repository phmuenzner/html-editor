package onlinehilfe.navigator.actions;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.actions.ActionDelegate;
import org.eclipse.ui.navigator.CommonNavigator;

import onlinehilfe.navigator.IOnlinehilfeElement;

public abstract class AbstractOnlinehilfeNavigatorViewAction extends ActionDelegate implements IViewActionDelegate {
	protected IOnlinehilfeElement selectedOnlineHilfeElement;
	
	protected IViewPart view;
	
	public void init(IViewPart view) {
		this.view = view;
	}
	
	@Override
	public final void selectionChanged(IAction action, ISelection selection) {
		if (selection instanceof IStructuredSelection) {
			IStructuredSelection structuredSelection= (IStructuredSelection) selection;
			if (structuredSelection.getFirstElement() instanceof IOnlinehilfeElement) {
				
				//System.out.println("AbstractOnlinehilfeNavigatorViewAction selectionChanged " + selection);
				selectedOnlineHilfeElement = (IOnlinehilfeElement) structuredSelection.getFirstElement();
				action.setEnabled(checkActive());
				return;
			}
		}
		
		//System.out.println("AbstractOnlinehilfeNavigatorViewAction selectionChanged null");
		selectedOnlineHilfeElement = null;
		action.setEnabled(false);
	}
	
	protected void refresh(IOnlinehilfeElement refrehElement) {
		
		final IOnlinehilfeElement currentSelectedOnlineHilfeElement = selectedOnlineHilfeElement;
		
		if (view instanceof CommonNavigator) {
			CommonNavigator nav = (CommonNavigator) view;
			if (refrehElement == null) {
				nav.getCommonViewer().refresh();
			} else {
				nav.getCommonViewer().refresh(refrehElement);
			}
			
			
			nav.selectReveal(new StructuredSelection(currentSelectedOnlineHilfeElement));
		}
		
		
	}
	
	public final void run(IAction action) {
		if (selectedOnlineHilfeElement==null) {
			return;
		}
		
		runAction(action);
	}
	
	public abstract void runAction(IAction action);
	
	public abstract boolean checkActive();
		
}
