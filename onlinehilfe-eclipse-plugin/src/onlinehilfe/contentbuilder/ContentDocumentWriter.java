package onlinehilfe.contentbuilder;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.Platform;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;

import onlinehilfe.navigator.OnlinehilfeNavigatorContentProvider;

public class ContentDocumentWriter {
	
	private static final Bundle BUNDLE = FrameworkUtil.getBundle(ContentDocumentWriter.class);
	private static final ILog LOGGER = Platform.getLog(BUNDLE);
	
	private static final VelocityEngine ve = new VelocityEngine();
	
	private static final String VM_TEMPLATE_COVER = "cover.vm";
	private static final String VM_TEMPLATE_TOC = "toc.vm";
	private static final String VM_TEMPLATE_FILELIST = "filelist.vm";
	private static final String VM_TEMPLATE_CONTENT = "content.vm"; //content als einzeldocumente
	private static final String VM_TEMPLATE_CONTENTCOLLECTION = "contentcollection.vm"; //alle content elemente als gesamtdocument
	
	private final File targetDir;
	private final String templatePrefix;
	private final File templateDir;
	
	private final FilenameCreator filenameCreator;
		
	private Properties props = new Properties();
			
	public ContentDocumentWriter(File projectDir, File targetDir, String templatePrefix, FilenameCreator filenameCreator) throws IOException {
		this.targetDir = targetDir;
		this.targetDir.mkdirs();
		this.templatePrefix = templatePrefix;
		this.templateDir = new File(projectDir, "_templates");
		this.filenameCreator = filenameCreator;
		
		props.put("file.resource.loader.path", templateDir.getCanonicalPath());
		props.put("input.encoding", FilesUtil.CHARSET_STRING);
		props.put("output.encoding", FilesUtil.CHARSET_STRING);
		ve.init(props);
		
		//bilder und Styles übertragen
		FilesUtil.copyFilesInDirectory(new File(projectDir, "_images"), new File(targetDir, "_images"));
		FilesUtil.copyFilesInDirectory(new File(projectDir, "_styles"), new File(targetDir, "_styles"));
	}
	
	public void buildCover(Charset targetCharset) throws IOException {
		
		if (!(new File(templateDir, templatePrefix+VM_TEMPLATE_COVER)).exists()) {
			LOGGER.info("Skip. Kein Cover-Template.");
			return;
		}
		
		LOGGER.info("Erstelle Cover-Document.");
			
		Map<String, Object> context = new HashMap<>();
		Template contenTemplate = ve.getTemplate(templatePrefix+VM_TEMPLATE_COVER, FilesUtil.CHARSET_STRING);
		writeContentToFile(buildOutputFileName("_cover"), contenTemplate, context, targetCharset);
	}

	public void buildFilelist(ContentMetadata contentMetadata, Charset targetCharset) throws IOException {
		
		if (!(new File(templateDir, templatePrefix+VM_TEMPLATE_FILELIST)).exists()) {
			LOGGER.info("Skip. Kein Filelist-Template.");
			return;
		}
		
		LOGGER.info("Erstelle FileList-Document.");
		
		List<String> filelist = buildFilelistInternal(contentMetadata);
		
		for (String string : filelist) {
			LOGGER.info(string);
		}
		
		Map<String, Object> context = new HashMap<>();
		context.put("filelist", filelist);
		
		Template contenTemplate = ve.getTemplate(templatePrefix+VM_TEMPLATE_FILELIST, FilesUtil.CHARSET_STRING);
		writeContentToFile(buildOutputFileName("_filelist"), contenTemplate, context, targetCharset);
	}
	
	private List<String> buildFilelistInternal(ContentMetadata contentMetadata) throws IOException {
		List<String> filelist = new ArrayList<String>();
		
		if (contentMetadata.getTitle()!=null && contentMetadata.getContentFile()!=null) {
			filelist.add(buildOutputFileName(contentMetadata).getName());
		}
		
		if (contentMetadata.getSubContent() !=null) {
			for (ContentMetadata subContent : contentMetadata.getSubContent()) {
				filelist.addAll(buildFilelistInternal(subContent));
			}
		}
		
		return filelist;
	}
	
