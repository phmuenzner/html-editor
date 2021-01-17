package onlinehilfe.contentbuilder;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Properties;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;

public final class FilesUtil {
	private FilesUtil() {}
	
	public static final int FILENAME_MAX_LENGTH = 35;
	
	public static final Charset CHARSET = StandardCharsets.UTF_8;
	public static final String CHARSET_STRING = CHARSET.toString();

	public static final String META_PROPERTIES_FILENAME = "meta.properties";
	public static final String CONTENT_FILENAME = "content.htm";
	public static final String PROJECT_PROPERTIES_FILENAME = "project.properties";
	
	/**
	 * Kopiert alle Dateien von einen Ordner in einen anderen Ordner, inklusive Unterordner
	 * @param from
	 * @param to
	 * @throws IOException
	 */
	public static void copyFilesInDirectory(File from, File to) throws IOException {
		if(!to.exists()) {
			to.mkdirs();	
		}
		for (File file : from.listFiles()) {
			if (file.isDirectory()) {
				copyFilesInDirectory(file, new File(to.getAbsolutePath() + "/" + file.getName()));
			} else {
				File n = new File(to.getAbsolutePath() + "/" + file.getName());
				Files.copy(file.toPath(), n.toPath(), StandardCopyOption.REPLACE_EXISTING);
			}
		}
	}
		
	public static Properties readProjectProperties(IFolder folder) throws IOException, CoreException {
		return readProjectProperties(folder.getProject());
	}

	public static Properties readProjectProperties(IProject project) throws IOException, CoreException {
		IFile projectPropertiesFile = project.getFile(PROJECT_PROPERTIES_FILENAME);
		Properties projectProperties = new Properties();
		if (projectPropertiesFile.exists()) {
			try (InputStream in = projectPropertiesFile.getContents(true)) {
				projectProperties.load(in);
			}
		}
		return projectProperties;
	}

	
	public static Properties readMetaProperties(IFolder folder) throws IOException, CoreException {
		//System.out.println("readMetaProperties("+folder+")");
		IFile metaPropertiesFile = folder.getFile(META_PROPERTIES_FILENAME);
		Properties metaProperties = new Properties();
		
		if (metaPropertiesFile.exists()) {
			try (InputStream in = metaPropertiesFile.getContents(true)) {
				metaProperties.load(in);
			}
		}
		return metaProperties;
	}
	
	public static Properties readMetaProperties(File folder) throws IOException {
		//System.out.println("readMetaProperties("+folder+")");
		File metaPropertiesFile = new File(folder, META_PROPERTIES_FILENAME);
		Properties metaProperties = new Properties();
		
		if (metaPropertiesFile.exists()) {
			try (InputStream in = new FileInputStream(metaPropertiesFile)) {
				metaProperties.load(in);
			}
		}
		return metaProperties;
	}
	
	public static void writeMetaProperties(IFolder folder, Properties metaProperties) throws IOException, CoreException {
		IFile metaPropertiesFile = folder.getFile(META_PROPERTIES_FILENAME);
	
		try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
			PropertiesStoreWithoutDateUtil.store(metaProperties, out);		
			if (metaPropertiesFile.exists()) {
				metaPropertiesFile.setContents(new ByteArrayInputStream(out.toByteArray()), IResource.FORCE, new NullProgressMonitor());
			} else {
				metaPropertiesFile.create(new ByteArrayInputStream(out.toByteArray()), IResource.FORCE, new NullProgressMonitor());
			}
		}
	}
	
	public static boolean deleteDirectory(File directoryToBeDeleted) {
	    File[] allContents = directoryToBeDeleted.listFiles();
	    if (allContents != null) {
	        for (File file : allContents) {
	            deleteDirectory(file);
	        }
	    }
	    return directoryToBeDeleted.delete();
	}
	
	public static String buildFilenameFromTitle(final String title) {
		return MetadataEscapedTitleFilenameCreator.buildFilenameFromTitle(title, FILENAME_MAX_LENGTH);
	}

}
