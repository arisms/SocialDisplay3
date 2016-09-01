package fi.tut.cs.social.socialdisplay3;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

/**
 * Created by Aris on 26/02/16.
 */
public class App implements Serializable{

    private String name;
    private Bitmap icon;
    private Boolean selected;

    public App(String name, Bitmap icon, Boolean selected) {
        this.name = name;
        this.icon = icon;
        this.selected = selected;
    }

    public void setName(String name) {
        this.name = name;
    }
    public String getName() {
        return this.name;
    }

    public void setIcon(Bitmap icon) {
        this.icon = icon;
    }
    public Bitmap getIcon() {
        return this.icon;
    }

    public void setSelected(Boolean selected) {
        this.selected = selected;
    }
    public Boolean getSelected() {
        return this.selected;
    }


    private void writeObject(ObjectOutputStream oos) throws IOException {
        // This will serialize all fields that you did not mark with 'transient'
        // (Java's default behaviour)
        oos.defaultWriteObject();

        // Now, manually serialize all transient fields that you want to be serialized
        if(icon != null){
            ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
            boolean success = icon.compress(Bitmap.CompressFormat.PNG, 100, byteStream);
            if(success){
                oos.writeObject(byteStream.toByteArray());
            }
        }
    }

    private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException{
        // Now, all again, deserializing - in the SAME ORDER!
        // All non-transient fields
        ois.defaultReadObject();

        // All other fields that you serialized
        byte[] image = (byte[]) ois.readObject();
        if(image != null && image.length > 0){
            icon = BitmapFactory.decodeByteArray(image, 0, image.length);
        }
    }

}
