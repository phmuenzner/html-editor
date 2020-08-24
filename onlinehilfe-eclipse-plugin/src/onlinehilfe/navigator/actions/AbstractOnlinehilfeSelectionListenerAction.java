package onlinehilfe.navigator.actions;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.util.OpenStrategy;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.SelectionListenerAction;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.internal.registry.EditorDescriptor;
import org.eclipse.ui.part.EditorPart;

import onlinehilfe.navigator.IOnlinehilfeElement;
import onlinehilfe.navigator.IOnlinehilfeElement.ElementType;

public abstract class AbstractOnlinehilfeSelectionListenerAction extends SelectionListenerAction {
	protected static String id;
	protected static String labelText;
	protected static String tooltipText;
	
	protected IWorkbenchPage workbenchPage;
		
	protected AbstractOnlinehilfeSelectionListenerAction(IWorkbenchPage page) {
		super(labelText);
		
		if (page == null) {
			throw new IllegalArgumentException();
		}
		this.workbenchPage = page
				;
		setText(labelText);
		setToolTipText(tooltipText);
		setId(id);
	}
	
	protected void openContentInEditorPart(IOnlinehilfeElement onlinehilfeElement) {
		if (onlinehilfeElement!=null && onlinehilfeElement.getElementType() == ElementType.NAVPOINT) {
			
			IFile file = onlinehilfeElement.getContentFile();
		
			try {
				boolean activate = OpenStrategy.activateOnOpen();
				IDE.openEditor(workbenchPage, file, activate);			
			} catch (PartInitException e) {
				e.printStackTrace();
			}
		}
		
	}
	
	protected boolean contentIsOpendInEditor(IOnlinehilfeElement onlinehilfeElement) {
		IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
		
		IEditorPart editorPart = getOpenEditorPart(page, onlinehilfeElement);
		return editorPart != null;
	}
	
	protected boolean openContentIsSaved(IOnlinehilfeElement onlinehilfeElement) {
		IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
		
		IEditorPart editorPart = getOpenEditorPart(page, onlinehilfeElement);
		if (editorPart != null) {
			return page.saveEditor(editorPart, true);		
		}
		
		return true;
	}
	
	protected boolean openContentIsClosed(IOnlinehilfeElement onlinehilfeElement) {
		IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
		
		IEditorPart editorPart = getOpenEditorPart(page, onlinehilfeElement);
		if (editorPart != null) {
			return page.closeEditor(editorPart, true);	
		}
		
		return true;
	}
	
	private IEditorPart getOpenEditorPart(IWorkbenchPage page, IOnlinehilfeElement onlinehilfeElement) {
		if (onlinehilfeElement!=null && onlinehilfeElement.getElementType() == ElementType.NAVPOINT) {
			
			IFile file = onlinehilfeElement.getContentFile();
		
			IEditorReference[] editorRefs = page.getEditorReferences();
			
			for (IEditorReference editorRef : editorRefs) {
				IEditorPart editorPart = editorRef.getEditor(false);
				if (editorPart==null) {
					continue;
				}
				
				if (editorPart.getEditorInput() instanceof IFileEditorInput) {
					IFileEditorInput editorInput = (IFileEditorInput) editorPart.getEditorInput();
					if (editorInput.getFile().equals(onlinehilfeElement.getContentFile())) {
						
						//gefunden
						return editorPart;
					}
				}
			}
			
		}
		
		return null;
	}
	
	
	
	@Override
	public void run() {
		for (Object nonresource : getSelectedNonResources()) {
			if (nonresource instanceof IOnlinehilfeElement ) {
				IOnlinehilfeElement onlinehilfeElement = (IOnlinehilfeElement) nonresource;
				
				runForEachSelectedOnlineHilfeElement(onlinehilfeElement);
			}
		}
	}

	protected abstract void runForEachSelectedOnlineHilfeElement(IOnlinehilfeElement onlinehilfeElement);

	
	@Override
	protected boolean updateSelection(IStructuredSelection selection) {
		return super.updateSelection(selection)
				&& (selection.getFirstElement() instanceof IOnlinehilfeElement);
	}
	
}
