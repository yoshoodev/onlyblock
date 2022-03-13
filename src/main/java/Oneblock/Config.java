// Copyright © 2022 MrMarL. All rights reserved.
package Oneblock;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;

public class Config {
	protected static File file;

	public static void Save(FileConfiguration fc, File f) {
		file = f;
		if (XMaterial.supports(18)) {
			try {
				fc.save(f);
			} catch (IOException e) {
				// exception handle
			}
		} else
			try {
				StringBuilder inputBuffer = new StringBuilder();
				try (BufferedReader fileIn = new BufferedReader(new FileReader(f))) {
					String line;

					ArrayList<String> inputStr1 = new ArrayList<>();
					while ((line = fileIn.readLine()) != null)
						inputStr1.add(line);
					ArrayList<String> inputStr2 = new ArrayList<>(Arrays.asList(fc.saveToString().split("\n")));

					int i = 0;
					for (String a : inputStr1) {
						if (i >= inputStr2.size())
							break;
						if (a.contains("#") || a.isEmpty())
							inputBuffer.append(a);
						else
							inputBuffer.append(inputStr2.get(i++));
						inputBuffer.append('\n');
					}

					while (i < inputStr2.size()) {
						inputBuffer.append(inputStr2.get(i++));
						inputBuffer.append('\n');
					}
				}
				try (FileOutputStream fileOut = new FileOutputStream(f)) {
					fileOut.write(inputBuffer.toString().getBytes());
				}
			} catch (Exception e) {
				try {
					fc.save(f);
				} catch (Exception e1) {
					Bukkit.getLogger().warning("Problem reading config.yml.");
				}
			}
	}

	public static void Save(FileConfiguration fc) {
		Save(fc, file);
	}
}