package br.edu.ifsp.mef.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DashboardController {

	@GetMapping("/")
	public String getHome() {
		return "dashboard";
	}
	
	@GetMapping("/dashboard")
	public String getDashboard() {
		return "dashboard";
	}
}
