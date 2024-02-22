package org.jabref.logic.layout;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Supplier;

import org.jabref.logic.formatter.bibtexfields.HtmlToLatexFormatter;
import org.jabref.logic.formatter.bibtexfields.UnicodeToLatexFormatter;
import org.jabref.logic.journals.JournalAbbreviationRepository;
import org.jabref.logic.layout.format.AuthorAbbreviator;
import org.jabref.logic.layout.format.AuthorAndToSemicolonReplacer;
import org.jabref.logic.layout.format.AuthorAndsCommaReplacer;
import org.jabref.logic.layout.format.AuthorAndsReplacer;
import org.jabref.logic.layout.format.AuthorFirstAbbrLastCommas;
import org.jabref.logic.layout.format.AuthorFirstAbbrLastOxfordCommas;
import org.jabref.logic.layout.format.AuthorFirstFirst;
import org.jabref.logic.layout.format.AuthorFirstFirstCommas;
import org.jabref.logic.layout.format.AuthorFirstLastCommas;
import org.jabref.logic.layout.format.AuthorFirstLastOxfordCommas;
import org.jabref.logic.layout.format.AuthorLF_FF;
import org.jabref.logic.layout.format.AuthorLF_FFAbbr;
import org.jabref.logic.layout.format.AuthorLastFirst;
import org.jabref.logic.layout.format.AuthorLastFirstAbbrCommas;
import org.jabref.logic.layout.format.AuthorLastFirstAbbrOxfordCommas;
import org.jabref.logic.layout.format.AuthorLastFirstAbbreviator;
import org.jabref.logic.layout.format.AuthorLastFirstCommas;
import org.jabref.logic.layout.format.AuthorLastFirstOxfordCommas;
import org.jabref.logic.layout.format.AuthorNatBib;
import org.jabref.logic.layout.format.AuthorOrgSci;
import org.jabref.logic.layout.format.Authors;
import org.jabref.logic.layout.format.CSLType;
import org.jabref.logic.layout.format.CompositeFormat;
import org.jabref.logic.layout.format.CreateBibORDFAuthors;
import org.jabref.logic.layout.format.CreateDocBook4Authors;
import org.jabref.logic.layout.format.CreateDocBook4Editors;
import org.jabref.logic.layout.format.CreateDocBook5Authors;
import org.jabref.logic.layout.format.CreateDocBook5Editors;
import org.jabref.logic.layout.format.CurrentDate;
import org.jabref.logic.layout.format.DOICheck;
import org.jabref.logic.layout.format.DOIStrip;
import org.jabref.logic.layout.format.DateFormatter;
import org.jabref.logic.layout.format.Default;
import org.jabref.logic.layout.format.EntryTypeFormatter;
import org.jabref.logic.layout.format.FileLink;
import org.jabref.logic.layout.format.FirstPage;
import org.jabref.logic.layout.format.FormatPagesForHTML;
import org.jabref.logic.layout.format.FormatPagesForXML;
import org.jabref.logic.layout.format.GetOpenOfficeType;
import org.jabref.logic.layout.format.HTMLChars;
import org.jabref.logic.layout.format.HTMLParagraphs;
import org.jabref.logic.layout.format.HayagrivaType;
import org.jabref.logic.layout.format.IfPlural;
import org.jabref.logic.layout.format.Iso690FormatDate;
import org.jabref.logic.layout.format.Iso690NamesAuthors;
import org.jabref.logic.layout.format.JournalAbbreviator;
import org.jabref.logic.layout.format.LastPage;
import org.jabref.logic.layout.format.LatexToUnicodeFormatter;
import org.jabref.logic.layout.format.MarkdownFormatter;
import org.jabref.logic.layout.format.NameFormatter;
import org.jabref.logic.layout.format.NoSpaceBetweenAbbreviations;
import org.jabref.logic.layout.format.NotFoundFormatter;
import org.jabref.logic.layout.format.Number;
import org.jabref.logic.layout.format.Ordinal;
import org.jabref.logic.layout.format.RTFChars;
import org.jabref.logic.layout.format.RemoveBrackets;
import org.jabref.logic.layout.format.RemoveBracketsAddComma;
import org.jabref.logic.layout.format.RemoveLatexCommandsFormatter;
import org.jabref.logic.layout.format.RemoveTilde;
import org.jabref.logic.layout.format.RemoveWhitespace;
import org.jabref.logic.layout.format.Replace;
import org.jabref.logic.layout.format.ReplaceWithEscapedDoubleQuotes;
import org.jabref.logic.layout.format.RisAuthors;
import org.jabref.logic.layout.format.RisKeywords;
import org.jabref.logic.layout.format.RisMonth;
import org.jabref.logic.layout.format.ShortMonthFormatter;
import org.jabref.logic.layout.format.ToLowerCase;
import org.jabref.logic.layout.format.ToUpperCase;
import org.jabref.logic.layout.format.WrapContent;
import org.jabref.logic.layout.format.WrapFileLinks;
import org.jabref.logic.layout.format.XMLChars;
import org.jabref.logic.openoffice.style.OOPreFormatter;
import org.jabref.model.database.BibDatabase;
import org.jabref.model.database.BibDatabaseContext;
import org.jabref.model.entry.BibEntry;
import org.jabref.model.entry.field.FieldFactory;
import org.jabref.model.entry.field.InternalField;
import org.jabref.model.entry.field.UnknownField;
import org.jabref.model.strings.StringUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


