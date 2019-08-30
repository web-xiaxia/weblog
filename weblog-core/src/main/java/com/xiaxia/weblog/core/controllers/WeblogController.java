package com.xiaxia.weblog.core.controllers;

import com.xiaxia.weblog.core.properties.WeblogConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/weblog")
public class WeblogController {
    @Autowired
    private WeblogConfig weblogConfig;

    @GetMapping("/index.html")
    public String index(Model model) {
        model.addAttribute("weblogConfig", weblogConfig);
        return "weblog";
    }
}
