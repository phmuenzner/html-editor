package onlinehilfe;

import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IProject;

public class CurrentPropertiesStore {

	private static CurrentPropertiesStore uniqueInstance = null;
	
	private List<PropertiesEventListener> propertyEvents = new LinkedList<>(); 
	
	private IContainer parent;
	private IProject project;

	public static CurrentPropertiesStore getInstance() {
		if (uniqueInstance == null) {
			generateInstance();
		}
		return uniqueInstance;
	}

	private static synchronized void generateInstance() {
		if (uniqueInstance == null) {
			uniqueInstance = new CurrentPropertiesStore();
		}
	}

	public void setParent(IContainer parent) {
		//System.out.println("setParent(" + parent + ")");
		
		if (this.parent==null || (parent!=null && !parent.equals(this.parent))) {
			this.parent = parent;
			notifyPropertiesEventListener("parent");
		}
	}

	public IContainer getParent() {
		return parent;
	}

	public void setProject(IProject project) {
		//System.out.println("setProject(" + project + ")");
		
		if (this.project==null || (project!=null && !project.equals(this.project))) {
			this.project = project;
			notifyPropertiesEventListener("project");
		}
	}

	public IProject getProject() {
		return project;
	}
	
	public synchronized void addPropertiesEventListener(PropertiesEventListener listener) {
		propertyEvents.add(listener);
	}
	
	public synchronized void removePropertiesEventListener(PropertiesEventListener listener) {
		propertyEvents.remove(listener);
	}
	
	private synchronized void notifyPropertiesEventListener(String propertyName) {
		propertyEvents.forEach(f -> f.propertyChanged(propertyName));
	}

}
