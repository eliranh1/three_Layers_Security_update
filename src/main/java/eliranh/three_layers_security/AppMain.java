package eliranh.three_layers_security;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.component.page.Push;

@SpringBootApplication @Push
public class AppMain implements AppShellConfigurator{

	// uploaded....
	public static void main(String[] args) { 
		SpringApplication.run(AppMain.class, args);
	}
	
}
	