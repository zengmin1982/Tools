package com.zengmin.tools;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;

import org.apache.log4j.Logger;

/**
 * 文件相关
 * 
 * @author 作者:pangkc
 * @version 创建时间：2011-12-29
 * 
 */
public final class FileUtil {
	private static final Logger logger = Logger.getLogger(FileUtil.class);

	private FileUtil() {
	}

	/**
	 * 保存文件
	 * 
	 * @param path
	 * @param stream
	 */
	public static void save(String path, String fileName, InputStream stream) {

	}

	public static boolean save(String path, String fileName, String saveContent) {
		FileUtil.createNewFolder(path, false);
		File file = new File(path + fileName);
		Writer writer = null;
		try {
			writer = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(file, true), "UTF-8"));
			writer.write(saveContent);
			writer.flush();
			writer.close();
			return true;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * 删除文件
	 * 
	 * @param file
	 * @param completely
	 * @return
	 */
	public static boolean delete(final File file, final boolean completely) {

		if (file.isDirectory()) {
			for (File i : file.listFiles()) {
				if (!delete(i, true)) {
					return false;
				}
			}

		}

		if (completely && file.exists()) {
			return file.delete();
		}

		return true;
	}

	/**
	 * 指定文件名删除
	 * 
	 * @param file
	 * @param completely
	 * @return
	 */
	public static boolean delete(final String file, final boolean completely) {

		return delete(new File(file), completely);
	}

	/**
	 * 复制文件
	 * 
	 * @param inputfile
	 * @param outputfile
	 */
	public static void copyFile(String inputfile, String outputfile) {
	}

	/**
	 * 文件是否存在
	 * 
	 * @param targetFileName
	 * @return
	 */
	public static boolean fileExistsChk(String targetFileName) {

		File targetFile = new File(targetFileName);
		if (targetFile.isFile()) {
			return true;
		} else {
			return false;
		}
	}

	public static boolean move(File file, String destPath) {
		createNewFolder(destPath, false);
		File dir = new File(destPath);
		boolean success = file.renameTo(new File(dir, file.getName()));
		delete(file, true);
		return success;
	}

	/**
	 * 创建文件夹
	 * 
	 * @param folderPath
	 * @param isForced
	 *            true先删后建
	 */
	public static void createNewFolder(String folderPath, boolean isForced) {

		File resultFolder = new File(folderPath);
		if (resultFolder.exists()) {

			if (isForced) {
				delete(resultFolder, true);
				resultFolder.mkdir();
			}
		} else {
			resultFolder.mkdirs();
		}

	}

	/**
	 * 获得文件的编码类型 add by zzm 20120131
	 * 
	 * @param filePath
	 * @return
	 */
	/*
	 * public String getFileCharacterEnding(URL r) { String fileCharacterEnding
	 * = "UTF-8"; CodepageDetectorProxy detector =
	 * CodepageDetectorProxy.getInstance();
	 * detector.add(JChardetFacade.getInstance()); Charset charset = null;
	 * 
	 * File file = new File(filePath);
	 * 
	 * try { charset = detector.detectCodepage(r); } catch (Exception e) {
	 * e.printStackTrace(); } if (charset != null) { fileCharacterEnding =
	 * charset.name(); } return fileCharacterEnding; }
	 */
	/**
	 * 获取文件编码类型
	 * 
	 * @param in
	 * @param length
	 * @return
	 */
	/*
	 * public String getFileCharacterEnding(InputStream in, int length) { String
	 * fileCharacterEnding = "UTF-8"; CodepageDetectorProxy detector =
	 * CodepageDetectorProxy.getInstance();
	 * detector.add(JChardetFacade.getInstance()); Charset charset = null; try {
	 * charset = detector.detectCodepage(in,length); } catch (Exception e) {
	 * e.printStackTrace(); } if (charset != null) { fileCharacterEnding =
	 * charset.name(); } return fileCharacterEnding; }
	 */

	/**
	 * 获取文件的内容
	 */
	public static String getFileContent(String file, String encode) {
		if (!FileUtil.fileExistsChk(file)) {
			return null;
		}
		StringBuilder result = new StringBuilder();
		BufferedReader bf = null;
		String line = null;
		try {
			bf = new BufferedReader(new InputStreamReader(new FileInputStream(
					file), encode));
			line = bf.readLine();
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		while (line != null) {
			result.append(line).append("\n");
			try {
				line = bf.readLine();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		String re = result.toString();
		if (re.length() != 0) {
			re = re.toString().substring(0, result.length() - 1);
		}
		return re;
	}

}
