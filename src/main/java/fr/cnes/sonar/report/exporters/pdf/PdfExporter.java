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
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.PosixFilePermission;
import java.util.HashSet;
import java.util.Set;
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

        File outputPdf = new File(path);
        File tempScript = null;

        try {
            // Extract the bash script from resources
            InputStream scriptStream = getClass().getResourceAsStream("/docx_to_pdf.sh");
            if (scriptStream != null) {
                tempScript = File.createTempFile("docx_to_pdf", ".sh");
                Files.copy(scriptStream, tempScript.toPath(), StandardCopyOption.REPLACE_EXISTING);

                // Make it executable
                Set<PosixFilePermission> perms = new HashSet<>();
                perms.add(PosixFilePermission.OWNER_READ);
                perms.add(PosixFilePermission.OWNER_WRITE);
                perms.add(PosixFilePermission.OWNER_EXECUTE);
                Files.setPosixFilePermissions(tempScript.toPath(), perms);

                // Execute the script
                ProcessBuilder pb = new ProcessBuilder(
                        tempScript.getAbsolutePath(),
                        docxFile.getAbsolutePath(),
                        outputPdf.getParentFile().getAbsolutePath()
                );

                Process process = pb.start();
                int exitCode = process.waitFor();

                if (exitCode != 0) {
                    LOGGER.log(Level.WARNING, "Bash script docx_to_pdf.sh failed with exit code: " + exitCode + ". Falling back to POI converter.");
                    fallbackToPoiConverter(docxFile, outputPdf);
                } else {
                    // LibreOffice output name is the same as docx file but with .pdf extension
                    String libreOfficeOutputName = docxFile.getName().substring(0, docxFile.getName().lastIndexOf('.')) + ".pdf";
                    File libreOfficeOutputFile = new File(outputPdf.getParentFile(), libreOfficeOutputName);
                    if (libreOfficeOutputFile.exists() && !libreOfficeOutputFile.getAbsolutePath().equals(outputPdf.getAbsolutePath())) {
                        Files.move(libreOfficeOutputFile.toPath(), outputPdf.toPath(), StandardCopyOption.REPLACE_EXISTING);
                    }
                }
            } else {
                LOGGER.log(Level.WARNING, "Could not find docx_to_pdf.sh in resources. Falling back to POI converter.");
                fallbackToPoiConverter(docxFile, outputPdf);
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error while executing DOCX to PDF script. Falling back to POI converter.", e);
            fallbackToPoiConverter(docxFile, outputPdf);
        } finally {
            if (tempScript != null && tempScript.exists()) {
                tempScript.delete();
            }
        }

        return outputPdf;
    }

    private void fallbackToPoiConverter(File docxFile, File outputPdf) {
        try (InputStream in = new FileInputStream(docxFile);
             OutputStream out = new FileOutputStream(outputPdf)) {

            XWPFDocument document = new XWPFDocument(in);
            PdfOptions options = PdfOptions.create();
            PdfConverter.getInstance().convert(document, out, options);

        } catch (Throwable e) {
            LOGGER.log(Level.SEVERE, "Error while converting DOCX to PDF using POI", e);
        }
    }
}
