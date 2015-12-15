package fred.docapp;

import android.os.Parcel;
import android.os.Parcelable;

public class FileTransferRequest implements Parcelable {
	String library;
    String host;
	int port;
    String userName;
    String passWord;
    String[] files;
	int requestNo;
	String localDir;
	boolean tryOpen;

    public FileTransferRequest(String library,
							   String host,
							   int port,
			       String userName,
			       String passWord,
			       String[] files,
							   boolean tryOpen,
							   String localDir,
							   	int requestNo) {
		this.library = library;
	this.host = host;
		this.port = port;
	this.userName = userName;
	this.passWord = passWord;
	this.files = files;
		this.tryOpen = tryOpen;
		this.localDir = localDir;
		this.requestNo = requestNo;
    }

    public FileTransferRequest(Parcel in) {
		int length = in.readInt();
		library = in.readString();
		host = in.readString();
		port = in.readInt();
		userName = in.readString();
		passWord = in.readString();
		files = new String[length];
		in.readStringArray(files);
		tryOpen = in.readInt()==0 ? false : true;
		localDir = in.readString();
		requestNo = in.readInt();
    }

    public int describeContents() {
	return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
		dest.writeInt(files.length);
		dest.writeString(library);
	dest.writeString(host);
		dest.writeInt(port);
	dest.writeString(userName);
	dest.writeString(passWord);
	dest.writeStringArray(files);
		dest.writeInt(tryOpen ? 1 : 0);
		dest.writeString(localDir);
		dest.writeInt(requestNo);
    }

    public static final Parcelable.Creator CREATOR =
	new Parcelable.Creator() {
	    public FileTransferRequest createFromParcel(Parcel in) {
		return new FileTransferRequest(in);
	    }

	    public FileTransferRequest[] newArray(int size) {
		return new FileTransferRequest[size];
	    }
	};

    public String toString() {
        StringBuffer fileStr = new StringBuffer();
        for (String file : files)
            fileStr.append(file+";");
        return "ftr: {" + requestNo + "," +library + "," + host + "," + userName + "," + passWord + "," + fileStr + "," + tryOpen + "," + localDir + "}";
    }

}
