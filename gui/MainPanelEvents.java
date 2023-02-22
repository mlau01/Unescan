package unescan.gui;

import java.io.IOException;
import java.util.regex.PatternSyntaxException;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;

import jssc.SerialPortEvent;
import jssc.SerialPortEventListener;
import jssc.SerialPortException;
import unescan.LANG;
import unescan.core.Core;
import unescan.core.Device;
import unescan.core.ScanHandler;

public class MainPanelEvents implements SerialPortEventListener{
	
	private final Win win;
	private final Core core;
	private final MainPanel panel;
	private TreeItem currentLocation;
	private Device lastscan;
	private final short verboseLevel = 2;
	private final ScanHandler scanHandler;
	
	public MainPanelEvents(final MainPanel p_panel, final Win p_win)
	{
		win = p_win;
		panel = p_panel;
		core = win.getCore();
		scanHandler = win.getCore().getScanHandler();
	
	}
	
	public void createEvents()
	{
		panel.getLocations().addListener(SWT.Selection, e -> action_setCurrentLocation(((Tree)e.widget).getSelection()[0]));
		panel.getPortButton().addListener(SWT.Selection, e -> action_connectHandScan());
		panel.getPortRefresh().addListener(SWT.Selection, e -> action_refreshPort());
		
		panel.getSaveButton().addListener(SWT.Selection, e -> action_save());
		
		panel.getManualAdd().addListener(SWT.Selection, e -> {
			if(currentLocation != null && currentLocationIsValid()) {
				action_newManualDevice();
			}
		});
		
				
	}
	
	/**
	 * Save datas to a file
	 * @return True if the save succeed, false otherwise
	 */
	public boolean action_save()
	{
		if(lastscan != null)
		{
			win.showError(this.getClass().getName(), "Le dernier scan n'a pas été validé");
			return false;
		}
		FileDialog savefile = new FileDialog(win.getShell(), SWT.SAVE);
		savefile.setFilterExtensions(new String[] { "*.csv" });
		savefile.setOverwrite(true);
		String filepath = savefile.open();
		if (filepath == null) return false;
		
		try {
			core.save(filepath);
		} catch (IOException e) {
			win.showError(e.getClass().getName(), e.getMessage());
			e.printStackTrace();
			return false;
		}
		
		panel.getSaveButton().setEnabled(false);
		panel.getLabelSaveStatus().setText("");
		
		return true;
	}
	
	private void action_refreshPort()
	{
		panel.getPort().setItems(scanHandler.getPorts());
		panel.getPort().select(0);
	}
	
	private void action_connectHandScan()
	{
		String port = panel.getPort().getText();
		try {
			scanHandler.connect(port);
			scanHandler.addListener(this);
		} catch (SerialPortException e) {
			win.showError(e.getClass().getName(), e.getMessage());
			e.printStackTrace();
			return;
		}
		
		panel.getPort().setEnabled(false);
		panel.getPortButton().setText("En écoute...");
		panel.getPortButton().setEnabled(false);
		panel.getPortRefresh().setEnabled(false);
		panel.getStatus().setText("Sélectionner une localisation");
	}

	// ##############---- TREE LOCATION ----####################
	public void action_selectLocation(final String locationName)
	{
		Tree tree = panel.getLocations();
		TreeItem root = tree.getItem(0);
		browseTree(root, locationName);
	}
	
	private void browseTree(final TreeItem parent, final String locationName)
	{
		parent.setExpanded(true);
		for(TreeItem children : parent.getItems()) {
			if(children.getText().equals(locationName)) {
				action_setCurrentLocation(children);
				return;
			} else
			{
				browseTree(children, locationName);
			}
		}
	}
	
	public void action_setCurrentLocation(final TreeItem location)
	{
		if(verboseLevel > 0) System.out.println(this.getClass().getName() + " -> Set current location to: " + location.getText());
		
		//Valid the last scan if isn't
		if(lastscan != null && core.existsSerial(lastscan.getSerial()) == null)
				action_validDevice(lastscan);
		
		lastscan = null;
		panel.resetDevice();
		
		

		panel.getLocations().setSelection(location);
		currentLocation = location;
		if(currentLocationIsValid()) {
			panel.loadLocation(location.getText());
			panel.getStatus().setText("En attente de scan...");
		} else {
			panel.getBqeLabel().setText("Sélectionner une localisation");
			panel.getStatus().setText("Sélectionner une localisation");
		}
		
		panel.getBqeLabel().pack();
	}
	