class LayoutEntry {

    private static Map<String, Supplier<LayoutFormatter>> formatterMap = new HashMap<>();


    private static boolean[] branchcoverage = new boolean[73];
    private static final Logger LOGGER = LoggerFactory.getLogger(LayoutEntry.class);

    private List<LayoutFormatter> option;
    // Formatter to be run after other formatters:
    private LayoutFormatter postFormatter;

    private String text;
    private List<LayoutEntry> layoutEntries;
    private final int type;
    private final List<String> invalidFormatter = new ArrayList<>();

    private final List<Path> fileDirForDatabase;
    private final LayoutFormatterPreferences preferences;
    private final JournalAbbreviationRepository abbreviationRepository;

    public LayoutEntry(StringInt si,
                       List<Path> fileDirForDatabase,
                       LayoutFormatterPreferences preferences,
                       JournalAbbreviationRepository abbreviationRepository) {
        this.preferences = preferences;
        this.abbreviationRepository = abbreviationRepository;
        this.fileDirForDatabase = Objects.requireNonNullElse(fileDirForDatabase, Collections.emptyList());

        type = si.i;
        switch (type) {
            case LayoutHelper.IS_LAYOUT_TEXT ->
                    text = si.s;
            case LayoutHelper.IS_SIMPLE_COMMAND ->
                    text = si.s.trim();
            case LayoutHelper.IS_OPTION_FIELD ->
                    doOptionField(si.s);
            default -> {
                // IS_FIELD_START and IS_FIELD_END
            }
        }
    }

    public LayoutEntry(List<StringInt> parsedEntries,
                       int layoutType,
                       List<Path> fileDirForDatabase,
                       LayoutFormatterPreferences preferences,
                       JournalAbbreviationRepository abbreviationRepository) {
        this.preferences = preferences;
        this.abbreviationRepository = abbreviationRepository;
        this.fileDirForDatabase = Objects.requireNonNullElse(fileDirForDatabase, Collections.emptyList());

        List<LayoutEntry> tmpEntries = new ArrayList<>();
        String blockStart = parsedEntries.getFirst().s;
        String blockEnd = parsedEntries.getLast().s;

        if (!blockStart.equals(blockEnd)) {
            LOGGER.warn("Field start and end entry must be equal.");
        }

        type = layoutType;
        text = blockEnd;
        List<StringInt> blockEntries = null;
        for (StringInt parsedEntry : parsedEntries.subList(1, parsedEntries.size() - 1)) {
            switch (parsedEntry.i) {
                case LayoutHelper.IS_FIELD_START:
                case LayoutHelper.IS_GROUP_START:
                    blockEntries = new ArrayList<>();
                    blockStart = parsedEntry.s;
                    break;
                case LayoutHelper.IS_FIELD_END:
                case LayoutHelper.IS_GROUP_END:
                    if (blockStart.equals(parsedEntry.s)) {
                        blockEntries.add(parsedEntry);
                        int groupType = parsedEntry.i == LayoutHelper.IS_GROUP_END ? LayoutHelper.IS_GROUP_START :
                                LayoutHelper.IS_FIELD_START;
                        LayoutEntry le = new LayoutEntry(blockEntries, groupType, fileDirForDatabase, preferences, abbreviationRepository);
                        tmpEntries.add(le);
                        blockEntries = null;
                    } else {
                        LOGGER.warn("Nested field entries are not implemented!");
                    }
                    break;
                case LayoutHelper.IS_LAYOUT_TEXT:
                case LayoutHelper.IS_SIMPLE_COMMAND:
                case LayoutHelper.IS_OPTION_FIELD:
                default:
                    // Do nothing
                    break;
            }

            if (blockEntries == null) {
                tmpEntries.add(new LayoutEntry(parsedEntry, fileDirForDatabase, preferences, abbreviationRepository));
            } else {
                blockEntries.add(parsedEntry);
            }
        }

        layoutEntries = new ArrayList<>(tmpEntries);

        for (LayoutEntry layoutEntry : layoutEntries) {
            invalidFormatter.addAll(layoutEntry.getInvalidFormatters());
        }
    }

    public void setPostFormatter(LayoutFormatter formatter) {
        this.postFormatter = formatter;
    }

