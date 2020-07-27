package org.tbwork.anole.loader.util;

import java.io.*;
import java.nio.file.Path;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.tbwork.anole.loader.core.loader.impl.AnoleFileLoader;
import org.tbwork.anole.loader.enums.FileLoadStatus;
import org.tbwork.anole.loader.enums.OsCategory;
import org.tbwork.anole.loader.exceptions.BadJarFileException;


public class FileUtil {


	public static FileInputStream getInputStream(File file){

		try {

			return new FileInputStream(file);

		} catch (FileNotFoundException e) {

			throw new RuntimeException("Something bad happened!");
		}

	}



	/**
	 * @param patternedPath like : /D://prject/a.jar!/BOOT-INF!/classes!/*.properties
	 */
	private static Map<String, InputStream> loadFileStreamFromJar(String patternedPath){
		Map<String, InputStream> result = new HashMap<>();
		String jarPath = ProjectUtil.getJarPath(patternedPath)+"/";
		patternedPath = patternedPath.replace("!", "");
		String relativePatternedPath =patternedPath.replace(jarPath, "");
		JarFile file;
		try {
			file = new JarFile(jarPath);
		}
		catch(FileNotFoundException e) {
			return result;
		}
		catch(Exception e) {
			throw new BadJarFileException(jarPath);
		}
		Enumeration<JarEntry> entrys = file.entries();
		while(entrys.hasMoreElements()){
			JarEntry fileInJar = entrys.nextElement();
			String fileInJarName = fileInJar.getName();
			if(fileInJarName.endsWith(".jar")) {
				// It is another jar and the current search is not for project information.
				Map<String,InputStream> inputStreamsMap = getConfigInputStreamsFromJar(IOUtil.getInputStream(file,
						fileInJar), jarPath+fileInJarName, PathUtil.format2Slash(patternedPath));
				result.putAll(inputStreamsMap);
				continue;
			}
			if(PathUtil.asteriskMatch(PathUtil.format2Slash(relativePatternedPath), PathUtil.format2Slash(fileInJarName))){
				InputStream tempStream = IOUtil.getCopiedInputStream(file, fileInJar);
				String fullPath = jarPath+fileInJarName;
				result.put(fullPath, tempStream);
			}
		}
		try {
			file.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return result;
	}


	public static Map<String, File> loadFileByPatternedPath(String patternedPath){
		Map<String, File> result = new HashMap<>();
		if(!patternedPath.contains("*")){
			// SOLID PATH
			result.put(patternedPath, getFile(patternedPath));
			return result;
		}

		if( PathUtil.isFuzzyDirectory(patternedPath)){
			AnoleLogger.warn("Use asterisk in directory is not recommended, e.g., D://a/*/*.txt. We hope you know" +
					" that it will cause plenty cost of time to seek every matched file.");
		}

		String solidDirectoryPath = PathUtil.getSolidDirectory(patternedPath);
		File directory = new File(solidDirectoryPath);
		if(!directory.exists()) {
			AnoleLogger.warn("The directory ({}) is not existed.", solidDirectoryPath);
			return result;
		}
		List<File> files = FileUtil.getFilesInDirectoryWithSpecifiedPattern(solidDirectoryPath, patternedPath);

		for(File file : files){
			result.put(file.getAbsolutePath(), file);
		}
		return result;
	}

	private static List<File> getFilesInDirectoryWithSpecifiedPattern(String solidDirectory, String patternedPath){
		List<File> result = new ArrayList<File>();
		File file = new File(solidDirectory);
		File[] fileList = file.listFiles();
		for (int i = 0; i < fileList.length; i++) {
			File tempFile = fileList[i];
			if(tempFile.isDirectory()){
				if(PathUtil.asteriskPreMatch(patternedPath, tempFile.getAbsolutePath())){
					result.addAll(getFilesInDirectoryWithSpecifiedPattern(tempFile.getAbsolutePath(), patternedPath));
				}
			}
			else {
				if(PathUtil.asteriskMatch(patternedPath, tempFile.getAbsolutePath())){
					result.add(tempFile);
				}
			} 
		} 
		return result;
	}

	private static Map<String,InputStream> getConfigInputStreamsFromJar(InputStream jarFileInputStream,
																		String directoryName,
																		String relativePattern){
		Map<String,InputStream> result = new HashMap<String, InputStream>();
		try {
			ZipInputStream jarInputStream = new ZipInputStream(jarFileInputStream);
			ZipEntry zipEntry = null;
			while ((zipEntry = jarInputStream.getNextEntry()) != null) {
				String fileInZipName = zipEntry.getName();
				if(PathUtil.asteriskMatch(relativePattern, PathUtil.format2Slash(fileInZipName))){
					String fullPath = directoryName+"/"+fileInZipName;
					result.put(fullPath , IOUtil.getZipInputStream(jarInputStream, zipEntry));
				}
			}
		}
		catch(Exception e) {
			AnoleLogger.error("Fail to get configuration file from jar due to {}", e.getMessage());
		}
		return result;
	}


	private static File getFile(String filepath){
		File file = new File(filepath);
		if(file.exists()){
			return file;
		}
		return null;
	}

	private static InputStream newInputStream(String filepath){
		File file = new File(filepath);
		if(file.exists()){
			try {
				return new FileInputStream(file);
			} catch (FileNotFoundException e) {
				// never goes here
			}
		}
		return null;
	}

}
