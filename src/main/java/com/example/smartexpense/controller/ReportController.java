package com.example.smartexpense.controller;

import java.security.Principal;
import java.time.LocalDate;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.example.smartexpense.service.ReportService;
import com.example.smartexpense.service.UserService;

@Controller
@RequestMapping("/reports")
public class ReportController {

    @Autowired
    private ReportService reportService;
    
    @Autowired
    private UserService userService;  // <-- autowired service

    @GetMapping("/pie/{userId}/{year}/{month}")
    @ResponseBody
    public Map<String, Double> getPieChartData(@PathVariable Long userId,
                                               @PathVariable int year,
                                               @PathVariable int month) {
        return reportService.getCategoryBreakdown(userId, year, month);
    }


    @GetMapping("/viewreports")
    public String viewReports(Model model, Principal principal) {
        // You can fetch the logged-in user’s ID/email here
        Long userId = userService.findByEmail(principal.getName()).getId();
        int year = LocalDate.now().getYear();
        int month = LocalDate.now().getMonthValue();

        model.addAttribute("userId", userId);
        model.addAttribute("year", year);
        model.addAttribute("month", month);

        return "reports";  // <-- points to reports.html
    }

    
}


