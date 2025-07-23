package code;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;
import org.xhtmlrenderer.pdf.ITextRenderer;

import com.lowagie.text.pdf.BaseFont;

import lombok.extern.slf4j.Slf4j;

/**
 * Modified code from business projects
 */
@Slf4j
@Component
public class FontTestRunner implements ApplicationRunner {

    @Override
    public void run(ApplicationArguments args) {

        boolean result = true;
        result = testCaptcha2() && result;
        result = testCaptcha() && result;
        result = testRegisterCustomFont() && result;
        result = testApachePoiWithFontAutoWidth() && result;
        result = testFlyingSaucer() && result;

        if (!result) {
//            throw new RuntimeException("Failed font test(s).");
        }
    }



    private boolean testFlyingSaucer() {

        final ClassPathResource resource = new ClassPathResource("sample.xhtml");
        try (InputStream inputStream = resource.getInputStream();
                OutputStream os = new FileOutputStream(new File("output.pdf"))) {
            final String xhtmlContent = StreamUtils.copyToString(inputStream, StandardCharsets.UTF_8);
            final ITextRenderer renderer = new ITextRenderer();
            renderer.getFontResolver().addFont("Branda-yolq.ttf", BaseFont.IDENTITY_H, true);
            renderer.setDocumentFromString(xhtmlContent);
            renderer.layout();

            renderer.createPDF(os);

            log.info("Generated PDF file.");

            return true;
        } catch (Exception e) {
            log.error("Failed to generate PDF with Flying Saucer.");

            return false;
        }
    }

    private boolean testCaptcha2() {

        final String captcha = RandomStringUtils.secure().nextAlphabetic(4).toUpperCase();

        try {
            CaptchaUtils2.generateCaptcha(captcha);
            log.info("Successfully generated captcha 2.");

            return true;
        } catch (Exception e) {
            log.error("Failed to generate captcha 2.", e);

            return false;
        }
    }

    private boolean testCaptcha() {
        try {
            CaptchaUtils.generateCaptcha("AAAAAA");

            return true;
        } catch (Exception e) {
            log.error("Failed to generate captcha.", e);

            return false;
        }
    }

    private boolean testRegisterCustomFont() {

        try {
            AwtFontHelper.loadFont("/font/Arial.ttf");

            return true;
        } catch (Exception e) {
            log.error("Failed to register custom font.", e);

            return false;
        }
    }

    private boolean testApachePoiWithFontAutoWidth() {

        // Doesn't seem to help in 5.3
        // Defaults to true in 5.4
//        System.setProperty("org.apache.poi.ss.ignoreMissingFontSystem", "true");

        try (final Workbook workbook = new XSSFWorkbook();) {
            final Sheet sheet = workbook.createSheet("ApplicationList");
            final CellStyle headerCellStyle = getCellStyleHeader(workbook);

            importDataExcel(sheet, headerCellStyle);

            try (final ByteArrayOutputStream os = new ByteArrayOutputStream()) {
                workbook.write(os);
            }

            log.info("Successfully processed Excel workbook with POI.");

            return true;
        } catch (Throwable e) {
            log.error("Failed to process Excel workbook with POI.", e);

            return false;
        }
    }

    private void importDataExcel(Sheet sheet, CellStyle headerCellStyle) {

        final String[] headers = { "foo", "bar", };

        // header row
        final Row headerRow = sheet.createRow(0);

        for (int i = 0; i < headers.length; i++) {
            addCellHeader(headers[i], headerCellStyle, headerRow, i);
        }

        for (int i = 0; i < headers.length; i++) {
            // XXX Missing Linux fontconfig package in base image can cause this line to throw exception.
            sheet.autoSizeColumn(i); // XXX This line requires font
        }
    }

    private CellStyle getCellStyleHeader(Workbook workbook) {

        final Font headerFont = workbook.createFont();
        headerFont.setFontName("Arial");
        headerFont.setBold(true);
        headerFont.setFontHeightInPoints((short) 14);
        headerFont.setColor(IndexedColors.BLACK.getIndex());

        final CellStyle headerCellStyle = workbook.createCellStyle();
        headerCellStyle.setFont(headerFont);

        return headerCellStyle;
    }

    private void addCellHeader(String cellValue, CellStyle cellStyle, Row row, int i) {
        final Cell headerCell = row.createCell(i);
        headerCell.setCellValue(cellValue);
        headerCell.setCellStyle(cellStyle);
    }
}