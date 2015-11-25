package fred.docapp;

import java.io.UnsupportedEncodingException;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;

public class LogicMatcher {
    static public Term<byte[]> convert(Term<String> stringTerm) {
	if (stringTerm.terms == null) {
	    try {
		byte[] bytes = stringTerm.literal.getBytes("UTF-8");
		return new Term<byte[]>(bytes);
	    } catch (UnsupportedEncodingException exc) {
		System.out.println("Cannot encode string in UTF-8");
		throw new RuntimeException();
	    }
	} else {
	    List<Term<byte[]>> list = new ArrayList<Term<byte[]>>();
	    for (Term<String> stringSubTerm : stringTerm.terms)
		list.add(convert(stringSubTerm));
	    return new Term<byte[]>(stringTerm.type,list);
	}
    }

    static public int match(MySlowReader reader,
			    Term<byte[]> term,
			    byte[] save,
			    boolean isDir,
			    byte[] path,
			    int pathMax) throws IOException {
	return match(reader,term,save,isDir,true,0,path,pathMax);
    }

    static public int match(MySlowReader reader,
			    Term<byte[]> matchTerm,
			    byte[] save,
			    boolean isDir,
			    boolean firstCall,
			    int bsMax,
			    byte[] path,
			    int pathMax) throws IOException {

	if (bsMax < 0)
	    throw new RuntimeException();

	if (matchTerm.type == Term.TermType.LITERAL) {
	    return reader.bytesMatch(matchTerm.literal,firstCall,save,bsMax,true);
	} else {
	    switch (matchTerm.type) {
	    case AND:
		{
		    for (Term<byte[]> subTerm : matchTerm.terms) {
			bsMax = match(reader,subTerm,save,isDir,firstCall,Math.abs(bsMax),path,pathMax);
			firstCall = false;
			if (bsMax < 0) return bsMax;
		    }

		    return bsMax;
		}

	    case OR:
		{
		    for (Term<byte[]> subTerm : matchTerm.terms) {
			bsMax = match(reader,subTerm,save,isDir,firstCall,Math.abs(bsMax),path,pathMax);
			firstCall = false;
			if (bsMax > 0) return bsMax;
		    }

		    return bsMax;
		}

	    case NOT:
		Term<byte[]> nTerm = matchTerm.terms.get(0);
		return -1*match(reader,nTerm,save,isDir,firstCall,bsMax,path,pathMax);

	    case CSI: {
		byte[] literal = matchTerm.terms.get(0).literal;
		return reader.bytesMatch(literal,firstCall,save,bsMax,false);
	    }

	    case FILE: {
		Term<byte[]> qTerm = matchTerm.terms.get(0);

		if (isDir) {
		    if (firstCall) 
			return neg(match(reader,qTerm,save,isDir,firstCall,bsMax,path,pathMax));
		    else return neg(bsMax);
		} else return match(reader,qTerm,save,isDir,firstCall,bsMax,path,pathMax);
	    }

	    case DIR: {
		Term<byte[]> qTerm = matchTerm.terms.get(0);

		if (!isDir) {
		    if (firstCall) 
			return neg(match(reader,qTerm,save,isDir,firstCall,bsMax,path,pathMax));
		    else return neg(bsMax);
		} else return match(reader,qTerm,save,isDir,firstCall,bsMax,path,pathMax);
	    }

	    case PATH: {
		Term<byte[]> qTerm = matchTerm.terms.get(0);

		if (isDir) {
		    if (firstCall)
			return neg(match(reader,qTerm,save,isDir,firstCall,bsMax,path,pathMax));
		else
		    return neg(bsMax);
		} else {
		    if (firstCall) 
			bsMax = match(reader,qTerm,save,isDir,firstCall,bsMax,path,pathMax);
		    int pathBsMax = 0;
		    // We have to check whether there is a CSI node or not
		    if (qTerm.literal != null)
			pathBsMax = reader.bytesMatch(qTerm.literal,false,path,pathMax,true);
		    else if (qTerm.type == Term.TermType.CSI)
			pathBsMax = reader.bytesMatch(qTerm.terms.get(0).literal,false,path,pathMax,false);
		    else throw new RuntimeException();

		    if (pathBsMax < 0) return neg(bsMax);
		    else return bsMax;
		}
	    }

	    default:
		break;
	    }
	}

	return 0;
    }

    static int neg(int n) {
	if (n <= 0) return n;
	else return -1*n;
    }

    static public String printByteArray(byte[] bytes, int max) {
	String result = "";
	for (int i=0; i<max; i++)
	    result += ((char) bytes[i]);
	return result;
    }
}
