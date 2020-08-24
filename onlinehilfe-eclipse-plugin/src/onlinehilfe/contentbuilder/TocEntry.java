package onlinehilfe.contentbuilder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * ToC --> Table Of Contents, ich will nicht jedes mal Inhaltsverzeichnis schreiben
 */
public class TocEntry {
	private String id;
	private String title;
	private String filename;
	
	private List<TocEntry> subEntries = new ArrayList<>();
	
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
	
	public String getFilename() {
		return filename;
	}
	public void setFilename(String filename) {
		this.filename = filename;
	}
	
	public List<TocEntry> getSubEntries() {
		return Collections.unmodifiableList(subEntries);
	}
	
	public void addSubEntries(TocEntry subEntry) {
		this.subEntries.add(subEntry);
	}
}
