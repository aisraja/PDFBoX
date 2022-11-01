package com.ilyas.pdfbox_demo.Pdfbox;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDSimpleFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;

import java.awt.*;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

public class Pdfbox {

    // FOR FONT CONFIGURATION
    private static final PDSimpleFont FONT = PDType1Font.HELVETICA_BOLD;
    private static final float FONT_SIZE = 12f;
    private static final float TABLE_FONT_SIZE = 8f;
    private static final float HEIGHT_TABLE_STARTING_POINT = 750f;
    private static final Float TABLE_MARGIN = 18f;
    private static final float CELL_MARGIN = 5.0f;
    private static final float ROW_HEIGHT = 22.0f;


    // STATIC VALUES
    private static final String HEADER_TEXT = "Account Balance Report";
    private static final String FOOTER_TEXT = "Report - Page %d";
    private static final String PDF_FILE_LOCATION = "/Users/ilyasdev/Desktop/test.pdf";

    // THIS IS DYNAMIC, YOU CAN TRY OTHER COLUMNS
    private static final String[] COLUMNS_NAMES = {"FIRST NAME", "LAST NAME", "ACCOUNT TYPE NAME", "ALIAS", "STATUS", "BALANCE"};


    // MAIN METHOD
    public static void main(String[] args) throws IOException {
        generatePdfFile();
    }

    public static void generatePdfFile() throws IOException {
        try (var doc = new PDDocument()) {
            var page = new PDPage(PDRectangle.A4);
            doc.addPage(page);

            try (var contentStream = new PDPageContentStream(doc, page)) {
                // PDF Header
                drawHeader(page, contentStream);

                // DRAW THE TABLE
                drawReportTable(doc, page, contentStream);

            }
            doc.save(PDF_FILE_LOCATION);
        }
    }

    private static void drawHeader(PDPage page, PDPageContentStream contentStream) throws IOException {
        var pageWidth = page.getMediaBox().getWidth();
        var pageHeight = page.getMediaBox().getHeight();
        var heightCount = pageHeight - 40;
        float tableWidth = pageWidth - 2.0f * TABLE_MARGIN;

        // Text to display in the header
        // Draw background color for title header
        contentStream.setNonStrokingColor(new Color(137, 207, 240));
        contentStream.addRect(TABLE_MARGIN, heightCount - 8, tableWidth, CELL_MARGIN + 18);
        contentStream.fill();
        contentStream.setNonStrokingColor(Color.BLACK);

        contentStream.beginText();
        contentStream.setFont(FONT, FONT_SIZE);
        contentStream.newLineAtOffset((pageWidth / 2) - ((FONT.getStringWidth(HEADER_TEXT) / 1700f) * FONT_SIZE), heightCount);
        contentStream.showText(HEADER_TEXT);
        contentStream.endText();
        heightCount -= 40;

        // Display current date
        contentStream.beginText();
        contentStream.setFont(FONT, FONT_SIZE);
        contentStream.newLineAtOffset((pageWidth / 2) + FONT_SIZE * 4, heightCount);
        contentStream.showText("Date: ");
        contentStream.endText();

        // Position the date value
        contentStream.beginText();
        contentStream.setFont(PDType1Font.HELVETICA, FONT_SIZE);
        contentStream.newLineAtOffset((pageWidth / 2) + FONT_SIZE * 10, heightCount);
        contentStream.showText(LocalDate.now().format(DateTimeFormatter.ofPattern("MM/dd/yyyy")));
        contentStream.endText();
    }

