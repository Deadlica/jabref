package org.jabref.logic.importer.fileformat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jabref.logic.importer.Importer;
import org.jabref.logic.importer.ParserResult;
import org.jabref.logic.l10n.Localization;
import org.jabref.logic.util.OS;
import org.jabref.logic.util.StandardFileType;
import org.jabref.logic.xmp.EncryptedPdfsNotSupportedException;
import org.jabref.logic.xmp.XmpUtilReader;
import org.jabref.model.entry.BibEntry;
import org.jabref.model.entry.LinkedFile;
import org.jabref.model.entry.field.StandardField;
import org.jabref.model.entry.types.EntryType;
import org.jabref.model.entry.types.StandardEntryType;
import org.jabref.model.strings.StringUtil;

import com.google.common.base.Strings;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

/**
 * PdfContentImporter parses data of the first page of the PDF and creates a BibTeX entry.
 * <p>
 * Currently, Springer and IEEE formats are supported.
 * <p>
 */
public class PdfContentImporter extends Importer {

    private static final Pattern YEAR_EXTRACT_PATTERN = Pattern.compile("\\d{4}");
    // input lines into several lines
    private String[] lines;
    // current index in lines
    private int lineIndex;
    private String curString;
    private String year;

    private boolean[] coverage = new boolean[52];

    /**
     * Removes all non-letter characters at the end
     * <p>
     * EXCEPTION: a closing bracket is NOT removed
     * </p>
     * <p>
     * TODO: Additionally replace multiple subsequent spaces by one space, which will cause a rename of this method
     * </p>
     */
    private String removeNonLettersAtEnd(String input) {
        String result = input.trim();
        if (result.isEmpty()) {
            return result;
        }
        char lastC = result.charAt(result.length() - 1);
        while (!Character.isLetter(lastC) && (lastC != ')')) {
            // if there is an asterix, a dot or something else at the end: remove it
            result = result.substring(0, result.length() - 1);
            if (result.isEmpty()) {
                break;
            } else {
                lastC = result.charAt(result.length() - 1);
            }
        }
        return result;
    }

    private String streamlineNames(String names) {
        // TODO: replace with NormalizeNamesFormatter?!
        String res;
        // supported formats:
        //   Matthias Schrepfer1, Johannes Wolf1, Jan Mendling1, and Hajo A. Reijers2
        if (names.contains(",")) {
            String[] splitNames = names.split(",");
            res = "";
            boolean isFirst = true;
            for (String splitName : splitNames) {
                String curName = removeNonLettersAtEnd(splitName);
                if (curName.indexOf("and") == 0) {
                    // skip possible ands between names
                    curName = curName.substring(3).trim();
                } else {
                    int posAnd = curName.indexOf(" and ");
                    if (posAnd >= 0) {
                        String nameBefore = curName.substring(0, posAnd);
                        // cannot be first name as "," is contained in the string
                        res = res.concat(" and ").concat(removeNonLettersAtEnd(nameBefore));
                        curName = curName.substring(posAnd + 5);
                    }
                }

                if (!curName.isEmpty()) {
                    if ("et al.".equalsIgnoreCase(curName)) {
                        curName = "others";
                    }
                    if (isFirst) {
                        isFirst = false;
                    } else {
                        res = res.concat(" and ");
                    }
                    res = res.concat(curName);
                }
            }
        } else {
            // assumption: names separated by space

            String[] splitNames = names.split(" ");
            if (splitNames.length == 0) {
                // empty names... something was really wrong...
                return "";
            }

            boolean workedOnFirstOrMiddle = false;
            boolean isFirst = true;
            int i = 0;
            res = "";
            do {
                if (workedOnFirstOrMiddle) {
                    // last item was a first or a middle name
                    // we have to check whether we are on a middle name
                    // if not, just add the item as last name and add an "and"
                    if (splitNames[i].contains(".")) {
                        // we found a middle name
                        res = res.concat(splitNames[i]).concat(" ");
                    } else {
                        // last name found
                        res = res.concat(removeNonLettersAtEnd(splitNames[i]));

                        if (!splitNames[i].isEmpty() && Character.isLowerCase(splitNames[i].charAt(0))) {
                            // it is probably be "van", "vom", ...
                            // we just rely on the fact that these things are written in lower case letters
                            // do NOT finish name
                            res = res.concat(" ");
                        } else {
                            // finish this name
                            workedOnFirstOrMiddle = false;
                        }
                    }
                } else {
                    if (!"and".equalsIgnoreCase(splitNames[i])) {
                        if (isFirst) {
                            isFirst = false;
                        } else {
                            res = res.concat(" and ");
                        }
                        if ("et".equalsIgnoreCase(splitNames[i]) && (splitNames.length > (i + 1))
                                && "al.".equalsIgnoreCase(splitNames[i + 1])) {
                            res = res.concat("others");
                            break;
                        } else {
                            res = res.concat(splitNames[i]).concat(" ");
                            workedOnFirstOrMiddle = true;
                        }
                    }  // do nothing, just increment i at the end of this iteration
                }
                i++;
            } while (i < splitNames.length);
        }
        return res;
    }

