package com.example.demo.Service;
import com.example.demo.Model.AiResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.templateresolver.StringTemplateResolver;
import java.util.List;

@Service
public class DigestTemplateService {

    private final TemplateEngine templateEngine;
    private final String actionBaseUrl;

    public DigestTemplateService(@Value("${gms.action-base-url:http://localhost:8081/api/emails}") String actionBaseUrl) {

        this.actionBaseUrl = actionBaseUrl;

        StringTemplateResolver resolver = new StringTemplateResolver();
        resolver.setTemplateMode("HTML");

        this.templateEngine = new TemplateEngine();
        this.templateEngine.setTemplateResolver(resolver);
    }

    public String generateDigest(String account, List<AiResponse> responses) {

        Context context = new Context();
        context.setVariable("account", account);
        context.setVariable("responses", responses);
        context.setVariable("actionBaseUrl", actionBaseUrl);
        return templateEngine.process(ThymeLeaf.Thyme.DIGEST_TEMPLATE, context);
    }
}