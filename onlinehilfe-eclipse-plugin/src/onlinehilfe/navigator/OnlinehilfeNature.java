package onlinehilfe.navigator;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectNature;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.Platform;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;

public class OnlinehilfeNature implements IProjectNature {
	private static final Bundle BUNDLE = FrameworkUtil.getBundle(OnlinehilfeNature.class);
	private static final ILog LOGGER = Platform.getLog(OnlinehilfeNature.class);
	
	public static final String NATURE_ID = BUNDLE.getSymbolicName() + ".onlinehilfeNature";
	
	private IProject project;

    public void configure() throws CoreException {
    	LOGGER.info("NATURE call configure()");
       // Add nature-specific information
       // for the project, such as adding a builder
       // to a project's build spec.  
    	
    	
    	for (String s : project.getDescription().getNatureIds()) {
    		LOGGER.info("NatureID: " + s);
		}
    }
    public void deconfigure() throws CoreException {
    	LOGGER.info("NATURE call deconfigure()");
       // Remove the nature-specific information here.
    }
    public IProject getProject() {
    	LOGGER.info("NATURE call getProject() -> " + project);
       return project;
    }
    public void setProject(IProject project) {
    	LOGGER.info("NATURE call setProject("+project+")");
       this.project = project;
    }
}