    private String streamlineTitle(String title) {
        return removeNonLettersAtEnd(title);
    }

    @Override
    public boolean isRecognizedFormat(BufferedReader input) throws IOException {
        return input.readLine().startsWith("%PDF");
    }

    @Override
    public ParserResult importDatabase(BufferedReader reader) throws IOException {
        Objects.requireNonNull(reader);
        throw new UnsupportedOperationException("PdfContentImporter does not support importDatabase(BufferedReader reader)."
                + "Instead use importDatabase(Path filePath, Charset defaultEncoding).");
    }

    @Override
    public ParserResult importDatabase(String data) throws IOException {
        Objects.requireNonNull(data);
        throw new UnsupportedOperationException("PdfContentImporter does not support importDatabase(String data)."
                + "Instead use importDatabase(Path filePath, Charset defaultEncoding).");
    }

    @Override
    public ParserResult importDatabase(Path filePath) {
        final ArrayList<BibEntry> result = new ArrayList<>(1);
        try (PDDocument document = new XmpUtilReader().loadWithAutomaticDecryption(filePath)) {
            String firstPageContents = getFirstPageContents(document);
            Optional<BibEntry> entry = getEntryFromPDFContent(firstPageContents, OS.NEWLINE);
            entry.ifPresent(result::add);
        } catch (EncryptedPdfsNotSupportedException e) {
            return ParserResult.fromErrorMessage(Localization.lang("Decryption not supported."));
        } catch (IOException exception) {
            return ParserResult.fromError(exception);
        }

        result.forEach(entry -> entry.addFile(new LinkedFile("", filePath.toAbsolutePath(), "PDF")));
        return new ParserResult(result);
    }

