package fred.docapp;


import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Arrays;


class Term<LIT> implements Comparator<Term<LIT>> {
    enum TermType { LITERAL, AND, OR, NOT, FILE, DIR, PATH, CSI, TRUE, FALSE, BEGIN, END, EXACT };
    TermType type;
    LIT literal = null;
    List<Term<LIT>> terms = null;

    Term(boolean b) {
	if (b) this.type = TermType.TRUE;
	else this.type = TermType.FALSE;
    }

    Term(LIT s) {
	this.type = TermType.LITERAL;
	this.literal = s;
    }

    Term(TermType type, Term<LIT> term) {
	this.type = type;
	if (
	    type==TermType.NOT
	    || type==TermType.FILE
	    || type==TermType.BEGIN
	    || type==TermType.END
	    || type==TermType.EXACT
	    || type==TermType.DIR
	    || type==TermType.PATH
	    || type==TermType.CSI
	    ) {
	    this.terms = new ArrayList<Term<LIT>>();
	    this.terms.add(term);
	}
	else throw new RuntimeException();
    }

    Term(TermType type, List<Term<LIT>> terms) {
	this.type = type;
	if (type != TermType.LITERAL) {
	    this.terms = terms;
	    sort(terms);
	}
	else throw new RuntimeException();
    }

    public String printTerms(List<Term<LIT>> terms) {
	String result = "";

	for (Term term : terms)
	    result += (result==""?"":" ")+term;

	return result;
    }

    public String toString() {
	if (type == TermType.LITERAL) {
		if (literal instanceof byte[]) {
			byte[] buf = (byte[]) literal;
			return "\"" + LogicMatcher.printByteArray(buf,buf.length) + "\"";
		} else return "\"" + literal + "\"";
	}
	else if (type == TermType.EXACT)
	    return "^ $ "+terms.get(0);
	else if (type == TermType.BEGIN)
	    return "^ "+terms.get(0);
	else if (type == TermType.END)
	    return "$ "+terms.get(0);
	else if (type == TermType.FILE)
	    return "file ("+terms.get(0) + ")";
	else if (type == TermType.DIR)
	    return "dir ("+terms.get(0) + ")";
	else if (type == TermType.PATH)
	    return "path ("+terms.get(0) + ")";
	else if (type == TermType.CSI)
	    return "csi ("+terms.get(0) + ")";
	else if (type == TermType.NOT)
	    return "not ("+terms.get(0) + ")";
	else if (type == TermType.OR)
	    return "or (" +printTerms(terms)+ ")";
	else if (type == TermType.AND)
	    return "and (" + printTerms(terms) + ")";
	else if (type == TermType.TRUE)
	    return "tt";
	else if (type == TermType.FALSE)
	    return "ff";
	else
	    throw new RuntimeException();
    }

    public int compare(Term<LIT> t1, Term<LIT> t2) {
	if (typeValue(t1.type) == typeValue(t2.type)) return 0;
	else if (typeValue(t1.type) < typeValue(t2.type)) return -1;
	else return 1;
    }

    int typeValue(TermType type) {
	if (type == TermType.LITERAL) return 0;
	else if (type == TermType.CSI) return 1;
	else if (type == TermType.FILE) return 2;
	else if (type == TermType.DIR) return 3;
	else if (type == TermType.PATH) return 4;
	else if (type == TermType.OR) return 5;
	else if (type == TermType.NOT) return 6;
	else if (type == TermType.BEGIN) return 7;
	else if (type == TermType.END) return 8;
	else if (type == TermType.TRUE) return 9;
	else if (type == TermType.FALSE) return 10;
	else if (type == TermType.EXACT) return 11;
	else throw new RuntimeException();
    }

    void sort(List<Term<LIT>> terms) {
	if (terms.size() > 1) 
	    Collections.sort(terms,terms.get(0));
    }

    //======================================================================

    static public Term<String> parse(String input) throws ParseException {
	Scan scan = new Scan();
	List<Pair<Scan.TokenType,String>> tokens = scan.tokens(input);
	return parse_and(tokens);
    }

    static Term<String> parse_and(List<Pair<Scan.TokenType,String>> tokens) throws ParseException {
	List<Term<String>> terms = parse_tokens(tokens);

	if (terms.size() == 1)
	    return terms.get(0);
	else if (terms.size() > 1) {
	    return new Term<String>(TermType.AND, terms);
	} else
	    throw new ParseException("empty conjunction");
    }

