package onlinehilfe.navigator;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ResourceLocator;
import org.eclipse.jface.viewers.BaseLabelProvider;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.swt.graphics.Image;

import onlinehilfe.navigator.IOnlinehilfeElement.ElementType;


public class OnlinehilfeNavigatorLabelProvider extends BaseLabelProvider implements ILabelProvider {
	
	public String getText(Object element) {
		//System.out.println("lpcall getText(" + element + ")");
		
		if (element !=null) {
			if (element instanceof IOnlinehilfeElement) {
				return ((IOnlinehilfeElement)element).getElementName();
			}
			
			//return element;
		}
		return null;
	}
	
	public Image getImage(Object element) {
		//System.out.println("lpcall getImage(" + element + ")");
		if (element instanceof IOnlinehilfeElement) {
			ImageDescriptor imageDescriptor = null;
			if (ElementType.NAVROOT == ((IOnlinehilfeElement)element).getElementType()) {
				imageDescriptor = ResourceLocator.imageDescriptorFromBundle("onlinehilfe-eclipse-plugin", "icons/book2d.png").orElse(null);
			} else if (ElementType.NAVPOINT == ((IOnlinehilfeElement)element).getElementType()) {
				imageDescriptor = ResourceLocator.imageDescriptorFromBundle("onlinehilfe-eclipse-plugin", "icons/bookstay.png").orElse(null);
			} else {
				imageDescriptor = ResourceLocator.imageDescriptorFromBundle("onlinehilfe-eclipse-plugin", "icons/page.png").orElse(null);
			}
			
			if (imageDescriptor != null) {
				return imageDescriptor.createImage();
			}
		} 
		return null;
	}
	
	@Override
	public boolean isLabelProperty(Object element, String property) {
		System.out.println("lpcall isLabelProperty(" + element+", " + property + ")");
		return true;
	}
}
