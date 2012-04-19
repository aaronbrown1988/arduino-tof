/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package trampoline;

import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;
import java.io.InputStream;

/**
 *
 * @author Kieran
 */
public class Handshake implements SerialPortEventListener{
    private InputStream input_;
    private String response_;
    private int error_;
    
    Handshake(InputStream input){
        this.input_ = input;
        this.response_ = "";
        this.error_ = 0;
    }
    
    public synchronized void serialEvent(SerialPortEvent oEvent){
        if (oEvent.getEventType() == SerialPortEvent.DATA_AVAILABLE) {
            try{   
                StringBuilder tempStr = new StringBuilder();

                byte chunk = (byte)this.input_.read();
                while((char)chunk!='\n'){
                    tempStr.append((char)chunk);
                    chunk = (byte)this.input_.read();
                }
                this.response_= tempStr.toString();
            }catch (Exception e){
                this.error_ = 1;
            }
        }
    }
    
    public String getResponse(){
        return this.response_;
    }
    
    public int getError(){
        return this.error_;
    }
}
