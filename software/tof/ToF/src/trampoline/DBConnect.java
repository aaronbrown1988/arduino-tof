/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package trampoline;
import java.sql.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 *
 * @author Andreas
 */
public class DBConnect {
    public Connection conn_;
    public Statement stat_;
    public ResultSet rs_;
    private MessageHandler messageHandler_; // Error Handler inherited from main class;
    
    DBConnect(MessageHandler errHandl) {
        this.messageHandler_ = errHandl;
        try {
            Class.forName("org.sqlite.JDBC");
            //connect to the database
            conn_ = DriverManager.getConnection("jdbc:sqlite:data/database");
            stat_ = conn_.createStatement();
        }
        catch (Exception e) {
            this.messageHandler_.setError(2);
            this.messageHandler_.setMoreDetails(e.toString());
        }
    }
    
    public int addTag(int gid, String tag){
        return executeUpdate("INSERT INTO tags (gymnastid, tname) "
                              + "VALUES ('"+gid+"' ,'"+tag+"')");
    }
    
    public int addClub(String shortName, String longName, String headCoach, String phoneNumber, String addressLine1, String addressLine2, String town, String county, String postcode) {
        return executeUpdate("INSERT INTO clubs (shortname, longname, headcoach, phonenumber, addressline1, addressline2, town, county, postcode)"
                + "VALUES ('"+shortName+"', '"+longName+"', '"+headCoach+"', '"+phoneNumber+"', '"+addressLine1+"', '"+addressLine2+"', '"+town+"', '"+county+"', '"+postcode+"')");
    }
    
    public int addGymnast(String name, int dobDay, int dobMonth, int dobYear, int categoryid, int cid) {
        String dobfull = dobYear+"-"+dobMonth+"-"+dobDay;
        
        return executeUpdate("INSERT INTO gymnasts (clubid, gname, dobday, dobmonth, dobyear, dobfull, categoryid) "
                + "VALUES ('"+cid+"', '"+name+"', '"+dobDay+"', '"+dobMonth+"', '"+dobYear+"', '"+dobfull+"', '"+categoryid+"')");
    }

    private int addJump(int routineid, int jumpnumber, double b1, double en, double b2, double tof, double ton, double total, String location) {
        return executeUpdate("INSERT INTO jumps (routineid, jumpnumber, break1, engage, break2, tof, ton, total, location) "
                + "VALUES ('"+routineid+"', '"+jumpnumber+"', '"+b1+"', '"+en+"', '"+b2+"', '"+tof+"', '"+ton+"', '"+total+"', '"+location+"')");
    }
    
    private int addJump(Jump j, int routineid, int jumpnumber) {
        return addJump(routineid, jumpnumber, j.getBreakStart(), j.getEngage(), j.getBreakEnd(), j.getTof(), j.getTon(), j.getTotal(), j.getLocation());
    }
    
    public int addRoutine(Routine r, int gid) {
        String datetime = r.getDateTime();
        int rid = executeUpdate("INSERT INTO routines (gymnastid, totaltof, totalton, totaltime, datetime, numberofjumps) "
            + "VALUES ('"+gid+"', '"+r.getTotalTof()+"', '"+r.getTotalTon()+"', '"+r.getTotalTime()+"', '"+datetime+"', '"+r.getNumberOfJumps()+"')");
        Jump[] jumpArray = r.getJumps();
        
        for (int i = 0; i < jumpArray.length; i++) {
            addJump(jumpArray[i], rid, i);
        }
        
        return rid;
    }
      
    public int addComments(int rid, String comments){
        return executeUpdate("UPDATE routines SET comments = '"+comments+"' WHERE rid = '"+rid+"'");
    }
    
    public int addTagMap(int rid, int tid){
        return executeUpdate("INSERT INTO tagmap (tagid, routineid) VALUES ('"+tid+"', '"+rid+"')");
    }
    
    public int deleteTagMap(int rid, int tid){
        return executeUpdate("DELETE FROM tagmap WHERE routineid = '"+rid+"' AND tagid ='"+tid+"'");
    }
    
