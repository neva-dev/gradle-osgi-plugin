package com.neva.example;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;

@Component(
    immediate = true,
    service = HelloService.class
)
class HelloService {

  @Activate
  protected void activate() {
    System.out.println("Hello World!");
  }

}
