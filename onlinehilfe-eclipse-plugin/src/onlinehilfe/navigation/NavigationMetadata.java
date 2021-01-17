package onlinehilfe.navigation;

import java.util.Comparator;
import java.util.Map;

public class NavigationMetadata {
	private String title;
	private String id;
	private int order;
	private boolean hasChanges;
	private Map<Object, Object> customFields;
	
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public int getOrder() {
		return order;
	}
	public void setOrder(int order) {
		this.order = order;
	}
	public boolean isHasChanges() {
		return hasChanges;
	}
	public void setHasChanges(boolean hasChanges) {
		this.hasChanges = hasChanges;
	}
	public Map<Object, Object> getCustomFields() {
		return customFields;
	}
	public void setCustomFields(Map<Object, Object> customFields) {
		this.customFields = customFields;
	}
	
	//Achtung der Comparator hier und der Comparator in ContentMetadata sind an sich gleich und das muss auch so sein 
	public static class NavigationMetadataComparator implements Comparator<NavigationMetadata> {
		@Override
		public int compare(NavigationMetadata o1, NavigationMetadata o2) {
						
			//System.out.println(1);
			if (o1==null && o2!=null && o2.getOrder() != -1) {
				return -1;
			}
			
			//System.out.println(2);
			
			if (o2==null && o1!=null && o1.getOrder() != -1) {
				return 1;
			}

			//System.out.println(3);
			
			int c = 0;
			
			if (o1!=null && o2!=null && o1.getOrder()!=-1 && o2.getOrder()!=-1) {
				c = Integer.compare(o1.getOrder(), o2.getOrder());
			}
			
			//System.out.println(4);
			
			if (c==0 && o1!=null && o1.getTitle()!=null && o2!=null) {
				return o1.getTitle().compareTo(o2.getTitle());
			}
			
			return c;
		}
	}
}
