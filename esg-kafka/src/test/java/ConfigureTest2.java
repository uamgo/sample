import java.io.File;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;

public class ConfigureTest2 {

	public static void main(String[] args) throws ConfigurationException {
		PropertiesConfiguration offsetConfig = new PropertiesConfiguration(new File("offset.properties"));
		offsetConfig.setAutoSave(true);
		offsetConfig.setProperty("ccccc", "bbbbbbbbb");
		while (true) {
			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			System.out.println("wake up...");
			offsetConfig.refresh();
			System.out.println(offsetConfig.getProperty("status"));
		}
	}

}
