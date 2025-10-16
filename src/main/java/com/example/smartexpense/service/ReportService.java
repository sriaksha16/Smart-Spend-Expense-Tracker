package com.example.smartexpense.service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import com.example.smartexpense.model.Expense;
import com.example.smartexpense.repo.ExpenseRepo;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.Element;
import com.itextpdf.text.FontFactory;     
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;


@Service
public class ReportService {

    private final ExpenseRepo expenseRepo;

    public ReportService(ExpenseRepo expenseRepo) {
        this.expenseRepo = expenseRepo;
    }

    public Map<String, Double> getCategoryBreakdown(Long userId, int year, int month) {
        List<Object[]> results = expenseRepo.getCategoryTotalsDetailed(userId, year, month);
        Map<String, Double> data = new HashMap<>();

        for (Object[] row : results) {
            String category = (String) row[0];
            String title = (String) row[1];
            String type = (String) row[2];
            Double total = ((Number) row[3]).doubleValue();

            String key = category + " - " + title + " (" + type + ")";
            data.put(key, total);
        }
        return data;
    }
    
	/*
	 * // ✅ PDF Export using iText public ByteArrayInputStream
	 * generatePdf(List<Expense> expenses, String fullName) { Document document =
	 * new Document(); ByteArrayOutputStream out = new ByteArrayOutputStream();
	 * 
	 * try { PdfWriter.getInstance(document, out); document.open();
	 * 
	 * // Title Font font = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16);
	 * Paragraph title = new Paragraph("My Expenses Report", font);
	 * title.setAlignment(Element.ALIGN_CENTER); document.add(title);
	 * document.add(Chunk.NEWLINE);
	 * 
	 * // 👇 Username Font userFont = FontFactory.getFont(FontFactory.HELVETICA,
	 * 12); Paragraph username = new Paragraph("User: " + fullName, userFont);
	 * username.setAlignment(Element.ALIGN_LEFT); document.add(username);
	 * 
	 * // 👇 Generated Date Paragraph date = new Paragraph("Generated on: " + new
	 * java.util.Date(), userFont); date.setAlignment(Element.ALIGN_LEFT);
	 * document.add(date);
	 * 
	 * document.add(Chunk.NEWLINE);
	 * 
	 * // Table with 6 columns PdfPTable table = new PdfPTable(6);
	 * table.setWidthPercentage(100);
	 * 
	 * // Header row table.addCell("Title"); table.addCell("Amount");
	 * table.addCell("Category"); table.addCell("Type"); table.addCell("Date");
	 * table.addCell("Description");
	 * 
	 * // Add expense rows for (Expense e : expenses) { table.addCell(e.getTitle());
	 * table.addCell(String.valueOf(e.getAmount())); table.addCell(e.getCategory());
	 * table.addCell(e.getType()); table.addCell(String.valueOf(e.getDate()));
	 * table.addCell(e.getDescription()); }
	 * 
	 * document.add(table); document.close(); } catch (Exception e) {
	 * e.printStackTrace(); } return new ByteArrayInputStream(out.toByteArray()); }
	 * 
	 */
    // ✅ PDF Export using iText
    public ByteArrayInputStream generatePdf(List<Expense> expenses, String fullName) {
        Document document = new Document();
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try {
            PdfWriter.getInstance(document, out);
            document.open();

            // Title
            com.itextpdf.text.Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16);
            Paragraph title = new Paragraph("My Expenses Report", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);
            document.add(Chunk.NEWLINE);

            // Username
            com.itextpdf.text.Font userFont = FontFactory.getFont(FontFactory.HELVETICA, 12);
            Paragraph username = new Paragraph("User: " + fullName, userFont);
            username.setAlignment(Element.ALIGN_LEFT);
            document.add(username);

            // Generated Date
            Paragraph date = new Paragraph("Generated on: " + new java.util.Date(), userFont);
            date.setAlignment(Element.ALIGN_LEFT);
            document.add(date);

            document.add(Chunk.NEWLINE);

            // Table with 6 columns
            PdfPTable table = new PdfPTable(6);
            table.setWidthPercentage(100);

            // Header row
            table.addCell("Title");
            table.addCell("Amount");
            table.addCell("Category");
            table.addCell("Type");
            table.addCell("Date");
            table.addCell("Description");

            // Expense rows
            for (Expense e : expenses) {
                table.addCell(e.getTitle());
                table.addCell(String.valueOf(e.getAmount()));
                table.addCell(e.getCategory());
                table.addCell(e.getType());
                table.addCell(String.valueOf(e.getDate()));
                table.addCell(e.getDescription());
            }

            document.add(table);
            document.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ByteArrayInputStream(out.toByteArray());
    }

    
    
 // Excel Export using Apache POI
    public ByteArrayInputStream generateExcel(List<Expense> expenses, String fullName) {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Expenses");

            // Bold header style
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);

            int rowIdx = 0;

            // Title Row
            Row titleRow = sheet.createRow(rowIdx++);
            Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue("My Expenses Report");
            CellStyle titleStyle = workbook.createCellStyle();
            Font titleFont = workbook.createFont();
            titleFont.setBold(true);
            titleFont.setFontHeightInPoints((short) 14);
            titleStyle.setFont(titleFont);
            titleCell.setCellStyle(titleStyle);

            // User Row
            Row userRow = sheet.createRow(rowIdx++);
            userRow.createCell(0).setCellValue("User: " + fullName);

            // Date Row
            Row dateRow = sheet.createRow(rowIdx++);
            dateRow.createCell(0).setCellValue("Generated on: " + new java.util.Date());

            rowIdx++; // empty row before table

            // Header Row
            Row headerRow = sheet.createRow(rowIdx++);
            String[] headers = {"Title", "Amount", "Category", "Type", "Date", "Description"};

            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // Expense Rows
            for (Expense e : expenses) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(e.getTitle());
                row.createCell(1).setCellValue(e.getAmount());
                row.createCell(2).setCellValue(e.getCategory());
                row.createCell(3).setCellValue(e.getType());
                row.createCell(4).setCellValue(e.getDate().toString());
                row.createCell(5).setCellValue(e.getDescription());
            }

            // Auto-size columns
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            // Write to stream
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);
            return new ByteArrayInputStream(out.toByteArray());
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }


}
