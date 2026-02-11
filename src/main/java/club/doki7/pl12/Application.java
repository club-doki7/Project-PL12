package club.doki7.pl12;

import club.doki7.pl12.drv.ANSIColor;
import club.doki7.pl12.exc.ParseException;
import club.doki7.pl12.syntax.Command;
import club.doki7.pl12.syntax.ParseContext;
import club.doki7.pl12.syntax.Parser;
import club.doki7.pl12.syntax.Program;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Scanner;

public final class Application implements ANSIColor {
    private static final String SPLASH =
    """
    === Project-PL12 ===
    """;

    private static void print(String color, String message) {
        System.out.print(color + message + ANSI_RESET);
    }

    private static void println(String color, String message) {
        System.out.println(color + message + ANSI_RESET);
    }

    private static void println(String color, long counter, String output) {
        System.out.println(color + "[" + counter + "] => " + output + ANSI_RESET);
    }

    static void main(String[] ignored) {
        System.out.println(SPLASH);

        Scanner scanner = new Scanner(System.in);
        long counter = 0;
        loop: while (true) {
            print(ANSI_BLUE + ANSI_BOLD, "» ");
            if (!scanner.hasNextLine()) {
                break;
            }

            String[] command = scanner.nextLine().trim().split("\\s+", 2);
            if (command.length == 0) {
                continue;
            }

            counter += 1;
            switch (command[0]) {
                case ":quit" -> { break loop; }
                case ":load" -> {
                    if (command.length < 2) {
                        println(ANSI_RED, counter, "用法:\t:load <文件名>");
                        continue;
                    }

                    String filename = command[1];
                    Path path;
                    try {
                        path = Path.of(filename);
                    } catch (Exception e) {
                        println(ANSI_RED, counter, "文件名、目录、卷标语法不正确");
                        continue;
                    }

                    String content;
                    try {
                        content = Files.readString(path);
                    } catch (IOException e) {
                        println(ANSI_RED, counter, "找不到 " + filename);
                        continue;
                    }

                    Program program;
                    try {
                        program = Parser.parseProgram(ParseContext.of(content, filename));
                    } catch (ParseException e) {
                        println(ANSI_RED, counter, e.getMessage());
                        continue;
                    }

                    println(ANSI_GREEN, counter, "成功加载 " + filename);
                    for (Command cmd : program.commands()) {
                        println(ANSI_CYAN, "\t" + cmd);
                    }
                }
                default -> println(ANSI_RED,
                                   counter,
                                   command[0] + " 不是内部或外部命令，也不是可运行的程序或批处理文件");
            }
        }
    }
}
