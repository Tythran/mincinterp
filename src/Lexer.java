import java.io.IOException;

public class Lexer
{
    private static final char EOF        =  0;
    private static final int BUFFER_SIZE = 10;

    private Parser         yyparser; // parent parser object
    private java.io.Reader reader;   // input stream
    public int             lineno;   // line number
    public int             column;   // column
    private char[]         inputBuffer; // buffer to store file content
    private char[] buffer1 = new char[BUFFER_SIZE];
    private char[] buffer2 = new char[BUFFER_SIZE];
    private boolean usingBuffer1 = true;
    private int forward = 0;
    private int lexBegin = 0;
    private int columntracker = 0;
    private StringBuilder lexemeBuilder = new StringBuilder();
    String[] keywords = {"int", "print", "if", "else", "while", "void"};

    public Lexer(java.io.Reader reader, Parser yyparser) throws Exception
    {
        this.reader   = reader;
        this.yyparser = yyparser;
        lineno = 1;
        column = 1;
        fillBuffers();
    }

    private void fillBuffers() throws IOException {
        int read1 = reader.read(buffer1, 0, BUFFER_SIZE);
        int read2 = reader.read(buffer2, 0, BUFFER_SIZE);
        if (read1 == -1) {
            buffer1[0] = EOF;
        } else if (read1 < BUFFER_SIZE) {
            buffer1[read1] = EOF;
        }
        if (read2 == -1) {
            buffer2[0] = EOF;
        } else if (read2 < BUFFER_SIZE) {
            buffer2[read2] = EOF;
        }
    }

    private char NextChar() throws IOException {
        if (forward >= BUFFER_SIZE) {
            usingBuffer1 = !usingBuffer1;
            forward = 0;
            if (!usingBuffer1) {
                int read = reader.read(buffer1, 0, BUFFER_SIZE);
                if (read == -1) {
                    buffer1[0] = EOF;
                } else if (read < BUFFER_SIZE) {
                    buffer1[read] = EOF;
                }
            } else {
                int read = reader.read(buffer2, 0, BUFFER_SIZE);
            if (read == -1) {
                buffer2[0] = EOF;
            } else if (read < BUFFER_SIZE) {
                buffer2[read] = EOF;
            }
            }
        }
        
        char c = usingBuffer1 ? buffer1[forward] : buffer2[forward];
        forward++;
        columntracker++;
        lexemeBuilder.append(c);
        return c;
    }

    public int Fail()
    {
        yyparser.yylval = new ParserVal((Object)yytext());
        return -1;
    }

    private void UngetChar() {
        if (forward > 0) {
            forward--;
            columntracker--;
            lexemeBuilder.setLength(lexemeBuilder.length() - 1);
        }
    }

    private String yytext() {
        return lexemeBuilder.toString();
    }

