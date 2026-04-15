package com.example.ChronoFlex.controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Controller;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

import jakarta.servlet.http.HttpSession;

@Controller
public class CaptchaController {

//    @PostMapping("/verify")
//    public String verifyCaptcha(
//            @RequestParam("g-recaptcha-response") String captchaResponse,
//            HttpSession session) {
//
//        String url = "https://www.google.com/recaptcha/api/siteverify";
//
//        RestTemplate restTemplate = new RestTemplate();
//
//        HttpHeaders headers = new HttpHeaders();
//        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
//
//        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
//        params.add("secret", secretKey);
//        params.add("response", captchaResponse);
//
//        HttpEntity<MultiValueMap<String, String>> request =
//                new HttpEntity<>(params, headers);
//
//        ResponseEntity<Map> response =
//                restTemplate.postForEntity(url, request, Map.class);
//
//        System.out.println("FULL GOOGLE RESPONSE: " + response.getBody());
//        System.out.println("CAPTCHA RESPONSE LENGTH: " +
//                (captchaResponse != null ? captchaResponse.length() : 0));
//
//        Boolean success = (Boolean) response.getBody().get("success");
//
//        if (success != null && success) {
//            session.setAttribute("captchaVerified", true);
//            return "redirect:/home";
//        } else {
//            return "redirect:/?error=true";
//        }
//    }

    @Value("${turnstile.secret}")
    private String secret;

    @PostMapping("/verify")
    public String verify(
            @RequestParam("cf-turnstile-response") String token,
            HttpServletRequest request,
            HttpSession session) {

        try {
            if (token == null || token.isEmpty()) {
                return "redirect:/?error=captcha_missing";
            }

            SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
            factory.setConnectTimeout(3000);
            factory.setReadTimeout(3000);

            RestTemplate restTemplate = new RestTemplate(factory);

            String url = "https://challenges.cloudflare.com/turnstile/v0/siteverify";

            MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
            body.add("secret", secret);
            body.add("response", token);
            body.add("remoteip", request.getRemoteAddr());

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            HttpEntity<MultiValueMap<String, String>> requestEntity =
                    new HttpEntity<>(body, headers);

            String response = restTemplate.postForObject(url, requestEntity, String.class);

            if (response == null) {
                return "redirect:/?error=verification_failed";
            }

            ObjectMapper mapper = new ObjectMapper();
            JsonNode jsonNode = mapper.readTree(response);

            boolean success = jsonNode.has("success") && jsonNode.get("success").asBoolean();

            if (success) {
                session.setAttribute("captchaVerified", true); // 🔥 THIS WAS MISSING
                return "redirect:/home";
            } else {
                System.out.println("Captcha failed: " + jsonNode.toString());
                return "redirect:/?error=true";
            }

        } catch (Exception e) {
            e.printStackTrace();
            return "redirect:/?error=server_error";
        }
    }

    @GetMapping("/home")
    public String home(HttpSession session) {

        Boolean verified = (Boolean) session.getAttribute("captchaVerified");

        if (verified != null && verified) {
            return "home";   // home.html inside templates
        } else {
            return "redirect:/";
        }
    }

    @GetMapping("/forms")
    public String registerPage(HttpSession session) {

        Boolean verified = (Boolean) session.getAttribute("captchaVerified");

        if (verified != null && verified) {
            return "forms";   // register.html in templates
        } else {
            return "redirect:/";
        }
    }

    @GetMapping("/adminportal")
    public String adminportal(HttpSession session) {

        Boolean verified = (Boolean) session.getAttribute("captchaVerified");

        if (verified != null && verified) {
            return "adminportal";   // home.html inside templates
        } else {
            return "redirect:/";
        }
    }

    @GetMapping("/facultyportal")
    public String facultyportal(HttpSession session) {

        Boolean verified = (Boolean) session.getAttribute("captchaVerified");

        if (verified != null && verified) {
            return "facultyportal";   // home.html inside templates
        } else {
            return "redirect:/";
        }
    }

    @GetMapping("/student-auth")
    public String StudentAuth(HttpSession session) {

        Boolean verified = (Boolean) session.getAttribute("captchaVerified");

        if (verified != null && verified) {
            return "student-auth";   // home.html inside templates
        } else {
            return "redirect:/";
        }
    }

    @GetMapping("/admin-dashboard")
    public String AdminDashboard(HttpSession session) {

        Boolean verified = (Boolean) session.getAttribute("captchaVerified");

        if (verified != null && verified) {
            return "/admin-dashboard";   // home.html inside templates
        } else {
            return "redirect:/";
        }
    }

    @GetMapping("/mapping")
    public String FacultySubjectMap(HttpSession session) {

        Boolean verified = (Boolean) session.getAttribute("captchaVerified");

        if (verified != null && verified) {
            return "/mapping";
        } else {
            return "redirect:/";
        }
    }

    @GetMapping("/timetableOpts")
    public String timetableOpts(HttpSession session) {

        Boolean verified = (Boolean) session.getAttribute("captchaVerified");

        if (verified != null && verified) {
            return "/timetableOpts";
        } else {
            return "redirect:/";
        }
    }

    @GetMapping("/student-master-list")
    public String StudentMasterList(HttpSession session) {

        Boolean verified = (Boolean) session.getAttribute("captchaVerified");

        if (verified != null && verified) {
            return "/student-master-list";
        } else {
            return "redirect:/";
        }
    }

    @GetMapping("/faculty-timetables")
    public String FacultyTimetables(HttpSession session) {

        Boolean verified = (Boolean) session.getAttribute("captchaVerified");

        if (verified != null && verified) {
            return "/faculty-timetables";
        } else {
            return "redirect:/";
        }
    }

    @GetMapping("/faculty-dashboard")
    public String FacultyDashboard(HttpSession session) {

        Boolean verified = (Boolean) session.getAttribute("captchaVerified");

        if (verified != null && verified) {
            return "/faculty-dashboard";
        } else {
            return "redirect:/";
        }
    }
}
