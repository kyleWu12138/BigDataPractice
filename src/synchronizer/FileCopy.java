package synchronizer;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.io.File;

public class FileCopy {
	/**
	 * @param src: The directory which all contents to be copied 
	 * 
	 * @param dest: dest has exists. After copied, there will be dest/(content of src, src not included)
	 */
	public static void fileCopy(Path src, Path dest) throws IOException{
		if(src == null) return;
		if(dest.toFile().exists()) {
			deleteDir(dest.toFile());
			System.out.print(dest+" exist");
		}
		Files.copy(src, dest);
		File[] sub_file = src.toFile().listFiles();
		if(sub_file == null)return;
		for(File f:sub_file) {
			File d = new File(dest+"\\"+f.getName());
			fileCopy(f.toPath(),d.toPath());
		}
	}
	
	public static void deleteDir(File file) {
		if(file == null) return;
		if(!file.isDirectory()) {
			file.delete();
			return;
		}
		for(File sub_file : file.listFiles()) {
			deleteDir(sub_file);
		}
		file.delete();
	}
}

