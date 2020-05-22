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


public class Main {
	public static void main(String args[]) throws IOException, InterruptedException {
//		String src = "D:\\Desktop\\src";
//		String dest = "D:\\Desktop\\src3";
//		
//		Synchronizer s = new Synchronizer(src,dest);
//		s.init();
		
		Path p1 = Paths.get("D:\\Desktop\\src");
		Path p2 = Paths.get("D:\\Desktop\\src\\新建文件夹");
		WatchService watchservice = FileSystems.getDefault().newWatchService();
		WatchKey k1 = p1.register(watchservice, StandardWatchEventKinds.ENTRY_CREATE);
		WatchKey k2 = p2.register(watchservice, StandardWatchEventKinds.ENTRY_CREATE,
				StandardWatchEventKinds.ENTRY_DELETE,
				StandardWatchEventKinds.ENTRY_MODIFY);
		
		if(k1 == k2) {
			System.out.println("==");
		}
		if(k1.equals(k2)) {
			System.out.println("k1 equals k2");
		}
		
		
		while(true) {
			System.out.println("readying...");
			WatchKey key = watchservice.take();
			
			System.out.println(key.toString());
			for(WatchEvent<?> event : key.pollEvents())
			if(event.kind() == StandardWatchEventKinds.ENTRY_CREATE) {
				System.out.println("create new file ");
			}
			else if(event.kind() == StandardWatchEventKinds.ENTRY_DELETE) {
				System.out.println("delete file ");
			}
			else if(event.kind() == StandardWatchEventKinds.ENTRY_MODIFY) {
				System.out.println("modify file ");
			}
			boolean valid = key.reset();
			if(!valid) {
				System.out.println("invalid");
			}else {
				System.out.println("valid");
			}
		}
	}
}
