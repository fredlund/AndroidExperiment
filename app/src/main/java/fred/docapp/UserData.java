package fred.docapp;

/**
 * Created by fred on 8/11/15.
 *
 */
public class UserData {
    UserHost uh;
    String password;

    public UserData(UserHost uh, String password) {
        this.uh = uh;
        this.password = password;
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        else if (o instanceof UserData) {
            UserData ud = (UserData) o;
            return this.uh.equals(ud.uh) && this.password.equals(ud.password);
        }
        else return false;
    }

    public String toString() {
        return "{" + password + "," + uh + "}";
    }
}