	public void buildToc(ContentMetadata contentMetadata, Charset targetCharset) throws IOException {
		
		if (!(new File(templateDir, templatePrefix+VM_TEMPLATE_TOC)).exists()) {
			LOGGER.info("Skip. Kein ToC-Template.");
			return;
		}
		
		LOGGER.info("Erstelle ToC-Document.");

		//ToC --> Table Of Contents, ich will nicht jedes mal Inhaltsverzeichnis schreiben
		
		TocEntry toc = buildTocInternal(contentMetadata);
		
		Map<String, Object> context = new HashMap<>();
		context.put("toc", toc);
		
		Template contenTemplate = ve.getTemplate(templatePrefix+VM_TEMPLATE_TOC, FilesUtil.CHARSET_STRING);
		writeContentToFile(buildOutputFileName("_toc"), contenTemplate, context, targetCharset);
	}
	
	private TocEntry buildTocInternal(ContentMetadata contentMetadata) throws IOException {
		TocEntry tocEntry = new TocEntry();
		tocEntry.setId(contentMetadata.getId());
		tocEntry.setTitle(contentMetadata.getTitle());
		tocEntry.setFilename(buildOutputFileName(contentMetadata).getName());
		
		if (contentMetadata.getSubContent() !=null) {
			for (ContentMetadata subContent : contentMetadata.getSubContent()) {
				TocEntry tocSubEntry = buildTocInternal(subContent);
				tocEntry.addSubEntries(tocSubEntry);
			}
		}
		return tocEntry;
	}
	
	public void buildContent(ContentMetadata contentMetadata, Charset targetCharset) throws IOException {
		LOGGER.info("Erstelle Content-Dokumente: " + contentMetadata.getTitle());
		
		buildContent(contentMetadata, null, null, null, null, 0, targetCharset);
	}
		
	public void buildContentcollection(ContentMetadata contentMetadata, Charset targetCharset) throws IOException {
		if (!(new File(templateDir, templatePrefix+VM_TEMPLATE_CONTENTCOLLECTION)).exists()) {
			LOGGER.info("Skip. Kein ContentCollection-Template.");
			return;
		}
		
		LOGGER.info("Erstelle ContentCollection-Dokument als Sammeldokument der Content-Dokumente: " + contentMetadata.getTitle());
		
		List<ContentMetadata> collectionList = new ArrayList<ContentMetadata>();
		buildContent(contentMetadata, null, null, null, collectionList, 0, targetCharset);
		
		Map<String, Object> context = new HashMap<>();
		context.put("contents", collectionList );
		
		Template contenTemplate = ve.getTemplate(templatePrefix+VM_TEMPLATE_CONTENTCOLLECTION, FilesUtil.CHARSET_STRING);
		writeContentToFile(buildOutputFileName("_contentcollection"), contenTemplate, context, targetCharset);
	}
	
	private void initContentMetadata (ContentMetadata contentMetadata) {
		if (contentMetadata!=null) {
			contentMetadata.setFilename(buildOutputFileName(contentMetadata).getName());
		}
	}
	
