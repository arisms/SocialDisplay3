package fi.tut.cs.social.socialdisplay3;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by Aris on 22/02/16.
 */
public class Profile implements Serializable{

    private String name;
    private String type;
    private List<String> apps = new ArrayList<String>();
    private Boolean active;

    public Profile() {
    }

    public Profile(String name, String type) {
        this.name = name;
        this.type = type;
        this.active = false;
    }

    protected Profile(Parcel in) {
        name = in.readString();
        type = in.readString();
    }

    public void setName(String name) {
        this.name = name;
    }
    public String getName() {
        return this.name;
    }

    public void setType(String type) {
        this.type = type;
    }
    public String getType() {
        return this.type;
    }

    public void addApp(String app) {
        this.apps.add(app);
    }
    public List<String> getApps() {
        return this.apps;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }
    public Boolean getActive() {
        return this.active;
    }

}
