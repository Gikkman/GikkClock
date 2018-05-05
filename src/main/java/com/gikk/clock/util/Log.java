package com.gikk.clock.util;

import java.util.Arrays;

/**
 *
 * @author Gikkman
 */
public class Log {
    enum Type{
        Trace("T"), Debug("D"), Info("I"), Warning("W"), Error("E");
        private final String code;
        private Type(String s){ this.code = s; }
    }
    public static void trace(String message, Object... args){
        System.err.println( buildMessage(Type.Trace, message, args) );
    }

    public static void debug(String message, Object... args){
        System.err.println( buildMessage(Type.Debug, message, args) );
    }

    public static void info(String message, Object... args){
        System.err.println( buildMessage(Type.Info, message, args) );
    }

    public static void warning(String message, Object... args){
        System.err.println( buildMessage(Type.Warning, message, args) );
    }

    public static void error(String message, Object... args){
        System.err.println( buildMessage(Type.Error, message, args) );
    }

    private static String buildMessage(Type t, String message, Object... args){
        StringBuilder builder = new StringBuilder();
        builder.append(t.code).append(" - ").append(message).append(" : ").append(getStackPos());
        for( Object o : args ) {
            String obj;
            if( o == null ) { obj = "null"; }
            else if( o.getClass().isArray() ){
                obj = Arrays.deepToString((Object[]) o);
            } else {
                obj = o.toString();
            }
            builder.append("\n").append(t.code).append("\t").append(obj);
        }
        return builder.toString();
    }

    /**
     * Returns a link to a code position.
     * Intended to be inserted in an error message.
     * <br>This will produce an error message in the output console with a clickable link that opens the file that caused the error.
     * <p>
     * <br><br><b>Example</b>
     * <br><code>System.err.println("Error. Unknown args. " + StackTrace.getStackPos() );</code>
     *
     * @return A stack trace position link
     */
    public static String getStackPos() {
        String out = "   ";
        StackTraceElement[] e = new Exception().getStackTrace();

        for (int i = 3; i < e.length && i < 6; i++) {
            String s = e[i].toString();
            int f = s.indexOf("(");
            int l = s.lastIndexOf(")") + 1;
            out += s.substring(f, l) + " ";
        }
        return out;
    }
}
