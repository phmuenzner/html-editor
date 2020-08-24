package onlinehilfe.dialogs;

import java.util.Properties;
import java.util.regex.Pattern;

import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

public class NewContentWizard extends Wizard {
	
	public static final String PROPERTIES_KEY_TITLE = "title";
	
	protected static String wizardTitle = "Neuen Content anlegen"; 
	protected static String wizardPageDescription = "Hiermit legen sie einen neuen Content an. Sie müssen nur den Titel (die Überschrift) eingeben. Dieser Titel muss eindeutig sein.";
	protected static String wizardPageTitleLabel = "Geben Sie den Titel des neuen Contents ein:";
	
	private final TitleValidator titleValidator = new TitleValidator();
	
	private NewContentWizardPage page;
	
	private Properties returnProperties;
	
	public NewContentWizard(Properties returnProperties) {
		super();
		setNeedsProgressMonitor(true);
		
		this.returnProperties = returnProperties;
	}
	
	@Override
    public String getWindowTitle() {
        return wizardTitle;
    }
	
	@Override
	public void addPages() {
		page = new NewContentWizardPage();
		addPage(page);
		
	}
	
	@Override
	public boolean performFinish() {
		returnProperties.put(PROPERTIES_KEY_TITLE, page.getTitleText());
		return true;
	}
	
	public class NewContentWizardPage extends WizardPage {
		private Text textTitleText;
		private Composite container;
		
		public NewContentWizardPage() {
			super(wizardTitle);
			setTitle(wizardTitle);
			setDescription(wizardPageDescription);
		}

		@Override
		public void createControl(Composite parent) {
			// TODO Auto-generated method stub
			
			container = new Composite(parent, SWT.NONE);
	        GridLayout layout = new GridLayout();
	        container.setLayout(layout);
	        layout.numColumns = 2;
	        
	        Label label1 = new Label(container, SWT.NONE);
	        label1.setText(wizardPageTitleLabel);

	        textTitleText = new Text(container, SWT.BORDER | SWT.SINGLE);
	        	        
	        textTitleText.setText(returnProperties.getProperty(PROPERTIES_KEY_TITLE, ""));
	        
	        textTitleText.addKeyListener(new KeyListener() {            
	            public void keyPressed(KeyEvent e) {
	            	
	            }

	            public void keyReleased(KeyEvent e) {
	                if (!textTitleText.getText().isEmpty()) {
	                	boolean valid = titleValidator.isValid(textTitleText.getText());
	                	
	                    setPageComplete(valid);
	                    
	                    //Hier müss noch eine Prüfung rein, ob es den Text schon gibt.

	                }
	            }

	        });
	        
	        GridData gd = new GridData(GridData.FILL_HORIZONTAL);
	        textTitleText.setLayoutData(gd);
	        
	        // required to avoid an error in the system
	        setControl(container);
	        setPageComplete(false);
		}
		
	    public String getTitleText() {
	        return textTitleText.getText();
	    }
	}
	
	private static class TitleValidator {
		private Pattern p = Pattern.compile("[~\"#%&*:<>?\\/\\\\{|}]„“•");
		
		private boolean isValid(String title) {
			return !p.matcher(title).find();
		}	
	}
}
