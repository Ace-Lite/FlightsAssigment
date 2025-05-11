package com.czechpostassigment.flightsviewer;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Controller;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

@Controller
public class WebController {

    private final Provider Service;

    @Autowired
    public WebController(Provider Service) {
        this.Service = Service;
    }

    // This method will handle both initial page load and form submissions
    @GetMapping("/odlety-web")
    public String showOdletyPage(
            // @RequestParam for filters. 'required = false' so page can load without params initially.
            @RequestParam(name = "letiste", required = false) String letisteKod,
            @RequestParam(name = "od", required = false) String odString,
            @RequestParam(name = "do", required = false) String doString,
            Model model) { // Model is used to pass data to Thymeleaf

        // Add submitted parameters back to model to repopulate form (Thymeleaf handles this with param.*)
        // model.addAttribute("currentLetiste", letisteKod);
        // model.addAttribute("currentOd", odString);
        // model.addAttribute("currentDo", doString);

        // Only filter if all required parameters are present
        if (letisteKod != null && !letisteKod.isBlank() &&
                odString != null && !odString.isBlank() &&
                doString != null && !doString.isBlank()) {

            LocalDateTime datumOd;
            LocalDateTime datumDo;

            try {
                // Parse date strings from request parameters
                // The @DateTimeFormat annotation on method params is more for direct binding,
                // here we parse manually since they are optional and we check presence first.
                DateTimeFormatter requestParamFormatter = DateTimeFormatter.ofPattern("yyyyMMddHHmm");
                datumOd = LocalDateTime.parse(odString, requestParamFormatter);
                datumDo = LocalDateTime.parse(doString, requestParamFormatter);

                if (datumOd.isAfter(datumDo)) {
                    model.addAttribute("errorMessage", "Chyba: Datum 'od' nesmí být po datu 'do'.");
                    return "odlety-page"; // Return to page with error
                }

                // --- Your Filtering Logic for JsonNode (same as your REST API) ---
                if (_Parsd == null || !_Parsd.isArray()) {
                    model.addAttribute("errorMessage", "Data odletů nejsou k dispozici.");
                    return "odlety-page";
                }

                List<JsonNode> filteredResults = StreamSupport.stream(_Parsd.spliterator(), false)
                        .filter(JsonNode::isObject)
                        .filter(flightNode -> {
                            JsonNode airportField = flightNode.get("letiste"); // Adjust field name
                            return airportField != null && airportField.isTextual() &&
                                    airportField.asText().equalsIgnoreCase(letisteKod);
                        })
                        .filter(flightNode -> {
                            JsonNode departureTimeField = flightNode.get("departureTime"); // Adjust field name
                            if (departureTimeField == null || !departureTimeField.isTextual()) return false;
                            try {
                                LocalDateTime flightDepartureTime = LocalDateTime.parse(departureTimeField.asText(), JSON_DATE_TIME_FORMATTER); // Use JSON specific formatter
                                return !flightDepartureTime.isBefore(datumOd) && !flightDepartureTime.isAfter(datumDo);
                            } catch (DateTimeParseException e) {
                                System.err.println("Web: Could not parse departureTime '" + departureTimeField.asText() + "': " + e.getMessage());
                                return false;
                            }
                        })
                        .collect(Collectors.toList());
                // --- End of Filtering Logic ---

                model.addAttribute("departures", filteredResults); // Pass filtered list to Thymeleaf

            } catch (DateTimeParseException e) {
                model.addAttribute("errorMessage", "Chybný formát data. Použijte RRRRMMDDHHMM. (" + e.getMessage() + ")");
            } catch (Exception e) {
                model.addAttribute("errorMessage", "Nastala neočekávaná chyba: " + e.getMessage());
                e.printStackTrace(); // Log for developer
            }
        }
        // else if any parameter is missing and it's not the initial load (i.e., some params provided but not all)
        // you might want to add a specific message or just show the empty form.
        // For simplicity, if not all params are there, it just shows the form without results.

        return "odlety-page"; // This string refers to "odlety-page.html" in templates folder
    }
}
