package print;

public class PrintHandler {
    private static int _verbosityLevel = -1; // -1 is print nothing, +inf is print everything

    private PrintHandler() {
    }

    public static void setVerbosityLevel(int verbosityLevel) {
        _verbosityLevel = verbosityLevel;
    }

    public static void println(Object o) {
        println(o, 0);
    }

    public static void println(Object o, int verbosityLevel) {
        if (verbosityLevel <= _verbosityLevel) {
            System.out.println(o);
        }
    }
}
