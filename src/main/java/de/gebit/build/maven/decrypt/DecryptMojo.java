package de.gebit.build.maven.decrypt;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.settings.Server;
import org.apache.maven.settings.Settings;
import org.apache.maven.settings.crypto.DefaultSettingsDecryptionRequest;
import org.apache.maven.settings.crypto.SettingsDecrypter;
import org.apache.maven.settings.crypto.SettingsDecryptionRequest;
import org.apache.maven.settings.crypto.SettingsDecryptionResult;

@Mojo(name = "decrypt", aggregator = true)
public final class DecryptMojo extends AbstractMojo {

	@Parameter(name = "serverId", property = "serverId", required = true)
	private String serverId;

	@Parameter(name = "outputFile", property = "outputFile", required = false)
	private File outputFile;

	@Component
	private Settings settings;

	@Component
	private SettingsDecrypter decrypter;

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		Server server = null;
		for (Server s : settings.getServers()) {
			if (s.getId().equals(serverId)) {
				server = s;
			}
		}
		if (server == null) {
			throw new MojoExecutionException("serverId not found in settings: " + serverId);
		} else {
			SettingsDecryptionRequest request = new DefaultSettingsDecryptionRequest(server);
			SettingsDecryptionResult result = decrypter.decrypt(request);
			String password = result.getServer().getPassword();
			if (outputFile != null) {
				try {
					outputFile.getParentFile().mkdirs();
					Files.write(outputFile.toPath(), password.getBytes(Charset.forName("UTF-8")));
				} catch (IOException e) {
					throw new MojoExecutionException(e.getMessage(), e);
				}
			} else {
				getLog().info("Decrypted password: '" + password + "'");
			}
		}
	}

}
