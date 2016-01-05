package fred.docapp;

/**
 * Created by fred on 8/11/15.
 *
 */
public class UserHost {
    String user;
    String host;

    public UserHost(String user, String host) {
        this.user = user;
        this.host = host;
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        else if (o instanceof UserHost) {
            UserHost uh = (UserHost) o;
            return this.host.equals(uh.host) && this.user.equals(uh.user);
        }
        else return false;
    }

    @Override
    public int hashCode() {
        return 37*user.hashCode()+host.hashCode();
    }

    public String toString() {
        return user + "@" + host;
    }
}
