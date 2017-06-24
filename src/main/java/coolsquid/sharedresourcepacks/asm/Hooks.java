package coolsquid.sharedresourcepacks.asm;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.resources.ResourcePackRepository;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

public class Hooks {

	public static void updateRepositoryEntriesAll(ResourcePackRepository repo) throws NoSuchMethodException, SecurityException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, IOException {
		File cfgFile = new File("./config/Shared Resource Packs.txt");
		ArrayList<File> files = new ArrayList<File>();
		if (cfgFile.exists()) {
			BufferedReader r = new BufferedReader(new InputStreamReader(new FileInputStream(cfgFile)));
			String line = r.readLine();
			while (line != null) {
				if (!line.startsWith("#")) {
					files.add(new File(line));
				}
				line = r.readLine();
			}
			r.close();
		} else {
			cfgFile.getParentFile().mkdirs();
			cfgFile.createNewFile();
			BufferedWriter w = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(cfgFile)));
			w.write("# List all directories to load here");
			w.newLine();
			w.write(System.getProperty("user.home") + "/Shared Resource Packs");
			w.close();
		}
		List<ResourcePackRepository.Entry> entries = ReflectionHelper.getPrivateValue(ResourcePackRepository.class, repo, 11);
		Constructor<ResourcePackRepository.Entry> c = ResourcePackRepository.Entry.class.getDeclaredConstructor(ResourcePackRepository.class, File.class);
		c.setAccessible(true);
		for (File dir: files) {
			if (dir.exists()) {
				if (dir.isDirectory()) {
					for (File file: dir.listFiles((f, n) -> n.endsWith(".zip"))) {
						ResourcePackRepository.Entry i = c.newInstance(repo, file);
						i.updateResourcePack();
						entries.add(i);
						i.closeResourcePack();
					}
				} else {
					ResourcePackRepository.Entry i = c.newInstance(repo, dir);
					i.updateResourcePack();
					entries.add(i);
					i.closeResourcePack();
				}
			} else {
				System.out.println(String.format("[Shared Resource Packs] Could not find '%s'", dir.getAbsolutePath()));
			}
		}
	}
}