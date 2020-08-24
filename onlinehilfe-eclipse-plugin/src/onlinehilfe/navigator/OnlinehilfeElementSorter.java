package onlinehilfe.navigator;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;

import onlinehilfe.navigation.NavigationMetadata.NavigationMetadataComparator;

public class OnlinehilfeElementSorter extends ViewerComparator {
	
	private NavigationMetadataComparator navigationMetadataComparator = new NavigationMetadataComparator();
	
	@Override
	public int compare(Viewer viewer, Object o1, Object o2) {
		// System.out.println("call compare(" + viewer + ", " + o1+ ", " + o2+ ")");
	
		if (o1 !=null && o2!=null && o1 instanceof IOnlinehilfeElement && o2 instanceof IOnlinehilfeElement) {
			// System.out.println("wir sortieren selbst");
			
			return navigationMetadataComparator.compare(((IOnlinehilfeElement)o1).getNavigationMetadata(), ((IOnlinehilfeElement)o2).getNavigationMetadata());
		} 
		
		return super.compare(viewer, o1, o2);
	}
}
