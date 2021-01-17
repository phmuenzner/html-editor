package onlinehilfe.dialogs;

import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;

import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

public class NewContentWizard extends Wizard {
	
	public static final String PROPERTIES_KEY_TITLE = "title";
	
	public static final String CUSTOM_FIELD_PREFIX_FORMAT = "custom.content.field.%s";
	private static final String CUSTOM_FIELD_LABEL_FORMAT = CUSTOM_FIELD_PREFIX_FORMAT + ".label";
	private static final String CUSTOM_FIELD_TYPE_FORMAT = CUSTOM_FIELD_PREFIX_FORMAT + ".type";
	private static final String CUSTOM_FIELD_KEYNAME_FORMAT = CUSTOM_FIELD_PREFIX_FORMAT + ".keyname";
	private static final String CUSTOM_FIELD_ACTIVEIFTRUE_FORMAT = CUSTOM_FIELD_PREFIX_FORMAT + ".activeiftrue";
	private static final String CUSTOM_FIELD_VALIDATEREGEX_FORMAT = CUSTOM_FIELD_PREFIX_FORMAT + ".validateregex";
		
	protected String wizardTitle = "Neuen Content anlegen"; 
	protected String wizardPageDescription = "Hiermit legen sie einen neuen Content an. Sie müssen den Titel (die Überschrift) eingeben. Dieser Titel muss eindeutig sein. Zusätzlich können Sie weitere Konfigurationen anpassen.";
	protected String wizardPageTitleLabel = "Geben Sie den Titel des neuen Contents ein:";
	
	private final TitleValidator titleValidator = new TitleValidator();
	
	private final Properties customFieldConfigurationProperties;
	private final Properties returnProperties;
	private final Properties stagedReturnProperties;
	
	private NewContentWizardPage page;
	
	
	public NewContentWizard(Properties customFieldConfigurationProperties, Properties returnProperties) {
		super();
		setNeedsProgressMonitor(true);
		this.customFieldConfigurationProperties = customFieldConfigurationProperties;
		this.returnProperties = returnProperties;
		this.stagedReturnProperties = returnProperties;
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
		returnProperties.putAll(stagedReturnProperties);
		return true;
	}
	
	public class NewContentWizardPage extends WizardPage {
		private Composite container;
		
		public NewContentWizardPage() {
			super(wizardTitle);
			setTitle(wizardTitle);
			setDescription(wizardPageDescription);
		}

		@Override
		public void createControl(Composite parent) {
			
			container = new Composite(parent, SWT.NONE);
	        GridLayout layout = new GridLayout();
	        container.setLayout(layout);
	        layout.numColumns = 2;
	        
	        GridData gd = new GridData(GridData.FILL_HORIZONTAL);
	        
	        buildTitleTextField(gd);
	        
	        buildLineSpacer(gd, 5);
	        
	        for (final AtomicInteger i = new AtomicInteger(1); true; i.incrementAndGet()) {
	        	if (customFieldConfigurationProperties.keySet().stream().anyMatch(f -> ((String)f).startsWith(String.format(CUSTOM_FIELD_PREFIX_FORMAT, i.get())))) {
	        		buildCustomField(gd, i.get());
	        		continue;
	        	}
	        	break;
			}
    
	        // required to avoid an error in the system
	        setControl(container);
	        setPageComplete(false);
		}
		
		private void buildLineSpacer(GridData gridData, int space) {
			Label labelLabel = new Label(container, SWT.NONE);
			Label spacerLabel = new Label(container, SWT.NONE);
			labelLabel.setSize(labelLabel.getSize().x, space);
			spacerLabel.setSize(spacerLabel.getSize().x, space);
			spacerLabel.setLayoutData(gridData);
		}

