package fred.docapp;

import android.os.Parcel;
import android.os.Parcelable;

public class FileTransferRequest implements Parcelable {
	String library;
    String host;
    String userName;
    String passWord;
    String[] files;
	int requestNo;
	String localDir;

    public FileTransferRequest(String library,
							   String host,
			       String userName,
			       String passWord,
			       String[] files,
							   String localDir,
							   	int requestNo) {
		this.library = library;
	this.host = host;
	this.userName = userName;
	this.passWord = passWord;
	this.files = files;
		this.localDir = localDir;
		this.requestNo = requestNo;
    }

    public FileTransferRequest(Parcel in) {
		int length = in.readInt();
		library = in.readString();
		host = in.readString();
		userName = in.readString();
		passWord = in.readString();
		files = new String[length];
		in.readStringArray(files);
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
	dest.writeString(userName);
	dest.writeString(passWord);
	dest.writeStringArray(files);
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
        return "ftr: {" + requestNo + "," +library + "," + host + "," + userName + "," + passWord + "," + fileStr + "," + localDir + "}";
    }

}
