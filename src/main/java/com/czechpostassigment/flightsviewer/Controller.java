package com.czechpostassigment.flightsviewer;

import com.fasterxml.jackson.databind.JsonNode;
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

@RestController
@RequestMapping("/api/v1")
public class Controller {

    private final Provider Service;

    @Autowired
    public Controller(Provider Service) {
        this.Service = Service;
    }

    @GetMapping(value = "/odlety", produces = MediaType.APPLICATION_JSON_VALUE)
    public String GetAPI_AllFlights() {
        return Service._Cache;
    }

    @GetMapping(value = "/filter", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> GetAPI_CertainFlights(
        @RequestParam("letiste") String letisteKod,
        @RequestParam("od") @DateTimeFormat(pattern = "yyyyMMddHHmm") LocalDateTime datumOd,
        @RequestParam("do") @DateTimeFormat(pattern = "yyyyMMddHHmm") LocalDateTime datumDo
            ) {

        List<JsonNode> filteredResults = new ArrayList<>();

        long UnixOd = datumOd.toEpochSecond(ZoneOffset.UTC);
        long UnixDo = datumDo.toEpochSecond(ZoneOffset.UTC);

        if (UnixOd > UnixDo) {
            return ResponseEntity.badRequest().body("Chyba: Datum 'od' nesmi byt po datu 'do'.");
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

        if (filteredResults.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(filteredResults);
    }
}