    // make this method package visible so we can test it
    Optional<BibEntry> getEntryFromPDFContent(String firstpageContents, String lineSeparator) {
        // idea: split[] contains the different lines
        // blocks are separated by empty lines
        // treat each block
        //   or do special treatment at authors (which are not broken)
        //   therefore, we do a line-based and not a block-based splitting
        // i points to the current line
        // curString (mostly) contains the current block
        //   the different lines are joined into one and thereby separated by " "

        String firstpageContentsUnifiedLineBreaks = StringUtil.unifyLineBreaks(firstpageContents, lineSeparator);

        lines = firstpageContentsUnifiedLineBreaks.split(lineSeparator);

        lineIndex = 0; // to prevent array index out of bounds exception on second run we need to reset i to zero

        proceedToNextNonEmptyLine();
        if (lineIndex >= lines.length) {
            coverage[0] = true;
            // PDF could not be parsed or is empty
            // return empty list
            return Optional.empty();
        }

        // we start at the current line
        curString = lines[lineIndex];
        // i might get incremented later and curString modified, too
        lineIndex = lineIndex + 1;

        String author;
        String editor = null;
        String abstractT = null;
        String keywords = null;
        String title;
        String conference = null;
        String DOI = null;
        String series = null;
        String volume = null;
        String number = null;
        String pages = null;
        // year is a class variable as the method extractYear() uses it;
        String publisher = null;

        EntryType type = StandardEntryType.InProceedings;
        if (curString.length() > 4) {
            coverage[1] = true;
            // special case: possibly conference as first line on the page
            extractYear();
            if (curString.contains("Conference")) {
                coverage[2] = true;
                fillCurStringWithNonEmptyLines();
                conference = curString;
                curString = "";
            } else {
                // e.g. Copyright (c) 1998 by the Genetics Society of America
                // future work: get year using RegEx
                String lower = curString.toLowerCase(Locale.ROOT);
                if (lower.contains("copyright")) {
                    coverage[3] = true;
                    fillCurStringWithNonEmptyLines();
                    publisher = curString;
                    curString = "";
                }
            }
        }

        // start: title
        fillCurStringWithNonEmptyLines();
        title = streamlineTitle(curString);
        curString = "";
        // i points to the next non-empty line

        // after title: authors
        author = null;
        while ((lineIndex < lines.length) && !"".equals(lines[lineIndex])) {
            coverage[4] = true;
            coverage[5] = true;
            // author names are unlikely to be lines among different lines
            // treat them line by line
            curString = streamlineNames(lines[lineIndex]);
            if (author == null) {
                coverage[6] = true;
                author = curString;
            } else {
                if (!"".equals(curString)) {
                    coverage[7] = true;
                    author = author.concat(" and ").concat(curString);
                }  // if lines[i] is "and" then "" is returned by streamlineNames -> do nothing
            }
            lineIndex++;
        }
        curString = "";
        lineIndex++;

        // then, abstract and keywords follow
        while (lineIndex < lines.length) {
            coverage[8] = true;
            curString = lines[lineIndex];
            if ((curString.length() >= "Abstract".length()) && "Abstract".equalsIgnoreCase(curString.substring(0, "Abstract".length()))) {
                coverage[9] = true;
                coverage[10] = true;
                if (curString.length() == "Abstract".length()) {
                    coverage[11] = true;
                    // only word "abstract" found -- skip line
                    curString = "";
                } else {
                    curString = curString.substring("Abstract".length() + 1).trim().concat(System.lineSeparator());
                }
                lineIndex++;
                // fillCurStringWithNonEmptyLines() cannot be used as that uses " " as line separator
                // whereas we need linebreak as separator
                while ((lineIndex < lines.length) && !"".equals(lines[lineIndex])) {
                    coverage[12] = true;
                    coverage[13] = true;
                    curString = curString.concat(lines[lineIndex]).concat(System.lineSeparator());
                    lineIndex++;
                }
                abstractT = curString.trim();
                lineIndex++;
            } else if ((curString.length() >= "Keywords".length()) && "Keywords".equalsIgnoreCase(curString.substring(0, "Keywords".length()))) {
                coverage[14] = true;
                coverage[15] = true;
                if (curString.length() == "Keywords".length()) {
                    coverage[16] = true;
                    // only word "Keywords" found -- skip line
                    curString = "";
                } else {
                    curString = curString.substring("Keywords".length() + 1).trim();
                }
                lineIndex++;
                fillCurStringWithNonEmptyLines();
                keywords = removeNonLettersAtEnd(curString);
            } else {
                String lower = curString.toLowerCase(Locale.ROOT);

                int pos = lower.indexOf("technical");
                if (pos >= 0) {
                    coverage[17] = true;
                    type = StandardEntryType.TechReport;
                    pos = curString.trim().lastIndexOf(' ');
                    if (pos >= 0) {
                        coverage[18] = true;
                        // assumption: last character of curString is NOT ' '
                        //   otherwise pos+1 leads to an out-of-bounds exception
                        number = curString.substring(pos + 1);
                    }
                }

                lineIndex++;
                proceedToNextNonEmptyLine();
            }
        }

        lineIndex = lines.length - 1;

        // last block: DOI, detailed information
        // sometimes, this information is in the third last block etc...
        // therefore, read until the beginning of the file

        while (lineIndex >= 0) {
            coverage[19] = true;
            readLastBlock();
            // i now points to the block before or is -1
            // curString contains the last block, separated by " "

            extractYear();

            int pos = curString.indexOf("(Eds.)");
            if ((pos >= 0) && (publisher == null)) {
                coverage[20] = true;
                coverage[21] = true;
                // looks like a Springer last line
                // e.g: A. Persson and J. Stirna (Eds.): PoEM 2009, LNBIP 39, pp. 161-175, 2009.
                publisher = "Springer";
                editor = streamlineNames(curString.substring(0, pos - 1));

                int edslength = "(Eds.)".length();
                int posWithEditor = pos + edslength + 2; // +2 because of ":" after (Eds.) and the subsequent space
                if (posWithEditor > curString.length()) {
                    coverage[22] = true;
                    curString = curString.substring(posWithEditor - 2); // we don't have any spaces after Eds so we substract the 2
                } else {
                    curString = curString.substring(posWithEditor);
                }
                String[] springerSplit = curString.split(", ");
                if (springerSplit.length >= 4) {
                    coverage[23] = true;
                    conference = springerSplit[0];

                    String seriesData = springerSplit[1];
                    int lastSpace = seriesData.lastIndexOf(' ');
                    series = seriesData.substring(0, lastSpace);
                    volume = seriesData.substring(lastSpace + 1);

                    pages = springerSplit[2].substring(4);

                    if (springerSplit[3].length() >= 4) {
                        coverage[24] = true;
                        year = springerSplit[3].substring(0, 4);
                    }
                }
            } else {
                if (DOI == null) {
                    coverage[25] = true;
                    pos = curString.indexOf("DOI");
                    if (pos < 0) {
                        coverage[26] = true;
                        pos = curString.indexOf(StandardField.DOI.getName());
                    }
                    if (pos >= 0) {
                        coverage[27] = true;
                        pos += 3;
                        if (curString.length() > pos) {
                            coverage[28] = true;
                            char delimiter = curString.charAt(pos);
                            if ((delimiter == ':') || (delimiter == ' ')) {
                                coverage[29] = true;
                                coverage[30] = true;
                                pos++;
                            }
                            int nextSpace = curString.indexOf(' ', pos);
                            if (nextSpace > 0) {
                                coverage[31] = true;
                                DOI = curString.substring(pos, nextSpace);
                            } else {
                                DOI = curString.substring(pos);
                            }
                        }
                    }
                }

                if ((publisher == null) && curString.contains("IEEE")) {
                    coverage[32] = true;
                    coverage[33] = true;
                    // IEEE has the conference things at the end
                    publisher = "IEEE";

                    // year is extracted by extractYear
                    // otherwise, we could it determine as follows:
                    // String yearStr = curString.substring(curString.length()-4);
                    // if (isYear(yearStr)) {
                    //  year = yearStr;
                    // }

                    if (conference == null) {
                        coverage[34] = true;
                        pos = curString.indexOf('$');
                        if (pos > 0) {
                            coverage[35] = true;
                            // we found the price
                            // before the price, the ISSN is stated
                            // skip that
                            pos -= 2;
                            while ((pos >= 0) && (curString.charAt(pos) != ' ')) {
                                coverage[36] = true;
                                coverage[37] = true;
                                pos--;
                            }
                            if (pos > 0) {
                                coverage[38] = true;
                                conference = curString.substring(0, pos);
                            }
                        }
                    }
                }
            }
        }

        BibEntry entry = new BibEntry();
        entry.setType(type);

        // TODO: institution parsing missing

        if (author != null) {
            coverage[39] = true;
            entry.setField(StandardField.AUTHOR, author);
        }
        if (editor != null) {
            coverage[40] = true;
            entry.setField(StandardField.EDITOR, editor);
        }
        if (abstractT != null) {
            coverage[41] = true;
            entry.setField(StandardField.ABSTRACT, abstractT);
        }
        if (!Strings.isNullOrEmpty(keywords)) {
            coverage[42] = true;
            entry.setField(StandardField.KEYWORDS, keywords);
        }
        if (title != null) {
            coverage[43] = true;
            entry.setField(StandardField.TITLE, title);
        }
        if (conference != null) {
            coverage[44] = true;
            entry.setField(StandardField.BOOKTITLE, conference);
        }
        if (DOI != null) {
            coverage[45] = true;
            entry.setField(StandardField.DOI, DOI);
        }
        if (series != null) {
            coverage[46] = true;
            entry.setField(StandardField.SERIES, series);
        }
        if (volume != null) {
            coverage[47] = true;
            entry.setField(StandardField.VOLUME, volume);
        }
        if (number != null) {
            coverage[48] = true;
            entry.setField(StandardField.NUMBER, number);
        }
        if (pages != null) {
            coverage[49] = true;
            entry.setField(StandardField.PAGES, pages);
        }
        if (year != null) {
            coverage[50] = true;
            entry.setField(StandardField.YEAR, year);
        }
        if (publisher != null) {
            coverage[51] = true;
            entry.setField(StandardField.PUBLISHER, publisher);
        }

        for (int i = 0; i < coverage.length; i++) {
            if (coverage[i]) {
                System.out.print(i + " ");
            }
        }

        return Optional.of(entry);
    }

