package club.doki7.pl12.util;

public final class CommonUtil {
    public static String superscriptNum(String prefix, int index) {
        String indexString = Integer.toString(index);
        StringBuilder sb = new StringBuilder();
        sb.append(prefix);

        for (char c : indexString.toCharArray()) {
            sb.append(switch (c) {
                case '0' -> '⁰';
                case '1' -> '¹';
                case '2' -> '²';
                case '3' -> '³';
                case '4' -> '⁴';
                case '5' -> '⁵';
                case '6' -> '⁶';
                case '7' -> '⁷';
                case '8' -> '⁸';
                case '9' -> '⁹';
                case '-' -> '⁻';
                case '+' -> '⁺';
                default -> throw new IllegalStateException("Invalid character in index: " + c);
            });
        }
        return sb.toString();
    }

    public static String subscriptNum(String prefix, int index) {
        String indexString = Integer.toString(index);
        StringBuilder sb = new StringBuilder();
        sb.append(prefix);

        for (char c : indexString.toCharArray()) {
            sb.append(switch (c) {
                case '0' -> '₀';
                case '1' -> '₁';
                case '2' -> '₂';
                case '3' -> '₃';
                case '4' -> '₄';
                case '5' -> '₅';
                case '6' -> '₆';
                case '7' -> '₇';
                case '8' -> '₈';
                case '9' -> '₉';
                case '-' -> '₋';
                case '+' -> '₊';
                default -> throw new IllegalStateException("Invalid character in index: " + c);
            });
        }
        return sb.toString();
    }
}