    // * If yylex reach to the end of file, return  0
    // * If there is a lexical error found, return -1
    // * If a proper lexeme is determined, return token <token-id, token-attribute> as follows:
    //   1. set token-attribute into yyparser.yylval
    //   2. return token-id defined in Parser
    //   token attribute can be lexeme, line number, column, etc.
    public int yylex() throws Exception
    {
        int state = 0;

        while(true)
        {
        
            char c;
            switch(state)
            {
                case 0:
                    column = columntracker + 1;
                    lexBegin = forward;
                    lexemeBuilder.setLength(0);

                    c = NextChar();
                    if(c == ';') { state= 1; continue; }
                    if(c == ',') { state= 2; continue; }
                    if(c == '(') { state= 3; continue; }
                    if(c == ')') { state= 4; continue; }
                    if(c == '{') { state= 5; continue; }
                    if(c == '}') { state= 6; continue; }
                    if(c == '+') { state= 7; continue; }
                    if(c == '-') { state= 8; continue; }
                    if(c == '*') { state= 9; continue; }
                    if(c == '/') { state= 10; continue; }
                    if(c == '<') { state= 11; continue; }
                    if(c == '>') { state= 12; continue; }
                    if(c == '=') { state= 20; continue; }
                    if(Character.isDigit(c)) { state= 16; continue; }
                    if(Character.isLetter(c)) { state= 18; continue; }
                    if(c == ' ') { column++; state=0; continue; }
                    if(c == '\n') {lineno++; column = 0; columntracker = 0; state=0; continue; }
                    if(c == '\t' || c == '\r') { column++; state=0; continue; }
                    if(c == EOF) { state=9999; continue; }
                    return Fail();                                  // if Fail, return -1 (indicating lexical error)
                case 1:
                    yyparser.yylval = new ParserVal((Object)";");   // set token-attribute to yyparser.yylval
                    return Parser.SEMI; // return token-name
                case 2:
                    yyparser.yylval = new ParserVal((Object)",");   // set token-attribute to yyparser.yylval
                    return Parser.COMMA; // return token-name
                case 3:
                    yyparser.yylval = new ParserVal((Object)"(");   // set token-attribute to yyparser.yylval
                    return Parser.LPAREN; // return token-name
                case 4:
                    yyparser.yylval = new ParserVal((Object)")");   // set token-attribute to yyparser.yylval
                    return Parser.RPAREN; // return token-name
                case 5:
                    yyparser.yylval = new ParserVal((Object)"{");   // set token-attribute to yyparser.yylval
                    return Parser.BEGIN; // return token-name
                case 6:
                    yyparser.yylval = new ParserVal((Object)"}");   // set token-attribute to yyparser.yylval
                    return Parser.END; // return token-name
                case 7:
                    yyparser.yylval = new ParserVal((Object)"+");   // set token-attribute to yyparser.yylval
                    return Parser.OP; // return token-name
                case 8:
                    yyparser.yylval = new ParserVal((Object)"-");   // set token-attribute to yyparser.yylval
                    return Parser.OP; // return token-name
                case 9:
                    yyparser.yylval = new ParserVal((Object)"*");   // set token-attribute to yyparser.yylval
                    return Parser.OP; // return token-name
                case 10:
                    yyparser.yylval = new ParserVal((Object)"/");   // set token-attribute to yyparser.yylval
                    return Parser.OP; // return token-name
                case 11:
                    c = NextChar();
                    if(c == '=') { state= 13; continue; }
                    if(c == '>') { state= 15; continue; }
                    if(c == '-') { state= 19; continue; }

                    // Other
                    UngetChar();
                    yyparser.yylval = new ParserVal((Object)"<");   // set token-attribute to yyparser.yylval
                    return Parser.RELOP; // return token-name
                case 12:
                    c = NextChar();
                    if(c == '=') { state= 14; continue; }

                    // Other
                    UngetChar();
                    yyparser.yylval = new ParserVal((Object)">");   // set token-attribute to yyparser.yylval
                    return Parser.RELOP; // return token-name
                case 13:
                    yyparser.yylval = new ParserVal((Object)"<=");   // set token-attribute to yyparser.yylval
                    return Parser.RELOP; // return token-name
                case 14:
                    yyparser.yylval = new ParserVal((Object)">=");   // set token-attribute to yyparser.yylval
                    return Parser.RELOP; // return token-name
                case 15:
                    yyparser.yylval = new ParserVal((Object)"<>");   // set token-attribute to yyparser.yylval
                    return Parser.RELOP; // return token-name
                case 16:
                    c = NextChar();
                    if(Character.isDigit(c)) { state=16; continue; }
                    if(c == '.') { state=17; continue; }
                    UngetChar();
                    yyparser.yylval = new ParserVal((Object)yytext()); // set token-attribute to yyparser.yylval
                    return Parser.NUM; // return token-name for integer
                case 17:
                    c = NextChar();
                    if(!Character.isDigit(c)) { return Fail(); }
                    if(Character.isDigit(c)) { state=21; continue; }
                case 18:
                    // Has to handle [a-zA-Z][a-zA-Z0-9_]*
                    c = NextChar();
                    if(Character.isLetter(c) || Character.isDigit(c) || c == '_') { state=18; continue; }
                    UngetChar();
                    
                    // Check if yytext is a keyword and then return the correct token
                    for (String keyword : keywords) {
                        if (keyword.equals(yytext())) {
                            yyparser.yylval = new ParserVal((Object)yytext()); // set token-attribute to yyparser.yylval
                            switch (keyword) {
                                case "int":
                                    return Parser.INT;
                                case "print":
                                    return Parser.PRINT;
                                case "if":
                                    return Parser.IF;
                                case "else":
                                    return Parser.ELSE;
                                case "while":
                                    return Parser.WHILE;
                                case "void":
                                    return Parser.VOID;
                            }
                        }
                    }
                    

                    yyparser.yylval = new ParserVal((Object)yytext()); // set token-attribute to yyparser.yylval
                    return Parser.ID; // return token-name
                case 19:
                    yyparser.yylval = new ParserVal((Object)"<-");   // set token-attribute to yyparser.yylval
                    return Parser.ASSIGN; // return token-name
                case 20:
                    yyparser.yylval = new ParserVal((Object)"=");   // set token-attribute to yyparser.yylval
                    return Parser.RELOP; // return token-name
                case 21:
                    c = NextChar();
                    if(Character.isDigit(c)) { state=21; continue; }
                    UngetChar();
                    yyparser.yylval = new ParserVal((Object)yytext()); // set token-attribute to yyparser.yylval
                    return Parser.NUM;
                case 9999:
                    return EOF;                                     // return end-of-file symbol (EOF == 0)
            }
        }
    }
}