	private void buildContent(ContentMetadata contentMetadata, ContentMetadata parent, ContentMetadata prev, ContentMetadata next, List<ContentMetadata> collectionList, final int navLevel, Charset targetCharset) throws IOException {
		
		int currentNavLevel = navLevel;
		
		// herausfiltern des Root-Containers
		if (contentMetadata.getTitle()!=null && contentMetadata.getContentFile()!=null) {
		
			initContentMetadata(contentMetadata);
			contentMetadata.setNavLevel(++currentNavLevel);
			
			
			if (collectionList==null) {
				//wenn ich das template hier einmal brauche und es nicht da ist, dann sind die nachfolgenden schritte auch egal...
				if (!(new File(templateDir, templatePrefix+VM_TEMPLATE_CONTENT)).exists()) {
					LOGGER.info("Skip. Kein Content-Template.");
					return;
				}
				
				LOGGER.info("Erstelle Content-Dokument: " + contentMetadata.getTitle());
				
				Map<String, Object> context = new HashMap<>();
				context.put("content", contentMetadata);
				context.put("contentParent", parent);
				context.put("contentPrev", prev);
				
				initContentMetadata(next); //minimal Vorbefüllung weil ich u.U Daten brauche
				context.put("contentNext", next);
				
				for (ContentMetadata subContent : contentMetadata.getSubContent()) {
					initContentMetadata(subContent);
				}
				context.put("contentSubcontent", contentMetadata.getSubContent());
							
				Template contenTemplate = ve.getTemplate(templatePrefix+VM_TEMPLATE_CONTENT, FilesUtil.CHARSET_STRING);
				writeContentToFile(buildOutputFileName(contentMetadata), contenTemplate, context, targetCharset);
			} else {
				LOGGER.info("Füge an Content-Dokument: " + contentMetadata.getTitle());
				collectionList.add(contentMetadata);
			}
		}
		
		List<ContentMetadata> subcontent = contentMetadata.getSubContent();
		
		if (subcontent !=null) {
			
			for (int i = 0; i < subcontent.size(); i++) {
				ContentMetadata subContent = subcontent.get(i);
				ContentMetadata subContentPerv = (i>0)?(subcontent.get(i-1)):null;
				ContentMetadata subContentNext = (i+1<subcontent.size())?(subcontent.get(i+1)):null;
				buildContent(subContent, (contentMetadata.getTitle()!=null && contentMetadata.getContentFile()!=null)?contentMetadata:null, subContentPerv, subContentNext, collectionList, currentNavLevel, targetCharset);
			}
		}
	}
		
	private File buildOutputFileName(ContentMetadata contentMetadata, String suffix) {
		return buildOutputFileName(filenameCreator.buildOutputFileName(contentMetadata), suffix);
	}
	
	private File buildOutputFileName(ContentMetadata contentMetadata) {
		return buildOutputFileName(filenameCreator.buildOutputFileName(contentMetadata));
	}
	
	private File buildOutputFileName(String filename) {
		return buildOutputFileName(filename, ".html");
	}
	
	private File buildOutputFileName(String filename, String suffix) {
		return new File(targetDir, filename + suffix);
	}
		
	private static void writeContentToFile(File file, Template contenTemplate, Map<String, Object> context, Charset targetCharset) throws IOException {
		try {
			IContainer contentFolder = ResourcesPlugin.getWorkspace().getRoot().findContainersForLocationURI(file.getParentFile().toURI())[0];
			Properties projectDataProperties = FilesUtil.readProjectProperties((IFolder)contentFolder);
			for (String key : projectDataProperties.stringPropertyNames()) {
			    String value = projectDataProperties.getProperty(key);
			    
			    if (value!=null) {
			    	value = value.replace("^\"?([^\\\"]*)\"?$", "$1");
			    }
			    
			    //title -> projectTitle ("." und "-" mapping funktionieren nicht bei der PDF erstellung)
			    context.put("project" + key.substring(0, 1).toUpperCase() + key.substring(1), value);
			}
			
		} catch (IOException|CoreException e) {
			e.printStackTrace();
		}
		context.put("ContentMetadata", ContentMetadata.class); //um statische Methoden zu callen
		context.put("currentdate", new SimpleDateFormat("dd.MMMM YYYY").format(new Date()));
		
		VelocityContext vc = new VelocityContext(context);
		StringWriter sw = new StringWriter();
		contenTemplate.merge(vc, sw);
		String outputContent = sw.toString();
		
		try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), targetCharset))) {
			writer.write(outputContent);
		}
	}
}
