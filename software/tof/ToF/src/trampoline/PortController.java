/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package trampoline;

import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;
import java.io.*;
import java.util.*;

/**
 *
 * @author Kieran
 */
public class PortController implements SerialPortEventListener{
    
    private String errorList_[] = {
                            "",
                            "ToF not located on any COM Port.",
                            "Error opening communications with ToF.",
                            "Error getting data from ToF.",
                            "Error clearing cache on COM Port.",
                            "Error settings file not located.",
                            "General IO error with a file.",
                            "Error opening output XML file.",
                            "Error writing bounce data to file.",
                            "Error with order of break/engage data coming from device."};
    
    private ArrayList<CommPortIdentifier> portsInUse_;  // The ports ToFs have been found on.
    private ArrayList<String> nameOfPorts_;             // The name of each ToF.
    private ArrayList<Integer> noOfTof_;                // The number of Tof on each port.
    private TofInterface tofInterfaces_[];              // Interfaces to each Tof device.
    private int error_;                                 // Error Flag.
    private int timeOut_;                               // Milliseconds to block while waiting for port open.
    private int dataRate_;                              // Default bits per second for COM port.
    private SerialPort serialPort_;                     // Serial Port associated with this object.
    private InputStream input_;                         // Buffered input stream from the port.
    private OutputStream output_;                       // The output stream to the port.
    private String portOpen_;                           // Name of Serial Port open.
    private ErrorHandler errorHandler_;                 // Error Handler inherited from main project.
    
    PortController(ErrorHandler errHandl){
        this.portsInUse_ = new ArrayList<CommPortIdentifier>();
        this.nameOfPorts_ = new ArrayList<String>();
        this.noOfTof_ = new ArrayList<Integer>();
        this.error_ = 0;
        this.serialPort_ = null;
        this.input_ = null;
        this.output_ = null;
        this.portOpen_ = null;
        this.tofInterfaces_ = null;
        this.errorHandler_ = errHandl;
        
        this.readSettings();
        this.listPorts();
    }
    
    PortController(ErrorHandler errHandl, String portName){
        this(errHandl);
        this.initialise(portName);
    }
    
    private void readSettings(){
        File settingsfile = new File("CommPortSettings.conf");
        BufferedReader buffRead = null;            
        try {
            buffRead = new BufferedReader(new FileReader(settingsfile));
            String line = null;

            while ((line = buffRead.readLine()) != null) {
                String[] split = line.split("=");

                if(split[0].equals("datarate")){
                    this.dataRate_ = Integer.parseInt(split[1]);
                }

                if(split[0].equals("timeout")){
                    this.timeOut_= Integer.parseInt(split[1]);
                }
            }
        } catch (FileNotFoundException e) {
            this.error_ = 5;
            this.errorList_[0] = e.toString();
        } catch (IOException e) {
            this.error_ = 6;
            this.errorList_[0] = e.toString();
        } finally {
            try {
                if (buffRead != null){
                    buffRead.close();
                }
            } catch (IOException e) {
                this.error_ = 6;
                this.errorList_[0] = e.toString();
            }
        }
    }
    
    /**
    * This method will fill the instance variables with all
    * ports which ToF devices are connected on.
    */
    private void listPorts(){
        /* List all ports on PC */
        Enumeration portEnum = CommPortIdentifier.getPortIdentifiers();

        //iterate through them, looking for ToFs
        while(portEnum.hasMoreElements()){
            CommPortIdentifier currPortId = (CommPortIdentifier) portEnum.nextElement();

            String s = this.handshake(currPortId);
 
            if(!(s.equals("Failed"))){      
                //Add it to our lists
                String split[] = s.split(" ");                               
                this.portsInUse_.add(currPortId);
                this.nameOfPorts_.add(split[1]);
                this.noOfTof_.add(Integer.parseInt(split[2]));
            }
        }

        if(this.portsInUse_.isEmpty()){
            this.error_ = 1;
            this.errorList_[0] = "ToF not Found.";
        }
    }
    
