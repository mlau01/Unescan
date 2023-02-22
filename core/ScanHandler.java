package unescan.core;
import java.util.HashMap;
import java.util.regex.PatternSyntaxException;

import jssc.SerialPort;
import jssc.SerialPortEventListener;
import jssc.SerialPortException;
import jssc.SerialPortList;

public class ScanHandler {
	
	public static final int ACTION_NOTHING = 0;
	public static final int ACTION_USCYES = 1;
	public static final int ACTION_USCNO = 2;
	public static final int ACTION_USCPREVLOC = 3;
	public static final int ACTION_USCNEXTLOC = 4;
	public static final int ACTION_NEWDEV = 5;
	public static final int ACTION_SETLOC = 6;
	public static final int ACTION_SETCLIID = 7;
	public static final int ACTION_UNKNOWN = 8;
	
	private final HashMap<String, String> models;
	private SerialPort serialPort;
	private final Core core;
	private final int verboseLevel = 0;
	
	public ScanHandler(final Core p_core)
	{
		core = p_core;
		models = core.getModelMap();
		
	}
	
	public void connect(final String port) throws SerialPortException
	{
		serialPort = new SerialPort(port);
		
	    serialPort.openPort();
	    serialPort.setParams(SerialPort.BAUDRATE_9600,
	                         SerialPort.DATABITS_8,
	                         SerialPort.STOPBITS_1,
	                         SerialPort.PARITY_NONE);

	    serialPort.setFlowControlMode(SerialPort.FLOWCONTROL_RTSCTS_IN | 
	                                  SerialPort.FLOWCONTROL_RTSCTS_OUT);
	}

	public void addListener(final SerialPortEventListener listener) throws SerialPortException
	{
		serialPort.addEventListener(listener, SerialPort.MASK_RXCHAR);
	}
	
	public void close() throws SerialPortException
	{
		if(serialPort != null)
		{
			serialPort.closePort();
		}
	}
	
	public final SerialPort getPort()
	{
		return serialPort;
	}
	
	public final String[] getPorts()
	{
		return SerialPortList.getPortNames();
	}
	
	public final int parseCode(String code) throws PatternSyntaxException
	{
		if(verboseLevel >= 2) System.out.println(this.getClass().getName() + " -> parsing received code: " + code);
		
		
		if(code.startsWith("USC-")) {
			char c = code.charAt(4);
			if(Character.isDigit(c)) {
				return Integer.parseInt(String.valueOf(c));
			}
		}
		
		if(code.length() == 6) return ACTION_SETCLIID;
		
		for(String model : models.keySet())
		{
			String spattern = models.get(model);
			if(verboseLevel >= 2) System.out.println(this.getClass().getName() + " -> Compiling regex: " + spattern);
			
			if( ! spattern.isEmpty() && code.matches(spattern.toUpperCase()))
				return ACTION_NEWDEV;
		}
		
		if(code.matches("T[1-2]-..-..*")) return ACTION_SETLOC;
	
		return ACTION_UNKNOWN;
	}

}
