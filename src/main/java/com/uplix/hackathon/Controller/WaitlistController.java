package com.uplix.hackathon.Controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.uplix.hackathon.Dto.GetWaitlistDTO;
import com.uplix.hackathon.Service.Waitlist;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("hackathon")
@AllArgsConstructor
@Slf4j
public class WaitlistController {

    private final Waitlist waitlist;

    @PostMapping("/waitlist")
    public ResponseEntity<Map<String, Boolean>> createWaitlist(@RequestBody HashMap<String,String> emailMap) throws UnirestException {

        boolean emailAdded = waitlist.sendEmailToClerk(emailMap);
        log.info("email added: {}", emailAdded);


        if (emailAdded) {
            return ResponseEntity.ok(Map.of("success", true));
        } else {
            return ResponseEntity.ok(Map.of("success", false));
        }
    }

    @GetMapping("/waitlist")
    public ResponseEntity<GetWaitlistDTO> getWaitlist() throws Exception {
        GetWaitlistDTO waitlistedPeople = waitlist.getWaitlist();
        return ResponseEntity.ok(waitlistedPeople);
    }
}
