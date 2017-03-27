package zju.homework.augmentedstudio.Container;

import android.os.Parcel;
import android.os.Parcelable;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Created by stardust on 2017/1/7.
 */

@JsonIgnoreProperties(ignoreUnknown = true)
public class ObjectInfoData implements Parcelable{
    private String name;
    private String imageUrl;
    private String filename;

    public ObjectInfoData(){

    }

    public ObjectInfoData(Parcel parcel){
        String[] data = new String[3];
        parcel.readStringArray(data);
        this.name = data[0];
        this.imageUrl = data[1];
        this.filename = data[2];
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeStringArray(new String[]{
                this.name,
                this.imageUrl,
                this.filename
        });
    }

    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public ObjectInfoData createFromParcel(Parcel in) {
            return new ObjectInfoData(in);
        }

        public ObjectInfoData[] newArray(int size) {
            return new ObjectInfoData[size];
        }
    };
}
