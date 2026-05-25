package com.finatiol.ventas;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class FinatiolVentasMsApplication {

	public static void main(String[] args) {
		SpringApplication.run(FinatiolVentasMsApplication.class, args);
	}

}