    public String doLayout(BibEntry bibtex, BibDatabase database) {
        switch (type) {
            case LayoutHelper.IS_LAYOUT_TEXT:
                return text;
            case LayoutHelper.IS_SIMPLE_COMMAND:
                String value = bibtex.getResolvedFieldOrAlias(FieldFactory.parseField(text), database).orElse("");

                // If a post formatter has been set, call it:
                if (postFormatter != null) {
                    value = postFormatter.format(value);
                }
                return value;
            case LayoutHelper.IS_FIELD_START:
            case LayoutHelper.IS_GROUP_START:
                return handleFieldOrGroupStart(bibtex, database);
            case LayoutHelper.IS_FIELD_END:
            case LayoutHelper.IS_GROUP_END:
                return "";
            case LayoutHelper.IS_OPTION_FIELD:
                return handleOptionField(bibtex, database);
            case LayoutHelper.IS_ENCODING_NAME:
                // Printing the encoding name is not supported in entry layouts, only
                // in begin/end layouts. This prevents breakage if some users depend
                // on a field called "encoding". We simply return this field instead:
                return bibtex.getResolvedFieldOrAlias(new UnknownField("encoding"), database).orElse(null);
            default:
                return "";
        }
    }

    private String handleOptionField(BibEntry bibtex, BibDatabase database) {
        String fieldEntry;

        if (InternalField.TYPE_HEADER.getName().equals(text)) {
            fieldEntry = bibtex.getType().getDisplayName();
        } else if (InternalField.OBSOLETE_TYPE_HEADER.getName().equals(text)) {
            LOGGER.warn("'" + InternalField.OBSOLETE_TYPE_HEADER
                    + "' is an obsolete name for the entry type. Please update your layout to use '"
                    + InternalField.TYPE_HEADER + "' instead.");
            fieldEntry = bibtex.getType().getDisplayName();
        } else {
            // changed section begin - arudert
            // resolve field (recognized by leading backslash) or text
            fieldEntry = text.startsWith("\\") ? bibtex
                    .getResolvedFieldOrAlias(FieldFactory.parseField(text.substring(1)), database)
                    .orElse("") : BibDatabase.getText(text, database);
            // changed section end - arudert
        }

        if (option != null) {
            for (LayoutFormatter anOption : option) {
                fieldEntry = anOption.format(fieldEntry);
            }
        }

        // If a post formatter has been set, call it:
        if (postFormatter != null) {
            fieldEntry = postFormatter.format(fieldEntry);
        }

        return fieldEntry;
    }

    private String handleFieldOrGroupStart(BibEntry bibtex, BibDatabase database) {
        Optional<String> field;
        boolean negated = false;
        if (type == LayoutHelper.IS_GROUP_START) {
            field = bibtex.getResolvedFieldOrAlias(FieldFactory.parseField(text), database);
        } else if (text.matches(".*(;|(\\&+)).*")) {
            // split the strings along &, && or ; for AND formatter
            String[] parts = text.split("\\s*(;|(\\&+))\\s*");
            field = Optional.empty();
            for (String part : parts) {
                negated = part.startsWith("!");
                field = bibtex.getResolvedFieldOrAlias(FieldFactory.parseField(negated ? part.substring(1).trim() : part), database);
                if (field.isPresent() == negated) {
                    break;
                }
            }
        } else {
            // split the strings along |, ||  for OR formatter
            String[] parts = text.split("\\s*(\\|+)\\s*");
            field = Optional.empty();
            for (String part : parts) {
                negated = part.startsWith("!");
                field = bibtex.getResolvedFieldOrAlias(FieldFactory.parseField(negated ? part.substring(1).trim() : part), database);
                if (field.isPresent() ^ negated) {
                    break;
                }
            }
        }

        if ((field.isPresent() == negated) || ((type == LayoutHelper.IS_GROUP_START)
                && field.get().equalsIgnoreCase(LayoutHelper.getCurrentGroup()))) {
            return null;
        } else {
            if (type == LayoutHelper.IS_GROUP_START) {
                LayoutHelper.setCurrentGroup(field.get());
            }
            StringBuilder sb = new StringBuilder(100);
            String fieldText;
            boolean previousSkipped = false;

            for (int i = 0; i < layoutEntries.size(); i++) {
                fieldText = layoutEntries.get(i).doLayout(bibtex, database);

                if (fieldText == null) {
                    if ((i + 1) < layoutEntries.size()) {
                        if (layoutEntries.get(i + 1).doLayout(bibtex, database).trim().isEmpty()) {
                            i++;
                            previousSkipped = true;
                            continue;
                        }
                    }
                } else {
                    // if previous was skipped --> remove leading line
                    // breaks
                    if (previousSkipped) {
                        int eol = 0;

                        while ((eol < fieldText.length())
                                && ((fieldText.charAt(eol) == '\n') || (fieldText.charAt(eol) == '\r'))) {
                            eol++;
                        }

                        if (eol < fieldText.length()) {
                            sb.append(fieldText.substring(eol));
                        }
                    } else {
                        sb.append(fieldText);
                    }
                }

                previousSkipped = false;
            }

            return sb.toString();
        }
    }

