package com.soaesps.schedulerservice.reports;

import com.soaesps.schedulerservice.dto.FailedDTO;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import org.springframework.web.servlet.ModelAndView;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.util.List;

/**
 * Service responsible for rendering scheduler incident reports.
 *
 * <p>Serves two separate consumers:
 * <ul>
 *   <li><b>Web layer</b> — {@link #makeIncident(Model, List)} and {@link #getModelAndView()}
 *       populate Spring MVC models for browser-facing views;</li>
 *   <li><b>Email layer</b> — {@link #createHtmlReport(List)} renders the same template
 *       to a standalone HTML string for outbound notifications.</li>
 * </ul>
 */
@Service("schedulerReport")
public class SchedulerReport {

    private static final String EMAIL_TEMPLATE_NAME = "email-template";
    private static final String WELCOME_VIEW_NAME = "welcomePage";
    private static final String INCIDENTS_VARIABLE = "incidentHtmlDtoList";

    private final SpringTemplateEngine templateEngine;

    public SchedulerReport(SpringTemplateEngine templateEngine) {
        this.templateEngine = templateEngine;
    }

    /**
     * Populates the Spring MVC model with the incident list for a web view.
     * Used by controllers that render Thymeleaf pages served to browsers.
     */
    public void makeIncident(Model model, List<FailedDTO> incidents) {
        model.addAttribute(INCIDENTS_VARIABLE, incidents);
    }

    /**
     * Builds a ModelAndView pointing at the welcome page.
     * Kept for backward compatibility with existing controllers.
     */
    public ModelAndView getModelAndView() {
        ModelAndView mav = new ModelAndView();
        mav.setViewName(WELCOME_VIEW_NAME);
        return mav;
    }

    /**
     * Renders the "email-template" Thymeleaf template into a standalone HTML string
     * suitable for embedding in an email body.
     *
     * @param incidents list of failed job entries to render
     * @return fully rendered HTML string
     */
    public String createHtmlReport(List<FailedDTO> incidents) {
        Context context = new Context();
        context.setVariable(INCIDENTS_VARIABLE, incidents);
        return templateEngine.process(EMAIL_TEMPLATE_NAME, context);
    }

    /**
     * Convenience overload for rendering a single incident.
     * Wraps the DTO in a singleton list so the same template (which expects a collection)
     * can be reused without duplication.
     */
    public String createHtmlReport(FailedDTO singleIncident) {
        return createHtmlReport(List.of(singleIncident));
    }
}