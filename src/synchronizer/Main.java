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
		String src = "D:\\Desktop\\src3";
		String dest = "src3/";
		RemoteSynchronizer s = new RemoteSynchronizer(src,dest);
		s.init();
	}
}
