#!/bin/bash

# Update CommandLineManager.java
sed -i 's/{"m", "disable-markdown", Boolean.FALSE.toString(), "Disable Markdown generation"},/{"m", "disable-markdown", Boolean.FALSE.toString(), "Disable Markdown generation"},\n            {"z", "disable-pdf", Boolean.FALSE.toString(), "Disable PDF generation"},/' src/main/java/fr/cnes/sonar/report/utils/CommandLineManager.java

# Update ReportConfiguration.java
sed -i 's/\/\*\* Options for m. \*\//\/\*\* Options for m. \*\//' src/main/java/fr/cnes/sonar/report/utils/ReportConfiguration.java
sed -i 's/private boolean enableMarkdown;/private boolean enableMarkdown;\n    \/\*\* Options for z. \*\/\n    private boolean enablePdf;/' src/main/java/fr/cnes/sonar/report/utils/ReportConfiguration.java
sed -i 's/final boolean enableMarkdown, String templateReport,/final boolean enableMarkdown, final boolean enablePdf, String templateReport,/' src/main/java/fr/cnes/sonar/report/utils/ReportConfiguration.java
sed -i 's/this.enableMarkdown = enableMarkdown;/this.enableMarkdown = enableMarkdown;\n        this.enablePdf = enablePdf;/' src/main/java/fr/cnes/sonar/report/utils/ReportConfiguration.java
sed -i 's/!commandLineManager.hasOption("m"),/!commandLineManager.hasOption("m"),\n                !commandLineManager.hasOption("z"),/' src/main/java/fr/cnes/sonar/report/utils/ReportConfiguration.java
sed -i 's/public boolean isEnableMarkdown(){ return enableMarkdown; }/public boolean isEnableMarkdown(){ return enableMarkdown; }\n\n    public boolean isEnablePdf(){ return enablePdf; }/' src/main/java/fr/cnes/sonar/report/utils/ReportConfiguration.java

# Update ReportFactory.java
sed -i 's/import fr.cnes.sonar.report.exporters.docx.DocXExporter;/import fr.cnes.sonar.report.exporters.docx.DocXExporter;\nimport fr.cnes.sonar.report.exporters.pdf.PdfExporter;/' src/main/java/fr/cnes/sonar/report/factory/ReportFactory.java
sed -i 's/private static final String CSV_FILENAME = "csv.output";/private static final String CSV_FILENAME = "csv.output";\n    \/\*\* Property for the PDF report filename. \*\/\n    private static final String PDF_FILENAME = "pdf.output";/' src/main/java/fr/cnes/sonar/report/factory/ReportFactory.java
sed -i 's/final CSVExporter csvExporter = new CSVExporter();/final CSVExporter csvExporter = new CSVExporter();\n        final PdfExporter pdfExporter = new PdfExporter();/' src/main/java/fr/cnes/sonar/report/factory/ReportFactory.java
sed -i 's/\/\/ Export issues in spreadsheet if requested./\/\/ Export in PDF if requested\n        if(configuration.isEnablePdf()) {\n            final String pdfFilename = formatFilename(PDF_FILENAME, configuration.getOutput(), configuration.getDate(), model.getProjectName());\n            pdfExporter.export(model, pdfFilename, configuration.getTemplateReport());\n        }\n\n        \/\/ Export issues in spreadsheet if requested./' src/main/java/fr/cnes/sonar/report/factory/ReportFactory.java

# Update plugin.properties
sed -i 's/api.report.args.enableCsv=enableCsv/api.report.args.enableCsv=enableCsv\napi.report.args.enablePdf=enablePdf/' src/main/resources/plugin.properties
sed -i 's/api.report.args.description.enableCsv=Enable csv generation/api.report.args.description.enableCsv=Enable csv generation\napi.report.args.description.enablePdf=Enable pdf generation/' src/main/resources/plugin.properties
sed -i 's/api.report.args.defaultValue.enableCsv=true/api.report.args.defaultValue.enableCsv=true\napi.report.args.defaultValue.enablePdf=true/' src/main/resources/plugin.properties

# Update report.properties
sed -i 's/csv.output=BASEDIR\/DATE-NAME-issues-report.csv/csv.output=BASEDIR\/DATE-NAME-issues-report.csv\n#Report filename'"'"'s pattern in PDF, DATE and NAME are placeholders\npdf.output=BASEDIR\/DATE-NAME-analysis-report.pdf/' src/main/resources/report.properties
sed -i 's/docx.template=\/template\/code-analysis-template.docx/docx.template=\/template\/code-analysis-template.docx\n#Name of the default template for pdf\npdf.template=\/template\/code-analysis-template-pdf.docx/' src/main/resources/report.properties

# Update ReportWs.java
sed -i 's/\/\/ Adding enableConf argument/\/\/ Adding enablePdf argument\n        WebService.NewParam enablePdfParam = report.createParam(PluginStringManager.getProperty("api.report.args.enablePdf"));\n        enablePdfParam.setDescription(PluginStringManager.getProperty("api.report.args.description.enablePdf"));\n        enablePdfParam.setRequired(false);\n        enablePdfParam.setBooleanPossibleValues();\n        enablePdfParam.setDefaultValue(PluginStringManager.getProperty("api.report.args.defaultValue.enablePdf"));\n\n        \/\/ Adding enableConf argument/' src/main/java/fr/cnes/sonar/plugin/ws/ReportWs.java

# Update ExportTask.java
sed -i 's/final Request.StringParam pEnableCsv =/final Request.StringParam pEnablePdf =\n                    request.getParam(PluginStringManager.getProperty("api.report.args.enablePdf"));\n\n            final Request.StringParam pEnableCsv =/' src/main/java/fr/cnes/sonar/plugin/ws/ExportTask.java
sed -i 's/String pEnableCsvValue = pEnableCsv.getValue();/String pEnableCsvValue = pEnableCsv.getValue();\n            String pEnablePdfValue = pEnablePdf.getValue();/' src/main/java/fr/cnes/sonar/plugin/ws/ExportTask.java
sed -i '/if(pEnableCsvValue != null && (pEnableCsvValue.equals(FALSE) || pEnableCsvValue.equals(NO))) {/,/}/a \
            if(pEnablePdfValue != null && (pEnablePdfValue.equals(FALSE) || pEnablePdfValue.equals(NO))) {\n                reportParams.add("-z");\n            }' src/main/java/fr/cnes/sonar/plugin/ws/ExportTask.java
