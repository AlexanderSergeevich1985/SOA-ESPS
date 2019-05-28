package com.soaesps.schedulerservice.reports;

import com.soaesps.schedulerservice.dto.FailedDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import org.springframework.web.servlet.ModelAndView;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring5.SpringTemplateEngine;

import java.util.List;

@Service("schedulerReport")
public class SchedulerReport {
    @Autowired
    private SpringTemplateEngine templateEngine;

    public void makeIncident(final Model model, final List<FailedDTO> incidents) {
        model.addAttribute("incidentHtmlDtoList", incidents);
    }

    public ModelAndView getModelAndView() {
        ModelAndView mav = new ModelAndView();
        mav.setViewName("welcomePage");
        mav.addObject("WelcomeMessage", message);
        return mav;
    }

    public String createHtmlReport(final FailedDTO exception) {
        Context context = new Context();
        context.setVariable("incidentHtmlDtoList", exception);
        String html = templateEngine.process("email-template", context);
        return html;
    }
}