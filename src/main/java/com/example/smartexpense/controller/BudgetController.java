package com.example.smartexpense.controller;

import java.security.Principal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import java.util.Map;
import java.util.HashMap;

import com.example.smartexpense.model.Budget;
import com.example.smartexpense.model.UserReg;
import com.example.smartexpense.repo.BudgetRepo;
import com.example.smartexpense.service.BudgetService;
import com.example.smartexpense.service.UserService;

@Controller
@RequestMapping("/budgets")
public class BudgetController {

    @Autowired
    private BudgetService budgetService;

    @Autowired
    private UserService userService;

    @Autowired
    private BudgetRepo budgetRepository;

    
    @GetMapping("/new")
    public String showBudgetForm(Model model) {


    	  model.addAttribute("budget", new Budget());

    	    model.addAttribute("categories", List.of(
    	        "Food", "Bills", "Transport", "Entertainment", 
    	        "Groceries", "Health", "Shopping", "Education", 
    	        "Savings", "Rent", "Clothing", "Other"
    	    ));
    	
        return "budget_form";
    }

    @PostMapping("/save")
    public String saveBudget(@ModelAttribute Budget budget, Principal principal) {
        UserReg user = userService.findByEmail(principal.getName());
        budget.setUser(user);
        budgetService.saveBudget(budget);
        return "redirect:/budgets/view";
    }

    @GetMapping("/view")
    public String viewBudgets(Model model, Principal principal) {
        UserReg user = userService.findByEmail(principal.getName());
        int month = LocalDate.now().getMonthValue();
        int year = LocalDate.now().getYear();
        
        // 1️⃣ Fetch budgets
        List<Budget> budgets = budgetService.getBudgetsForMonth(user, month, year);

        // 2️⃣ Fetch total expenses for each category (optional: write a service method)
        double totalSpent = budgets.stream()
                .mapToDouble(b -> budgetService.getSpentForCategory(user, b.getCategory(), month, year))
                .sum();

        // 3️⃣ Calculate stats
        double totalBudget = budgets.stream().mapToDouble(Budget::getAmount).sum();
        double remaining = totalBudget - totalSpent;
        int totalCategories = budgets.size();

        // 4️⃣ Add to model
        model.addAttribute("budgets", budgets);
        model.addAttribute("totalBudget", totalBudget);
        model.addAttribute("totalSpent", totalSpent);
        model.addAttribute("remaining", remaining);
        model.addAttribute("totalCategories", totalCategories);

     

        model.addAttribute("budgets", budgetService.getBudgetsForMonth(user, month, year));
        return "budget_list";
    }
    
    @GetMapping("/budgets")
    public String viewRemainings(Model model, Principal principal) {
        UserReg user = userService.findByEmail(principal.getName());

        List<Budget> budgets = budgetRepository.findByUser(user);

        // Attach spent + remaining
        List<Map<String, Object>> budgetData = budgets.stream()
            .map(b -> {
                double spent = budgetService.getSpentForCategory(
                        user,
                        b.getCategory(),
                        b.getMonth(),
                        b.getYear()
                );
                double remaining = b.getAmount() - spent;

                Map<String, Object> map = new HashMap<>();
                map.put("category", b.getCategory());
                map.put("amount", b.getAmount());
                map.put("month", b.getMonth());
                map.put("year", b.getYear());
                map.put("spent", spent);
                map.put("remaining", remaining);
                return map;
            })
            .collect(Collectors.toList());

        // Add attributes for Thymeleaf
        model.addAttribute("budgets", budgetData);
        model.addAttribute("totalBudget", budgets.stream().mapToDouble(Budget::getAmount).sum());
        model.addAttribute("totalCategories", budgets.size());
        model.addAttribute("remaining", budgetData.stream()
                .mapToDouble(b -> (double) b.get("remaining"))
                .sum());

        return "budget_list"; // -> points to budgets.html
    }

}
