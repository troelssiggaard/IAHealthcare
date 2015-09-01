package dk.troelssiggaard.iacontacts;

/**
 * Created by ts.
 */

public class ListItem {

    private String name = "";
    private int profilePicture;
    private String title = "";
    private String department = "";
    private String currentLocation = "";
    private String currentActivity = "";
    private String interruptibility = "";
    private int intPicture;

    public ListItem(int profilePicture, String name, String title, String department, int intPicture) {
        this.name = name;
        this.intPicture = intPicture;

        setProfilePicture(profilePicture);
        setTitle(title);
        setDepartment(department);
    }


    public void setProfilePicture(int profilePicture) {
        this.profilePicture = profilePicture;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public void setCurrentLocation(String currentLocation) {
        this.currentLocation = currentLocation;
    }

    public void setInterruptibility(String interruptibility) {
        this.interruptibility = interruptibility;
    }

    public String getName() {
        return this.name;
    }

    public int getProfilePicture() {
        return this.profilePicture;
    }

    public String getTitle() {
        return this.title;
    }

    public String getDepartment() {
        return this.department;
    }

    public int getIntPicture() {
        return this.intPicture;
    }
}