    public static void drawReportTable(PDDocument document, PDPage page, PDPageContentStream contentStream) throws IOException {

        // Here replace this with the data coming from your Database
        var employeesList = getEmployees();

        // Group by employee status to determine the rows length
        var employeeStatusListMap = employeesList.stream().collect(Collectors.groupingBy(Employee::getStatus));

        final int cols = COLUMNS_NAMES.length;
        final float tableWidth = page.getMediaBox().getWidth() - 2.0f * TABLE_MARGIN;
        final float colWidth = tableWidth / cols;

        //now add the text
        contentStream.setFont(PDType1Font.HELVETICA_BOLD, FONT_SIZE - 5f);

        float textHorizontalPoint = TABLE_MARGIN + CELL_MARGIN;
        float textVerticalPoint = HEIGHT_TABLE_STARTING_POINT - 15.0f;

        float nexty = HEIGHT_TABLE_STARTING_POINT;
        float nextx = TABLE_MARGIN;

        // Grand total
        double totalAmount = 0;
        var pageCounter = 1;
        final int rowsPerPage = 32;

        drawLineShape(contentStream, nexty, tableWidth);
        nexty -= ROW_HEIGHT;
        createTableHeader(contentStream, textHorizontalPoint, textVerticalPoint, colWidth, tableWidth);
        drawLineShape(contentStream, nexty, tableWidth);

        for (var employeeStatus : employeeStatusListMap.keySet()) {
            // For status row
            contentStream.moveTo(TABLE_MARGIN, nexty - ROW_HEIGHT);
            contentStream.lineTo(colWidth + 18, nexty - ROW_HEIGHT);
            contentStream.stroke();

            textVerticalPoint -= ROW_HEIGHT;
            textHorizontalPoint = TABLE_MARGIN + CELL_MARGIN;

            createStatusRow(employeeStatus, contentStream, textHorizontalPoint, textVerticalPoint);

            textVerticalPoint -= ROW_HEIGHT;
            textHorizontalPoint = TABLE_MARGIN + CELL_MARGIN;

            drawLineShape(contentStream, nexty, tableWidth);
            nexty -= ROW_HEIGHT;
            nextx += colWidth;

            var subTotal = 0;

            var list = employeeStatusListMap.get(employeeStatus);
            for (final var employee : list) {
                // NEW PAGE DETECTED
                if (nexty < TABLE_MARGIN + 60) {
                    nextx = TABLE_MARGIN;
                    // Draw columns
                    float tableHeight = ROW_HEIGHT * rowsPerPage;
                    for (int i = 0; i < cols + 1; i++) {
                        drawColumnShape(contentStream, nextx, tableHeight, HEIGHT_TABLE_STARTING_POINT);
                        nextx += colWidth;
                    }
                    // Draw the footer
                    createFooter(contentStream, page.getMediaBox().getWidth(), pageCounter++);

                    contentStream.close();
                    page = new PDPage(PDRectangle.A4);
                    document.addPage(page);
                    contentStream = new PDPageContentStream(document, page);

                    nextx = TABLE_MARGIN;
                    nexty = HEIGHT_TABLE_STARTING_POINT;
                    textVerticalPoint = nexty - 15.0f;
                    textHorizontalPoint = TABLE_MARGIN + CELL_MARGIN;

                    // Draw again the header for the new page with columns and everything
                    drawLineShape(contentStream, nexty, tableWidth);
                    nexty -= ROW_HEIGHT;
                    createTableHeader(contentStream, textHorizontalPoint, textVerticalPoint, colWidth, tableWidth);
                    drawLineShape(contentStream, nexty, tableWidth);
                    textVerticalPoint -= ROW_HEIGHT;
                }

                createEmployeeRow(employee, contentStream, textHorizontalPoint, textVerticalPoint, colWidth);
                textVerticalPoint -= ROW_HEIGHT;
                textHorizontalPoint = TABLE_MARGIN + CELL_MARGIN;

                nexty -= ROW_HEIGHT;
                nextx += colWidth;
                drawLineShape(contentStream, nexty, tableWidth);
                subTotal += employee.getBalance();
            }
            // Calculate sub-totals alongside with GRAND TOTAL
            totalAmount += subTotal;
            createSubTotal(String.format("%s Total", employeeStatus), subTotal, contentStream, textVerticalPoint, colWidth);

            nexty -= ROW_HEIGHT;
            nextx += colWidth;
            drawLineShape(contentStream, nexty, tableWidth);
        }
        // Draw footer
        createFooter(contentStream, page.getMediaBox().getWidth(), pageCounter);

        // Grand total amount
        drawGrandTotal(totalAmount, contentStream, textVerticalPoint - ROW_HEIGHT, colWidth);
        // Draw last line and columns
        nexty -= ROW_HEIGHT;
        drawLineShape(contentStream, nexty, tableWidth);
        // Draw columns
        nextx = TABLE_MARGIN;
        nexty += ROW_HEIGHT;
        for (int i = 0; i < cols + 1; i++) {
            contentStream.moveTo(nextx, HEIGHT_TABLE_STARTING_POINT);
            contentStream.lineTo(nextx, nexty);
            contentStream.stroke();
            nextx += colWidth;
        }
        contentStream.close();
    }

    private static void drawLineShape(PDPageContentStream contentStream, float nexty, float tableWidth) throws IOException {
        contentStream.moveTo(TABLE_MARGIN, nexty);
        contentStream.lineTo(TABLE_MARGIN + tableWidth, nexty);
        contentStream.stroke();
    }

