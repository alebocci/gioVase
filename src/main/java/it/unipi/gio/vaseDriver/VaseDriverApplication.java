package it.unipi.gio.vaseDriver;

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

@SpringBootApplication
public class VaseDriverApplication {

	public static void main(String[] args) {
		SpringApplication.run(VaseDriverApplication.class, args);
	}

	@Bean
	public GioPlants vaseDriver(Environment env, RestTemplate restTemplate){
		String ip = env.getProperty("gio_plants.ip");
		Integer port = env.getProperty("gio_plants.port",Integer.class);
		String mac = env.getProperty("vase_mac");
		if(port==null){
			port = 5000;
		}
		if(mac==null){
			mac="FE:F4:1C:74:66:B3";
		}
		GioPlants vase=null;
		try {
			InetAddress inetAddress = InetAddress.getByName(ip);
			vase = new GioPlants(inetAddress,port, restTemplate,mac);
		} catch (UnknownHostException | IllegalArgumentException e) {
			System.exit(-3);
		}
		return vase;
	}

	@Bean
	public RestTemplate restTemplate(
			RestTemplateBuilder restTemplateBuilder) {

		return restTemplateBuilder
				.setConnectTimeout(Duration.ofSeconds(2))
				.setReadTimeout(Duration.ofSeconds(2))
				.build();
	}
}