    private String handshake(CommPortIdentifier thisComm){
        String result = "";
        SerialPort serialPort  = null;
        try{
           serialPort = (SerialPort) thisComm.open(this.getClass().getName(),timeOut_);
           serialPort.setSerialPortParams(this.dataRate_,
                                SerialPort.DATABITS_8,
                                SerialPort.STOPBITS_1,
                                SerialPort.PARITY_NONE);
            
            InputStream input = serialPort.getInputStream();
            OutputStream output = serialPort.getOutputStream();
            Handshake handshakeListener = new Handshake(input);
            // add event listeners
            this.serialPort_.addEventListener(handshakeListener);
            this.serialPort_.notifyOnDataAvailable(true);
                                   
            Thread.sleep(1500);
            String messageString = "TOF\r\n";
            output.write(messageString.getBytes()); 
            
            while(result.equals("")){
                result = handshakeListener.getResponse();
            }
            
            String split[] = result.split(" ");
            
            if(!(split[0].equals("TOF"))){
                result = "Failed";
            }
            
            serialPort.removeEventListener();
            serialPort.close();
            result = "Failed";
        }catch (Exception e) {
            result = "Failed";

            if(serialPort!=null){
                serialPort.close();
            }
        }
        return result;
    }
    
    
    public void initialise(String portName) {
        int index = this.nameOfPorts_.indexOf(portName);
        CommPortIdentifier portId = this.portsInUse_.get(index);

        try {
            // open serial port, and use class name for the appName.
            this.serialPort_ = (SerialPort) portId.open(this.getClass().getName(),timeOut_);

            // set port parameters
            this.serialPort_.setSerialPortParams(this.dataRate_,
                                SerialPort.DATABITS_8,
                                SerialPort.STOPBITS_1,
                                SerialPort.PARITY_NONE);

            // open the streams
            this.input_ = this.serialPort_.getInputStream();
            this.output_ = this.serialPort_.getOutputStream();

            // add event listeners
            this.serialPort_.addEventListener(this);
            this.serialPort_.notifyOnDataAvailable(true);
            this.portOpen_ = portName;
        } catch (Exception e) {
            this.errorList_[0] = e.toString();
            this.error_ = 2;
        }
        
        //Intialise Tof Interfaces for all Tof on the port
        this.tofInterfaces_ = new TofInterface[this.noOfTof_.get(index)];        
        for(int i=0;i<this.noOfTof_.get(index);i++){
            this.tofInterfaces_[i] = new TofInterface(this.errorHandler_, this);
        }
    }
    
    public synchronized void serialEvent(SerialPortEvent oEvent) {
        if (oEvent.getEventType() == SerialPortEvent.DATA_AVAILABLE) {
            try{   
                StringBuilder tempStr = new StringBuilder();

                byte chunk = (byte)this.input_.read();
                while((char)chunk!='\n'){
                    tempStr.append((char)chunk);
                    chunk = (byte)this.input_.read();
                }

                tempStr.deleteCharAt(tempStr.length()-1);

                String inputString = tempStr.toString();
                String[] split = inputString.split(" ");
                int tofId;
                int time;
                int broken;

                tofId = Integer.parseInt(split[0]);
                time = Integer.parseInt(split[1]);
                broken = Integer.parseInt(split[2]);
                
                if(tofId < this.tofInterfaces_.length){
                    this.tofInterfaces_[tofId-1].receiveBounce(time, broken);
                }
            }catch (Exception e){
                this.errorList_[0] = e.toString();
                this.error_ = 3;
            }
        }
    // Ignore all the other eventTypes, but you should consider the other ones.
    }
    
    public synchronized void clearBuffer(){
        try{
            int available = this.input_.available();
            byte chunk[] = new byte[available];
            this.input_.read(chunk,0,available);
        } catch (Exception e){
            this.errorList_[0] = e.toString();
            this.error_ = 4;
        }
    }
    
    /**
    * This should be called when you stop using the port.
    * This will prevent port locking on platforms like Linux.
    */
    public synchronized void close() {
            if (this.serialPort_ != null) {
                    this.serialPort_.removeEventListener();
                    this.serialPort_.close();
            }
    }
    
    public int getError(){
        return this.error_;
    }

    public String[] getErrorList(){
        return this.errorList_;
    }

    public ArrayList<String> getPorts(){
        return this.nameOfPorts_;
    }
    
    public ArrayList<Integer> getNoTof(){
        return this.noOfTof_;
    }
    
    public TofInterface getInterface(int id){
        return this.tofInterfaces_[id];
    }
    
    public String getCurrentPort(){
        return this.portOpen_;
    }
}