    private String getFirstPageContents(PDDocument document) throws IOException {
        PDFTextStripper stripper = new PDFTextStripper();

        stripper.setStartPage(1);
        stripper.setEndPage(1);
        stripper.setSortByPosition(true);
        stripper.setParagraphEnd(System.lineSeparator());
        StringWriter writer = new StringWriter();
        stripper.writeText(document, writer);

        return writer.toString();
    }

    /**
     * Extract the year out of curString (if it is not yet defined)
     */
    private void extractYear() {
        if (year != null) {
            return;
        }

        Matcher m = YEAR_EXTRACT_PATTERN.matcher(curString);
        if (m.find()) {
            year = curString.substring(m.start(), m.end());
        }
    }

    /**
     * PDFTextStripper normally does NOT produce multiple empty lines
     * (besides at strange PDFs). These strange PDFs are handled here:
     * proceed to next non-empty line
     */
    private void proceedToNextNonEmptyLine() {
        while ((lineIndex < lines.length) && lines[lineIndex].trim().isEmpty()) {
            lineIndex++;
        }
    }

    /**
     * Fill curString with lines until "" is found
     * No trailing space is added
     * i is advanced to the next non-empty line (ignoring white space)
     * <p>
     * Lines containing only white spaces are ignored,
     * but NOT considered as ""
     * <p>
     * Uses GLOBAL variables lines, curLine, i
     */
    private void fillCurStringWithNonEmptyLines() {
        // ensure that curString does not end with " "
        curString = curString.trim();
        while ((lineIndex < lines.length) && !"".equals(lines[lineIndex])) {
            String curLine = lines[lineIndex].trim();
            if (!curLine.isEmpty()) {
                if (!curString.isEmpty()) {
                    // insert separating space if necessary
                    curString = curString.concat(" ");
                }
                curString = curString.concat(lines[lineIndex]);
            }
            lineIndex++;
        }

        proceedToNextNonEmptyLine();
    }

    /**
     * resets curString
     * curString now contains the last block (until "" reached)
     * Trailing space is added
     * <p>
     * invariant before/after: i points to line before the last handled block
     */
    private void readLastBlock() {
        while ((lineIndex >= 0) && lines[lineIndex].trim().isEmpty()) {
            lineIndex--;
        }
        // i is now at the end of a block

        int end = lineIndex;

        // find beginning
        while ((lineIndex >= 0) && !"".equals(lines[lineIndex])) {
            lineIndex--;
        }
        // i is now the line before the beginning of the block
        // this fulfills the invariant

        curString = "";
        for (int j = lineIndex + 1; j <= end; j++) {
            curString = curString.concat(lines[j].trim());
            if (j != end) {
                curString = curString.concat(" ");
            }
        }
    }

    @Override
    public String getName() {
        return "PDFcontent";
    }

    @Override
    public StandardFileType getFileType() {
        return StandardFileType.PDF;
    }

    @Override
    public String getDescription() {
        return "PdfContentImporter parses data of the first page of the PDF and creates a BibTeX entry. Currently, Springer and IEEE formats are supported.";
    }
}
