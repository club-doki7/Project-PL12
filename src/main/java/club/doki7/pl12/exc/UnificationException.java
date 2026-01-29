package club.doki7.pl12.exc;

import club.doki7.pl12.syntax.Token;

public final class UnificationException extends LocatedException {
    public UnificationException(Token location, String message) {
        super(location, UnificationException.class, message);
    }
}
