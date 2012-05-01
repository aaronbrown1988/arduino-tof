/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package trampoline;

/**
 *
 * @author Andreas
 */
public class Gymnast {
    private int id_ = 0;
    private String name_ = "";
    private int clubid_ = 0;
    private String clubname_ = "";
    private int dobday_;
    private int dobmonth_;
    private int dobyear_;
    private String dobfull_;
    private String category_;
    
    Gymnast(int id, String name, int clubid, String clubname, int dobday, int dobmonth, int dobyear, String category) {
        id_ = id;
        name_ = name;
        clubid_ = clubid;
        clubname_ = clubname;
        dobday_ = dobday;
        dobmonth_ = dobmonth;
        dobyear_ = dobyear;
        dobfull_ = dobyear+"-"+dobmonth+"-"+dobday;
        category_ = category;
    }
    
    Gymnast(int id, String name) {
        id_ = id;
        name_ = name;
    }
    
    public String getCategory() {
        return category_;
    }
    
    public int getClubID() {
        return clubid_;
    }
    
    public String getClubName() {
        return clubname_;
    }
    
    public int getDobDay() {
        return dobday_;
    }
    
    public int getDobMonth() {
        return dobmonth_;
    }
    
    public int getDobYear() {
        return dobyear_;
    }
    
    public int getID() {
        return id_;
    }
    
    public String getName() {
        return name_;
    }
}