    static List<Term<String>> parse_tokens(List<Pair<Scan.TokenType,String>> tokens)
	throws ParseException {
	List<Term<String>> ands = new ArrayList<Term<String>>();

	while (!tokens.isEmpty()) {
	    Pair<Scan.TokenType,String> pair = tokens.get(0);
	    Scan.TokenType type = pair.left;
	    String value = pair.right;
	    tokens.remove(0);
	    //System.out.println("Token="+type+" value="+value);

	    if (type == Scan.TokenType.TRUE)
		ands.add(new Term<String>(true));
	    else if (type == Scan.TokenType.FALSE)
		ands.add(new Term<String>(false));
	    else if (type == Scan.TokenType.LEFTPAR)
		ands.add(parse_paren(tokens,true));
	    else if (type == Scan.TokenType.NEG)
		ands.add(new Term<String>(TermType.NOT,parse_not(tokens)));
	    else if (type == Scan.TokenType.BEGIN) {
		Pair<Scan.TokenType,String> newPair = tokens.get(0);
		if (newPair.left == Scan.TokenType.END) {
		    tokens.remove(0);
		    ands.add(new Term<String>(TermType.EXACT,parse_csi_word(tokens)));
		} else 
		    ands.add(new Term<String>(TermType.BEGIN,parse_csi_word(tokens)));
	    }
	    else if (type == Scan.TokenType.END)  {
		Pair<Scan.TokenType,String> newPair = tokens.get(0);
		if (newPair.left == Scan.TokenType.BEGIN) {
		    tokens.remove(0);
		    ands.add(new Term<String>(TermType.EXACT,parse_csi_word(tokens)));
		} else 
		    ands.add(new Term<String>(TermType.END,parse_csi_word(tokens)));
	    }
	    else if (type == Scan.TokenType.FILE)
		ands.add(new Term<String>(TermType.FILE,parse_csi_anchor_word(tokens)));
	    else if (type == Scan.TokenType.DIR)
		ands.add(new Term<String>(TermType.DIR,parse_csi_anchor_word(tokens)));
	    else if (type == Scan.TokenType.PATH)
		ands.add(new Term<String>(TermType.PATH,parse_csi_word(tokens)));
	    else if (type == Scan.TokenType.CSI)
		ands.add(new Term<String>(TermType.CSI,parse_word(tokens)));
	    else if (type == Scan.TokenType.WORD)
		ands.add(new Term<String>(value));
	    else if (type == Scan.TokenType.QUOTEDWORD)
		ands.add(new Term<String>(value.replace("\"","")));
	    else {
		tokens.add(0,pair);
		break;
	    }
	}
	return ands;
    }

    static Term<String> parse_paren(List<Pair<Scan.TokenType,String>> tokens,
				    boolean isOr)
	throws ParseException {
	TermType t;
	if (isOr) t = TermType.OR;
	else t = TermType.AND;

	List<Term<String>> terms = parse_tokens(tokens);
	
	Pair<Scan.TokenType,String> pair = tokens.get(0);
	Scan.TokenType type = pair.left;
	String value = pair.right;

	if (type == Scan.TokenType.RIGHTPAR) {
	    tokens.remove(0);
	    if (terms.size() == 1)
		return terms.get(0);
	    else if (terms.size() > 1) {
		return new Term<String>(t, terms);
	    } else
		throw new ParseException("empty disjuncion");
	} else throw new ParseException("right parenthesis missing");
    }

