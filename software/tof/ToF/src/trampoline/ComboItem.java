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
    private String id;
    private String name;

    public ComboItem(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public ComboItem(int id, String name) {
        this.id = Integer.toString(id);
        this.name = name;
    }

    public String getID() {
        return this.id;
    }

    //Shortcut only
    public int getNumericID() {
        return Integer.parseInt(this.id);
    }

    public String getName() {
        return this.name;
    }

    @Override
    public String toString() {
        return name;
    }
}