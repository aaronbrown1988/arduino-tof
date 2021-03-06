/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package trampoline;

/**
 *
 * @author Kieran
 */
public class Jump {
    private int breakStart_;
    private int engage_;
    private int breakEnd_;
    private String location_;
    
    /**
     * Null constructor
     */
    public Jump(){
        this(0,0,0,"A0");
    }
    
    /**
     * Constructor for a jump event
     * @param breakStart
     * @param engage
     * @param breakEnd 
     */
    public Jump(int breakStart, int engage, int breakEnd, String location) {
        this.breakStart_ = breakStart;
        this.engage_ = engage;
        this.breakEnd_ = breakEnd;
        this.location_ = location;
    }
    
    //Shortcut function.
    public Jump(String breakStart, String engage, String breakEnd, String location) {
        this.breakStart_ = Integer.parseInt(breakStart);
        this.engage_ = Integer.parseInt(engage);
        this.breakEnd_ = Integer.parseInt(breakEnd);
        this.location_ = location;
    }
    
    /** Setters **/
       
    /**
     * Set the first break time
     * @param breakStart 
     */    
    public void setBreakStart(int breakStart){
        this.breakStart_ = breakStart;
    }
    
    /**
     * Set engage time
     * @param engage 
     */
    public void setEngage(int engage){
        this.engage_ = engage;
    }
    
    /**
     * Set the second break time
     * @param breakEnd 
     */
    public void setBreakEnd(int breakEnd){
        this.breakEnd_ = breakEnd;
    }
    
    public void setlocation(String location){
        this.location_ = location;
    }
    
    public int getBreakStart(){
        return this.breakStart_;
    }
    
    public int getEngage(){
        return this.engage_;
    }
    
    public int getBreakEnd(){
        return this.breakEnd_;
    }
    
    public double getHeight() {
        return 0.5*9.8*getTof()*getTof()*0.5;
    }
    
    public double getTof(){
       return (this.breakEnd_ - this.engage_)/1000.0;
    }
    
    public double getTon(){
        return (this.engage_ - this.breakStart_)/1000.0;
    }
    
    public double getTotal(){
        return (this.breakEnd_ - this.breakStart_)/1000.0;
    }
    
    public String getLocation(){
        return this.location_;
    }
    
    public double getLocationDeduction(){
        double deduction=0;
        char[] location = location_.toCharArray();
        
        if(location[0] == 'A'){
            deduction+=3;
        }else if(location[0] == 'B'){
            deduction+=2;
        }else if(location[0] == 'C'){
            deduction+=1;
        }else if(location[0] == 'D'){
            deduction+=0;
        }else if(location[0] == 'E'){
            deduction+=1;
        }else if(location[0] == 'F'){
            deduction+=2;
        }else if(location[0] == 'G'){
            deduction+=3;
        }
        
        switch(location[1]){
            case 0:
                deduction+=2;
                break;
            case 1:
                deduction+=1;
                break;
            case 2:
                deduction+=0;
                break;
            case 3:
                deduction+=1;
                break;
            case 4:
                deduction+=2;
                break;
        }
        
        return deduction;
    }
    
    public String toString(){
        return ("ToF: " + this.getTof() + 
                " ToN: " + this.getTon() + " Total: " + this.getTotal());
    }
}
