package onlinehilfe.navigator;

import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ResourceLocator;
import org.eclipse.jface.viewers.BaseLabelProvider;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.swt.graphics.Image;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;

import onlinehilfe.navigator.IOnlinehilfeElement.ElementType;


public class OnlinehilfeNavigatorLabelProvider extends BaseLabelProvider implements ILabelProvider {
	
	private static final Bundle BUNDLE = FrameworkUtil.getBundle(OnlinehilfeNavigatorLabelProvider.class);
	private static final ILog LOGGER = Platform.getLog(OnlinehilfeNavigatorLabelProvider.class);
	
	public String getText(Object element) {
		//LOGGER.info("call getText(" + element + ")");
		if (element !=null) {
			if (element instanceof IOnlinehilfeElement) {
				return ((IOnlinehilfeElement)element).getElementName();
			}
		}
		return null;
	}
	
	public Image getImage(Object element) {
		//LOGGER.info("call getImage(" + element + ")");
		if (element instanceof IOnlinehilfeElement) {
			ImageDescriptor imageDescriptor = null;
			if (ElementType.NAVROOT == ((IOnlinehilfeElement)element).getElementType()) {
				imageDescriptor = ResourceLocator.imageDescriptorFromBundle(BUNDLE.getSymbolicName(), "icons/book2d.png").orElse(null);
			} else if (ElementType.NAVPOINT == ((IOnlinehilfeElement)element).getElementType()) {
				imageDescriptor = ResourceLocator.imageDescriptorFromBundle(BUNDLE.getSymbolicName(), "icons/bookstay.png").orElse(null);
			} else {
				imageDescriptor = ResourceLocator.imageDescriptorFromBundle(BUNDLE.getSymbolicName(), "icons/page.png").orElse(null);
			}
			
			if (imageDescriptor != null) {
				return imageDescriptor.createImage();
			}
		} 
		return null;
	}
	
	@Override
	public boolean isLabelProperty(Object element, String property) {
		return true;
	}
}
