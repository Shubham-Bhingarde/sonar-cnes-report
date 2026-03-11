package fr.cnes.sonar.report.exporters.pdf;

import fr.cnes.sonar.report.exceptions.BadExportationDataTypeException;
import fr.cnes.sonar.report.exporters.IExporter;
import fr.cnes.sonar.report.model.Report;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
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

        try {
            // Extract the bash script from resources to a temporary file
            File scriptFile = File.createTempFile("convert_pdf", ".sh");
            scriptFile.deleteOnExit();
            try (InputStream is = getClass().getResourceAsStream("/convert_pdf.sh");
                 FileOutputStream fos = new FileOutputStream(scriptFile)) {
                if (is == null) {
                    throw new RuntimeException("Could not find /convert_pdf.sh in resources");
                }
                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = is.read(buffer)) != -1) {
                    fos.write(buffer, 0, bytesRead);
                }
            }

            // Make the script executable
            scriptFile.setExecutable(true);

            // Execute the script
            File outputDir = new File(path).getParentFile();
            if (outputDir == null) {
                outputDir = new File(".");
            }

            ProcessBuilder pb = new ProcessBuilder(
                    scriptFile.getAbsolutePath(),
                    docxFile.getAbsolutePath(),
                    outputDir.getAbsolutePath()
            );

            Process process = pb.start();
            int exitCode = process.waitFor();

            if (exitCode != 0) {
                LOGGER.log(Level.SEVERE, "LibreOffice conversion script failed with exit code: " + exitCode);
                return null;
            }

            // LibreOffice creates a file with the same name as docxFile but with .pdf extension
            String docxName = docxFile.getName();
            String generatedPdfName = docxName.substring(0, docxName.lastIndexOf('.')) + ".pdf";
            File generatedPdf = new File(outputDir, generatedPdfName);

            // Rename/Move to the target path if different
            File targetPdf = new File(path);
            if (!generatedPdf.getAbsolutePath().equals(targetPdf.getAbsolutePath())) {
                Files.move(generatedPdf.toPath(), targetPdf.toPath(), StandardCopyOption.REPLACE_EXISTING);
            }

            return targetPdf;

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error while converting DOCX to PDF using LibreOffice", e);
            return null;
        }
    }
}
