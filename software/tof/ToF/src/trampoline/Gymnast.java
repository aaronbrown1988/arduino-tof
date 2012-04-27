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
    
    Gymnast(int id, String name, int clubid, String clubname) {
        id_ = id;
        name_ = name;
        clubid_ = clubid;
        clubname_ = clubname;
    }
    
    Gymnast(int id, String name) {
        id_ = id;
        name_ = name;
    }
    
    public int getClubID() {
        return clubid_;
    }
    
    public String getClubName() {
        return clubname_;
    }
    
    public int getID() {
        return id_;
    }
    
    public String getName() {
        return name_;
    }
}
