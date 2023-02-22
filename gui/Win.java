package unescan.gui;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.xml.sax.SAXException;

import jssc.SerialPortException;
import unescan.Unescan;
import unescan.core.Core;
public class Win {
	
	private final Display display;
	private final Shell shell;
	private MainPanel mainPanel;
	private Listener closeListener, disposeListener;
	private final Ressource ress;
	private Core core;
	

	public Win()
	{
		display = new Display();
		shell = new Shell(display, SWT.SHELL_TRIM);
		loadListeners();
		shell.addListener(SWT.Dispose, disposeListener);
		shell.addListener(SWT.Close, closeListener);
		
		ress = new Ressource(this);
		
		shell.setText(Unescan.PROJECT_NAME.substring(0, 1).toUpperCase() + Unescan.PROJECT_NAME.substring(1) + " " + Unescan.PROJECT_VERSION);
		shell.setImage(ress.getImage(Ressource.IMAGE_ICON));
		
		try {
			core = new Core();
		} catch (ParserConfigurationException | SAXException | IOException e) {
			e.printStackTrace();
			showError(e.getClass().getName(), e.getMessage());
		}
		
		createWidgets();
	
		shell.pack();
		shell.open();
		
		fill();
		
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
	}
	
	private void createWidgets()
	{
		shell.setLayout(new FillLayout());
		mainPanel = new MainPanel(this);
		mainPanel.createWidgets(shell);
		mainPanel.getEvents().createEvents();
	}
	
	private void loadListeners()
	{
		closeListener = e -> {
			if(core.isModified()) {
		        MessageBox messageBox = new MessageBox(shell, SWT.APPLICATION_MODAL | SWT.ICON_WARNING | SWT.YES | SWT.NO | SWT.CANCEL);
		        messageBox.setText("Information");
		        messageBox.setMessage("Sauvegarder les changements avant de quitter ?");
		        int IDButton = messageBox.open();
		        switch(IDButton)
		        {
			        case SWT.YES :
			        	if( ! mainPanel.getEvents().action_save()) e.doit = false;
			        	
			        break;
			        case SWT.NO :
			        	e.doit = true;
			        break;
			        case SWT.CANCEL :
			        	e.doit = false;
			        break;
		        }
			}
		};
		
		disposeListener = e -> {
			ress.disposeAll();
			try {
				core.dispose();
			} catch (SerialPortException | IOException e1) {
				e1.printStackTrace();
				showError(e1.getClass().getName(), e1.getMessage());
			}
		};
	}
	
	private void fill()
	{
		mainPanel.fill();
	}
	
	public final void showError(final String title, final String message)
	{
		final MessageBox info = new MessageBox(shell, SWT.ERROR | SWT.OK);
		
		info.setText(title);
		info.setMessage(message);
		info.open();
	}
	
	public static void main(String[] args)
	{
		new Win();
	}
	
	public final Core getCore()
	{
		return core;
	}
	

	public final MainPanel getMainPanel()
	{
		return mainPanel;
	}
	
	public final Display getDisplay()
	{
		return display;
	}
	
	public final Shell getShell()
	{
		return shell;
	}
	
	public final Ressource getRessource()
	{
		return ress;
	}

}
