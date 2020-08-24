package onlinehilfe.navigator.actions;

import java.util.Arrays;
import java.util.stream.StreamSupport;

import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.IOpenListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.OpenEvent;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IWorkbenchSite;
import org.eclipse.ui.actions.ActionGroup;
import org.eclipse.ui.actions.OpenFileAction;
import org.eclipse.ui.actions.SelectionListenerAction;
import org.eclipse.ui.navigator.ICommonActionConstants;
import org.eclipse.ui.navigator.ICommonViewerWorkbenchSite;

import onlinehilfe.navigator.IOnlinehilfeElement;
import onlinehilfe.navigator.IOnlinehilfeElement.ElementType;

public class OnlineHilfeContextActionGroup extends ActionGroup {
	
	private final StructuredViewer structuredViewer;
	private final ICommonViewerWorkbenchSite commonViewerWorkbenchSite;
	private OnlinehilfeOpenAction openAction;
	private OnlinehilfeNewAction newAction;
	private OnlinehilfeRenameAction renameAction;
	private OnlinehilfeRemoveAction removeAction;
	
	public OnlineHilfeContextActionGroup(StructuredViewer structuredViewer, ICommonViewerWorkbenchSite commonViewerWorkbenchSite) {
		this.structuredViewer = structuredViewer;
		this.commonViewerWorkbenchSite = commonViewerWorkbenchSite;
		makeActions();
		
		addOpenEvent();
	}
	
	protected void makeActions() {
		IWorkbenchSite ws = getSite();
		if (ws != null) {
			openAction = new OnlinehilfeOpenAction(ws.getPage());
			newAction = new OnlinehilfeNewAction(ws.getPage());
			renameAction = new OnlinehilfeRenameAction(ws.getPage());
			removeAction = new OnlinehilfeRemoveAction(ws.getPage());
		}
	}
	
	private void addOpenEvent() { //standard doubleclick event auf dem Element
		structuredViewer.addOpenListener(openAction);
	}
	
	private IWorkbenchSite getSite() {
		return commonViewerWorkbenchSite.getSite();
	}
	
	public OnlinehilfeOpenAction getOpenAction() {
		return openAction;
	}
	
	@Override
	public void fillActionBars(IActionBars actionBars) {
		super.fillActionBars(actionBars);
		
		actionBars.setGlobalActionHandler(ICommonActionConstants.OPEN, openAction);
	}
	
	public void fillContextMenu(IMenuManager menu) {
		ISelection selection = getSite().getSelectionProvider().getSelection();
		if (selection instanceof IStructuredSelection) {
			IStructuredSelection structuredSelection = (IStructuredSelection) selection;
				addNew(menu, structuredSelection);
				menu.add(new Separator());
				addOpen(menu, structuredSelection);
				addRename(menu, structuredSelection);
				addRemove(menu, structuredSelection);
		}
	}
	
	private void addOpen(IMenuManager menu, IStructuredSelection selection) {
		addAction(menu, selection, openAction, ElementType.NAVPOINT);
	}
	
	private void addNew(IMenuManager menu, IStructuredSelection selection) {
		addAction(menu, selection, newAction);
	}
	
	private void addRename(IMenuManager menu, IStructuredSelection selection) {
		addAction(menu, selection, renameAction, ElementType.NAVPOINT);
	}
	
	private void addRemove(IMenuManager menu, IStructuredSelection selection) {
		addAction(menu, selection, removeAction, ElementType.NAVPOINT);
	}
	
	private void addAction(IMenuManager menu, IStructuredSelection selection, SelectionListenerAction action, ElementType... elementTypes) {
		if (selection == null || selection.size() < 1)
			return;
		
		Object[] elements = selection.toArray();
		
		boolean allNavPoints = true;
		for (Object element : elements) {
			if (element instanceof IOnlinehilfeElement) {
				IOnlinehilfeElement onlinehilfeElement = (IOnlinehilfeElement) element;
				if (elementTypes.length>0 && Arrays.asList(elementTypes).stream().noneMatch(f -> (f == onlinehilfeElement.getElementType()))) {
					allNavPoints = false;
				}
			} else {
				allNavPoints = false; 
			}
		}
		
		if (allNavPoints) {
			if (action != null) {
				action.selectionChanged(selection);
				menu.add(action);
			}	
		}
	}	
}
