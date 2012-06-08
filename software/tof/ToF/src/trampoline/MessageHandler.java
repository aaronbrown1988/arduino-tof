/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package trampoline;

/**
 *
 * @author Kieran
 */
public class MessageHandler {
    
    private boolean flag_;   // Flag if there is an error
    private String currentErrorShort_; //Short version for display of current error;
    private String currentErrorLong_; //Long version of current error;
    private java.awt.Color currentColour_; // Colour of current error;
    private String moreDetails_; // Debugging details 
    
    
    private static final java.awt.Color[] errorColour_ = {null, new java.awt.Color(255,0,0),
                                                          new java.awt.Color(255,0,0),
                                                          new java.awt.Color(255,0,0),
                                                          new java.awt.Color(0,0,255),
                                                          new java.awt.Color(255,0,0),
                                                          new java.awt.Color(255,0,0),
                                                          new java.awt.Color(255,0,0),
                                                          new java.awt.Color(255,0,0),
                                                          new java.awt.Color(255,0,0),
                                                          new java.awt.Color(255,0,0),
                                                          new java.awt.Color(255,0,0),
                                                          new java.awt.Color(0,0,255),
                                                          new java.awt.Color(0,0,255),
                                                          new java.awt.Color(255,0,0),
                                                          new java.awt.Color(0,0,255),
                                                          new java.awt.Color(255,0,0),
                                                          new java.awt.Color(255,0,0),
                                                          new java.awt.Color(0,0,255),
                                                          new java.awt.Color(255,0,0),
                                                          new java.awt.Color(0,0,255),
                                                          new java.awt.Color(0,0,255),
                                                          new java.awt.Color(0,0,255),
                                                          new java.awt.Color(255,0,0),
                                                          new java.awt.Color(255,0,0),
                                                          new java.awt.Color(0,0,255),
                                                          new java.awt.Color(255,0,0),
                                                          new java.awt.Color(0,0,255),
                                                          new java.awt.Color(255,0,0),
                                                          new java.awt.Color(255,0,0),
                                                          new java.awt.Color(0,0,255)
                                                         };
    
    private static final String[] shortErrors_ =   {"","Error: Incorrect password",
                                                    "Error: Database connection error",
                                                    "Error: ToF not attached",
                                                    "Logout Successful",
                                                    "Error: Settings file",
                                                    "Error: General file I/O",
                                                    "Error: Handshake failed",
                                                    "Error: Device Communications",
                                                    "Error: COM Port Cache",
                                                    "Error: Database Access",
                                                    "Error: No tag name",
                                                    "Gymnast Added Successfully",
                                                    "Gymnast Modified Successfully",
                                                    "Error: Gymnast Details",
                                                    "Gymnast Deleted Successfully",
                                                    "Error: Incorrect password",
                                                    "Master Reset Aborted",
                                                    "Master Reset Successful",
                                                    "Error: Club Name",
                                                    "Club Added Successfully",
                                                    "Club Deleted Successfully",
                                                    "Club Modified Successfully",
                                                    "Gymnast Delete Aborted",
                                                    "Club Delete Aborted",
                                                    "Routine Deleted Successfully",
                                                    "Routine Delete Aborted",
                                                    "Tag Added Successfully",
                                                    "Error: Incorrect Password",
                                                    "Error: Password mismatch",
                                                    "Password Successfully Changed"
                                                   };
    
    
    
    private static final String[] longErrors_ =    {"","The password supplied for access to the Club Managment tab was incorrect. Please try again.",
                                                    "An error occured whilst trying to initialise the database connection.",
                                                    "No time of flight device was found attached to the computer. Please check all cable connections are secure and the device is powered on.",
                                                    "You have successfully logged out of the Club Management panel.",
                                                    "The settings file for the COM ports on this system cannot be located. Please check the installation of the program has not been tampered with an re-instal if necessary.",
                                                    "There has been a general input or output error when communicating with a file. This could be caused by the file not being found or by the file being in use by another program.",
                                                    "The handshake with one of the ToF devices or COM ports on the system was not completed correctly. Please reboot all attached ToF devices.",
                                                    "There was an error communicating with the ToF device. Please reboot all attached ToF devices.",
                                                    "There was an issue with emptying the COM port data cache. Please restart the computer and reboot all attached ToF devices.",
                                                    "An issue was encountered whilst reading from the SQL database. Please ensure that only one copy of the program is open and try restarting the computer if the problem persists.",
                                                    "The new tag was not added to the database because no tag name was provided or it was the same as an existing tag name. Please try again and input a different tag name.",
                                                    "The new gymnast was successfully added to the database.",
                                                    "The gymnast details were succesfully modified in the database.",
                                                    "Please provide a name and selected a club for the new gymnast to be added to the database.",
                                                    "The gymnast was successfully deleted from the database.",
                                                    "The password supplied for the master reset was incorrect. Please try again.",
                                                    "The master reset of all the database data was aborted by the user.",
                                                    "The database was successfully purged of all data.",
                                                    "Please provide a short and long name for the new club to be added to the database.",
                                                    "The new club was successfully added to the database.",
                                                    "The club was successfully deleted from the database.",
                                                    "The club details successfully modified in the database.",
                                                    "Deleting the gymnast was aborted by the user.",
                                                    "Deleting the club was aborted by the user.",
                                                    "The routine was successfully deleted from the database.",
                                                    "Deleting the routine was aborted by the user.",
                                                    "The tag was successfully added to the selected routines.",
                                                    "The old password supplied for access to the Club Management tab was incorrect. Please try again.",
                                                    "The two new passwords supplied were different. Please try again and enter the same password both times.",
                                                    "The password to access the Club Management tab was successfully changed. Please use the new password to login."
                                                   };
    
    MessageHandler(){
        this.flag_ = false;
        this.currentErrorShort_ = "";
        this.currentErrorLong_ = "";
        this.moreDetails_ = "";
        this.currentColour_ = null;
    }
    
    boolean isError(){
        return this.flag_;
    }
    
    String getCurrentErrorShort(){
        return this.currentErrorShort_;
    }
    
    String getCurrentErrorLong(){
        return this.currentErrorLong_;
    }
    
    String getMoreDetails(){
        return this.moreDetails_;
    }
    
    java.awt.Color getColour(){
        return this.currentColour_;
    }
                
    void setError(int errno){
        this.flag_ = true;
        this.currentErrorShort_ = this.shortErrors_[errno];
        this.currentErrorLong_ = this.longErrors_[errno];
        this.currentColour_ = this.errorColour_[errno];
        this.moreDetails_ = "";
    }
    
    void setMoreDetails(String details){
        this.moreDetails_ = details;
        System.out.println(this.moreDetails_);
    }
    
    void clearError(){
        setError(0);
        this.flag_ = false;
    }
}
