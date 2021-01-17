package onlinehilfe.contentbuilder;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ContentMetadata {
		
	private String id;
	private String title;
	private File contentFile;
	private String filename;
	private int navLevel;
	private int order;
	private List<ContentMetadata> subContent = new ArrayList<>();
	private ContentMetadata parentContent;
	private ContentMetadata previousContent;
	private ContentMetadata nextContent;
	private Map<Object, Object> customFields;
	
	public String getId() {
		return id;
	}
	
	public void setId(String id) {
		this.id = id;
	}
	
	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}
	public File getContentFile() {
		return contentFile;
	}

	public void setContentFile(File contentFile) {
		this.contentFile = contentFile;
	}

	public String getFilename() {
		return filename;
	}
	
	public void setFilename(String filename) {
		this.filename = filename;
	}
	
	public int getNavLevel() {
		return navLevel;
	}
	
	public void setNavLevel(int navLevel) {
		this.navLevel = navLevel;
	}
	
	public int getOrder() {
		return order;
	}

	public void setOrder(int order) {
		this.order = order;
	}

	public List<ContentMetadata> getSubContent() {
		Collections.sort(subContent, new ContentMetadataComparator());
		return Collections.unmodifiableList(subContent);
	}
	
	public void addSubContent(ContentMetadata subContent) {
		this.subContent.add(subContent);
	}
	
	public void setParentContent(ContentMetadata parentContent) {
		this.parentContent = parentContent;
	}
	
	public ContentMetadata getParentContent() {
		return parentContent;
	}
	
	public void setPreviousContent(ContentMetadata previousContent) {
		this.previousContent = previousContent;
	}
	
	public ContentMetadata getPreviousContent() {
		return previousContent;
	}
	
	public void setNextContent(ContentMetadata nextContent) {
		this.nextContent = nextContent;
	}
	
	public ContentMetadata getNextContent() {
		return nextContent;
	}
	
	public void setCustomFields(Map<Object, Object> customFields) {
		this.customFields = customFields;
	}
	
	public Map<Object, Object> getCustomFields() {
		return customFields;
	}
	
	// vorübergehend
	@Override
	public String toString() {
		return "NavName: " + title + " - Id: "+ id + " [" + ((contentFile != null) ? (contentFile.getName()) : ("null")) + "], " + navLevel + ", "
				+ Arrays.toString(subContent.stream().map(m -> m.toString()).collect(Collectors.toList()).toArray());
	}	
	
	//Achtung der Compaerator hier und und Comparator in NavigationMetadata sind an sich gleich und das muss auch so sein
	private static class ContentMetadataComparator implements Comparator<ContentMetadata> {
		@Override
		public int compare(ContentMetadata o1, ContentMetadata o2) {
						
			if (o1==null && o2!=null && o2.getOrder() != -1) {
				return -1;
			}
			
			if (o2==null && o1!=null && o1.getOrder() != -1) {
				return 1;
			}

			int c = 0;
			
			if (o1!=null && o2!=null && o1.getOrder()!=-1 && o2.getOrder()!=-1) {
				c = Integer.compare(o1.getOrder(), o2.getOrder());
			}
			
			if (c==0 && o1!=null && o1.getTitle()!=null && o2!=null) {
				return o1.getTitle().compareTo(o2.getTitle());
			}
			
			return c;
		}
	}
	
	public static String textContentOf(ContentMetadata contentMetadata) {
		try {
			String contentText = new String(Files.readAllBytes(contentMetadata.getContentFile().toPath()), FilesUtil.CHARSET);
			return contentText.replace("/_images/", "_images/");
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
}