    /**
     * Do layout for general formatters (no bibtex-entry fields).
     *
     * @param databaseContext Bibtex Database
     */
    public String doLayout(BibDatabaseContext databaseContext, Charset encoding) {
        switch (type) {
            case LayoutHelper.IS_LAYOUT_TEXT:
                return text;

            case LayoutHelper.IS_SIMPLE_COMMAND:
                throw new UnsupportedOperationException("bibtex entry fields not allowed in begin or end layout");

            case LayoutHelper.IS_FIELD_START:
            case LayoutHelper.IS_GROUP_START:
                throw new UnsupportedOperationException("field and group starts not allowed in begin or end layout");

            case LayoutHelper.IS_FIELD_END:
            case LayoutHelper.IS_GROUP_END:
                throw new UnsupportedOperationException("field and group ends not allowed in begin or end layout");

            case LayoutHelper.IS_OPTION_FIELD:
                String field = BibDatabase.getText(text, databaseContext.getDatabase());
                if (option != null) {
                    for (LayoutFormatter anOption : option) {
                        field = anOption.format(field);
                    }
                }
                // If a post formatter has been set, call it:
                if (postFormatter != null) {
                    field = postFormatter.format(field);
                }

                return field;

            case LayoutHelper.IS_ENCODING_NAME:
                return encoding.displayName();

            case LayoutHelper.IS_FILENAME:
            case LayoutHelper.IS_FILEPATH:
                return databaseContext.getDatabasePath().map(Path::toAbsolutePath).map(Path::toString).orElse("");

            default:
                break;
        }
        return "";
    }

    private void doOptionField(String s) {
        List<String> v = StringUtil.tokenizeToList(s, "\n");

        if (v.size() == 1) {
            text = v.getFirst();
        } else {
            text = v.getFirst().trim();

            option = getOptionalLayout(v.get(1));
            // See if there was an undefined formatter:
            for (LayoutFormatter anOption : option) {
                if (anOption instanceof NotFoundFormatter formatter) {
                    String notFound = formatter.getNotFound();

                    invalidFormatter.add(notFound);
                }
            }
        }
    }

    public LayoutFormatter getLayoutFormatterByName(String name) {
        formatterMap = initHashMap();
        return formatterMap.get(name).get();
    }

