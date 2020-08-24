package onlinehilfe.contentbuilder;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Properties;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;

import onlinehilfe.CurrentPropertiesStore;

public class ContentDocumentBuilder {
		
	private final IPath projectLocation;
	private final FilenameCreator filenameCreator;
	private final String templatePrefix;
	
	private final IPath targetLocation;
	
	private Charset charsetCover = FilesUtil.CHARSET;
	private Charset charsetToc = FilesUtil.CHARSET;
	private Charset charsetFilelist = FilesUtil.CHARSET;
	private Charset charsetContent = FilesUtil.CHARSET;
	private Charset charsetContentcollection = FilesUtil.CHARSET;
	
	public ContentDocumentBuilder(FilenameCreator filenameCreator, String templatePrefix, String... targetSegemnts) {
		projectLocation = CurrentPropertiesStore.getInstance().getProject().getLocation();
		this.filenameCreator = filenameCreator;
		this.templatePrefix = templatePrefix;
			
		IPath targetLocation = projectLocation;
		for(String targetSegemnt: targetSegemnts) {
			targetLocation = targetLocation.append(targetSegemnt);
		}
		this.targetLocation = targetLocation;
	}
	
	public IPath getTargetLocation() {
		return targetLocation;
	}
	
	public void setCharsetCover(Charset charsetCover) {
		this.charsetCover = charsetCover;
	}

	public void setCharsetToc(Charset charsetToc) {
		this.charsetToc = charsetToc;
	}

	public void setCharsetFilelist(Charset charsetFilelist) {
		this.charsetFilelist = charsetFilelist;
	}

	public void setCharsetContent(Charset charsetContent) {
		this.charsetContent = charsetContent;
	}

	public void setCharsetContentcollection(Charset charsetContentcollection) {
		this.charsetContentcollection = charsetContentcollection;
	}
	
	public void build() throws CoreException, IOException {
		File projectDir = projectLocation.toFile();
		ContentMetadata contentMetadata = workWithFiles(projectDir, "", null);

		ContentDocumentWriter contentDocumentWriter = new ContentDocumentWriter(projectDir, targetLocation.toFile(), templatePrefix, filenameCreator);
		contentDocumentWriter.buildCover(charsetCover);
		contentDocumentWriter.buildToc(contentMetadata, charsetToc);
		contentDocumentWriter.buildFilelist(contentMetadata, charsetFilelist);
		contentDocumentWriter.buildContent(contentMetadata, charsetContent);
		contentDocumentWriter.buildContentcollection(contentMetadata, charsetContentcollection);
	}
	
	private ContentMetadata workWithFiles(File dir, String navName, ContentMetadata parentMetadata) {
		ContentMetadata contentMetadata = new ContentMetadata();
		for (File file : dir.listFiles()) {
			if (file.isDirectory()) {
				if (!file.getName().startsWith("_") || !file.getName().startsWith("."))
					workWithFiles(file, navName + "/" + file.getName(), contentMetadata);
			} else {
				if ("content.htm".equalsIgnoreCase(file.getName())) {
					if (parentMetadata != null)
						parentMetadata.addSubContent(contentMetadata);

					fillContentMetadata(dir, file, navName, contentMetadata);
				}

			}
		}
		return contentMetadata;
	}

	private void fillContentMetadata(File dir, File content, String navName, ContentMetadata contentMetadata) {
		System.out.println(
				"NavName: " + navName + "(" + dir.getName() + ", " + content.getName() + ", " + dir.getName() + ")");

		try {
			Properties metaProperties = FilesUtil.readMetaProperties(dir);
			contentMetadata.setTitle(metaProperties.getProperty("contentTitle"));
			contentMetadata.setOrder(Integer.valueOf(metaProperties.getProperty("contentOrder", "-1")));
			contentMetadata.setId(metaProperties.getProperty("contentId"));
			contentMetadata.setContentFile(content); //TODO File raus machen		
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
}