    public void deleteGymnast(int gid) {
        Routine[] routines = getRoutinesForGymnast(gid);
        
        for(Routine r:routines){
            deleteRoutine(r.getID());
        }
        
        Map<Integer,String> tags = getTags(gid);
        
        for(int tid:tags.keySet()){
            deleteTag(tid);
        }
                
        executeUpdate("DELETE FROM gymnasts WHERE gid = '"+gid+"'");
    }
    
    public void deleteClub(int cid) {
        Gymnast[] gymnasts = getGymnastsForClub(cid);
        
        for(Gymnast g:gymnasts){
            deleteGymnast(g.getID());
        }
        executeUpdate("DELETE FROM clubs WHERE cid = '"+cid+"'");
    }
    
    public void deleteJump(int jid){
        executeUpdate("DELETE FROM jumps WHERE jid ='"+jid+"'");
    }
    
    public void deleteTag(int tid){
        executeUpdate("DELETE FROM tags WHERE tid='"+tid+"'");
    }
    
    public void deleteRoutine(int rid) {
        executeQuery("SELECT * FROM jumps WHERE routineid = '"+rid+"'");
        ArrayList<Integer> jlist = new ArrayList<Integer>(10);
        try {
            while(rs_.next()){
                jlist.add(resultGetInt("jid"));
            }
            rs_.close();
        } catch (Exception e) {
            messageHandler_.setError(10);
            messageHandler_.setMoreDetails(e.toString());
        }
        
        for(int i:jlist){
            deleteJump(i);
        }
        
        executeUpdate("DELETE FROM routines WHERE rid ='"+rid+"'");
    }
    
    public int editGymnast(int gid, String name, int dobDay, int dobMonth, int dobYear, int categoryid, int cid) {
        String dobfull = dobYear+"-"+dobMonth+"-"+dobDay;
        
        return executeUpdate("UPDATE gymnasts SET clubid = '"+cid+"', gname = '"+name+"', dobday = '"+dobDay+"', dobmonth = '"+dobMonth+"', "
                + "dobyear = '"+dobYear+"', dobfull = '"+dobfull+"', categoryid = '"+categoryid+"' "
                + "WHERE gid = '"+gid+"'");
    }
    
    public int editClub(int cid, String shortName, String longName, String headCoach, String phoneNumber, String addressLine1, String addressLine2, String town, String county, String postcode) {
        
        return executeUpdate("UPDATE clubs SET shortname = '"+shortName+"', longname = '"+longName+"', headcoach = '"+headCoach+"', " 
               +"phoneNumber = '"+phoneNumber+"', addressLine1 = '"+addressLine1+"', addressLine2 = '"+addressLine2+"', town = '"+town+"', "
               +" county ='"+county+"', postcode = '"+postcode + "' WHERE cid = '"+cid+"'");
    }
    
    public int editTag(int tid, String tag){
        return executeUpdate("UPDATE tags SET tname = '"+tag+"' WHERE tid = '"+tid+"'");
    }
    
    public int editPassword(int pid, String password){
        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        java.util.Date date = new java.util.Date();
        
        return executeUpdate("UPDATE password SET passvalue ='"+password+"', datechanged ='"+dateFormat.format(date)+"' WHERE pid = '"+pid+"'");
    }
 
    public Map<Integer,String> getTags(int gid){
        executeQuery("SELECT * FROM tags WHERE gymnastid = '"+gid+"'");
        
        Map<Integer,String> tagMap = new HashMap<Integer,String>(10);
        
        try {
            while(rs_.next()){
                tagMap.put(resultGetInt("tid"),resultGetString("tname"));
            }
            rs_.close();
        } catch (Exception e) {
            messageHandler_.setError(10);
            messageHandler_.setMoreDetails(e.toString());
        }
        
        return tagMap;
    }
    
    public Category[] getAllCategories() {
        executeQuery("SELECT * FROM categories");
        
        ArrayList<Category> categoryList = new ArrayList<Category>();
        
        try {
            while (rs_.next()) {
                categoryList.add(new Category(resultGetInt("catid"), resultGetString("categoryname")));
            }
            rs_.close();
        }
        catch (Exception e) {
            messageHandler_.setError(10);
            messageHandler_.setMoreDetails(e.toString());
        }
        
        return categoryList.toArray(new Category[categoryList.size()]);
    }
        
