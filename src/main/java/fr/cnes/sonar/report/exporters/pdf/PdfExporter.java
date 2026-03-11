package fr.cnes.sonar.report.exporters.pdf;

import fr.cnes.sonar.report.exceptions.BadExportationDataTypeException;
import fr.cnes.sonar.report.exporters.IExporter;
import fr.cnes.sonar.report.model.Report;
import fr.opensagres.poi.xwpf.converter.pdf.PdfConverter;
import fr.opensagres.poi.xwpf.converter.pdf.PdfOptions;
import org.apache.poi.xwpf.usermodel.XWPFDocument;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Exporter for PDF reports
 */
public class PdfExporter implements IExporter {

    private static final Logger LOGGER = Logger.getLogger(PdfExporter.class.getName());

    @Override
    public File export(Object data, String path, String filename) throws BadExportationDataTypeException {
        // Since we are reading from an existing DOCX file, filename here will be the path to the DOCX file.
        // path is the destination PDF file.

        if (!(data instanceof Report)) {
            throw new BadExportationDataTypeException();
        }

        File docxFile = new File(filename);
        if (!docxFile.exists()) {
            LOGGER.log(Level.WARNING, "Unable to find DOCX file to convert to PDF: " + docxFile.getAbsolutePath());
            return null;
        }

        try (InputStream in = new FileInputStream(docxFile);
             OutputStream out = new FileOutputStream(new File(path))) {

            XWPFDocument document = new XWPFDocument(in);
            PdfOptions options = PdfOptions.create();
            PdfConverter.getInstance().convert(document, out, options);

        } catch (Throwable e) {
            LOGGER.log(Level.SEVERE, "Error while converting DOCX to PDF", e);
        }

        return new File(path);
    }
}
