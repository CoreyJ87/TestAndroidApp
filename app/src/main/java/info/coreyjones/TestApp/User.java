package info.coreyjones.TestApp;

import android.databinding.BaseObservable;
import android.databinding.Bindable;

/**
 * Created by CJones on 7/10/2015.
 */
public class User extends BaseObservable {
    private String firstName;
    private String lastName;

    public User(String firstName, String lastName) {
        this.firstName = firstName;
        this.lastName = lastName;
    }

    @Bindable
    public String getFirstName() {
        return this.firstName;
    }

    @Bindable
    public String getLastName() {
        return this.lastName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
        notifyPropertyChanged(info.coreyjones.TestApp.BR.firstName);
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
        notifyPropertyChanged(info.coreyjones.TestApp.BR.lastName);
    }
}
