package api.poja.app.config;

import java.time.Clock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ClockConf {

  @Bean
  public Clock clock() {
    return Clock.systemUTC();
  }
}
