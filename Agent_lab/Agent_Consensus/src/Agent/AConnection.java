package Agent;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class AConnection {
    private Socket s;
    private DataInputStream in;
    private DataOutputStream out;
    private AGui AGui;
    private AListener al = new AListener();

    public void setAGui(AGui AGui){
        this.AGui = AGui;
    }

    public void connect(){
        try{
            s = new Socket("163.239.200.192", 7777);
            out = new DataOutputStream(s.getOutputStream());
            in = new DataInputStream(s.getInputStream());
            while (true){
                al.messageHandler(in.readUTF(), out);
            }

        } catch (IOException e){
            e.printStackTrace();
        }
    }




}
