package onlinehilfe.navigator;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.model.IWorkbenchAdapter;

public class OnlinehilfeWorkbenchAdapter implements IWorkbenchAdapter {
	
	protected static final Object[] NO_CHILDREN = new Object[0];
	
	@Override
	public Object[] getChildren(Object o) {
		//System.out.println("ohwba call getChildren(" + o + ")");
		
		if (o instanceof IOnlinehilfeElement) {
			try {
				return ((IOnlinehilfeElement)o).getChildren();				
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		return NO_CHILDREN;
	}
	
	@Override
	public ImageDescriptor getImageDescriptor(Object object) {
		//System.out.println("call getImageDescriptor("+object+")");
		
		return null;
	}
	
	@Override
	public String getLabel(Object o) {
		if (o instanceof IOnlinehilfeElement) {
			return ((IOnlinehilfeElement)o).getElementName();
		}

		return null;
	}
	
	@Override
	public Object getParent(Object o) {
		if (o instanceof IOnlinehilfeElement) {
			((IOnlinehilfeElement)o).getParent();
		}
		return null;
	}
}
