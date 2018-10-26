/*
 * Copyright 2018 BSC Msc, LLC
 *
 * This file is part of the AuTe Framework project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ru.bsc.test.at.mock.application;

import com.fasterxml.classmate.TypeResolver;
import com.github.tomakehurst.wiremock.common.Notifier;
import com.github.tomakehurst.wiremock.common.Slf4jNotifier;
import com.github.tomakehurst.wiremock.core.WireMockApp;
import com.github.tomakehurst.wiremock.http.AdminRequestHandler;
import com.github.tomakehurst.wiremock.http.StubRequestHandler;
import com.github.tomakehurst.wiremock.servlet.NotImplementedContainer;
import com.github.tomakehurst.wiremock.servlet.WireMockHandlerDispatchingServlet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.context.request.async.DeferredResult;
import ru.bsc.test.at.mock.filter.CorsFilter;
import ru.bsc.test.at.mock.wiremock.webcontextlistener.configuration.CustomWarConfiguration;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.builders.ResponseMessageBuilder;
import springfox.documentation.schema.WildcardType;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import javax.servlet.Servlet;
import javax.servlet.ServletContext;
import java.time.LocalDate;

import static com.google.common.collect.Lists.newArrayList;
import static springfox.documentation.schema.AlternateTypeRules.newRule;


@SpringBootApplication(scanBasePackages = "ru.bsc.test.at.mock")
@EnableSwagger2
public class MockApplication {

    private static final String APP_CONTEXT_KEY = "WireMockApp";
    private final TypeResolver typeResolver;
    private final ServletContext context;

    @Autowired
    public MockApplication(TypeResolver typeResolver, ServletContext context) {
        this.typeResolver = typeResolver;
        this.context = context;
    }

    public static void main(String[] args) {
        SpringApplication.run(new Class[]{MockApplication.class, SpringWebConfig.class}, args);
    }

    @Bean
    public FilterRegistrationBean corsFilter() {
        FilterRegistrationBean filter = new FilterRegistrationBean(new CorsFilter());
        filter.addUrlPatterns("/*");
        return filter;
    }

    @Bean
    public WireMockApp wireMockApp() {
        WireMockApp wireMockApp = new WireMockApp(new CustomWarConfiguration(context, "."), new NotImplementedContainer());

        context.setAttribute(APP_CONTEXT_KEY, wireMockApp);
        context.setAttribute(StubRequestHandler.class.getName(), wireMockApp.buildStubRequestHandler());
        context.setAttribute(AdminRequestHandler.class.getName(), wireMockApp.buildAdminRequestHandler());
        context.setAttribute(Notifier.KEY, new Slf4jNotifier(false));

        return wireMockApp;
    }

    @Bean
    public ServletRegistrationBean wiremockAdminHandlerBean() {
        ServletRegistrationBean bean = new ServletRegistrationBean(new WireMockHandlerDispatchingServlet(), "/__admin/*");
        bean.addInitParameter("RequestHandlerClass", AdminRequestHandler.class.getName());
        bean.setLoadOnStartup(1);
        bean.setName("wiremockAdmin");
        return bean;
    }

    @Bean
    public ServletRegistrationBean wiremockMockHandlerBean() {
        Servlet servlet = new WireMockHandlerDispatchingServlet();
        ServletRegistrationBean bean = new ServletRegistrationBean(servlet, "/bsc-wire-mock/*");
        bean.addInitParameter("RequestHandlerClass", StubRequestHandler.class.getName());
        bean.addInitParameter(WireMockHandlerDispatchingServlet.MAPPED_UNDER_KEY, "/bsc-wire-mock/");
        bean.setLoadOnStartup(1);
        bean.setName("wiremockStub");
        return bean;
    }

    @Bean
    public Docket wiremockApi() {
        return new Docket(DocumentationType.SWAGGER_2)
                .select()
                .apis(RequestHandlerSelectors.any())
                .paths(PathSelectors.regex(".*__admin.*"))
                .build()
                .pathMapping("/")
                .directModelSubstitute(LocalDate.class,
                        String.class)
                .alternateTypeRules(
                        newRule(typeResolver.resolve(DeferredResult.class,
                                typeResolver.resolve(ResponseEntity.class, WildcardType.class)),
                                typeResolver.resolve(WildcardType.class)))
                .useDefaultResponseMessages(false)
                .globalResponseMessage(RequestMethod.GET,
                        newArrayList(new ResponseMessageBuilder()
                                .code(500)
                                .message("Http error 500")
                                .build()))
                .enableUrlTemplating(true);
    }
}