    private Map<String, Supplier<LayoutFormatter>> initHashMap() {
        formatterMap.put("HTMLToLatexFormatter", HtmlToLatexFormatter::new);
        formatterMap.put("HtmlToLatex", HtmlToLatexFormatter::new);
        formatterMap.put("UnicodeToLatexFormatter", UnicodeToLatexFormatter::new);
        formatterMap.put("UnicodeToLatex", UnicodeToLatexFormatter::new);
        formatterMap.put("OOPreFormatter", OOPreFormatter::new);
        formatterMap.put("AuthorAbbreviator", AuthorAbbreviator::new);
        formatterMap.put("AuthorAndToSemicolonReplacer", AuthorAndToSemicolonReplacer::new);
        formatterMap.put("AuthorAndsCommaReplacer", AuthorAndsCommaReplacer::new);
        formatterMap.put("AuthorAndsReplacer", AuthorAndsReplacer::new);
        formatterMap.put("AuthorFirstAbbrLastCommas", AuthorFirstAbbrLastCommas::new);
        formatterMap.put("AuthorFirstAbbrLastOxfordCommas", AuthorFirstAbbrLastOxfordCommas::new);
        formatterMap.put("AuthorFirstFirst", AuthorFirstFirst::new);
        formatterMap.put("AuthorFirstFirstCommas", AuthorFirstFirstCommas::new);
        formatterMap.put("AuthorFirstLastCommas", AuthorFirstLastCommas::new);
        formatterMap.put("AuthorFirstLastOxfordCommas", AuthorFirstLastOxfordCommas::new);
        formatterMap.put("AuthorLastFirst", AuthorLastFirst::new);
        formatterMap.put("AuthorLastFirstAbbrCommas", AuthorLastFirstAbbrCommas::new);
        formatterMap.put("AuthorLastFirstAbbreviator", AuthorLastFirstAbbreviator::new);
        formatterMap.put("AuthorLastFirstAbbrOxfordCommas", AuthorLastFirstAbbrOxfordCommas::new);
        formatterMap.put("AuthorLastFirstCommas", AuthorLastFirstCommas::new);
        formatterMap.put("AuthorLastFirstOxfordCommas", AuthorLastFirstOxfordCommas::new);
        formatterMap.put("AuthorLF_FF", AuthorLF_FF::new);
        formatterMap.put("AuthorLF_FFAbbr", AuthorLF_FFAbbr::new);
        formatterMap.put("AuthorNatBib", AuthorNatBib::new);
        formatterMap.put("AuthorOrgSci", AuthorOrgSci::new);
        formatterMap.put("CompositeFormat", CompositeFormat::new);
        formatterMap.put("CreateBibORDFAuthors", CreateBibORDFAuthors::new);
        formatterMap.put("CreateDocBook4Authors", CreateDocBook4Authors::new);
        formatterMap.put("CreateDocBook4Editors", CreateDocBook4Editors::new);
        formatterMap.put("CreateDocBook5Authors", CreateDocBook5Authors::new);
        formatterMap.put("CreateDocBook5Editors", CreateDocBook5Editors::new);
        formatterMap.put("CurrentDate", CurrentDate::new);
        formatterMap.put("DateFormatter", DateFormatter::new);
        formatterMap.put("DOICheck", () -> new DOICheck(preferences.getDoiPreferences()));
        formatterMap.put("DOIStrip", DOIStrip::new);
        formatterMap.put("EntryTypeFormatter", EntryTypeFormatter::new);
        formatterMap.put("FirstPage", FirstPage::new);
        formatterMap.put("FormatPagesForHTML", FormatPagesForHTML::new);
        formatterMap.put("FormatPagesForXML", FormatPagesForXML::new);
        formatterMap.put("GetOpenOfficeType", GetOpenOfficeType::new);
        formatterMap.put("HTMLChars", HTMLChars::new);
        formatterMap.put("HTMLParagraphs", HTMLParagraphs::new);
        formatterMap.put("Iso690FormatDate", Iso690FormatDate::new);
        formatterMap.put("Iso690NamesAuthors", Iso690NamesAuthors::new);
        formatterMap.put("JournalAbbreviator", () -> new JournalAbbreviator(abbreviationRepository));
        formatterMap.put("LastPage", LastPage::new);
        formatterMap.put("LatexToUnicode", LatexToUnicodeFormatter::new);
        formatterMap.put("NameFormatter", NameFormatter::new);
        formatterMap.put("NoSpaceBetweenAbbreviations", NoSpaceBetweenAbbreviations::new);
        formatterMap.put("Ordinal", Ordinal::new);
        formatterMap.put("RemoveBrackets", RemoveBrackets::new);
        formatterMap.put("RemoveBracketsAddComma", RemoveBracketsAddComma::new);
        formatterMap.put("RemoveLatexCommands", RemoveLatexCommandsFormatter::new);
        formatterMap.put("RemoveTilde", RemoveTilde::new);
        formatterMap.put("RemoveWhitespace", RemoveWhitespace::new);
        formatterMap.put("RisKeywords", RisKeywords::new);
        formatterMap.put("RisMonth", RisMonth::new);
        formatterMap.put("RTFChars", RTFChars::new);
        formatterMap.put("ToLowerCase", ToLowerCase::new);
        formatterMap.put("ToUpperCase", ToUpperCase::new);
        formatterMap.put("XMLChars", XMLChars::new);
        formatterMap.put("Default", Default::new);
        formatterMap.put("FileLink", () -> new FileLink(fileDirForDatabase, preferences.getMainFileDirectory()));
        formatterMap.put("Number", Number::new);
        formatterMap.put("RisAuthors", RisAuthors::new);
        formatterMap.put("Authors", Authors::new);
        formatterMap.put("IfPlural", IfPlural::new);
        formatterMap.put("Replace", Replace::new);
        formatterMap.put("WrapContent", WrapContent::new);
        formatterMap.put("WrapFileLinks", () -> new WrapFileLinks(fileDirForDatabase, preferences.getMainFileDirectory()));
        formatterMap.put("Markdown", MarkdownFormatter::new);
        formatterMap.put("CSLType", CSLType::new);
        formatterMap.put("ShortMonth", ShortMonthFormatter::new);
        formatterMap.put("ReplaceWithEscapedDoubleQuotes", ReplaceWithEscapedDoubleQuotes::new);
        formatterMap.put("HayagrivaType", HayagrivaType::new);
        return formatterMap;
    }
    /*
    public LayoutFormatter getLayoutFormatterByName(String name) {

        switch (name) {
            case "HTMLToLatexFormatter", "HtmlToLatex" -> branchcoverage[0] = true;
            case "UnicodeToLatexFormatter", "UnicodeToLatex" -> branchcoverage[1] = true;
            case "OOPreFormatter" -> branchcoverage[2] = true;
            case "AuthorAbbreviator" -> branchcoverage[3] = true;
            case "AuthorAndToSemicolonReplacer" -> branchcoverage[4] = true;
            case "AuthorAndsCommaReplacer" -> branchcoverage[5] = true;
            case "AuthorAndsReplacer" -> branchcoverage[6] = true;
            case "AuthorFirstAbbrLastCommas" -> branchcoverage[7] = true;
            case "AuthorFirstAbbrLastOxfordCommas" -> branchcoverage[8] = true;
            case "AuthorFirstFirst" ->branchcoverage[9] = true;
            case "AuthorFirstFirstCommas" -> branchcoverage[10] = true;
            case "AuthorFirstLastCommas" -> branchcoverage[11] = true;
            case "AuthorFirstLastOxfordCommas" -> branchcoverage[12] = true;
            case "AuthorLastFirst" -> branchcoverage[13] = true;
            case "AuthorLastFirstAbbrCommas" -> branchcoverage[14] = true;
            case "AuthorLastFirstAbbreviator" -> branchcoverage[15] = true;
            case "AuthorLastFirstAbbrOxfordCommas" -> branchcoverage[16] = true;
            case "AuthorLastFirstCommas" -> branchcoverage[17] = true;
            case "AuthorLastFirstOxfordCommas" -> branchcoverage[18] = true;
            case "AuthorLF_FF" -> branchcoverage[19] = true;
            case "AuthorLF_FFAbbr" -> branchcoverage[20] = true;
            case "AuthorNatBib" -> branchcoverage[21] = true;
            case "AuthorOrgSci" -> branchcoverage[22] = true;
            case "CompositeFormat" -> branchcoverage[23] = true;
            case "CreateBibORDFAuthors" -> branchcoverage[24] = true;
            case "CreateDocBook4Authors" -> branchcoverage[25] = true;
            case "CreateDocBook4Editors" -> branchcoverage[26] = true;
            case "CreateDocBook5Authors" -> branchcoverage[27] = true;
            case "CreateDocBook5Editors" -> branchcoverage[28] = true;
            case "CurrentDate" -> branchcoverage[29] = true;
            case "DateFormatter" -> branchcoverage[30] = true;
            case "DOICheck" -> branchcoverage[31] = true;
            case "DOIStrip" -> branchcoverage[32] = true;
            case "EntryTypeFormatter" -> branchcoverage[33] = true;
            case "FirstPage" -> branchcoverage[34] = true;
            case "FormatPagesForHTML" -> branchcoverage[35] = true;
            case "FormatPagesForXML" -> branchcoverage[36] = true;
            case "GetOpenOfficeType" -> branchcoverage[37] = true;
            case "HTMLChars" -> branchcoverage[38] = true;
            case "HTMLParagraphs" -> branchcoverage[39] = true;
            case "Iso690FormatDate" -> branchcoverage[40] = true;
            case "Iso690NamesAuthors" -> branchcoverage[41] = true;
            case "JournalAbbreviator" -> branchcoverage[42] = true;
            case "LastPage" -> branchcoverage[43] = true;
// For backward compatibility
            case "FormatChars", "LatexToUnicode" -> branchcoverage[44] = true;
            case "NameFormatter" -> branchcoverage[45] = true;
            case "NoSpaceBetweenAbbreviations" -> branchcoverage[46] = true;
            case "Ordinal" -> branchcoverage[47] = true;
            case "RemoveBrackets" -> branchcoverage[48] = true;
            case "RemoveBracketsAddComma" -> branchcoverage[49] = true;
            case "RemoveLatexCommands" -> branchcoverage[50] = true;
            case "RemoveTilde" -> branchcoverage[51] = true;
            case "RemoveWhitespace" -> branchcoverage[52] = true;
            case "RisKeywords" -> branchcoverage[53] = true;
            case "RisMonth" -> branchcoverage[54] = true;
            case "RTFChars" -> branchcoverage[55] = true;
            case "ToLowerCase" -> branchcoverage[56] = true;
            case "ToUpperCase" -> branchcoverage[57] = true;
            case "XMLChars" -> branchcoverage[58] = true;
            case "Default" -> branchcoverage[59] = true;
            case "FileLink" -> branchcoverage[60] = true;
            case "Number" -> branchcoverage[61] = true;
            case "RisAuthors" -> branchcoverage[62] = true;
            case "Authors" -> branchcoverage[63] = true;
            case "IfPlural" -> branchcoverage[64] = true;
            case "Replace" -> branchcoverage[65] = true;
            case "WrapContent" -> branchcoverage[66] = true;
            case "WrapFileLinks" -> branchcoverage[67] = true;
            case "Markdown" -> branchcoverage[68] = true;
            case "CSLType" -> branchcoverage[69] = true;
            case "ShortMonth" -> branchcoverage[70] = true;
            case "ReplaceWithEscapedDoubleQuotes" -> branchcoverage[71] = true;
            case "HayagrivaType" -> branchcoverage[72] = true;
        };


        try (FileWriter writer = new FileWriter("branch_coverage.txt", true)) { // true for append mode
            //prints out the reached branches for each test case and save to file if true
            for (int i = 0; i < branchcoverage.length; i++) {
                if (branchcoverage[i]) {
                    String line = "branch " + i + ": " + branchcoverage[i] + "\n";
                    System.out.println(line);
                    writer.write(line);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return switch (name) {
            // For backward compatibility
            case "HTMLToLatexFormatter", "HtmlToLatex" -> new HtmlToLatexFormatter();
            // For backward compatibility
            case "UnicodeToLatexFormatter", "UnicodeToLatex" -> new UnicodeToLatexFormatter();
            case "OOPreFormatter" -> new OOPreFormatter();
            case "AuthorAbbreviator" -> new AuthorAbbreviator();
            case "AuthorAndToSemicolonReplacer" -> new AuthorAndToSemicolonReplacer(); //added test
            case "AuthorAndsCommaReplacer" -> new AuthorAndsCommaReplacer();
            case "AuthorAndsReplacer" -> new AuthorAndsReplacer();
            case "AuthorFirstAbbrLastCommas" -> new AuthorFirstAbbrLastCommas();
            case "AuthorFirstAbbrLastOxfordCommas" -> new AuthorFirstAbbrLastOxfordCommas(); // added test case
            case "AuthorFirstFirst" -> new AuthorFirstFirst();
            case "AuthorFirstFirstCommas" -> new AuthorFirstFirstCommas();
            case "AuthorFirstLastCommas" -> new AuthorFirstLastCommas();
            case "AuthorFirstLastOxfordCommas" -> new AuthorFirstLastOxfordCommas();
            case "AuthorLastFirst" -> new AuthorLastFirst();
            case "AuthorLastFirstAbbrCommas" -> new AuthorLastFirstAbbrCommas();
            case "AuthorLastFirstAbbreviator" -> new AuthorLastFirstAbbreviator();
            case "AuthorLastFirstAbbrOxfordCommas" -> new AuthorLastFirstAbbrOxfordCommas();
            case "AuthorLastFirstCommas" -> new AuthorLastFirstCommas();
            case "AuthorLastFirstOxfordCommas" -> new AuthorLastFirstOxfordCommas();
            case "AuthorLF_FF" -> new AuthorLF_FF();
            case "AuthorLF_FFAbbr" -> new AuthorLF_FFAbbr();
            case "AuthorNatBib" -> new AuthorNatBib();
            case "AuthorOrgSci" -> new AuthorOrgSci();
            case "CompositeFormat" -> new CompositeFormat();
            case "CreateBibORDFAuthors" -> new CreateBibORDFAuthors();
            case "CreateDocBook4Authors" -> new CreateDocBook4Authors();
            case "CreateDocBook4Editors" -> new CreateDocBook4Editors();
            case "CreateDocBook5Authors" -> new CreateDocBook5Authors();
            case "CreateDocBook5Editors" -> new CreateDocBook5Editors();
            case "CurrentDate" -> new CurrentDate();
            case "DateFormatter" -> new DateFormatter();
            case "DOICheck" -> new DOICheck(preferences.getDoiPreferences());
            case "DOIStrip" -> new DOIStrip();
            case "EntryTypeFormatter" -> new EntryTypeFormatter();
            case "FirstPage" -> new FirstPage();
            case "FormatPagesForHTML" -> new FormatPagesForHTML();
            case "FormatPagesForXML" -> new FormatPagesForXML();
            case "GetOpenOfficeType" -> new GetOpenOfficeType();
            case "HTMLChars" -> new HTMLChars();
            case "HTMLParagraphs" -> new HTMLParagraphs();
            case "Iso690FormatDate" -> new Iso690FormatDate();
            case "Iso690NamesAuthors" -> new Iso690NamesAuthors();
            case "JournalAbbreviator" -> new JournalAbbreviator(abbreviationRepository);
            case "LastPage" -> new LastPage();
// For backward compatibility
            case "FormatChars", "LatexToUnicode" -> new LatexToUnicodeFormatter();
            case "NameFormatter" -> new NameFormatter();
            case "NoSpaceBetweenAbbreviations" -> new NoSpaceBetweenAbbreviations();
            case "Ordinal" -> new Ordinal();
            case "RemoveBrackets" -> new RemoveBrackets();
            case "RemoveBracketsAddComma" -> new RemoveBracketsAddComma();
            case "RemoveLatexCommands" -> new RemoveLatexCommandsFormatter();
            case "RemoveTilde" -> new RemoveTilde();
            case "RemoveWhitespace" -> new RemoveWhitespace();
            case "RisKeywords" -> new RisKeywords();
            case "RisMonth" -> new RisMonth();
            case "RTFChars" -> new RTFChars();
            case "ToLowerCase" -> new ToLowerCase();
            case "ToUpperCase" -> new ToUpperCase();
            case "XMLChars" -> new XMLChars();
            case "Default" -> new Default();
            case "FileLink" -> new FileLink(fileDirForDatabase, preferences.getMainFileDirectory());
            case "Number" -> new Number();
            case "RisAuthors" -> new RisAuthors();
            case "Authors" -> new Authors();
            case "IfPlural" -> new IfPlural();
            case "Replace" -> new Replace();
            case "WrapContent" -> new WrapContent();
            case "WrapFileLinks" -> new WrapFileLinks(fileDirForDatabase, preferences.getMainFileDirectory());
            case "Markdown" -> new MarkdownFormatter();
            case "CSLType" -> new CSLType();
            case "ShortMonth" -> new ShortMonthFormatter();
            case "ReplaceWithEscapedDoubleQuotes" -> new ReplaceWithEscapedDoubleQuotes();
            case "HayagrivaType" -> new HayagrivaType();
            default -> null;
        };
    }
    */
    /**
     * Return an array of LayoutFormatters found in the given formatterName string (in order of appearance).
     */
    private List<LayoutFormatter> getOptionalLayout(String formatterName) {
        List<List<String>> formatterStrings = parseMethodsCalls(formatterName);
        List<LayoutFormatter> results = new ArrayList<>(formatterStrings.size());
        Map<String, String> userNameFormatter = NameFormatter.getNameFormatters(preferences.getNameFormatterPreferences());
        for (List<String> strings : formatterStrings) {
            String nameFormatterName = strings.getFirst().trim();

            // Check if this is a name formatter defined by this export filter:
            Optional<String> contents = preferences.getCustomExportNameFormatter(nameFormatterName);
            if (contents.isPresent()) {
                NameFormatter nf = new NameFormatter();
                nf.setParameter(contents.get());
                results.add(nf);
                continue;
            }

            // Try to load from formatters in formatter folder
            LayoutFormatter formatter = getLayoutFormatterByName(nameFormatterName);
            if (formatter != null) {
                // If this formatter accepts an argument, check if we have one, and set it if so
                if ((formatter instanceof ParamLayoutFormatter layoutFormatter) && (strings.size() >= 2)) {
                    layoutFormatter.setArgument(strings.get(1));
                }
                results.add(formatter);
                continue;
            }

            // Then check whether this is a user defined formatter
            String formatterParameter = userNameFormatter.get(nameFormatterName);
            if (formatterParameter != null) {
                NameFormatter nf = new NameFormatter();
                nf.setParameter(formatterParameter);
                results.add(nf);
                continue;
            }

            results.add(new NotFoundFormatter(nameFormatterName));
        }

        return results;
    }

