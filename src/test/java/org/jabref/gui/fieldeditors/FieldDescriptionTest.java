package org.jabref.gui.fieldeditors;

import org.jabref.model.entry.field.Field;
import org.jabref.model.entry.field.SpecialField;
import org.jabref.model.entry.field.StandardField;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FieldDescriptionTest {

    @Test
    void getDescription_Abstract() {
        Field field = StandardField.ABSTRACT;
        String expected = "This field is intended for recording abstracts, to be printed by a special bibliography style.";
        String actual = FieldDescription.getDescription(field);
        assertEquals(expected, actual);
    }

    @Test
    void getDescription_Comment() {
        Field field = StandardField.COMMENT;
        String expected = "Comment to this entry.";
        String actual = FieldDescription.getDescription(field);
        assertEquals(expected, actual);
    }

    @Test
    void getDescription_Printed() {
        Field field = SpecialField.PRINTED;
        String expected = "User-specific printed flag, in case the entry has been printed.";
        String actual = FieldDescription.getDescription(field);
        assertEquals(expected, actual);
    }
}

