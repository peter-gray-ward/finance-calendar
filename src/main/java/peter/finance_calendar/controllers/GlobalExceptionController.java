package peter.finance_calendar.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

@ControllerAdvice
public class GlobalExceptionController {

    
    // Handle all unchecked exceptions (RuntimeException and its subclasses)
    @ExceptionHandler(RuntimeException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)  // Set the HTTP status to 500
    public String handleRuntimeException(RuntimeException ex, Model model) {
        // Add exception details to the model
        model.addAttribute("errorMessage", ex.getMessage());
        return "error";  // This will return the error.html template
    }

    // You can also handle other types of exceptions globally, if needed
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String handleGenericException(Exception ex, Model model) {
        model.addAttribute("errorMessage", "An unexpected error occurred: " + ex.getMessage());
        return "error";  // Render the same error page
    }
}
