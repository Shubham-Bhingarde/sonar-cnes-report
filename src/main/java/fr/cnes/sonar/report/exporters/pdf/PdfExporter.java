/*
 * This file is part of cnesreport.
 *
 * cnesreport is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * cnesreport is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with cnesreport.  If not, see <http://www.gnu.org/licenses/>.
 */

package fr.cnes.sonar.report.exporters.pdf;

import fr.cnes.sonar.report.exceptions.BadExportationDataTypeException;
import fr.cnes.sonar.report.exporters.IExporter;
import fr.cnes.sonar.report.exporters.docx.DocXExporter;
import fr.cnes.sonar.report.model.Report;
import fr.cnes.sonar.report.utils.StringManager;
import fr.opensagres.poi.xwpf.converter.pdf.PdfConverter;
import fr.opensagres.poi.xwpf.converter.pdf.PdfOptions;
import org.apache.poi.openxml4j.exceptions.OpenXML4JException;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.xmlbeans.XmlException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;

/**
 * Exports the report in .pdf format
 */
public class PdfExporter implements IExporter {

    private static final String DEFAULT_PDF_TEMPLATE = "pdf.template";

    @Override
    public File export(Object data, String path, String filename) throws BadExportationDataTypeException, IOException, OpenXML4JException, XmlException {
        if (!(data instanceof Report)) {
            throw new BadExportationDataTypeException();
        }

        // Generate temporary docx file
        File tempDocxFile = File.createTempFile("temp-report", ".docx");
        tempDocxFile.deleteOnExit();

        DocXExporter docXExporter = new DocXExporter();

        // Let DocXExporter use the specific PDF template
        String pdfTemplate = filename != null && !filename.isEmpty() ? filename : "";
        if (pdfTemplate.isEmpty()) {
            // Need to get absolute path to template to force docx exporter to use it,
            // or we could write a temporary file from the stream and use that as 'filename'.
            File tempTemplateFile = File.createTempFile("pdf-template", ".docx");
            tempTemplateFile.deleteOnExit();
            try (InputStream in = getClass().getResourceAsStream(StringManager.getProperty(DEFAULT_PDF_TEMPLATE));
                 OutputStream out = new FileOutputStream(tempTemplateFile)) {
                if (in != null) {
                    in.transferTo(out);
                    pdfTemplate = tempTemplateFile.getAbsolutePath();
                }
            }
        }

        docXExporter.export(data, tempDocxFile.getAbsolutePath(), pdfTemplate);

        // Convert docx to pdf securely
        try (InputStream docxInputStream = new FileInputStream(tempDocxFile);
             XWPFDocument document = new XWPFDocument(docxInputStream);
             OutputStream out = new FileOutputStream(path)) {

            PdfOptions options = PdfOptions.create();
            PdfConverter.getInstance().convert(document, out, options);
        }

        return new File(path);
    }
}
