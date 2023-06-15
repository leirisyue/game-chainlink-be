package com.stid.project.fido2server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.file.Path;

@Controller
@SpringBootApplication
@ConfigurationPropertiesScan
public class Fido2ServerApplication {
    private static final Logger LOGGER = LoggerFactory.getLogger(Fido2ServerApplication.class);

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(Fido2ServerApplication.class);
        Environment env = app.run(args).getEnvironment();
        logApplicationStartup(env);
    }

    private static void logApplicationStartup(Environment env) {
        String protocol = "http";
        if (Boolean.parseBoolean(env.getProperty("server.ssl.enabled")) && env.getProperty("server.ssl.key-store") != null) {
            protocol = "https";
        }
        String serverPort = env.getProperty("server.port");
        String contextPath = env.getProperty("server.servlet.context-path");
        if (!StringUtils.hasText(contextPath)) {
            contextPath = "/";
        }
        String hostAddress = "localhost";
        try {
            hostAddress = InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            LOGGER.warn("The host name could not be determined, using `localhost` as fallback");
        }

        LOGGER.info(
                "\n----------------------------------------------------------\n" +
                        "\tApplication '{}-v{}' is running!\n" +
                        "\tLocal URL: \t\t{}://localhost:{}{}\n" +
                        "\tExternal URL: \t{}://{}:{}{}\n" +
                        "\tProfile(s): \t{}\n" +
                        "\tWorking Dir: \t{}\n" +
                        "----------------------------------------------------------",
                env.getProperty("app.name"),
                env.getProperty("app.version"),
                protocol,
                serverPort,
                contextPath,
                protocol,
                hostAddress,
                serverPort,
                contextPath,
                env.getActiveProfiles(),
                Path.of("").normalize().toAbsolutePath()
        );
    }

    @RequestMapping({"/auth/**", "/pages/**"})
    public String forwardApp() {
        return "forward:/app.html";
    }

    @RequestMapping("/app")
    public String redirectApp() {
        return "redirect:/app.html";
    }

}