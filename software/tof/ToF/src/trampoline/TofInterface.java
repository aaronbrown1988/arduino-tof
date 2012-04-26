/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package trampoline;

import java.io.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 *
 * @author Kieran
 */
public class TofInterface {
    
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
    private int error_;                         // Error Flag.
    private boolean beamStatus_[];              // Status of 8 beams in the ToF
    private int noToCollect_;                   // Flag for collecting data.
    private boolean gridStatus_;                // Status of grid
    private BufferedWriter outputBuff_;         // File to write XML out to.
    private PortController myPort_;             // Refernce to the PortController which owns this TofInterface
    private int jumpData_[];                    // Jump data to be converted into a jump object.
    private Jump jumps_[];                      // Array to hold calculate jumps
    private int noJumpsInArray_;                 // Number of jumps currently in array
    
    TofInterface(){
        this.beamStatus_ = new boolean[8];
        this.beamStatus_[0] = false;
        this.beamStatus_[1] = false;
        this.beamStatus_[2] = false;
        this.beamStatus_[3] = false;
        this.beamStatus_[4] = false;
        this.beamStatus_[5] = false;
        this.beamStatus_[6] = false;
        this.beamStatus_[7] = false;
        
        this.jumpData_ = new int[3];
        this.jumpData_[0] = -1;
        this.jumpData_[1] = -1;
        this.jumpData_[2] = -1;
        
        this.error_ = 0;
        this.noToCollect_ = 0;
        this.gridStatus_ = true;
        this.outputBuff_ = null;
        this.jumps_ = new Jump[21];
        this.noJumpsInArray_ = 0;
    }
    
    TofInterface(PortController myPort){
        this();
        this.myPort_ = myPort;
    }
    
    public void setMyPort(PortController myPort){
        this.myPort_ = myPort;
    }
    
    public void receiveBounce(int time, int broken){
        String strBeamStatus = Integer.toBinaryString(broken);
        char[] chrBeamStatus = strBeamStatus.toCharArray();

        if(this.noToCollect_!=0){ 
            if(this.gridStatus_==true && broken!=255){
                //grid was intact and is now broken
                this.noToCollect_--;
                this.write(time,broken);
            }else if(this.gridStatus_==false && broken==255){
                //grid was broken and now is intact
                this.noToCollect_--;
                this.write(time,broken);
                
                //USE THE ARRAY OF BEAM BREAK ORDERS TO CALCULATE THE POSITION TO SEND WITH THE WRITE
            }else{
                //INPUT CODE HERE TO DETERMINE THE POSITION
                // ADD TO AN ARRAY OF INTS THE ORDER WHICH THE BEAMS WERE BROKEN THEN RECONNECTED
            }
        }
            
        this.gridStatus_ = (broken==255);    
       
        for(int i=0;i<7;i++){
            this.beamStatus_[i] = (chrBeamStatus[i]=='1');
        }
    }
    
     public void collectBounces(int noOfBounces, String filename, String passName){
        
         //REWRITE WITH DATABASE
         
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
           
        
        
        
        
        
        
        
        
        
        this.myPort_.clearBuffer();       
        this.jumpData_[0] = -1;
        this.jumpData_[1] = -1;
        this.jumpData_[2] = -1;
        this.noJumpsInArray_ = 0;
        this.noToCollect_ = noOfBounces*2 + 1;
    }
     
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
            this.jumps_[this.noJumpsInArray_] = new Jump(this.jumpData_[0], this.jumpData_[1], this.jumpData_[2],"A0");
            this.noJumpsInArray_++;
            this.jumpData_[0] = this.jumpData_[2];
            this.jumpData_[1] = -1;
            this.jumpData_[2] = -1;
        }
    }
    
     public int getNoOfBouncesRemaining(){
        return this.noToCollect_;
    }

    public int getError(){
        return this.error_;
    }

    public String[] getErrorList(){
        return this.errorList_;
    }
    
    public boolean[] getBeamStatus(){
        return this.beamStatus_;
    }
    
    public Jump[] getJumps(){
        return this.jumps_;
    }
    
    public PortController getPort(){
        return this.myPort_;
    }
}
