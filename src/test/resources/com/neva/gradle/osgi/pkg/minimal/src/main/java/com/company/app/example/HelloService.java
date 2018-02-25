package com.company.app.example;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(immediate = true, service = HelloService.class)
class HelloService {

  private static final Logger LOGGER = LoggerFactory.getLogger(HelloService.class);

  @Activate
  protected void activate() {
    LOGGER.info("Hello world!");
  }

  @Deactivate
  protected void deactivate() {
    LOGGER.info("Good bye world!");
  }

}