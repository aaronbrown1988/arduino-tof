/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package trampoline;

/**
 *
 * @author Kieran
 */
public class Club {
    
    private int clubId_;
    private String shortName_;
    private String longName_;
    private String addressLine1_;
    private String addressLine2_;
    private String town_;
    private String county_;
    private String postcode_;
    private String headCoach_;
    private String contactNumber_;
    
    Club(int clubId, String shortName, String longName, String addressLine1, String addressLine2, String town,
         String county, String postcode, String headCoach, String contactNumber){
    
        this.clubId_ = clubId;
        this.shortName_ = shortName;
        this.longName_ = longName;
        this.addressLine1_ = addressLine1;
        this.addressLine2_ = addressLine2;
        this.town_ = town;
        this.county_ = county;
        this.postcode_ = postcode;
        this.headCoach_ = headCoach;
        this.contactNumber_ = contactNumber;
    }
    Club(){
        this(0,"","","","","","","","","");
    }
    
    int getId(){
        return this.clubId_;
    }
    
    String getShortName(){
        return this.shortName_;
    }
    
    String getLongName(){
        return this.longName_;
    }
    
    String getAddressLine1(){
        return this.addressLine1_;
    }
    
    String getAddressLine2(){
        return this.addressLine1_;
    }
    
    String getTown(){
        return this.town_;
    }
    
    String getCounty(){
        return this.county_;
    }
    
    String getPostcode(){
        return this.postcode_;
    }
    
    String getHeadCoach(){
        return this.headCoach_;
    }
    
    String getContactDetails(){
        return this.contactNumber_;
    }
}