    public List<String> getInvalidFormatters() {
        return invalidFormatter;
    }

    public static List<List<String>> parseMethodsCalls(String calls) {
        List<List<String>> result = new ArrayList<>();

        char[] c = calls.toCharArray();

        int i = 0;
        while (i < c.length) {
            int start = i;
            if (Character.isJavaIdentifierStart(c[i])) {
                i++;
                while ((i < c.length) && (Character.isJavaIdentifierPart(c[i]) || (c[i] == '.'))) {
                    i++;
                }
                if ((i < c.length) && (c[i] == '(')) {
                    String method = calls.substring(start, i);

                    // Skip the brace
                    i++;
                    int bracelevel = 0;

                    if (i < c.length) {
                        if (c[i] == '"') {
                            // Parameter is in format "xxx"

                            // Skip "
                            i++;

                            int startParam = i;
                            i++;
                            boolean escaped = false;
                            while (((i + 1) < c.length)
                                    && !(!escaped && (c[i] == '"') && (c[i + 1] == ')') && (bracelevel == 0))) {
                                if (c[i] == '\\') {
                                    escaped = !escaped;
                                } else if (c[i] == '(') {
                                    bracelevel++;
                                } else if (c[i] == ')') {
                                    bracelevel--;
                                } else {
                                    escaped = false;
                                }
                                i++;
                            }

                            String param = calls.substring(startParam, i);

                            result.add(Arrays.asList(method, param));
                        } else {
                            // Parameter is in format xxx

                            int startParam = i;

                            while ((i < c.length) && (!((c[i] == ')') && (bracelevel == 0)))) {
                                if (c[i] == '(') {
                                    bracelevel++;
                                } else if (c[i] == ')') {
                                    bracelevel--;
                                }
                                i++;
                            }

                            String param = calls.substring(startParam, i);

                            result.add(Arrays.asList(method, param));
                        }
                    } else {
                        // Incorrectly terminated open brace
                        result.add(Collections.singletonList(method));
                    }
                } else {
                    String method = calls.substring(start, i);
                    result.add(Collections.singletonList(method));
                }
            }
            i++;
        }

        return result;
    }

    public String getText() {
        return text;
    }
}
