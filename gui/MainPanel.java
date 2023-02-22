package unescan.gui;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.concurrent.ThreadLocalRandom;

import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import unescan.Unescan;
import unescan.core.Device;

public class MainPanel {
	
	private final Win win;
	private final MainPanelEvents events;
	private Tree locations;
	private final String[] models, owners;
	private ScrolledComposite scrolledDevices;
	private Group devices;
	private Combo port, defaultOwner;
	private Button save, portRefresh, portButton, manualAdd;
	private Composite info;
	private Label bqeLabel, lbl_conf, status, labelSaveStatus;
	private final Ressource ress;
	private final int CONTAINER_STYLE = SWT.NONE;
	private final Hashtable<Device, PanelDevice> locationDevices;
	
	private final short verboseLevel = 2;
	
	public MainPanel(final Win p_win)
	{
		win = p_win;
		events = new MainPanelEvents(this, win);
		ress = win.getRessource();
		HashMap<String, String> modelMap = win.getCore().getModelMap();
		owners = win.getCore().getOwners();
		models = win.getCore().getSortedModels(modelMap);
		locationDevices = new Hashtable<Device, PanelDevice>();
	}
	
	public void createWidgets(final Composite parent)
	{
		Composite mainContainer = new Composite(parent, CONTAINER_STYLE);
		{
			mainContainer.setLayout(new GridLayout(2, false));
			
			Composite topConf = new Composite(mainContainer, CONTAINER_STYLE);
			topConf.setLayoutData(new GridData(GridData.FILL, GridData.BEGINNING, true, false, 2, 1));
			{
				topConf.setLayout(new GridLayout(2, true));
			
				Composite saveButton = new Composite(topConf, CONTAINER_STYLE);
				saveButton.setLayoutData(new GridData(GridData.BEGINNING, GridData.CENTER, false, false));
				{
					saveButton.setLayout(new RowLayout(SWT.HORIZONTAL));
					
					save = new Button(saveButton, SWT.PUSH);
					save.setImage(ress.getImage(Ressource.IMAGE_SAVE));
					save.setEnabled(false);
					
					labelSaveStatus = new Label(saveButton, SWT.NONE);
					labelSaveStatus.setText("                                                               ");
					labelSaveStatus.setFont(win.getRessource().getFont(Ressource.FONT_SAVESTATUS));
					labelSaveStatus.setForeground(ress.getColor(Ressource.COLOR_REDRED));
				}

				Composite handscanContainer = new Composite(topConf, SWT.BORDER);
				handscanContainer.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, false, false));
				{
					handscanContainer.setLayout(new GridLayout(4, false));
					
					Label label = new Label(handscanContainer, SWT.NONE);
					label.setText("Douchette: ");
					label.setLayoutData(new GridData(GridData.END, GridData.CENTER, true, false));
					port = new Combo(handscanContainer, SWT.BORDER | SWT.READ_ONLY);
					port.setLayoutData(new GridData(70,20));
					portRefresh = new Button(handscanContainer, SWT.PUSH);
					portRefresh.setText("Rafraichir");
					portRefresh.setLayoutData(new GridData(98,20));
					portButton = new Button(handscanContainer, SWT.PUSH);
					portButton.setText("Connecter");
					portButton.setLayoutData(new GridData(98,20));
					win.getShell().setDefaultButton(portButton);
				}
		
			
			}
			
			//Tree locations
			Composite tree = new Composite(mainContainer ,CONTAINER_STYLE);
			GridData gd = new GridData(GridData.FILL, GridData.FILL, false, true);
			gd.widthHint = 190;
			gd.heightHint = 600;
			
