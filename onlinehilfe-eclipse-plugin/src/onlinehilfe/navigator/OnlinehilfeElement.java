package onlinehilfe.navigator;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResourceProxyVisitor;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.ui.model.IWorkbenchAdapter;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;

import onlinehilfe.navigation.NavigationMetadata;
import onlinehilfe.navigation.NavigationMetadataController;

public class OnlinehilfeElement implements IOnlinehilfeElement { 

	private static final Bundle BUNDLE = FrameworkUtil.getBundle(OnlinehilfeElement.class);
	private static final ILog LOGGER = Platform.getLog(OnlinehilfeElement.class);
	
	private static final IOnlinehilfeElement[] NO_CHILDREN = new IOnlinehilfeElement[0];
	
	private static final String CONTENTFILE = "content.htm";
		
	private final IContainer containerDelegate;
	private final ElementType elementType;
	private final IOnlinehilfeElement parentOnlinehilfeElement;
	
	public OnlinehilfeElement(IContainer container, ElementType elementType, IOnlinehilfeElement parentOnlinehilfeElement) {
		//LOGGER.info("      create OnlinehilfeElement("+container+", "+elementType+", "+parentOnlinehilfeElement+") + ");
		
		this.containerDelegate = container;
		this.elementType = elementType;
		this.parentOnlinehilfeElement = parentOnlinehilfeElement;
	}
			
	public ElementType getElementType() {
		return elementType;
	}
	
	public String getElementName() {
		//LOGGER.info("      getElementName() -- filename: " + containerDelegate.getName());
		
		if (ElementType.NAVROOT == this.getElementType()) {
			return "Navigationbaum";
		}
				
		if (getNavigationMetadata() !=null) {
			//LOGGER.info("                title found: " + getNavigationMetadata().getTitle());
			
			return getNavigationMetadata().getTitle();
		}
		
		//LOGGER.info("                TITLE NOT FOUND");
		
		return containerDelegate.getName();
	}
	
	public String getElementFilename() {
		if (ElementType.NAVROOT == this.getElementType()) {
			return "Navigationbaum";
		}
				
		return containerDelegate.getName();
	}
	
	@Override
	public NavigationMetadata getNavigationMetadata() {
		if (ElementType.NAVPOINT == this.getElementType()) {
			if (containerDelegate instanceof IFolder) {
				NavigationMetadata metaData = NavigationMetadataController.getInstance().getNavigationMetadataByIFolder((IFolder)containerDelegate);
				return metaData;	
			}
		}
		
		return null;
	}

	public <T> T getAdapter(Class<T> adapter) {
		
		if (IWorkbenchAdapter.class.equals(adapter)) {
			return (T) getWorkbench();
		}
		
		return null;
	}
	
	private IWorkbenchAdapter getWorkbench() {
		//System.out.println(" call getWorkbench()");
		return new OnlinehilfeWorkbenchAdapter();
	}
	
	private IOnlinehilfeElement[] collectElements(IContainer source, IOnlinehilfeElement parent) throws CoreException{
		return collectElements(source, parent, f -> (true));
	}
	
	private IOnlinehilfeElement[] collectElements(IContainer source, IOnlinehilfeElement parent, Predicate<IOnlinehilfeElement> filter) throws CoreException{
		//System.out.println(containerDelegate.members().length);
				
		List<IOnlinehilfeElement> children = Arrays.asList(source.members()).stream()
				.map(f -> OnlinehilfeElement.mapValidElement(f, this))
				.filter(f -> (f!=null))
				.filter(filter)
				.collect(Collectors.toList());
		OnlinehilfeElementSorter elementComparator = new OnlinehilfeElementSorter();
		
		Collections.sort(children, (IOnlinehilfeElement o1,IOnlinehilfeElement o2) -> {
			return elementComparator.compare(null,o1,o2);
		});
		
		return children.toArray(new IOnlinehilfeElement[children.size()]);
	}
	
	public IOnlinehilfeElement[] getChildren() throws CoreException {
		return this.collectElements(this.containerDelegate, this);
	}
	
	public IOnlinehilfeElement findChildren(String name) throws CoreException {
		IOnlinehilfeElement[] elements = this.collectElements(this.containerDelegate, this, (f -> (f.getElementName().equals(name))));
		if (elements!=null && elements.length>0) {
			return elements[0];
		}
		return null;
	}
	
	public IOnlinehilfeElement[] getNeighbours() throws CoreException {
		return this.collectElements(this.containerDelegate.getParent(), this.getParentOnlinehilfeElement());
	}
	
	public IOnlinehilfeElement getParentOnlinehilfeElement() {
		return parentOnlinehilfeElement;
	}
	
	public int getRelativeListIndexBetweenNeighbours() throws CoreException {
		List<IOnlinehilfeElement> nbs = Arrays.asList(this.getNeighbours());
		String currentId = this.getNavigationMetadata().getId();
		//System.out.println("##"+this.getElementName()+nbs+currentId);
		for (int i=0;i<nbs.size(); i++) {
			if (nbs.get(i).getNavigationMetadata().getId() == currentId) {
				return i;
			};
		}
		return -1;
	}
	
	public IFolder getIFolder() {
		return (IFolder) containerDelegate;
	}
	
	public IFolder getParent() {
		if (elementType == ElementType.NAVPOINT && containerDelegate.getParent() instanceof IFolder) {
			return (IFolder)containerDelegate.getParent();	
		}
		
		return null;
	}
	
	public static Object mapElement(Object object, IOnlinehilfeElement parentOhe) {
		
		IOnlinehilfeElement mappedObject = mapValidElement(object, parentOhe);
		
		return (mappedObject!=null)?mappedObject:object;
	}
	
	private static IOnlinehilfeElement mapValidElement(Object object, IOnlinehilfeElement parentOhe) {
		
		if (object instanceof IFolder) {
			IFolder folderObject = (IFolder)object;
			
			if (!folderObject.getName().startsWith("_") && !folderObject.getName().startsWith(".")) {
				return new OnlinehilfeElement(folderObject, ElementType.NAVPOINT, parentOhe);
			}			
						
		}
		
		return null;
	}
	
	

	public boolean isConflicting(ISchedulingRule rule) {
		return containerDelegate.isConflicting(rule);
	}

	
	public void accept(IResourceProxyVisitor visitor, int memberFlags) throws CoreException {
		containerDelegate.accept(visitor, memberFlags);
	}


	public void accept(IResourceProxyVisitor visitor, int depth, int memberFlags) throws CoreException {
		containerDelegate.accept(visitor, depth, memberFlags);
	}

	public void accept(IResourceVisitor visitor) throws CoreException {
		containerDelegate.accept(visitor);
	}

	public void accept(IResourceVisitor visitor, int depth, boolean includePhantoms) throws CoreException {
		containerDelegate.accept(visitor, depth, includePhantoms);
	}

	public void accept(IResourceVisitor visitor, int depth, int memberFlags) throws CoreException {
		containerDelegate.accept(visitor, depth, memberFlags);
	}

	public IPath getFullPath() {
		return containerDelegate.getFullPath();
	}

	public IProject getProject() {
		return containerDelegate.getProject();
	}
	
	public IFile getContentFile() {
		if (elementType == ElementType.NAVPOINT && containerDelegate instanceof IFolder) {
			IFolder folder = (IFolder)containerDelegate;
			return folder.getFile(CONTENTFILE);
		}
		return null;
	}

	@Override
	public String toString() {
		return getElementType() + getFullPath().toString();
	}
	
}
