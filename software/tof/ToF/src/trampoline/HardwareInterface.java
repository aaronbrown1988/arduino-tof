package trampoline;

import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;
import java.io.*;
import java.util.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

/**
 *
 * @author Kieran Bhardwaj
 */

public class HardwareInterface implements SerialPortEventListener {
    /** List of Error Messages **/    
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
    private ArrayList<Jump> jumpEvents_;                // Array to hold calculated jumps
    private SerialPort serialPort_;                     // Serial Port associated with this object.
    private InputStream input_;                         // Buffered input stream from the port.
    private OutputStream output_;                       // The output stream to the port.
    private BufferedWriter outputBuff_;                 // File to write XML out to.
    private int timeOut_;                               // Milliseconds to block while waiting for port open.
    private int dataRate_;                              // Default bits per second for COM port.
    private int noToCollect_;                           // Flag for collecting data.
    private int jumpData_[];                            // Jump data to be converted into a jump object.
    private int error_;                                 // Error Flag.
    private int idOfTof_;                               // ID of the ToF to look for in breaks.
    private int nextType_;                              // Next type of event expected (break/engage)
    private int beamStatus_[];                          // Status of 3 beams in the ToF
    private String portOpen_;                           // Name of Serial Port open
    private String portsToCheck_[];                     // The port we're going to check for ToFs.


    HardwareInterface(){
        this.nameOfPorts_ = new ArrayList<String>();
        this.portsInUse_ = new ArrayList<CommPortIdentifier>();
        this.jumpEvents_ = new ArrayList<Jump>();
        this.noToCollect_ = 0;
        this.error_ = 0;
        this.idOfTof_ = 0;
        this.outputBuff_ = null;
        this.portOpen_ = null;
        this.nextType_ = 1;
        this.beamStatus_= new int[3];
        this.jumpData_ = new int[3];
        
        this.readSettings();
        this.listPorts();
    }
    