			tree.setLayoutData(gd);
			{
				GridLayout layoutTree = new GridLayout(1, true);
				tree.setLayout(layoutTree);
			
				
				
				Label previous = new Label(tree, SWT.CENTER);
				previous.setText("Précédent");
				previous.setFont(ress.getFont(Ressource.FONT_BARCODE));
				previous.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, true, false));
				
				new Label(tree, SWT.NONE).setImage(ress.getImage(Ressource.IMAGE_USC3));
				
				
				locations = new Tree(tree, SWT.SINGLE | SWT.BORDER);
				locations.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, true));
				
				
				new Label(tree, SWT.NONE).setImage(ress.getImage(Ressource.IMAGE_USC4));
				Label next = new Label(tree, SWT.CENTER);
				next.setText("Suivant");
				next.setFont(ress.getFont(Ressource.FONT_BARCODE));
				next.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, true, false));
				
			}
			
			
			Composite detailsPanel = new Composite(mainContainer, CONTAINER_STYLE);
			gd = new GridData(GridData.FILL, GridData.FILL, true, true);
			gd.widthHint = 500;
			detailsPanel.setLayoutData(gd);
			
			{
				detailsPanel.setLayout(new GridLayout(1, false));
				
				bqeLabel = new Label(detailsPanel, SWT.NONE);
				bqeLabel.setFont(ress.getFont(Ressource.FONT_LOCATION));
				bqeLabel.setText("Sélectionner une localisation");

				Composite owner = new Composite(detailsPanel, SWT.BORDER);
				{
					owner.setLayout(new GridLayout(2, false));
				
					new Label(owner, SWT.NONE).setText("Propriétaire par défaut: ");
					defaultOwner = new Combo(owner, SWT.NONE);
				}
				
				scrolledDevices = new ScrolledComposite(detailsPanel, SWT.V_SCROLL | SWT.H_SCROLL);
				scrolledDevices.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, true));
				{
					devices = new Group(scrolledDevices, SWT.NONE);
					devices.setText("Périphériques");
					devices.setLayout(new GridLayout(3, true));
					devices.setSize(devices.computeSize(SWT.DEFAULT, SWT.DEFAULT));
				}
				scrolledDevices.setShowFocusedControl(true);
				scrolledDevices.setContent(devices);
				
				manualAdd = new Button(detailsPanel, SWT.PUSH);
				manualAdd.setText("Ajout manuel");
				manualAdd.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, true, false));
			}
			
			Composite c_bot = new Composite(mainContainer, CONTAINER_STYLE);
			c_bot.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, true, false, 2, 1));
			{
				c_bot.setLayout(new GridLayout(2, true));
				
				Label sep = new Label(c_bot, SWT.SEPARATOR | SWT.HORIZONTAL);
				sep.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, true, false, 2, 1));
				
				Composite c_status = new Composite(c_bot, CONTAINER_STYLE);
				c_status.setLayoutData(new GridData(GridData.BEGINNING, GridData.CENTER, true, false));
				{
					c_status.setLayout(new GridLayout());
					
					status = new Label(c_status, SWT.NONE);
					status.setText("Douchette non connectée");
					status.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, true, false));
				}
				
				
				info = new Composite(c_bot, CONTAINER_STYLE);
				info.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, false));
				{
					info.setLayout(new GridLayout());
					
					lbl_conf = new Label(info, SWT.NONE);
					lbl_conf.setLayoutData(new GridData(GridData.END, GridData.FILL, true, false));
				}
			}
		}
	}
	
	public void newDevice(final Device dev, final boolean edit_serial)
	{	
		int comboWidth = 150;
		int textWidth = 165;
		Composite deviceContainer = new Composite(devices, SWT.BORDER);
		{
			deviceContainer.setLayout(new GridLayout());
			
			Combo model = new Combo(deviceContainer, SWT.READ_ONLY);
			model.setItems(models);
			model.setLayoutData(new GridData(comboWidth,15));
			int nbModels = models.length;
			for(int i = 0; i < nbModels; i++)
			{
				if(models[i].equals(dev.getModel()))
				{
					model.select(i);
					break;
				}
			}
			if(model.getSelectionIndex() == -1) model.setBackground(ress.getColor(Ressource.COLOR_RED));
			
			Composite c_serial = new Composite(deviceContainer, CONTAINER_STYLE);
			
			GridLayout layout = new GridLayout(2, false);
			layout.marginHeight = 0;
			layout.marginWidth = 0;
			c_serial.setLayout(layout);
			
			Text serial = new Text(c_serial, SWT.BORDER);
			serial.setText(dev.getSerial());
			serial.setEditable(edit_serial);
			serial.setMessage("Serial");
			if(serial.getText().isEmpty()) serial.setBackground(ress.getColor(Ressource.COLOR_RED));
			
			if(edit_serial)
			{
				serial.setLayoutData(new GridData(125,15));
				Button b = new Button(c_serial, SWT.PUSH);
				b.setText("N/C");
				b.addListener(SWT.Selection, e -> {
					int rand = ThreadLocalRandom.current().nextInt(1000000, 9999999);
					serial.setText("NC" + rand);
				});
			} else
			{
				serial.setLayoutData(new GridData(textWidth,15));
			}
			
			Combo owner = new Combo(deviceContainer, SWT.NONE);
			owner.setItems(owners);
			owner.setLayoutData(new GridData(comboWidth,15));
			int nbOwners = owners.length;
			for(int i = 0; i < nbOwners; i++)
			{
				if(owners[i].equals(dev.getOwner()))
				{
					owner.select(i);
					break;
				}
			}
			
			Text clientId = new Text(deviceContainer, SWT.BORDER);
			clientId.setText(dev.getClientId());
			clientId.setLayoutData(new GridData(textWidth,15));
			if(clientId.getText().isEmpty()) clientId.setBackground(ress.getColor(Ressource.COLOR_BLUE));
			clientId.setMessage("Id Client");
			
			Text comment = new Text(deviceContainer, SWT.BORDER);
			comment.setText(dev.getComment());
			comment.setLayoutData(new GridData(textWidth,15));
			if(comment.getText().isEmpty()) comment.setBackground(ress.getColor(Ressource.COLOR_BLUE));
			comment.setMessage("Commentaire");
			
			Button valid = null;
			Button cancel = null;
			Composite buttons = new Composite(deviceContainer, SWT.NONE);
			{
				buttons.setLayout(new GridLayout(2, false));
				
				if(dev.getModel().isEmpty() || dev.getSerial().isEmpty() || dev.getClientId().isEmpty() || dev.getComment().isEmpty()) {
					valid = new Button(buttons, SWT.PUSH);
					valid.setText("Valider");
					valid.setLayoutData(new GridData(80, 20));
					valid.addListener(SWT.Selection, e -> {
						dev.setSerial(serial.getText().toUpperCase());
						dev.setModel(model.getText());
						dev.setOwner(owner.getText());
						dev.setClientId(clientId.getText());
						dev.setComment(comment.getText());
						events.action_validDevice(dev);
					});
					cancel = new Button(buttons, SWT.PUSH);
					cancel.setText("Supprimer");
					cancel.setLayoutData(new GridData(80, 20));
					cancel.addListener(SWT.Selection, e -> {
						locationDevices.remove(dev);
						deviceContainer.dispose();
						events.clearLastScan();
						events.action_remove(dev);
					});
				}
			}
			
			
			PanelDevice panelDevice = new PanelDevice(deviceContainer, model, serial, owner, clientId, comment, valid, cancel);
			locationDevices.put(dev, panelDevice);
			
		}
		devices.setSize(devices.computeSize(SWT.DEFAULT, SWT.DEFAULT));
		devices.layout();
		scrolledDevices.setOrigin(0, 9999);
		
	}
	
	public void writeClientId(final Device target, final String clientId)
	{
		PanelDevice panelDevice = locationDevices.get(target);
		panelDevice.getIn_clientId().setText(clientId);
		panelDevice.getIn_clientId().setBackground(ress.getColor(Ressource.COLOR_GREEN));
		//panelDevice.getB_valid().dispose();
		devices.setSize(devices.computeSize(SWT.DEFAULT, SWT.DEFAULT));
		devices.layout();
	}
	
	public void validDevice(final Device target)
	{
		PanelDevice panelDevice = locationDevices.get(target);
		
		panelDevice.getIn_model().setBackground(ress.getColor(Ressource.COLOR_GREEN));
		panelDevice.getIn_model().setEnabled(false);
		panelDevice.getIn_serial().setBackground(ress.getColor(Ressource.COLOR_GREEN));
		panelDevice.getIn_serial().setEnabled(false);
		panelDevice.getIn_owner().setBackground(ress.getColor(Ressource.COLOR_GREEN));
		panelDevice.getIn_owner().setEnabled(false);
		panelDevice.getIn_clientId().setBackground(ress.getColor(Ressource.COLOR_GREEN));
		panelDevice.getIn_clientId().setMessage("");
		panelDevice.getIn_clientId().setEnabled(false);
		panelDevice.getIn_comment().setBackground(ress.getColor(Ressource.COLOR_GREEN));
		panelDevice.getIn_comment().setMessage("");
		panelDevice.getIn_comment().setEnabled(false);
		
		
		if(panelDevice.getB_valid() != null)
			panelDevice.getB_valid().dispose();
		
		devices.setSize(devices.computeSize(SWT.DEFAULT, SWT.DEFAULT));
		devices.layout();
	}
	
	public void loadLocation(final String location)
	{
		locationDevices.clear();
		ArrayList<Device> scanList = win.getCore().getScanList();
		for(Device dev : scanList){
			if(dev.getLocation().equals(location))
			{
				newDevice(dev, false);
				validDevice(dev);
			}
		}
		bqeLabel.setText(location);
		
		devices.setSize(devices.computeSize(SWT.DEFAULT, SWT.DEFAULT));
		devices.layout();
	}
	
	public void resetDevice()
	{
		for(Control c : devices.getChildren())
		{
			c.dispose();
		}
		
		devices.setSize(devices.computeSize(SWT.DEFAULT, SWT.DEFAULT));
		devices.layout();
	}
	

	
	
	public void fill()
	{
		setTree();
		
		lbl_conf.setText("Date des paramètres: " + win.getCore().getConfDate());
		lbl_conf.pack();
		info.layout(true, true);
		
		port.setItems(win.getCore().getScanHandler().getPorts());
		port.select(0);
		
		defaultOwner.setItems(win.getCore().getOwners());
		defaultOwner.select(0);
		
		//Set focus to the fist input
		
	}
	
	private void setTree()
	{
		Node nlocations = null;
		try {
			nlocations = win.getCore().getXmlRootNode("locations");
		} catch (ParserConfigurationException | SAXException | IOException e) {
			e.printStackTrace();
			win.showError(e.getClass().getName(), e.getMessage());
		}
		
		NodeList cloc = nlocations.getChildNodes();
		Element root = null;
		for(int i = 0 ; i < cloc.getLength(); i++)
		{
			if(cloc.item(i) instanceof Element)
			{
				Element elem = (Element)cloc.item(i);
				if( (elem.getAttribute("value")).equals("NCE") )
				{
					root = elem;
				}
			}
		}
		
		TreeItem tiroot = new TreeItem(locations, SWT.NONE);
		tiroot.setText((root).getAttribute("value"));
		
		setTreeChild(root, tiroot);

	}
	private void setTreeChild(Node node, TreeItem tiParent)
	{
		if(node instanceof Element)
		{	
			Element elem = (Element)node;
			TreeItem ti = tiParent;
			if( ! (elem.getAttribute("value")).equals("NCE")) {
				ti = new TreeItem(tiParent, SWT.NONE);
				ti.setText(elem.getAttribute("value"));
			}
	
			NodeList children = elem.getChildNodes();
			if( ! elem.hasChildNodes())
				ti.setData(true);
			else ti.setData(false);
				
			int nbChildren = children.getLength();
			for(int i = 0; i < nbChildren; i++)
			{
				setTreeChild(children.item(i),ti);
				
			}
			
		}
	}
	
	public final Tree getLocations()
	{
		return locations;
	}
	public final MainPanelEvents getEvents()
	{
		return events;
	}
	public final Label getBqeLabel()
	{
		return bqeLabel;
	}
	public final Combo getPort()
	{
		return port;
	}
	public final Button getPortButton()
	{
		return portButton;
	}
	public final Button getPortRefresh()
	{
		return portRefresh;
	}
	public final Button getSaveButton()
	{
		return save;
	}
	public final Button getManualAdd()
	{
		return manualAdd;
	}
	public final Label getStatus()
	{
		return status;
	}
	public final Combo getDefaultOwner()
	{
		return defaultOwner;
	}
	public Label getLabelSaveStatus()
	{
		return labelSaveStatus;
	}

}