		private void buildTitleTextField(GridData gridData) {
	        Label labelTitleText = new Label(container, SWT.NONE);
	        labelTitleText.setText(wizardPageTitleLabel);

	        Text textTitleText = new Text(container, SWT.BORDER | SWT.SINGLE);
	        textTitleText.setText(returnProperties.getProperty(PROPERTIES_KEY_TITLE, ""));
	        textTitleText.addKeyListener(new org.eclipse.swt.events.KeyAdapter() {
	            public void keyReleased(KeyEvent e) {
	                if (!textTitleText.getText().isEmpty()) {
	                	boolean valid = titleValidator.isValid(textTitleText.getText());
	                	
	                	if(valid) {
	                		stagedReturnProperties.put(PROPERTIES_KEY_TITLE, textTitleText.getText());
	                	}
	                	
	                    setPageComplete(valid);
	                    
	                    //Hier müss noch eine Prüfung rein, ob es den Text schon gibt.

	                }
	            }

	        });
	        
	        textTitleText.setLayoutData(gridData);
		}
		
		
		private void buildCustomField(GridData gridData, int fieldCount) {
			String labelText = customFieldConfigurationProperties.getProperty(String.format(CUSTOM_FIELD_LABEL_FORMAT, fieldCount), "");
			CustomFilesType type = CustomFilesType.valueOfString(customFieldConfigurationProperties.getProperty(String.format(CUSTOM_FIELD_TYPE_FORMAT, fieldCount)));
			
			String keyName = customFieldConfigurationProperties.getProperty(String.format(CUSTOM_FIELD_KEYNAME_FORMAT, fieldCount), null);
			String valueKey = String.format(CUSTOM_FIELD_PREFIX_FORMAT, (keyName!=null) ? keyName : fieldCount);
			
			String enableCheckField = customFieldConfigurationProperties.getProperty(String.format(CUSTOM_FIELD_ACTIVEIFTRUE_FORMAT, fieldCount), "");
			String validateRegex = customFieldConfigurationProperties.getProperty(String.format(CUSTOM_FIELD_VALIDATEREGEX_FORMAT, fieldCount), "");
			
			
			
	        Label label = new Label(container, SWT.NONE);
	        label.setText(labelText);

	        Control customField;
	        if (type == CustomFilesType.CHECK) {
	        	
	        	Button checkBox = new Button(container ,SWT.CHECK);
	        	customField = checkBox;
	        	
	        	if (Boolean.valueOf(returnProperties.getProperty(valueKey, Boolean.FALSE.toString()))) {
	        		checkBox.setSelection(true);
	        	}
	        	
	        	checkBox.addSelectionListener(new SelectionAdapter() {
	        		@Override
	        	    public void widgetSelected(SelectionEvent event) {
	        			stagedReturnProperties.put(valueKey, Boolean.valueOf(checkBox.getSelection()).toString());
	                    notifyListener(valueKey);
	        		}
	        	});
	        } else {
	        	
		        Text textField = new Text(container, SWT.BORDER | SWT.SINGLE);
		        customField = textField;
		        textField.setText(returnProperties.getProperty(valueKey, ""));
		        
		        textField.addKeyListener(new org.eclipse.swt.events.KeyAdapter() {
		            public void keyReleased(KeyEvent e) {
	                	boolean valid = (validateRegex.isEmpty())?true:textField.getText().matches(validateRegex);
	                	
	                	if (valid) {
	                		if (textField.getText()==null || textField.getText().isEmpty()){
	                			stagedReturnProperties.remove(valueKey);
	                		} else {
	                			stagedReturnProperties.put(valueKey, textField.getText());
	                		}
	                		notifyListener(valueKey);
	                	}
	                    setPageComplete(valid);
		            }
				});
		        
	        }
	        customField.setLayoutData(gridData);

	        CustomValueChangedListener customValueChangedListener = new CustomValueChangedListener() {
				@Override
				public void hasChanged(String customFieldPrefix) {
					if (customFieldPrefix.equals(enableCheckField)) {
				        customField.setEnabled(enableCheckField.isEmpty() || Boolean.valueOf(stagedReturnProperties.getProperty(enableCheckField)));
					}
				}
			};
			customValueChangedListener.hasChanged(enableCheckField);
			customValueChangedListeners.add(customValueChangedListener);
	        
		}
	}
	
	private static class TitleValidator {
		private Pattern p = Pattern.compile("[~\"#%&*:<>?\\/\\\\{|}]„“•");
		
		private boolean isValid(String title) {
			return !p.matcher(title).find();
		}	
	}
	
	private static enum CustomFilesType {
		TEXT, CHECK;
		
		public static CustomFilesType valueOfString(String value) {
			if (value == null || value.isEmpty()) {
				return TEXT;
			}
			return valueOf(value.toUpperCase());
		}
	}
	
	
	private List<CustomValueChangedListener> customValueChangedListeners = new LinkedList<>();
	
	private void notifyListener(final String customFieldPrefix) {
		customValueChangedListeners.forEach(f -> {f.hasChanged(customFieldPrefix);});
	}
	
	private static interface CustomValueChangedListener {
		public void hasChanged(String customFieldPrefix);
	}
}