	public void action_nextLocation()
	{
		if(currentLocation == null) return;
		TreeItem parent = currentLocation.getParentItem();
		if(parent == null) return;
		TreeItem[] items = parent.getItems();
		for(int i=0; i < items.length; i++)
		{
			if(currentLocation == items[i]){
				if(i + 1 < items.length)
				{
					if(verboseLevel > 0) System.out.println(this.getClass().getName() + " -> Next location...");
					action_setCurrentLocation(items[i+1]);
				}
				return;
			}
		}
	}
	
	public void action_prevLocation()
	{
		if(currentLocation == null) return;
		TreeItem parent = currentLocation.getParentItem();
		if(parent == null) return;
		TreeItem[] items = parent.getItems();
		for(int i=0; i < items.length; i++)
		{
			if(currentLocation == items[i]){
				if(i - 1 >= 0)
				{
					if(verboseLevel > 0) System.out.println(this.getClass().getName() + " -> Prev location...");
					action_setCurrentLocation(items[i-1]);
					
				}
				return;
			}
		}
	}
	
	public final boolean currentLocationIsValid()
	{
		if(currentLocation != null) {
			if((boolean)currentLocation.getData()) return true;
		}
		
		return false;
	}
	
	public final TreeItem getCurrentLocation()
	{
		return currentLocation;
	}
	
	//##########---- DEVICE MANAGING ----#######################
	private final boolean pretest_newDevice(final String serial)
	{
		//Test location valid
		if(! currentLocationIsValid()) {
			win.showError(this.getClass().getName(), "Localisation invalide");
			return false;
		}
		
		//Valid the last scan if isn't
		if(lastscan != null)
		{
			if(action_validDevice(lastscan) == null) return false;
		}
		
		//If current scan was already scanned, ask to replace
		Device exists = core.existsSerial(serial);
		if(exists != null)
		{
			if(ask_remove(exists)) action_remove(exists);
			else return false;
		}
		return true;
	}
	
	private void action_newUnknownDevice(final String serial)
	{
		if( ! currentLocationIsValid()) return;
		if(ask_unknown(serial)) action_newAutoDevice(serial);
	}
	
	private void action_newAutoDevice(final String serial)
	{
		if( ! pretest_newDevice(serial)) return;
		
		//Getting model for corresponding serial
		String model = core.getModel(serial);
		
		//Create the bean
		String defaultOwner = panel.getDefaultOwner().getText();
		Device newDev = new Device(currentLocation.getText(), serial, model, defaultOwner, "", "");
		
		lastscan = newDev;

		//Create the device in the GUI
		panel.newDevice(newDev, false);

	}
	
	private void action_newManualDevice()
	{
		//Test location is valid
		if(! currentLocationIsValid()) return;
		
		//Valid the last scan if isn't
		if(lastscan != null)
		{
			if(action_validDevice(lastscan) == null) return;
		}
		
		//Create the new device bean
		String defaultOwner = panel.getDefaultOwner().getText();
		Device newDev = new Device(currentLocation.getText(), "", "", defaultOwner, "", "");
		
		//Set the lastscan
		lastscan = newDev;
		
		//Create GUI device and let all input open
		panel.newDevice(newDev, true);
	}
	
	public void clearLastScan()
	{
		lastscan = null;
	}
	
	public void action_remove(final Device exists)
	{
		core.removeDevice(exists);
		panel.resetDevice();
		panel.loadLocation(currentLocation.getText());
	}
	
	public final Device action_validDevice(Device dev)
	{
		String location = dev.getLocation();
		String model = dev.getModel();
		String serial = dev.getSerial();
		String owner = dev.getOwner();
		
		if(location.isEmpty() ||model.isEmpty() ||serial.isEmpty() || owner.isEmpty()) 
		{
			win.showError(this.getClass().getName(), "Un ou plusieurs champ requis manquant pour la validation");
			return null;
		}
		
		if(verboseLevel > 1) System.out.println(this.getClass().getName() + " -> Validing device: " + model + "(" + serial + ")");
		//Test serial en cas d'ajout manuel
		Device exists = core.existsSerial(serial);
		if(exists != null) {
			win.showError("Machine existante", "Cette machine a déjà été ajouter: " + exists.getLocation() + " - " + exists.getModel());
			return null;
		}
		panel.validDevice(dev);
		panel.getSaveButton().setEnabled(true);
		panel.getLabelSaveStatus().setText(LANG.LBL_SAVE_STATUS_UNSAVED.get());
		panel.getLabelSaveStatus().pack();
		core.writeDevice(dev);
		lastscan = null;
		return dev;
	}
	
