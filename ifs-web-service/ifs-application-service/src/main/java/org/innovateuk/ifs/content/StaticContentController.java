package org.innovateuk.ifs.content;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/info")
@PreAuthorize("permitAll")
public class StaticContentController {

    @GetMapping("contact")
    public String contact() {
        return "content/contact";
    }

    @GetMapping("cookies")
    public String cookies() {
        return "content/cookies";
    }

    @GetMapping("terms-and-conditions")
    public String termsAndConditions() {
        return "content/terms-and-conditions";
    }
}