    public Category getCategory(String name) {
        executeQuery("SELECT * FROM categories WHERE categoryname = '"+name+"'");
        
        try {
            rs_.next();
            rs_.close();
        }
        catch (Exception e) {
            messageHandler_.setError(10);
            messageHandler_.setMoreDetails(e.toString());
        }
        
        return new Category(resultGetInt("catid"), resultGetString("categoryname"));
    }
    
    public Category getCategory(int id) {
        executeQuery("SELECT * FROM categories WHERE catid = '"+id+"'");
        
        try {
            rs_.next();
            rs_.close();
        }
        catch (Exception e) {
            messageHandler_.setError(10);
            messageHandler_.setMoreDetails(e.toString());
        }
        
        return new Category(resultGetInt("catid"), resultGetString("categoryname"));
    }
    
    public Gymnast getGymnast(int gid) {
        executeQuery("SELECT g.*, c.* FROM gymnasts g, clubs c WHERE g.gid = '"+gid+"' AND g.clubid = c.cid");
        
        return new Gymnast(resultGetInt("gid"), resultGetString("gname"), resultGetInt("cid"), resultGetInt("dobday"), resultGetInt("dobmonth"), resultGetInt("dobyear"), resultGetInt("categoryid"));
    }
    
    public Club getClub(int cid){
        executeQuery("SELECT * FROM clubs WHERE cid = '"+cid+"'");
        
        return new Club(resultGetInt("cid"), resultGetString("shortname"), resultGetString("longname"), resultGetString("addressline1"), resultGetString("addressline2"), resultGetString("town"), resultGetString("county"), resultGetString("postcode"),resultGetString("headcoach"),resultGetString("phonenumber"));
    }
    
    public String getPassword(int pid){
        executeQuery("SELECT * FROM password WHERE pid ='"+pid+"'");
        
        return resultGetString("passvalue");
    }
    
    public Gymnast[] getAllGymnasts() {
        executeQuery("SELECT g.*, c.* FROM gymnasts AS g, clubs AS c WHERE g.clubid = c.cid");
        
        ArrayList<Gymnast> gymnastList = new ArrayList<Gymnast>();
        
        try {
            while (rs_.next()) {
                gymnastList.add(new Gymnast(resultGetInt("gid"), resultGetString("gname"), resultGetInt("clubid"), resultGetInt("dobday"), resultGetInt("dobmonth"), resultGetInt("dobyear"), resultGetInt("categoryid")));
            }
            rs_.close();
        }
        catch (Exception e) {
            messageHandler_.setError(10);
            messageHandler_.setMoreDetails(e.toString());
        }
        
        return gymnastList.toArray(new Gymnast[gymnastList.size()]);
    }
    
    public Gymnast[] getGymnastsForClub(int cid){
        executeQuery("SELECT g.*, c.* FROM gymnasts AS g, clubs AS c WHERE g.clubid = c.cid");
        
        ArrayList<Gymnast> gymnastList = new ArrayList<Gymnast>();
        
        try {
            while (rs_.next()) {
                Gymnast newgym = new Gymnast(resultGetInt("gid"), resultGetString("gname"), resultGetInt("clubid"), resultGetInt("dobday"), resultGetInt("dobmonth"), resultGetInt("dobyear"), resultGetInt("categoryid"));
                if(newgym.getClubID()==cid){
                    gymnastList.add(newgym);
                }
            }
            rs_.close();
        }
        catch (Exception e) {
            messageHandler_.setError(10);
            messageHandler_.setMoreDetails(e.toString());
        }
        
        return gymnastList.toArray(new Gymnast[gymnastList.size()]);
    }
    
    public Club[] getAllClubs() {
        executeQuery("SELECT * FROM clubs");
        
        ArrayList<Club> clubList = new ArrayList<Club>();
        try {
            while (rs_.next()) {
                clubList.add(new Club(resultGetInt("cid"), resultGetString("shortname"), resultGetString("longname"), resultGetString("addressline1"), resultGetString("addressline2"), resultGetString("town"), resultGetString("county"), resultGetString("postcode"),resultGetString("headcoach"),resultGetString("phonenumber")));
            }
            rs_.close();
        }
        catch (Exception e) {
            messageHandler_.setError(10);
            messageHandler_.setMoreDetails(e.toString());
        }
        return clubList.toArray(new Club[clubList.size()]);
    }
    
