package code;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

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
import org.w3c.dom.Document;
import org.xhtmlrenderer.pdf.CJKFontResolver;
import org.xhtmlrenderer.pdf.ITextOutputDevice;
import org.xhtmlrenderer.pdf.ITextRenderer;
import org.xhtmlrenderer.pdf.ITextUserAgent;
import org.xhtmlrenderer.resource.XMLResource;

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
        result = testFlyingSaucer("sample-with-font.xhtml", "output-with-font.pdf") && result;
        result = testFlyingSaucer("sample.xhtml", "output.pdf") && result;

        if (!result) {
//            throw new RuntimeException("Failed font test(s).");
        }
    }



    private boolean testFlyingSaucer(String xhtmlFileName, String outputFileName) {

        try (InputStream is = new ClassPathResource(xhtmlFileName).getInputStream();
                OutputStream os = new FileOutputStream(new File(outputFileName))) {

            doTestFlyingSaucer(is, os);

            log.info("PDF - Successfully generated PDF file - {} to {}", xhtmlFileName, outputFileName);

            return true;
        } catch (Exception e) {
            log.error("PDF - Failed to generate PDF with Flying Saucer.");

            return false;
        }
    }

    private void doTestFlyingSaucer(InputStream inputStream, OutputStream outputStream) throws Exception {

        final Document doc = XMLResource.load(inputStream).getDocument();
        final CJKFontResolver fontResolver = new CJKFontResolver();
        fontResolver.addFont(new ClassPathResource("font/Branda-yolq.ttf").getPath(), BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
        final ITextRenderer renderer = new ITextRenderer(4.1666f, 2, fontResolver);
        final ResourceLoaderUserAgent callback = new ResourceLoaderUserAgent(renderer.getOutputDevice(), renderer.getSharedContext().getDotsPerPixel());
        renderer.getSharedContext().setUserAgentCallback(callback);
        renderer.createPDF(doc, outputStream);
    }

    private static class ResourceLoaderUserAgent extends ITextUserAgent {
        private ResourceLoaderUserAgent(ITextOutputDevice outputDevice, int dotsPerPixel) {
            super(outputDevice, dotsPerPixel);
        }

    }

    private boolean testCaptcha2() {

        final String captcha = RandomStringUtils.secure().nextAlphabetic(4).toUpperCase();

        try {
            CaptchaUtils2.generateCaptcha(captcha);
            log.info("CAPTCHA - Successfully generated captcha 2.");

            return true;
        } catch (Exception e) {
            log.error("CAPTCHA - Failed to generate captcha 2.", e);

            return false;
        }
    }

    private boolean testCaptcha() {
        try {
            CaptchaUtils.generateCaptcha("AAAAAA");
            log.info("CAPTCHA - Successfully generated captcha.");

            return true;
        } catch (Exception e) {
            log.error("CAPTCHA - Failed to generate captcha.", e);

            return false;
        }
    }

    private boolean testRegisterCustomFont() {

        try {
            AwtFontHelper.loadFont("/font/Arial.ttf");
            log.info("Font.createFont - Successfully registered custom font.");

            return true;
        } catch (Exception e) {
            log.error("Font.createFont - Failed to register custom font.", e);

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

            try (final OutputStream os = new FileOutputStream(new File("output.xlsx"))) {
                workbook.write(os);
            }

            log.info("POI - Successfully processed Excel workbook with POI.");

            return true;
        } catch (Throwable e) {
            log.error("POI - Failed to process Excel workbook with POI.", e);

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
        headerFont.setFontName("Forte");
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