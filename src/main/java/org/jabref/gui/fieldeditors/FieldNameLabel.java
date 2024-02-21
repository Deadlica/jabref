package org.jabref.gui.fieldeditors;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.stage.Screen;

import org.jabref.logic.l10n.Localization;
import org.jabref.model.entry.field.Field;
import org.jabref.model.entry.field.InternalField;
import org.jabref.model.entry.field.SpecialField;
import org.jabref.model.entry.field.StandardField;
import org.jabref.model.strings.StringUtil;

public class FieldNameLabel extends Label {

    public FieldNameLabel(Field field) {
        super(field.getDisplayName());

        setPadding(new Insets(4, 0, 0, 0));
        setAlignment(Pos.CENTER);
        setPrefHeight(Double.POSITIVE_INFINITY);

        String description = getDescription(field);
        if (StringUtil.isNotBlank(description)) {
            Screen currentScreen = Screen.getPrimary();
            double maxWidth = currentScreen.getBounds().getWidth();
            Tooltip tooltip = new Tooltip(description);
            tooltip.setMaxWidth(maxWidth * 2 / 3);
            tooltip.setWrapText(true);
            this.setTooltip(tooltip);
        }
    }

    public String getDescription(Field field) {
        boolean[] coverage = new boolean[96];
        if (field.isStandardField()) {
            coverage[0] = true;
            StandardField standardField = (StandardField) field;
            switch (standardField) {
                case ABSTRACT:
                    coverage[1] = true;
                    printCoverage(coverage);
                    return Localization.lang("This field is intended for recording abstracts, to be printed by a special bibliography style.");
                case ADDENDUM:
                    coverage[2] = true;
                    printCoverage(coverage);
                    return Localization.lang("Miscellaneous bibliographic data usually printed at the end of the entry.");
                case AFTERWORD:
                    coverage[3] = true;
                    printCoverage(coverage);
                    return Localization.lang("Author(s) of an afterword to the work.");
                case ANNOTATION:
                case ANNOTE:
                    coverage[4] = true;
                    printCoverage(coverage);
                    return Localization.lang("This field may be useful when implementing a style for annotated bibliographies.");
                case ANNOTATOR:
                    coverage[5] = true;
                    printCoverage(coverage);
                    return Localization.lang("Author(s) of annotations to the work.");
                case AUTHOR:
                    coverage[6] = true;
                    printCoverage(coverage);
                    return Localization.lang("Author(s) of the work.");
                case BOOKSUBTITLE:
                    coverage[7] = true;
                    printCoverage(coverage);
                    return Localization.lang("Subtitle related to the \"Booktitle\".");
                case BOOKTITLE:
                    coverage[8] = true;
                    printCoverage(coverage);
                    return Localization.lang("Title of the main publication this work is part of.");
                case BOOKTITLEADDON:
                    coverage[9] = true;
                    printCoverage(coverage);
                    return Localization.lang("Annex to the \"Booktitle\", to be printed in a different font.");
                case CHAPTER:
                    coverage[10] = true;
                    printCoverage(coverage);
                    return Localization.lang("Chapter or section or any other unit of a work.");
                case COMMENT:
                    coverage[11] = true;
                    printCoverage(coverage);
                    return Localization.lang("Comment to this entry.");
                case COMMENTATOR:
                    coverage[12] = true;
                    printCoverage(coverage);
                    return Localization.lang("Author(s) of a commentary to the work.") + "\n" +
                            Localization.lang("Note that this field is intended for commented editions which have a commentator in addition to the author. If the work is a stand-alone commentary, the commentator should be given in the author field.");
                case DATE:
                    coverage[13] = true;
                    printCoverage(coverage);
                    return Localization.lang("Publication date of the work.");
                case DOI:
                    coverage[14] = true;
                    printCoverage(coverage);
                    return Localization.lang("Digital Object Identifier of the work.");
                case EDITION:
                    coverage[15] = true;
                    printCoverage(coverage);
                    return Localization.lang("Edition of a printed publication.");
                case EDITOR:
                    coverage[16] = true;
                    printCoverage(coverage);
                    return Localization.lang("Editor(s) of the work or the main publication, depending on the type of the entry.");
                case EDITORA:
                    coverage[17] = true;
                    printCoverage(coverage);
                    return Localization.lang("Secondary editor performing a different editorial role, such as compiling, redacting, etc.");
                case EDITORB:
                    coverage[18] = true;
                    printCoverage(coverage);
                    return Localization.lang("Another secondary editor performing a different role.");
                case EDITORC:
                    coverage[19] = true;
                    printCoverage(coverage);
                    return Localization.lang("Another secondary editor performing a different role.");
                case EDITORTYPE:
                    coverage[20] = true;
                    printCoverage(coverage);
                    return Localization.lang("Type of editorial role performed by the \"Editor\".");
                case EDITORATYPE:
                    coverage[21] = true;
                    printCoverage(coverage);
                    return Localization.lang("Type of editorial role performed by the \"Editora\".");
                case EDITORBTYPE:
                    coverage[22] = true;
                    printCoverage(coverage);
                    return Localization.lang("Type of editorial role performed by the \"Editorb\".");
                case EDITORCTYPE:
                    coverage[23] = true;
                    printCoverage(coverage);
                    return Localization.lang("Type of editorial role performed by the \"Editorc\".");
                case EID:
                    coverage[24] = true;
                    printCoverage(coverage);
                    return Localization.lang("Electronic identifier of a work.") + "\n" +
                            Localization.lang("This field may replace the pages field for journals deviating from the classic pagination scheme of printed journals by only enumerating articles or papers and not pages.");
                case EPRINT:
                    coverage[25] = true;
                    printCoverage(coverage);
                    return Localization.lang("Electronic identifier of an online publication.") + "\n" +
                            Localization.lang("This is roughly comparable to a DOI but specific to a certain archive, repository, service, or system.");
                case EPRINTCLASS:
                case PRIMARYCLASS:
                    coverage[26] = true;
                    printCoverage(coverage);
                    return Localization.lang("Additional information related to the resource indicated by the eprint field.") + "\n" +
                            Localization.lang("This could be a section of an archive, a path indicating a service, a classification of some sort.");
                case EPRINTTYPE:
                case ARCHIVEPREFIX:
                    coverage[27] = true;
                    printCoverage(coverage);
                    return Localization.lang("Type of the eprint identifier, e. g., the name of the archive, repository, service, or system the eprint field refers to.");
                case EVENTDATE:
                    coverage[28] = true;
                    printCoverage(coverage);
                    return Localization.lang("Date of a conference, a symposium, or some other event.");
                case EVENTTITLE:
                    coverage[29] = true;
                    printCoverage(coverage);
                    return Localization.lang("Title of a conference, a symposium, or some other event.") + "\n"
                            + Localization.lang("Note that this field holds the plain title of the event. Things like \"Proceedings of the Fifth XYZ Conference\" go into the titleaddon or booktitleaddon field.");
                case EVENTTITLEADDON:
                    coverage[30] = true;
                    printCoverage(coverage);
                    return Localization.lang("Annex to the eventtitle field.") + "\n" +
                            Localization.lang("Can be used for known event acronyms.");
                case FILE:
                case PDF:
                    coverage[31] = true;
                    printCoverage(coverage);
                    return Localization.lang("Link(s) to a local PDF or other document of the work.");
                case FOREWORD:
                    coverage[32] = true;
                    printCoverage(coverage);
                    return Localization.lang("Author(s) of a foreword to the work.");
                case HOWPUBLISHED:
                    coverage[33] = true;
                    printCoverage(coverage);
                    return Localization.lang("Publication notice for unusual publications which do not fit into any of the common categories.");
                case INSTITUTION:
                case SCHOOL:
                    coverage[34] = true;
                    printCoverage(coverage);
                    return Localization.lang("Name of a university or some other institution.");
                case INTRODUCTION:
                    coverage[35] = true;
                    printCoverage(coverage);
                    return Localization.lang("Author(s) of an introduction to the work.");
                case ISBN:
                    coverage[36] = true;
                    printCoverage(coverage);
                    return Localization.lang("International Standard Book Number of a book.");
                case ISRN:
                    coverage[37] = true;
                    printCoverage(coverage);
                    return Localization.lang("International Standard Technical Report Number of a technical report.");
                case ISSN:
                    coverage[38] = true;
                    printCoverage(coverage);
                    return Localization.lang("International Standard Serial Number of a periodical.");
                case ISSUE:
                    coverage[39] = true;
                    printCoverage(coverage);
                    return Localization.lang("Issue of a journal.") + "\n" +
                            Localization.lang("This field is intended for journals whose individual issues are identified by a designation such as \"Spring\" or \"Summer\" rather than the month or a number. Integer ranges and short designators are better written to the number field.");
                case ISSUESUBTITLE:
                    coverage[40] = true;
                    printCoverage(coverage);
                    return Localization.lang("Subtitle of a specific issue of a journal or other periodical.");
                case ISSUETITLE:
                    coverage[41] = true;
                    printCoverage(coverage);
                    return Localization.lang("Title of a specific issue of a journal or other periodical.");
                case JOURNALSUBTITLE:
                    coverage[42] = true;
                    printCoverage(coverage);
                    return Localization.lang("Subtitle of a journal, a newspaper, or some other periodical.");
                case JOURNALTITLE:
                case JOURNAL:
                    coverage[43] = true;
                    printCoverage(coverage);
                    return Localization.lang("Name of a journal, a newspaper, or some other periodical.");
                case LABEL:
                    coverage[44] = true;
                    printCoverage(coverage);
                    return Localization.lang("Designation to be used by the citation style as a substitute for the regular label if any data required to generate the regular label is missing.");
                case LANGUAGE:
                    coverage[45] = true;
                    printCoverage(coverage);
                    return Localization.lang("Language(s) of the work. Languages may be specified literally or as localisation keys.");
                case LIBRARY:
                    coverage[46] = true;
                    printCoverage(coverage);
                    return Localization.lang("Information such as a library name and a call number.");
                case LOCATION:
                case ADDRESS:
                    coverage[47] = true;
                    printCoverage(coverage);
                    return Localization.lang("Place(s) of publication, i. e., the location of the publisher or institution, depending on the entry type.");
                case MAINSUBTITLE:
                    coverage[48] = true;
                    printCoverage(coverage);
                    return Localization.lang("Subtitle related to the \"Maintitle\".");
                case MAINTITLE:
                    coverage[49] = true;
                    printCoverage(coverage);
                    return Localization.lang("Main title of a multi-volume book, such as \"Collected Works\".");
                case MAINTITLEADDON:
                    coverage[50] = true;
                    printCoverage(coverage);
                    return Localization.lang("Annex to the \"Maintitle\", to be printed in a different font.");
                case MONTH:
                    coverage[51] = true;
                    printCoverage(coverage);
                    return Localization.lang("Publication month.");
                case NAMEADDON:
                    coverage[52] = true;
                    printCoverage(coverage);
                    return Localization.lang("Addon to be printed immediately after the author name in the bibliography.");
                case NOTE:
                    coverage[53] = true;
                    printCoverage(coverage);
                    return Localization.lang("Miscellaneous bibliographic data which does not fit into any other field.");
                case NUMBER:
                    coverage[54] = true;
                    printCoverage(coverage);
                    return Localization.lang("Number of a journal or the volume/number of a book in a series.");
                case ORGANIZATION:
                    coverage[55] = true;
                    printCoverage(coverage);
                    return Localization.lang("Organization(s) that published a manual or an online resource, or sponsored a conference.");
                case ORIGDATE:
                    coverage[56] = true;
                    printCoverage(coverage);
                    return Localization.lang("If the work is a translation, a reprint, or something similar, the publication date of the original edition.");
                case ORIGLANGUAGE:
                    coverage[57] = true;
                    printCoverage(coverage);
                    return Localization.lang("If the work is a translation, the language(s) of the original work.");
                case PAGES:
                    coverage[58] = true;
                    printCoverage(coverage);
                    return Localization.lang("One or more page numbers or page ranges.") + "\n" +
                            Localization.lang("If the work is published as part of another one, such as an article in a journal or a collection, this field holds the relevant page range in that other work. It may also be used to limit the reference to a specific part of a work (a chapter in a book, for example). For papers in electronic journals with anon-classical pagination setup the eid field may be more suitable.");
                case PAGETOTAL:
                    coverage[59] = true;
                    printCoverage(coverage);
                    return Localization.lang("Total number of pages of the work.");
                case PAGINATION:
                    coverage[60] = true;
                    printCoverage(coverage);
                    return Localization.lang("Pagination of the work. The key should be given in the singular form.");
                case PART:
                    coverage[61] = true;
                    printCoverage(coverage);
                    return Localization.lang("Number of a partial volume. This field applies to books only, not to journals. It may be used when a logical volume consists of two or more physical ones.");
                case PUBLISHER:
                    coverage[62] = true;
                    printCoverage(coverage);
                    return Localization.lang("Name(s) of the publisher(s).");
                case PUBSTATE:
                    coverage[63] = true;
                    printCoverage(coverage);
                    return Localization.lang("Publication state of the work, e. g., \"in press\".");
                case SERIES:
                    coverage[64] = true;
                    printCoverage(coverage);
                    return Localization.lang("Name of a publication series, such as \"Studies in...\", or the number of a journal series.");
                case SHORTTITLE:
                    coverage[65] = true;
                    printCoverage(coverage);
                    return Localization.lang("Title in an abridged form.");
                case SUBTITLE:
                    coverage[66] = true;
                    printCoverage(coverage);
                    return Localization.lang("Subtitle of the work.");
                case TITLE:
                    coverage[67] = true;
                    printCoverage(coverage);
                    return Localization.lang("Title of the work.");
                case TITLEADDON:
                    coverage[68] = true;
                    printCoverage(coverage);
                    return Localization.lang("Annex to the \"Title\", to be printed in a different font.");
                case TRANSLATOR:
                    coverage[69] = true;
                    printCoverage(coverage);
                    return Localization.lang("Translator(s) of the \"Title\" or \"Booktitle\", depending on the entry type. If the translator is identical to the \"Editor\", the standard styles will automatically concatenate these fields in the bibliography.");
                case TYPE:
                    coverage[70] = true;
                    printCoverage(coverage);
                    return Localization.lang("Type of a \"Manual\", \"Patent\", \"Report\", or \"Thesis\".");
                case URL:
                    coverage[71] = true;
                    printCoverage(coverage);
                    return Localization.lang("URL of an online publication.");
                case URLDATE:
                    coverage[72] = true;
                    printCoverage(coverage);
                    return Localization.lang("Access date of the address specified in the url field.");
                case VENUE:
                    coverage[73] = true;
                    printCoverage(coverage);
                    return Localization.lang("Location of a conference, a symposium, or some other event.");
                case VERSION:
                    coverage[74] = true;
                    printCoverage(coverage);
                    return Localization.lang("Revision number of a piece of software, a manual, etc.");
                case VOLUME:
                    coverage[75] = true;
                    printCoverage(coverage);
                    return Localization.lang("Volume of a multi-volume book or a periodical.");
                case VOLUMES:
                    coverage[76] = true;
                    printCoverage(coverage);
                    return Localization.lang("Total number of volumes of a multi-volume work.");
                case YEAR:
                    coverage[77] = true;
                    printCoverage(coverage);
                    return Localization.lang("Year of publication.");
                case CROSSREF:
                    coverage[78] = true;
                    printCoverage(coverage);
                    return Localization.lang("This field holds an entry key for the cross-referencing feature. Child entries with a \"Crossref\" field inherit data from the parent entry specified in the \"Crossref\" field.");
                case GENDER:
                    coverage[79] = true;
                    printCoverage(coverage);
                    return Localization.lang("Gender of the author or gender of the editor, if there is no author.");
                case KEYWORDS:
                    coverage[80] = true;
                    printCoverage(coverage);
                    return Localization.lang("Separated list of keywords.");
                case RELATED:
                    coverage[81] = true;
                    printCoverage(coverage);
                    return Localization.lang("Citation keys of other entries which have a relationship to this entry.");
                case XREF:
                    coverage[82] = true;
                    printCoverage(coverage);
                    return Localization.lang("This field is an alternative cross-referencing mechanism. It differs from \"Crossref\" in that the child entry will not inherit any data from the parent entry specified in the \"Xref\" field.");
                case GROUPS:
                    coverage[83] = true;
                    printCoverage(coverage);
                    return Localization.lang("Name(s) of the (manual) groups the entry belongs to.");
                case OWNER:
                    coverage[84] = true;
                    printCoverage(coverage);
                    return Localization.lang("Owner/creator of this entry.");
                case TIMESTAMP:
                    coverage[85] = true;
                    printCoverage(coverage);
                    return Localization.lang("Timestamp of this entry, when it has been created or last modified.");
            }
        } else if (field instanceof InternalField internalField) {
            coverage[86] = true;
            switch (internalField) {
                case KEY_FIELD:
                    coverage[87] = true;
                    printCoverage(coverage);
                    return Localization.lang("Key by which the work may be cited.");
            }
        } else if (field instanceof SpecialField specialField) {
            coverage[88] = true;
            switch (specialField) {
                case PRINTED:
                    coverage[89] = true;
                    printCoverage(coverage);
                    return Localization.lang("User-specific printed flag, in case the entry has been printed.");
                case PRIORITY:
                    coverage[90] = true;
                    printCoverage(coverage);
                    return Localization.lang("User-specific priority.");
                case QUALITY:
                    coverage[91] = true;
                    printCoverage(coverage);
                    return Localization.lang("User-specific quality flag, in case its quality is assured.");
                case RANKING:
                    coverage[92] = true;
                    printCoverage(coverage);
                    return Localization.lang("User-specific ranking.");
                case READ_STATUS:
                    coverage[93] = true;
                    printCoverage(coverage);
                    return Localization.lang("User-specific read status.");
                case RELEVANCE:
                    coverage[94] = true;
                    printCoverage(coverage);
                    return Localization.lang("User-specific relevance flag, in case the entry is relevant.");
            }
        }
        coverage[95] = true;
        printCoverage(coverage);
                    return "";
    }

    static void printCoverage(boolean[] coverage) {
        for (int i = 0; i < coverage.length; i++) {
            if (coverage[i]) {
                System.out.print(i + " ");
            }
        }
        System.out.println();
    }
}
