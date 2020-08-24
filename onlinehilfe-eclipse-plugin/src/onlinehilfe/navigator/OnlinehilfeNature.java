package onlinehilfe.navigator;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectNature;
import org.eclipse.core.runtime.CoreException;

public class OnlinehilfeNature implements IProjectNature {
	
	private IProject project;

    public void configure() throws CoreException {
    	System.out.println("NATURE call configure()");
       // Add nature-specific information
       // for the project, such as adding a builder
       // to a project's build spec.
    }
    public void deconfigure() throws CoreException {
    	System.out.println("NATURE call deconfigure()");
       // Remove the nature-specific information here.
    }
    public IProject getProject() {
    	System.out.println("NATURE call getProject() -> " + project);
       return project;
    }
    public void setProject(IProject value) {
    	System.out.println("NATURE call setProject("+value+")");
       project = value;
    }
}
