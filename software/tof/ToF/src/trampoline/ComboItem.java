/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package trampoline;

/**
 *
 * @author Andreas
 */
public class ComboItem {
    private String id_;
    private String name_;

    public ComboItem(String id, String name) {
        this.id_ = id;
        this.name_ = name;
    }

    public ComboItem(int id, String name) {
        this.id_ = Integer.toString(id);
        this.name_ = name;
    }

    public ComboItem(int id, int name) {
        this.id_ = Integer.toString(id);
        this.name_ = Integer.toString(name);;
    }
    
    @Override
    public boolean equals(Object obj)
    {
        //System.out.println("testing for equality ("+name_+")");
        if(this == obj) {
            return true;
        }
        //System.out.println("not same object");
        if(obj == null) {
            System.out.println("null object");
            return false;
        }
        //System.out.println("a");
        if (obj.getClass() != this.getClass()) {
            System.out.println("classes different"+obj.getClass()+"---"+this.getClass());
            return false;
        }
        //System.out.println("b");
        ComboItem otherItem = (ComboItem)obj;
        
        //System.out.println("c");
        System.out.println("is same class, and name="+name_+", othername="+otherItem.getName()+", equals is ");
        return name_ == otherItem.getName();
    }

    @Override
    public int hashCode() {
        final int seed = 37;
        int result = 1;
        result = seed * result + ((name_ == null) ? 0 : name_.hashCode());
        result = seed * result + Integer.parseInt(id_);
        return result;
    }

    public String getID() {
        return this.id_;
    }

    //Shortcut only
    public int getNumericID() {
        return Integer.parseInt(this.id_);
    }

    public String getName() {
        return this.name_;
    }

    @Override
    public String toString() {
        return name_;
    }
}