package core;

import java.util.Scanner;

public class NativeConsole implements Commander {

    private final Scanner _scanner = new Scanner(System.in);

    @Override
    public String read() {
        if ( _scanner.hasNextLine() )
            return _scanner.nextLine();
        else
            return "";
    }

    @Override
    public void println(String text) {
        System.out.println(text);
    }

    @Override
    public void print(String text) {
        System.out.print(text);
    }
}
