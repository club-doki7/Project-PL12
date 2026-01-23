package club.doki7.pl12.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ConsListTest {
    @Nested
    @DisplayName("Nil tests")
    class NilTests {
        @Test
        @DisplayName("nil should have length 0")
        void nilShouldHaveLengthZero() {
            ConsList<Integer> nil = ConsList.nil();
            assertEquals(0, nil.length());
        }

        @Test
        @DisplayName("nil get should throw IndexOutOfBoundsException")
        void nilGetShouldThrow() {
            ConsList<Integer> nil = ConsList.nil();
            assertThrows(IndexOutOfBoundsException.class, () -> nil.get(0));
            assertThrows(IndexOutOfBoundsException.class, () -> nil.get(-1));
        }

        @Test
        @DisplayName("nil toString should return []")
        void nilToStringShouldReturnEmptyBrackets() {
            ConsList<Integer> nil = ConsList.nil();
            assertEquals("[]", nil.toString());
        }

        @SuppressWarnings("EqualsWithItself")
        @Test
        @DisplayName("nil should be singleton")
        void nilShouldBeSingleton() {
            assertSame(ConsList.nil(), ConsList.nil());
        }
    }

    @Nested
    @DisplayName("Cons tests")
    class ConsTests {
        @Test
        @DisplayName("single element cons should have length 1")
        void singleElementConsShouldHaveLengthOne() {
            ConsList<Integer> list = ConsList.cons(1, ConsList.nil());
            assertEquals(1, list.length());
        }

        @Test
        @DisplayName("multiple element cons should have correct length")
        void multipleElementConsShouldHaveCorrectLength() {
            ConsList<Integer> list = ConsList.cons(1,
                    ConsList.cons(2,
                            ConsList.cons(3, ConsList.nil())));
            assertEquals(3, list.length());
        }

        @Test
        @DisplayName("get should return correct elements")
        void getShouldReturnCorrectElements() {
            // List is [a, b, c] built as cons(a, cons(b, cons(c, nil)))
            ConsList<String> list = ConsList.cons("a",
                    ConsList.cons("b",
                            ConsList.cons("c", ConsList.nil())));
            // get(0) should return first element "a"
            assertEquals("a", list.get(0));
            // get(1) should return "b"
            assertEquals("b", list.get(1));
            // get(2) should return "c"
            assertEquals("c", list.get(2));
        }

        @Test
        @DisplayName("get with invalid index should throw")
        void getWithInvalidIndexShouldThrow() {
            ConsList<Integer> list = ConsList.cons(1, ConsList.cons(2, ConsList.nil()));
            assertThrows(IndexOutOfBoundsException.class, () -> list.get(2));
            assertThrows(IndexOutOfBoundsException.class, () -> list.get(10));
        }

        @Test
        @DisplayName("toString should show elements in order")
        void toStringShouldShowElementsInOrder() {
            ConsList<Integer> list = ConsList.cons(1,
                    ConsList.cons(2,
                            ConsList.cons(3, ConsList.nil())));
            assertEquals("[1, 2, 3]", list.toString());
        }

        @Test
        @DisplayName("head should return the first element")
        void headShouldReturnFirstElement() {
            ConsList.Cons<Integer> list = new ConsList.Cons<>(42, ConsList.nil());
            assertEquals(42, list.head());
        }

        @Test
        @DisplayName("tail should return all elements except the first")
        void tailShouldReturnAllExceptFirst() {
            ConsList<Integer> tail = ConsList.cons(2, ConsList.cons(3, ConsList.nil()));
            ConsList.Cons<Integer> list = new ConsList.Cons<>(1, tail);
            assertSame(tail, list.tail());
        }
    }

    @Nested
    @DisplayName("Factory method tests")
    class FactoryMethodTests {
        @Test
        @DisplayName("of() with no elements should return empty list")
        void ofWithNoElementsShouldReturnEmptyList() {
            ConsList<Integer> list = ConsList.of();
            assertEquals(0, list.length());
        }

        @Test
        @DisplayName("of() should create list with elements")
        void ofShouldCreateListWithElements() {
            ConsList<Integer> list = ConsList.of(1, 2, 3, 4, 5);
            assertEquals(5, list.length());
            // Elements are added in order, so get(0) returns first element
            assertEquals(1, list.get(0));
            assertEquals(2, list.get(1));
            assertEquals(3, list.get(2));
            assertEquals(4, list.get(3));
            assertEquals(5, list.get(4));
        }

        @Test
        @DisplayName("from array should create list with elements")
        void fromArrayShouldCreateListWithElements() {
            Integer[] array = {10, 20, 30};
            ConsList<Integer> list = ConsList.from(array);
            assertEquals(3, list.length());
            assertEquals(10, list.get(0));
            assertEquals(20, list.get(1));
            assertEquals(30, list.get(2));
        }

        @Test
        @DisplayName("from List should create cons list with elements")
        void fromListShouldCreateConsListWithElements() {
            List<String> javaList = Arrays.asList("x", "y", "z");
            ConsList<String> consList = ConsList.from(javaList);
            assertEquals(3, consList.length());
            assertEquals("x", consList.get(0));
            assertEquals("y", consList.get(1));
            assertEquals("z", consList.get(2));
        }

        @Test
        @DisplayName("cons should prepend element to the front")
        void consShouldPrependElementToFront() {
            ConsList<Integer> list = ConsList.nil();
            list = ConsList.cons(3, list);
            list = ConsList.cons(2, list);
            list = ConsList.cons(1, list);

            assertEquals(3, list.length());
            assertEquals("[1, 2, 3]", list.toString());
            assertEquals(1, list.get(0)); // first element
            assertEquals(3, list.get(2)); // last element
        }
    }
}

