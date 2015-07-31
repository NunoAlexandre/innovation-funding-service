package com.worth.ifs.controller;

import com.worth.ifs.domain.User;
import com.worth.ifs.filter.LoginFilter;
import com.worth.ifs.form.LoginForm;
import com.worth.ifs.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * This controller handles user login, logout and authentication / authorization.
 * It will also redirect the user after the login/logout is successful.
 */
@Controller
@Configuration
//@EnableWebMvcSecurity
//@EnableGlobalMethodSecurity
public class LoginController {
    @Autowired
    UserService userService;

    @RequestMapping(value="/login", method= RequestMethod.GET)
     public String login( Model model, HttpServletResponse response) {
        String token = "";
        if(token != null && token != ""){
            User user = userService.retrieveUserByToken(token);
            if(user != null){
                System.out.println("already logged in, redirect to dashboard");
                return "redirect:/applicant/dashboard";
            }
        }

        List<User> users =userService.findAll();

        System.out.println("Users in frontend " + users.size());

        model.addAttribute("users", users);
        model.addAttribute("loginForm", new LoginForm());


        return "login";
    }

    @RequestMapping(value="/logout", method= RequestMethod.GET)
    public String logout(Model model, HttpServletResponse response) {
        // Removing the cookie is not possible, just expire it as soon as possible.
        Cookie cookie = new Cookie(LoginFilter.IFS_AUTH_COOKIE_NAME, null);
        cookie.setMaxAge(0);
        response.addCookie(cookie);
        System.out.println("removed cookie, redirect to login");
        return "redirect:/login";
    }


    @RequestMapping(value="/login", method= RequestMethod.POST)
    public String loginSubmit(@ModelAttribute LoginForm loginForm, HttpServletResponse response){
        User user = userService.retrieveUserByToken(loginForm.getToken());
        if(user != null){
            response.addCookie(new Cookie(LoginFilter.IFS_AUTH_COOKIE_NAME, user.getToken()));

            // redirect to my applications
            return "redirect:/applicant/dashboard";
        }

        return "login";
    }






}

