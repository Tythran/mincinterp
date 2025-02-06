import java.io.IOException;

public class Lexer
{
    private static final char EOF        =  0;

    private Parser         yyparser; // parent parser object
    private java.io.Reader reader;   // input stream
    public int             lineno;   // line number
    public int             column;   // column
    private char[]         inputBuffer; // buffer to store file content
    private int            currentIndex; // current index in the buffer

    public Lexer(java.io.Reader reader, Parser yyparser) throws Exception
    {
        this.reader   = reader;
        this.yyparser = yyparser;
        lineno = 1;
        column = 0;
        inputBuffer = readInput(reader);
        currentIndex = 0;
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
        if (currentIndex >= inputBuffer.length)
        {
            return EOF;
        }
        char c = inputBuffer[currentIndex++];
        if (c == '\n')
        {
            lineno++;
            column = 1;
        }
        else
        {
            column++;
        }
        return c;
    }

    public int Fail()
    {
        return -1;
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
                    c = NextChar();
                    if(c == ';') { state=   1; continue; }
                    if(c == EOF) { state=9999; continue; }
                    return Fail();                                  // if Fail, return -1 (indicating lexical error)
                case 1:
                    yyparser.yylval = new ParserVal((Object)";");   // set token-attribute to yyparser.yylval
                    return Parser.SEMI;                             // return token-name
                case 9999:
                    return EOF;                                     // return end-of-file symbol (EOF == 0)
            }
        }
    }
}
