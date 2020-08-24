package onlinehilfe.navigator.actions;

import org.eclipse.jface.viewers.IOpenListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.OpenEvent;
import org.eclipse.ui.IWorkbenchPage;

import onlinehilfe.navigator.IOnlinehilfeElement;

public class OnlinehilfeOpenAction extends AbstractOnlinehilfeSelectionListenerAction implements IOpenListener {
	static {
		id = "onlinehilfe.OnlinehilfeOpenAction";
		labelText = "Ö&ffnen";
		tooltipText = "Öffnet Content";
	}
		
	public OnlinehilfeOpenAction(IWorkbenchPage page) {
		super(page);
	}
		
	public void runForEachSelectedOnlineHilfeElement(IOnlinehilfeElement onlinehilfeElement) {
		openContentInEditorPart(onlinehilfeElement);	
	}
	
	@Override
	public void open(OpenEvent event) {
		if (event.getSelection() instanceof IStructuredSelection) {
			IStructuredSelection structuredSelection = (IStructuredSelection) event.getSelection();
			if (structuredSelection.getFirstElement() instanceof IOnlinehilfeElement ) {
				IOnlinehilfeElement onlinehilfeElement = (IOnlinehilfeElement) structuredSelection.getFirstElement();
				
				openContentInEditorPart(onlinehilfeElement);
			}
			
		}
	}
}
