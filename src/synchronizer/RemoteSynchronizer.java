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

public class RemoteSynchronizer {
	private WatchService watchservice = null;
	private HashMap<WatchKey,Path> key_to_path = null;
	private final Path src;
	private final String dest;	// 默认带后缀
	
	/**
	 * 
	 * @param src_str: local dir path
	 * @param dest_str: s3 dir path, either ends with "/" or empty string
	 * @throws IOException
	 */
	public RemoteSynchronizer(String src_str, String dest_str) throws IOException {
		watchservice = FileSystems.getDefault().newWatchService();
		key_to_path = new HashMap();
		
		if(src_str != null) {
			src = Paths.get(src_str);
			if(!src.toFile().isDirectory())
				throw new IOException("local file is not a directory or file doesn't exist");
		}
		else
			throw new IOException();
		if(dest_str != null) {
			dest = dest_str;
			if(!dest_str.endsWith("/") && dest_str.length()!= 0)
				throw new IOException("dest_str must end with '/'");
		}
		else 
			throw new IOException();
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
	
	/**
	 * 
	 * @param src_path: 输入一个完整的目录，输出一个  s3 中对应的目录路径
	 * @return
	 */
	public String getDest(Path src_path) {
		if(src_path.equals(src)) return dest;
		Path common_path = src_path.subpath(this.src.getNameCount(), src_path.getNameCount());
		String dest_path = dest + common_path.toString().replace("\\", "/") + "/";
		return dest_path;
	}
	
	public void listening() throws InterruptedException, IOException {
		while(true) {
			WatchKey watchkey = watchservice.take();
			for(WatchEvent<?> event : watchkey.pollEvents()) {
				Path parent = key_to_path.get(watchkey);
				Path src_path = parent.resolve((Path)event.context());
				String dest_prefix = getDest(src_path.getParent());
				if(event.kind() == StandardWatchEventKinds.ENTRY_CREATE) {
					registerWatchService(src_path);
					System.out.format("create new file 【%s】", src_path);
					
					S3Helper.uploadDir(src_path.getParent(), dest_prefix, src_path.getFileName().toString());
				}
				else if(event.kind() == StandardWatchEventKinds.ENTRY_DELETE) {
						S3Helper.deleteDir(dest_prefix+src_path.getFileName());
				}
				else if(event.kind() == StandardWatchEventKinds.ENTRY_MODIFY) {
					if(!src_path.toFile().isDirectory()) {
						S3Helper.upload(src_path,dest_prefix+src_path.getFileName());
						System.out.println("modify file "+ src_path);
					}
//					if(!file.isDirectory()) 
//						System.out.println("modify file " + src_path);
				}
			}
			boolean valid = watchkey.reset();
			if(!valid) {
//				System.out.println(key_to_path.get(watchservice)+"doesn't exist");
				key_to_path.remove(watchkey);
			}
		}
	}
	
	public void init() throws IOException, InterruptedException {
		S3Helper.init();
		
		for(File sub_file : src.toFile().listFiles())
			S3Helper.uploadDir(src, dest, sub_file.getName());
		registerWatchService(this.src);
		listening();
	}
}
