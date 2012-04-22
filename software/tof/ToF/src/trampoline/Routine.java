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
    private Jump[] jumpArray_;
    private int lowestJump_;
    private int numberOfJumps_;
    private int numberOfJumpsUsed_;
    private double[] statsHeights_;
    private double[] statsTimes_;
    private double totalTime_;
    private int worstJumpForLosingHeight_;
    
    Routine (Jump[] jumpList) {
        numberOfJumps_ = jumpList.length;
        jumpArray_     = new Jump[numberOfJumps_];
        statsHeights_  = new double[numberOfJumps_];
        statsTimes_    = new double[numberOfJumps_];
        numberOfJumpsUsed_ = 0;
        for (Jump j:jumpList) {
            addJump(j);
        }
    }
    
    Routine (int numberOfJumps) {
        numberOfJumps_ = numberOfJumps;
        jumpArray_     = new Jump[numberOfJumps_];
        statsHeights_  = new double[numberOfJumps_];
        statsTimes_    = new double[numberOfJumps_];
        numberOfJumpsUsed_ = 0;
    }
    
    //Simply adds another jump to the jumpArray and returns false if the jump wasn't added because the jumpArray_ is full.
    public boolean addJump(Jump j) {
        if (numberOfJumpsUsed_ >= numberOfJumps_) {
            return false;
        }
        jumpArray_[numberOfJumpsUsed_] = j;
        numberOfJumpsUsed_++;
        if (numberOfJumpsUsed_ == numberOfJumps_) {
            generateStats();
        }
        return true;
    }
    
    //Called automatically when addJump fills in the last jump, this generates stats about the routine, to easily cache them. 
    private void generateStats() {
        double worstHeightLossSoFar = 0.0;
        double highestJumpHeight    = 0.0;
        double lowestJumpHeight     = 0.0;
        
        for (int i = 0; i < numberOfJumps_; i++) {
            //s = ut+.5at^2
            statsHeights_[i] = jumpArray_[i].getHeight();
            statsTimes_[i]   = jumpArray_[i].getTof();
            
            if (i > 0) {
                if (jumpArray_[i-1].getTotal() - jumpArray_[i].getTotal() > worstHeightLossSoFar) {
                    worstHeightLossSoFar = jumpArray_[i-1].getTotal() - jumpArray_[i].getTotal();
                    worstJumpForLosingHeight_ = i;
                }
            }
            
            if (highestJumpHeight < jumpArray_[i].getTof()) {
                highestJump_ = i;
            }
            
            if (lowestJumpHeight > jumpArray_[i].getTof()) {
                lowestJump_ = i;
            }
            
            totalTime_ += jumpArray_[i].getTotal();
        }
        
        averageJumpTime_ = totalTime_ / 10.0;
    }
    
    public int getNumberOfJumps() {
        return numberOfJumps_;
    }
    
    public double[] getStatsHeights() {
        return statsHeights_;
    }
    
    public double[] getStatsTimes() {
        return statsTimes_;
    }
}
