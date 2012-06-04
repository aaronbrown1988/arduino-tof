/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package trampoline;

/**
 *
 * @author Andreas
 */
public class Category {
    private int id_;
    private String name_;
    
    Category(int id, String name) {
        this.id_ = id;
        this.name_ = name;
    }
    
    public int getID() {
        return id_;
    }
    
    public String getName() {
        return name_;
    }
}
