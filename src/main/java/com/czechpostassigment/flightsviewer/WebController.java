package com.czechpostassigment.flightsviewer;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Controller;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

@Controller
public class WebController {

    private final Provider Service;

    @Autowired
    public WebController(Provider Service) {
        this.Service = Service;
    }

    @GetMapping("/odlety-web")
    public String WebForm(
            @RequestParam(name = "letiste", required = false) String letisteKod,
            @RequestParam(name = "od", required = false) String odString,
            @RequestParam(name = "do", required = false) String doString,
            Model model) {

        //
        //  Filter dle výsledku
        //


        if (letisteKod != null && !letisteKod.isBlank() &&
                odString != null && !odString.isBlank() &&
                doString != null && !doString.isBlank()) {

            LocalDateTime datumOd;
            LocalDateTime datumDo;

            try {
                DateTimeFormatter requestParamFormatter = DateTimeFormatter.ofPattern("yyyyMMddHHmm");
                datumOd = LocalDateTime.parse(odString, requestParamFormatter);
                datumDo = LocalDateTime.parse(doString, requestParamFormatter);

                List<JsonNode> filteredResults = new ArrayList<>();

                long UnixOd = datumOd.toEpochSecond(ZoneOffset.UTC);
                long UnixDo = datumDo.toEpochSecond(ZoneOffset.UTC);

                if (UnixOd > UnixDo) {
                    model.addAttribute("errorMessage", "Chyba: Datum 'od' nesmí být po datu 'do'.");
                    return "odlety-page";
                }

                for (JsonNode node : Service._Parsd) {
                    if (!node.isObject()) {
                        continue;
                    }

                    JsonNode end = node.get("estArrivalAirport");
                    if (end == null || end.isNull() || !end.isTextual()) {
                        continue;
                    }

                    String endVal = end.asText();

                    if (!endVal.equalsIgnoreCase(letisteKod)) {
                        continue;
                    }

                    JsonNode _date = node.get("firstSeen");

                    if (_date == null || _date.isNull()) {
                        continue;
                    }

                    long _dateVal = Long.parseLong(_date.asText().trim());

                    if (UnixOd < _dateVal && _dateVal < UnixDo) {
                        filteredResults.add(node);
                    } else {
                        continue;
                    }
                }

                model.addAttribute("departures", filteredResults);

            } catch (DateTimeParseException e) {
                model.addAttribute("errorMessage", "Chybný formát data. Použijte RRRRMMDDHHMM. (" + e.getMessage() + ")");
            } catch (Exception e) {
                model.addAttribute("errorMessage", "Nastala neočekávaná chyba: " + e.getMessage());
            }

        //
        //  Celý list
        //
        } else if (!Service._Parsd.isNull()) {
            model.addAttribute("departures", Service._Parsd);
            return "odlety-page";
        }

        return "odlety-page";
    }

}
