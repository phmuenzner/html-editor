package onlinehilfe;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.ui.PlatformUI;

public final class MessageBoxUtil {
	private MessageBoxUtil() {}
		
	public static final void displayMessage(String message, Object... args) {
		message = String.format(message, args);
		
		MessageBox box = new MessageBox(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell());
		box.setText("Information");
		box.setMessage(message);
		box.open();
	}
	
	private static final void displayErrorInternal(String message, Throwable t, Object... args) {
		message = String.format(message, args);
		
		if( t != null) {
			StringWriter stacktrace = new StringWriter();
			t.printStackTrace(new PrintWriter(stacktrace));
			
			message += "\n\nStacktrace:\n" + ( (stacktrace.toString().length() > 4096) ? stacktrace.toString().substring(0, 4096)+ "\n...": stacktrace.toString() );
		}
		
		MessageBox box = new MessageBox(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), SWT.OK | SWT.ICON_ERROR | SWT.APPLICATION_MODAL);
		box.setText("Fehler!");
		box.setMessage(message);			
		box.open();
	}
	
	public static final void displayError(String message, Throwable t, Object... args) {
		displayErrorInternal(message, t, args);
	}
	
	public static final void displayError(String message, Object... args) {
		displayErrorInternal(message, null, args);
	}
	
	public static final boolean confirmDialog(String title, String confirmRequestText, Object... args) {
		confirmRequestText = String.format(confirmRequestText, args);
		return MessageDialog.openConfirm(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), title, confirmRequestText);
	} 
	
}
