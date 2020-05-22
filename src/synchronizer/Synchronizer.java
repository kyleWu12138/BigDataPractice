package synchronizer;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.HashMap;

public class Synchronizer {
	private WatchService watchservice = null;
	private HashMap<WatchKey,Path> key_to_path = null;
	final private Path src;
	final private Path dest;
	
	public Synchronizer(String src_str, String dest_str) throws IOException {
		watchservice = FileSystems.getDefault().newWatchService();
		key_to_path = new HashMap();
		if(src_str != null)
			src = Paths.get(src_str);
		else src = null;
		
		if(dest_str != null)
			dest = Paths.get(dest_str);
		else dest = null;
	}
	
	// 为 path 及其下所有目录和文件登记
	public void registerWatchService(Path path) throws IOException {

		File f = path.toFile();
		if(f == null || !f.isDirectory()) return;
		
		WatchKey key = path.register(watchservice, 
									StandardWatchEventKinds.ENTRY_CREATE,
						 			StandardWatchEventKinds.ENTRY_DELETE,
						 			StandardWatchEventKinds.ENTRY_MODIFY);
		key_to_path.put(key,path);

		for(File sub_file : f.listFiles()) {
				registerWatchService(sub_file.toPath());
		}
	}
	
	public Path getDest(Path src_path) {
		Path common_path = src_path.subpath(this.src.getNameCount(), src_path.getNameCount());
		Path dest_path = dest.resolve(common_path);
		return dest_path;
	}
	
	public void listening() throws InterruptedException, IOException {
		while(true) {
			WatchKey watchkey = watchservice.take();
			for(WatchEvent<?> event : watchkey.pollEvents()) {
				Path parent = key_to_path.get(watchkey);
				Path src_path = parent.resolve((Path)event.context());
				File file = src_path.toFile();
				
				if(event.kind() == StandardWatchEventKinds.ENTRY_CREATE) {
					registerWatchService(src_path);
					System.out.println("create new file "+ src_path);
					Path dest_path = getDest(src_path);
					FileCopy.fileCopy(src_path, dest_path);
				}
				else if(event.kind() == StandardWatchEventKinds.ENTRY_DELETE) {
					System.out.println("delete file " + src_path);
					Path dest_path = getDest(src_path);
					FileCopy.deleteDir(dest_path.toFile());
				}
				else if(event.kind() == StandardWatchEventKinds.ENTRY_MODIFY) {
					if(!file.isDirectory())
						System.out.println("modify file " + src_path);
				}
			}
			boolean valid = watchkey.reset();
			if(!valid) {
				System.out.println(key_to_path.get(watchservice)+"doesn't exist");
				key_to_path.remove(watchkey);
			}
		}
	}
	
	public void init() throws IOException, InterruptedException {
		FileCopy.fileCopy(src, dest);
		registerWatchService(this.src);
		listening();
		
	}
}
