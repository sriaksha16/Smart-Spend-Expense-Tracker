package com.example.smartexpense.controller;
import java.io.ByteArrayInputStream;      // For holding PDF/Excel data
import java.security.Principal;           // To get logged-in user
import java.time.LocalDate;
import java.util.List;                    // For collections
import org.springframework.beans.factory.annotation.Autowired; // Dependency injection
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;                   // To set file download headers
import org.springframework.http.MediaType;                     // To set content type (PDF/Excel)
import org.springframework.http.ResponseEntity;                // To return proper HTTP response
import org.springframework.stereotype.Controller;              // Marks this as a Spring MVC Controller
import org.springframework.ui.Model;                           // To pass data to Thymeleaf template
import org.springframework.web.bind.annotation.GetMapping;     // For GET endpoints
import org.springframework.web.bind.annotation.ModelAttribute; // For form binding
import org.springframework.web.bind.annotation.PathVariable;   // For dynamic URL params
import org.springframework.web.bind.annotation.PostMapping;    // For POST endpoints
import org.springframework.web.bind.annotation.RequestMapping; // Base path mapping
import org.springframework.web.bind.annotation.RequestParam;
import com.example.smartexpense.model.Expense;
import com.example.smartexpense.model.UserReg;
import com.example.smartexpense.service.ExpenseService;
import com.example.smartexpense.service.ReportService;
import com.example.smartexpense.service.UserService;

@Controller
@RequestMapping("/expenses") // 👈 base path for all mappings in this controller
public class ExpenseController {

	@Autowired
	private ExpenseService expenseService;

	@Autowired
	private UserService userService; // to get logged-in user

	@Autowired
	private ReportService reportService;


	// Display all expenses of logged-in user
	@GetMapping("/expenseshome")
	public String listExpenses(Model model, Principal principal) {

		// principal.getName() gives email
		UserReg user = userService.findByEmail(principal.getName()); // logged-in user
		
		  // Add BOTH the user object and some extra attributes
	    model.addAttribute("user", user);  // <-- Add this line


		// Add the actual full name to model
		model.addAttribute("currentuser", user.getFullName());

		model.addAttribute("expenses", expenseService.getAllExpenses(user));
		model.addAttribute("totalIncome", expenseService.getTotalIncome(user));
		model.addAttribute("totalExpenses", expenseService.getTotalExpenses(user));
		model.addAttribute("balance", expenseService.getBalance(user));
		model.addAttribute("transactionCount", expenseService.getTransactionCount(user));

		return "expenses"; // -> expenses.html
	}

	// Show form
	@GetMapping("/add/new")
	public String showExpenseForm(Model model) {
		model.addAttribute("expense", new Expense());
		return "expenseeditform"; // -> expense_form.html
	}

	// Save expense or updated expense
	@PostMapping("/save")
	public String saveExpense(@ModelAttribute Expense expense, Principal principal) {
		UserReg user = userService.findByEmail(principal.getName());
		expense.setUser(user); // assign expense to logged-in user

		expenseService.saveExpense(expense);
		return "redirect:/expenses/expenseshome";
	}

	// Show update form
	@GetMapping("/edit/{id}")
	public String editExpense(@PathVariable Long id, Model model) {
		Expense expense = expenseService.getExpenseById(id);
		model.addAttribute("expense", expense);
		return "expenseeditform"; // separate form page
	}

	// Delete expense
	@GetMapping("/delete/{id}")
	public String deleteExpense(@PathVariable Long id) {
		expenseService.deleteExpense(id);
		return "redirect:/expenses/expenseshome";
	}

	@GetMapping("/reports")
	public String showReports(Model model, Principal principal) {

		// Get current user from Principal
		UserReg user = userService.findByEmail(principal.getName());
		Long userId = user.getId(); // Now you have the logged-in userId

		System.out.println("here current user id is:" + userId);

		// Example data (replace with actual queries)
		model.addAttribute("categories", List.of("Food", "Bills", "Transport", "Entertainment"));
		model.addAttribute("categoryTotals", List.of(300, 450, 150, 200));

		model.addAttribute("totalIncome", 3000);
		model.addAttribute("totalExpenses", 1100);

		model.addAttribute("months", List.of("Jan", "Feb", "Mar", "Apr"));
		model.addAttribute("monthlyExpenses", List.of(400, 250, 300, 150));

		return "reports";
	}

	@GetMapping("/export/pdf")
	public ResponseEntity<byte[]> exportPdf(Principal principal) throws Exception {
	    // 👇 Fetch logged in user
	    UserReg user = userService.findByEmail(principal.getName());
	    List<Expense> expenses = expenseService.getAllExpenses(user);

	    // 👇 Pass both expenses AND user full name
	    ByteArrayInputStream bis = reportService.generatePdf(expenses, user.getFullName());

	    HttpHeaders headers = new HttpHeaders();
	    headers.add("Content-Disposition", "attachment; filename=expenses_" + user.getFullName() + ".pdf");

	    return ResponseEntity.ok()
	            .headers(headers)
	            .contentType(MediaType.APPLICATION_PDF)
	            .body(bis.readAllBytes());
	}


	@GetMapping("/export/excel")
	public ResponseEntity<byte[]> exportExcel(Principal principal) throws Exception {
	    UserReg user = userService.findByEmail(principal.getName());
	    List<Expense> expenses = expenseService.getAllExpenses(user);

	    ByteArrayInputStream bis = reportService.generateExcel(expenses, user.getFullName());

	    HttpHeaders headers = new HttpHeaders();
	    headers.add("Content-Disposition", "attachment; filename=expenses_" + user.getFullName() + ".xlsx");

	    return ResponseEntity.ok()
	            .headers(headers)
	            .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
	            .body(bis.readAllBytes());
	}

	@GetMapping("/filter")
	public String filterExpenses(
	        @RequestParam(required = false) String category,
	        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
	        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end,
	        @RequestParam(required = false) Double min,
	        @RequestParam(required = false) Double max,
	        Principal principal,
	        Model model) {

	    UserReg user = userService.findByEmail(principal.getName()); // logged-in user
	    List<Expense> expenses = expenseService.filterExpenses(user, category, start, end, min, max);

	    model.addAttribute("expenses", expenses);
	    model.addAttribute("currentuser", user.getFullName());
	    model.addAttribute("user", user);   // <-- add this


	    return "expenses"; // thymeleaf page
	}



}
