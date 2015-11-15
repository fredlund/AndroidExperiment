public class FileTransferRequest implements Parceable {
    String host;
    String userName;
    String passWord;
    String[] files;

    public FileTransferRequest(String host,
			       String userName,
			       String passWord,
			       String[] files) {
	this.host = host;
	this.userName = userName;
	this.passWord = passWord;
	this.files = files;
    }

    public FileTransferRequest(Parcel in) {
	FileTransferRequest(in.readString(),
			    in.readString(),
			    in.readString(),
			    in.readStringArray());
    }

    public int describeContents() {
	return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
	dest.writeString(host);
	dest.writeString(userName);
	dest.writeString(passWord);
	dest.writeStringArray(files);
    }

    public static final Parelable.Creator CREATOR =
	new Parceable.Creator() {
	    public FileTransferRequest createFromParcel(Parcel in) {
		return new FileTransferRequest(in);
	    }

	    public FileTransferRequest[] newArray(int size) {
		return new FileTransferRequest[size];
	    }
	};

}
