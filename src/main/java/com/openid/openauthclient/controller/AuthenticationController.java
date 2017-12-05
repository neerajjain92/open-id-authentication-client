package com.openid.openauthclient.controller;

import com.google.gson.Gson;
import com.openid.openauthclient.interceptors.HeaderRequestInterceptor;
import com.openid.openauthclient.model.OpenIdConnectUserDetails;
import com.openid.openauthclient.model.SpringBootQuote;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class AuthenticationController {

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private static final String BASE_URL = "http://kamkr01-2w7/ppm/rest/v1/";
    RestTemplate restTemplate = new RestTemplate();
    List<ClientHttpRequestInterceptor> interceptors = new ArrayList<ClientHttpRequestInterceptor>();
    private static String authToken = null;
    Gson gson = new Gson();
    Map<String, String> userExistResponse = new HashMap<>();

    @RequestMapping("/authenticate")
    public final String authenticate(HttpServletRequest servletRequest, HttpServletResponse response) throws Exception {

        for (Cookie cookie : servletRequest.getCookies()) {
            logger.info("Cookie is {}",cookie.getName());
            cookie.setValue("");
            cookie.setMaxAge(0);
            cookie.setPath("/");
            response.addCookie(cookie);
        }
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        OpenIdConnectUserDetails userDetails = (OpenIdConnectUserDetails) authentication.getPrincipal();
        logger.info("User Id {}", userDetails.getUserId());
        logger.info("User Name {}", userDetails.getUsername());
        logger.info("Access Token {}", userDetails.getToken());

        interceptors.add(new HeaderRequestInterceptor("Content-Type", MediaType.APPLICATION_JSON_VALUE));

        userExistResponse = isUserExist(userDetails.getToken().toString());
        response.addCookie(new Cookie("sessionId", authToken));

        if ("yes".equalsIgnoreCase(userExistResponse.get("userExist"))) {
            userExistResponse.put("firstName", "Neeraj");
            userExistResponse.put("lastName", "Jain");
            userExistResponse.put("email", "jain007neeraj@gmail.com");
            return "redirect:" + "http://localhost:3000/";
        } else {
            return "redirect:" + "http://localhost:8080/index.html?showRegisterForm=true&firstName=" + userExistResponse.get("firstName")
                    + "&lastName=" + userExistResponse.get("lastName") + "&email=" + userExistResponse.get("email");
        }
    }

    private String parseAuthToken(String authTokenString) {
        authTokenString = authTokenString.substring(authTokenString.lastIndexOf("::") + 2);
        return authTokenString;
    }

    public Map<String, String> isUserExist(String accessToken) throws Exception {
        String accessTokenQueryParam = "?access_token=" + accessToken;
        String buildUrl = BASE_URL.concat("auth/login").concat(accessTokenQueryParam);
        logger.info("User Info End Point {}", buildUrl);
        String json = restTemplate.getForObject(buildUrl, String.class);
        logger.info("JSON String {}", json);


        userExistResponse = gson.fromJson(json, userExistResponse.getClass());
        authToken = userExistResponse.get("authToken");

        if (!authToken.equals("")) { // Session created for user, let's redirect to our app
            authToken = parseAuthToken(authToken);
            logger.info("Clarity Session is {}", authToken);
            userExistResponse.put("userExist", "yes");
        } else { // Let's create user
            userExistResponse.put("firstName", userExistResponse.get("given_name"));
            userExistResponse.put("lastName", userExistResponse.get("family_name"));
            userExistResponse.put("persona", "projectMgmt");
            userExistResponse.put("userExist", "no");
        }
        return userExistResponse;
    }

    @RequestMapping(value = "/register")
    public String registerMe(@RequestParam("firstName") String firstName,
                             @RequestParam("lastName") String lastName,
                             @RequestParam("persona") String persona,
                             @RequestParam("email") String email,
                             HttpServletResponse servletResponse) throws Exception {

        logger.info("User Info {}::{}::{}::{}", firstName, lastName, email, persona);

        Map<String, String> userInfo = new HashMap<>();
        userInfo.put("firstName", firstName);
        userInfo.put("lastName", lastName);
        userInfo.put("persona", persona);
        userInfo.put("email", email);


        String registerUrl = BASE_URL.concat("auth/login?registerUser=true");
        HttpClient httpClient = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost(registerUrl);
        httpPost.setHeader(HttpHeaders.CONTENT_TYPE, "application/json");

        StringEntity params = new StringEntity(new Gson().toJson(userInfo));
        httpPost.setEntity(params);


        logger.info("URI is {} and params {}", httpPost.getURI(), params);
        //Execute and get the response.
        HttpResponse response = httpClient.execute(httpPost);
        HttpEntity httpEntity = response.getEntity();
        Gson gson = new Gson();
        Map<String, String> userRegistrationResponse = new HashMap<>();


        if (httpEntity != null) {
            BufferedReader br = new BufferedReader(new InputStreamReader(httpEntity.getContent()));
            try {
                String inputLine = br.readLine();
                System.out.println(inputLine);
                userRegistrationResponse = gson.fromJson(inputLine, userRegistrationResponse.getClass());
//                while ((inputLine = br.readLine()) != null) {
//                    System.out.println(inputLine);
//                }
                br.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        servletResponse.addCookie(new Cookie("sessionId", parseAuthToken(userRegistrationResponse.get("authToken"))));
//        return "redirect:" + "http://kamkr01-2w7/pm";

        return "redirect:" + "http://localhost:3000/";
    }


    public void getRandomSpringBootQuotation() {
        RestTemplate restTemplate = new RestTemplate();
        SpringBootQuote springBootQuote = restTemplate.getForObject("https://gturnquist-quoters.cfapps.io/api/random", SpringBootQuote.class);
        logger.info("Today's Quote {}", springBootQuote.toString());
    }

    public class SearchRequest {
        private Map<String, String> params;
    }
}
