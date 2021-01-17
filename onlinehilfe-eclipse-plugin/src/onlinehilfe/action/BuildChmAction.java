package onlinehilfe.action;

import java.io.File;
import java.nio.charset.Charset;
import java.util.Properties;

import org.apache.commons.io.IOUtils;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.actions.ActionDelegate;
import org.eclipse.ui.preferences.ScopedPreferenceStore;

import onlinehilfe.CurrentPropertiesStore;
import onlinehilfe.contentbuilder.ContentDocumentBuilder;
import onlinehilfe.contentbuilder.FilesUtil;
import onlinehilfe.contentbuilder.MetadataIdFilenameCreator;
import onlinehilfe.dialogs.MessageBoxUtil;
import onlinehilfe.preferences.PreferenceConstants;

public class BuildChmAction extends ActionDelegate implements IWorkbenchWindowActionDelegate {

	private IPreferenceStore preferenceStore = new ScopedPreferenceStore(InstanceScope.INSTANCE, PreferenceConstants.SCOPED_PREF_INSTANCE);
		
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
			//Prüfen ob Pfad zu HTML Help workshop installiert ist.
			File hhcExe = new File(preferenceStore.getString(PreferenceConstants.HHC_PATH));
			if (!hhcExe.exists()) {
				MessageBoxUtil.displayError("Der Pfad zur hhc.exe existiert nicht. Die hhc.exe ist teil der Anwendung \"Microsoft HTML Help Workshop\".\n\nDer Pfad muss in den Eclipse-Preferences [ HTML Editor Preferences > HHC Path ] konfiguriert werden.\n\nSollte die Anwendung \"Microsoft HTML Help Workshop\" nicht installiert sein, können Sie die Anwendung von Microsoft downloaden.\n\nLink: https://docs.microsoft.com/en-us/previous-versions/windows/desktop/htmlhelp/microsoft-html-help-downloads");
				return;
			}
			
			//Prüfen ob Projekt geöffnet und selektiert wurde
			if (CurrentPropertiesStore.getInstance().getProject()==null || CurrentPropertiesStore.getInstance().getProject().getLocation() == null) {
				MessageBoxUtil.displayError("Sie müssen ein Projekt geöffnet haben!");
				return;
			}
			
			Properties projectProperties = FilesUtil.readProjectProperties(CurrentPropertiesStore.getInstance().getProject());
			
			//Bereite Contentbuilder vor
			ContentDocumentBuilder documentBuilder = new ContentDocumentBuilder(new MetadataIdFilenameCreator(), "chm.", "_target-chm", "html");
			
			//Ermittle Arbeits- und Zielverzeichnis
			IPath innerTargetLocation = documentBuilder.getTargetLocation();
			File targetLocation = innerTargetLocation.toFile().getParentFile();
			
			//Arbeitsverzeichnis löschen
			FilesUtil.deleteDirectory(innerTargetLocation.toFile());
			
			//Erstelle HTML-Content
			documentBuilder.setCharsetToc(Charset.forName("Cp1252"));
			documentBuilder.build();

			//Zielausgabedatei definieren und löschen wenn existierend
			File outputFileDst = new File(targetLocation, "output.chm");
			if (outputFileDst.exists() && !outputFileDst.delete()) {
				MessageBoxUtil.displayError("Die Zielausgabedatei ist blockiert. Wenn diese noch geöffnet ist, müssen sie diese schießen!");
				return;
			}
			
			//toc-File umbenennen zu toc.hhc
			File tocFile = new File(innerTargetLocation.toFile(), "_toc.html");
			File tocNewFileDst = new File(innerTargetLocation.toFile(), "toc.hhc");
			if (tocFile.exists()) {
				tocFile.renameTo(tocNewFileDst);
			}
			
			//filelist-File umbenennen zu project.hhp
			File filelistFile = new File(innerTargetLocation.toFile(), "_filelist.html");
			File filelistFileDst = new File(innerTargetLocation.toFile(), "project.hhp");
			if (filelistFile.exists()) {
				filelistFile.renameTo(filelistFileDst);
			}
						
			//CHM-Datei aus hhp-Projekt erstellen
			
			Process process = Runtime.getRuntime().exec(hhcExe.getAbsolutePath() + " project.hhp", null, innerTargetLocation.toFile());
			
			String processOut = IOUtils.toString(process.getInputStream());
			String processErr = IOUtils.toString(process.getErrorStream());
			
			if (processErr != null && !processErr.isEmpty()) {
				throw new Exception("Fehler aus HHC: \n\nOut: " + processOut + "\n\nErr: " + processErr);
			} 
			
			//Ausgabe verschieben
			File outputFile = new File(innerTargetLocation.toFile(), "output.chm");
			if (outputFile.exists()) {
				outputFile.renameTo(outputFileDst);
			}
			
			//TODO: das muss noch umgebaut werden hier wird die contentcollection.html in die mapping.csv (oder wie auch immer genannt) umbenannt.
			String contentcollectionNewName = projectProperties.getProperty("build.chm.contentcollection.targetfilename");
			if (contentcollectionNewName != null && !contentcollectionNewName.isBlank()) {
				File contentcollectionFile = new File(innerTargetLocation.toFile(), "_contentcollection.html");
				File contentcollectionFileDst = new File(targetLocation, contentcollectionNewName);
				if (contentcollectionFile.exists()) {
					contentcollectionFile.renameTo(contentcollectionFileDst);
				}	
			}
			
			
			//Arbeitsverzeichnis löschen
			FilesUtil.deleteDirectory(innerTargetLocation.toFile());
			
			MessageBoxUtil.displayMessage("Die Ausleitung als Chm wurde abgeschlossen und in \""+targetLocation.getCanonicalFile()+"\" abgelegt.\n\nOut: "+ processOut);
		} catch (Exception e) {
			e.printStackTrace();
			MessageBoxUtil.displayError("Fehler!", e);
		}

	}

	
	
}