    private static void drawColumnShape(PDPageContentStream contentStream, float nextx, float tableHeight, float startingFrom) throws IOException {
        contentStream.moveTo(nextx, startingFrom);
        contentStream.lineTo(nextx, HEIGHT_TABLE_STARTING_POINT - tableHeight);
        contentStream.stroke();
    }

    private static void createStatusRow(EmployeeStatus status, PDPageContentStream contentStream, float textHorizontalPoint, float textVerticalPoint) throws IOException {
        contentStream.beginText();
        contentStream.setFont(FONT, TABLE_FONT_SIZE);
        contentStream.newLineAtOffset(textHorizontalPoint, textVerticalPoint);
        contentStream.showText(String.format("Status: %s", status));
        contentStream.endText();
    }

    private static void drawGrandTotal(double totalAmount, PDPageContentStream contentStream, float textVerticalPoint, float colWidth) throws IOException {
        var textHorizontalPoint = TABLE_MARGIN + CELL_MARGIN;
        for (int i = 0; i < COLUMNS_NAMES.length - 2; i++) {
            textHorizontalPoint += colWidth;
        }

        contentStream.setNonStrokingColor(new Color(137, 207, 240));
        contentStream.addRect(textHorizontalPoint-5, textVerticalPoint - 6, colWidth*2,
                ROW_HEIGHT-2);
        contentStream.fill();
        contentStream.setNonStrokingColor(Color.BLACK);

        contentStream.beginText();
        contentStream.setFont(FONT, TABLE_FONT_SIZE);
        contentStream.newLineAtOffset(textHorizontalPoint, textVerticalPoint);
        contentStream.showText("GRAND TOTAL");
        contentStream.endText();
        textHorizontalPoint += colWidth;

        contentStream.beginText();
        contentStream.newLineAtOffset(textHorizontalPoint, textVerticalPoint);
        contentStream.showText(String.valueOf(totalAmount));
        contentStream.endText();
    }


    private static void createEmployeeRow(Employee employee, PDPageContentStream contentStream, float textHorizontalPoint, float textVerticalPoint, float colWidth) throws IOException {
        contentStream.beginText();
        contentStream.setFont(FONT, TABLE_FONT_SIZE);
        contentStream.newLineAtOffset(textHorizontalPoint, textVerticalPoint);
        contentStream.showText(employee.getFirstName());
        contentStream.endText();
        textHorizontalPoint += colWidth;

        contentStream.beginText();
        contentStream.newLineAtOffset(textHorizontalPoint, textVerticalPoint);
        contentStream.showText(employee.getLastName());
        contentStream.endText();
        textHorizontalPoint += colWidth;

        contentStream.beginText();
        contentStream.newLineAtOffset(textHorizontalPoint, textVerticalPoint);
        contentStream.showText(employee.getAccountTypeName());
        contentStream.endText();
        textHorizontalPoint += colWidth;

        contentStream.beginText();
        contentStream.newLineAtOffset(textHorizontalPoint, textVerticalPoint);
        contentStream.showText(employee.getAlias());
        contentStream.endText();
        textHorizontalPoint += colWidth;

        contentStream.beginText();
        contentStream.newLineAtOffset(textHorizontalPoint, textVerticalPoint);
        contentStream.showText(employee.getStatus().toString());
        contentStream.endText();
        textHorizontalPoint += colWidth;

        contentStream.beginText();
        contentStream.newLineAtOffset(textHorizontalPoint, textVerticalPoint);
        contentStream.showText(String.valueOf(employee.getBalance()));
        contentStream.endText();
    }


    private static void createSubTotal(String columnName, double amount, PDPageContentStream contentStream, float textVerticalPoint, float colWidth) throws IOException {

        var textHorizontalPoint = TABLE_MARGIN + CELL_MARGIN;
        for (int i = 0; i < COLUMNS_NAMES.length - 2; i++) {
            textHorizontalPoint += colWidth;
        }

        contentStream.setNonStrokingColor(new Color(137, 207, 240));
        contentStream.addRect(textHorizontalPoint-5, textVerticalPoint - 6, colWidth*2,
                ROW_HEIGHT-2);
        contentStream.fill();
        contentStream.setNonStrokingColor(Color.BLACK);

        contentStream.beginText();
        contentStream.setFont(FONT, TABLE_FONT_SIZE);
        contentStream.newLineAtOffset(textHorizontalPoint, textVerticalPoint);
        contentStream.showText(columnName);
        contentStream.endText();
        textHorizontalPoint += colWidth;

        contentStream.beginText();
        contentStream.newLineAtOffset(textHorizontalPoint, textVerticalPoint);
        contentStream.showText(String.valueOf(amount));
        contentStream.endText();
    }

