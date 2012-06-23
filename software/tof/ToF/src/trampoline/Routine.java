/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package trampoline;

/**
 *
 * @author Andreas
 */
public class Routine {
    private double averageJumpTime_;
    private int highestJump_;
    private int lowestJump_;
    private int furthestFromCross_;
    private double averageLocationDeduction_;
    private double smallestLocationDeduction_;
    private double largestLocationDeduction_;
    private int id_;
    private Jump[] jumpArray_;
    private int numberOfJumps_;
    private int numberOfJumpsUsed_;
    private double[] statsHeights_;
    private double[] statsTimes_;
    private double totalTime_;
    private double totalTof_;
    private double totalTon_;
    private String dateTime_;
    private String comments_;
    private boolean routineComplete_;
    
    Routine (Jump[] jumpList, int id, String dateTime, String comments) {
        id_ = id;
        numberOfJumps_ = jumpList.length;
        jumpArray_     = new Jump[numberOfJumps_];
        statsHeights_  = new double[numberOfJumps_];
        statsTimes_    = new double[numberOfJumps_];
        numberOfJumpsUsed_ = 0;
        for (Jump j:jumpList) {
            addJump(j);
        }
        dateTime_ = dateTime;
        comments_ = comments;
        routineComplete_ = false;
    }
    
    Routine (int numberOfJumps, int id, String dateTime, String comments) {
        id_ = id;
        numberOfJumps_ = numberOfJumps;
        jumpArray_     = new Jump[numberOfJumps_];
        statsHeights_  = new double[numberOfJumps_];
        statsTimes_    = new double[numberOfJumps_];
        numberOfJumpsUsed_ = 0;
        dateTime_ = dateTime;
        comments_ = comments;
        routineComplete_ = false;
    }
    
    //Simply adds another jump to the jumpArray and returns false if the jump wasn't added because the jumpArray_ is full.
    public boolean addJump(Jump j) {
        if (routineComplete_) {
            return false;
        }
        jumpArray_[numberOfJumpsUsed_] = j;
        numberOfJumpsUsed_++;
        if (numberOfJumpsUsed_ == numberOfJumps_) {
            generateStats();
            routineComplete_ = true;
        }
        return true;
    }
    
    //Called automatically when addJump fills in the last jump, this generates stats about the routine, to easily cache them. 
    private void generateStats() {
        double highestJumpHeight    = jumpArray_[0].getTof();
        double lowestJumpHeight     = jumpArray_[0].getTof();
        largestLocationDeduction_    = jumpArray_[0].getLocationDeduction();
        smallestLocationDeduction_   = jumpArray_[0].getLocationDeduction();
        
        for (int i = 0; i < numberOfJumps_; i++) {
            //s = ut+.5at^2
            statsHeights_[i] = jumpArray_[i].getHeight();
            statsTimes_[i]   = jumpArray_[i].getTof();
            
            if(largestLocationDeduction_ < jumpArray_[i].getLocationDeduction()){
                largestLocationDeduction_ = jumpArray_[i].getLocationDeduction();
                furthestFromCross_ = i;
            }
            
            if(smallestLocationDeduction_ < jumpArray_[i].getLocationDeduction()){
                smallestLocationDeduction_ = jumpArray_[i].getLocationDeduction();
            }
            
            if (highestJumpHeight < jumpArray_[i].getTof()) {
                highestJump_ = i;
            }
            
            if (lowestJumpHeight > jumpArray_[i].getTof()) {
                lowestJump_ = i;
            }
            
            totalTime_ += jumpArray_[i].getTotal();
            totalTof_  += jumpArray_[i].getTof();
            totalTon_  += jumpArray_[i].getTon();
            averageLocationDeduction_ += jumpArray_[i].getLocationDeduction();
        }
        averageJumpTime_ = totalTime_ / numberOfJumps_;
        averageLocationDeduction_ /= numberOfJumps_;
    }
    
    public double getAverageLocationDeduction(){
        double temp = averageLocationDeduction_;
        return roundToDecimals(temp,2);
    }
    
    public double getSmallestLocationDeduction(){
        double temp = smallestLocationDeduction_;
        return roundToDecimals(temp,2);
    }
    
    public double getLargestLocationDeduction(){
        double temp = largestLocationDeduction_;
        return roundToDecimals(temp,2);
    }
    
    public int getJumpFurthestFromCross(){
        return furthestFromCross_;
    }
    
    public double getAverageTime() {
        double temp = numberOfJumps_;
        return roundToDecimals(totalTime_ / temp, 3);
    }
    
    public double getAverageTof() {
        double temp = numberOfJumps_;
        return roundToDecimals(totalTof_ / temp, 3);
    }
    
    public double getAverageTon() {
        double temp = numberOfJumps_;
        return roundToDecimals(totalTon_ / temp, 3);
    }
    
    public int getID() {
        return id_;
    }
    
    public Jump[] getJumps() {
        return jumpArray_;
    }
    
    public int getNumberOfJumps() {
        return numberOfJumps_;
    }
    
    public int getNumberOfJumpsUsed(){
        return numberOfJumpsUsed_;
    }
    
    public double[] getStatsHeights() {
        return statsHeights_;
    }
    
    public double[] getStatsTimes() {
        return statsTimes_;
    }
    
    public double getTotalTime() {
        return roundToDecimals(totalTime_, 3);
    }
    
    public double getTotalTof() {
        return roundToDecimals(totalTof_, 3);
    }
    
    public double getTotalTon() {
        return roundToDecimals(totalTon_, 3);
    }
    
    public Jump getHighestJump() {
        return jumpArray_[highestJump_];
    }
    
    public Jump getLowestJump() {
        return jumpArray_[lowestJump_];
    }
    
    public String getDateTime(){
        return dateTime_;
    }
    
    public static double roundToDecimals(double d, int c) {
        int temp=(int)((d*Math.pow(10,c)));
        return (((double)temp)/Math.pow(10,c));
    }
    
    public void setRoutineId(int id) {
        id_ = id;
    }
    
    public void setDateTime(String dateTime){
        dateTime_ = dateTime;
    }
    
    public void setComments(String comments){
        comments_ = comments;
    }
    
    public String getComments(){
        return comments_;
    }
    
    public boolean finishedRoutine(){
        return routineComplete_;
    }
}
