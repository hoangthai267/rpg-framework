package com.rpg.framework.core;

import java.io.IOException;
import java.io.PrintStream;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Debugger {
	private Path folderPath = null;
	private Path filePath = null;
	private PrintStream printStream = null;

	public Debugger() {
		String workingDir = System.getProperty("user.dir") + "\\log";

		folderPath = Paths.get(workingDir);

		try {
			if (!Files.exists(folderPath)) {
				Files.createDirectory(Paths.get(workingDir));
			} else {
				filePath = Files.createTempFile(folderPath, null, "");
				printStream = new PrintStream(filePath.toFile());
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
		
		printStream.print("Create time: ");
		printStream.println(dtf.format(LocalDateTime.now()));
	}
	
	public void WriteLog(String message) {
		printStream.print(message);
	}

	public void WritelnLog(String message) {
		printStream.println(message);
	}

	public void ShowLog(String message) {
		System.out.println(message);
	}

	private static Debugger instance;

	private static Debugger GetInstance() {
		if (instance == null)
			instance = new Debugger();
		return instance;
	}

	public static void Log(String message) {
		GetInstance().ShowLog(message);
	}
	
	public static void Write(String message) {
		GetInstance().ShowLog(message);
	}
	
	public static void Writeln(String message) {
		GetInstance().ShowLog(message);
	}
	
	public static void WriteException(Exception ex) {
		ex.printStackTrace();
	}
}