    private static void createFooter(PDPageContentStream contentStream, float pageWidth, int count) throws IOException {

        contentStream.beginText();
        contentStream.setFont(FONT, TABLE_FONT_SIZE);
        contentStream.newLineAtOffset((pageWidth / 2) - ((FONT.getStringWidth(FOOTER_TEXT) / 1700f) * TABLE_FONT_SIZE), TABLE_MARGIN);
        contentStream.showText(String.format(FOOTER_TEXT, count));
        contentStream.endText();
    }


    private static void createTableHeader(PDPageContentStream contentStream, float textHorizontalPoint,
                                          float textVerticalPoint, float colWidth, float tableWidth) throws IOException {
        contentStream.setNonStrokingColor(new Color(137, 207, 240));
        contentStream.addRect(TABLE_MARGIN, textVerticalPoint - 8, tableWidth, CELL_MARGIN + 18);
        contentStream.fill();
        contentStream.setNonStrokingColor(Color.BLACK);

        // Create table columns
        for (final var columnName : COLUMNS_NAMES) {
            contentStream.beginText();
            contentStream.setFont(FONT, TABLE_FONT_SIZE - 1);
            contentStream.newLineAtOffset(textHorizontalPoint, textVerticalPoint);
            contentStream.showText(columnName);
            contentStream.endText();
            textHorizontalPoint += colWidth;
        }
    }


