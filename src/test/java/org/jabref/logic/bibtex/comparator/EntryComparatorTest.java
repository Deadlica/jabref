package org.jabref.logic.bibtex.comparator;

import org.jabref.model.entry.BibEntry;
import org.jabref.model.entry.field.InternalField;
import org.jabref.model.entry.field.StandardField;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class EntryComparatorTest {

    @SuppressWarnings("EqualsWithItself")
    @Test
    void recognizeIdenticalObjectsAsEqual() {
        BibEntry entry = new BibEntry();
        assertEquals(0, new EntryComparator(false, false, StandardField.TITLE).compare(entry, entry));
    }

    @Test
    void compareAuthorFieldBiggerAscending() {
        BibEntry entry1 = new BibEntry()
                .withField(StandardField.AUTHOR, "Stephen King");
        BibEntry entry2 = new BibEntry()
                .withField(StandardField.AUTHOR, "Henning Mankell");
        EntryComparator entryComparator = new EntryComparator(false, false, StandardField.AUTHOR);

        assertEquals(-2, entryComparator.compare(entry1, entry2));
    }

    @Test
    void bothEntriesHaveNotSetTheFieldToCompareAscending() {
        BibEntry entry1 = new BibEntry()
                .withField(StandardField.BOOKTITLE, "Stark - The Dark Half (1989)");
        BibEntry entry2 = new BibEntry()
                .withField(StandardField.COMMENTATOR, "Some Commentator");
        EntryComparator entryComparator = new EntryComparator(false, false, StandardField.TITLE);

        assertEquals(-1, entryComparator.compare(entry1, entry2));
    }

    @Test
    void secondEntryHasNotSetFieldToCompareAscending() {
        BibEntry entry1 = new BibEntry()
                .withField(StandardField.TITLE, "Stark - The Dark Half (1989)");
        BibEntry entry2 = new BibEntry()
                .withField(StandardField.COMMENTATOR, "Some Commentator");
        EntryComparator entryComparator = new EntryComparator(false, false, StandardField.TITLE);

        assertEquals(1, entryComparator.compare(entry1, entry2));
    }

    @Test
    void firstEntryHasNotSetFieldToCompareAscending() {
        BibEntry entry1 = new BibEntry()
                .withField(StandardField.COMMENTATOR, "Some Commentator");
        BibEntry entry2 = new BibEntry()
                .withField(StandardField.TITLE, "Stark - The Dark Half (1989)");

        EntryComparator entryComparator = new EntryComparator(false, false, StandardField.TITLE);

        assertEquals(-1, entryComparator.compare(entry1, entry2));
    }

    @Test
    void bothEntriesNumericAscending() {
        BibEntry entry1 = new BibEntry()
                .withField(StandardField.EDITION, "1");
        BibEntry entry2 = new BibEntry()
                .withField(StandardField.EDITION, "3");

        EntryComparator entryComparator = new EntryComparator(false, false, StandardField.EDITION);

        assertEquals(-1, entryComparator.compare(entry1, entry2));
    }

    @Test
    void compareObjectsByKeyAscending() {
        BibEntry e1 = new BibEntry()
                .withCitationKey("Mayer2019b");
        BibEntry e2 = new BibEntry()
                .withCitationKey("Mayer2019a");
        assertEquals(1, new EntryComparator(false, false, InternalField.KEY_FIELD).compare(e1, e2));
        assertEquals(-1, new EntryComparator(false, false, InternalField.KEY_FIELD).compare(e2, e1));
    }

    @Test
    void compareObjectsByKeyWithNull() {
        BibEntry e1 = new BibEntry()
                .withCitationKey("Mayer2019b");
        BibEntry e2 = new BibEntry();
        assertEquals(1, new EntryComparator(false, false, InternalField.KEY_FIELD).compare(e1, e2));
        assertEquals(-1, new EntryComparator(false, false, InternalField.KEY_FIELD).compare(e2, e1));
    }

    @Test
    void compareObjectsByKeyWithBlank() {
        BibEntry e1 = new BibEntry()
                .withCitationKey("Mayer2019b");
        BibEntry e2 = new BibEntry()
                .withCitationKey(" ");
        assertEquals(1, new EntryComparator(false, false, InternalField.KEY_FIELD).compare(e1, e2));
        assertEquals(-1, new EntryComparator(false, false, InternalField.KEY_FIELD).compare(e2, e1));
    }

    @Test
    void compareWithCrLfFields() {
        BibEntry e1 = new BibEntry()
                .withField(StandardField.COMMENT, "line1\r\n\r\nline3\r\n\r\nline5");
        BibEntry e2 = new BibEntry()
                .withField(StandardField.COMMENT, "line1\r\n\r\nline3\r\n\r\nline5");
        assertEquals(0, new EntryComparator(false, false, InternalField.KEY_FIELD).compare(e1, e2));
    }

    @Test
    void compareWithLfFields() {
        BibEntry e1 = new BibEntry()
                .withField(StandardField.COMMENT, "line1\n\nline3\n\nline5");
        BibEntry e2 = new BibEntry()
                .withField(StandardField.COMMENT, "line1\n\nline3\n\nline5");
        assertEquals(0, new EntryComparator(false, false, InternalField.KEY_FIELD).compare(e1, e2));
    }

    @Test
    void compareWithMixedLineEndings() {
        BibEntry e1 = new BibEntry()
                .withField(StandardField.COMMENT, "line1\n\nline3\n\nline5");
        BibEntry e2 = new BibEntry()
                .withField(StandardField.COMMENT, "line1\r\n\r\nline3\r\n\r\nline5");
        assertEquals(-1, new EntryComparator(false, false, InternalField.KEY_FIELD).compare(e1, e2));
    }

    @Test
    void withAuthorIsBeforeWithEmptyAuthorWhenSortingWithNonExistentKey() {
        BibEntry e1 = new BibEntry()
            .withField(StandardField.AUTHOR, "Stephen King");
        BibEntry e2 = new BibEntry()
            .withField(StandardField.AUTHOR, "");
        assertEquals(-1, new EntryComparator(true, false, InternalField.KEY_FIELD).compare(e1, e2));
    }

    @Test
    void withAuthorIsBeforeEmptyAuthorWhenSortingWithAuthor() {
        BibEntry e1 = new BibEntry()
            .withField(StandardField.AUTHOR, "Stephen King");
        BibEntry e2 = new BibEntry()
            .withField(StandardField.AUTHOR, "");
        assertEquals(-1, new EntryComparator(true, false, StandardField.AUTHOR).compare(e1, e2));
    }

    @Test
    void withAuthorIsBeforeWithoutAuthorWhenSortingWithAuthor() {
        BibEntry e1 = new BibEntry()
            .withField(StandardField.AUTHOR, "Stephen King");
        BibEntry e2 = new BibEntry();
        assertEquals(1, new EntryComparator(false, false, StandardField.AUTHOR).compare(e1, e2));
    }

    @Test
    void useNextComparatorForTieBreak() {
        // Entries with the same title
        BibEntry entry1 = new BibEntry()
                .withField(StandardField.TITLE, "Shared Title")
                .withField(StandardField.YEAR, "2020");
        BibEntry entry2 = new BibEntry()
                .withField(StandardField.TITLE, "Shared Title")
                .withField(StandardField.YEAR, "2019");

        // Primary comparator based on title, next comparator based on year
        EntryComparator entryComparator = new EntryComparator(false, false, StandardField.TITLE,
                new EntryComparator(false, false, StandardField.YEAR));

        // Expecting entry1 > entry2 because of the next comparator (year comparison)
        assertEquals(1, entryComparator.compare(entry1, entry2));
    }

    @Test
    void testCompareWithInvalidNumericFields() {
        // Create a comparator that sorts based on a numeric field, e.g., VOLUME
        EntryComparator comparator = new EntryComparator(false, false, StandardField.VOLUME);

        // Create two BibEntry objects with non-numeric values in a numeric field
        BibEntry entry1 = new BibEntry();
        entry1.setField(StandardField.VOLUME, "Volume1");
        BibEntry entry2 = new BibEntry();
        entry2.setField(StandardField.VOLUME, "Volume2");

        // Compare the entries, expecting the comparison to fall back to non-numeric comparison due to NumberFormatException
        int result = comparator.compare(entry1, entry2);

        // Assert that the comparison did not result in a tie
        assertTrue(result != 0);
    }

}


