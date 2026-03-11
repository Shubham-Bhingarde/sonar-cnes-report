package fr.cnes.sonar.report.exporters.pdf;

import fr.cnes.sonar.report.exceptions.BadExportationDataTypeException;
import fr.cnes.sonar.report.exporters.IExporter;
import org.apache.poi.openxml4j.exceptions.OpenXML4JException;
import org.apache.xmlbeans.XmlException;
import org.docx4j.Docx4J;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class PdfExporter implements IExporter {

    @Override
    public File export(Object data, String path, String filename) throws BadExportationDataTypeException, IOException,
            OpenXML4JException, XmlException {
        if (!(data instanceof String docxPath)) {
            throw new BadExportationDataTypeException("Bad data type for pdf export");
        }

        final WordprocessingMLPackage wordMLPackage = WordprocessingMLPackage.load(new File(docxPath));
        final File pdfFile = new File(path);
        try (FileOutputStream outputStream = new FileOutputStream(pdfFile)) {
            Docx4J.toPDF(wordMLPackage, outputStream);
        } catch (Exception e) {
            throw new IOException("Unable to convert DOCX to PDF", e);
        }

        return pdfFile;
    }
}
