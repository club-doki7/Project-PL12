package club.doki7.pl12.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SnocListTest {
    @Nested
    @DisplayName("Nil tests")
    class NilTests {
        @Test
        @DisplayName("nil should have length 0")
        void nilShouldHaveLengthZero() {
            SnocList<Integer> nil = SnocList.nil();
            assertEquals(0, nil.length());
        }

        @Test
        @DisplayName("nil revGet should throw IndexOutOfBoundsException")
        void nilRevGetShouldThrow() {
            SnocList<Integer> nil = SnocList.nil();
            assertThrows(IndexOutOfBoundsException.class, () -> nil.revGet(0));
            assertThrows(IndexOutOfBoundsException.class, () -> nil.revGet(-1));
        }

        @Test
        @DisplayName("nil toString should return []")
        void nilToStringShouldReturnEmptyBrackets() {
            SnocList<Integer> nil = SnocList.nil();
            assertEquals("[]", nil.toString());
        }

        @SuppressWarnings("EqualsWithItself")
        @Test
        @DisplayName("nil should be singleton")
        void nilShouldBeSingleton() {
            assertSame(SnocList.nil(), SnocList.nil());
        }
    }

    @Nested
    @DisplayName("Snoc tests")
    class SnocTests {
        @Test
        @DisplayName("single element snoc should have length 1")
        void singleElementConsShouldHaveLengthOne() {
            SnocList<Integer> list = SnocList.snoc(SnocList.nil(), 1);
            assertEquals(1, list.length());
        }

        @Test
        @DisplayName("multiple element cons should have correct length")
        void multipleElementConsShouldHaveCorrectLength() {
            SnocList<Integer> list = SnocList.snoc(
                SnocList.snoc(
                    SnocList.snoc(SnocList.nil(), 1),
                    2),
                3);
            assertEquals(3, list.length());
        }

        @Test
        @DisplayName("revGet should return correct elements (reverse indexed)")
        void revGetShouldReturnCorrectElements() {
            // List is [a, b, c] built as rcons(rcons(rcons(nil, a), b), c)
            SnocList<String> list = SnocList.snoc(
                SnocList.snoc(
                    SnocList.snoc(SnocList.nil(), "a"),
                    "b"),
                "c");
            // revGet(0) should return last element "c"
            assertEquals("c", list.revGet(0));
            // revGet(1) should return "b"
            assertEquals("b", list.revGet(1));
            // revGet(2) should return "a"
            assertEquals("a", list.revGet(2));
        }

        @Test
        @DisplayName("revGet with invalid index should throw")
        void revGetWithInvalidIndexShouldThrow() {
            SnocList<Integer> list = SnocList.snoc(SnocList.snoc(SnocList.nil(), 1), 2);
            assertThrows(IndexOutOfBoundsException.class, () -> list.revGet(2));
            assertThrows(IndexOutOfBoundsException.class, () -> list.revGet(10));
        }

        @Test
        @DisplayName("toString should show elements in order")
        void toStringShouldShowElementsInOrder() {
            SnocList<Integer> list = SnocList.snoc(
                SnocList.snoc(
                    SnocList.snoc(SnocList.nil(), 1),
                    2),
                3);
            assertEquals("[1, 2, 3]", list.toString());
        }

        @Test
        @DisplayName("init should return all elements except the last")
        void initShouldReturnAllExceptLast() {
            SnocList<Integer> init = SnocList.snoc(SnocList.snoc(SnocList.nil(), 1), 2);
            SnocList.Snoc<Integer> list = SnocList.snoc(init, 3);
            assertSame(init, list.init());
        }

        @Test
        @DisplayName("last should return the last element")
        void lastShouldReturnLastElement() {
            SnocList.Snoc<Integer> list = SnocList.snoc(SnocList.nil(), 42);
            assertEquals(42, list.last());
        }
    }

    @Nested
    @DisplayName("Factory method tests")
    class FactoryMethodTests {
        @Test
        @DisplayName("of() with no elements should return empty list")
        void ofWithNoElementsShouldReturnEmptyList() {
            SnocList<Integer> list = SnocList.of();
            assertEquals(0, list.length());
        }

        @Test
        @DisplayName("of() should create list with elements")
        void ofShouldCreateListWithElements() {
            SnocList<Integer> list = SnocList.of(1, 2, 3, 4, 5);
            assertEquals(5, list.length());
            // Elements are added in order, so revGet(0) returns last element
            assertEquals(5, list.revGet(0));
            assertEquals(4, list.revGet(1));
            assertEquals(3, list.revGet(2));
            assertEquals(2, list.revGet(3));
            assertEquals(1, list.revGet(4));
        }

        @Test
        @DisplayName("from array should create list with elements")
        void fromArrayShouldCreateListWithElements() {
            Integer[] array = {10, 20, 30};
            SnocList<Integer> list = SnocList.from(array);
            assertEquals(3, list.length());
            assertEquals(30, list.revGet(0));
            assertEquals(20, list.revGet(1));
            assertEquals(10, list.revGet(2));
        }

        @Test
        @DisplayName("from List should create cons rev list with elements")
        void fromListShouldCreateConsRevListWithElements() {
            List<String> javaList = Arrays.asList("x", "y", "z");
            SnocList<String> snocList = SnocList.from(javaList);
            assertEquals(3, snocList.length());
            assertEquals("z", snocList.revGet(0));
            assertEquals("y", snocList.revGet(1));
            assertEquals("x", snocList.revGet(2));
        }

        @Test
        @DisplayName("rcons should append element to the end")
        void rconsShouldAppendElementToEnd() {
            SnocList<Integer> list = SnocList.nil();
            list = SnocList.snoc(list, 1);
            list = SnocList.snoc(list, 2);
            list = SnocList.snoc(list, 3);

            assertEquals(3, list.length());
            assertEquals("[1, 2, 3]", list.toString());
            assertEquals(3, list.revGet(0)); // last element
            assertEquals(1, list.revGet(2)); // first element
        }
    }
}

