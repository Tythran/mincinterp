import java.io.IOException;

public class Lexer
{
    private static final char EOF        =  0;

    private Parser         yyparser; // parent parser object
    private java.io.Reader reader;   // input stream
    public int             lineno;   // line number
    public int             column;   // column
    private char[]         inputBuffer; // buffer to store file content
    int forward = 0;                    // forward index
    int lexBegin = 0;                   // lexeme begin index
    int columntracker = 0;              // column tracker
    String[] keywords = {"int", "print", "if", "else", "while", "void"};

    public Lexer(java.io.Reader reader, Parser yyparser) throws Exception
    {
        this.reader   = reader;
        this.yyparser = yyparser;
        lineno = 1;
        column = 1;
        inputBuffer = readInput(reader);
    }

    private char[] readInput(java.io.Reader reader) throws IOException
    {
        StringBuilder sb = new StringBuilder();
        int data;
        while ((data = reader.read()) != -1)
        {
            sb.append((char) data);
        }
        return sb.toString().toCharArray();
    }

    public char NextChar() throws Exception
    {
        if (forward >= inputBuffer.length)
        {
            return EOF;
        }
        char c = inputBuffer[forward++];
        columntracker++;
        return c;
    }

    public int Fail()
    {
        return -1;
    }

    private void UngetChar() {
        if (forward > 1) {
            forward--;
            columntracker--;
        }
    }

    private String yytext() {
        return new String(inputBuffer, lexBegin, forward - lexBegin);
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
                    column = columntracker+1;
                    lexBegin = forward;

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
                    if(Character.isDigit(c)) { state=17; continue; }
                    UngetChar();
                    yyparser.yylval = new ParserVal((Object)yytext()); // set token-attribute to yyparser.yylval
                    if(c == '.') {column = columntracker - (forward - lexBegin)+1; return Parser.NUM; }
                    return Parser.NUM;
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
                case 9999:
                    return EOF;                                     // return end-of-file symbol (EOF == 0)
            }
        }
    }
}
