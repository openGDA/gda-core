/*-
 * Copyright © 2020 Diamond Light Source Ltd.
 *
 * This file is part of GDA.
 *
 * GDA is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License version 3 as published by the Free
 * Software Foundation.
 *
 * GDA is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along
 * with GDA. If not, see <http://www.gnu.org/licenses/>.
 */

package uk.ac.diamond.daq.context.web;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.validation.MessageCodesResolver;
import org.springframework.validation.Validator;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.filter.CommonsRequestLoggingFilter;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.HandlerMethodReturnValueHandler;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.config.annotation.AsyncSupportConfigurer;
import org.springframework.web.servlet.config.annotation.ContentNegotiationConfigurer;
import org.springframework.web.servlet.config.annotation.DefaultServletHandlerConfigurer;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import uk.ac.diamond.daq.mapping.api.document.DocumentMapper;
import uk.ac.diamond.daq.web.SpringWebInitializer;

/**
 * Defines the configuration for the rest {@link AnnotationConfigWebApplicationContext}
 *
 * @see SpringWebInitializer
 *
 * @author Maurizio Nagni
 */
@Configuration
@EnableWebMvc
@ComponentScan(basePackages = {"uk.ac.diamond.daq.service", "uk.ac.diamond.daq.mapping.api.document", 
		"uk.ac.diamond.daq.experiment.structure", "uk.ac.gda.core.tool.spring"})
public class SpringContextConfiguration implements WebMvcConfigurer {

	@Autowired
	private DocumentMapper documentMapper; 

	@Override
	public void addFormatters(FormatterRegistry registry) {
			// 	Not used		
	}

	@Override
	public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
		MappingJackson2HttpMessageConverter jacksonMapper = new MappingJackson2HttpMessageConverter();
		jacksonMapper.setObjectMapper(documentMapper.getJacksonObjectMapper());
		converters.add(jacksonMapper);		
	}

	@Override
	public Validator getValidator() {
		// 	Not used
		return null;
	}

	@Override
	public void configureContentNegotiation(ContentNegotiationConfigurer configurer) {
		// 	Not used
	}

	@Override
	public void configureAsyncSupport(AsyncSupportConfigurer configurer) {
		// 	Not used
	}

	@Override
	public void configurePathMatch(PathMatchConfigurer configurer) {
		// 	Not used
	}

	@Override
	public void addArgumentResolvers(List<HandlerMethodArgumentResolver> argumentResolvers) {
		// 	Not used
	}

	@Override
	public void addReturnValueHandlers(List<HandlerMethodReturnValueHandler> returnValueHandlers) {
		// 	Not used
	}

	@Override
	public void configureHandlerExceptionResolvers(List<HandlerExceptionResolver> exceptionResolvers) {
		// 	Not used
	}

	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		// 	Not used
	}

	@Override
	public MessageCodesResolver getMessageCodesResolver() {
		// 	Not used
		return null;
	}

	@Override
	public void addViewControllers(ViewControllerRegistry registry) {
		// 	Not used
	}

	@Override
	public void addResourceHandlers(ResourceHandlerRegistry registry) {
		// 	Not used
	}

	@Override
	public void configureDefaultServletHandling(DefaultServletHandlerConfigurer configurer) {
		// 	Not used
	}

	@Bean
	public CommonsRequestLoggingFilter requestLoggingFilter() {
	    CommonsRequestLoggingFilter loggingFilter = new CommonsRequestLoggingFilter();
	    loggingFilter.setIncludeClientInfo(true);
	    loggingFilter.setIncludeQueryString(true);
	    loggingFilter.setIncludePayload(true);
	    loggingFilter.setMaxPayloadLength(64000);
	    return loggingFilter;
	}
    
}
