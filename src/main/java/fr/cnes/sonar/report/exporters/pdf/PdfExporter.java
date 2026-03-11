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
        File tempProfileDir = null;

        try {
            // Create a unique temporary directory for the LibreOffice user profile to prevent lock file issues
            tempProfileDir = Files.createTempDirectory("libreoffice_profile_").toFile();

            // Execute libreoffice command directly
            ProcessBuilder pb = new ProcessBuilder(
                    "libreoffice",
                    "-env:UserInstallation=file://" + tempProfileDir.getAbsolutePath(),
                    "--headless",
                    "--convert-to", "pdf",
                    "--outdir", outputPdf.getParentFile().getAbsolutePath(),
                    docxFile.getAbsolutePath()
            );

            pb.redirectErrorStream(true); // Combine stdout and stderr
            Process process = pb.start();

            // Read output to prevent process blocking and to log any errors
            StringBuilder output = new StringBuilder();
            try (java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line).append(System.lineSeparator());
                }
            }

            int exitCode = process.waitFor();

            if (exitCode != 0) {
                LOGGER.log(Level.WARNING, "LibreOffice command failed with exit code: " + exitCode + ". Output:\n" + output.toString() + "\nFalling back to POI converter.");
                fallbackToPoiConverter(docxFile, outputPdf);
            } else {
                // LibreOffice output name is the same as docx file but with .pdf extension
                String libreOfficeOutputName = docxFile.getName().substring(0, docxFile.getName().lastIndexOf('.')) + ".pdf";
                File libreOfficeOutputFile = new File(outputPdf.getParentFile(), libreOfficeOutputName);
                if (libreOfficeOutputFile.exists() && !libreOfficeOutputFile.getAbsolutePath().equals(outputPdf.getAbsolutePath())) {
                    Files.move(libreOfficeOutputFile.toPath(), outputPdf.toPath(), StandardCopyOption.REPLACE_EXISTING);
                }
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error while executing LibreOffice command. Falling back to POI converter.", e);
            fallbackToPoiConverter(docxFile, outputPdf);
        } finally {
            if (tempProfileDir != null && tempProfileDir.exists()) {
                deleteDirectoryRecursively(tempProfileDir);
            }
        }

        return outputPdf;
    }

    private void deleteDirectoryRecursively(File directoryToBeDeleted) {
        File[] allContents = directoryToBeDeleted.listFiles();
        if (allContents != null) {
            for (File file : allContents) {
                deleteDirectoryRecursively(file);
            }
        }
        directoryToBeDeleted.delete();
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