    static Term<String> parse_not(List<Pair<Scan.TokenType,String>> tokens)	throws ParseException {
	    Pair<Scan.TokenType,String> pair = tokens.get(0);
	    Scan.TokenType type = pair.left;
	    String value = pair.right;
	    tokens.remove(0);

	    if (type == Scan.TokenType.BEGIN) {
		Pair<Scan.TokenType,String> newPair = tokens.get(0);
		if (newPair.left == Scan.TokenType.END) {
		    tokens.remove(0);
		    return new Term<String>(TermType.EXACT,parse_csi_word(tokens));
		} else 
		    return new Term<String>(TermType.BEGIN,parse_csi_word(tokens));
	    }
	    else if (type == Scan.TokenType.END) {
		Pair<Scan.TokenType,String> newPair = tokens.get(0);
		if (newPair.left == Scan.TokenType.BEGIN) {
		    tokens.remove(0);
		    return new Term<String>(TermType.EXACT,parse_csi_word(tokens));
		} else 
		    return new Term<String>(TermType.END,parse_csi_word(tokens));
	    }
	    else if (type == Scan.TokenType.FILE) 
		return new Term<String>(TermType.FILE,parse_csi_anchor_word(tokens));
	    else if (type == Scan.TokenType.LEFTPAR) 
		return parse_paren(tokens,false);
	    else if (type == Scan.TokenType.DIR)
		return new Term<String>(TermType.DIR,parse_csi_anchor_word(tokens));
	    else if (type == Scan.TokenType.PATH)
		return new Term<String>(TermType.PATH,parse_csi_word(tokens));
	    else if (type == Scan.TokenType.CSI)
		return new Term<String>(TermType.CSI,parse_word(tokens));
	    else if (type == Scan.TokenType.WORD)
		return new Term<String>(value);
	    else if (type == Scan.TokenType.QUOTEDWORD)
		return new Term<String>(value.replace("\"",""));
	    else
		throw new ParseException("type "+type+" not understood in context");
    }

    static Term<String> parse_word(List<Pair<Scan.TokenType,String>> tokens)
	throws ParseException {
	    Pair<Scan.TokenType,String> pair = tokens.get(0);
	    Scan.TokenType type = pair.left;
	    String value = pair.right;
	    tokens.remove(0);

	    if (type == Scan.TokenType.WORD)
		return new Term<String>(value);
	    else if (type == Scan.TokenType.QUOTEDWORD)
		return new Term<String>(value.replace("\"", ""));
	    else
		throw new ParseException("type "+type+" is not a word");
    }

    static Term<String> parse_csi_word(List<Pair<Scan.TokenType,String>> tokens)
	throws ParseException {
	    Pair<Scan.TokenType,String> pair = tokens.get(0);
	    Scan.TokenType type = pair.left;
	    String value = pair.right;
	    tokens.remove(0);

	    if (type == Scan.TokenType.WORD)
		return new Term<String>(value);
	    else if (type == Scan.TokenType.QUOTEDWORD)
		return new Term<String>(value.replace("\"",""));
	    else if (type == Scan.TokenType.CSI) {
		Term<String> word = parse_word(tokens);
		return new Term<String>(TermType.CSI,word);
	    }
	    else
		throw new ParseException("type "+type+" is not a word");
    }

    static Term<String> parse_csi_anchor_word(List<Pair<Scan.TokenType,String>> tokens)
	throws ParseException {
	    Pair<Scan.TokenType,String> pair = tokens.get(0);
	    Scan.TokenType type = pair.left;
	    String value = pair.right;
	    tokens.remove(0);

	    if (type == Scan.TokenType.BEGIN) {
		Pair<Scan.TokenType,String> newPair = tokens.get(0);
		if (newPair.left == Scan.TokenType.END) {
		    tokens.remove(0);
		    return new Term<String>(TermType.EXACT,parse_csi_word(tokens));
		} else 
		    return new Term<String>(TermType.BEGIN,parse_csi_word(tokens));
	    }
	    else if (type == Scan.TokenType.END) {
		Pair<Scan.TokenType,String> newPair = tokens.get(0);
		if (newPair.left == Scan.TokenType.BEGIN) {
		    tokens.remove(0);
		    return new Term<String>(TermType.EXACT,parse_csi_word(tokens));
		} else 
		    return new Term<String>(TermType.END,parse_csi_word(tokens));
	    }
	    else if (type == Scan.TokenType.WORD)
		return new Term<String>(value);
	    else if (type == Scan.TokenType.QUOTEDWORD)
		return new Term<String>(value.replace("\"",""));
	    else if (type == Scan.TokenType.CSI) {
		Term<String> word = parse_word(tokens);
		return new Term<String>(TermType.CSI,word);
	    }
	    else
		throw new ParseException("type "+type+" is not a word");
    }

    static public void main(String args[]) {
	try {
	    Scan scan = new Scan();
	    BufferedReader in =
		new BufferedReader(new InputStreamReader(System.in));
	    String s;
	    do {
		System.out.print("input: ");
		System.out.flush();
		String input = in.readLine();
		try {
		    System.out.println("Term: "+parse(input));
		} catch (ParseException exc) {
		    System.out.println("Could not parse "+input+" due to "+exc);
		}
	    } while(true);
	}
	catch (IOException exc) { };
    }
}
