package com.project.application.controller;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@Slf4j
public class CustomErrorController implements ErrorController {

    @RequestMapping("/error")
    public String handleError(HttpServletRequest request, Model model) {
        // Get error status code
        Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);

        if (status != null) {
            int statusCode = Integer.parseInt(status.toString());

            // Log the error
            String requestUrl = (String) request.getAttribute(RequestDispatcher.ERROR_REQUEST_URI);
            log.warn("Error {} occurred for URL: {}", statusCode, requestUrl);

            // Handle different error types
            switch (statusCode) {
                case 404:
                    model.addAttribute("errorTitle", "Page Not Found");
                    model.addAttribute("errorMessage", "The page you are looking for doesn't exist.");
                    model.addAttribute("errorCode", "404");
                    break;
                case 403:
                    model.addAttribute("errorTitle", "Access Denied");
                    model.addAttribute("errorMessage", "You don't have permission to access this page.");
                    model.addAttribute("errorCode", "403");
                    break;
                case 500:
                    model.addAttribute("errorTitle", "Internal Server Error");
                    model.addAttribute("errorMessage", "Something went wrong on our end. Please try again later.");
                    model.addAttribute("errorCode", "500");
                    break;
                default:
                    model.addAttribute("errorTitle", "Error");
                    model.addAttribute("errorMessage", "An unexpected error occurred.");
                    model.addAttribute("errorCode", statusCode);
                    break;
            }
        } else {
            model.addAttribute("errorTitle", "Error");
            model.addAttribute("errorMessage", "An unexpected error occurred.");
            model.addAttribute("errorCode", "Unknown");
        }

        return "error";
    }
}