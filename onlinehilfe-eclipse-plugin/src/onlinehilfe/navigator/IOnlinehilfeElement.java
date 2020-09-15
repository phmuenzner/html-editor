package onlinehilfe.navigator;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;

import onlinehilfe.navigation.NavigationMetadata;

public interface IOnlinehilfeElement extends IAdaptable {
	
	public static enum ElementType {
		NAVROOT, NAVPOINT
	}
		
	public ElementType getElementType();
	
	public String getElementName();
	
	public String getElementFilename();
	
	public IOnlinehilfeElement[] getChildren() throws CoreException;
	
	public IOnlinehilfeElement findChildren(String name) throws CoreException;
	
	public IOnlinehilfeElement[] getNeighbours() throws CoreException;
	
	public IOnlinehilfeElement getParentOnlinehilfeElement();
	
	public int getRelativeListIndexBetweenNeighbours() throws CoreException;
	
	public IFolder getIFolder();
	
	public IFolder getParent();
	
	public NavigationMetadata getNavigationMetadata();
	
	public IPath getFullPath();
	
	public IProject getProject();
	
	public IFile getContentFile();
	
}
