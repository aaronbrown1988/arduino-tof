/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package trampoline;
import java.sql.*;
import java.util.*;

/**
 *
 * @author Andreas
 */
public class DBConnect {
    public Connection conn_;
    public Statement stat_;
    public ResultSet rs_;
    
    DBConnect() {
        try {
            Class.forName("org.sqlite.JDBC");
            //connect to the database
            conn_ = DriverManager.getConnection("jdbc:sqlite:data/database");
            stat_ = conn_.createStatement();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public int addClub(String name) {
        return executeUpdate("INSERT INTO clubs (name) VALUES ('"+name+"')");
    }
    
    public int addGymnast(int cid, String name) {
        return executeUpdate("INSERT INTO gymnasts (cid, name) VALUES ('"+cid+"', '"+name+"')");
    }
    
    private int addJump(int routineid, int jumpnumber, double b1, double en, double b2, double tof, double ton, double total, String location) {
        return executeUpdate("INSERT INTO jumps (routineid, jumpnumber, break1, engage, break2, tof, ton, total, location) "
                + "VALUES ('"+routineid+"', '"+jumpnumber+"', '"+b1+"', '"+en+"', '"+b2+"', '"+tof+"', '"+ton+"', '"+total+"', '"+location+"')");
    }
    
    private int addJump(Jump j, int routineid, int jumpnumber) {
        return addJump(routineid, jumpnumber, j.getBreakStart(), j.getEngage(), j.getBreakEnd(), j.getTof(), j.getTon(), j.getTotal(), j.getlocation());
    }
    
    public int addRoutine(Routine r, int gid, String datetime) {
        int rid = executeUpdate("INSERT INTO routines (gymnastid, totaltof, totalton, totaltime, datetime, numberofjumps) "
            + "VALUES ('"+gid+"', '"+r.getTotalTof()+"', '"+r.getTotalTon()+"', '"+r.getTotalTime()+"', '"+datetime+"', '"+r.getNumberOfJumps()+"')");
        Jump[] jumpArray = r.getJumps();
        
        for (int i = 0; i < jumpArray.length; i++) {
            addJump(jumpArray[i], rid, i);
        }
        
        return rid;
    }
    
    public Gymnast getGymnast(int gid) {
        executeQuery("SELECT g.*, c.* FROM gymnasts g, clubs c WHERE g.id = '"+gid+"' AND g.clubid = c.id");
        
        return new Gymnast(resultGetInt("gid"), resultGetString("gname"), resultGetInt("cid"), resultGetString("cname"));
    }
    
    public Gymnast[] getAllGymnasts() {
        executeQuery("SELECT g.*, c.* FROM gymnasts AS g, clubs AS c WHERE g.clubid = c.cid");
        
        ArrayList<Gymnast> gymnastList = new ArrayList<Gymnast>();
        int i = 0;
        
        try {
            while (rs_.next()) {
                gymnastList.add(new Gymnast(resultGetInt("gid"), resultGetString("gname"), resultGetInt("cid"), resultGetString("cname")));
                i++;
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        
        return gymnastList.toArray(new Gymnast[gymnastList.size()]);
    }
    
    public Jump getJump(int jid) {
        executeQuery("SELECT * FROM jumps WHERE jid = '"+jid+"'");
        
        return new Jump(resultGetInt("break1"), resultGetInt("engage"), resultGetInt("break2"), "A0");
    }
    
    //If we know we've already got the jump row loaded into "rs_" then use this. 
    Jump getJump() {
        return new Jump(resultGetInt("break1"), resultGetInt("engage"), resultGetInt("break2"), "A0");
    }
    
    public Routine getRoutine(int rid) {
        executeQuery("SELECT * FROM routine WHERE rid = '"+rid+"'");
        int numberOfJumps = resultGetInt("numberofjumps");

        Routine r = new Routine(numberOfJumps);
        //executeQuery("SELECT * FROM jumps WHERE routineid = '"+rid+"' ORDER BY jumpnumber ASC");
        
        try {
            rs_ = stat_.executeQuery("SELECT * FROM jumps WHERE routineid = '"+rid+"' ORDER BY jumpnumber ASC");
            while (rs_.next()) {
                r.addJump(getJump());
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        
        return r;
    }
    
    //We run this, then just use the class variable "rs_" to get our result set. 
    private boolean executeQuery(String s) {
        System.out.println("Execute: "+s);
        try {
            //conn_ = DriverManager.getConnection("jdbc:sqlite:data/database");
            //stat_ = conn_.createStatement();
            rs_ = stat_.executeQuery(s);
        }

        catch (Exception e) {
            e.printStackTrace();
        }

        return true;
    }
    
    //We run this, then just use the class variable "rs_" to get our result set. 
    private int executeUpdate(String s) {
        int i = 0;
        System.out.println("Execute Update: "+s);
        try {
            stat_ = conn_.createStatement();
            i = stat_.executeUpdate(s);
        
            ResultSet keys = stat_.getGeneratedKeys();  

            keys.next();  
            i = keys.getInt(1);
        }

        catch (Exception e) {
            e.printStackTrace();
        }
        
        System.out.println("Update:"+i);
        return i;
    }
    
    private int resultGetInt(String recordName) {
        int i = 0;
        
        try {
            i = rs_.getInt(recordName);
        }

        catch (Exception e) {
            e.printStackTrace();
        }

        return i;
    }
    
    private String resultGetString(String recordName) {
        String s = "";
        
        try {
            s = rs_.getString(recordName);
        }

        catch (Exception e) {
            e.printStackTrace();
        }

        return s;
    }
}
