import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;

import com.esgyn.kafka.impl.Runner;

public class ConfigureTest {

	public static void main(String[] args) throws ConfigurationException {
		String bPath=System.getProperty("java.ext.dirs");
		int startIndex=bPath.lastIndexOf(":");
		String absolutepath=bPath.substring(startIndex+1);
		System.out.println("absolute path 2:" + absolutepath);
		try {
			InputStream in=Runner.class.getResource(absolutepath+ "/../config/config.properties").openStream();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
