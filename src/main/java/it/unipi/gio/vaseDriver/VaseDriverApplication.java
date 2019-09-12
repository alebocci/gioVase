package it.unipi.gio.vaseDriver;

import it.unipi.gio.vaseDriver.model.Goal;
import it.unipi.gio.vaseDriver.rest.out.GioPlants;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import org.springframework.web.client.RestTemplate;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.Duration;
import java.util.concurrent.CopyOnWriteArrayList;

@SpringBootApplication
public class VaseDriverApplication {

	public static void main(String[] args) {
		SpringApplication.run(VaseDriverApplication.class, args);
	}

	@Bean
	public GioPlants vaseDriver(Environment env, RestTemplate restTemplate){
		String ip = env.getProperty("gio_plants.ip");
		Integer port = env.getProperty("gio_plants.port",Integer.class);
		if(port==null){
			port = 5000;
		}
		GioPlants vase=null;
		try {
			InetAddress inetAddress = InetAddress.getByName(ip);
			vase = new GioPlants(inetAddress,port, restTemplate);
		} catch (UnknownHostException | IllegalArgumentException e) {
			System.exit(-3);
		}
		return vase;
	}

	@Bean
	public RestTemplate restTemplate(
			RestTemplateBuilder restTemplateBuilder) {

		return restTemplateBuilder
				.setConnectTimeout(Duration.ofSeconds(10))
				.setReadTimeout(Duration.ofSeconds(10))
				.build();
	}
}