	private final boolean ask_remove(final Device exists)
	{
		 MessageBox messageBox = new MessageBox(win.getShell(), SWT.APPLICATION_MODAL | SWT.ICON_WARNING | SWT.OK | SWT.CANCEL);
	        messageBox.setText("Information");
	        messageBox.setMessage("Ce numéro de série à déjà été scanné pour: " + exists.getLocation() + ", Remplacer ?");
	        if(messageBox.open() == SWT.OK) return true;
	        
	        return false;
	}
	
	private final boolean ask_unknown(final String serial)
	{
	
		 MessageBox messageBox = new MessageBox(win.getShell(), SWT.APPLICATION_MODAL | SWT.ICON_WARNING | SWT.OK | SWT.CANCEL);
	     messageBox.setText("Serial inconnu");
	     messageBox.setMessage("Ce numéro de série n'est pas reconnu: " + serial + ", Ajouter quand même ?");
	     if(messageBox.open() == SWT.OK) return true;
	        
	     return false;
		
	}
	
	private void message_duplicateClientId(final Device original)
	{
		 MessageBox messageBox = new MessageBox(win.getShell(), SWT.APPLICATION_MODAL | SWT.ICON_ERROR | SWT.OK);
	        messageBox.setText("Erreur");
	        messageBox.setMessage("Ce client id est déjà attribué à la machine: " + original.getLocation() + " - " + original.getModel() + " - " + original.getSerial());
	        messageBox.open();
	}
	
	private void action_setAutoClientId(final String clientId)
	{
		Device exists = core.existsClientId(clientId);
		if(exists != null)
		{
			message_duplicateClientId(exists);
			return;
		}
		if(lastscan != null) {
			if(verboseLevel > 1) System.out.println(this.getClass().getName() + " -> Setting client id to last scan");
			lastscan.setClientId(clientId);
			panel.writeClientId(lastscan, clientId);
			action_validDevice(lastscan);
		}
		else {
			win.showError(this.getClass().getName(), "Client ID: " + clientId + " reconnu mais aucun périphérique scanné pour association");
		}
	}

	
	@Override
	public void serialEvent(SerialPortEvent event) {
		if(event.isRXCHAR() && event.getEventValue() > 0) {
			String receivedData = null;
			try {
                receivedData = scanHandler.getPort().readString(event.getEventValue());
		    }
            catch (SerialPortException | PatternSyntaxException ex) {
            	win.showError(ex.getClass().getName(), ex.getMessage());
            	return;
            }
			
			if(Integer.valueOf(receivedData.charAt(0)) != 30) {
				win.showError(this.getClass().getName(), "Douchette non Crews, Veuillez utiliser une douchette configuré Crews");
				return;
			}
			
        	//Cutting data
            if(verboseLevel > 0) System.out.println(this.getClass().getName() + " -> Raw datas: " + receivedData);
       
            final String cuttedData = receivedData.substring(3, receivedData.length() -2).toUpperCase();
            win.getDisplay().asyncExec(new Runnable(){

				@Override
				public void run() {
					 int response = win.getCore().getScanHandler().parseCode(cuttedData);
					 if(verboseLevel > 0) System.out.println(this.getClass().getName() + " -> Cutted datas: " + cuttedData);
					 if(verboseLevel > 0) System.out.println(this.getClass().getName() + " -> Response code: " + response);
		                switch(response)
		    			{
		    				case ScanHandler.ACTION_USCYES :
		    					
		    				break;
		    				case ScanHandler.ACTION_USCNO :
		    					
		    				break;
		    				case ScanHandler.ACTION_USCNEXTLOC :
		    					action_nextLocation();
		    				break;
		    				case ScanHandler.ACTION_USCPREVLOC :
		    					action_prevLocation();
		    				break;
		    				case ScanHandler.ACTION_NEWDEV :
		    					action_newAutoDevice(cuttedData);
		    				break;
		    				case ScanHandler.ACTION_SETLOC :
		    					action_selectLocation(cuttedData);
		    				break;
		    				case ScanHandler.ACTION_SETCLIID :
		    					action_setAutoClientId(cuttedData);
		    				break;
		    				case ScanHandler.ACTION_UNKNOWN :
		    					action_newUnknownDevice(cuttedData);
		    				break;
		    			}
					
				}
            	
            });
               
     
		}
	}

}
