package onlinehilfe.action;

import java.io.IOException;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.action.IAction;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.actions.ActionDelegate;

import onlinehilfe.MessageBoxUtil;
import onlinehilfe.contentbuilder.ContentDocumentBuilder;
import onlinehilfe.contentbuilder.FilesUtil;
import onlinehilfe.contentbuilder.MetadataEscapedTitleFilenameCreator;

public class BuildHtmlAction extends ActionDelegate implements IWorkbenchWindowActionDelegate {

	@Override
	public void init(IWorkbenchWindow arg0) {

	}

	@Override
	public void dispose() {
		super.dispose();
	}

	@Override
	public void run(IAction action) {
		try {
			
			ContentDocumentBuilder documentBuilder = new ContentDocumentBuilder(new MetadataEscapedTitleFilenameCreator(), "html.", "_target-html");
			IPath targetLocation = documentBuilder.getTargetLocation();
			
			//Ausgabeverzeichnis löschen
			FilesUtil.deleteDirectory(targetLocation.toFile());
			
			documentBuilder.build();
			
			MessageBoxUtil.displayMessage("Die Ausleitung als Html wurde abgeschlossen und in \""+targetLocation.toFile().getCanonicalFile()+"\" abgelegt.\n");
		} catch (CoreException|IOException e) {
			e.printStackTrace();
			MessageBoxUtil.displayError("Fehler!", e);
		}

	}

	
	
}
