package onlinehilfe.navigation;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;

import onlinehilfe.CurrentPropertiesStore;
import onlinehilfe.MessageBoxUtil;
import onlinehilfe.PropertiesEventListener;
import onlinehilfe.contentbuilder.FilesUtil;
import onlinehilfe.navigation.NavigationMetadata.NavigationMetadataComparator;
import onlinehilfe.navigator.IOnlinehilfeElement;
import onlinehilfe.navigator.IOnlinehilfeElement.ElementType;
import onlinehilfe.navigator.OnlinehilfeElement;

public class NavigationMetadataController implements PropertiesEventListener, IResourceChangeListener {
	private static NavigationMetadataController uniqueInstance = null;
	
	private Map<IFolder, NavigationMetadata> navigationMetadatas = new ConcurrentHashMap<>();
	private Set<String> contentIDs = new HashSet<>();
	
	private static final int SLEEP_SECONDS = 1; 
	
	private boolean canUpdate = false;
	
	private Scheduler scheduler = new Scheduler();
	
	private Long nextContentId = UUID.randomUUID().getMostSignificantBits() & Long.MAX_VALUE; //der Versuch eine relativ eizigartige Id zu bekommen...
	
	public static NavigationMetadataController getInstance() {
		if (uniqueInstance == null) {
			generateInstance();
		}
		return uniqueInstance;
	}
	
	private static synchronized void generateInstance() {
		if (uniqueInstance == null) {
			uniqueInstance = new NavigationMetadataController();
		}
	}
	
	private NavigationMetadataController() {
		updateMetdaData();
	}
	
	@Override
	public void propertyChanged(String propertyName) {
		if ("project".equals(propertyName)) {
			updateMetdaData();
		}
	}
	
	@Override
	public void resourceChanged(IResourceChangeEvent event) {
		if (!canUpdate) return;
		reset();
		scheduler.halt();
		updateMetdaData();
		scheduler.resume();
	}
	
	public void updateMetdaData() {
		scheduler.start();
	}
	
	public void immediateDataRefresh() {
		//System.out.println("immediateDataRefresh()");
		this.updateMetdaDataFromScheduler();
	}
	
