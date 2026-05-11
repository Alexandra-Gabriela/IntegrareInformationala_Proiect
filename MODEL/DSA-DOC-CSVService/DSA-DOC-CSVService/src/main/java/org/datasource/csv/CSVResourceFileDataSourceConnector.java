package org.datasource.csv;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.logging.Logger;

@Component
public class CSVResourceFileDataSourceConnector {
	private static final Logger logger = Logger.getLogger(CSVResourceFileDataSourceConnector.class.getName());

	/**
	 * Metoda returneaza fisierul CSV.
	 * Incearca intai calea relativa de proiect, apoi resursele interne.
	 */
	public File getCSVFile(String specificPath) {
		try {
			File file = new File(specificPath);

			// Daca fisierul nu exista la calea specificata, il cautam in resursele proiectului
			if (!file.exists()) {
				logger.info("Fisierul nu a fost gasit la " + specificPath + ". Se incearca ClassPathResource...");

				// Cream un fisier temporar pentru a putea fi citit ca File (obiectele din interiorul JAR nu sunt Files directe)
				File tempFile = File.createTempFile("temp_data", ".csv");
				tempFile.deleteOnExit();

				Files.copy(
						new ClassPathResource(specificPath).getInputStream(),
						tempFile.toPath(),
						StandardCopyOption.REPLACE_EXISTING
				);

				logger.info("... fisierul a fost incarcat din resursele interne (ClassPathResource)!");
				return tempFile;
			} else {
				logger.info("... fisierul a fost incarcat din sistemul local (Local FileSystem)!");
				return file;
			}
		} catch (Exception e) {
			logger.severe("EROARE critica la incarcarea fisierului CSV: " + e.getMessage());
			return null;
		}
	}
}