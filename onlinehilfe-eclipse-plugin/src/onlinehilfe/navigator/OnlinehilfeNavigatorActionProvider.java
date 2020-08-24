package onlinehilfe.navigator;

import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.team.ui.mapping.SynchronizationActionProvider;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.actions.ActionContext;
import org.eclipse.ui.navigator.ICommonActionConstants;
import org.eclipse.ui.navigator.ICommonActionExtensionSite;
import org.eclipse.ui.navigator.ICommonViewerSite;
import org.eclipse.ui.navigator.ICommonViewerWorkbenchSite;

import onlinehilfe.navigator.actions.OnlineHilfeContextActionGroup;

public class OnlinehilfeNavigatorActionProvider extends SynchronizationActionProvider {
	
	private OnlineHilfeContextActionGroup contextActions;
	
	public OnlinehilfeNavigatorActionProvider() {

	}
		
	@Override
	public void init(ICommonActionExtensionSite aSite) {
		System.out.println("OnlinehilfeNavigatorActionProvider call init("+aSite+")");
		
		super.init(aSite);		
		initOpenActions();				
	}	
	
	private  void initOpenActions() {
		ICommonViewerSite viewSite = getActionSite().getViewSite();
		if (viewSite instanceof ICommonViewerWorkbenchSite) {
			ICommonViewerWorkbenchSite commonViewerWorkbenchSite = (ICommonViewerWorkbenchSite) viewSite;
			
			System.out.println("commonViewerWorkbenchSite infos: " + commonViewerWorkbenchSite.getPage() + ", " + commonViewerWorkbenchSite.getSelectionProvider());
			
			contextActions = new OnlineHilfeContextActionGroup(getActionSite().getStructuredViewer(), commonViewerWorkbenchSite);			
		}
	}
	
	@Override
	public void fillContextMenu(IMenuManager menu) {
		super.fillContextMenu(menu);
		
		if (contextActions != null) {
			contextActions.fillContextMenu(menu);
		}
	}
	
	@Override
	public void fillActionBars(IActionBars actionBars) {
		super.fillActionBars(actionBars);
		if (contextActions != null)  {
			contextActions.fillActionBars(actionBars);
		}
		
		
	}

	@Override
	public void updateActionBars() {
		super.updateActionBars();
		if (contextActions != null) contextActions.updateActionBars();
	}

	@Override
	public void setContext(ActionContext context) {
		super.setContext(context);
		if (contextActions != null) contextActions.setContext(context);
	}
}
