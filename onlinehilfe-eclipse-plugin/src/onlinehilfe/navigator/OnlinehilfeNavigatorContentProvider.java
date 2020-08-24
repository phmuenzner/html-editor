package onlinehilfe.navigator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.ui.model.BaseWorkbenchContentProvider;

import onlinehilfe.CurrentPropertiesStore;
import onlinehilfe.navigator.IOnlinehilfeElement.ElementType;

public class OnlinehilfeNavigatorContentProvider extends BaseWorkbenchContentProvider {
	
	@Override
	public Object[] getChildren(Object parentElement) {
		//System.out.println("call getChildren(" + parentElement + ")");
		//System.out.println(Arrays.toString(Thread.currentThread().getStackTrace()));
		
		
		if (parentElement instanceof IProject) {
			try {
				
				if (((IProject)parentElement).hasNature("onlinehilfe.onlinehilfeNature")) {
					CurrentPropertiesStore.getInstance().setProject((IProject)parentElement);
					//System.out.println("ProjectNature: onlinehilfeNature found");
					List<Object> children = new ArrayList<Object>(Arrays.asList(super.getChildren(parentElement)));
					children.add(new OnlinehilfeElement((IProject)parentElement, ElementType.NAVROOT, null));
					return children.toArray();
				}	
			} catch (Exception e) {
				e.printStackTrace();
			}	
			
		}
		
//		if (parentElement instanceof IProject) {
//			System.out.println(" getChildren für IProject");
//			return Arrays.asList(super.getChildren(parentElement)).stream().map(f -> OnlinehilfeElement.mapElement(f, null))
//					.peek(f -> System.out.println("    " + f.toString()))
//					.collect(Collectors.toList()).toArray();
//		}
		if (parentElement instanceof IOnlinehilfeElement) {
			//System.out.println(" getChildren für IOnlinehilfeElement");
			return Arrays.asList(super.getChildren(parentElement)).stream().map(f -> OnlinehilfeElement.mapElement(f, (IOnlinehilfeElement)parentElement))
					//.peek(f -> System.out.println("    " + f.toString()))
					.collect(Collectors.toList()).toArray();
		}
		
		
		
		return super.getChildren(parentElement);
	}
	
	@Override
	public Object[] getElements(Object element) {
		//System.out.println("call getElements(" + element + ")");
		return getChildren(element);
	}
		
	@Override
	public Object getParent(Object element) {
		//System.out.println("call getParent(" + element + ")");
		return super.getParent(element);
	}
	
	@Override
	public boolean hasChildren(Object element) {
		//System.out.println("call hasChildren(" + element + ")");
		
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
