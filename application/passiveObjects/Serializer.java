package bgu.spl.mics.application.passiveObjects;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

public class Serializer {

    private String filename;
    private Object object;

    public Serializer(String filename, Object object){
        this.filename = filename;
        this.object = object;
    }

    public void serialize(){
        try
        {
            FileOutputStream fos =
                    new FileOutputStream(filename);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(object);
            oos.close();
            fos.close();
        } catch(IOException ioe){}
    }
}