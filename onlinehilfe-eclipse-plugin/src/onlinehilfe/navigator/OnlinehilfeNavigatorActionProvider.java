package onlinehilfe.navigator;

import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.Platform;
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
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;

import onlinehilfe.navigator.actions.OnlineHilfeContextActionGroup;

public class OnlinehilfeNavigatorActionProvider extends SynchronizationActionProvider {
	
	private static final Bundle BUNDLE = FrameworkUtil.getBundle(OnlinehilfeNavigatorActionProvider.class);
	private static final ILog LOGGER = Platform.getLog(OnlinehilfeNavigatorActionProvider.class);
	
	private OnlineHilfeContextActionGroup contextActions;
	
	public OnlinehilfeNavigatorActionProvider() {

	}
		
	@Override
	public void init(ICommonActionExtensionSite aSite) {
		LOGGER.info("OnlinehilfeNavigatorActionProvider call init("+aSite+")");
		
		super.init(aSite);		
		initOpenActions();				
	}	
	
	private  void initOpenActions() {
		ICommonViewerSite viewSite = getActionSite().getViewSite();
		if (viewSite instanceof ICommonViewerWorkbenchSite) {
			ICommonViewerWorkbenchSite commonViewerWorkbenchSite = (ICommonViewerWorkbenchSite) viewSite;
			
			LOGGER.info("commonViewerWorkbenchSite infos: " + commonViewerWorkbenchSite.getPage() + ", " + commonViewerWorkbenchSite.getSelectionProvider());
			
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
