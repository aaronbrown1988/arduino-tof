/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package trampoline;
import java.io.File;
import java.util.ArrayList;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 * @author Andreas
 */
public class Read {
    public Jump[] createJumpList(String filename) {
        String[] stringList = readFileToList(filename);
        
        //For testing
        for (int j = 0; j < stringList.length; j++) {
            //System.out.println(j+" == "+stringList[j]);
        }
        
        //Convert the ArrayList into a list. 
        int numberOfJumps = stringList.length/2;
        //System.out.println("number of jumpst "+numberOfJumps);
        Jump[] jumpList = new Jump[numberOfJumps];
        for (int i = 0; i < numberOfJumps; i++) {
            //System.out.println("IIIIIII"+i);
            //System.out.println(i+ "a - "+ stringList[i*2]);
            //System.out.println(i+ "b - "+ stringList[i*2+1]);
            //System.out.println(i+ "c - "+ stringList[i*2+2]);
            jumpList[i] = new Jump(stringList[i*2], stringList[i*2+1], stringList[i*2+2], "location not set in read.java");
        }
        
        return jumpList;
    }
    
    public ArrayList<String> readFile(String filename) {
        ArrayList<String> stringList = new ArrayList<String>();
        
        try {
            File file = new File(filename);
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(file);
            doc.getDocumentElement().normalize();
            System.out.println("Root element " + doc.getDocumentElement().getNodeName());
            NodeList nodeLst = doc.getElementsByTagName("event");
            System.out.println("nodeLst.getLength() = "+nodeLst.getLength());
            
            String s1;
            String s2;

            for (int s = 0; s < nodeLst.getLength(); s++) {

                Node fstNode = nodeLst.item(s);

                if (fstNode.getNodeType() == Node.ELEMENT_NODE) {

                    Element fstElmnt = (Element) fstNode;
                    NodeList fstNmElmntLst = fstElmnt.getElementsByTagName("time");
                    Element fstNmElmnt = (Element) fstNmElmntLst.item(0);
                    NodeList fstNm = fstNmElmnt.getChildNodes();
                    //System.out.println("First Name : "  + ((Node) fstNm.item(0)).getNodeValue());
                    
                    NodeList lstNmElmntLst = fstElmnt.getElementsByTagName("type");
                    Element lstNmElmnt = (Element) lstNmElmntLst.item(0);
                    NodeList lstNm = lstNmElmnt.getChildNodes();
                    //System.out.println("Last Name : " + ((Node) lstNm.item(0)).getNodeValue());
                    
                    s1 = fstNm.item(0).getNodeValue();
                    s2 = lstNm.item(0).getNodeValue();
                    stringList.add(s1);
                }

            }
            
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return stringList;
    }
    
    public String[] readFileToList(String filename) {
        ArrayList<String> temp = readFile(filename);
        
        return temp.toArray(new String[temp.size()]);
    }
    
    public static void main(String[] args) {
        Read r;
        r = new Read();
        //r.readFile();
    }
}