    HardwareInterface(String port){
        this();
        this.initialise(port);
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

                if(split[0].equals("portstocheck")){
                    this.portsToCheck_ = split[1].split(";");
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

            // If the port is on our list to check...
            for (String portName : this.portsToCheck_){

                //...And it passes the test (currently just matched the name!)
                if (currPortId.getName().equals(portName)){
                    //Add it to our lists
                    int noOfTof = 2;
                    if(noOfTof > 1){
                        for(int i=1; i<=noOfTof;i++){
                            this.portsInUse_.add(currPortId);
                            this.nameOfPorts_.add(currPortId.getName() + " Device "+i);
                        }
                    }else{
                        this.portsInUse_.add(currPortId);
                        this.nameOfPorts_.add(currPortId.getName());
                    }
                }
            }
        }

        if(this.portsInUse_.isEmpty()){
            this.error_ = 1;
            this.errorList_[0] = "ToF not Found.";
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

    private void initialise(String portName) {
        String split[] = portName.split(" ");
        CommPortIdentifier portId = this.portsInUse_.get(this.nameOfPorts_.indexOf(portName));
        
        if(split.length==3){
            this.idOfTof_ = Integer.parseInt(split[2]);
        }else{
            this.idOfTof_ = 1;
        }

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
    }



    /**
    * Handle an event on the serial port. Read the data and print it.
    */

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
                if(tofId == this.idOfTof_){
                    time = Integer.parseInt(split[1]);
                    broken = Integer.parseInt(split[2]);

                    if(this.noToCollect_!=0){ 
                        if(this.nextType_ == broken){
                            this.noToCollect_--;
                            this.nextType_ = (this.nextType_ +1)%2;
                            this.write(time,broken);
                        }else{
                            this.errorList_[0] = "Device out of sync";
                            this.error_ = 9;
                        }
                    }
                    this.beamStatus_[0] = broken;
                    this.beamStatus_[1] = broken;
                    this.beamStatus_[2] = broken;
                }
            }catch (Exception e){
                this.errorList_[0] = e.toString();
                this.error_ = 3;
            }
        }
    // Ignore all the other eventTypes, but you should consider the other ones.
    }
    
    /**
     * Method to write the bounce out in XML format and to the jump array
     * 
     * @param time
     * @param broken 
     */
    
    private void write(int time, int broken){
        try{
            StringBuilder tempStr = new StringBuilder();
            tempStr.append("\t<event>\n");
            tempStr.append("\t\t<time>");
            tempStr.append(time);
            tempStr.append("</time>\n");
            tempStr.append("\t\t<type>");
            
            if(broken==1){
                tempStr.append("break");
            }else{
                tempStr.append("engage");
            }
            
            tempStr.append("</type>\n");
            tempStr.append("\t</event>\n");
            
            this.outputBuff_.write(tempStr.toString());
        }catch(IOException e){
            this.error_ = 8;
            this.errorList_[0] = e.toString();
        }
        
        if(this.noToCollect_==0 && this.outputBuff_!=null){
            try{
                this.outputBuff_.write("</data>\n\n");
                this.outputBuff_.close();
            }catch(IOException e){
                this.error_ = 6;
                this.errorList_[0] = e.toString();
            }
        }
        
        if(this.jumpData_[0] == -1){
            this.jumpData_[0] = time;
        }else if(this.jumpData_[1] == -1){
            this.jumpData_[1] = time;
        }else if(this.jumpData_[2] == -1){
            this.jumpData_[2] = time;
            Jump thisJump = new Jump(this.jumpData_[0], this.jumpData_[1], this.jumpData_[2]);
            this.jumpEvents_.add(thisJump);
            this.jumpData_[0] = this.jumpData_[2];
            this.jumpData_[1] = -1;
            this.jumpData_[2] = -1;
        }
    }

    /**
    * Method to start the collection of bounce data.
    * 
    * @param idOfTof
    * @param noOfBounces 
    */

    public void collectBounces(int noOfBounces, String filename, String passName){
        try{
            FileWriter fstream = new FileWriter(filename,true);
            this.outputBuff_ = new BufferedWriter(fstream);
            
            StringBuilder tempStr = new StringBuilder();
            tempStr.append("<data>\n");
            DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
            Date date = new Date();
            tempStr.append("\t<timestamp>"+dateFormat.format(date)+"</timestamp>\n");
            
            if(passName.equals("")){
                tempStr.append("\t<passname> --No Name Provided -- </passname>\n");
            }else{
                tempStr.append("\t<passname>"+passName+"</passname>\n");
            }
            
            this.outputBuff_.write(tempStr.toString());
        }catch(IOException e){
            this.errorList_[0]=e.toString();
            error_ = 7;
        }
                
        try{
            int available = this.input_.available();
            byte chunk[] = new byte[available];
            this.input_.read(chunk,0,available);
            
        } catch (Exception e){
            this.errorList_[0] = e.toString();
            this.error_ = 4;
        }
        this.jumpData_[0] = -1;
        this.jumpData_[1] = -1;
        this.jumpData_[2] = -1;
        this.jumpEvents_.clear();
        this.nextType_ = 1;
        this.noToCollect_ = noOfBounces*2 + 1;
    }

    /**
    * GETTERS for variables
    */

    public int getNoOfBouncesRemaining(){
        return this.noToCollect_;
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
    
    public String getCurrentPort(){
        return this.portOpen_;
    }
    
    public int[] getBeamStatus(){
        return this.beamStatus_;
    }
    
    public ArrayList<Jump> getJumps(){
        return this.jumpEvents_;
    }
    
    public static void main(String[] args) throws Exception {
        HardwareInterface test = new HardwareInterface();
        ArrayList<String> ports = test.getPorts();
        test.initialise(ports.get(1));

        System.out.println(test.getError());
        System.out.println(test.getPorts());
        System.out.println(test.getNoOfBouncesRemaining());

        System.out.println("Started");
        test.collectBounces(10,"data/testfile.xml","testpass");	
    }
}