    private static List<Employee> getEmployees() {
        return List.of(new Employee("Karim", "Douglas", "CORPORATE", "", EmployeeStatus.ACTIVE, 44), new Employee("Daamar", "Levy", "CORPORATE", "", EmployeeStatus.ACTIVE, 44), new Employee("Winsome", "Stewart", "CORPORATE", "", EmployeeStatus.PENDING, 32), new Employee("Keron", "Wisdom", "CORPORATE", "", EmployeeStatus.ACTIVE, 54), new Employee("Nevado", "Stewart", "CORPORATE", "", EmployeeStatus.PENDING, 32), new Employee("Rashun", "Watson", "CORPORATE", "", EmployeeStatus.PENDING, 32), new Employee("Tracey", "Marshall", "CORPORATE", "", EmployeeStatus.ACTIVE, 32), new Employee("LLOYD-ANN", "Hylton", "PREFERED", "", EmployeeStatus.PENDING, 32), new Employee("Waid", "Daley", "PREFERRED", "", EmployeeStatus.INACTIVE, 32), new Employee("Akalia", "Smart", "PREFERRED", "", EmployeeStatus.INACTIVE, 32), new Employee("Adisha", "Edwards", "PREFERRED", "", EmployeeStatus.INACTIVE, 32), new Employee("Nevado", "Stewart", "CORPORATE", "", EmployeeStatus.PENDING, 32), new Employee("Rashun", "Watson", "CORPORATE", "", EmployeeStatus.PENDING, 32), new Employee("Tracey", "Marshall", "CORPORATE", "", EmployeeStatus.ACTIVE, 32), new Employee("LLOYD-ANN", "Hylton", "PREFERED", "", EmployeeStatus.PENDING, 32), new Employee("Waid", "Daley", "PREFERRED", "", EmployeeStatus.INACTIVE, 32), new Employee("Akalia", "Smart", "PREFERRED", "", EmployeeStatus.INACTIVE, 32), new Employee("Adisha", "Edwards", "PREFERRED", "", EmployeeStatus.INACTIVE, 32), new Employee("Nevado", "Stewart", "CORPORATE", "", EmployeeStatus.PENDING, 32), new Employee("Rashun", "Watson", "CORPORATE", "", EmployeeStatus.PENDING, 32), new Employee("Tracey", "Marshall", "CORPORATE", "", EmployeeStatus.ACTIVE, 32), new Employee("LLOYD-ANN", "Hylton", "PREFERED", "", EmployeeStatus.PENDING, 32), new Employee("Waid", "Daley", "PREFERRED", "", EmployeeStatus.INACTIVE, 32), new Employee("Akalia", "Smart", "PREFERRED", "", EmployeeStatus.INACTIVE, 32), new Employee("Adisha", "Edwards", "PREFERRED", "", EmployeeStatus.INACTIVE, 32), new Employee("Nevado", "Stewart", "CORPORATE", "", EmployeeStatus.PENDING, 32), new Employee("Rashun", "Watson", "CORPORATE", "", EmployeeStatus.PENDING, 32), new Employee("Tracey", "Marshall", "CORPORATE", "", EmployeeStatus.ACTIVE, 32), new Employee("LLOYD-ANN", "Hylton", "PREFERED", "", EmployeeStatus.PENDING, 32), new Employee("Waid", "Daley", "PREFERRED", "", EmployeeStatus.INACTIVE, 32), new Employee("Akalia", "Smart", "PREFERRED", "", EmployeeStatus.INACTIVE, 32), new Employee("Adisha", "Edwards", "PREFERRED", "", EmployeeStatus.INACTIVE, 32), new Employee("Akalia", "Smart", "PREFERRED", "", EmployeeStatus.INACTIVE, 32), new Employee("Adisha", "Edwards", "PREFERRED", "", EmployeeStatus.INACTIVE, 32), new Employee("Akalia", "Smart", "PREFERRED", "", EmployeeStatus.INACTIVE, 32), new Employee("Adisha", "Edwards", "PREFERRED", "", EmployeeStatus.INACTIVE, 32), new Employee("Akalia", "Smart", "PREFERRED", "", EmployeeStatus.INACTIVE, 32), new Employee("Adisha", "Edwards", "PREFERRED", "", EmployeeStatus.INACTIVE, 32),
                new Employee("Akalia", "Smart", "PREFERRED", "", EmployeeStatus.INACTIVE, 32), new Employee("Adisha", "Edwards", "PREFERRED", "", EmployeeStatus.INACTIVE, 32), new Employee("Akalia", "Smart", "PREFERRED", "", EmployeeStatus.INACTIVE, 32), new Employee("Adisha", "Edwards", "PREFERRED", "", EmployeeStatus.INACTIVE, 32), new Employee("Akalia", "Smart", "PREFERRED", "", EmployeeStatus.INACTIVE, 32), new Employee("Adisha", "Edwards", "PREFERRED", "", EmployeeStatus.INACTIVE, 32),
                new Employee("Akalia", "Smart", "PREFERRED", "", EmployeeStatus.ACTIVE, 32), new Employee("Adisha", "Edwards", "PREFERRED", "", EmployeeStatus.ACTIVE, 32), new Employee("Akalia", "Smart", "PREFERRED", "", EmployeeStatus.ACTIVE, 32), new Employee("Adisha", "Edwards", "PREFERRED", "", EmployeeStatus.ACTIVE, 32), new Employee("Akalia", "Smart", "PREFERRED", "", EmployeeStatus.ACTIVE, 32), new Employee("Adisha", "Edwards", "PREFERRED", "", EmployeeStatus.ACTIVE, 32),
                new Employee("Akalia", "Smart", "PREFERRED", "", EmployeeStatus.INACTIVE, 32), new Employee("Adisha", "Edwards", "PREFERRED", "", EmployeeStatus.INACTIVE, 32), new Employee("Akalia", "Smart", "PREFERRED", "", EmployeeStatus.INACTIVE, 32), new Employee("Adisha", "Edwards", "PREFERRED", "", EmployeeStatus.INACTIVE, 32), new Employee("Akalia", "Smart", "PREFERRED", "", EmployeeStatus.INACTIVE, 32), new Employee("Adisha", "Edwards", "PREFERRED", "", EmployeeStatus.INACTIVE, 32),
                new Employee("Akalia", "Smart", "PREFERRED", "", EmployeeStatus.ACTIVE, 32), new Employee("Adisha", "Edwards", "PREFERRED", "", EmployeeStatus.ACTIVE, 32), new Employee("Akalia", "Smart", "PREFERRED", "", EmployeeStatus.ACTIVE, 32), new Employee("Adisha", "Edwards", "PREFERRED", "", EmployeeStatus.ACTIVE, 32), new Employee("Akalia", "Smart", "PREFERRED", "", EmployeeStatus.ACTIVE, 32), new Employee("Adisha", "Edwards", "PREFERRED", "", EmployeeStatus.ACTIVE, 32),
                new Employee("Akalia", "Smart", "PREFERRED", "", EmployeeStatus.ACTIVE, 32), new Employee("Adisha", "Edwards", "PREFERRED", "", EmployeeStatus.ACTIVE, 32), new Employee("Akalia", "Smart", "PREFERRED", "", EmployeeStatus.ACTIVE, 32), new Employee("Adisha", "Edwards", "PREFERRED", "", EmployeeStatus.ACTIVE, 32), new Employee("Akalia", "Smart", "PREFERRED", "", EmployeeStatus.ACTIVE, 32), new Employee("Adisha", "Edwards", "PREFERRED", "", EmployeeStatus.ACTIVE, 32),
                new Employee("Akalia", "Smart", "PREFERRED", "", EmployeeStatus.ACTIVE, 32), new Employee("Adisha", "Edwards", "PREFERRED", "", EmployeeStatus.ACTIVE, 32), new Employee("Akalia", "Smart", "PREFERRED", "", EmployeeStatus.ACTIVE, 32), new Employee("Adisha", "Edwards", "PREFERRED", "", EmployeeStatus.ACTIVE, 32), new Employee("Akalia", "Smart", "PREFERRED", "", EmployeeStatus.ACTIVE, 32), new Employee("Adisha", "Edwards", "PREFERRED", "", EmployeeStatus.ACTIVE, 32),
                new Employee("Akalia", "Smart", "PREFERRED", "", EmployeeStatus.ACTIVE, 32), new Employee("Adisha", "Edwards", "PREFERRED", "", EmployeeStatus.ACTIVE, 32), new Employee("Akalia", "Smart", "PREFERRED", "", EmployeeStatus.ACTIVE, 32), new Employee("Adisha", "Edwards", "PREFERRED", "", EmployeeStatus.ACTIVE, 32), new Employee("Akalia", "Smart", "PREFERRED", "", EmployeeStatus.ACTIVE, 32), new Employee("Adisha", "Edwards", "PREFERRED", "", EmployeeStatus.ACTIVE, 32),
                new Employee("Akalia", "Smart", "PREFERRED", "", EmployeeStatus.ACTIVE, 32), new Employee("Adisha", "Edwards", "PREFERRED", "", EmployeeStatus.ACTIVE, 32), new Employee("Akalia", "Smart", "PREFERRED", "", EmployeeStatus.ACTIVE, 32), new Employee("Adisha", "Edwards", "PREFERRED", "", EmployeeStatus.ACTIVE, 32), new Employee("Akalia", "Smart", "PREFERRED", "", EmployeeStatus.ACTIVE, 32), new Employee("Adisha", "Edwards", "PREFERRED", "", EmployeeStatus.ACTIVE, 32),
                new Employee("Akalia", "Smart", "PREFERRED", "", EmployeeStatus.ACTIVE, 32), new Employee("Adisha", "Edwards", "PREFERRED", "", EmployeeStatus.ACTIVE, 32), new Employee("Akalia", "Smart", "PREFERRED", "", EmployeeStatus.ACTIVE, 32), new Employee("Adisha", "Edwards", "PREFERRED", "", EmployeeStatus.ACTIVE, 32), new Employee("Akalia", "Smart", "PREFERRED", "", EmployeeStatus.ACTIVE, 32), new Employee("Adisha", "Edwards", "PREFERRED", "", EmployeeStatus.ACTIVE, 32),
                new Employee("Akalia", "Smart", "PREFERRED", "", EmployeeStatus.ACTIVE, 32), new Employee("Adisha", "Edwards", "PREFERRED", "", EmployeeStatus.ACTIVE, 32), new Employee("Akalia", "Smart", "PREFERRED", "", EmployeeStatus.ACTIVE, 32), new Employee("Adisha", "Edwards", "PREFERRED", "", EmployeeStatus.ACTIVE, 32), new Employee("Akalia", "Smart", "PREFERRED", "", EmployeeStatus.ACTIVE, 32), new Employee("Adisha", "Edwards", "PREFERRED", "", EmployeeStatus.ACTIVE, 32),
                new Employee("Akalia", "Smart", "PREFERRED", "", EmployeeStatus.ACTIVE, 32), new Employee("Adisha", "Edwards", "PREFERRED", "", EmployeeStatus.ACTIVE, 32), new Employee("Akalia", "Smart", "PREFERRED", "", EmployeeStatus.ACTIVE, 32), new Employee("Adisha", "Edwards", "PREFERRED", "", EmployeeStatus.ACTIVE, 32), new Employee("Akalia", "Smart", "PREFERRED", "", EmployeeStatus.ACTIVE, 32), new Employee("Adisha", "Edwards", "PREFERRED", "", EmployeeStatus.ACTIVE, 32));
    }
}
