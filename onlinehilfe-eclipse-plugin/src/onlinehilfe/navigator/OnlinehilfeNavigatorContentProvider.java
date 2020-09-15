package onlinehilfe.navigator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.Platform;
import org.eclipse.ui.model.BaseWorkbenchContentProvider;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;

import onlinehilfe.CurrentPropertiesStore;
import onlinehilfe.navigator.IOnlinehilfeElement.ElementType;

public class OnlinehilfeNavigatorContentProvider extends BaseWorkbenchContentProvider {
	
	private static final Bundle BUNDLE = FrameworkUtil.getBundle(OnlinehilfeNavigatorContentProvider.class);
	private static final ILog LOGGER = Platform.getLog(OnlinehilfeNavigatorContentProvider.class);
	
	public OnlinehilfeNavigatorContentProvider() {
		super();
		LOGGER.info("OnlinehilfeNavigatorContentProvider init...");
	}
	
	@Override
	public Object[] getChildren(Object parentElement) {
		LOGGER.info("call getChildren(" + parentElement + ")");
		
		
		if (parentElement instanceof IProject) {
			try {
								
				for (String s : ((IProject)parentElement).getDescription().getNatureIds()) {
					LOGGER.info("NatureID: " + s);
				}
				
				if (((IProject)parentElement).hasNature(OnlinehilfeNature.NATURE_ID)) {
					CurrentPropertiesStore.getInstance().setProject((IProject)parentElement);
					LOGGER.info("ProjectNature: onlinehilfeNature found");
					List<Object> children = new ArrayList<Object>(Arrays.asList(super.getChildren(parentElement)));
					children.add(new OnlinehilfeElement((IProject)parentElement, ElementType.NAVROOT, null));
					return children.toArray();
				}	
			} catch (Exception e) {
				LOGGER.error("Fehler beim Auflösen der Projektstruktur", e);
			}	
			
		}
		
		if (parentElement instanceof IOnlinehilfeElement) {
			LOGGER.info("getChildren für IOnlinehilfeElement");
			return Arrays.asList(super.getChildren(parentElement)).stream().map(f -> OnlinehilfeElement.mapElement(f, (IOnlinehilfeElement)parentElement))
					//.peek(f -> System.out.println("    " + f.toString()))
					.collect(Collectors.toList()).toArray();
		}
		
		
		
		return super.getChildren(parentElement);
	}
	
	@Override
	public Object[] getElements(Object element) {
		LOGGER.info("call getElements(" + element + ")");
		return getChildren(element);
	}
		
	@Override
	public Object getParent(Object element) {
		LOGGER.info("call getParent(" + element + ")", new Throwable("dummy"));
		return super.getParent(element);
	}
	
	@Override
	public boolean hasChildren(Object element) {
		LOGGER.info("call hasChildren(" + element + ")");
		
		if (element instanceof OnlinehilfeElement && ((OnlinehilfeElement) element).getElementType() == ElementType.NAVPOINT) {
			if (super.hasChildren(element)) {
				boolean result = Arrays.asList(getChildren(element)).stream().anyMatch(f -> !(f instanceof IFile));
				//System.out.println("  " + result);
				return result;
			}
		}
		
		return super.hasChildren(element);
		
	}
}