	public IOnlinehilfeElement renameElement(IOnlinehilfeElement selected, String newName) {
		IOnlinehilfeElement parent = selected.getParentOnlinehilfeElement();
		try {
			NavigationMetadata metaData = navigationMetadatas.remove(selected.getIFolder());
			metaData.setTitle(newName);
			
			
			IPath newPath = parent.getFullPath().append(newName);
			selected.getIFolder().move(parent.getFullPath().append(newName), false, new NullProgressMonitor());
			
			IFolder newFolder = selected.getParent().getFolder(newName);
			
			navigationMetadatas.put(newFolder, metaData);
			
			//OnlineHilfeElememnt direkt ersetzen
			selected = new OnlinehilfeElement(newFolder, selected.getElementType(), selected.getParentOnlinehilfeElement());
			return selected;
			
		} catch (CoreException e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	public void removeElement(IOnlinehilfeElement selected) {
		try {
			navigationMetadatas.remove(selected.getIFolder());
			selected.getIFolder().delete(false, new NullProgressMonitor());
		} catch (CoreException e) {
			e.printStackTrace();
		}
	}
	
	public IOnlinehilfeElement newElement(IOnlinehilfeElement parent, String newName) throws CoreException {
		
		String newFilename = FilesUtil.buildFilenameFromTitle(newName);
		
		int newOrderId =  parent.getChildren().length+1;
		
		IPath newPath = parent.getFullPath().append(newFilename);
		IFolder targetFolder = ResourcesPlugin.getWorkspace().getRoot().getFolder(newPath);
		
		try {
			targetFolder.create(false, true, new NullProgressMonitor());
			
			IFile emptyContent = targetFolder.getFile(FilesUtil.CONTENT_FILENAME);
			emptyContent.create(new ByteArrayInputStream("".getBytes()), true, new NullProgressMonitor());
			
			NavigationMetadata newMetaData = new NavigationMetadata();
			newMetaData.setHasChanges(true);
			newMetaData.setId(newContentId());
			newMetaData.setOrder(newOrderId);
			newMetaData.setTitle(newName);
			navigationMetadatas.put(targetFolder, newMetaData);
			writeOneMetaProperties(targetFolder);
						
			return parent.findChildren(newName);
			
		} catch (CoreException|IOException e) {
			e.printStackTrace();
		} finally {
			this.immediateDataRefresh();	
		}
		
		return null;
	}
	
	public boolean canMoveItemVertical(IOnlinehilfeElement selected, boolean moveUp) {
		try {
		
			if (selected.getElementType() == ElementType.NAVROOT) {
				return false;
			}
			
			List<IOnlinehilfeElement> neighbours = Arrays.asList(selected.getNeighbours());
			int indexSelected = selected.getRelativeListIndexBetweenNeighbours();
					
			boolean validMove = ((moveUp)? (indexSelected > 0) : (indexSelected < neighbours.size() - 1));
			return validMove;
		
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return false;
	}
	
	public boolean moveItemVertical(IOnlinehilfeElement selected, boolean moveUp) throws CoreException, IOException {
		if (canMoveItemVertical(selected, moveUp)) {
			List<IOnlinehilfeElement> neighbours = Arrays.asList(selected.getNeighbours());
			int indexSelected = selected.getRelativeListIndexBetweenNeighbours();
			
			try (UpdateMetadataSection update = new UpdateMetadataSection()) {	
				int offset = ((moveUp)? -1:1);
				NavigationMetadata currentData = selected.getNavigationMetadata();
				NavigationMetadata targetData = neighbours.get(indexSelected + offset).getNavigationMetadata();
				
				int currentOrder = currentData.getOrder();
				int targetOrder = targetData.getOrder();
				
				currentData.setOrder(targetOrder);
				targetData.setOrder(currentOrder);
		
				currentData.setHasChanges(true);
				targetData.setHasChanges(true);
		
				writeContentMetaDatasToFolders();
			}
		
			return true;
		}
		
		return false;
	}
	
	public boolean canMoveItemHorizontal(IOnlinehilfeElement selected, boolean moveIn) {
		try {
			
			if (selected.getElementType() == ElementType.NAVROOT) {
				return false;
			}
			
			return (moveIn) 
					? (selected.getRelativeListIndexBetweenNeighbours() > 0)
					: (selected.getParentOnlinehilfeElement() != null && selected.getParentOnlinehilfeElement().getElementType() == ElementType.NAVPOINT);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return false;	
	}
	
	public boolean moveItemHorizontal(IOnlinehilfeElement selected, boolean moveIn) throws CoreException, IOException {
		
		if (canMoveItemHorizontal(selected, moveIn)) {
			
			try (UpdateMetadataSection update = new UpdateMetadataSection()) {
				IFolder selectedFolder = selected.getIFolder();
				NavigationMetadata src = selected.getNavigationMetadata();
				
				// move in under neighbour above
				IOnlinehilfeElement dest = (moveIn) ? Arrays.asList(selected.getNeighbours()).get(selected.getRelativeListIndexBetweenNeighbours() - 1): selected.getParentOnlinehilfeElement();		
				IOnlinehilfeElement[] neighbours = (moveIn) ? dest.getChildren() : dest.getNeighbours();
				
				src.setOrder((neighbours.length>0)?(getNavigationMetadataByIFolder(neighbours[neighbours.length - 1].getIFolder()).getOrder()+1):0);
				src.setHasChanges(true);
				
				writeOneMetaProperties(selectedFolder);
				
				IPath toPath = (moveIn) ? dest.getFullPath(): dest.getParentOnlinehilfeElement().getFullPath();
				
				selectedFolder.move(toPath.append(selectedFolder.getName()), false, new NullProgressMonitor());
				reset();
			}
			
			return true;
		}
		
		return false;
		
	}
		
	private void updateMetdaDataFromScheduler() {
		IContainer projectContainer = ResourcesPlugin.getWorkspace().getRoot().findContainersForLocationURI(CurrentPropertiesStore.getInstance().getProject().getRawLocationURI())[0];	
		
		try {
			readContentMetaDatasFromFolders(projectContainer);
			//System.out.println("Verifying Order Integrity");
			try(UpdateMetadataSection update = new UpdateMetadataSection()) {
				boolean shouldReload = this.verifyOrderIntegrity();
				if (shouldReload) {
					readContentMetaDatasFromFolders(projectContainer);
				}
				update.close();
			}
			initContentMetaDatas();
			
			writeContentMetaDatasToFolders();
		} catch (Exception e) {
			e.printStackTrace();
			MessageBoxUtil.displayError("Fehler beim Initialisierung der Navigations-Metadaten", e);
		}
	}
	
	private void reset() {
		navigationMetadatas.clear();
	}
	
	private boolean verifyOrderIntegrity() {
		// Collect folders with same parent
		Map<IContainer, ArrayList<IFolder>> parentRef = new HashMap<>();
		for (IFolder folder: this.navigationMetadatas.keySet()) {
			if (!parentRef.containsKey(folder.getParent())) {
				parentRef.put(folder.getParent(), new ArrayList<IFolder>());
			}
			parentRef.get(folder.getParent()).add(folder);
		}
		
		boolean writeRequired = false;
		
		// Extract Metadata for each entry, sort, overwrite order if required
		for (IContainer container: parentRef.keySet()) {			
			List<NavigationMetadata> navdata = new ArrayList<NavigationMetadata>();
			Map<String, Long> lastModified = new HashMap<String, Long>();
			Map<String, IFolder> ifoldermap = new HashMap<String, IFolder>();
			for (IFolder folder: parentRef.get(container)) {
				NavigationMetadata itemData = this.navigationMetadatas.get(folder);
				navdata.add(itemData);
				lastModified.put(itemData.getId(), folder.getFile(FilesUtil.META_PROPERTIES_FILENAME).getLocation().toFile().lastModified());
				ifoldermap.put(itemData.getId(), folder);
			}
			
			//remove duplicates
			navdata.sort(new NavigationMetadataComparator());
			if (this.hasDuplicates(navdata)) {
				navdata = this.removeDuplicates(navdata, lastModified);
				writeRequired = true;
			}
		}
		
		if (writeRequired) {
			try {
				this.writeContentMetaDatasToFolders();
			} catch (IOException|CoreException e) {
				e.printStackTrace();
			}
		}
		return writeRequired;
	}
	
	private boolean hasDuplicates(List<NavigationMetadata> toCheck) {
		for(int i=0; i<toCheck.size(); i++) {
			for (int j=0;j<toCheck.size(); j++) {
				if (i != j && toCheck.get(i).getOrder() == toCheck.get(j).getOrder()) {
					return true;
				}
			}
		}
		return false;
	}
	
	private List<NavigationMetadata> removeDuplicates(List<NavigationMetadata> toCheck, Map<String, Long> age) {
		int maxOrder = 0;
		List<String> needsNewOrder = new ArrayList<String>();
		for (int i=0; i<toCheck.size(); i++) {
			NavigationMetadata validate = toCheck.get(i);
			if (!needsNewOrder.contains(validate.getId())) {
				for (int j=0; j<toCheck.size(); j++) {
					NavigationMetadata target = toCheck.get(j);
					if (i != j && validate.getOrder() == target.getOrder()) {
						if (age.get(validate.getId()) < age.get(target.getId())) {
							needsNewOrder.add(target.getId());
						} else {
							needsNewOrder.add(validate.getId());
						}
					}
				}
			}
			if (validate.getOrder() > maxOrder) {
				maxOrder = validate.getOrder();
			}
		}
		
		int currentOrder = maxOrder + 1;
		for (String contentId: needsNewOrder) {
			for (int i= 0; i<toCheck.size(); i++) {
				NavigationMetadata data = toCheck.get(i);
				if (data.getId() == contentId) {
					data.setOrder(currentOrder);
					data.setHasChanges(true);
					currentOrder += 1;
				}
			}
		}
		return toCheck;
	}
	
	private void readContentMetaDatasFromFolders(IContainer container) throws IOException, CoreException {
		//System.out.println("readContentMetaDatasFromFolders("+container+")");
		for(IResource member : container.members()) {
			if (IFolder.FOLDER == (IFolder.FOLDER & member.getType())) {
				if (!member.getName().startsWith("_") && !member.getName().startsWith(".")) {
					if (member instanceof IFolder) {
						readOneMetaProperties((IFolder)member);
						readContentMetaDatasFromFolders((IFolder)member);
					}
				}
			}
		}
	}
	
	private void readOneMetaProperties(IFolder folder) throws IOException, CoreException {
		Properties metaProperties = FilesUtil.readMetaProperties(folder);
		
		NavigationMetadata navigationMetadata = new NavigationMetadata();
						
		if (metaProperties.getProperty("contentTitle") != null) {
			navigationMetadata.setTitle(metaProperties.getProperty("contentTitle"));
		}
		
		if (metaProperties.getProperty("contentId") != null) {
			navigationMetadata.setId(metaProperties.getProperty("contentId"));
			contentIDs.add(navigationMetadata.getId());
		}
		
		if (metaProperties.getProperty("contentOrder") != null) {
			navigationMetadata.setOrder(Integer.valueOf(metaProperties.getProperty("contentOrder")));
		}
		
		navigationMetadatas.put(folder, navigationMetadata);
	}
	
	private void writeContentMetaDatasToFolders() throws IOException, CoreException {
		for (IFolder folder: navigationMetadatas.keySet()) {
			writeOneMetaProperties(folder);
		}
	}
		
	private void writeOneMetaProperties(IFolder folder) throws IOException, CoreException {
		Properties metaProperties = FilesUtil.readMetaProperties(folder);
		
		NavigationMetadata navigationMetadata = navigationMetadatas.get(folder);
				
		if (navigationMetadata == null) {
			System.out.println("Keine Daten für " + folder + " vorhanden.");
			return;
		} 
		
		if (navigationMetadata.isHasChanges()) {
			System.out.println("writeContentMetaDatasToFolders()" + folder);
			metaProperties.setProperty("contentTitle", navigationMetadata.getTitle());
			metaProperties.setProperty("contentId", navigationMetadata.getId());
			metaProperties.setProperty("contentOrder", Integer.toString(navigationMetadata.getOrder()));
			
			FilesUtil.writeMetaProperties(folder, metaProperties);
		}
		
	}

	
	private void initContentMetaDatas() {
		for (Entry<IFolder, NavigationMetadata> entry : navigationMetadatas.entrySet()) {
									
			NavigationMetadata navigationMetadata = entry.getValue(); 
			if (navigationMetadata.getId() == null) {
				System.out.println("Das dürfte eigentlich nicht mehr passieren");
				//navigationMetadata.setId(newContentId());
				//navigationMetadata.setHasChanges(true);
			}
			
//			if (!entry.getKey().getName().equals(navigationMetadata.getTitle())) {
//				navigationMetadata.setTitle(entry.getKey().getName());
//				navigationMetadata.setHasChanges(true);
//			}
			
//			if (navigationMetadata.getOrder() <= 0) {
//				System.out.println("Baue initiale Sortierung auf, das kann etwas dauern....");
//				
//				Optional<Integer> maxOrder = navigationMetadatas.keySet().stream()
//						.filter(f -> (entry.getKey().getParent().equals(f.getParent()))) //finde andere auf gleicher ordnerhöhe
//						.filter(f-> !entry.equals(f)) // sortiert sich selbst aus
//						.map(f -> navigationMetadatas.get(f).getOrder()) //maped auf order aus value
//						.filter(f -> (f != -1)) //alle order != -1
//						.max(Integer::compare);
//				navigationMetadata.setOrder((maxOrder.isPresent()) ? maxOrder.get()+1 : 1);
//				navigationMetadata.setHasChanges(true);
//			}
			
		}
	}
	
	private synchronized String newContentId() {
		while (contentIDs.contains(nextContentId.toString())) {
			nextContentId++;
		}
		
		String newContentId = nextContentId.toString();
		contentIDs.add(newContentId);
		return newContentId;
	}
	
	public NavigationMetadata getNavigationMetadataByIFolder(IFolder folder) {
		//System.out.println("call getNavigationMetadataByIFolder("+folder+") --> " + navigationMetadatas.size() + ", " + navigationMetadatas.get(folder));
		return navigationMetadatas.get(folder);
	}
		
	public void terminate() {
		scheduler.terminate();
	}
	
	private class Scheduler implements Runnable {
		private ExecutorService executorService = Executors.newSingleThreadExecutor();
		private AtomicBoolean stopped = new AtomicBoolean();
		private AtomicBoolean triggerd = new AtomicBoolean();
		private AtomicBoolean running = new AtomicBoolean();
				
		public void halt() {
			stopped.set(true);
		}
		
		public void resume() {
			new Thread(new Runnable() {
				public void run() {
					try {
						TimeUnit.SECONDS.sleep(SLEEP_SECONDS);	
					} catch (Exception e) {
						e.printStackTrace();
					}
					
					stopped.set(false);
					if (triggerd.get()) {
						start();
					}		
				}
			}).start();
			 
		}
		
		public void start() {
			triggerd.set(true);
			if (!running.get()) {			
				executorService.submit(this);
			}
		}
		
		@Override
		public void run() {
			if (triggerd.get()) {
				triggerd.set(false);
				running.set(true);
				
				try {
					while (stopped.get()) {
						TimeUnit.SECONDS.sleep(SLEEP_SECONDS);
					}
					
					//System.out.println("scheduler run");
					updateMetdaDataFromScheduler();
					
					TimeUnit.SECONDS.sleep(SLEEP_SECONDS);
					
				} catch (InterruptedException e) {
					e.printStackTrace();
					resume();
				} finally {
					running.set(false);
					if (triggerd.get()) {
						start();
					}
				}
				
			}
		}
		
		public void terminate() {
			try {
				executorService.awaitTermination(2*SLEEP_SECONDS, TimeUnit.SECONDS);	
			} catch (Exception e) {
				e.printStackTrace();
			}
			 
		}
	}
	
	private class UpdateMetadataSection implements AutoCloseable {
		public UpdateMetadataSection() {
			canUpdate = false;
		}
		
		@Override
		public void close() {
			updateMetdaData();			
			canUpdate = true;
			
		}
	}
}
