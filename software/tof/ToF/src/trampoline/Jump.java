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
    private String position_;
    
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
    public Jump(int breakStart, int engage, int breakEnd, String position) {
        this.breakStart_ = breakStart;
        this.engage_ = engage;
        this.breakEnd_ = breakEnd;
        this.position_ = position;
    }
    
    //Shortcut function.
    public Jump(String breakStart, String engage, String breakEnd, String position) {
        this.breakStart_ = Integer.parseInt(breakStart);
        this.engage_ = Integer.parseInt(engage);
        this.breakEnd_ = Integer.parseInt(breakEnd);
        this.position_ = position;
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
    
    public void setPosition(String position){
        this.position_ = position;
    }
    /** Getters **/
    /**
     * Get the BreakStart time
     * @return 
     */
    public int getBreakStart(){
        return this.breakStart_;
    }
    
    /**
     * Get the Engage time
     * @return 
     */
    public int getEngage(){
        return this.engage_;
    }
    
    /** 
     * Get the BreakEnd time
     * @return 
     */
    public int getBreakEnd(){
        return this.breakEnd_;
    }
    
    public double getHeight() {
        return 0.5*9.8*getTof()*getTof()*0.5;
    }
    
    /**
     * Get the ToF
     * @return 
     */
    public double getTof(){
       return (this.breakEnd_ - this.engage_)/1000.0;
    }
    
    /**
     * Get the Ton
     * @return 
     */
    public double getTon(){
        return (this.engage_ - this.breakStart_)/1000.0;
    }
    
    /**
     * Get the Total jump time
     * @return 
     */
    public double getTotal(){
        return (this.breakEnd_ - this.breakStart_)/1000.0;
    }
    
    public String getPosition(){
        return this.position_;
    }
    
    /**
     * To String method for function calls
     * @return 
     */
    public String toString(){
        return ("ToF: " + this.getTof() + 
                " ToN: " + this.getTon() + " Total: " + this.getTotal());
    }
}
