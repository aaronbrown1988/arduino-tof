/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nob;

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
    private int beamStatus_[];                  // Status of 3 beams in the ToF
    private int noToCollect_;                   // Flag for collecting data.
    private int nextType_;                      // Next type of event expected (break/engage)
    private BufferedWriter outputBuff_;         // File to write XML out to.
    private PortController myPort_;             // Refernce to the PortController which owns this TofInterface
    private int jumpData_[];                    // Jump data to be converted into a jump object.
    private ArrayList<Jump> jumpEvents_;        // Array to hold calculated jumps
    
    TofInterface(){
        this.beamStatus_ = new int[3];
        this.beamStatus_[0] = 0;
        this.beamStatus_[1] = 0;
        this.beamStatus_[2] = 0;
        
        this.jumpData_ = new int[3];
        this.jumpData_[0] = -1;
        this.jumpData_[1] = -1;
        this.jumpData_[2] = -1;
        
        this.error_ = 0;
        this.noToCollect_ = 0;
        this.nextType_ = 1;
        this.outputBuff_ = null;
        this.jumpEvents_ = new ArrayList<Jump>();
        
    }
    
    TofInterface(PortController myPort){
        this();
        this.myPort_ = myPort;
    }
    
    public void setMyPort(PortController myPort){
        this.myPort_ = myPort;
    }
    
    public void receiveBounce(int time, int broken){
        if(this.noToCollect_!=0){ 
            if(this.nextType_ == broken){
                this.noToCollect_--;
                this.nextType_ = (this.nextType_ +1)%2;
                this.write(time,broken);
            }else{
                this.errorList_[0] = "Device out of sync";
                this.error_ = 9;
            }
        }else{
        }
        this.beamStatus_[0] = broken;
        this.beamStatus_[1] = broken;
        this.beamStatus_[2] = broken;
    }
    
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
                
        this.myPort_.clearBuffer();       
        
        this.jumpData_[0] = -1;
        this.jumpData_[1] = -1;
        this.jumpData_[2] = -1;
        this.jumpEvents_.clear();
        this.nextType_ = 1;
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
            Jump thisJump = new Jump(this.jumpData_[0], this.jumpData_[1], this.jumpData_[2]);
            this.jumpEvents_.add(thisJump);
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
    
    public int[] getBeamStatus(){
        return this.beamStatus_;
    }
    
    public ArrayList<Jump> getJumps(){
        return this.jumpEvents_;
    }
    
    public PortController getPort(){
        return this.myPort_;
    }
}