    public Jump getJump(int jid) {
        Jump j = new Jump();
        try {
            rs_ = stat_.executeQuery("SELECT * FROM jumps WHERE jid = '"+jid+"'");
            while (rs_.next()) {
                System.out.println("new jump123");
                j = new Jump(rs_.getInt("break1"), rs_.getInt("engage"), rs_.getInt("break2"), "A0");
            }
            rs_.close();
        }
        catch (Exception e) {
            messageHandler_.setError(10);
            messageHandler_.setMoreDetails(e.toString());
        }
        return j;
    }
    
    //If we know we've already got the jump row loaded into "rs_" then use this. 
    Jump getJump() {
        return new Jump(resultGetInt("break1"), resultGetInt("engage"), resultGetInt("break2"), "A0");
    }
    
    public Routine getRoutine(int rid) {
        executeQuery("SELECT * FROM routines WHERE rid = '"+rid+"'");
        int numberOfJumps = resultGetInt("numberofjumps");
        String dateTime = resultGetString("datetime");
        String comments = resultGetString("comments");
               
        Routine r = new Routine(numberOfJumps, rid, dateTime, comments);
        
        try {
            rs_ = stat_.executeQuery("SELECT * FROM jumps WHERE routineid = '"+rid+"' ORDER BY jumpnumber ASC");
            while (rs_.next()) {
                r.addJump(getJump());
            }
        }
        catch (Exception e) {
            messageHandler_.setError(10);
            messageHandler_.setMoreDetails(e.toString());
        }
        

        
        return r;
    }
    
    public ComboItem[] getRoutineTags(int rid){
        executeQuery("SELECT * FROM tagmap WHERE routineid = '"+rid+"'");
        
        ArrayList<Integer> tagidList = new ArrayList<Integer>();
        try {
            while (rs_.next()) {
                tagidList.add(resultGetInt("tagid"));
            }
        }
        catch (Exception e) {
            messageHandler_.setError(10);
            messageHandler_.setMoreDetails(e.toString());
        }
        
        ComboItem[] tags = new ComboItem[tagidList.size()];
        
        int count = 0;
        for(int i:tagidList){
            executeQuery("SELECT tname FROM tags WHERE tid ='"+i+"'");
            try {
                tags[count] = new ComboItem(i,resultGetString("tname"));
            }
            catch (Exception e) {
                messageHandler_.setError(10);
                messageHandler_.setMoreDetails(e.toString());
            }
            count++;
        }
        
        return tags;
    }
    
    //Gets all a Gymnasts Routines
    public Routine[] getRoutinesForGymnast(int gid) {
        executeQuery("SELECT * FROM routines WHERE gymnastid = '"+gid+"'");
        
        ArrayList<String> idList = new ArrayList<String>();
        
        //First get the list of IDs.
        try {
            while (rs_.next()) {
                idList.add(resultGetString("rid"));
            }
        }
        catch (Exception e) {
            messageHandler_.setError(10);
            messageHandler_.setMoreDetails(e.toString());
        }
        
        //Then, we have the list of IDs, so create an array of routines and return it. 
        Routine[] r = new Routine[idList.size()];
        for (int i = 0; i < idList.size(); i++) {
            r[i] = getRoutine(Integer.parseInt(idList.get(i)));
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
            messageHandler_.setError(10);
            messageHandler_.setMoreDetails(e.toString());
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
            messageHandler_.setError(10);
            messageHandler_.setMoreDetails(e.toString());
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
            messageHandler_.setError(10);
            messageHandler_.setMoreDetails(e.toString());
        }

        return i;
    }
    
    private String resultGetString(String recordName) {
        String s = "";
        
        try {
            s = rs_.getString(recordName);
        }

        catch (Exception e) {
            messageHandler_.setError(10);
            messageHandler_.setMoreDetails(e.toString());
        }

        return s;
    }
}
