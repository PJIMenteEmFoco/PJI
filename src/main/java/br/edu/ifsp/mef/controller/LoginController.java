package br.edu.ifsp.mef.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class LoginController {
	
	@GetMapping("/login")
    public String getLogin() {
        return "login";
    }

	@GetMapping("/")
	public String getHome() {
		return "login";
	}
	
}
