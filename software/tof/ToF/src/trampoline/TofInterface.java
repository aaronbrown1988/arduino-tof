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
    private boolean beamStatus_[];              // Status of 8 beams in the ToF
    private int noOfBeamsOnDevice_;             // Number of beams active on device
    private int engagedStatus_;                 // integer for fullly engaged field
    private int noToCollect_;                   // Flag for collecting data.
    private boolean gridStatus_;                // Status of grid
    private PortController myPort_;             // Refernce to the PortController which owns this TofInterface
    private DBConnect db_;                      // Database object
    private Routine routine_;                   // Temp routine holder
    private Jump jump_;                         // Temp jump holder
    private int jumpData_[];                    // Jump data to be converted into a jump object.
    private int breakOrderShortSide_[];         // Array to list the beam breaks along short side.
    private int breakOrderLongSide_[];          // Array to list the beam breaks along long side.
    private int noBeamsBrokenShort_;            // Number of breaks in an event.
    private int noBeamsBrokenLong_;             // Number of breaks in an event.
    private int gymnast_;                       // ID of gymnast currently jumping.
    private int currentRoutineID_;              // ID of current routine
    private String lastLocation_;               // Location of the last jump.
    private MessageHandler messageHandler_;         // Error Handler inherited from main project.
    
    
    private int currentID_;                     // Bounce counter bits
    private int currentBounces_;
    
    TofInterface(MessageHandler errHandl, int noOfBeams){
        this.noOfBeamsOnDevice_ = noOfBeams;
        this.beamStatus_ = new boolean[8];
        for(int i=0;i<this.noOfBeamsOnDevice_;i++){
            this.beamStatus_[i] = true;
        }
        for(int i=this.noOfBeamsOnDevice_;i<8;i++){
            this.beamStatus_[i] = false;
        }
        
        for(int i=0; i<this.noOfBeamsOnDevice_;i++){
            this.engagedStatus_ += Math.pow(10,i);
        }

        this.noToCollect_ = 0;
        this.gridStatus_ = true;
        this.db_ = null;
        this.routine_ = null;
        this.jump_ = null;
        this.currentRoutineID_ = 0;
        
        this.lastLocation_ = "none";
        this.jumpData_ = new int[3];
        this.jumpData_[0] = -1;
        this.jumpData_[1] = -1;
        this.jumpData_[2] = -1;
        this.breakOrderShortSide_ = new int[3];
        for(int i=0;i<3;i++){
            this.breakOrderShortSide_[i] = -1;
        }
        this.breakOrderLongSide_ = new int[5];
        for(int i=0;i<5;i++){
            this.breakOrderLongSide_[i] = -1;
        }
        this.noBeamsBrokenShort_ = 0;
        this.noBeamsBrokenLong_ = 0;
        this.messageHandler_ = errHandl;
        
        this.currentID_ = 0;
        this.currentBounces_ = 0;
    }
    
    TofInterface(MessageHandler errHandl, PortController myPort, int noOfBeams){
        this(errHandl, noOfBeams);
        this.myPort_ = myPort;
    }
    
    public void setMyPort(PortController myPort){
        this.myPort_ = myPort;
    }
    
    public void receiveBounce(int time, String broken, int uniqueID){
        char[] chrBeamStatus = broken.toCharArray();
        if(this.gridStatus_==true && !(Integer.parseInt(broken) == this.engagedStatus_)){
            //grid was intact and is now broken
            if(this.jumpData_[0] == -1){
                this.jumpData_[0] = time;
            }else{
                this.jumpData_[2] = time;
                if(noToCollect_!=0){
                    this.jump_ = new Jump(this.jumpData_[0], this.jumpData_[1], this.jumpData_[2],lastLocation_);
                    this.routine_.addJump(this.jump_);
                    noToCollect_--;
                    if(noToCollect_==0){
                        write();
                    }
                }
                this.jumpData_[0] = this.jumpData_[2];
                this.jumpData_[1] = -1;
                this.jumpData_[2] = -1;
            }
            
            this.noBeamsBrokenShort_ = 0;
            this.noBeamsBrokenLong_ = 0;
        }else if(this.gridStatus_==false && (Integer.parseInt(broken) == this.engagedStatus_)){
            //grid was broken and now is intact
             char[] location = new char[2];
             location[0] = ' ';
             location[1] = ' ';
            //USE THE ARRAY OF BEAM BREAK ORDERS TO CALCULATE THE LOCATION TO SEND WITH THE WRITE

            switch(breakOrderLongSide_[0]){
                case 4:
                    if(breakOrderLongSide_[1]==5){
                        location[0]='B';
                    }else{
                        location[0]='A';
                    }
                    break;
                case 5:
                    location[0]='C';
                    break;
                case 6:
                    location[0]='D';
                    break;
                case 7:
                    location[0]='E';
                    break;
                case 8:
                    if(breakOrderLongSide_[1]==7){
                        location[0]='F';
                    }else{
                        location[0]='G';
                    }
                    break;
            }
            System.out.println(breakOrderShortSide_[0] + " "+ breakOrderShortSide_[1] + " "+ breakOrderShortSide_[2]);
            switch(breakOrderShortSide_[0]){
                case 1:
                    if(breakOrderShortSide_[1]==2){
                        location[1]= '1';
                    }else{
                        location[1]= '0';
                    }
                    break;
                case 2:
                    location[1]='2';
                    break;
                case 3:
                    if(breakOrderShortSide_[1]==2){
                        location[1]= '3';
                    }else{
                        location[1]= '4';
                    }
                    break;
            }
            
            if(location[0] == ' ' || location[1] == ' '){
                lastLocation_ = "none";
            }else{         
                lastLocation_ = location[0]+""+location[1];
            }
            this.jumpData_[1] = time;
            for(int i=0;i<3;i++){
                this.breakOrderShortSide_[i] = -1;
            }
            this.breakOrderLongSide_ = new int[5];
            for(int i=0;i<5;i++){
                this.breakOrderLongSide_[i] = -1;
            }
            
            countBounce(uniqueID);
        }
        
        for(int i=0;i<3;i++){
            if(chrBeamStatus[7-i]=='0' && this.beamStatus_[i] == true){
                this.breakOrderShortSide_[this.noBeamsBrokenShort_] = i+1;
                this.noBeamsBrokenShort_++;
            }
        }

        for(int i=3;i<8;i++){
            if(chrBeamStatus[7-i]=='0' && this.beamStatus_[i] == true){
                this.breakOrderLongSide_[this.noBeamsBrokenLong_] = i+1;
                this.noBeamsBrokenLong_++;
            }
        }
        
            
        this.gridStatus_ = (Integer.parseInt(broken) == this.engagedStatus_);    

        for(int i=0;i<this.noOfBeamsOnDevice_;i++){
            this.beamStatus_[i] = (chrBeamStatus[7-i]=='1');
        }
    }
    
     public void countBounce(int uniqueID){
         if(uniqueID == this.currentID_){
             this.currentBounces_++;
         }else{
             //STORE BOUNCES
             this.currentID_ = uniqueID;
             this.currentBounces_ = 1;
         }
         
         if(this.currentBounces_ == 50){
             //STORE BOUNCES
         }
     }
     public void collectBounces(int noOfBounces, DBConnect database, int gymnast){
         
         this.db_ = database;
         this.gymnast_ = gymnast;
         
         DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
         Date date = new Date();
         
         this.routine_ = new Routine(noOfBounces, 0, dateFormat.format(date),"");
         this.currentRoutineID_ = 0;
         
         this.myPort_.clearBuffer();       
         this.jumpData_[0] = -1;
         this.jumpData_[1] = -1;
         this.jumpData_[2] = -1;
         this.noBeamsBrokenShort_ = 0;
         this.noBeamsBrokenLong_ = 0;
         for(int i=0;i<3;i++){
             this.breakOrderShortSide_[i]=0;
         }
         for(int i=0;i<5;i++){
             this.breakOrderLongSide_[i]=0;
         }
         this.noToCollect_ = noOfBounces*2 + 1;
}
     
     private void write(){
        
            
        this.currentRoutineID_ = this.db_.addRoutine(this.routine_, this.gymnast_);
        this.routine_.setRoutineId(this.currentRoutineID_);
    }
    
     public int getNoOfBouncesRemaining(){
        return this.noToCollect_;
    }
    
    public boolean[] getBeamStatus(){
        return this.beamStatus_;
    }
    
    public Routine getRoutine(){
        return this.routine_;
    }
    
    public int getRoutineId(){
        return this.currentRoutineID_;
    }
    
    public PortController getPort(){
        return this.myPort_;
    }
    
    public String getLastLocation(){
        return this.lastLocation_;
    }
    
    public int getNoBeams(){
        return this.noOfBeamsOnDevice_;
    }
}
