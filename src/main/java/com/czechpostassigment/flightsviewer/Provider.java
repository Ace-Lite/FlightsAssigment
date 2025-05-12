package com.czechpostassigment.flightsviewer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import org.springframework.web.client.HttpClientErrorException; // For error handling

//Pro import JSON souborů
//import com.fasterxml.jackson.core.JsonProcessingException;
//import java.nio.file.Files;
//import java.nio.file.Path;

/*
 *
 *   Pořizovatel dat
 *
 */

@Service("Provider")
public class Provider {

    // Config

    private static final String LINK = "opensky-network.org";
    private static final String ICAO_ID = "LKPR";

    private final RestTemplate restTemplate;

    // _Cache = String JSON
    public String   _Cache = null;

    // _Parsd = _Cache vytvoření JSON NODE
    public JsonNode _Parsd = null;

    /*
    *
    *   METODY
    *
    */

    @Scheduled(fixedRate = 3600)
    public String Receive()
    {
        long UNIX_TIME = System.currentTimeMillis() / 1000L;

        try {
            String url = UriComponentsBuilder.newInstance()
                    .scheme("https")
                    .host(LINK)
                    .path("/api/flights/departure")
                    .queryParam("airport",  ICAO_ID)
                    .queryParam("begin",    UNIX_TIME-596160)
                    .queryParam("end",      UNIX_TIME)
                    .build(true)
                    .toUriString();

            ObjectMapper mapper = new ObjectMapper();
            assert restTemplate != null;

            String data = restTemplate.getForObject(url, String.class);

            //ObjectMapper mapper = new ObjectMapper();
            //Path filename = Path.of("D:\\[00] Project Files\\FlightAssigment\\src\\main\\resources\\tempdata.json");
            //String data = Files.readString(filename);

            _Parsd = mapper.readTree(data);
            _Cache = data;

            System.out.println("Cache aktualizovana - " + UNIX_TIME);
            return _Cache;
        //} catch (JsonProcessingException e) {
        //    System.out.println("Chyba pri cteni JSON souboru - " + e.getMessage());
        } catch (HttpClientErrorException e) {
            System.err.println("Chyba pri zadani o JSON data: " + e.getStatusCode() + " " + e.getResponseBodyAsString());
        } catch (java.io.IOException e) {
            System.out.println("Chyba pri cteni JSON souboru - " + e.getMessage());
        } catch (RestClientException e) {
            System.out.println("Chyba pri odberu dat ze OpenSky site - " + e.getMessage());
        }

        return null;
    }

    @Autowired
    public Provider(RestTemplateBuilder restTemplateBuilder) {
        this.restTemplate = restTemplateBuilder.build();
        this.Receive();
    }
}
