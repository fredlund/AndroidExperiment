package fred.docapp;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import java.util.regex.*;

public class Scan {
    enum TokenType { LEFTPAR, RIGHTPAR, NEG, FILE, PATH, DIR, WORD, CSI, QUOTEDWORD, BEGIN, END, TRUE, FALSE };

    List<Pair<TokenType,Pattern>> patterns = null;
    
    public Scan() {
	this.patterns = new ArrayList<Pair<TokenType,Pattern>>();
	this.patterns.add(new Pair<TokenType,Pattern>(TokenType.LEFTPAR, Pattern.compile("^\\s*(\\()")));
	this.patterns.add(new Pair<TokenType,Pattern>(TokenType.RIGHTPAR, Pattern.compile("^\\s*(\\))")));
	this.patterns.add(new Pair<TokenType,Pattern>(TokenType.NEG, Pattern.compile("^\\s*(\\-)")));
	this.patterns.add(new Pair<TokenType,Pattern>(TokenType.BEGIN, Pattern.compile("^\\s*(\\^)")));
	this.patterns.add(new Pair<TokenType,Pattern>(TokenType.END, Pattern.compile("^\\s*(\\$)")));
	this.patterns.add(new Pair<TokenType,Pattern>(TokenType.TRUE, Pattern.compile("^\\s*(tt)")));
	this.patterns.add(new Pair<TokenType,Pattern>(TokenType.FALSE, Pattern.compile("^\\s*(ff)")));
	this.patterns.add(new Pair<TokenType,Pattern>(TokenType.FILE, Pattern.compile("^\\s*(file)")));
	this.patterns.add(new Pair<TokenType,Pattern>(TokenType.DIR, Pattern.compile("^\\s*(dir)")));
	this.patterns.add(new Pair<TokenType,Pattern>(TokenType.PATH, Pattern.compile("^\\s*(path)")));
	this.patterns.add(new Pair<TokenType,Pattern>(TokenType.CSI, Pattern.compile("^\\s*(csi)")));
	this.patterns.add(new Pair<TokenType,Pattern>(TokenType.WORD, Pattern.compile("^\\s*([\\S&&[^\\(\\)]]+)")));
	this.patterns.add(new Pair<TokenType,Pattern>(TokenType.QUOTEDWORD, Pattern.compile("^\\s*(\\x22.*?\\x22)")));
    }

    List<Pair<TokenType,String>> tokens(String input) {
	List<Pair<TokenType,String>> tokens = 
	    new ArrayList<Pair<TokenType,String>>();
	Pair<Pair<TokenType,String>,String> tokenAndString;

	while ((tokenAndString = nextToken(input)) != null) {
	    tokens.add(tokenAndString.left);
	    input = tokenAndString.right;
	}

	return tokens;
    }

    Pair<Pair<TokenType,String>,String> nextToken(String input) {
	for (Pair<TokenType,Pattern> pat : patterns) {
	    Matcher matcher = pat.right.matcher(input);
	    if (matcher.find()) {
		String group = matcher.group(1);
		Pair<TokenType,String> token =
		    new Pair<TokenType,String>(pat.left,group);
		int endSymbol = matcher.end(1);
		input = input.substring(endSymbol);
		return new Pair<Pair<TokenType,String>,String>(token,input);
	    }
	}
	return null;
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
		List<Pair<TokenType,String>> tokens = scan.tokens(input);
		System.out.println("Tokens: "+tokens);
	    } while(true);
	} catch (IOException exc) { };
    }
}
